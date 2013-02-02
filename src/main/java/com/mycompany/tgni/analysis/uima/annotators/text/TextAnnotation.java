

/* First created by JCasGen Mon Apr 04 19:38:58 PDT 2011 */
package com.mycompany.tgni.analysis.uima.annotators.text;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Sat Apr 23 08:03:20 PDT 2011
 * XML source: /Users/sujit/Projects/tagi/src/main/java/com/mycompany/tagi/uima/annotators/text/Text.xml
 * @generated */
public class TextAnnotation extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(TextAnnotation.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected TextAnnotation() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public TextAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public TextAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public TextAnnotation(JCas jcas, int begin, int end) {
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
  //* Feature: tagName

  /** getter for tagName - gets Enclosing Tag Name
   * @generated */
  public String getTagName() {
    if (TextAnnotation_Type.featOkTst && ((TextAnnotation_Type)jcasType).casFeat_tagName == null)
      jcasType.jcas.throwFeatMissing("tagName", "com.mycompany.tagi.uima.annotators.text.TextAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TextAnnotation_Type)jcasType).casFeatCode_tagName);}
    
  /** setter for tagName - sets Enclosing Tag Name 
   * @generated */
  public void setTagName(String v) {
    if (TextAnnotation_Type.featOkTst && ((TextAnnotation_Type)jcasType).casFeat_tagName == null)
      jcasType.jcas.throwFeatMissing("tagName", "com.mycompany.tagi.uima.annotators.text.TextAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((TextAnnotation_Type)jcasType).casFeatCode_tagName, v);}    
   
    
  //*--------------*
  //* Feature: confidence

  /** getter for confidence - gets confidence level (0-1)
   * @generated */
  public float getConfidence() {
    if (TextAnnotation_Type.featOkTst && ((TextAnnotation_Type)jcasType).casFeat_confidence == null)
      jcasType.jcas.throwFeatMissing("confidence", "com.mycompany.tagi.uima.annotators.text.TextAnnotation");
    return jcasType.ll_cas.ll_getFloatValue(addr, ((TextAnnotation_Type)jcasType).casFeatCode_confidence);}
    
  /** setter for confidence - sets confidence level (0-1) 
   * @generated */
  public void setConfidence(float v) {
    if (TextAnnotation_Type.featOkTst && ((TextAnnotation_Type)jcasType).casFeat_confidence == null)
      jcasType.jcas.throwFeatMissing("confidence", "com.mycompany.tagi.uima.annotators.text.TextAnnotation");
    jcasType.ll_cas.ll_setFloatValue(addr, ((TextAnnotation_Type)jcasType).casFeatCode_confidence, v);}    
  }

    
