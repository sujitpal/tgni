package com.mycompany.tgni.analysis.uima.annotators.keyword;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.IntRange;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.springframework.core.annotation.AnnotationUtils;

import com.mycompany.tgni.analysis.uima.conf.SharedSetResource;
import com.mycompany.tgni.utils.AnnotatorUtils;

/**
 * Recognizes chemical names and marks them as keywords so
 * they can be matched exactly.
 */
public class ChemicalNameAnnotator extends JCasAnnotator_ImplBase {

  private static final String CHEM_COMP_SEP = "-,[](){}~ ";
  private static final String CHEM_COMP_MUST_HAVE_CHARS = 
    "-,[](){}~0123456789";
  private static Pattern[] INVALID_CHEM_COMP_PATTERNS = new Pattern[] {
    Pattern.compile("[A-Z]{1,2}"), // 1-2 consecutive uppercase alphas
    Pattern.compile("[0-9]+"),     // numerics
    Pattern.compile("[A-Z][0-9]"), // alpha followed by number
  };
  
  private Set<String> chemicalComponents;
  @Override
  public void initialize(UimaContext ctx) 
      throws ResourceInitializationException {
    super.initialize(ctx);
    try {
      SharedSetResource res = (SharedSetResource) 
        ctx.getResourceObject("chemicalComponents");
      chemicalComponents = res.getConfig();
    } catch (ResourceAccessException e) {
      throw new ResourceInitializationException(e);
    }
  }
  
  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    String text = StringUtils.lowerCase(jcas.getDocumentText());
    // the text must have one or more of the separator chars
    // to qualify as a chemical (systematic) name
    if (StringUtils.indexOfAny(text, CHEM_COMP_MUST_HAVE_CHARS) > -1) {
      List<IntRange> annotationSpans = AnnotatorUtils.getAnnotationSpans(
        jcas, KeywordAnnotation.type);
      if (annotationSpans.size() == 1 && 
          annotationSpans.get(0).equals(new IntRange(0, text.length()))) {
        // already fully annotated, return
        return;
      }
      // split the input by the chemical separator set
      List<String> components = new ArrayList<String>(
        Arrays.asList(StringUtils.split(text, CHEM_COMP_SEP)));
      // filter out stuff we don't care about
      CollectionUtils.filter(components, new Predicate<String>() {
        @Override
        public boolean evaluate(String component) {
          for (Pattern p : INVALID_CHEM_COMP_PATTERNS) {
            Matcher m = p.matcher(component);
            return (! m.matches());
          }
          return true;
        }
      });
      // ensure that the components are contained in our
      // dictionary
      Set<String> compset = new HashSet<String>(components);
      if (CollectionUtils.intersection(
          chemicalComponents, compset).size() > 0) {
        if (! AnnotatorUtils.hasContainingAnnotation(
            annotationSpans, 0, text.length())) {
          KeywordAnnotation annotation = new KeywordAnnotation(jcas);
          annotation.setBegin(0);
          annotation.setEnd(text.length());
          annotation.addToIndexes();
        }
      }
    }
  }
}
