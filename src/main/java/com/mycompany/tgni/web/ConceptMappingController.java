package com.mycompany.tgni.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.util.OpenBitSet;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.mycompany.tgni.analysis.uima.annotators.concept.ConceptAnnotation;
import com.mycompany.tgni.beans.TConcept;
import com.mycompany.tgni.beans.TRelTypes;
import com.mycompany.tgni.beans.TRelation;
import com.mycompany.tgni.services.NodeService;
import com.mycompany.tgni.utils.JsonUtils;
import com.mycompany.tgni.utils.UimaUtils;

/**
 * Controller to expose TGNI functionality via web application.
 */
@Controller
public class ConceptMappingController {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  private String conceptMappingAEDescriptor;
  
  private AnalysisEngine conceptMappingAE;
  private NodeService nodeService;
  private Connection conn;

  public void setConceptMappingAEDescriptor(
      String conceptMappingAEDescriptor) {
    this.conceptMappingAEDescriptor = conceptMappingAEDescriptor;
  }
  
  @PostConstruct
  public void init() throws Exception {
    nodeService = NodeService.getInstance();
    conceptMappingAE = UimaUtils.getAE(conceptMappingAEDescriptor, null);
  }
  
  @PreDestroy
  public void destroy() throws Exception {
    conceptMappingAE.destroy();
  }

  @RequestMapping(value="/find.html")
  public ModelAndView find(
      @RequestParam(value="q", required=false) String q) {
    ModelAndView mav = new ModelAndView();
    mav.addObject("operation", "find");
    if (StringUtils.isEmpty(q)) {
      mav.setViewName("find");
      return mav;
    }
    try {
      if (NumberUtils.isNumber(q) && 
          StringUtils.length(q) == 7) {
        return show(Integer.valueOf(q));
      } else {
        long startTs = System.currentTimeMillis();
        List<TConcept> concepts = nodeService.findConcepts(q);
        mav.addObject("concepts", concepts);
        long endTs = System.currentTimeMillis();
        mav.addObject("q", q);
        mav.addObject("elapsed", new Long(endTs - startTs));
      }
    } catch (Exception e) {
      mav.addObject("error", e.getMessage());
    }
    mav.setViewName("find");
    return mav;
  }
  
  @RequestMapping(value="/map.html")
  public ModelAndView map(
      @RequestParam(value="q1", required=false) String q1,
      @RequestParam(value="q2", required=false) String q2,
      @RequestParam(value="q3", required=false) String q3,
      @RequestParam(value="if", required=false, defaultValue=UimaUtils.MIMETYPE_STRING) String inputFormat,
      @RequestParam(value="of", required=true, defaultValue="html") String outputFormat,
      @RequestParam(value="ofs", required=false, defaultValue="pretty") String outputFormatStyle,
      @RequestParam(value="sgf", required=false) String[] styGroupFilter,
      @RequestParam(value="scf", required=false) String[] styCodeFilter) {

    ModelAndView mav = new ModelAndView();
    mav.addObject("operation", "map");
    // validate parameters (at least one of o, q, u or t must
    // be supplied, otherwise show the input form
    mav.addObject("q1", StringUtils.isEmpty(q1) ? "" : q1);
    mav.addObject("q2", StringUtils.isEmpty(q2) ? "" : q2);
    mav.addObject("q3", StringUtils.isEmpty(q3) ? "" : q3);
    String q = StringUtils.isNotEmpty(q1) ? q1 : 
      StringUtils.isNotEmpty(q2) ? q2 : 
      StringUtils.isNotEmpty(q3) ? q3 : null;
    if (StringUtils.isEmpty(q)) {
      setViewName(mav, outputFormat, outputFormatStyle);
      return mav;
    }
    try {
      if (NumberUtils.isNumber(q) && 
          StringUtils.length(q) == 7) {
        return show(Integer.valueOf(q));
      } else {
        // show list of concepts
        String text = q;
        if ((q.startsWith("http://") && 
            UimaUtils.MIMETYPE_HTML.equals(inputFormat))) {
          URL u = new URL(q);
          BufferedReader br = new BufferedReader(
            new InputStreamReader(u.openStream()));
          StringBuilder tbuf = new StringBuilder();
          String line = null;
          while ((line = br.readLine()) != null) {
            tbuf.append(line).append("\n");
          }
          br.close();
          text = tbuf.toString();
        }
        List<ConceptAnnotation> annotations = new ArrayList<ConceptAnnotation>();
        long startTs = System.currentTimeMillis();
        JCas jcas = UimaUtils.runAE(conceptMappingAE, text, inputFormat, null);
        FSIndex fsindex = jcas.getAnnotationIndex(ConceptAnnotation.type);
        for (Iterator<ConceptAnnotation> it = fsindex.iterator(); it.hasNext(); ) {
          ConceptAnnotation annotation = it.next();
          annotations.add(annotation);
        }
        CollectionUtils.filter(annotations, new StyGroupPredicate(styGroupFilter)); 
        CollectionUtils.filter(annotations, new StyCodePredicate(styCodeFilter));
        annotations = filterSubsumedConcepts(q, annotations);
        if (annotations.size() == 0) {
          mav.addObject("error", "No concepts found");
        } else {
          mav.addObject("text", text);
          mav.addObject("annotations", annotations);
          long endTs = System.currentTimeMillis();
          mav.addObject("elapsed", new Long(endTs - startTs));
        }
        setViewName(mav, outputFormat, outputFormatStyle);
      }
    } catch (Exception e) {
      mav.addObject("error", e.getMessage());
      setViewName(mav, outputFormat, outputFormatStyle);
    }
    return mav;
  }

