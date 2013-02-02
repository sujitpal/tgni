

/* First created by JCasGen Fri Aug 26 09:33:42 PDT 2011 */
package com.mycompany.tgni.analysis.uima.annotators.nlp;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Nov 21 20:02:53 PST 2011
 * XML source: /Users/sujit/Projects/tgni/src/main/resources/descriptors/NounPhrase.xml
 * @generated */
public class NounPhraseAnnotation extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(NounPhraseAnnotation.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected NounPhraseAnnotation() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public NounPhraseAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public NounPhraseAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public NounPhraseAnnotation(JCas jcas, int begin, int end) {
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
  //* Feature: coordExpansions

  /** getter for coordExpansions - gets Expansions put in by CoordExpAE
   * @generated */
  public String getCoordExpansions() {
    if (NounPhraseAnnotation_Type.featOkTst && ((NounPhraseAnnotation_Type)jcasType).casFeat_coordExpansions == null)
      jcasType.jcas.throwFeatMissing("coordExpansions", "com.mycompany.tgni.uima.annotators.nlp.NounPhraseAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((NounPhraseAnnotation_Type)jcasType).casFeatCode_coordExpansions);}
    
  /** setter for coordExpansions - sets Expansions put in by CoordExpAE 
   * @generated */
  public void setCoordExpansions(String v) {
    if (NounPhraseAnnotation_Type.featOkTst && ((NounPhraseAnnotation_Type)jcasType).casFeat_coordExpansions == null)
      jcasType.jcas.throwFeatMissing("coordExpansions", "com.mycompany.tgni.uima.annotators.nlp.NounPhraseAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((NounPhraseAnnotation_Type)jcasType).casFeatCode_coordExpansions, v);}    
  }

    
