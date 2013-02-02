
/* First created by JCasGen Wed Apr 06 15:11:11 PDT 2011 */
package com.mycompany.tgni.analysis.uima.annotators.keyword;

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
 * Updated by JCasGen Mon Dec 19 19:43:31 PST 2011
 * @generated */
public class KeywordAnnotation_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (KeywordAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = KeywordAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new KeywordAnnotation(addr, KeywordAnnotation_Type.this);
  			   KeywordAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new KeywordAnnotation(addr, KeywordAnnotation_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = KeywordAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("com.mycompany.tgni.uima.annotators.keyword.KeywordAnnotation");
 
  /** @generated */
  final Feature casFeat_transformedValue;
  /** @generated */
  final int     casFeatCode_transformedValue;
  /** @generated */ 
  public String getTransformedValue(int addr) {
        if (featOkTst && casFeat_transformedValue == null)
      jcas.throwFeatMissing("transformedValue", "com.mycompany.tgni.uima.annotators.keyword.KeywordAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_transformedValue);
  }
  /** @generated */    
  public void setTransformedValue(int addr, String v) {
        if (featOkTst && casFeat_transformedValue == null)
      jcas.throwFeatMissing("transformedValue", "com.mycompany.tgni.uima.annotators.keyword.KeywordAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_transformedValue, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public KeywordAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_transformedValue = jcas.getRequiredFeatureDE(casType, "transformedValue", "uima.cas.String", featOkTst);
    casFeatCode_transformedValue  = (null == casFeat_transformedValue) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_transformedValue).getCode();

  }
}



    
