package com.mycompany.tgni.analysis.lucene;

import java.io.StringReader;

import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.junit.Test;

import com.mycompany.tgni.utils.AnalyzerPool;
import com.mycompany.tgni.utils.UimaUtils;

/**
 * Test for the Lucene UIMA Tokenizer.
 */
public class UimaAETokenizerTest {

  private AnalyzerPool analyzerPool;
  
  private static final String[] testStrings = new String[] {
    "Heart Attack", "Myocardial Infarction",
    "African HIV-1 virus", "Female AIDS carrier",
    "Cancers", "Cancer of the breast", "Be", "As", "be", "as"
  };
  
  @Test
  public void testUimaKeywordTokenizer() throws Exception {
    analyzerPool = new AnalyzerPool(
      FilenameUtils.concat(UimaUtils.getTgniHome(), "conf/stopwords.txt"),
      Runtime.getRuntime().availableProcessors());
    Analyzer analyzer = analyzerPool.borrowAnalyzer();
//    for (String s : KeywordAnnotatorsTest.TEST_STRINGS) {
    for (String s : testStrings) {
      System.out.println("input=" + s);
      TokenStream tokenStream = analyzer.tokenStream("f", new StringReader(s));
      while (tokenStream.incrementToken()) {
        CharTermAttribute termAttr = tokenStream.getAttribute(CharTermAttribute.class);
        OffsetAttribute offsetAttr = tokenStream.getAttribute(OffsetAttribute.class);
        System.out.print("output term=" + 
          new String(termAttr.buffer(), 0, termAttr.length()) +
          ", offset=" + offsetAttr.startOffset() + "," + offsetAttr.endOffset());
        KeywordAttribute keywordAttr = tokenStream.getAttribute(KeywordAttribute.class);
        System.out.print(", keyword?" + keywordAttr.isKeyword());
        PositionIncrementAttribute posIncAttr = tokenStream.getAttribute(PositionIncrementAttribute.class);
        System.out.print(", posinc=" + posIncAttr.getPositionIncrement());
        System.out.println();
      }
    }
    analyzerPool.returnAnalyzer(analyzer);
  }
}
