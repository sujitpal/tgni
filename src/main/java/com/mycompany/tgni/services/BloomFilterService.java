package com.mycompany.tgni.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.mycompany.tgni.utils.DbConnectionPool;
import com.mycompany.tgni.utils.DbUtils;
import com.skjegstad.utils.BloomFilter;

/**
 * Service based on a standalone bloom filter containing
 * synonyms (from oid_name.name). NodeService first makes
 * a call to this in-memory filter to determine if the name
 * exists in the database or not. If the bloom filter returns
 * a true, then it looks for it in the EhCache cache via Cache
 * Service. If it does not exist, then it executes a pull-
 * through from the database (so it is available in the cache
 * the next time the name is queried).
 */
public class BloomFilterService {

  private BloomFilter<String> filter;
  private DbConnectionPool mysqlPool;
  
  private static final double falsePositiveProbability = 0.1D;
  
  public BloomFilterService(DbConnectionPool mysqlPool) {
    this.mysqlPool = mysqlPool;
  }
  
  private static final String GET_NAME_COUNT_SQL = 
    "select count(*) from oid_name";
  private static final String GET_ALL_NAMES_SQL =
    "select name from oid_name";
  
  public void init() throws Exception {
    int numRecords = 0;
    Connection conn = null;
    PreparedStatement psCount = null;
    ResultSet rsCount = null;
    PreparedStatement psList = null;
    ResultSet rsList = null;
    try {
      conn = mysqlPool.borrowConnection();
      psCount = conn.prepareStatement(GET_NAME_COUNT_SQL);
      rsCount = psCount.executeQuery();
      rsCount.next();
      numRecords = rsCount.getInt(1);
      filter = new BloomFilter<String>(falsePositiveProbability, numRecords);
      psList = conn.prepareStatement(GET_ALL_NAMES_SQL);
      rsList = psList.executeQuery();
      while (rsList.next()) {
        filter.add(rsList.getString(1));
      }
    } finally {
      DbUtils.closeResultSet(rsCount);
      DbUtils.closePreparedStatement(psCount);
      DbUtils.closeResultSet(rsList);
      DbUtils.closePreparedStatement(psList);
      DbUtils.returnConnection(mysqlPool, conn);
    }
  }
  
  public void destroy() {
    filter.clear();
  }
  
  public boolean contains(String name) {
    return filter.contains(name);
  }
}
