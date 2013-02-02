package com.mycompany.tgni.services;

import java.util.List;

import org.apache.commons.collections15.Bag;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mycompany.tgni.beans.TConcept;
import com.mycompany.tgni.beans.TRelTypes;
import com.mycompany.tgni.beans.TRelation;

/**
 * Testcase to verify database load.
 */
public class NodeServiceTest {

  private static NodeService nodeService;
  
  @BeforeClass
  public static void setupBeforeClass() throws Exception {
    nodeService = NodeService.getInstance();
  }
  
  @AfterClass
  public static void teardownAfterClass() throws Exception {
    nodeService.destroy();
  }

  @Test
  public void testLookupByOid() throws Exception {
    TConcept c = nodeService.getConcept(2800541);
    Assert.assertNotNull(c);
    System.out.println("concept=" + c);
  }
  
  @Test
  public void testLookupByName() throws Exception {
    String[] names = new String[] {
      "Asthma", 
      "Heart Attack",
      "Myocardial infarction",
      "Aneurysm"
    };
    for (String name : names) {
      List<TConcept> concepts = 
        nodeService.getConcepts(name);
//      Assert.assertTrue(concepts.size() > 0);
      System.out.println("name=" + name);
      for (TConcept concept : concepts) {
        System.out.println("...concept=" + concept);
      }
    }
  }
  
  @Test
  public void testGetRelationCounts() throws Exception {
    TConcept concept = nodeService.getConcept(2805580);
    Bag<TRelTypes> counts = nodeService.getRelationCounts(concept);
    for (TRelTypes key : counts.uniqueSet()) {
      System.out.println(key.name() + " => " + counts.getCount(key));
    }
  }
  
  @Test
  public void testGetRelatedConcepts() throws Exception {
    TConcept concept = nodeService.getConcept(2805580);
    List<TRelation> rels = 
      nodeService.getRelatedConcepts(concept, TRelTypes.HAS_DRUG);
    boolean foundExpectedRelatedConcept = false;
    for (TRelation rel : rels) {
      System.out.println(rel.getFromOid() +  
        "--(" + rel.getRelType().name() + 
        ")-->" + rel.getToOid() + 
        " (" + rel.getMstip() + "," + rel.getMrank() + 
        "," + rel.getArank() + ")");
      if (rel.getToOid() == 2793050) {
        foundExpectedRelatedConcept = true;
      }
    }
//    Assert.assertTrue(foundExpectedRelatedConcept);
  }
  
  @Test
  public void testReverseRelationships() throws Exception {
    TConcept concept = nodeService.getConcept(2793050);
    List<TRelation> rels = 
      nodeService.getRelatedConcepts(concept, TRelTypes.IS_DRUG_FOR);
    boolean foundExpectedRelatedConcept = false;
    for (TRelation rel : rels) {
      if (rel.getToOid() == 2805580) {
        foundExpectedRelatedConcept = true;
        break;
      }
    }
//    Assert.assertTrue(foundExpectedRelatedConcept);
  }
}
