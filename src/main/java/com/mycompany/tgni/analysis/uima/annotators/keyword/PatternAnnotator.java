package com.mycompany.tgni.analysis.uima.annotators.keyword;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.IntRange;
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
 * Annotates pattern found in input text. Operates in preserve
 * or transform mode. In preserve mode, recognizes and annotates
 * a set of supplied regex patterns. In transform mode, recognizes
 * and annotates a map of regex patterns which have associated
 * transforms, and additionally applies the transformation and
 * stores it in its transformedValue feature.
 */
public class PatternAnnotator extends JCasAnnotator_ImplBase {

  private String preserveOrTransform;
  private Set<Pattern> patternSet;
  private Map<Pattern,String> patternMap;
  
  private final static String PRESERVE = "preserve";
  private final static String TRANSFORM = "transform";
  
  @Override
  public void initialize(UimaContext ctx) 
      throws ResourceInitializationException {
    super.initialize(ctx);
    preserveOrTransform = 
      (String) ctx.getConfigParameterValue("preserveOrTransform");
    try {
      if (PRESERVE.equals(preserveOrTransform)) {
        SharedSetResource res = (SharedSetResource) 
          ctx.getResourceObject("patternAnnotatorProperties");
        patternSet = new HashSet<Pattern>();
        for (String patternString : res.getConfig()) {
          patternSet.add(Pattern.compile(patternString));
        }
      } else if (TRANSFORM.equals(preserveOrTransform)) {
        SharedMapResource res = (SharedMapResource)
          ctx.getResourceObject("patternAnnotatorProperties");
        patternMap = new HashMap<Pattern,String>();
        Map<String,String> confMap = res.getConfig();
        for (String patternString : confMap.keySet()) {
          patternMap.put(Pattern.compile(patternString), 
            confMap.get(patternString));
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
    int pcnt = 0;
    Set<Pattern> patterns = PRESERVE.equals(preserveOrTransform) ?
      patternSet : patternMap.keySet();
    for (Pattern pattern : patterns) {
      Matcher matcher = pattern.matcher(text);
      int pos = 0;
      while (matcher.find(pos)) {
        pos = matcher.end();
        if (! AnnotatorUtils.hasContainingAnnotation(
            annotationSpans, matcher.start(), pos)) {
          KeywordAnnotation annotation = new KeywordAnnotation(jcas);
          annotation.setBegin(matcher.start());
          annotation.setEnd(pos);
          if (TRANSFORM.equals(preserveOrTransform)) {
            String token = StringUtils.substring(
                text, annotation.getBegin(), annotation.getEnd());
            String transform = patternMap.get(pattern);
            String transformedValue = applyTransform(token, transform);
            annotation.setTransformedValue(transformedValue);
          }
          annotation.addToIndexes();
        }
      }
      pcnt++;
    }
  }

  private String applyTransform(String token, String transform) {
    String[] tcols = 
      StringUtils.splitPreserveAllTokens(transform, "/");
    if (tcols.length == 4) {
      Pattern p = Pattern.compile(tcols[1]);
      Matcher m = p.matcher(token);
      return m.replaceAll(tcols[2]);
    } else {
      return token;
    }
  }
}
