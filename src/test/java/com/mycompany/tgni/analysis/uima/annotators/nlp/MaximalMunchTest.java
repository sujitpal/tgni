package com.mycompany.tgni.analysis.uima.annotators.nlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.util.OpenBitSet;
import org.junit.Test;

/**
 * Code to implement Maximal Munch using BitSets.
 */
public class MaximalMunchTest {

  private static class Annot {
    public int start;
    public int end;
    public String text;
    public Annot(int start, int end, String text) {
      this.start = start;
      this.end = end;
      this.text = text;
    }
  };

  private static final String INPUT = 
    "lung cancer symptoms and kidney cancer";
  
  // 0         1         2         3
  // 0123456789012345678901234567890123456789
  // lung cancer symptoms and kidney cancer
  public static final Annot[] ANNOTS = new Annot[] {
    new Annot(0, 3, "lung"),
    new Annot(0, 10, "lung cancer"),
    new Annot(0, 19, "lung cancer symptoms"),
    new Annot(5, 10, "cancer"),
    new Annot(5, 19, "cancer symptoms"),
    new Annot(5, 10, "cancer"),
    new Annot(25, 30, "kidney"),
    new Annot(25, 37, "kidney cancer"),
    new Annot(32, 37, "cancer")
  };
  
  @SuppressWarnings("unchecked")
  @Test
  public void testMaximalMunch() throws Exception {
    OpenBitSet tset = new OpenBitSet(INPUT.length());
    tset.set(0, tset.length());
    List<Annot> annots = Arrays.asList(ANNOTS);
    // sort the annotations, longest first
    Collections.sort(annots, new Comparator<Annot>() {
      @Override
      public int compare(Annot annot1, Annot annot2) {
        Integer len1 = annot1.end - annot1.start;
        Integer len2 = annot2.end - annot2.start;
        return len2.compareTo(len1);
      }
    });
    List<Annot> maxMunchAnnots = new ArrayList<Annot>();
    long prevCardinality = tset.cardinality();
    for (Annot annot : annots) {
      OpenBitSet aset = new OpenBitSet(tset.length());
      aset.set(0, tset.length());
      aset.flip(annot.start, annot.end);
      tset.intersect(aset);
      long cardinality = tset.cardinality();
      if (cardinality == prevCardinality) {
        // complete overlap, skip
        continue;
      }
      maxMunchAnnots.add(annot);
      prevCardinality = cardinality;
    }
    for (Annot annot : maxMunchAnnots) {
      System.out.println("(" + annot.start + "," + 
        annot.end + "): " + annot.text);
    }
  }
}
