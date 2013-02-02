package com.mycompany.tgni.services;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;
import org.apache.commons.lang.StringUtils;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipExpander;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.Traversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mycompany.tgni.beans.TConcept;
import com.mycompany.tgni.beans.TRelTypes;
import com.mycompany.tgni.beans.TRelation;
import com.mycompany.tgni.utils.AnalysisEnginePool;
import com.mycompany.tgni.utils.AnalyzerPool;
import com.mycompany.tgni.utils.DbConnectionPool;
import com.mycompany.tgni.utils.DbUtils;
import com.mycompany.tgni.utils.JsonUtils;
import com.mycompany.tgni.utils.UimaUtils;

/**
 * Service that provides methods to operate on TConcept
 * and TRelation objects.
 */
public class NodeService {

  public static final Comparator<TRelation> DEFAULT_SORT = 
    new Comparator<TRelation>() {
      @Override public int compare(TRelation r1, TRelation r2) {
        if (r1.getMstip() != r2.getMstip()) {
          // return the one thats is manually stipulated
          return r1.getMstip() ? -1 : 1;
        } else {
          Long mrank1 = r1.getMrank();
          Long mrank2 = r2.getMrank();
          if (mrank1 != mrank2) {
            return mrank2.compareTo(mrank1);
          } else {
            Long arank1 = r1.getArank();
            Long arank2 = r2.getArank();
            return arank2.compareTo(arank1);
          }
        }
      }
  };

  protected Logger logger = LoggerFactory.getLogger(getClass());
  
  private static NodeService instance = new NodeService();
  
  private GraphDatabaseService graphService;
  private DbConnectionPool mysqlPool;
  private AnalyzerPool analyzerPool;
  private AnalysisEnginePool aePool;
  private NameNormalizer nameNormalizer;
  private BloomFilterService bloomFilterService;
  private CacheService cacheService;
  private String cacheDescriptor;

  private NodeService() {
    try {
      init();
    } catch (Exception e) {
      logger.error("Cannot instantiate nodeservice");
    }
  }
  
  public static NodeService getInstance() {
    return instance;
  }
  
  public void init() throws Exception {
    Properties props = new Properties();
    props.load(new FileInputStream(new File(
      UimaUtils.getTgniHome(), 
      "conf/nodeservice.properties")));
    int numCpus = Runtime.getRuntime().availableProcessors();
    this.graphService = new EmbeddedGraphDatabase(
      props.getProperty("graphDir"));
    this.mysqlPool = new DbConnectionPool(
      props.getProperty("mysqlProps"), numCpus);
    this.analyzerPool = new AnalyzerPool(
      props.getProperty("stopwordsFile"), numCpus);
    this.aePool = new AnalysisEnginePool(
      props.getProperty("taxonomyMappingAEDescriptor"), numCpus);
    // TODO: initialize a pool of name normalizers with
    // the aePool and analyzerPool elements
    this.bloomFilterService = new BloomFilterService(mysqlPool);
    this.bloomFilterService.init();
    this.cacheDescriptor = props.getProperty("cacheDescriptor");
    if (StringUtils.isNotEmpty(cacheDescriptor)) {
      this.cacheService = new CacheService();
      cacheService.setCacheDescriptor(cacheDescriptor);
      cacheService.init();
    }
  }
  
  public void destroy() throws Exception {
    graphService.shutdown();
    mysqlPool.destroy();
    analyzerPool.destroy();
    aePool.destroy();
    if (StringUtils.isNotEmpty(cacheDescriptor)) {
      cacheService.destroy();
    }
  }
  
  public TConcept getConcept(Integer oid) throws Exception {
    if (cacheService != null) {
      TConcept concept = cacheService.getConcept(oid);
      if (concept != null) {
        return concept;
      }
    }
    Long nid = getNidFromOid(oid);
    TConcept concept = null;
    if (nid != null) {
      synchronized(graphService) {
        Node node = graphService.getNodeById(nid);
        concept = toConcept(node);
      }
    }
    if (cacheService != null) {
      cacheService.putConcept(oid, concept);
    }
    return concept; 
  }

