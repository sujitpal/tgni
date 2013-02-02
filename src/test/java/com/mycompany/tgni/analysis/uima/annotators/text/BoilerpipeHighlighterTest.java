package com.mycompany.tgni.analysis.uima.annotators.text;

import java.net.URL;

import org.junit.Test;

import de.l3s.boilerpipe.BoilerpipeExtractor;
import de.l3s.boilerpipe.extractors.CommonExtractors;
import de.l3s.boilerpipe.extractors.DefaultExtractor;
import de.l3s.boilerpipe.sax.HTMLHighlighter;

/**
 * TODO: class level javadocs
 */
public class BoilerpipeHighlighterTest {

  @Test
  public void testOneliner() throws Exception {
    URL url = new URL("file:///Users/sujit/Projects/tgni/src/test/data/sujitpal.blogspot.com.htm");
    BoilerpipeExtractor extractor = CommonExtractors.ARTICLE_EXTRACTOR;
    HTMLHighlighter hh = HTMLHighlighter.newHighlightingInstance();
    hh.setOutputHighlightOnly(true);
    hh.setExtraStyleSheet("");
    System.out.println(hh.process(url, extractor));
  }
}
