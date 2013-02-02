package com.mycompany.tgni.analysis.uima.annotators.nlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.mycompany.tgni.utils.UimaUtils;

/**
 * Finds noun-phrases in sentences, detects if these phrases are
 * candidates for coordinate expansion, expands them and annotates
 * the resulting expanded forms at the same location as the parent
 * as NounPhraseAnnotations.
 */
public class CoordExpAnnotator extends JCasAnnotator_ImplBase {

  private static final Set<String> PREFIX_WORDS = 
    new HashSet<String>(Arrays.asList(new String[] {
    "pre", "post", "hypo", "hyper", "inter", "intra", 
    "over", "under", "infra", "ultra", "hetero", "homo",
  }));

  @Override
  public void initialize(UimaContext ctx) 
      throws ResourceInitializationException {
    super.initialize(ctx);
  }
  
  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    if (UimaUtils.MIMETYPE_STRING.equals(jcas.getSofaMimeType())) {
      return;
    }
    FSIndex fsindex = jcas.getAnnotationIndex(NounPhraseAnnotation.type);
    for (Iterator<NounPhraseAnnotation> it = fsindex.iterator(); it.hasNext(); ) {
      NounPhraseAnnotation annotation = it.next();
      String phrase = annotation.getCoveredText();
      if (phrase.contains("/") ||
          phrase.contains(" and") ||
          phrase.contains(" or")) {
        List<String> coordinateExpansions = expandPhrase(phrase);
        if (coordinateExpansions.size() > 0) {
          annotation.setCoordExpansions(StringUtils.join(
            coordinateExpansions.iterator(), 
            UimaUtils.MULTI_VALUED_FIELD_SEPARATOR));
        }
      }
    }
  }

  /**
   * Coordinate phrase expander. Will return a list of expansions
   * if possible, else returns an empty list.
   * @param phrase the incoming phrase to expand.
   * @return a List of expansions if found.
   */
  private List<String> expandPhrase(String phrase) {
    List<String> expandedPhrases = new ArrayList<String>();
    if (phrase.contains("/")) {
      // handles the following cases:
      // xx A/B/.. yy => xx A yy, xx B yy, ...
      int slashPos = phrase.indexOf('/');
      int wordStart = phrase.lastIndexOf(' ', slashPos-1) + 1;
      if (wordStart == -1) wordStart = 0;
      int wordEnd = phrase.indexOf(' ', slashPos+1);
      if (wordEnd == -1) wordEnd = phrase.length();
      String[] ccwords = StringUtils.split(phrase.substring(wordStart, wordEnd), "/");
      // check for similarity between elements of A/B/C
      // similar if all elements are same size or has overlap
      boolean isSimilar = false;
      String prevWord = ccwords[0];
      for (int i = 1; i < ccwords.length; i++) {
        if (ccwords[i].length() == prevWord.length()) {
          isSimilar = true;
          continue;
        }
        double similarity = computeStringSimilarity(prevWord, ccwords[i]);
        if (similarity == 0) {
          isSimilar = false;
          break;
        } else {
          isSimilar = true;
        }
        prevWord = ccwords[i];
      }
      if (isSimilar) {
        String phrasePrefix = phrase.substring(0, wordStart-1);
        String phraseSuffix = phrase.substring(wordEnd+1);
        for (String ccword : ccwords) {
          expandedPhrases.add(StringUtils.join(new String[] {
            phrasePrefix, ccword, phraseSuffix}, " "));
        }
      }
    } else {
      // handles phrases with "and" and "or" in them
      // first pass, find the position of the CC token
      String[] words = StringUtils.split(phrase.replaceAll("-", " "), " ");
      List<String> ccwords = new ArrayList<String>();
      int ccpos = -1;
      for (int i = 0; i < words.length; i++) {
        if ("and".equalsIgnoreCase(words[i]) ||
            "or".equalsIgnoreCase(words[i])) {
          ccpos = i;
          break;
        }
      }
      if (ccpos > -1) {
        String phrasePre = "";
        String phrasePost = "";
        if (ccpos > 0 && ccpos < words.length - 1) {
          // handles the following cases:
          // xx A (and|or) B C yy => xx A C yy, xx B C yy
          // xx A B (and|or) C yy => xx A C yy, xx B C yy
          ccwords.add(words[ccpos - 1]);
          ccwords.add(words[ccpos + 1]);
          // look back from ccpos-1 until we stop seeing
          // words with trailing commas
          int currpos = ccpos - 2;
          while (currpos >= 0) {
            if (words[currpos].endsWith(",")) {
              ccwords.add(words[currpos].substring(
                  0, words[currpos].length() - 1));
              currpos--;
            } else {
              break;
            }
          }
          if (currpos >= 0) {
            phrasePre = StringUtils.join(
              Arrays.asList(ArrayUtils.subarray(
              words, 0, currpos+1)), " ");
          }
          if (ccpos + 2 < words.length) {
            phrasePost = StringUtils.join(
              Arrays.asList(ArrayUtils.subarray(
              words, ccpos + 2, words.length)), " ");
          }
        }
        // expand ccwords in case one or more of them are
        // known prefixes
        ccwords = expandPrefixes(ccwords);
        for (String ccword : ccwords) {
          ccword = ccword.replace(",", "").
            replace(".", "");
          expandedPhrases.add(StringUtils.trim(
            StringUtils.join(new String[] {
            phrasePre, ccword, phrasePost}, " ")));
        }
      }
    }
    return expandedPhrases;
  }
    
  /**
   * Expand patterns of the form "overt hypo- and hyperthyroidism"
   * to "over hypothyroidism and hyperthyroidism". This is done by
   * maintaining a set of known prefix words, and using them to 
   * compute the suffix from the full word, then attaching this
   * suffix to all the other partial words.
   * @param ccwords the list of coordinate words.
   * @return the list of coordinate words with expansion.
   */
  private List<String> expandPrefixes(List<String> ccwords) {
    List<String> prefixExpansions = new ArrayList<String>();
    String fullWord = null;
    List<String> partialWords = new ArrayList<String>();
    for (String ccword : ccwords) {
      if (ccword.endsWith("-")) {
        ccword = ccword.substring(0, ccword.length() - 1);
      }
      // partition the input into the full word and partials
      if (PREFIX_WORDS.contains(StringUtils.lowerCase(ccword))) {
        partialWords.add(ccword);
      } else {
        fullWord = ccword;
      }
    }
    if (StringUtils.isEmpty(fullWord) ||
        partialWords.size() < ccwords.size() - 1) {
      // no prefix expansion necessary, return input
      prefixExpansions.addAll(ccwords);
    } else {
      String suffix = null;
      for (String prefix : PREFIX_WORDS) {
        if (StringUtils.lowerCase(fullWord).startsWith(prefix)) {
          suffix = fullWord.substring(prefix.length());
          break;
        }
      }
      if (StringUtils.isNotEmpty(suffix)) {
        for (String prefixWord : partialWords) {
          prefixExpansions.add(prefixWord + suffix);
        }
        prefixExpansions.add(fullWord);
      } else {
        // prefix unknown, skip expansion and return input
        prefixExpansions.addAll(ccwords);
      }
    }
    return prefixExpansions;
  }

  /**
   * Implementation of strike-a-match string similarity
   * copy-pasted from author's website.
   * @param s1 the first string.
   * @param s2 the second string.
   * @return a similarity score based on intersection of
   *         letter pairs.
   */
  private double computeStringSimilarity(String s1, String s2) {
    ArrayList<String> pairs1 = letterPairs(s1);
    ArrayList<String> pairs2 = letterPairs(s2);
    int intersection = 0;
    int union = pairs1.size() + pairs2.size();
    for (int i = 0; i < pairs1.size(); i++) {
      Object pair1 = pairs1.get(i);
      for(int j = 0; j < pairs2.size(); j++) {
        Object pair2 = pairs2.get(j);
        if (pair1.equals(pair2)) {
          intersection++;
          pairs2.remove(j);
          break;
        }
      }
    }
    return (2.0 * intersection) / union;
  }

  private ArrayList<String> letterPairs(String str) {
    int numPairs = str.length()-1;
    ArrayList<String> pairs = new ArrayList<String>();
    for (int i = 0; i < numPairs; i++) {
        pairs.add(str.substring(i, i + 2));
    }
    return pairs;
  }
}
