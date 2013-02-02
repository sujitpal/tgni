package com.mycompany.tgni.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.IntRange;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.Version;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import com.mycompany.tgni.analysis.lucene.StopFilter;
import com.mycompany.tgni.analysis.uima.annotators.keyword.KeywordAnnotation;
import com.mycompany.tgni.utils.UimaUtils;

/**
 * Wraps the UIMA Taxonomy Mapping Analyzer and a Lucene 
 * analyzer chain to produce a set of normalized phrases for 
 * a given input string.
 */
public class NameNormalizer {

  private String stopwordFile;
  private String aeDescriptor;
  
  private AnalysisEngine ae;
  private JCas jcas;
  private Analyzer analyzer;
  
  public NameNormalizer(String stopwordFile, String aeDescriptor) {
    this.stopwordFile = stopwordFile;
    this.aeDescriptor = aeDescriptor;
  }
  
  public NameNormalizer(AnalysisEngine ae, Analyzer analyzer,
      JCas jcas) throws Exception {
    this.ae = ae;
    this.analyzer = analyzer;
    this.jcas = ae.newJCas();
  }
  
  /**
   * Alternate constructor to be used by multi-threaded data
   * loader. Do not call init if using this constructor, you
   * are basically passing in objects that init() would have
   * created. You do need to call destroy though.
   * Ctor: @param ae
   * Ctor: @param analyzer
   * Ctor: @throws Exception
   */
  public NameNormalizer(AnalysisEngine ae, Analyzer analyzer) 
      throws Exception {
    this.ae = ae;
    this.analyzer = analyzer;
    this.jcas = ae.newJCas();
  }
  
  public void init() throws Exception {
    this.ae = UimaUtils.getAE(aeDescriptor, null);
    this.jcas = ae.newJCas();
    this.analyzer = getAnalyzer(makeStopSet());
  }
  
  public void destroy() throws Exception {
    this.ae.destroy();
    this.analyzer.close();
    this.jcas.release();
  }
  
  public List<String> normalize(String input) throws Exception {
    // run the annotator
    jcas.reset();
    jcas.setSofaDataString(input, UimaUtils.MIMETYPE_STRING);
    ae.process(jcas);
    // first pass: list of annotations to consider
    List<KeywordAnnotation> annotations = new ArrayList<KeywordAnnotation>();
    FSIndex<? extends Annotation> index = jcas.getAnnotationIndex(KeywordAnnotation.type);
    for (Iterator<? extends Annotation> it = index.iterator(); it.hasNext(); ) {
      annotations.add((KeywordAnnotation) it.next());
    }
    List<KeywordAnnotation> sortedAnnotations = new ArrayList<KeywordAnnotation>();
    sortedAnnotations.addAll(annotations);
    Collections.sort(sortedAnnotations, new Comparator<KeywordAnnotation>() {
      @Override
      public int compare(KeywordAnnotation a1, KeywordAnnotation a2) {
        Integer span1 = a1.getEnd() - a1.getBegin();
        Integer span2 = a2.getEnd() - a2.getBegin();
        return span2.compareTo(span1);
      }
    });
    // remove overlapping annotations, so longer ones are kept
    Set<IntRange> annotationsToRemove = new HashSet<IntRange>();
    OpenBitSet bitset = new OpenBitSet(input.length());
    bitset.set(0, input.length());
    long prevCardinality = bitset.cardinality();
    for (KeywordAnnotation sortedAnnotation : sortedAnnotations) {
      bitset.flip(sortedAnnotation.getBegin(), sortedAnnotation.getEnd());
      long cardinality = bitset.cardinality();
      if (cardinality == prevCardinality) {
        annotationsToRemove.add(new IntRange(
          sortedAnnotation.getBegin(), sortedAnnotation.getEnd()));
      }
    }
    // analyze the annotations produced and build a set of
    // phrases to send to the analyzer chain
    List<List<String>> normalizedWords = new ArrayList<List<String>>();
    int prevBegin = 0;
    for (KeywordAnnotation annotation : annotations) {
      int begin = annotation.getBegin();
      int end = annotation.getEnd();
      String[] transforms = new String[0];
      if (StringUtils.isNotEmpty(annotation.getTransformedValue())) {
        transforms = StringUtils.split(
            annotation.getTransformedValue(), "__");
      }
      if (annotationsToRemove.contains(new IntRange(begin, end))) {
        continue;
      }
      if (begin > prevBegin) {
        String normalized = normalize(analyzer,  
          input.substring(prevBegin, begin));
        if (StringUtils.isNotEmpty(normalized)) {
          normalizedWords.add(Arrays.asList(normalized));
        }
      }
      List<String> wordList = new ArrayList<String>();
      wordList.add(annotation.getCoveredText());
      for (String transform : transforms) {
        wordList.add(transform);
      }
      normalizedWords.add(wordList);
      prevBegin = end;
    }
    // wipe up the last one
    if (prevBegin < input.length()) {
      String normalized = normalize(analyzer, 
        input.substring(prevBegin, input.length()));
      if (StringUtils.isNotEmpty(normalized)) {
        normalizedWords.add(Arrays.asList(normalized));
      }
    }
    // now flatten it into a bunch of phrases
    List<String> phrases = new ArrayList<String>();
    flatten(normalizedWords, phrases, 0);
    // alphabetize the words in the string
    List<String> alphaSortedPhrases = new ArrayList<String>();
    for (String phrase : phrases) {
      alphaSortedPhrases.add(alphaSort(phrase));
    }
    phrases.clear();
    phrases.addAll(alphaSortedPhrases);
    return phrases;
  }

