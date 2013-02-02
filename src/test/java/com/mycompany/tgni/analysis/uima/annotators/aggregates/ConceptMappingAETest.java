package com.mycompany.tgni.analysis.uima.annotators.aggregates;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mycompany.tgni.analysis.uima.annotators.concept.ConceptAnnotation;
import com.mycompany.tgni.services.NodeService;
import com.mycompany.tgni.utils.UimaUtils;

/**
 * Test for concept mapping aggregate ae.
 */
public class ConceptMappingAETest {

  private static NodeService nodeService;

  @BeforeClass
  public static void setupBeforeClass() throws Exception {
    nodeService = NodeService.getInstance();
  }
  
  @AfterClass
  public static void teardownAfterClass() throws Exception {
    nodeService.destroy();
  }

//  @Test
//  public void testConceptMappingPipeline() throws Exception {
//    AnalysisEngine ae = UimaUtils.getAE(
//      "/prod/web/data/tgni/conf/descriptors/ConceptMappingAE.xml", 
//      null);
//    String text = FileUtils.readFileToString(
//      new File("src/test/data/hl-heart-attack-boilerpipe.txt"), 
//      "UTF-8");
//    JCas jcas = UimaUtils.runAE(ae, text, UimaUtils.MIMETYPE_TEXT);
//    FSIndex<? extends Annotation> fsindex = jcas.getAnnotationIndex(NounPhraseAnnotation.type);
//    for (Iterator<? extends Annotation> it = fsindex.iterator(); it.hasNext(); ) {
//      NounPhraseAnnotation annotation = (NounPhraseAnnotation) it.next();
//      System.out.println("(" + annotation.getBegin() + "," + 
//          annotation.getEnd() + "): " + 
//          annotation.getCoveredText()); 
//    }
//  }
  
  private final String[] TEST_SENTENCES = new String[] {
      "Viral hepatitis, including hepatitis A, B, and C, are distinct diseases that affect the liver.", //(webmd.com)
//      "This page contains late breaking information, as well as an archival record of updates on safety and regulatory issues related to Hepatitis A and B, including product approvals, significant labeling changes, safety warnings, notices of upcoming public meetings, and notices about proposed regulatory guidances.", // (fda.gov)
//      "Lead poisoning can cause an increased risk of brain, lung, stomach and kidney cancer.", // (cleanupblackwell.com)
//      "Before we look at the difference between diabetes type-I and II, let's firstly look at diabaetes in general.", // (medicalnewstoday.com)
//      "Restricting and purging anorexia are two different types of anorexia that people suffer from.", // (anorexiasurvivalguidaae.com)
//      "Here are some tips on pre and post surgery nutrition.", // (bestofmotherearth.com)
//      "A computer-based register linked to thyroid diagnostic laboratories was used to continuously identify all new cases of overt hyper- and hypothyroidism in two population cohorts with moderate and mild ID, respectively (Aalborg, n = 310,124; urinary iodine, 45 micro g/liter; and Copenhagen, n = 225,707; urinary iodine, 61 micro g/liter).", // (nlm.nih.gov)
//      "Medical and assistive devices are taxable for the most part, unconditionally zero-rated in certain cases, and conditionally zero-rated in certain cases.", //(revenuequebec.ca)
//      "These regions correspond to the least well conserved regions of the whole miRNA/snoRNA molecules.", // (ploscompbiol.org)
//      "Hetero- and Homogeneous mixtures are alike because they are both compounds, and both made up of different elements.", // (answers.com)
    };

  @Test
  public void testConceptMappingPipelineWithCoordinateExpansion() throws Exception {
    AnalysisEngine ae = UimaUtils.getAE(
      "/prod/web/data/tgni/conf/descriptors/ConceptMappingAE.xml", 
      null);
    for (String sentence : TEST_SENTENCES) {
      System.out.println();
      System.out.println(sentence);
      JCas jcas = UimaUtils.runAE(ae, sentence, UimaUtils.MIMETYPE_TEXT, null);
//      System.out.println("==== noun phrases ====");
//      FSIndex<? extends Annotation> fsindex = jcas.getAnnotationIndex(NounPhraseAnnotation.type);
//      for (Iterator<? extends Annotation> it = fsindex.iterator(); it.hasNext(); ) {
//        NounPhraseAnnotation annotation = (NounPhraseAnnotation) it.next();
//        System.out.println("(" + annotation.getBegin() + "," + 
//          annotation.getEnd() + "): " + 
//          annotation.getCoveredText() + " {coordExp=" +
//          annotation.getCoordExpansions() + "}");
//      }
      System.out.println("==== concept annotations ====");
      FSIndex fsindex2 = jcas.getAnnotationIndex(ConceptAnnotation.type);
      for (Iterator<? extends Annotation> it = fsindex2.iterator(); it.hasNext(); ) {
        ConceptAnnotation annotation = (ConceptAnnotation) it.next();
        System.out.println("(" + annotation.getBegin() + "," + 
          annotation.getEnd() + "): " + 
          annotation.getCoveredText() + 
          ", oid=" + annotation.getOid() +
          ", pname=" + annotation.getPname());
      }
    }
  }
}
