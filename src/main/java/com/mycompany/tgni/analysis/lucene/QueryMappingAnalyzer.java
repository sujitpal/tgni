package com.mycompany.tgni.analysis.lucene;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.Version;

/**
 * Analyzer to use when mapping a short string against the
 * taxonomy represented by the neo4j/lucene datastores.
 */
public class QueryMappingAnalyzer extends Analyzer {

  private Set<?> stopset;
  private String aeDescriptor;
  
  public QueryMappingAnalyzer(String stopwordsFile, String aeDescriptor) 
      throws IOException {
    this.stopset = StopFilter.makeStopSet(Version.LUCENE_40, 
      new File(stopwordsFile));
    this.aeDescriptor = aeDescriptor;
  }
  
  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    SynonymMap synonymMap = new SynonymMap();
    TokenStream input = new UimaAETokenizer(reader, aeDescriptor, synonymMap);
    input = new SynonymFilter(input, synonymMap);
    input = new LowerCaseFilter(Version.LUCENE_40, input, true);
    input = new StopFilter(Version.LUCENE_40, input, stopset, false);
    input = new PorterStemFilter(input);
    input = new TokenConcatenatingTokenFilter(input, true);
    return input;
  }
}
