package com.mycompany.tgni.utils;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO: class level javadocs
 */
public class AnalysisEnginePool {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  private ObjectPool pool;
  
  public AnalysisEnginePool(String aeDescriptor, int poolSize) {
    this.pool = new GenericObjectPool(
      new AnalysisEngineFactory(aeDescriptor), 
      poolSize, 
      GenericObjectPool.WHEN_EXHAUSTED_BLOCK, 1000L, 
      poolSize);
  }
  
  public AnalysisEngine borrowAnalysisEngine() throws Exception {
    return (AnalysisEngine) pool.borrowObject();
  }
  
  public void returnAnalysisEngine(AnalysisEngine analysisEngine) 
      throws Exception {
    if (analysisEngine != null) {
      pool.returnObject(analysisEngine);
    }
  }
  
  public void destroy() throws Exception {
    pool.clear();
    pool.close();
  }
  
  private class AnalysisEngineFactory extends BasePoolableObjectFactory {

    private String aeDescriptor;
    
    public AnalysisEngineFactory(String aeDescriptor) {
      this.aeDescriptor = aeDescriptor;
    }
    
    @Override
    public Object makeObject() throws Exception {
      return UimaUtils.getAE(aeDescriptor, null);
    }
    
    @Override
    public void destroyObject(Object obj) throws Exception {
      if (obj instanceof AnalysisEngine) {
        ((AnalysisEngine) obj).destroy();
      }
    }
  }
}
