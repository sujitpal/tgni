package com.mycompany.tgni.analysis.uima.annotators.concept;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.apache.commons.lang.math.IntRange;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import com.mycompany.tgni.services.NodeService;

/**
 * Loops through the list of concept annotations, and if it
 * finds one that overlaps exactly with another concept annotation,
 * applies a concept disambiguation metric to remove one of them
 * from the list of concept annotations.
 */
public class HomonymDisambigAnnotator extends JCasAnnotator_ImplBase {

  private NodeService nodeService;
  
  private static final int TOPN_OIDS = 3;
  private static final int MAX_DEPTH = 5;
  
  @Override
  public void initialize(UimaContext ctx) 
      throws ResourceInitializationException {
    super.initialize(ctx);
    nodeService = NodeService.getInstance();
  }
  
  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    try {
      FSIndex<? extends Annotation> fsindex = jcas.getAnnotationIndex(
          ConceptAnnotation.type);
      final Map<Integer,Integer> scoreMap = new HashMap<Integer,Integer>();
      MultiMap<IntRange,ConceptAnnotation> homonymMap = 
        new MultiHashMap<IntRange,ConceptAnnotation>(); 
      for (Iterator<? extends Annotation> it = fsindex.iterator(); it.hasNext(); ) {
        ConceptAnnotation annotation = (ConceptAnnotation) it.next();
        int oid = annotation.getOid();
        if (scoreMap.containsKey(oid)) {
          int score = scoreMap.get(oid);
          scoreMap.put(oid, score);
        } else {
          scoreMap.put(oid, 1);
        }
        IntRange range = new IntRange(annotation.getBegin(), annotation.getEnd());
        homonymMap.put(range, annotation);
      }
      // scan the homonymMap to see if any range has multiple 
      // annotations, if so, these are homonyms and need to be
      // processed
      MultiMap<IntRange,ConceptAnnotation> candidateHomonyms = 
        new MultiHashMap<IntRange,ConceptAnnotation>();
      for (IntRange range : homonymMap.keySet()) {
        if (homonymMap.get(range).size() > 1) {
          for (ConceptAnnotation annotation : homonymMap.get(range)) {
            candidateHomonyms.put(range, annotation);
          }
        }
      }
      if (candidateHomonyms.size() > 0) {
        // get top scoring OIDs in this document
        List<Integer> oids = new ArrayList<Integer>();
        oids.addAll(scoreMap.keySet());
        Collections.sort(oids, new Comparator<Integer>() {
          @Override
          public int compare(Integer oid1, Integer oid2) {
            Integer score1 = scoreMap.get(oid1);
            Integer score2 = scoreMap.get(oid2);
            return score2.compareTo(score1);
          }
        });
        for (IntRange range : candidateHomonyms.keySet()) {
          final Collection<ConceptAnnotation> annotations = 
            candidateHomonyms.get(range);
          int closestHomonymPos = 0;
          int pos = 0;
          double prevDist = Double.MAX_VALUE;
          for (ConceptAnnotation annotation : annotations) {
            double dist = findDistance(annotation.getOid(), oids, scoreMap);
            if (dist < prevDist) {
              closestHomonymPos = pos;
            }
            pos++;
          }
          // remove the other two from the index
          pos = 0;
          for (ConceptAnnotation annotation : annotations) {
            if (pos != closestHomonymPos) {
              annotation.removeFromIndexes(jcas);
            }
          }
        }
      }
    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  private double findDistance(final Integer oid, 
      List<Integer> oids, Map<Integer,Integer> scoreMap) 
      throws Exception {
    List<Integer> topOids = new ArrayList<Integer>();
    topOids.addAll(oids);
    CollectionUtils.filter(topOids, new Predicate<Integer>() {
      private int pos;
      
      @Override
      public boolean evaluate(Integer tempOid) {
        boolean include = false;
        if (tempOid.equals(oid) || pos > TOPN_OIDS) {
          include = false;
        } else {
          include = true;
        }
        pos++;
        return include;
      }
    });
    double pythDist = 0.0D;
    for (Integer topOid : topOids) {
      Path shortestPath = nodeService.getShortestPath(oid, topOid, MAX_DEPTH);
      if (shortestPath == null) continue;
      double dist = 0.0D;
      int depth = 1;
      for (Iterator<PropertyContainer> it = shortestPath.iterator(); it.hasNext(); ) {
        PropertyContainer pc = it.next();
        if (pc instanceof Relationship) {
          Long weight = (Long) ((Relationship) pc).getProperty("mrank");
          dist += scoreMap.get(topOid) * weight / Math.pow(depth, 2);
          depth++;
        }
      }
      pythDist += Math.pow(dist, 2);
    }
    if (pythDist > 0.0D) {
      return (1.0D / Math.sqrt(pythDist));
    } else {
      return Double.MAX_VALUE;
    }
  }
}