  @RequestMapping(value="/show.html", method=RequestMethod.GET)
  public ModelAndView show(
      @RequestParam(value="q", required=true) int q) {
    ModelAndView mav = new ModelAndView();
    mav.addObject("operation", "show");
    try {
      long startTs = System.currentTimeMillis();
      // show all details about the concept
      TConcept concept = nodeService.getConcept(q);
      Bag<TRelTypes> relCounts = nodeService.getRelationCounts(concept);
      Map<String,List<TRelation>> relmap = 
        new HashMap<String,List<TRelation>>();
      Map<Integer,String> oidmap = new HashMap<Integer,String>();
      for (TRelTypes reltype : relCounts.uniqueSet()) {
        List<TRelation> rels = nodeService.getRelatedConcepts(
          concept, reltype);
        for (TRelation rel : rels) {
          TConcept toConcept = nodeService.getConcept(rel.getToOid());
          oidmap.put(rel.getToOid(), toConcept.getPname());
        }
        relmap.put(reltype.name(), rels);
      }
      mav.addObject("concept", concept);
      mav.addObject("relmap", relmap);
      mav.addObject("oidmap", oidmap);
      long endTs = System.currentTimeMillis();
      mav.addObject("elapsed", new Long(endTs - startTs));
    } catch (Exception e) {
      mav.addObject("error", e.getMessage());
    }
    mav.setViewName("show");
    return mav;
  }
  
  private List<ConceptAnnotation> filterSubsumedConcepts(
      String q, List<ConceptAnnotation> annotations) {
    OpenBitSet qset = new OpenBitSet(q.length());
    qset.set(0, qset.length());
    // sort the annotations, longest first
    Collections.sort(annotations, new Comparator<ConceptAnnotation>() {
      @Override
      public int compare(ConceptAnnotation ca1, ConceptAnnotation ca2) {
        Integer len1 = ca1.getEnd() - ca1.getBegin();
        Integer len2 = ca2.getEnd() - ca2.getBegin();
        return len2.compareTo(len1);
      }
    });
    List<ConceptAnnotation> filtered = new ArrayList<ConceptAnnotation>();
    long prevCardinality = qset.cardinality();
    for (ConceptAnnotation annotation : annotations) {
      OpenBitSet cset = new OpenBitSet(qset.length());
      cset.set(0, qset.length());
      cset.flip(annotation.getBegin(), annotation.getEnd());
      cset.intersect(qset);
      long cardinality = cset.cardinality();
      if (cardinality == prevCardinality) {
        // concept is subsumed, skip it
        continue;
      }
      filtered.add(annotation);
      prevCardinality = cardinality;
    }
    return filtered;
  }

  private void setViewName(ModelAndView mav, String format, 
      String style) {
    if ("html".equals(format)) {
      mav.setViewName("map");
    } else if ("xml".equals(format)) {
      addXmlAnnotations(mav, style);
      mav.setViewName("map-xml");
    } else if ("json".equals(format)) {
      addJsonAnnotations(mav, style, false);
      mav.setViewName("map-json");
    } else if ("jsonp".equals(format)) {
      addJsonAnnotations(mav, style, true);
      mav.setViewName("map-json");
    }
  }
  
