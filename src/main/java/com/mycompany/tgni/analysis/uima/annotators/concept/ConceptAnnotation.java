

/* First created by JCasGen Tue Jul 26 08:59:31 PDT 2011 */
package com.mycompany.tgni.analysis.uima.annotators.concept;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.StringList;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Wed Aug 03 21:00:33 PDT 2011
 * XML source: /Users/sujit/Projects/tgni/src/main/resources/descriptors/Concept.xml
 * @generated */
public class ConceptAnnotation extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(ConceptAnnotation.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected ConceptAnnotation() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public ConceptAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public ConceptAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public ConceptAnnotation(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: oid

  /** getter for oid - gets The matched concept's OID
   * @generated */
  public int getOid() {
    if (ConceptAnnotation_Type.featOkTst && ((ConceptAnnotation_Type)jcasType).casFeat_oid == null)
      jcasType.jcas.throwFeatMissing("oid", "com.mycompany.tgni.uima.annotators.concept.ConceptAnnotation");
    return jcasType.ll_cas.ll_getIntValue(addr, ((ConceptAnnotation_Type)jcasType).casFeatCode_oid);}
    
  /** setter for oid - sets The matched concept's OID 
   * @generated */
  public void setOid(int v) {
    if (ConceptAnnotation_Type.featOkTst && ((ConceptAnnotation_Type)jcasType).casFeat_oid == null)
      jcasType.jcas.throwFeatMissing("oid", "com.mycompany.tgni.uima.annotators.concept.ConceptAnnotation");
    jcasType.ll_cas.ll_setIntValue(addr, ((ConceptAnnotation_Type)jcasType).casFeatCode_oid, v);}    
   
    
  //*--------------*
  //* Feature: stycodes

  /** getter for stycodes - gets List of semantic type codes for concept
   * @generated */
  public String getStycodes() {
    if (ConceptAnnotation_Type.featOkTst && ((ConceptAnnotation_Type)jcasType).casFeat_stycodes == null)
      jcasType.jcas.throwFeatMissing("stycodes", "com.mycompany.tgni.uima.annotators.concept.ConceptAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ConceptAnnotation_Type)jcasType).casFeatCode_stycodes);}
    
  /** setter for stycodes - sets List of semantic type codes for concept 
   * @generated */
  public void setStycodes(String v) {
    if (ConceptAnnotation_Type.featOkTst && ((ConceptAnnotation_Type)jcasType).casFeat_stycodes == null)
      jcasType.jcas.throwFeatMissing("stycodes", "com.mycompany.tgni.uima.annotators.concept.ConceptAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((ConceptAnnotation_Type)jcasType).casFeatCode_stycodes, v);}    
   
    
  //*--------------*
  //* Feature: stygroup

  /** getter for stygroup - gets Semantic Group for concept
   * @generated */
  public String getStygroup() {
    if (ConceptAnnotation_Type.featOkTst && ((ConceptAnnotation_Type)jcasType).casFeat_stygroup == null)
      jcasType.jcas.throwFeatMissing("stygroup", "com.mycompany.tgni.uima.annotators.concept.ConceptAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ConceptAnnotation_Type)jcasType).casFeatCode_stygroup);}
    
  /** setter for stygroup - sets Semantic Group for concept 
   * @generated */
  public void setStygroup(String v) {
    if (ConceptAnnotation_Type.featOkTst && ((ConceptAnnotation_Type)jcasType).casFeat_stygroup == null)
      jcasType.jcas.throwFeatMissing("stygroup", "com.mycompany.tgni.uima.annotators.concept.ConceptAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((ConceptAnnotation_Type)jcasType).casFeatCode_stygroup, v);}    
   
    
  //*--------------*
  //* Feature: pname

  /** getter for pname - gets Preferred name for concept
   * @generated */
  public String getPname() {
    if (ConceptAnnotation_Type.featOkTst && ((ConceptAnnotation_Type)jcasType).casFeat_pname == null)
      jcasType.jcas.throwFeatMissing("pname", "com.mycompany.tgni.uima.annotators.concept.ConceptAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ConceptAnnotation_Type)jcasType).casFeatCode_pname);}
    
  /** setter for pname - sets Preferred name for concept 
   * @generated */
  public void setPname(String v) {
    if (ConceptAnnotation_Type.featOkTst && ((ConceptAnnotation_Type)jcasType).casFeat_pname == null)
      jcasType.jcas.throwFeatMissing("pname", "com.mycompany.tgni.uima.annotators.concept.ConceptAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((ConceptAnnotation_Type)jcasType).casFeatCode_pname, v);}    
   
    
  //*--------------*
  //* Feature: similarity

  /** getter for similarity - gets Similarity with input string
   * @generated */
  public int getSimilarity() {
    if (ConceptAnnotation_Type.featOkTst && ((ConceptAnnotation_Type)jcasType).casFeat_similarity == null)
      jcasType.jcas.throwFeatMissing("similarity", "com.mycompany.tgni.uima.annotators.concept.ConceptAnnotation");
    return jcasType.ll_cas.ll_getIntValue(addr, ((ConceptAnnotation_Type)jcasType).casFeatCode_similarity);}
    
  /** setter for similarity - sets Similarity with input string 
   * @generated */
  public void setSimilarity(int v) {
    if (ConceptAnnotation_Type.featOkTst && ((ConceptAnnotation_Type)jcasType).casFeat_similarity == null)
      jcasType.jcas.throwFeatMissing("similarity", "com.mycompany.tgni.uima.annotators.concept.ConceptAnnotation");
    jcasType.ll_cas.ll_setIntValue(addr, ((ConceptAnnotation_Type)jcasType).casFeatCode_similarity, v);}    
  }

    
