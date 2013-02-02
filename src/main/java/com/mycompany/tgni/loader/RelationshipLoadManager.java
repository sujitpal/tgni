package com.mycompany.tgni.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mycompany.tgni.beans.TRelTypes;
import com.mycompany.tgni.beans.TRelation;
import com.mycompany.tgni.utils.DbUtils;
import com.mycompany.tgni.utils.GraphInstance;

/**
 * TODO: class level javadocs
 */
public class RelationshipLoadManager {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  private static final int NUM_WORKERS = 
    Runtime.getRuntime().availableProcessors();
  private static final CountDownLatch LATCH = 
    new CountDownLatch(NUM_WORKERS);

  private static final String GET_OIDS_SQL = 
    "select oid from oid_nid";
  private static final String GET_RELS_SQL =
    "select concept_id, rconcept_id, rela_id, " +
    "rconcept_rank, auto_rconcept_rank_1, bnr, " +
    "concept_rank, auto_reverse_rank " +
    "from concept_rel";
  private static final String GET_NID_FROM_OID_SQL =
    "select nid from oid_nid where oid = ?";

  public static void main(String[] args) throws Exception {
    // extract parameters from command line
    if (args.length != 3) {
      System.out.println("Usage: RelationshipLoadManager " +
        "/path/to/graph/dir /path/to/mysql-properties " +
        "/path/to/oracle-properties");
      System.exit(-1);
    }
    // initialize manager
    RelationshipLoadManager manager = new RelationshipLoadManager();
    final String mysqlProps = args[1];
    final String oraProps = args[2];
    // seed input queue
    Set<Integer> validOids = manager.getValidOids(mysqlProps);
    final BlockingQueue<TRelation> queue = 
      new LinkedBlockingQueue<TRelation>();
    manager.seed(queue, oraProps, validOids);
    // add poison pills
    for (int i = 0; i < NUM_WORKERS; i++) {
      TRelation stop = new TRelation();
      stop.setFromOid(-1);
      stop.setToOid(-1);
      queue.put(stop);
    }
    // set up worker threads
    ExecutorService workerPool = Executors.newFixedThreadPool(NUM_WORKERS);
    for (int i = 0; i < NUM_WORKERS; i++) {
      RelationshipLoadWorker worker =
        new RelationshipLoadManager().new RelationshipLoadWorker(
        i, queue, mysqlProps);
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

  private Set<Integer> getValidOids(String mysqlProps) 
      throws Exception {
    Set<Integer> validOids = new HashSet<Integer>();
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      conn = DbUtils.getConnection(mysqlProps);
      ps = conn.prepareStatement(GET_OIDS_SQL);
      rs = ps.executeQuery();
      while (rs.next()) {
        validOids.add(rs.getInt(1));
      }
    } finally {
      DbUtils.closeResultSet(rs);
      DbUtils.closePreparedStatement(ps);
      DbUtils.closeConnection(conn);
    }
    return validOids;
  }

  private void seed(BlockingQueue<TRelation> queue,
      String oraProps, Set<Integer> validOids) {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      conn = DbUtils.getConnection(oraProps);
      ps = conn.prepareStatement(GET_RELS_SQL);
      rs = ps.executeQuery();
      while (rs.next()) {
        Integer fromOid = rs.getBigDecimal(1).intValue();
        Integer toOid = rs.getBigDecimal(2).intValue();
        if (!(validOids.contains(fromOid) &&
            validOids.contains(toOid))) {
          continue;
        }
        TRelation rel = new TRelation();
        rel.setFromOid(fromOid);
        rel.setToOid(toOid);
        rel.setRelType(
          TRelTypes.fromOid(rs.getBigDecimal(3).intValue()));
        rel.setMrank(rs.getBigDecimal(4).longValue());
        rel.setArank(rs.getBigDecimal(5).longValue());
        rel.setMstip("1".equals(rs.getString(6)));
        long revMRank = rs.getBigDecimal(7).longValue();
        if (revMRank > 0L) {
          rel.setRmrank(revMRank);
        } else {
          rel.setRmrank(rel.getMrank());
        }
        long revARank = rs.getBigDecimal(7).longValue();
        if (revARank > 0L) {
          rel.setRarank(revARank);
        } else {
          rel.setRarank(rel.getArank());
        }
        queue.add(rel);
      }
    } catch (Exception e) {
      logger.warn("Can't generate seed relations", e);
    } finally {
      DbUtils.closeResultSet(rs);
      DbUtils.closePreparedStatement(ps);
      DbUtils.closeConnection(conn);
    }
  }

