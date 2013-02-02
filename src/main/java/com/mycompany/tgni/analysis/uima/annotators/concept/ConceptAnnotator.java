package com.mycompany.tgni.analysis.uima.annotators.concept;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.IntRange;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mycompany.tgni.analysis.uima.annotators.nlp.NounPhraseAnnotation;
import com.mycompany.tgni.beans.TConcept;
import com.mycompany.tgni.services.NodeService;
import com.mycompany.tgni.utils.AnnotatorUtils;
import com.mycompany.tgni.utils.JsonUtils;
import com.mycompany.tgni.utils.UimaUtils;

/**
 * Annotates a sentence annotation with concept annotations by 
 * matching against the Lucene/Neo4j datastore.
 */
public class ConceptAnnotator extends JCasAnnotator_ImplBase {

  private final static Logger logger = 
    LoggerFactory.getLogger(ConceptAnnotator.class);
  
  private static final int SHINGLE_SIZE = 5; 
  
  private NodeService nodeService;
  
  @Override
  public void initialize(UimaContext ctx) 
      throws ResourceInitializationException {
    super.initialize(ctx);
    nodeService = NodeService.getInstance();
  }
  
  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    Set<String> conceptPosOids = new HashSet<String>();
    FSIndex index = jcas.getAnnotationIndex(NounPhraseAnnotation.type);
    for (Iterator<NounPhraseAnnotation> it = index.iterator(); it.hasNext(); ) {
      NounPhraseAnnotation inputAnnotation = it.next();
      int start = inputAnnotation.getBegin();
      String[] phrases; 
      if (StringUtils.isEmpty(inputAnnotation.getCoordExpansions())) {
        phrases = new String[] {inputAnnotation.getCoveredText()};
      } else {
        phrases = StringUtils.split(
          inputAnnotation.getCoordExpansions(), 
          UimaUtils.MULTI_VALUED_FIELD_SEPARATOR);
      }
      for (String phrase : phrases) {
        System.out.println("noun phrase=" + phrase);
        // replace HTML fragments with whitespace
        List<String> words = new ArrayList<String>();
        List<IntRange> spans = new ArrayList<IntRange>();
        tokenize(AnnotatorUtils.whiteoutHtml(phrase), words, spans);
        int numWords = words.size();
        int phraseTokenStart = 0;
        while (phraseTokenStart < numWords) {
          List<IntRange> shingleTokenSpans = getShingleTokenSpans(words, phraseTokenStart);
          for (IntRange shingleTokenSpan : shingleTokenSpans) {
            int shingleTokenStart = shingleTokenSpan.getMinimumInteger();
            int shingleTokenEnd = shingleTokenSpan.getMaximumInteger();
            String shingle = StringUtils.join(words.subList(
              shingleTokenStart, shingleTokenEnd + 1).iterator(), 
              " ");
            // pass the shingle to the NodeService
            try {
              List<TConcept> concepts = nodeService.getConcepts(shingle);
              if (concepts.size() > 0) {
                for (TConcept concept : concepts) {
                  int startOffset = start + spans.get(
                    shingleTokenStart).getMinimumInteger();
                  int endOffset = start + spans.get(
                    shingleTokenEnd).getMaximumInteger();
                  String conceptPosOid = getConceptPosOid(concept, 
                    startOffset, endOffset);
                  if (conceptPosOids.contains(conceptPosOid)) {
                    // eliminate duplicate mappings for the 
                    // same text to the same concept
                    continue;
                  }
                  conceptPosOids.add(conceptPosOid);
                  ConceptAnnotation annotation = 
                    new ConceptAnnotation(jcas);
                  annotation.setBegin(startOffset);
                  annotation.setEnd(endOffset);
                  annotation.setOid(concept.getOid());
                  annotation.setPname(concept.getPname());
                  List<String> stycodes = new ArrayList<String>();
                  stycodes.addAll(concept.getStycodes().keySet());
                  annotation.setStycodes(JsonUtils.listToString(stycodes));
                  annotation.setStygroup(concept.getStygrp());
                  annotation.addToIndexes(jcas);
                }
                // already found a concept for the longest shingle,
                // skip subsequent shingles in this span (concept
                // subsumption)
                break;
              }
            } catch (Exception e) {
              throw new AnalysisEngineProcessException(e);
            }
          }
          phraseTokenStart++;
        }
      }
    }
  }

  /**
   * Generate unique key of concept with position, so duplicate
   * mappings can be removed.
   * @param concept the concept to generate key for.
   * @param start start character position in input string.
   * @param end end character position in input string.
   * @return a unique composite key.
   */
  private String getConceptPosOid(TConcept concept, int start, int end) {
    return StringUtils.join(new String[] {
      String.valueOf(concept.getOid()),
      String.valueOf(start),
      String.valueOf(end)}, 
      UimaUtils.MULTI_VALUED_FIELD_SEPARATOR);
  }

  /**
   * Remove punctuations and tokenize the input phrase into
   * a List of words and word spans.
   * @param phrase the input phrase.
   * @param words a reference to the Word list.
   * @param spans a reference to the Span list.
   */
  private void tokenize(String phrase, List<String> words,
      List<IntRange> spans) {
    String s = AnnotatorUtils.whiteoutPunctuations(phrase);
    char[] cs = s.toCharArray();
    char prevChar = 0;
    int wordStart = 0;
    for (int i = 0; i < cs.length; i++) {
      if (cs[i] != ' ' && prevChar == ' ') {
        // beginning of word
        wordStart = i;
      }
      if (cs[i] == ' ' && prevChar != ' ') {
        // end of word
        words.add(s.substring(wordStart, i));
        spans.add(new IntRange(wordStart, i));
      }
      prevChar = cs[i];
    }
    if (wordStart < cs.length && prevChar != ' ') {
      words.add(s.substring(wordStart, cs.length));
      spans.add(new IntRange(wordStart, cs.length));
    }
//    for (int i = 0; i < words.size(); i++) {
//      System.out.println(i + ": " + words.get(i) + 
//        " (" + spans.get(i).getMinimumInteger() + 
//        "," + spans.get(i).getMaximumInteger() + ")");
//    }
  }

  /**
   * Given a List of word strings and a start position, return
   * a List of token spans that represent shingles produced from
   * the word. The longest shingle is returned first, so concept
   * subsumption can be enforced (ie, if a longer span can be 
   * mapped, then sub-spans of that shingle should not be mapped).
   * @param words the List of words.
   * @param start the start token position.
   * @return a List of spans representing shingles.
   */
  private List<IntRange> getShingleTokenSpans(List<String> words, int start) {
    List<IntRange> shingleTokenSpans = new ArrayList<IntRange>();
    int curr = Math.min(start + SHINGLE_SIZE, words.size());
    while (curr > start) {
      shingleTokenSpans.add(new IntRange(start, curr - 1));
      curr--;
    }
    return shingleTokenSpans;
  }
}
