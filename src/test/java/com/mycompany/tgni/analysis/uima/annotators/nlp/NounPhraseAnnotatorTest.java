package com.mycompany.tgni.analysis.uima.annotators.nlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import org.apache.commons.io.IOUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import com.mycompany.tgni.utils.UimaUtils;

/**
 * Tests for the Noun Phrase Annotator.
 */
public class NounPhraseAnnotatorTest {

  private static final String[] INPUTS = new String[] { 
    "The U.S. LEI continued to increase in July. However, with the exception of the money supply and interest rate components, other leading indicators show greater weakness Ð consistent with increasing concerns about the health of the economic expansion. Despite rising volatility, the leading indicators still suggest economic activity should be slowly expanding through the end of the year.",
    "The composite economic indexes are the key elements in an analytic system designed to signal peaks and troughs in the business cycle. The leading, coincident, and lagging economic indexes are essentially composite averages of several individual leading, coincident, or lagging indicators. They are constructed to summarize and reveal common turning point patterns in economic data in a clearer and more convincing manner than any individual component Ð primarily because they smooth out some of the volatility of individual components.",
    "Last week Professor Herbert Davenport Kay & associates of Toronto suggested in The Journal of Nutrition that beryllium, a metal related to calcium and now coming into industrial use (it strengthens and hardens aluminum alloys), may be an obscure cause of rickets.",
    "Beryllium foil remains indispensible for high-resolution medical radiography, including CT scanning and mammography. Beryllium in newer generation mammography equipment enables a lower radiation dose scan with significantly finer tumor resolution, enabling breast cancer detection at its early, most treatable stages.",
    "Arsenic, element 33, has a long and nefarious history; its very name has become synonymous with poison. In the 15th and 16th centuries, the Italian family of Borgias used arsenic as their favorite poison for political assassinations. Some even have suggested that Napoleon was poisoned by arsenic-tainted wine served to him while in exile.",
    // and some of my own internal test cases
//    "Dr Johnson was leading the team.",
//    "Lead was used in pencils in the olden days.",
//    // set of shorter sentences for quick testing
//    "Be that as it may, the show must go on.",
//    "As I was telling you, he will not attend the meeting.",
//    "Dr Johnson will lead the team",
//    "Lead is the lead cause of lead poisoning.",
    // test out coordinate expansion
//    "AIDS epidemics are usually preceded by an increased onset of Hepatitis B and C.",
//    "Patients with Hepatitis B/C are usually a greater risk for AIDS.",
  };    

//  @Test
//  public void testNounPhraseAnnotation() throws Exception {
//    AnalysisEngine ae = UimaUtils.getAE("conf/descriptors/ConceptMappingAE.xml", null);
//    for (String input : INPUTS) {
//      System.out.println("text: " + input);
//      JCas jcas = UimaUtils.runAE(ae, input, UimaUtils.MIMETYPE_TEXT, null);
//      FSIndex index = jcas.getAnnotationIndex(NounPhraseAnnotation.type);
//      for (Iterator<NounPhraseAnnotation> it = index.iterator(); it.hasNext(); ) {
//        NounPhraseAnnotation annotation = it.next();
//        System.out.println("...(" + annotation.getBegin() + "," + 
//          annotation.getEnd() + "): " + 
//          annotation.getCoveredText());
//      }
//    }
//    ae.destroy();
//  }
  
  @Test
  public void testNounPhraseExtractionStandalone() throws Exception {
    SentenceDetectorME sentenceDetector;
    TokenizerME tokenizer;
    POSTaggerME posTagger;
    ChunkerME chunker;
    InputStream smis = null;
    InputStream tmis = null;
    InputStream pmis = null;
    InputStream cmis = null;
    try {
      smis = new FileInputStream(new File("/Users/sujit/Projects/tgni/src/main/resources/models/en_sent.bin"));
      tmis = new FileInputStream(new File("/Users/sujit/Projects/tgni/src/main/resources/models/en_token.bin"));
      pmis = new FileInputStream(new File("/Users/sujit/Projects/tgni/src/main/resources/models/en_pos_maxent.bin"));
      cmis = new FileInputStream(new File("/Users/sujit/Projects/tgni/src/main/resources/models/en_chunker.bin"));
      SentenceModel smodel = new SentenceModel(smis);
      sentenceDetector = new SentenceDetectorME(smodel);
      TokenizerModel tmodel = new TokenizerModel(tmis);
      tokenizer = new TokenizerME(tmodel);
      POSModel pmodel = new POSModel(pmis);
      posTagger = new POSTaggerME(pmodel);
      ChunkerModel cmodel = new ChunkerModel(cmis);
      chunker = new ChunkerME(cmodel);
    } finally {
      IOUtils.closeQuietly(cmis);
      IOUtils.closeQuietly(pmis);
      IOUtils.closeQuietly(tmis);
      IOUtils.closeQuietly(smis);
    }
    String text = "This article provides a review of the literature on clinical correlates of awareness in dementia. Most inconsistencies were found with regard to an association between depression and higher levels of awareness. Dysthymia, but not major depression, is probably related to higher levels of awareness. Anxiety also appears to be related to higher levels of awareness. Apathy and psychosis are frequently present in patients with less awareness, and may share common neuropathological substrates with awareness. Furthermore, unawareness seems to be related to difficulties in daily life functioning, increased caregiver burden, and deterioration in global dementia severity. Factors that may be of influence on the inconclusive data are discussed, as are future directions of research.";
    Span[] sentSpans = sentenceDetector.sentPosDetect(text);
    for (Span sentSpan : sentSpans) {
      String sentence = sentSpan.getCoveredText(text).toString();
      int start = sentSpan.getStart();
      Span[] tokSpans = tokenizer.tokenizePos(sentence);
      String[] tokens = new String[tokSpans.length];
      for (int i = 0; i < tokens.length; i++) {
        tokens[i] = tokSpans[i].getCoveredText(sentence).toString();
      }
      String[] tags = posTagger.tag(tokens);
      Span[] chunks = chunker.chunkAsSpans(tokens, tags);
      for (Span chunk : chunks) {
        if ("NP".equals(chunk.getType())) {
          int npstart = start + tokSpans[chunk.getStart()].getStart();
          int npend = start + tokSpans[chunk.getEnd() - 1].getEnd();
          System.out.println(text.substring(npstart, npend));
        }
      }
    }
  }
}
