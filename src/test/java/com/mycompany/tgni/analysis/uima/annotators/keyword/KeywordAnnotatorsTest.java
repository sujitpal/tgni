package com.mycompany.tgni.analysis.uima.annotators.keyword;

import java.util.Iterator;

import org.apache.commons.io.FilenameUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeaturePath;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.Sofa;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Assert;
import org.junit.Test;

import com.mycompany.tgni.analysis.uima.annotators.keyword.KeywordAnnotation;
import com.mycompany.tgni.utils.UimaUtils;

/**
 * Test cases for Keyword Annotations.
 */
public class KeywordAnnotatorsTest {

  public static final String[] TEST_STRINGS = new String[] { 
    // pattern preservations - abbreviations
    "Born in the USA I was...",
    "CSC and IBM are Fortune 500 companies.",
    "Linux is embraced by the Oracles and IBMs of the world",
    "PET scans are uncomfortable.",
    "The HIV-1 virus is an AIDS carrier",
    "Unstructured Information Management Application (UIMA) is fantastic!",
    // pattern transformation - abbreviations
    "Born in the U.S.A., I was...",
    // pattern transformation - hyphenated words
    "He is a free-wheeling kind of guy.",
    // dictionary preseve ignore case
    "Magellan was one of our great mariners",
    "Get your daily dose of Vitamin A here!",
    
    "(-)-(6aR,10aR)-6,6,9-trimethyl-3-pentyl-6a,7,8,10a-tetrahydro-6H-benzo[c]chromen-1-ol", // marijuana
    "2-Acetoxybenzoic acid", // aspirin
    "Calcium bis{(3R,5R)-7-[2-(4-fluorophenyl)-5-isopropyl-3-phenyl-4-(phenylcarbamoyl)-1H-pyrrol-1-yl]-3,5-dihydroxyheptanoate}", // lipitor

    "methyl salicylate overdose",
    "Methylmercury Compound",
    "Methylmercury Compound Poisoning",

  };

//  @Test
//  public void testPatternPreserveAnnotator() throws Exception {
//    AnalysisEngine ae = UimaUtils.getAE(
//      "src/main/resources/descriptors/PatternPreserveAE.xml", 
//      null);
//    for (String testString : TEST_STRINGS) {
//      JCas jcas = UimaUtils.runAE(ae, testString, UimaUtils.MIMETYPE_STRING, null);
//      FSIndex<? extends Annotation> fsindex = 
//        jcas.getAnnotationIndex(KeywordAnnotation.type);
//      System.out.println("[PP]input=" + testString);
//      for (Iterator<? extends Annotation> it = fsindex.iterator(); 
//          it.hasNext(); ) {
//        KeywordAnnotation annotation = (KeywordAnnotation) it.next();
//        System.out.println("(" + annotation.getBegin() + "," + 
//          annotation.getEnd() + "):" + 
//          annotation.getCoveredText());
//      }
//    }
//  }
//  
//  @Test
//  public void testPatternTransformAnnotator() throws Exception {
//    AnalysisEngine ae = UimaUtils.getAE(
//      "src/main/resources/descriptors/PatternTransformAE.xml", 
//      null);
//    for (String testString : TEST_STRINGS) {
//      JCas jcas = UimaUtils.runAE(ae, testString, UimaUtils.MIMETYPE_STRING, null);
//      FSIndex<? extends Annotation> fsindex = 
//        jcas.getAnnotationIndex(KeywordAnnotation.type);
//      System.out.println("[PT]input=" + testString);
//      for (Iterator<? extends Annotation> it = fsindex.iterator(); 
//          it.hasNext(); ) {
//        KeywordAnnotation annotation = (KeywordAnnotation) it.next();
//        System.out.println("(" + annotation.getBegin() + "," + 
//          annotation.getEnd() + "):" + 
//          annotation.getCoveredText() + " => " +
//          annotation.getTransformedValue());
//      }
//    }
//  }
//  
//  @Test
//  public void testDictionaryPreserveIgnoreCaseAnnotator() throws Exception {
//    AnalysisEngine ae = UimaUtils.getAE(
//      "conf/descriptors/DictionaryPreserveIgnoreCaseAE.xml", null); 
//    for (String testString : TEST_STRINGS) {
//      JCas jcas = UimaUtils.runAE(ae, testString, UimaUtils.MIMETYPE_STRING, null);
//      FSIndex<? extends Annotation> fsindex = 
//        jcas.getAnnotationIndex(KeywordAnnotation.type);
//      System.out.println("[DPI]input=" + testString);
//      for (Iterator<? extends Annotation> it = fsindex.iterator(); 
//          it.hasNext(); ) {
//        KeywordAnnotation annotation = (KeywordAnnotation) it.next();
//        System.out.println("(" + annotation.getBegin() + "," + 
//          annotation.getEnd() + "):" + 
//          annotation.getCoveredText());
//      }
//    }
//  }
//
//  @Test
//  public void testDictionaryPreserveMatchCaseAnnotator() throws Exception {
//    AnalysisEngine ae = UimaUtils.getAE(
//      "conf/descriptors/DictionaryPreserveMatchCaseAE.xml", null); 
//    for (String testString : TEST_STRINGS) {
//      JCas jcas = UimaUtils.runAE(ae, testString, UimaUtils.MIMETYPE_STRING, null);
//      FSIndex<? extends Annotation> fsindex = 
//        jcas.getAnnotationIndex(KeywordAnnotation.type);
//      System.out.println("[DPM]input=" + testString);
//      for (Iterator<? extends Annotation> it = fsindex.iterator(); 
//          it.hasNext(); ) {
//        KeywordAnnotation annotation = (KeywordAnnotation) it.next();
//        System.out.println("(" + annotation.getBegin() + "," + 
//          annotation.getEnd() + "):" + 
//          annotation.getCoveredText());
//      }
//    }
//  }
//
//  @Test
//  public void testDictionaryTransformIgnoreCaseAnnotator() throws Exception {
//    AnalysisEngine ae = UimaUtils.getAE(
//      "conf/descriptors/DictionaryTransformIgnoreCaseAE.xml", null); 
//    for (String testString : TEST_STRINGS) {
//      JCas jcas = UimaUtils.runAE(ae, testString, UimaUtils.MIMETYPE_STRING, null);
//      FSIndex<? extends Annotation> fsindex = 
//        jcas.getAnnotationIndex(KeywordAnnotation.type);
//      System.out.println("[DTI]input=" + testString);
//      for (Iterator<? extends Annotation> it = fsindex.iterator(); 
//          it.hasNext(); ) {
//        KeywordAnnotation annotation = (KeywordAnnotation) it.next();
//        System.out.println("(" + annotation.getBegin() + "," + 
//          annotation.getEnd() + "):" + 
//          annotation.getCoveredText() + " => " + 
//          annotation.getTransformedValue());
//      }
//    }
//  }
//
//  @Test
//  public void testDictionaryTransformMatchCaseAnnotator() throws Exception {
//    AnalysisEngine ae = UimaUtils.getAE(
//      "conf/descriptors/DictionaryTransformMatchCaseAE.xml", null); 
//    for (String testString : TEST_STRINGS) {
//      JCas jcas = UimaUtils.runAE(ae, testString, UimaUtils.MIMETYPE_STRING, null);
//      FSIndex<? extends Annotation> fsindex = 
//        jcas.getAnnotationIndex(KeywordAnnotation.type);
//      System.out.println("[DTM]input=" + testString);
//      for (Iterator<? extends Annotation> it = fsindex.iterator(); 
//          it.hasNext(); ) {
//        KeywordAnnotation annotation = (KeywordAnnotation) it.next();
//        System.out.println("(" + annotation.getBegin() + "," + 
//          annotation.getEnd() + "):" + 
//          annotation.getCoveredText() + " => " + 
//          annotation.getTransformedValue());
//      }
//    }
//  }

