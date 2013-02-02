

/* First created by JCasGen Wed Apr 06 15:11:11 PDT 2011 */
package com.mycompany.tgni.analysis.uima.annotators.keyword;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Dec 19 19:43:31 PST 2011
 * XML source: /Users/sujit/Projects/tgni/src/main/resources/descriptors/Keyword.xml
 * @generated */
public class KeywordAnnotation extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(KeywordAnnotation.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected KeywordAnnotation() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public KeywordAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public KeywordAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public KeywordAnnotation(JCas jcas, int begin, int end) {
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
  //* Feature: transformedValue

  /** getter for transformedValue - gets The transformed value (can be empty)
   * @generated */
  public String getTransformedValue() {
    if (KeywordAnnotation_Type.featOkTst && ((KeywordAnnotation_Type)jcasType).casFeat_transformedValue == null)
      jcasType.jcas.throwFeatMissing("transformedValue", "com.mycompany.tgni.uima.annotators.keyword.KeywordAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((KeywordAnnotation_Type)jcasType).casFeatCode_transformedValue);}
    
  /** setter for transformedValue - sets The transformed value (can be empty) 
   * @generated */
  public void setTransformedValue(String v) {
    if (KeywordAnnotation_Type.featOkTst && ((KeywordAnnotation_Type)jcasType).casFeat_transformedValue == null)
      jcasType.jcas.throwFeatMissing("transformedValue", "com.mycompany.tgni.uima.annotators.keyword.KeywordAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((KeywordAnnotation_Type)jcasType).casFeatCode_transformedValue, v);}    
  }

    
