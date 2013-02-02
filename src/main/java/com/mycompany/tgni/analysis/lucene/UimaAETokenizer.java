package com.mycompany.tgni.analysis.lucene;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.IntRange;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mycompany.tgni.analysis.uima.annotators.keyword.KeywordAnnotation;
import com.mycompany.tgni.utils.UimaUtils;

/**
 * Tokenizes a block of text from the passed in reader and
 * annotates it with the specified UIMA Analysis Engine. Terms
 * in the text that are not annotated by the Analysis Engine
 * are split on whitespace and punctuation. Attributes available:
 * CharTermAttribute, OffsetAttribute, PositionIncrementAttribute
 * and KeywordAttribute. 
 */
public final class UimaAETokenizer extends Tokenizer {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  private final CharTermAttribute termAttr;
  private final OffsetAttribute offsetAttr;
  private final PositionIncrementAttribute posIncAttr;
  private final KeywordAttribute keywordAttr;

  private AttributeSource.State current;
  private SynonymMap synmap;
  private LinkedList<IntRange> rangeList;
  private Map<IntRange,Object> rangeMap;
  private Reader reader = null;
  private boolean eof = false;
  private AnalysisEngine queryMappingAE;
  private JCas jcas = null;

  private static final Pattern PUNCT_OR_SPACE_PATTERN = 
    Pattern.compile("[\\p{Punct}\\s+]");
  private static final String SYN_DELIMITER = "__";
  
  private int maxLoop = 1000; // debugging
  
