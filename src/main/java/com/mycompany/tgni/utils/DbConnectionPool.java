package com.mycompany.tgni.utils;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database connection pool implementation.
 */
public class DbConnectionPool {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  private int poolSize;
  private ObjectPool pool;

  private Map<Connection,Map<String,PreparedStatement>> pstmts;

  public DbConnectionPool(String dbPropsFile, int poolSize) {
    this.poolSize = poolSize;
    this.pool = new GenericObjectPool(
      new DbConnectionFactory(dbPropsFile), 
      this.poolSize, 
      GenericObjectPool.WHEN_EXHAUSTED_GROW, 0L,
      this.poolSize);
    this.pstmts = new HashMap<Connection,Map<String,PreparedStatement>>();
  }
  
  public Connection borrowConnection() throws Exception {
    logger.debug("borrowing connection");
    return (Connection) pool.borrowObject();
  }
  
  public void returnConnection(Connection conn) throws Exception {
    if (conn != null) {
      logger.debug("returning connection");
      pool.returnObject(conn);
    }
  }
  
  public void destroy() throws Exception {
    pool.clear();
    pool.close();
  }
  
  private class DbConnectionFactory 
      extends BasePoolableObjectFactory {

    private String dbPropsFile;
    
    public DbConnectionFactory(String dbPropsFile) {
      this.dbPropsFile = dbPropsFile;
    }
    
    @Override
    public Object makeObject() throws Exception {
      Properties props = new Properties();
      props.load(new FileInputStream(new File(dbPropsFile)));
      Class.forName(props.getProperty("database.driverClassName"));
      Connection conn = DriverManager.getConnection(
        props.getProperty("database.url"), 
        props.getProperty("database.username"),
        props.getProperty("database.password"));
      conn.setAutoCommit(false);
      return conn;
    }
    
    @Override
    public void destroyObject(Object obj) throws Exception {
      if (obj instanceof Connection) {
        try {
          ((Connection) obj).close();
        } catch (SQLException e) {}
      }
    }
  }
}
