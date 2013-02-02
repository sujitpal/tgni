package com.mycompany.tgni.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FilenameUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Singleton graph database service which is started up non-lazily.
 * Repeated invocations to GraphInstance.getInstance() will 
 * return the same (thread-safe) graph database service.
 */
public class GraphInstance {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  private static GraphInstance instance = new GraphInstance();
  
  private GraphDatabaseService service;
  
  private GraphInstance() {
    try {
      Properties props = new Properties();
      props.load(new FileInputStream(new File(
        FilenameUtils.concat(UimaUtils.getTgniHome(), 
        "conf/nodeservice.properties"))));
      String graphDir = props.getProperty("graphDir");
      this.service = new EmbeddedGraphDatabase(graphDir);
    } catch (Exception e) {
      logger.warn("Could not start up Embedded Graph DB", e);
    }
  }
  
  public static GraphDatabaseService getInstance() {
    return instance.service;
  }
  
  public static void destroy() {
    instance.service.shutdown();
  }
}
