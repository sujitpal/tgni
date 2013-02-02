package com.mycompany.tgni.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import opennlp.tools.util.Pair;
import opennlp.tools.util.Span;

import org.apache.commons.lang.math.IntRange;

/**
 * Group of (possibly unrelated) utility methods for NLP.
 */
public class NlpUtils {

  @SuppressWarnings("deprecation")
  public static List<Pair<String,IntRange>> getChunkTags(String[] tokens, 
      String[] tags, Span[] chunks) {
    List<Pair<String,IntRange>> chunkTags = 
      new ArrayList<Pair<String,IntRange>>();
    int chunkpos = 0;
    int tokenpos = 0;
    while (tokenpos < tokens.length) {
      if (chunkpos < chunks.length && chunks[chunkpos].getStart() == tokenpos) {
        chunkTags.add(new Pair<String,IntRange>(
          chunks[chunkpos].getType(), 
          new IntRange(chunks[chunkpos].getStart(), 
          chunks[chunkpos].getEnd())));
        tokenpos = chunks[chunkpos].getEnd();
        chunkpos++;
      } else {
        chunkTags.add(new Pair<String,IntRange>(
          tags[tokenpos], new IntRange(tokenpos, tokenpos + 1)
        ));
        tokenpos++;
      }
    }
    return chunkTags;
  }
  
  public static List<String> getTokenTags(String[] tokens, 
      String[] tags, Span chunk) {
    List<String> tokenTags = new ArrayList<String>();
    for (int i = chunk.getStart(); i < chunk.getEnd(); i++) {
      tokenTags.add(tags[i]);
    }
    return tokenTags;
  }
  
  public static IntRange getPhraseRange(String sentence, 
      String chunkView, Map<Integer,IntRange> chunkPositions, 
      Span[] tokenSpans, int start, int end) {
    int startTagMatch = start;
    int endTagMatch = chunkView.lastIndexOf(' ', end - 1) + 1;
    int startToken = chunkPositions.get(startTagMatch).getMinimumInteger();
    int endToken = chunkPositions.get(endTagMatch).getMaximumInteger();
    return new IntRange(tokenSpans[startToken].getStart(), 
      tokenSpans[endToken-1].getEnd());
  }
  
  public static String getPhrase(String sentence, 
      String chunkView, Map<Integer,IntRange> chunkPositions, 
      Span[] tokenSpans, int start, int end) {
    IntRange phraseRange = getPhraseRange(sentence, chunkView, 
      chunkPositions, tokenSpans, start, end);
    return sentence.substring(phraseRange.getMinimumInteger(), 
      phraseRange.getMaximumInteger());
  }
  
  public static String getChunkingDebugInfo(String[] tokens, String[] tags, Span[] chunks) {
    StringBuilder buf = new StringBuilder();
    int ci = 0;
    for (int i = 0; i < tokens.length; i++) {
      if (ci < chunks.length && chunks[ci].getStart() == i) {
        buf.append("[");
      }
      buf.append(tokens[i]).append("/").append(tags[i]).append(" ");
      if (ci < chunks.length && chunks[ci].getEnd() - 1 == i) {
        buf.append("]/").append(chunks[ci].getType()).append(" ");
        ci++;
      }
    }
    return buf.toString();
  }
}
