package com.mycompany.tgni.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.RelationshipType;

/**
 * Enumeration of all relationship types.
 */
public enum TRelTypes implements RelationshipType, Serializable {

  PARENT_OF (1, true),
  HAS_RELATED (2, false),
  HAS_SYMPTOM (3, true),
  HAS_DIAGNOSTIC_PROCEDURE (4, true),
  HAS_TREATMENT_PROCEDURE (5, true),
  HAS_DRUG (6, true),
  HAS_CONTRAINDICATION_DRUG (7, true),
  HAS_PHYSICIAN_SPECIALTY (8, true),
  HAS_CAUSE (9, true),
  HAS_RISK_FACTOR (10, true),
  HAS_COMPLICATION (11, true),
  HAS_PREVENTION (12, true),
  HAS_LOCAL_CHILDREN (14, true),
  HAS_TOOLS (15, true),
  HAS_INGREDIENTS (16, true),
  HAS_NUTRITIONAL_SUPPLEMENTS (17, true),
  HAS_ALTERNATIVE_PROCEDURE (18, true),
  HAS_DIET (19, true),
  HAS_HEALTH_RESOURCES (20, false),
  AD_CATEGORY_IS_MAPPED_TO (21, false),
  HAS_NEGATIVE_CONCEPTS (22, false),
  EXPERIMENTAL_TAXONOMY_HAS_CHILD (23, false),
  HAS_MEASURED_COMPONENT (24, false),
  AD_CATEGORIES_HIERARCHY_HAS_SUBCATEGORY (25, false),
  ANATOMY_TO_TREATMENTS (43, false),
  DIAGNOSTIC_PROCEDURE_HAS_DRUG (26, false),
  DIAGNOSTIC_PROCEDURE_HAS_RISK_FACTOR (27, false),
  DIAGNOSTIC_PROCEDURE_HAS_SYMPTOMS (28, false),
  DIAGNOSTIC_PROCEDURE_HAS_TREATMENT_PROCEDURES (29, false),
  DIAGNOSTIC_PROCEDURE_HAS_PHYSICIAN_SPECIALTY (30, false),
  TREATMENT_PROCEDURE_HAS_PHYSICIAN_SPECIALTY (31, false),
  TREATMENT_PROCEDURE_HAS_SYMPTOM (32, false),
  TREATMENT_PROCEDURE_HAS_RISK_FACTOR (33, false),
  TREATMENT_PROCEDURES_TO_DRUGS (34, false),
  PHYSICIAN_SPECIALTIES_TO_SYMPTOMS (35, false),
  PHYSICIAN_SPECIALTIES_TO_DRUGS (36, false),
  DRUGS_TO_RISK_FACTORS (37, false),
  RISK_FACTORS_TO_SYMPTOMS (38, false),
  SYMPTOMS_TO_DRUGS (39, false),
  ANATOMY_TO_SYMPTOMS (40, false),
  DISEASES_TO_ANATOMY (41, false),
  COMORBIDITY (42, false),
  HAS_GLOBAL_CONCEPT (44, false),
  SIDE_EFFECTS (45, false),
  CONDITION_TO_NUTRITION_CATEGORY (46, false),
  
  // this set of relationships do not exist in the database,
  // they are implicitly created during taxonomy load. The OID
  // for the reverse relation is (by convention, but note that
  // the loading code depends on this convention) the negative 
  // of the relationship it is a reverse of.
  HAS_PARENT(-1, true),
  IS_SYMPTOM_OF (-3, true),
  IS_DIAGNOSTIC_PROCEDURE_OF (-4, true),
  IS_TR_PROCEDURE_OF (-5, true),
  IS_DRUG_FOR (-6, true),
  IS_CONTRAINDICATION_DRUG_OF (-7, true),
  IS_PHYSICIAN_SPECIALTY_FOR (-8, true),
  IS_CAUSE_OF (-9, true),
  IS_RISK_FACTOR_OF (-10, true),
  IS_COMPLICATION_OF (-11, true),
  IS_PREVENTION_FOR (-12, true),
  HAS_LOCAL_PARENT (-14, true),
  IS_TOOL_FOR (-15, true),
  IS_INGREDIENT_OF (-16, true),
  IS_NUTRITIONAL_SUPPLEMENT_FOR (-17, true),
  IS_ALTERNATIVE_PROCEDURE_FOR (-18, true),
  IS_DIET_FOR (-19, true),
  ;
  
  public Integer oid;
  public boolean reversible;
  
  private TRelTypes(Integer oid, boolean reversible) {
    this.oid = oid;
    this.reversible = reversible;
  }

  private static Map<Integer,TRelTypes> oidMap = null;
  private static Map<String,TRelTypes> nameMap = null;
  static {
    oidMap = new HashMap<Integer,TRelTypes>();
    nameMap = new HashMap<String,TRelTypes>();
    for (TRelTypes type : TRelTypes.values()) {
      oidMap.put(type.oid, type);
      nameMap.put(type.name(), type);
    }
  }
  
  public static TRelTypes fromOid(Integer oid) {
    return oidMap.get(oid);
  }
  
  public static TRelTypes fromName(String name) {
    return nameMap.get(name);
  }
}
