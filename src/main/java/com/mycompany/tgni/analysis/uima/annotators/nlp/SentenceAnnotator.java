package com.mycompany.tgni.analysis.uima.annotators.nlp;

import java.io.InputStream;
import java.util.Iterator;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.mycompany.tgni.analysis.uima.annotators.text.TextAnnotation;
import com.mycompany.tgni.utils.AnnotatorUtils;
import com.mycompany.tgni.utils.UimaUtils;

/**
 * Annotates sentences within HTML or plain text documents. The
 * documents need to be previously annotated with the Text Annotator.
 * For each annotated text span, this annotator will use the
 * OpenNLP sentence detector to annotate sentences.
 */
public class SentenceAnnotator extends JCasAnnotator_ImplBase {

  private SentenceDetectorME sentenceDetector;
  
  @Override
  public void initialize(UimaContext ctx) 
      throws ResourceInitializationException {
    super.initialize(ctx);
    InputStream stream = null;
    try {
      stream = getContext().getResourceAsStream("SentenceModel");
      SentenceModel model = new SentenceModel(stream);
      sentenceDetector = new SentenceDetectorME(model);
      stream.close();
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    } finally {
      IOUtils.closeQuietly(stream);
    }
  }
  
  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    FSIndex index = jcas.getAnnotationIndex(TextAnnotation.type);
    for (Iterator<TextAnnotation> it = index.iterator(); it.hasNext(); ) {
      TextAnnotation inputAnnotation = it.next();
      int start = inputAnnotation.getBegin();
      if (UimaUtils.MIMETYPE_STRING.equals(jcas.getSofaMimeType())) {
        SentenceAnnotation annotation = new SentenceAnnotation(jcas);
        annotation.setBegin(start);
        annotation.setEnd(inputAnnotation.getEnd());
        annotation.addToIndexes(jcas);
      } else {
        String text = AnnotatorUtils.whiteoutHtml(
          inputAnnotation.getCoveredText());
        Span[] spans = sentenceDetector.sentPosDetect(text);
        for (int i = 0; i < spans.length; i++) {
          SentenceAnnotation annotation = new SentenceAnnotation(jcas);
          annotation.setBegin(start + spans[i].getStart());
          annotation.setEnd(start + spans[i].getEnd());
          annotation.addToIndexes(jcas);
        }
      }
    }
  }
}
