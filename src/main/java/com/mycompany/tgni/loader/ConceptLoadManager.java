package com.mycompany.tgni.loader;

import java.io.File;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import opennlp.tools.util.Pair;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.util.StopWatch;

import com.mycompany.tgni.analysis.lucene.StopFilter;
import com.mycompany.tgni.beans.TConcept;
import com.mycompany.tgni.services.NameNormalizer;
import com.mycompany.tgni.utils.DbUtils;
import com.mycompany.tgni.utils.GraphInstance;
import com.mycompany.tgni.utils.JsonUtils;
import com.mycompany.tgni.utils.UimaUtils;

/**
 * TODO: class level javadocs
 */
public class ConceptLoadManager {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  private static final int NUM_WORKERS =
    // was 1.4
    Math.round(2.5F * Runtime.getRuntime().availableProcessors());
  private static final long TASK_TIMEOUT_MILLIS = 1000L;
//  private static final int NUM_TASKS = 1000;
  private static final CountDownLatch LATCH = new CountDownLatch(NUM_WORKERS);
  private static final BlockingQueue<Integer> QUEUE = new LinkedBlockingQueue<Integer>();

  // oracle queries
  private static final String LIST_OIDS_SQL = 
    "select concept_id from concept where t_id = 1 " +
    "minus (" +
    "select concept_id from concept_retired " +
    "union " + 
    "select concept_id from dent where ud_id = 10)";
  private static final String GET_HEAD_SQL =
    "select t_id, google_article_count_rank, " +
    "google_adwords_rank " +
    "from concept " +
    "where concept_id = ?";
  private static final String GET_PNAMES_SQL = 
    "select display_type_id, display " +
    "from concept_display " +
    "where display_type_id in (1,2) " +
    "and concept_id = ?";
  private static final String GET_SYNS_SQL =
    "select str from concept_synonyms " +
    "where concept_id = ?";
  private static final String GET_STY_SQL = 
    "select s.sty_name, s.source_code, s.sty_group " +
    "from concept_sty cs, sty s " +
    "where cs.sty_id = s.sty_id " +
    "and cs.concept_id = ?";
  // mysql queries
  private static final String ADD_NAME_SQL = 
    "insert into oid_name (" +
    "oid, name, pri) " +
    "values (?,?,?)";
  private static final String ADD_NID_SQL =
    "insert into oid_nid (oid, nid) values (?, ?)";

