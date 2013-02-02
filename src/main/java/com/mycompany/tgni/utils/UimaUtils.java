package com.mycompany.tgni.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.IntRange;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasPool;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.ProcessTrace;
import org.apache.uima.util.ProcessTraceEvent;
import org.apache.uima.util.XMLInputSource;

/**
 * Largely copied from the TestUtils class in UIMA Sandbox component
 * AlchemyAPIAnnotator.
 */
public class UimaUtils {

  public static final String MIMETYPE_STRING = "string/plain";
  public static final String MIMETYPE_TEXT = "text/plain";
  public static final String MIMETYPE_HTML = "text/html";
  
  public static final String MULTI_VALUED_FIELD_SEPARATOR = "__|__";
  
  private static final String DEFAULT_TGNI_HOME = "/prod/web/data/tgni";
  private static String tgniHome = null;
  
  public static String getTgniHome() {
    if (tgniHome == null) {
      Map<String,String> env = System.getenv();
      if (env.containsKey("TGNI_HOME")) {
        tgniHome = env.get("TGNI_HOME");
      } else {
        tgniHome = DEFAULT_TGNI_HOME;
      }
    }
    return tgniHome;
  }
  
  public static AnalysisEngine getAE(
      String descriptor, Map<String,Object> params) 
      throws IOException, InvalidXMLException, 
      ResourceInitializationException {
    AnalysisEngine ae = null;
    try {
      XMLInputSource in = new XMLInputSource(
        descriptor.startsWith(File.separator) ?
        descriptor :
        FilenameUtils.concat(UimaUtils.getTgniHome(), descriptor));
      AnalysisEngineDescription desc = 
        UIMAFramework.getXMLParser().
        parseAnalysisEngineDescription(in);
      if (params != null) {
        for (String key : params.keySet()) {
          desc.getAnalysisEngineMetaData().
            getConfigurationParameterSettings().
            setParameterValue(key, params.get(key));
        }
      }
      ae = UIMAFramework.produceAnalysisEngine(desc);
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
    return ae;
  }
  
  public static JCas runAE(AnalysisEngine ae, String text, 
      String mimeType, JCas jcas) throws AnalysisEngineProcessException,
      ResourceInitializationException {
    if (jcas == null) {
      jcas = ae.newJCas();
    } else {
      jcas.reset();
    }
    jcas.setSofaDataString(text, mimeType);
    ProcessTrace trace = ae.process(jcas);
    for (ProcessTraceEvent evt : trace.getEvents()) {
      if (evt != null && evt.getResultMessage() != null &&
          evt.getResultMessage().contains("error")) {
        throw new AnalysisEngineProcessException(
          new Exception(evt.getResultMessage()));
      }
    }
    return jcas;
  }
  
  public static void printResults(JCas jcas) {
    FSIndex<Annotation> index = jcas.getAnnotationIndex();
    for (Iterator<Annotation> it = index.iterator(); it.hasNext(); ) {
      Annotation annotation = it.next();
      List<Feature> features = new ArrayList<Feature>();
      if (annotation.getType().getName().contains("com.mycompany")) {
        features = annotation.getType().getFeatures();
      }
      List<String> fasl = new ArrayList<String>();
      for (Feature feature : features) {
        if (feature.getName().contains("com.mycompany")) {
          String name = feature.getShortName();
          String value = annotation.getStringValue(feature);
          fasl.add(name + "=\"" + value + "\"");
        }
      }
      System.out.println(
        annotation.getType().getShortName() + ": " +
        annotation.getCoveredText() + " " +
        (fasl.size() > 0 ? StringUtils.join(fasl.iterator(), ",") : "") + " " +
        annotation.getBegin() + ":" + annotation.getEnd());
    }
    System.out.println("==");
  }
}