  /**
   * Used to map one or more concepts to the name. The name is
   * normalized into one or more versions (for synonyms) by the
   * UIMA analyzer chain. The normalized name is checked against
   * the bloom filter to see if a cache lookup is justified, and
   * then against the cache. If not found in cache, then a database
   * lookup with pass-through via the cache is done to retrieve
   * the associated concept object.
   * @param name the raw string to map concepts against.
   * @return a list of TConcept objects (can be empty, never null).
   * @throws Exception if thrown.
   */
  public List<TConcept> getConcepts(String name) throws Exception {
    List<TConcept> concepts = new ArrayList<TConcept>();
    List<String> normalizedNames = nameNormalizer.normalize(name);
    for (String normalizedName : normalizedNames) {
      if (bloomFilterService.contains(name)) {
        if (cacheService != null) {
          List<TConcept> nameConcepts = cacheService.getConcepts(normalizedName);
          if (nameConcepts != null) {
            concepts.addAll(nameConcepts);
            continue;
          }
        }
        List<TConcept> nameConcepts = new ArrayList<TConcept>();
        Set<Long> nids = getNidsFromName(normalizedName);
        for (Long nid : nids) {
          synchronized (graphService) {
            Node node = graphService.getNodeById(nid);
            nameConcepts.add(toConcept(node));
          }
        }
        if (cacheService != null) {
          cacheService.putConcepts(normalizedName, nameConcepts);
        }
        concepts.addAll(nameConcepts);
      }
    }
    return concepts;
  }

  /**
   * This method is used to find concepts that are "similar to"
   * the concept desired. This is just a plain database query 
   * against the un-normalized name in the oid_name table in the
   * database. It is not used for concept mapping.
   * @param name the raw string to find concepts against.
   * @return a List of TConcepts (can be empty, never null).
   * @throws Exception if thrown.
   */
  public List<TConcept> findConcepts(String name) throws Exception {
    Set<Long> nids = findNidsFromName(name);
    List<TConcept> concepts = new ArrayList<TConcept>();
    for (Long nid : nids) {
      synchronized(graphService) {
        Node node = graphService.getNodeById(nid);
        concepts.add(toConcept(node));
      }
    }
    return concepts;
  }

  public Bag<TRelTypes> getRelationCounts(TConcept concept) 
      throws Exception {
    if (cacheService != null) {
      Bag<TRelTypes> counts =  cacheService.getRelationCounts(concept);
      if (counts != null) {
        return counts;
      }
    }
    Bag<TRelTypes> counts = new HashBag<TRelTypes>();
    Long nid = getNidFromOid(concept.getOid());
    synchronized(graphService) {
      Node node = graphService.getNodeById(nid);
      for (Relationship relationship : 
        node.getRelationships(Direction.OUTGOING)) {
        TRelTypes type = TRelTypes.fromName(
            relationship.getType().name()); 
        if (type != null) {
          counts.add(type);
        }
      }
    }
    if (cacheService != null) {
      cacheService.putRelationCounts(concept, counts);
    }
    return counts;
  }
  
  public List<TRelation> getRelatedConcepts(TConcept concept,
      TRelTypes type) throws Exception {
    return getRelatedConcepts(concept, type, DEFAULT_SORT);
  }
  
  public List<TRelation> getRelatedConcepts(TConcept concept, 
      TRelTypes type, Comparator<TRelation> sort) 
      throws Exception {
    if (cacheService != null) {
      List<TRelation> rels = cacheService.getRelatedConcepts(concept, type);
      if (rels != null) {
        Collections.sort(rels, sort);
        return rels;
      }
    }
    Long nid = getNidFromOid(concept.getOid());
    synchronized(graphService) {
      Node node = graphService.getNodeById(nid);
      List<TRelation> rels = new ArrayList<TRelation>();
      if (node != null) {
        for (Relationship relationship : 
          node.getRelationships(type, Direction.OUTGOING)) {
          RelationshipType relationshipType = relationship.getType();
          if (TRelTypes.fromName(relationshipType.name()) != null) {
            Node relatedNode = relationship.getEndNode();
            Integer relatedConceptOid = (Integer) relatedNode.getProperty("oid");
            TRelation rel = new TRelation();
            rel.setFromOid(concept.getOid());
            rel.setToOid(relatedConceptOid);
            rel.setMstip((Boolean) relationship.getProperty("mstip"));
            rel.setMrank((Long) relationship.getProperty("mrank"));
            rel.setArank((Long) relationship.getProperty("arank"));
            rel.setRelType(TRelTypes.fromName(relationshipType.name()));
            rels.add(rel);
          }
        }
      } else {
        return Collections.emptyList();
      }
      if (cacheService != null) {
        cacheService.putRelatedConcepts(concept, type, rels);
      }
      Collections.sort(rels, sort);
      return rels;
    }
  }
  
