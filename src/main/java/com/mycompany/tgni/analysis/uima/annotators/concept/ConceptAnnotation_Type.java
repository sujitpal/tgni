
/* First created by JCasGen Tue Jul 26 08:59:31 PDT 2011 */
package com.mycompany.tgni.analysis.uima.annotators.concept;

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
 * Updated by JCasGen Wed Aug 03 21:00:34 PDT 2011
 * @generated */
public class ConceptAnnotation_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (ConceptAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = ConceptAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new ConceptAnnotation(addr, ConceptAnnotation_Type.this);
  			   ConceptAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new ConceptAnnotation(addr, ConceptAnnotation_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = ConceptAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("com.mycompany.tgni.uima.annotators.concept.ConceptAnnotation");
 
  /** @generated */
  final Feature casFeat_oid;
  /** @generated */
  final int     casFeatCode_oid;
  /** @generated */ 
  public int getOid(int addr) {
        if (featOkTst && casFeat_oid == null)
      jcas.throwFeatMissing("oid", "com.mycompany.tgni.uima.annotators.concept.ConceptAnnotation");
    return ll_cas.ll_getIntValue(addr, casFeatCode_oid);
  }
  /** @generated */    
  public void setOid(int addr, int v) {
        if (featOkTst && casFeat_oid == null)
      jcas.throwFeatMissing("oid", "com.mycompany.tgni.uima.annotators.concept.ConceptAnnotation");
    ll_cas.ll_setIntValue(addr, casFeatCode_oid, v);}
    
  
 
  /** @generated */
  final Feature casFeat_stycodes;
  /** @generated */
  final int     casFeatCode_stycodes;
  /** @generated */ 
  public String getStycodes(int addr) {
        if (featOkTst && casFeat_stycodes == null)
      jcas.throwFeatMissing("stycodes", "com.mycompany.tgni.uima.annotators.concept.ConceptAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_stycodes);
  }
  /** @generated */    
  public void setStycodes(int addr, String v) {
        if (featOkTst && casFeat_stycodes == null)
      jcas.throwFeatMissing("stycodes", "com.mycompany.tgni.uima.annotators.concept.ConceptAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_stycodes, v);}
    
  
 
  /** @generated */
  final Feature casFeat_stygroup;
  /** @generated */
  final int     casFeatCode_stygroup;
  /** @generated */ 
  public String getStygroup(int addr) {
        if (featOkTst && casFeat_stygroup == null)
      jcas.throwFeatMissing("stygroup", "com.mycompany.tgni.uima.annotators.concept.ConceptAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_stygroup);
  }
  /** @generated */    
  public void setStygroup(int addr, String v) {
        if (featOkTst && casFeat_stygroup == null)
      jcas.throwFeatMissing("stygroup", "com.mycompany.tgni.uima.annotators.concept.ConceptAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_stygroup, v);}
    
  
 
  /** @generated */
  final Feature casFeat_pname;
  /** @generated */
  final int     casFeatCode_pname;
  /** @generated */ 
  public String getPname(int addr) {
        if (featOkTst && casFeat_pname == null)
      jcas.throwFeatMissing("pname", "com.mycompany.tgni.uima.annotators.concept.ConceptAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_pname);
  }
  /** @generated */    
  public void setPname(int addr, String v) {
        if (featOkTst && casFeat_pname == null)
      jcas.throwFeatMissing("pname", "com.mycompany.tgni.uima.annotators.concept.ConceptAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_pname, v);}
    
  
 
  /** @generated */
  final Feature casFeat_similarity;
  /** @generated */
  final int     casFeatCode_similarity;
  /** @generated */ 
  public int getSimilarity(int addr) {
        if (featOkTst && casFeat_similarity == null)
      jcas.throwFeatMissing("similarity", "com.mycompany.tgni.uima.annotators.concept.ConceptAnnotation");
    return ll_cas.ll_getIntValue(addr, casFeatCode_similarity);
  }
  /** @generated */    
  public void setSimilarity(int addr, int v) {
        if (featOkTst && casFeat_similarity == null)
      jcas.throwFeatMissing("similarity", "com.mycompany.tgni.uima.annotators.concept.ConceptAnnotation");
    ll_cas.ll_setIntValue(addr, casFeatCode_similarity, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public ConceptAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_oid = jcas.getRequiredFeatureDE(casType, "oid", "uima.cas.Integer", featOkTst);
    casFeatCode_oid  = (null == casFeat_oid) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_oid).getCode();

 
    casFeat_stycodes = jcas.getRequiredFeatureDE(casType, "stycodes", "uima.cas.String", featOkTst);
    casFeatCode_stycodes  = (null == casFeat_stycodes) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_stycodes).getCode();

 
    casFeat_stygroup = jcas.getRequiredFeatureDE(casType, "stygroup", "uima.cas.String", featOkTst);
    casFeatCode_stygroup  = (null == casFeat_stygroup) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_stygroup).getCode();

 
    casFeat_pname = jcas.getRequiredFeatureDE(casType, "pname", "uima.cas.String", featOkTst);
    casFeatCode_pname  = (null == casFeat_pname) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_pname).getCode();

 
    casFeat_similarity = jcas.getRequiredFeatureDE(casType, "similarity", "uima.cas.Integer", featOkTst);
    casFeatCode_similarity  = (null == casFeat_similarity) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_similarity).getCode();

  }
}



    
