package com.mycompany.tgni.utils;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: class level javadocs
 */
public class DbUtils {

  private static final Logger logger = LoggerFactory.getLogger(DbUtils.class);
  
  public static Connection getConnection(String dbPropsFile) 
      throws Exception {
    Properties props = new Properties();
    props.load(new FileInputStream(new File(dbPropsFile)));
    Class.forName(props.getProperty("database.driverClassName"));
    Connection conn = DriverManager.getConnection(
      props.getProperty("database.url"), 
      props.getProperty("database.username"),
      props.getProperty("database.password"));
    return conn;
  }

  public static void closeConnection(Connection conn) {
    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException e) {
        logger.warn("Can't close connection", e);
      }
    }
  }

  public static void closePreparedStatement(PreparedStatement ps) {
    if (ps != null) {
      try {
        ps.close();
      } catch (SQLException e) {
        logger.warn("Can't close prepared statement", e);
      }
    }
  }

  public static void closeResultSet(ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException e) {
        logger.warn("Can't close resultset", e);
      }
    }
  }

  public static void returnConnection(DbConnectionPool mysqlPool,
      Connection conn) {
    try {
      mysqlPool.returnConnection(conn);
    } catch (Exception e) {
      logger.error("Can't return connection to pool", e);
    }
  }
}
