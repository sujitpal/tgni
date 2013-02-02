package com.mycompany.tgni.analysis.uima.annotators.keyword;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;
import org.junit.Test;

/**
 * TODO: class level javadocs
 */
public class AdjacencyMapTest {

  private String[] dictEntries = new String[] {
    "vitamin a deficiency",
    "vitamin d deficiency",
    "canine vitamin k deficiency",
    "sun burn",
    "wingbat",
  };
  private String[] testStrings = new String[] {
    "Jack had vitamin a and d deficiency", // should not match
    "Jill had vitamin a deficiency", // should match vitamin a deficiency
    "Jack's dog had canine vitamin k deficiency", // should match canine vitamin k deficiency
    "Joe had sun burn but no vitamin deficiency", // should match sun burn
  };
  
//  @Test
//  public void testLoading() throws Exception {
//    Map<String,Set<String>> amap = new HashMap<String,Set<String>>();
//    for (String dictEntry : dictEntries) {
//      String[] words = StringUtils.split(dictEntry, " ");
//      String prevWord = words[0];
//      for (int i = 1; i < words.length; i++) {
//        if (! amap.containsKey(prevWord)) {
//          amap.put(prevWord, new HashSet<String>());
//        }
//        amap.get(prevWord).add(words[i]);
//        prevWord = "_" + words[i];
//      }
//    }
//    System.out.println("amap=" + amap);
//  }
  
//  @Test
//  public void testLoading2() throws Exception {
//    Map<Integer,String> pmap = new HashMap<Integer,String>();
//    MultiMap<String,Integer> amap = new MultiHashMap<String,Integer>();
//    int pos = 0;
//    for (String dictEntry : dictEntries) {
//      String[] words = StringUtils.split(dictEntry, " ");
//      String prevWord = null;
//      for (int i = 0; i < words.length; i++) {
//        pmap.put(pos + i, words[i]);
//        if (i > 0) {
//          amap.put(prevWord, pos + i);
//        }
//        prevWord = (i == 0 ? words[0] : "_" + String.valueOf(pos + i));
//      }
//      pos += words.length;
//    }
//    System.out.println("pmap=" + pmap);
//    System.out.println("amap=" + amap);
//  }
  
  @Test
  public void testMatching() throws Exception {
    // load the collocations into adjacency map
    Map<String,Set<String>> adjmap = new HashMap<String,Set<String>>();
    Map<String,String> dictmap = new HashMap<String,String>();
    for (String dictEntry : dictEntries) {
      dictmap.put(dictEntry, null);
      String[] words = StringUtils.split(dictEntry, " ");
      String prevWord = words[0];
      for (int i = 1; i < words.length; i++) {
        if (! adjmap.containsKey(prevWord)) {
          adjmap.put(prevWord, new HashSet<String>());
        }
        adjmap.get(prevWord).add(words[i]);
        prevWord = "_" + words[i];
      }
    }
    // parse input strings to match
    Stack<Word> collocations = new Stack<Word>();
    for (String testString : testStrings) {
      System.out.println("Processing: " + testString);
      WhitespaceTokenizer tokenizer = new WhitespaceTokenizer(
        Version.LUCENE_40, new StringReader(testString));
      while (tokenizer.incrementToken()) {
        CharTermAttribute term = 
          (CharTermAttribute) tokenizer.getAttribute(
          CharTermAttribute.class);
        OffsetAttribute offset = 
          (OffsetAttribute) tokenizer.getAttribute(
          OffsetAttribute.class);
        Word word = new Word();
        word.word = term.toString();
        word.start = offset.startOffset();
        word.end = offset.endOffset();
//        System.out.println("..word=" + word.word + "(" + word.start + "," + word.end + ")");
        if (collocations.isEmpty()) {
          // no previous word in stack
          if (adjmap.containsKey(word.word)) {
//            System.out.println("..pushing " + word.word);
            collocations.push(word);
          }
        } else {
          // previous word exists, try match
          Word prevWord = collocations.peek();
//          System.out.println("..peek at stack, top=" + prevWord.word);
          Set<String> nextWords = adjmap.get(prevWord.word);
          if (nextWords != null && nextWords.contains(word.word)) {
//            System.out.println("..next word: " + word.word + " found, pushing");
            word.word = "_" + word.word;
            collocations.push(word);
          } else {
            // try matching the full string against dictmap
            List<String> wordlist = new ArrayList<String>();
            int start = collocations.elementAt(0).start;
            int end = 0;
            for (Iterator<Word> it = collocations.iterator(); it.hasNext(); ) {
              Word w = it.next();
              wordlist.add(w.word.startsWith("_") ? w.word.substring(1) : w.word);
              end = w.end;
            }
            String phrase = StringUtils.join(wordlist.iterator(), " ");
//            System.out.println("..phrase=" + phrase);
            if (dictmap.containsKey(phrase)) {
              System.out.println("..matched [" + phrase + "] (" + start + "," + end + ")");
            }
            collocations.clear();
          }
        }
      }
      // mop up anything in stack
      if (! collocations.isEmpty()) {
        // try matching the full string against dictmap
        List<String> wordlist = new ArrayList<String>();
        int start = collocations.elementAt(0).start;
        int end = 0;
        for (Iterator<Word> it = collocations.iterator(); it.hasNext(); ) {
          Word w = it.next();
          wordlist.add(w.word.startsWith("_") ? w.word.substring(1) : w.word);
          end = w.end;
        }
        String phrase = StringUtils.join(wordlist.iterator(), " ");
//        System.out.println("..phrase=" + phrase);
        if (dictmap.containsKey(phrase)) {
          System.out.println("..matched [" + phrase + "] (" + start + "," + end + ")");
        }
        collocations.clear();
      }
    }

  }
  
  private class Word {
    public String word;
    public int start;
    public int end;
  }
}