  public static void main(String[] args) throws Exception {
    // extract parameters from command line
    if (args.length != 5) {
      System.out.println("Usage: ConceptLoadManager " +
        "/path/to/graph/dir /path/to/mysql-properties " +
        "/path/to/stopwords/file /path/to/ae/descriptor " +
        "/path/to/oracle-properties");
      System.exit(-1);
    }

    // Initialize manager
    ConceptLoadManager manager = new ConceptLoadManager();
    final String mysqlProps = args[1];
    final Set<?> stopwords = StopFilter.makeStopSet(
        Version.LUCENE_40, new File(args[2]));
    final String aeDescriptor = args[3];
    final String oraProps = args[4];

    // seed input queue
    manager.seed(oraProps);
    // add poison pills
    for (int i = 0; i < NUM_WORKERS; i++) {
      try {
        QUEUE.put(-1);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    // set up worker threads
    ExecutorService workerPool = Executors.newFixedThreadPool(NUM_WORKERS);
    for (int i = 0; i < NUM_WORKERS; i++) {
      ConceptLoadWorker worker = 
        new ConceptLoadManager().new ConceptLoadWorker(
          i, mysqlProps, stopwords, aeDescriptor, 
          oraProps);
      workerPool.execute(worker);
    }
    
    // wait for all tasks to process, then shutdown
    workerPool.shutdown();
    try {
      LATCH.await();
    } catch (InterruptedException e) { /* NOOP */ }
    GraphInstance.destroy();
    workerPool.awaitTermination(1000L, TimeUnit.MILLISECONDS);
  }

  private void seed(String oraProps) {
    List<Integer> oids = new ArrayList<Integer>();
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      conn = DbUtils.getConnection(oraProps);
      ps = conn.prepareStatement(LIST_OIDS_SQL);
      rs = ps.executeQuery();
//      int i = 0; // TODO: remove later
      while (rs.next()) {
        QUEUE.put(rs.getInt(1));
//        if (i > NUM_TASKS) {
//          break;
//        }
//        i++;
      }
    } catch (Exception e) {
      logger.warn("Can't generate OIDs to process", e);
    } finally {
      DbUtils.closeResultSet(rs);
      DbUtils.closePreparedStatement(ps);
      DbUtils.closeConnection(conn);
    }
  }

  /////////////// Worker Class ///////////////////
  
  private class ConceptLoadWorker implements Runnable {
    private int workerId;
    private AtomicInteger count;
    private int totalTasks;
    private Set<?> stopwords;
    private String mysqlProps;
    private String aeDescriptor;
    private String oraProps;
    
    private Connection mysqlConn;
    private PreparedStatement psAddNames, psAddNid;
    private Connection oraConn;
    private PreparedStatement psGetHead, psGetNames, psGetSyns, psGetSty; 
    private AnalysisEngine ae;
    private JCas jcas;
    private Analyzer analyzer;

    public ConceptLoadWorker(int workerId, String mysqlProps,
        Set<?> stopwords, String aeDescriptor, 
        String oraProps) {
      this.workerId = workerId;
      this.count = new AtomicInteger(0);
      this.totalTasks = QUEUE.size();
      this.mysqlProps = mysqlProps;
      this.stopwords = stopwords;
      this.aeDescriptor = aeDescriptor;
      this.oraProps = oraProps;
    }
    
    @Override
    public void run() {
      try {
        initWorker();
        ExecutorService taskExec = Executors.newSingleThreadExecutor();
//        StopWatch watch = new StopWatch();
        for (;;) {
          Integer oid = QUEUE.take();
          if (oid < 0) {
            break;
          }
          int curr = count.incrementAndGet();
          // load the concept by OID from oracle
//          watch.start("get_concept");
          TConcept concept = null;
          try {
            concept = loadConcept(oid);
          } catch (SQLException e) {
            logger.warn("Exception retrieving concet (OID:" + 
              oid + ")", e);
//            watch.stop();
            continue;
          }
//          watch.stop();
          // normalize names using UIMA/Lucene chains. This is
          // a slow process so we want to time this out if it
          // takes too long. In that case, the node/oid mapping
          // will not be written out into Neo4J.
//          watch.start("norm_names");
          NameNormalizer normalizer = new NameNormalizer(ae, analyzer, jcas);
          NameNormalizerTask task = new NameNormalizerTask(
            concept, normalizer);
          Future<List<Pair<String,Boolean>>> futureResult = 
            taskExec.submit(task);
          List<Pair<String,Boolean>> result = null;
          try {
            result = futureResult.get(TASK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
          } catch (ExecutionException e) {
            logger.warn("Task (OID:" + oid + ") skipped", e);
            reinitWorker();
//            watch.stop();
            continue;
          } catch (TimeoutException e) {
            futureResult.cancel(true);
            logger.warn("Task (OID:" + oid + ") timed out", e);
            reinitWorker();
//            watch.stop();
            continue;
          }
//          watch.stop();
//          watch.start("write_concept");
          try {
            // add the OID-Name mappings to MySQL
            addNames(oid, result);
            // add the OID-NID mapping to Neo4j
            writeNodeConceptMapping(concept);
          } catch (Exception e) {
            logger.warn("Exception persisting concept (OID:" + oid + 
              ")", e);
//            watch.stop();
            continue;
          }
//          watch.stop();
          // report on progress
          if (curr % 100 == 0) {
            logger.info("Worker " + workerId + " processed (" + curr + 
              "/" + totalTasks + ") OIDs");
          }
        }
        taskExec.shutdownNow();
//        logger.info("STOPWATCH:" + watch.prettyPrint());
      } catch (InterruptedException e) {
        logger.error("Worker:" + workerId + " Interrupted", e);
      } catch (Exception e) {
        logger.error("Worker:" + workerId + " threw exception", e);
      } finally {
        destroyWorker();
        LATCH.countDown();
        logger.info("Worker:" + workerId + " counting down, current=" + LATCH.getCount());
      }
    }

    private TConcept loadConcept(Integer oid) throws SQLException {
      TConcept concept = new TConcept();
      concept.setOid(oid);
      BigDecimal bgOid = new BigDecimal(concept.getOid());
      psGetHead.setBigDecimal(1, bgOid);
      ResultSet rsHead = psGetHead.executeQuery();
      while (rsHead.next()) {
        concept.setTid(rsHead.getBigDecimal(1).intValue());
        concept.setArank(rsHead.getBigDecimal(2).longValue());
        concept.setMrank(rsHead.getBigDecimal(3).longValue());
        break;
      }
      DbUtils.closeResultSet(rsHead);

      // get pname and qname
      psGetNames.setBigDecimal(1, bgOid);
      ResultSet rsNames = psGetNames.executeQuery();
      while (rsNames.next()) {
        int displayTypeId = rsNames.getBigDecimal(1).intValue();
        if (displayTypeId == 1) {
          concept.setPname(rsNames.getString(2));
        } else if (displayTypeId == 2) {
          concept.setQname(rsNames.getString(2));
        }
      }
      DbUtils.closeResultSet(rsNames);

      // get concept synonyms
      psGetSyns.setBigDecimal(1, bgOid);
      ResultSet rsSyns = psGetSyns.executeQuery();
      List<String> syns = new ArrayList<String>();
      while (rsSyns.next()) {
        syns.add(rsSyns.getString(1));
      }
      concept.setSynonyms(syns);
      DbUtils.closeResultSet(rsSyns);

      // get classification codes
      psGetSty.setBigDecimal(1, bgOid);
      ResultSet rsSty = psGetSty.executeQuery();
      Map<String,String> stycodes = new HashMap<String,String>();
      boolean firstResult = true;
      while (rsSty.next()) {
        String styname = rsSty.getString(1);
        String stycode = rsSty.getString(2);
        stycodes.put(stycode, styname);
        if (firstResult) {
          concept.setStygrp(rsSty.getString(3));
          firstResult = false;
        }
      }
      concept.setStycodes(stycodes);
      DbUtils.closeResultSet(rsSty);
      return concept;
    }

    private void addNames(Integer oid,
        List<Pair<String, Boolean>> names) 
        throws SQLException {
      if (names == null) return;
      try {
        psAddNames.clearBatch();
        for (Pair<String,Boolean> name : names) {
          if (StringUtils.length(StringUtils.trim(name.a)) > 255) {
            continue;
          }
          psAddNames.setInt(1, oid);
          psAddNames.setString(2, name.a);
          psAddNames.setString(3, name.b ? "T" : "F");
          psAddNames.addBatch();
        }
        psAddNames.executeBatch();
        mysqlConn.commit();
      } catch (SQLException e) {
        mysqlConn.rollback();
        throw e;
      }
    }

    private void writeNodeConceptMapping(TConcept concept) 
        throws Exception {
      logger.info("Writing concept (OID=" + concept.getOid() + ")");
      GraphDatabaseService graphService = GraphInstance.getInstance();
      Transaction tx = graphService.beginTx();
      try {
        // update neo4j
        Node node = graphService.createNode();
        concept.setNid(node.getId());
        node.setProperty("oid", concept.getOid());
        node.setProperty("pname", concept.getPname());
        node.setProperty("qname", concept.getQname());
        node.setProperty("synonyms", 
          JsonUtils.listToString(concept.getSynonyms())); 
        node.setProperty("stycodes", 
          JsonUtils.mapToString(concept.getStycodes())); 
        node.setProperty("stygrp", StringUtils.isEmpty(
          concept.getStygrp()) ? "UNKNOWN" : concept.getStygrp());
        node.setProperty("mrank", concept.getMrank());
        node.setProperty("arank", concept.getArank());
        node.setProperty("tid", concept.getTid());
        // update mysql
        psAddNid.setInt(1, concept.getOid());
        psAddNid.setLong(2, concept.getNid());
        psAddNid.executeUpdate();
        mysqlConn.commit();
        tx.success();
      } catch (Exception e) {
        mysqlConn.rollback();
        tx.failure();
        logger.info("Exception writing mapping (OID=" + 
          concept.getOid() + ")");
        throw e;
      } finally {
        tx.finish();
      }
    }

    private void initWorker() throws Exception {
      logger.info("Worker:" + workerId + " init");
      // mysql
      this.mysqlConn = DbUtils.getConnection(mysqlProps);
      this.mysqlConn.setAutoCommit(false);
      this.psAddNames = mysqlConn.prepareStatement(ADD_NAME_SQL);
      this.psAddNid = mysqlConn.prepareStatement(ADD_NID_SQL);
      // oracle
      this.oraConn = DbUtils.getConnection(oraProps);
      this.psGetHead = oraConn.prepareStatement(GET_HEAD_SQL);
      this.psGetNames = oraConn.prepareStatement(GET_PNAMES_SQL);
      this.psGetSyns = oraConn.prepareStatement(GET_SYNS_SQL);
      this.psGetSty = oraConn.prepareStatement(GET_STY_SQL);
      // uima/lucene
      this.ae = UimaUtils.getAE(aeDescriptor, null);
      this.analyzer = getAnalyzer(stopwords);
      this.jcas = ae.newJCas();
    }

    private void destroyWorker() {
      logger.info("LC: Worker:" + workerId + " destroy");
      // mysql
      DbUtils.closePreparedStatement(psAddNames);
      DbUtils.closePreparedStatement(psAddNid);
      DbUtils.closeConnection(this.mysqlConn);
      // oracle
      DbUtils.closePreparedStatement(psGetHead);
      DbUtils.closePreparedStatement(psGetNames);
      DbUtils.closePreparedStatement(psGetSyns);
      DbUtils.closePreparedStatement(psGetSty);
      DbUtils.closeConnection(this.oraConn);
      // uima/lucene
      this.ae.destroy();
      this.analyzer.close();
      this.jcas.release();
      this.jcas.reset();
    }

    private void reinitWorker() throws Exception {
      logger.info("LC: Worker:" + workerId + " reinit resources");
      this.ae.destroy();
      this.analyzer.close();
      this.jcas.release();
      this.jcas.reset();
      this.ae = UimaUtils.getAE(aeDescriptor, null);
      this.analyzer = getAnalyzer(stopwords);
      this.jcas = ae.newJCas();
    }
    
    private Analyzer getAnalyzer(final Set<?> stopwords) {
      return new Analyzer() {
        @Override
        public TokenStream tokenStream(String fieldName, Reader reader) {
          TokenStream input = new StandardTokenizer(Version.LUCENE_40, reader);
          input = new LowerCaseFilter(Version.LUCENE_40, input);
          input = new StopFilter(Version.LUCENE_40, input, stopwords);;
          input = new PorterStemFilter(input);
          return input;
        }
      };
    }
  }

  ///////////////// Task class ////////////////
  
  private class NameNormalizerTask implements Callable<List<Pair<String,Boolean>>> {

    private TConcept concept;
    private NameNormalizer normalizer;

    public NameNormalizerTask(TConcept concept, NameNormalizer normalizer) {
      this.concept = concept;
      this.normalizer = normalizer;
    }
    
    @Override
    public List<Pair<String,Boolean>> call() throws Exception {
      logger.info("Executing task (OID:" + concept.getOid() + ")");
      Set<String> uniques = new HashSet<String>();
      Set<String> normalizedUniques = new HashSet<String>();
      List<Pair<String,Boolean>> results = new ArrayList<Pair<String,Boolean>>();
      String pname = concept.getPname();
      if (StringUtils.isNotEmpty(pname) &&
          (! uniques.contains(pname))) {
        List<String> normalized = normalizer.normalize(pname);
        uniques.add(pname);
        normalizedUniques.addAll(normalized);
      }
      String qname = concept.getQname();
      if (StringUtils.isNotEmpty(qname) &&
          (! uniques.contains(qname))) {
        List<String> normalized = normalizer.normalize(qname);
        uniques.add(qname);
        normalizedUniques.addAll(normalized);
      }
      for (String normalizedUnique : normalizedUniques) {
        results.add(new Pair<String,Boolean>(normalizedUnique, true));
      }
      Set<String> normalizedUniqueSyns = new HashSet<String>();
      normalizedUniqueSyns.addAll(normalizedUniques);
      List<String> syns = concept.getSynonyms();
      for (String syn : syns) {
        if (StringUtils.isNotEmpty(syn) && 
            (! uniques.contains(syn))) {
          List<String> normalizedSyn = normalizer.normalize(syn);
          uniques.add(syn);
          normalizedUniqueSyns.addAll(normalizedSyn);
        }
      }
      Collection<String> normalizedSyns = CollectionUtils.subtract(
        normalizedUniques, normalizedUniqueSyns);
      for (String normalizedSyn : normalizedSyns) {
        results.add(new Pair<String,Boolean>(normalizedSyn, false));
      }
      return results;
    }
  }
}
