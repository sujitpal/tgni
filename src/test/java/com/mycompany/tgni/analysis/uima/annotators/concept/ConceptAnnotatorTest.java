package com.mycompany.tgni.analysis.uima.annotators.concept;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mycompany.tgni.utils.UimaUtils;

/**
 * Tests for the ConceptAnnotator.
 */
public class ConceptAnnotatorTest {

  private static AnalysisEngine ae;
  
  private static final String[] TEST_STRINGS = new String[] {
    "Heart Attack", "Asthma", "Myocardial Infarction",
    "Asthma in young children", "Asthma symptoms",
    "Symptoms of Asthma", "Hearing aids", "Bad Allergy",
    "BAD Allergy", "cold"
  };
  
  @BeforeClass
  public static void setupBeforeClass() throws Exception {
    ae = UimaUtils.getAE(
      "conf/descriptors/ConceptMappingAE.xml", null);
  }
  
  @AfterClass
  public static void teardownAfterClass() throws Exception {
    if (ae != null) {
      ae.destroy();
    }
  }
  
  @Test
  public void testConceptAnnatatorForHtmlText() throws Exception {
    System.out.println("========== html ===========");
    String text = FileUtils.readFileToString(
      new File("src/test/data/hl-heart-attack.html"), 
      "UTF-8");
    long startTime = System.currentTimeMillis();
    JCas jcas = UimaUtils.runAE(ae, text, UimaUtils.MIMETYPE_HTML, null);
    FSIndex fsindex = jcas.getAnnotationIndex(ConceptAnnotation.type);
    for (Iterator<ConceptAnnotation> it = fsindex.iterator(); it.hasNext(); ) {
      ConceptAnnotation annotation = it.next();
      System.out.println("(" + annotation.getBegin() + "," + 
        annotation.getEnd() + "): " + annotation.getCoveredText() +
        " : (" + annotation.getOid() + "," + annotation.getPname() + 
        "," + annotation.getStygroup() + "," + annotation.getStycodes() +
        "): sim=" + annotation.getSimilarity());
    }
    long endTime = System.currentTimeMillis();
    System.out.println("Elapsed time for html (ms): " + (endTime - startTime));
    
  }
  
  @Test
  public void testConceptAnnotatorForPlainText() throws Exception {
    System.out.println("========== text ===========");
    String text = FileUtils.readFileToString(
      new File("src/test/data/hl-heart-attack-boilerpipe.txt"), 
      "UTF-8");
    long startTime = System.currentTimeMillis();
    JCas jcas = UimaUtils.runAE(ae, text, UimaUtils.MIMETYPE_TEXT, null);
    FSIndex fsindex = jcas.getAnnotationIndex(ConceptAnnotation.type);
    for (Iterator<ConceptAnnotation> it = fsindex.iterator(); it.hasNext(); ) {
      ConceptAnnotation annotation = it.next();
      System.out.println("(" + annotation.getBegin() + "," + 
        annotation.getEnd() + "): " + annotation.getCoveredText() +
        " : (" + annotation.getOid() + "," + annotation.getPname() + 
        "," + annotation.getStygroup() + "," + annotation.getStycodes() +
        "): sim=" + annotation.getSimilarity());
    }
    long endTime = System.currentTimeMillis();
    System.out.println("Elapsed time for text (ms): " + (endTime - startTime));
  }
  
  @Test
  public void testConceptAnnotatorForString() throws Exception {
    System.out.println("========== query ===========");
    for (String text : TEST_STRINGS) {
      System.out.println(">> " + text);
      JCas jcas = UimaUtils.runAE(ae, text, UimaUtils.MIMETYPE_STRING, null);
      FSIndex fsindex = jcas.getAnnotationIndex(ConceptAnnotation.type);
      for (Iterator<ConceptAnnotation> it = fsindex.iterator(); it.hasNext(); ) {
        ConceptAnnotation annotation = it.next();
        System.out.println("(" + annotation.getBegin() + "," + 
          annotation.getEnd() + "): " + annotation.getCoveredText() +
          " : (" + annotation.getOid() + "," + annotation.getPname() + 
          "," + annotation.getStygroup() + "," + annotation.getStycodes() +
          "): sim=" + annotation.getSimilarity());
      }
    }
  }
}
