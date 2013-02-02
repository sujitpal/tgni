package com.mycompany.tgni.analysis.uima.annotators.text;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.junit.Assert;
import org.junit.Test;

import com.mycompany.tgni.utils.UimaUtils;

/**
 * Test case for the UIMA text annotator.
 */
public class TextAnnotatorTest {
  
  @Test
  public void testBlogPageAnnotation() throws Exception {
    AnalysisEngine ae = UimaUtils.getAE(
      "conf/descriptors/TextAE.xml", null);
    String text = FileUtils.readFileToString(
        new File("src/test/data/sujitpal.blogspot.com.htm"), "UTF-8");
    JCas jcas = UimaUtils.runAE(ae, text, UimaUtils.MIMETYPE_HTML, null);
    FSIndex index = jcas.getAnnotationIndex(TextAnnotation.type);
    for (Iterator<TextAnnotation> it = index.iterator(); it.hasNext(); ) {
      TextAnnotation annotation = it.next();
      System.out.println("==");
      System.out.println(annotation.getTagName() + " " + 
        annotation.getBegin() + ":" + annotation.getEnd() + " " +
        annotation.getConfidence() + ": " + annotation.getCoveredText());
    }
  }
  
  @Test
  public void testPlainTextAnnotation() throws Exception {
    AnalysisEngine ae = UimaUtils.getAE(
      "conf/descriptors/TextAE.xml", null);
    String text = FileUtils.readFileToString(
      new File("src/test/data/xdebug-notes.txt"), "UTF-8");
    JCas jcas = UimaUtils.runAE(ae, text, UimaUtils.MIMETYPE_TEXT, null);
    FSIndex index = jcas.getAnnotationIndex(TextAnnotation.type);
    int numTextAnnotations = 0;
    for (Iterator<TextAnnotation> it = index.iterator(); it.hasNext(); ) {
      TextAnnotation annotation = it.next();
      System.out.println("==");
      System.out.println(annotation.getTagName() + " " + 
        annotation.getBegin() + ":" + annotation.getEnd() + " " +
        annotation.getConfidence() + ": " + annotation.getCoveredText());
      numTextAnnotations++;
    }
    Assert.assertEquals(1, numTextAnnotations);
  }

  @Test
  public void testQueryStringAnnotation() throws Exception {
    AnalysisEngine ae = UimaUtils.getAE(
      "conf/descriptors/TextAE.xml", null);
    String text = "Heart attack";
    JCas jcas = UimaUtils.runAE(ae, text, UimaUtils.MIMETYPE_STRING, null);
    FSIndex index = jcas.getAnnotationIndex(TextAnnotation.type);
    int numTextAnnotations = 0;
    for (Iterator<TextAnnotation> it = index.iterator(); it.hasNext(); ) {
      TextAnnotation annotation = it.next();
      System.out.println("==");
      System.out.println(annotation.getTagName() + " " + 
        annotation.getBegin() + ":" + annotation.getEnd() + " " +
        annotation.getConfidence() + ": " + annotation.getCoveredText());
      numTextAnnotations++;
    }
    Assert.assertEquals(1, numTextAnnotations);
  }

}