  private static final String[] CHEM_NAMES = new String[] {
    "(-)-(6aR,10aR)-6,6,9-trimethyl-3-pentyl-6a,7,8,10a-tetrahydro-6H-benzo[c]chromen-1-ol", // marijuana
    "2-Acetoxybenzoic acid", // aspirin
    "Calcium bis{(3R,5R)-7-[2-(4-fluorophenyl)-5-isopropyl-3-phenyl-4-(phenylcarbamoyl)-1H-pyrrol-1-yl]-3,5-dihydroxyheptanoate}", // lipitor
    "2-hydroxy-5-[1-hydroxy-2-[(1-methyl-3-phenylpropyl)amino]ethyl]benzamide monohydrochloride",
    "24-ethylcholest-5-en-3 beta-ol",
    "d,l-N-[4-[l-hydroxy-2-[(l-methylethyl) amino]ethyl]phenyl]methane-sulfonamide monohydrochloride",
    "d,l- N -[4-[1-hydroxy-2-[(1-methylethyl)amino]ethyl]phenyl]methane-sulfonamide monohydrochloride",
    "1-[2-(ethylsulfonyl)ethyl]-2-methyl-5-nitroimidazole, a second-generation 2-methyl-5-nitroimidazole",
    "Beta-Methylbutyric Acid",
  };
  private static final String[] NOT_CHEM_NAMES = new String[] {
    // these should not be flagged as keyword
    "Methyl Phenyl Tetrahydropyridine Poisoning",
    "methyl salicylate overdose",
    "Methylmercury Compound",
    "Methylmercury Compound Poisoning",
    "Toxic effect of ethyl alcohol",
  };
  
  @Test
  public void testChemicalNameAnnotator() throws Exception {
    AnalysisEngine ae = UimaUtils.getAE("conf/descriptors/ChemNameAE.xml", null);
    JCas jcas = null;
    for (String chemName : CHEM_NAMES) {
      System.out.println("Chem name: " + chemName);
      jcas = UimaUtils.runAE(ae, chemName, UimaUtils.MIMETYPE_STRING, null);
      FSIndex fsindex = jcas.getAnnotationIndex(KeywordAnnotation.type);
      int numAnnotations = 0;
      for (Iterator it = fsindex.iterator(); it.hasNext(); ) {
        KeywordAnnotation annotation = (KeywordAnnotation) it.next();
        System.out.println("..(" + annotation.getBegin() + "," +
          annotation.getEnd() + "): " + annotation.getCoveredText());
        numAnnotations++;
      }
      Assert.assertEquals(numAnnotations, 1);
    }
    for (String notChemName : NOT_CHEM_NAMES) {
      System.out.println("Not Chem Name: " + notChemName);
      jcas = UimaUtils.runAE(ae, notChemName, UimaUtils.MIMETYPE_STRING, null);
      FSIndex fsindex = jcas.getAnnotationIndex(KeywordAnnotation.type);
      int numAnnotations = 0;
      for (Iterator it = fsindex.iterator(); it.hasNext(); ) {
        KeywordAnnotation annotation = (KeywordAnnotation) it.next();
        System.out.println("..(" + annotation.getBegin() + "," +
          annotation.getEnd() + "): " + annotation.getCoveredText());
        numAnnotations++;
      }
      Assert.assertEquals(numAnnotations, 0);
    }
  }
}
