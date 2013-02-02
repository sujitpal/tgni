
/* First created by JCasGen Fri Aug 26 09:33:42 PDT 2011 */
package com.mycompany.tgni.analysis.uima.annotators.nlp;

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
 * Updated by JCasGen Mon Nov 21 20:02:54 PST 2011
 * @generated */
public class NounPhraseAnnotation_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (NounPhraseAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = NounPhraseAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new NounPhraseAnnotation(addr, NounPhraseAnnotation_Type.this);
  			   NounPhraseAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new NounPhraseAnnotation(addr, NounPhraseAnnotation_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = NounPhraseAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("com.mycompany.tgni.uima.annotators.nlp.NounPhraseAnnotation");



  /** @generated */
  final Feature casFeat_coordExpansions;
  /** @generated */
  final int     casFeatCode_coordExpansions;
  /** @generated */ 
  public String getCoordExpansions(int addr) {
        if (featOkTst && casFeat_coordExpansions == null)
      jcas.throwFeatMissing("coordExpansions", "com.mycompany.tgni.uima.annotators.nlp.NounPhraseAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_coordExpansions);
  }
  /** @generated */    
  public void setCoordExpansions(int addr, String v) {
        if (featOkTst && casFeat_coordExpansions == null)
      jcas.throwFeatMissing("coordExpansions", "com.mycompany.tgni.uima.annotators.nlp.NounPhraseAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_coordExpansions, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public NounPhraseAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_coordExpansions = jcas.getRequiredFeatureDE(casType, "coordExpansions", "uima.cas.String", featOkTst);
    casFeatCode_coordExpansions  = (null == casFeat_coordExpansions) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_coordExpansions).getCode();

  }
}



    