  private void addXmlAnnotations(ModelAndView mav, String style) {
    List<ConceptAnnotation> annotations = 
      (List<ConceptAnnotation>) mav.getModel().get("annotations");
    mav.addObject("mimetype", "text/xml");
    if (annotations != null) {
      Document doc = DocumentFactory.getInstance().createDocument();
      Element mappings = doc.addElement("mappings");
      for (ConceptAnnotation annotation : annotations) {
        Element mapping = mappings.addElement("mapping");
        mapping.addAttribute("id", String.valueOf(annotation.getOid()));
        mapping.addAttribute("start", String.valueOf(annotation.getBegin()));
        mapping.addAttribute("end", String.valueOf(annotation.getEnd()));
        mapping.addAttribute("pname", annotation.getPname());
        mapping.addAttribute("group", annotation.getStygroup());
        mapping.addAttribute("codes", 
          StringUtils.replace(annotation.getStycodes(), "\"", ""));
        mapping.setText(annotation.getCoveredText());
      }
      OutputFormat outputFormat = "compact".equals(style) ?
        OutputFormat.createCompactFormat() :
        OutputFormat.createPrettyPrint();
      StringWriter swriter = new StringWriter();
      XMLWriter writer = new XMLWriter(swriter, outputFormat);
      try {
        writer.write(doc);
        writer.flush();
        mav.addObject("stringAnnotations", swriter.toString());
      } catch (IOException e) {
        logger.warn("IOException writing XML to buffer", e);
      }
    }
  }

  private void addJsonAnnotations(ModelAndView mav, String style, 
      boolean prefix) {
    if (prefix) {
      mav.addObject("mapPrefix", "map");
      mav.addObject("mimetype", "application/x-javascript");
    } else {
      mav.addObject("mimetype", "application/json");
    }
    List<ConceptAnnotation> annotations = 
      (List<ConceptAnnotation>) mav.getModel().get("annotations");
    if (annotations != null) {
      JSONArray mappings = new JSONArray();
      for (ConceptAnnotation annotation : annotations) {
        JSONObject mapping = new JSONObject();
        mapping.accumulate("id", String.valueOf(annotation.getOid()));
        mapping.accumulate("start", String.valueOf(annotation.getBegin()));
        mapping.accumulate("end", String.valueOf(annotation.getEnd()));
        mapping.accumulate("pname", annotation.getPname());
        mapping.accumulate("group", annotation.getStygroup());
        mapping.accumulate("codes", 
          StringUtils.replace(annotation.getStycodes(), "\"", ""));
        mapping.accumulate("text", annotation.getCoveredText());
        mappings.put(mapping);
      }
      mav.addObject("stringAnnotations", "pretty".equals(style) ?
        mappings.toString(2) : mappings.toString());
    }
  }

  private class StyGroupPredicate implements 
      Predicate<ConceptAnnotation> {

    private Set<String> stygrpset;
    
    public StyGroupPredicate(String[] stygroups) {
      stygrpset = new HashSet<String>();
      if (ArrayUtils.isNotEmpty(stygroups)) {
        stygrpset.addAll(Arrays.asList(stygroups));
      }
    }
    
    @Override
    public boolean evaluate(ConceptAnnotation annotation) {
      if (stygrpset.size() == 0) {
        return true;
      } else {
        String styGroup = annotation.getStygroup();
        return stygrpset.contains(styGroup);
      }
    }
  }
  
  private class StyCodePredicate implements 
      Predicate<ConceptAnnotation> {

    private Set<String> stycodeset;
    
    public StyCodePredicate(String[] stycodes) {
      stycodeset = new HashSet<String>();
      if (ArrayUtils.isNotEmpty(stycodes)) {
        stycodeset.addAll(Arrays.asList(stycodes));
      }
    }
    
    @Override
    public boolean evaluate(ConceptAnnotation annotation) {
      if (stycodeset.size() == 0) {
        return true;
      } else {
        Set<String> stycodes = JsonUtils.stringToMap(
          annotation.getStycodes()).keySet();
        return (CollectionUtils.intersection(
          stycodeset, stycodes).size() > 0);
      }
    }
  }
}
