package com.mycompany.tgni.analysis.uima.annotators.text;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.StartTagType;
import net.htmlparser.jericho.Tag;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang.math.Range;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.jfree.util.LineBreakIterator;

import com.mycompany.tgni.analysis.uima.conf.SharedMapResource;
import com.mycompany.tgni.utils.AnnotatorUtils;
import com.mycompany.tgni.utils.UimaUtils;

/**
 * Annotates text regions in marked up documents (HTML, XML, plain
 * text). Allows setting of include and skip tags and (class) 
 * attributes. Contents of tags and class attributes marked as skip
 * are completely ignored. Contents of tags and class attributes
 * marked as include are accepted without further filtering. All
 * remaining chunks (separated by newline) are passed through a link
 * density filter and a plain text length filter to determine if
 * they should be considered as text for further processing. 
 * TODO: the remaining (qualifying) text is passed through a Naive 
 * Bayes classifier to remove boilerplate text.
 */
public class TextAnnotator extends JCasAnnotator_ImplBase {

  private static final String UNKNOWN_TAG = "pre";
  
  private Set<String> skipTags = new HashSet<String>();
  private Set<String> skipAttrs = new HashSet<String>();
  private Set<String> includeTags = new HashSet<String>();
  private Set<String> includeAttrs = new HashSet<String>();
  private float minTextDensity = 0.5F;
  private int minTextLength = 20;
  
  @Override
  public void initialize(UimaContext ctx) 
      throws ResourceInitializationException {
    super.initialize(ctx);
    skipTags.clear();
    skipAttrs.clear();
    includeTags.clear();
    includeAttrs.clear();
    try {
      SharedMapResource res = (SharedMapResource) 
        getContext().getResourceObject("textAnnotatorProperties");
      Map<String,String> config = res.getConfig(); 
      for (String key : config.keySet()) {
        if ("skiptags".equals(key)) {
          skipTags.addAll(res.asList(config.get(key)));
        } else if ("skipattrs".equals(key)) {
          skipAttrs.addAll(res.asList(config.get(key)));
        } else if ("incltags".equals(key)) {
          includeTags.addAll(res.asList(config.get(key)));
        } else if ("inclattrs".equals(key)) {
          includeAttrs.addAll(res.asList(config.get(key)));
        } else if ("minTxtDensity".equals(key)) {
          minTextDensity = Float.valueOf(config.get(key));
        } else if ("minTxtLength".equals(key)) {
          minTextLength = Integer.valueOf(config.get(key));
        }
      }
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }
  
  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    String text = jcas.getDocumentText();
    String mimeType = jcas.getSofaMimeType();
    if (UimaUtils.MIMETYPE_STRING.equals(mimeType) ||
        UimaUtils.MIMETYPE_TEXT.equals(mimeType)) {
      annotateAsText(jcas, 0, text.length(), UNKNOWN_TAG, 1.0F);
      return;
    }
    // PHASE I
    // parse out text within skipTags and skipAttrs and replace
    // with whitespace so they are eliminated as annotation
    // candidates later
    char[] copy = text.toCharArray();
    Source source = new Source(text);
    source.fullSequentialParse();
    int skipTo = 0;
    for (Iterator<Segment> it = source.getNodeIterator(); it.hasNext(); ) {
      Segment segment = it.next();
      int start = segment.getBegin();
      int end = segment.getEnd();
      if (end < skipTo) {
        continue;
      }
      if (segment instanceof Tag) {
        Tag tag = (Tag) segment;
        if (tag.getTagType() == StartTagType.NORMAL) {
          StartTag stag = (StartTag) tag;
          String stagname = StringUtils.lowerCase(stag.getName());
          if (skipTags.contains(stagname)) {
            skipTo = stag.getElement().getEnd();
            AnnotatorUtils.whiteout(copy, start, skipTo);
            continue;
          }
          String classAttr = StringUtils.lowerCase(
            stag.getAttributeValue("class"));
          if (StringUtils.isNotEmpty(classAttr)) {
            for (String skipAttr : skipAttrs) {
              if (classAttr.contains(skipAttr)) {
                skipTo = stag.getElement().getEnd();
                AnnotatorUtils.whiteout(copy, start, skipTo);
                continue;
              }
            }
          }
          if (includeTags.contains(stagname)) {
            annotateAsText(jcas, start, end, stagname, 1.0F);
          }
          if (StringUtils.isNotEmpty(classAttr)) {
            for (String includeAttr : includeAttrs) {
              if (classAttr.contains(includeAttr)) {
                annotateAsText(jcas, start, end, stagname, 1.0F);
              }
            }
          }
        }
      } else {
        continue;
      }
    }
    // PHASE II
    // make another pass on the text, this time chunking by newline
    // and filtering by density to determine text candidates
    String ctext = new String(copy);
    LineBreakIterator lbi = new LineBreakIterator();
    lbi.setText(ctext);
    int start = 0;
    while (lbi.hasNext()) {
      int end = lbi.nextWithEnd();
      if (end == LineBreakIterator.DONE) {
        break;
      }
      if (alreadyAnnotated(jcas, start, end)) {
        start = end;
        continue;
      }
      // compute density and mark as text if satisfied
      float density = 0.0F;
      float ll = (float) (end - start);
      String line = StringUtils.substring(ctext, start, end);
      float tl = (float) StringUtils.strip(line).length();
      if (tl > 0.0F) {
        Source s = new Source(line);
        Element fe = s.getFirstElement();
        String fetn = fe == null ? 
          UNKNOWN_TAG : StringUtils.lowerCase(fe.getName());
        String plain = StringUtils.strip(
          s.getTextExtractor().toString());
        if (StringUtils.isNotEmpty(plain) && looksLikeText(plain)) {
          float pl = (float) plain.length();
          if (minTextLength > 0 && pl > minTextLength) {
            density = pl / ll;
          }
        }
        if (density > minTextDensity) {
          // this is a candidate for annotation
          annotateAsText(jcas, start, end, fetn, density);
        }
      }
      start = end;
    }
  }

  private void annotateAsText(JCas jcas, int startPos, int endPos, 
      String tagname, float confidence) {
    TextAnnotation annotation = new TextAnnotation(jcas);
    annotation.setBegin(startPos);
    annotation.setEnd(endPos);
    annotation.setTagName(tagname);
    annotation.setConfidence(confidence);
    annotation.addToIndexes(jcas);
  }
  
  private boolean alreadyAnnotated(JCas jcas, int start, int end) {
    Range r = new IntRange(start, end);
    FSIndex<Annotation> tai = jcas.getAnnotationIndex(TextAnnotation.type);
    for (Iterator<Annotation> it = tai.iterator(); it.hasNext(); ) {
      Annotation ta = it.next();
      Range ar = new IntRange(ta.getBegin(), ta.getEnd());
      if (ar.containsRange(r)) {
        return true;
      }
    }
    return false;
  }

  private boolean looksLikeText(String plain) {
    return plain.indexOf('.') > -1 &&
      plain.indexOf(' ') > -1;
  }
}
