package com.mycompany.tgni.analysis.uima.annotators.keyword;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.IntRange;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;

import com.mycompany.tgni.analysis.uima.conf.SharedMapResource;
import com.mycompany.tgni.analysis.uima.conf.SharedSetResource;
import com.mycompany.tgni.utils.AnnotatorUtils;

/**
 * Annotates patters found in input text. Operates in preserve
 * or transform mode. In preserve mode, recognizes and annotates
 * a set of supplied dictionary words. In transform mode, the
 * recognized words are annotated and the transformed value 
 * set into the annotation. Default matching is case-insensitive
 * but can be overriden using ignoreCase config parameter. Multi-
 * word patterns can be specified in the dictionaries (upto a 
 * maximum size of maxShingleSize (default 5).
 */
public class DictionaryAnnotator extends JCasAnnotator_ImplBase {

  private String preserveOrTransform;
  private boolean ignoreCase;
  private int maxShingleSize = 5;

  private Set<String> dictSet;
  private Map<String,String> dictMap;
  
  private final static String PRESERVE = "preserve";
  private final static String TRANSFORM = "transform";

  @Override
  public void initialize(UimaContext ctx) 
      throws ResourceInitializationException {
    super.initialize(ctx);
    preserveOrTransform = (String) ctx.getConfigParameterValue("preserveOrTransform");
    ignoreCase = (Boolean) ctx.getConfigParameterValue("ignoreCase");
    maxShingleSize = (Integer) ctx.getConfigParameterValue("maxShingleSize");
    try {
      if (PRESERVE.equals(preserveOrTransform)) {
        SharedSetResource res = (SharedSetResource) 
          ctx.getResourceObject("dictAnnotatorProperties");
        dictSet = new HashSet<String>();
        for (String dictPhrase : res.getConfig()) {
          if (ignoreCase) {
            dictSet.add(StringUtils.lowerCase(dictPhrase));
          } else {
            dictSet.add(dictPhrase);
          }
        }
      } else if (TRANSFORM.equals(preserveOrTransform)) {
        SharedMapResource res = (SharedMapResource) 
          ctx.getResourceObject("dictAnnotatorProperties");
        Map<String,String> confMap = res.getConfig();
        dictMap = new HashMap<String,String>();
        for (String dictPhrase : confMap.keySet()) {
          if (ignoreCase) {
            dictMap.put(StringUtils.lowerCase(dictPhrase),
              confMap.get(dictPhrase));
          } else {
            dictMap.put(dictPhrase, confMap.get(dictPhrase));
          }
        }
      } else {
        throw new ResourceInitializationException(
          new IllegalArgumentException(
          "Configuration parameter preserveOrTransform " +
          "must be either 'preserve' or 'transform'"));
      }
    } catch (ResourceAccessException e) {
      throw new ResourceInitializationException(e);
    }
  }
  
  @Override
  public void process(JCas jcas) 
      throws AnalysisEngineProcessException {
    String text = jcas.getDocumentText();
    List<IntRange> annotationSpans = AnnotatorUtils.getAnnotationSpans(
        jcas, KeywordAnnotation.type);
    if (annotationSpans.size() == 1 && 
        annotationSpans.get(0).equals(new IntRange(0, text.length()))) {
      // already fully annotated, return
      return;
    }
    // replace punctuation in working copy of text so the presence
    // of punctuation does not throw off the matching process
    text = text.replaceAll("[\\.,;:]", " ");
    // for HTML text fragments, replace tagged span with spaces
    text = AnnotatorUtils.whiteoutHtml(text);
    WhitespaceTokenizer tokenizer = new WhitespaceTokenizer(
      Version.LUCENE_40, new StringReader(text));
    TokenStream tokenStream;
    if (ignoreCase) {
      tokenStream = new LowerCaseFilter(
        Version.LUCENE_40, tokenizer);
      tokenStream = new ShingleFilter(tokenStream, maxShingleSize);
    } else {
      tokenStream = new ShingleFilter(tokenizer, maxShingleSize);
    }
    try {
      while (tokenStream.incrementToken()) {
        CharTermAttribute term = tokenStream.getAttribute(CharTermAttribute.class);
        OffsetAttribute offset = tokenStream.getAttribute(OffsetAttribute.class);
        String shingle = new String(term.buffer(), 0, term.length());
        boolean foundToken = false;
        if (PRESERVE.equals(preserveOrTransform)) {
          if (dictSet.contains(shingle)) {
            foundToken = true;
          }
        } else {
          if (dictMap.containsKey(shingle)) {
            foundToken = true;
          }
        }
        if (foundToken) {
          if (! AnnotatorUtils.hasContainingAnnotation(
              annotationSpans, offset.startOffset(), offset.endOffset())) {
            KeywordAnnotation annotation = new KeywordAnnotation(jcas);
            annotation.setBegin(offset.startOffset());
            annotation.setEnd(offset.endOffset());
            if (TRANSFORM.equals(preserveOrTransform)) {
              // replace with the specified phrase
              annotation.setTransformedValue(dictMap.get(shingle));
            }
            annotation.addToIndexes();
          }
        }
      }
    } catch (IOException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }
}