  /////////////////// RelationshipLoadWorker class //////////////////
  
  private class RelationshipLoadWorker implements Runnable {

    private int workerId;
    private BlockingQueue<TRelation> queue;
    private String mysqlProps;
    
    private AtomicInteger count;
    private int totalTasks;
    private Connection conn;
    private PreparedStatement psGetNid;
    
    public RelationshipLoadWorker(int workerId,
        BlockingQueue<TRelation> queue, String mysqlProps) { 
      this.workerId = workerId;
      this.queue = queue;
      this.count = new AtomicInteger(0);
      this.totalTasks = queue.size();
      this.mysqlProps = mysqlProps;
    }
    
    @Override
    public void run() {
      try {
        initWorker();
        count = new AtomicInteger(0);
        for (;;) {
          TRelation rel = queue.take();
          if (rel.getFromOid() == -1 &&
              rel.getToOid() == -1) {
            break;
          }
          int curr = count.incrementAndGet();
          if (curr % 100 == 0) {
            logger.info("Worker " + workerId + " processed (" + curr +
              "/" + totalTasks + ") OID pairs");
          }
          GraphDatabaseService graphService = GraphInstance.getInstance();
          Transaction tx = graphService.beginTx();
          try {
            Long fromNodeId = getNidFromOid(rel.getFromOid());
            Long toNodeId = getNidFromOid(rel.getToOid());
            if (fromNodeId == -1L || toNodeId == -1) {
              logger.warn("Can't find node IDs for (" + rel.getFromOid() + 
                "," + rel.getToOid() + "), skipping");
              continue;
            }
            Node fromNode = graphService.getNodeById(fromNodeId);
            Node toNode = graphService.getNodeById(toNodeId);
            TRelTypes relType = rel.getRelType();
            Relationship relationship = 
              fromNode.createRelationshipTo(toNode, relType);
            relationship.setProperty("mrank", rel.getMrank());
            relationship.setProperty("arank", rel.getArank());
            relationship.setProperty("mstip", rel.getMstip());
            if (relType.reversible) {
              TRelTypes reverseRelType = 
                TRelTypes.fromOid(relType.oid * -1);
              Relationship reverseRelationship = 
                toNode.createRelationshipTo(fromNode, reverseRelType);
              reverseRelationship.setProperty("mrank", rel.getRmrank());
              reverseRelationship.setProperty("arank", rel.getRarank());
              reverseRelationship.setProperty("mstip", rel.getMstip());
            }
            tx.success();
          } catch (Exception e) {
            tx.failure();
            logger.info("Exception inserting relationship: (" + 
              rel.getFromOid() + "," + rel.getToOid() + 
              "), skipping", e);
            continue;
          } finally {
            tx.finish();
          }
        }
      } catch (Exception e) {
        logger.error("Worker " + workerId + " threw exception", e);
      } finally {
        destroyWorker();
        LATCH.countDown();
        logger.info("Worker " + workerId + " counting down");
      }
    }
    
    private void initWorker() throws Exception {
      conn = DbUtils.getConnection(mysqlProps);
      psGetNid = conn.prepareStatement(GET_NID_FROM_OID_SQL);
    }
    
    private void destroyWorker() {
      DbUtils.closePreparedStatement(psGetNid);
      DbUtils.closeConnection(conn);
    }
    
    public Long getNidFromOid(Integer oid) {
      Long nid = -1L;
      ResultSet rs = null;
      try {
        psGetNid.setInt(1, oid);
        rs = psGetNid.executeQuery();
        while (rs.next()) {
          nid = rs.getLong(1);
          break;
        }
      } catch (SQLException e) {
        logger.warn("No NID found for OID: " + oid, e);
      } finally {
        DbUtils.closeResultSet(rs);
      }
      return nid;
    }
  }
}
