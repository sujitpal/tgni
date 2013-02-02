package com.mycompany.tgni.utils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mycompany.tgni.analysis.lucene.StopFilter;

/**
 * Lucene Analyzer Pool implementation.
 */
public class AnalyzerPool {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  private ObjectPool pool;
  
  public AnalyzerPool(String stopwordsFile, int poolSize) {
    Set<?> stopset = null;
    try {
      stopset = StopFilter.makeStopSet(
        Version.LUCENE_40, new File(stopwordsFile));
    } catch (Exception e) {
      logger.warn("Could not make stop set from file:" + stopwordsFile, e);
    }
    this.pool = new GenericObjectPool(
      new AnalyzerFactory(stopset), 
      poolSize,
      GenericObjectPool.WHEN_EXHAUSTED_BLOCK, 1000L, 
      poolSize);
  }
  
  public Analyzer borrowAnalyzer() throws Exception {
    logger.debug("borrowing analyzer");
    return (Analyzer) pool.borrowObject();
  }
  
  public void returnAnalyzer(Analyzer analyzer) throws IOException {
    if (analyzer != null) {
      try {
        logger.debug("returning analyzer");
        pool.returnObject(analyzer);
      } catch (Exception e) {
        throw new IOException(e);
      }
    }
  }
  
  public void destroy() throws Exception {
    pool.clear();
    pool.close();
  }
  
  private class AnalyzerFactory extends BasePoolableObjectFactory {

    private Set<?> stopwords;
    
    public AnalyzerFactory(Set<?> stopwords) {
      this.stopwords = stopwords;
    }

    @Override
    public Object makeObject() throws Exception {
      return new Analyzer() {
        @Override
        public TokenStream tokenStream(String fieldname, Reader reader) {
          TokenStream input = new StandardTokenizer(Version.LUCENE_40, reader);
          input = new LowerCaseFilter(Version.LUCENE_40, input);
          input = new StopFilter(Version.LUCENE_40, input, stopwords);;
          input = new PorterStemFilter(input);
          return input;
        }
      };
    }
    
    @Override
    public void destroyObject(Object obj) throws Exception {
      if (obj instanceof Analyzer) {
        ((Analyzer) obj).close();
      }
    }
  }
}
