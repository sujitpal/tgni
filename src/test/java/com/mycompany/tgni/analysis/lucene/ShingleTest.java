package com.mycompany.tgni.analysis.lucene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.IntRange;
import org.junit.Test;

import com.mycompany.tgni.utils.AnnotatorUtils;

/**
 * Home grown shingle algorithm to eliminate the need for
 * concept subsumption checking. Checks the longest sequence
 * first and if a match is found, the rest of the shingles
 * are ignored.
 */
public class ShingleTest {

  private int SHINGLE_SIZE = 5;
  private String testString = "Attention-deficit/hyperactivity disorder (ADHD) is a chronic condition that affects millions of children and often persists into adulthood. ADHD includes some combination of problems, such as difficulty sustaining attention, hyperactivity and impulsive behavior.";
  
  @Test
  public void testShingle() throws Exception {
    System.out.println(testString);
    List<String> words = new ArrayList<String>();
    List<IntRange> spans = new ArrayList<IntRange>();
    tokenize(testString, words, spans);
    int numWords = words.size();
    int start = 0;
    while (start < numWords) {
      List<IntRange> shingleTokenSpans = getShingleTokenSpans(words, start);
//      System.out.println("all shingle token spans="+ shingleTokenSpans);
      for (IntRange shingleTokenSpan : shingleTokenSpans) {
//        System.out.println("shingle token span=" + shingleTokenSpan);
        int shingleTokenStart = shingleTokenSpan.getMinimumInteger();
        int shingleTokenEnd = shingleTokenSpan.getMaximumInteger();
        IntRange shingleCharOffset = new IntRange(
          spans.get(shingleTokenStart).getMinimumInteger(),
          spans.get(shingleTokenEnd).getMaximumInteger());
        System.out.println(StringUtils.join(
          words.subList(shingleTokenStart, shingleTokenEnd + 1).iterator(), " ") + 
          " (" + shingleCharOffset.getMinimumInteger() + "," +
          shingleCharOffset.getMaximumInteger() + ")");
      }
      start++;
    }
  }
  
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

  private List<IntRange> getShingleTokenSpans(List<String> words, int start) {
    List<IntRange> shingleTokenSpans = new ArrayList<IntRange>();
    int curr = Math.min(start + SHINGLE_SIZE, words.size());
    while (curr > start) {
      shingleTokenSpans.add(new IntRange(start, curr - 1));
      curr--;
    }
    return shingleTokenSpans;
  }
  
//  private List<String> getShingles(List<String> words, int start) {
//    List<String> shingles = new ArrayList<String>();
//    int curr = Math.min(start + SHINGLE_SIZE, words.length);
//    while (curr > start) {
//      shingles.add(StringUtils.join(
//        ArrayUtils.subarray(words, start, curr), " "));
//      curr--;
//    }
//    return shingles;
//  }
}
