package com.mycompany.tgni.analysis.uima.annotators.nlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.junit.Assert;
import org.junit.Test;

import com.mycompany.tgni.utils.UimaUtils;

/**
 * Tests for the UIMA Sentence Annotator.
 */
public class SentenceAnnotatorTest {

  private String TEST_STRING = 
    "The growing popularity of Linux in Asia, Europe, and the U.S. is a major concern " +
    "for Microsoft. It costs less than 1 USD a month to maintain a Linux PC in Asia. " +
    "By 2007, over 500,000 PCs sold in Asia may be Linux based. " + 
    "Jaguar will sell its new XJ-6 model in the U.S. for a small fortune :( " +
    "(http://www.jaguar.com/sales or contact xj-6@jaguar.com). " + 
    "Argentine officials said the country needed \"a more serious climate\" to carry " +
    "out structured changes in the economy. " + 
    "They know that controlling inflation will make the industry more competitive and " +
    "bring down unemployment in the long run. " + 
    "OTTOWA, March 3 -- Canada's real gross domestic product, seasonally adjusted, " +
    "rose 1.1 pct in the fourth quarter of 1986, the same as the growth as in the " +
    "previous quarter, Statistics Canada said. That left growth for the full year at " +
    "3 pct, which is down from 1985's four pct increase. The rise was also slightly " +
    "below the 3.3 pct growth rate Finance Minister Michael Wilson predicted for 1986 " +
    "in February's budget. He also forecast GDP would rise 2.8 pct in 1987. Statistics " +
    "Canada said final domestic demand rose 0.6 pct in the final three months of the " +
    "year after a 1.0% gain in the third quarter. " + 
    "I have a <a href=\"http://www.funny.com/funnyurl\">funny url</a> to share. " + 
    "The problem occured at 3:00am in the morning on www.ibm.co.in because of a request from 192.168.1.119."
  ;
  
  @Test
  public void testSentenceWithOpenNlp() throws Exception {
    InputStream data = new FileInputStream("src/main/resources/models/en_sent.bin");
    SentenceModel model = new SentenceModel(data);
    SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
    long st = System.currentTimeMillis();
    String[] sentences = sentenceDetector.sentDetect(TEST_STRING);
    Span[] spans = sentenceDetector.sentPosDetect(TEST_STRING);
    for (int i = 0; i < sentences.length; i++) {
      System.out.println("sentence[opennlp]:" + spans[i].getStart() + "," + spans[i].getEnd() + ": " + sentences[i]);
    }
    data.close();
  }
  
  // NOTE: SentenceAnnotator cannot be tested standalone, it
  // expects a JCas annotated with TextAnnotations
  @Test
  public void testSentenceAnnotator() throws Exception {
    AnalysisEngine ae = UimaUtils.getAE(
      "conf/descriptors/TextMappingAE.xml", null);
    String text = FileUtils.readFileToString(
      new File("src/test/data/sujitpal.blogspot.com.htm"), "UTF-8");
    JCas jcas = UimaUtils.runAE(ae, text, UimaUtils.MIMETYPE_HTML, null);
    FSIndex index = jcas.getAnnotationIndex(SentenceAnnotation.type);
    int numSentences = 0;
    for (Iterator<SentenceAnnotation> it = index.iterator(); it.hasNext(); ) {
      SentenceAnnotation annotation = it.next();
      System.out.println("(" + annotation.getBegin() + 
        ":" + annotation.getEnd() + 
        "): " + annotation.getCoveredText());
      numSentences++;
    }
    Assert.assertTrue(numSentences > 0);
  }
}
