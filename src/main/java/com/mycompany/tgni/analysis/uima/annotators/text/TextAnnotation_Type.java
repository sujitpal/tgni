
/* First created by JCasGen Mon Apr 04 19:38:58 PDT 2011 */
package com.mycompany.tgni.analysis.uima.annotators.text;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Sat Apr 23 08:03:20 PDT 2011
 * @generated */
public class TextAnnotation_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (TextAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = TextAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new TextAnnotation(addr, TextAnnotation_Type.this);
  			   TextAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new TextAnnotation(addr, TextAnnotation_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = TextAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("com.mycompany.tagi.uima.annotators.text.TextAnnotation");
 
  /** @generated */
  final Feature casFeat_tagName;
  /** @generated */
  final int     casFeatCode_tagName;
  /** @generated */ 
  public String getTagName(int addr) {
        if (featOkTst && casFeat_tagName == null)
      jcas.throwFeatMissing("tagName", "com.mycompany.tagi.uima.annotators.text.TextAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_tagName);
  }
  /** @generated */    
  public void setTagName(int addr, String v) {
        if (featOkTst && casFeat_tagName == null)
      jcas.throwFeatMissing("tagName", "com.mycompany.tagi.uima.annotators.text.TextAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_tagName, v);}
    
  
 
  /** @generated */
  final Feature casFeat_confidence;
  /** @generated */
  final int     casFeatCode_confidence;
  /** @generated */ 
  public float getConfidence(int addr) {
        if (featOkTst && casFeat_confidence == null)
      jcas.throwFeatMissing("confidence", "com.mycompany.tagi.uima.annotators.text.TextAnnotation");
    return ll_cas.ll_getFloatValue(addr, casFeatCode_confidence);
  }
  /** @generated */    
  public void setConfidence(int addr, float v) {
        if (featOkTst && casFeat_confidence == null)
      jcas.throwFeatMissing("confidence", "com.mycompany.tagi.uima.annotators.text.TextAnnotation");
    ll_cas.ll_setFloatValue(addr, casFeatCode_confidence, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public TextAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_tagName = jcas.getRequiredFeatureDE(casType, "tagName", "uima.cas.String", featOkTst);
    casFeatCode_tagName  = (null == casFeat_tagName) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_tagName).getCode();

 
    casFeat_confidence = jcas.getRequiredFeatureDE(casType, "confidence", "uima.cas.Float", featOkTst);
    casFeatCode_confidence  = (null == casFeat_confidence) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_confidence).getCode();

  }
}



    
