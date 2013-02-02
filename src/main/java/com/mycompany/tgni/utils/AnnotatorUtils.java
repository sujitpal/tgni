package com.mycompany.tgni.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.IntRange;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Common annotator utility methods.
 */
public class AnnotatorUtils {

  private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<.*?>");
  private static final char[] PUNCTUATIONS = new char[] {
    '-', '/', '(', ')', '[', ']', '{', '}', '?', '!', ';', 
    '.', ':'
  };
  
  public static String whiteoutHtml(String s) {
    char[] chars = s.toCharArray();
    Matcher m = HTML_TAG_PATTERN.matcher(s);
    int pos = 0;
    while (m.find(pos)) {
      int start = m.start();
      pos = m.end();
      for (int i = start; i < pos; i++) {
        chars[i] = ' ';
      }
    }
    return new String(chars);
  }
  
  public static String whiteoutPunctuations(String input) {
    return whiteout(input, PUNCTUATIONS);
  }
  
  public static String whiteout(String input, char[] chars) {
    StringBuilder buf = new StringBuilder();
    Set<Character> charset = new HashSet<Character>();
    for (char c : chars) {
      charset.add(c);
    }
    char[] inchars = input.toCharArray();
    for (int i = 0; i < inchars.length; i++) {
      if (charset.contains(inchars[i])) {
        buf.append(" ");
      } else {
        buf.append(inchars[i]);
      }
    }
    return buf.toString();
  }

  public static void whiteout(char[] chars, int start, int end) {
    for (int i = start; i < end; i++) {
      chars[i] = ' ';
    }
  }

  public static String removePunctuations(String s) {
    if (StringUtils.isEmpty(s)) {
      return s;
    }
    char[] c = s.toCharArray();
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < c.length; i++) {
      if (Character.isLetterOrDigit(c[i])) {
        buf.append(c[i]);
      }
    }
    return buf.toString();
  }
  
  public static List<IntRange> getAnnotationSpans(JCas jcas, int type) {
    List<IntRange> annotationSpans = new ArrayList<IntRange>();
    FSIndex<Annotation> fsindex = jcas.getAnnotationIndex(type);
    for (Iterator<Annotation> it = fsindex.iterator(); it.hasNext(); ) {
      Annotation annotation = it.next();
      annotationSpans.add(new IntRange(
        annotation.getBegin(), annotation.getEnd()));
    }
    return annotationSpans;
  }
  
  public static boolean hasContainingAnnotation(
      List<IntRange> annotationSpans, int begin, int end) {
    IntRange currentRange = new IntRange(begin, end);
    for (IntRange annotationSpan : annotationSpans) {
      if (annotationSpan.containsRange(currentRange)) {
        return true;
      }
    }
    return false; 
  }
}
