package com.mycompany.tgni.analysis.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Accumulates all the terms that are emitted from the underlying
 * tokenizer/filter chain, and creates multiple terms for storage
 * into a Lucene index.
 */
public class TokenConcatenatingTokenFilter extends TokenFilter {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  private CharTermAttribute termAttr;
  private PositionIncrementAttribute posIncAttr;
  private boolean alphaSortTokens = false;
  
  private AttributeSource.State current;
  private LinkedList<List<String>> words;
  private LinkedList<String> phrases;

  private boolean concat = false;
  private int maxLoop = 1000;
  
  protected TokenConcatenatingTokenFilter(TokenStream input, 
      boolean alphaSortTokens) {
    super(input);
    this.alphaSortTokens = alphaSortTokens;
    this.termAttr = addAttribute(CharTermAttribute.class);
    this.posIncAttr = addAttribute(PositionIncrementAttribute.class);
    this.words = new LinkedList<List<String>>();
    this.phrases = new LinkedList<String>();
  }

  @Override
  public boolean incrementToken() throws IOException {
    logger.debug("TokenConcatTokenFilter.incrementToken");
    if (maxLoop <= 0) {
      logger.debug("Breaking out of TokenConcatenatingTokenFilter (maxLoop)");
      maxLoop = 1000;
      return false;
    } else {
      maxLoop--;
    }
    int i = 0;
    while (input.incrementToken()) {
      String term = new String(termAttr.buffer(), 0, termAttr.length());
      if (posIncAttr.getPositionIncrement() == 0 && i > 0) {
        // upstream filter has marked this as a synonym, 
        // place the word at the same position as previous
        // word
        words.get(i).add(term);
      } else {
        // create a word position and populate it
        List<String> word = new ArrayList<String>();
        word.add(term);
        words.add(word);
      }
//      List<String> word = posIncAttr.getPositionIncrement() > 0 ?
//        new ArrayList<String>() : words.removeLast();
//      word.add(term);
//      words.add(word);
      i++;
    }
    // now write out as a single token
    if (! concat) {
      makePhrases(words, phrases, 0);
      if (alphaSortTokens) {
        // alpha-sort the phrases
        List<String> sortedPhrases = new ArrayList<String>();
        for (String phrase : phrases) {
          List<String> words = new ArrayList<String>();
          words.addAll(Arrays.asList(StringUtils.split(phrase, " ")));
          Collections.sort(words);
          sortedPhrases.add(StringUtils.join(words, " "));
        }
        phrases.clear();
        phrases.addAll(sortedPhrases);
      }
      concat = true;
    }
    while (phrases.size() > 0) {
      String phrase = phrases.removeFirst();
      restoreState(current);
      clearAttributes();
      termAttr.copyBuffer(phrase.toCharArray(), 0, phrase.length());
      termAttr.setLength(phrase.length());
      current = captureState();
      return true;
    }
    concat = false;
    return false;
  }
  
  private void makePhrases(List<List<String>> words, 
      List<String> phrases, int currPos) {
    if (currPos == words.size()) {
      return;
    }
    if (phrases.size() == 0) {
      phrases.addAll(words.get(currPos));
    } else {
      List<String> newPhrases = new ArrayList<String>();
      for (String phrase : phrases) {
        for (String word : words.get(currPos)) {
          newPhrases.add(StringUtils.join(new String[] {phrase, word}, " "));
        }
      }
      phrases.clear();
      phrases.addAll(newPhrases);
    }
    makePhrases(words, phrases, currPos + 1);
  }
}