  public UimaAETokenizer(Reader input, 
      String aeDescriptor, SynonymMap synonymMap) {
    super(input);
    // validate inputs
    // get UIMA artifacts
    try {
      this.queryMappingAE = UimaUtils.getAE(aeDescriptor, null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    if (synonymMap == null) {
      throw new RuntimeException(
        "Need valid (non-null) reference to a SynonymMap");
    }
    synmap = synonymMap;
    reader = new BufferedReader(input);
    // set available attributes
    termAttr = addAttribute(CharTermAttribute.class);
    offsetAttr = addAttribute(OffsetAttribute.class);
    posIncAttr = addAttribute(PositionIncrementAttribute.class);
    keywordAttr = addAttribute(KeywordAttribute.class);
    // initialize variables
    rangeList = new LinkedList<IntRange>();
    rangeMap = new HashMap<IntRange,Object>();
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public boolean incrementToken() throws IOException {
    logger.debug("UimaAETokenizer.incrementToken");
    if (maxLoop <= 0) {
      logger.debug("Breaking out of UimaAETokenizer (maxLoop)");
      maxLoop = 1000;
      return false;
    } else {
      maxLoop--;
    }
    // if no more tokens, return
    if (eof) {
      return false;
    }
    // look for next token in buffer
    if (rangeList.size() > 0) {
      populateAttributes();
      current = captureState();
      restoreState(current);
      return true;
    }
    // analyze input and buffer tokens
    clearAttributes();
    rangeList.clear();
    rangeMap.clear();
    try {
      List<String> texts = IOUtils.readLines(reader);
      for (String text : texts) {
        jcas = UimaUtils.runAE(queryMappingAE, text, 
          UimaUtils.MIMETYPE_STRING, jcas);
        FSIndex<? extends Annotation> fsindex = 
          jcas.getAnnotationIndex(KeywordAnnotation.type);
        int pos = 0;
        for (Iterator<? extends Annotation> it = fsindex.iterator();
            it.hasNext(); ) {
          KeywordAnnotation annotation = (KeywordAnnotation) it.next();
          int begin = annotation.getBegin();
          int end = annotation.getEnd();
          if (pos < begin) {
            // this is plain text, split this up by whitespace
            // into individual terms
            addNonAnnotatedTerms(pos, text.substring(pos, begin));
          }
          IntRange range = new IntRange(begin, end);
          mergeAnnotationInfo(range, annotation);     
          pos = end;
        }
        if (pos < text.length()) {
          addNonAnnotatedTerms(pos, text.substring(pos));
        }
        current = captureState();
      }
    } catch (Exception e) {
      throw new IOException(e);
    }
    // return the first term from rangeList
    if (rangeList.size() > 0) {
      populateAttributes();
      return true;
    } else {
      return false;
    }
  }
  
  private void populateAttributes() {
    // return buffered tokens one by one. If current
    // token has an associated UimaAnnotationAttribute,
    // then set the attribute in addition to term
    if (rangeList.size() == 0) {
      eof = true;
    } else {
      IntRange range = rangeList.removeFirst();
      if (rangeMap.containsKey(range)) {
        Object rangeValue = rangeMap.get(range);
        if (rangeValue instanceof KeywordAnnotation) {
          // this is a UIMA Keyword annotation
          KeywordAnnotation annotation = (KeywordAnnotation) rangeValue;
          String term = annotation.getCoveredText();
          String transformedValue = annotation.getTransformedValue();
          if (StringUtils.isNotEmpty(transformedValue)) {
            List<Token> tokens = SynonymMap.makeTokens(
              Arrays.asList(StringUtils.split(
              transformedValue, SYN_DELIMITER)));
            // rather than add all the synonym tokens in a single
            // add, we have to do this separately to ensure that
            // the position increment attribute is set to 0 for
            // all the synonyms, not just the first one
            for (Token token : tokens) {
              synmap.add(Arrays.asList(term), Arrays.asList(token), true, true);
            }
          }
          offsetAttr.setOffset(annotation.getBegin(), 
            annotation.getEnd());
          termAttr.copyBuffer(term.toCharArray(), 0, term.length());
          termAttr.setLength(term.length());
          keywordAttr.setKeyword(true);
          posIncAttr.setPositionIncrement(1);
        } else {
          // this is a plain text term
          String term = (String) rangeValue;
          termAttr.copyBuffer(term.toCharArray(), 0, term.length());
          termAttr.setLength(term.length());
          offsetAttr.setOffset(range.getMinimumInteger(), 
            range.getMaximumInteger());
          keywordAttr.setKeyword(false);
          posIncAttr.setPositionIncrement(1);
        }
      }
    }
  }

  private void addNonAnnotatedTerms(int pos, String snippet) {
    int start = 0;
    Matcher m = PUNCT_OR_SPACE_PATTERN.matcher(snippet);
    while (m.find(start)) {
      int begin = m.start();
      int end = m.end();
      if (start == begin) {
        // this is a punctuation character, skip it
        start = end;
        continue;
      }
      IntRange range = new IntRange(pos + start, pos + begin);
      rangeList.add(range);
      rangeMap.put(range, snippet.substring(start, begin));
      start = end; 
    }
    // take care of trailing string in snippet
    if (start < snippet.length()) {
      IntRange range = new IntRange(pos + start, pos + snippet.length());
      rangeList.add(range);
      rangeMap.put(range, snippet.substring(start));
    }
  }

  private void mergeAnnotationInfo(IntRange range, 
      KeywordAnnotation annotation) {
    // verify if the range has not already been recognized.
    // this is possible if multiple AEs recognize and act
    // on the same pattern/dictionary entry
    if (rangeMap.containsKey(range) &&
        rangeMap.get(range) instanceof KeywordAnnotation) {
      KeywordAnnotation prevAnnotation = 
        (KeywordAnnotation) rangeMap.get(range);
      Set<String> synonyms = new HashSet<String>();
      if (StringUtils.isNotEmpty(
          prevAnnotation.getTransformedValue())) {
        synonyms.addAll(Arrays.asList(StringUtils.split(
          prevAnnotation.getTransformedValue(), SYN_DELIMITER)));
      }
      if (StringUtils.isNotEmpty(annotation.getTransformedValue())) {
        synonyms.addAll(Arrays.asList(StringUtils.split(
          annotation.getTransformedValue(), SYN_DELIMITER)));
      }
      annotation.setTransformedValue(StringUtils.join(
        synonyms.iterator(), SYN_DELIMITER));
      rangeMap.put(range, annotation);
    } else {
      rangeList.add(range);
      rangeMap.put(range, annotation);
    }
  }
}