  private String alphaSort(String phrase) {
    List<String> words = Arrays.asList(StringUtils.split(phrase, " "));
    Collections.sort(words);
    return StringUtils.join(words.iterator(), " ");
  }

  private Set<String> makeStopSet() throws IOException {
    Set<String> stopwords = new HashSet<String>();
    BufferedReader reader = new BufferedReader(new FileReader(
      new File(UimaUtils.getTgniHome(), "conf/stopwords.txt")));
    String line = null;
    while ((line = reader.readLine()) != null) {
      if (StringUtils.isEmpty(line) || line.startsWith("#")) {
        continue;
      }
      stopwords.add(StringUtils.trim(line));
    }
    reader.close();
    return stopwords;
  }

  private Pattern PUNCT_PATTERN = Pattern.compile("\\p{Punct}");
  
  private String normalize(Analyzer analyzer, String name) 
      throws IOException {
    List<String> terms = new ArrayList<String>();
    StringReader reader = null;
    try {
      reader = new StringReader(name);
      TokenStream tokenStream = analyzer.tokenStream("f", reader);
      while (tokenStream.incrementToken()) {
        CharTermAttribute termAttr = tokenStream.getAttribute(CharTermAttribute.class);
        String term = new String(termAttr.buffer(), 0, termAttr.length());
        if (StringUtils.isEmpty(term)) continue;
        Matcher m = PUNCT_PATTERN.matcher(term);
        if (m.matches()) continue;
        terms.add(term);
      }
    } finally {
      IOUtils.closeQuietly(reader);
    }
    return StringUtils.join(terms.iterator(), " ");
  }

  private void flatten(List<List<String>> normalizedWords, List<String> phrases,
      int currentWord) {
//    System.out.println("normalized words=" + normalizedWords);
//    System.out.println("phrases=" + phrases);
    if (currentWord == normalizedWords.size()) {
      return;
    }
    List<String> newPhrases = new ArrayList<String>();
    List<String> wordList = normalizedWords.get(currentWord);
    if (phrases.size() == 0) {
      for (String word : wordList) {
        newPhrases.add(word);
      }
    } else {
      for (String phrase : phrases) {
        for (String word : wordList) {
          newPhrases.add(StringUtils.join(new String[] {phrase, word}, " "));
        }
      }
    }
    phrases.clear();
    phrases.addAll(newPhrases);
    flatten(normalizedWords, phrases, currentWord + 1);
  }

  private Analyzer getAnalyzer(final Set<String> stopset) {
    return new Analyzer() {
      @Override
      public TokenStream tokenStream(String fieldname, Reader reader) {
        TokenStream input = new StandardTokenizer(Version.LUCENE_40, reader);
        input = new LowerCaseFilter(Version.LUCENE_40, input);
        input = new StopFilter(Version.LUCENE_40, input, stopset);;
        input = new PorterStemFilter(input);
        return input;
      }
    };
  }

}