  /**
   * Returns a Neo4J Path object representing the shortest
   * (in terms of hops) distance between two nodes in a graph.
   * May return null if there is no path.
   * @param oid1 the OID for the first node.
   * @param oid2 the OID for the second node.
   * @param maxDepth the max depth to search.
   * @return a Path or null.
   * @throws Exception if thrown.
   */
  public Path getShortestPath(int oid1, int oid2, int maxDepth) 
      throws Exception {
    long nid1 = getNidFromOid(oid1);
    long nid2 = getNidFromOid(oid2);
    synchronized(graphService) {
      Node node1 = graphService.getNodeById(nid1);
      Node node2 = graphService.getNodeById(nid2);
      RelationshipExpander expander = Traversal.expanderForAllTypes();
      PathFinder<Path> finder = GraphAlgoFactory.shortestPath(expander, maxDepth);
      Iterable<Path> paths = finder.findAllPaths(node1, node2);
      // these are the shortest path(s) in terms of number of hops
      // now we need to find the most likely path based on the 
      // sum of the rank of relationships
      Path bestPath = null;
      Long maxStrength = 0L;
      for (Path path : paths) {
        Long strength = 0L;
        for (Iterator<PropertyContainer> it = path.iterator(); it.hasNext(); ) {
          PropertyContainer pc = it.next();
          if (pc instanceof Relationship) {
            strength += (Long) ((Relationship) pc).getProperty("mrank"); 
          }
        }
        if (strength > maxStrength) {
          maxStrength = strength;
          bestPath = path;
        }
      }
      return bestPath;
    }
  }
  
  public String pathString(Path path) {
    if (path == null) return "NONE";
    StringBuilder buf = new StringBuilder();
    for (Iterator<PropertyContainer> it = path.iterator(); it.hasNext(); ) {
      PropertyContainer pc = it.next();
      if (pc instanceof Node) {
        Node npc = (Node) pc;
        buf.append((String) npc.getProperty("pname")).
          append("(").
          append((Integer) npc.getProperty("oid")).
          append(")");
      } else if (pc instanceof Relationship) {
        Relationship rpc = (Relationship) pc;
        buf.append("--(").
          append(rpc.getType().name()).
          append("[").
          append((Long) rpc.getProperty("mrank")).
          append("])-->");
      }
    }
    return buf.toString();
  }

  private TConcept toConcept(Node node) {
    TConcept concept = new TConcept();
    concept.setOid((Integer) node.getProperty("oid"));
    concept.setPname((String) node.getProperty("pname"));
    concept.setQname((String) node.getProperty("qname"));
    concept.setSynonyms(JsonUtils.stringToList(
      (String) node.getProperty("synonyms")));
    concept.setStycodes(JsonUtils.stringToMap(
      (String) node.getProperty("stycodes")));
    concept.setStygrp((String) node.getProperty("stygrp"));
    concept.setMrank((Long) node.getProperty("mrank"));
    concept.setArank((Long) node.getProperty("arank"));
    concept.setTid((Integer) node.getProperty("tid"));
    return concept;
  }
  
  private static final String GET_NID_FROM_OID_SQL = 
    "select nid " +
    "from oid_nid " +
    "where oid = ?";
  private static final String GET_NIDS_FROM_NAME_SQL =
    "select b.nid " +
    "from oid_name a, oid_nid b " +
    "where a.oid = b.oid " +
    "and a.name = ?";
  private static final String FIND_NIDS_FROM_NAME_SQL =
    "select b.nid f" +
    "rom oid_name a, oid_nid b " +
    "where a.oid = b.oid " +
    "and a.name like ?";
    
  private Long getNidFromOid(Integer oid) throws Exception {
    Long nid = 0L;
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      conn = mysqlPool.borrowConnection();
      ps = conn.prepareStatement(GET_NID_FROM_OID_SQL);
      ps.setInt(1, oid);
      rs = ps.executeQuery();
      while (rs.next()) {
        nid = rs.getLong(1);
        break;
      }
    } finally {
      DbUtils.closeResultSet(rs);
      DbUtils.closePreparedStatement(ps);
      DbUtils.returnConnection(mysqlPool, conn);
    }
    return nid;
  }

  private Set<Long> getNidsFromName(String name) throws Exception {
    Set<Long> nids = new HashSet<Long>();
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      conn = mysqlPool.borrowConnection();
      ps = conn.prepareStatement(GET_NIDS_FROM_NAME_SQL);
      ps.setString(1, name);
      rs = ps.executeQuery();
      while (rs.next()) {
        nids.add(rs.getLong(1));
      }
    } finally {
      DbUtils.closeResultSet(rs);
      DbUtils.closePreparedStatement(ps);
      DbUtils.returnConnection(mysqlPool, conn);
    }
    return nids;
  }
  
  private Set<Long> findNidsFromName(String name) throws Exception {
    Set<Long> nids = new HashSet<Long>();
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      conn = mysqlPool.borrowConnection();
      ps = conn.prepareStatement(FIND_NIDS_FROM_NAME_SQL);
      ps.setString(1, "%" + name + "%");
      rs = ps.executeQuery();
      while (rs.next()) {
        nids.add(rs.getLong(1));
      }
    } finally {
      DbUtils.closeResultSet(rs);
      DbUtils.closePreparedStatement(ps);
      DbUtils.returnConnection(mysqlPool, conn);
    }
    return nids;
  }
}
