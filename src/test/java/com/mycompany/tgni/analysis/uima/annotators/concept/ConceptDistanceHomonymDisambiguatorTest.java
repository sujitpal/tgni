package com.mycompany.tgni.analysis.uima.annotators.concept;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import com.mycompany.tgni.services.NodeService;

/**
 * Tests for calculating shortest path between two nodes
 * for homonym disambiguation.
 */
public class ConceptDistanceHomonymDisambiguatorTest {

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
  public void testPathBetweenTwoNodes() throws Exception {
    // Acute renal failure: 3814964
    // Acute respiratory failure: 5355868
    double d11 = findDistance(3814964, CONCEPTS_FOR_ACUTE_RENAL_FAILURE_DOC);
    double d12 = findDistance(5355868, CONCEPTS_FOR_ACUTE_RENAL_FAILURE_DOC);
    double d21 = findDistance(3814964, CONCEPTS_FOR_ACUTE_RESPIRATORY_FAILURE_DOC);
    double d22 = findDistance(5355868, CONCEPTS_FOR_ACUTE_RESPIRATORY_FAILURE_DOC);
    System.out.println("d11=" + d11);
    System.out.println("d12=" + d12);
    Assert.assertTrue(d11 < d12);
    System.out.println("d21=" + d21);
    System.out.println("d22=" + d22);
    Assert.assertTrue(d22 < d21);
  }
  
  private double findDistance(int oid, String[][] docterms) 
      throws Exception {
    double pythDist = 0.0D;
    List<String[]> topTerms = getTopTerms(oid, docterms);
    for (String[] topTerm : topTerms) {
      Integer topTermOid = Integer.valueOf(topTerm[0]);
      Integer occurrences = Integer.valueOf(topTerm[2]);
      Path shortestPath = nodeService.getShortestPath(oid, topTermOid, 5);
      System.out.println(nodeService.pathString(shortestPath));
      if (shortestPath == null) continue;
      double distance = 0.0D;
      int hops = 1;
      for (Iterator<PropertyContainer> it = shortestPath.iterator(); it.hasNext(); ) {
        PropertyContainer pc = it.next();
        if (pc instanceof Relationship) {
          Long strength = (Long) ((Relationship) pc).getProperty("mrank");
          distance += (occurrences * strength) / Math.pow(hops, 2);
          hops++;
        }
      }
      pythDist += Math.pow(distance, 2);
    }
    return (1.0D / Math.sqrt(pythDist));
  }

  private List<String[]> getTopTerms(int oid, String[][] docterms) {
    List<String[]> topTerms = new ArrayList<String[]>();
    for (String[] docterm : docterms) {
      topTerms.add(docterm);
    }
//    // find the concepts whose scores are within 1 standard
//    // deviation of the mean score
//    DescriptiveStatistics stats = new DescriptiveStatistics();
//    for (String[] docterm : docterms) {
//      Integer docOid = Integer.valueOf(docterm[0]);
//      if (docOid == oid) continue;
//      stats.addValue(Double.valueOf(docterm[2]));
//    }
//    double mean = stats.getMean();
//    double stddev = stats.getStandardDeviation();
//    DoubleRange range = 
//      new DoubleRange(mean - stddev, mean + stddev);
//    for (String[] docterm : docterms) {
//      double score = Double.valueOf(docterm[2]);
//      if (range.containsDouble(score)) {
//        topTerms.add(docterm);
//      }
//    }
    Collections.sort(topTerms, new Comparator<String[]>() {
      @Override
      public int compare(String[] term1, String[] term2) {
        Integer count1 = Integer.valueOf(term1[2]);
        Integer count2 = Integer.valueOf(term2[2]);
        return count2.compareTo(count1);
    }});
    if (topTerms.size() > 3) {
      for (String[] topterm : topTerms.subList(0, 3)) {
        System.out.println(StringUtils.join(topterm, ";"));
      }
      return topTerms.subList(0, 3);
    } else {
      for (String[] topterm : topTerms) {
        System.out.println(StringUtils.join(topterm, ";"));
      }
      return topTerms;
    }
  }

//  private Path findShortestPath(int oid1, int oid2) throws Exception {
//    long nid1 = nodeService.indexService.getNid(oid1);
//    long nid2 = nodeService.indexService.getNid(oid2);
//    Node node1 = nodeService.graphService.getNodeById(nid1);
//    Node node2 = nodeService.graphService.getNodeById(nid2);
//    RelationshipExpander expander = Traversal.expanderForAllTypes();
//    PathFinder<Path> finder = GraphAlgoFactory.shortestPath(expander, 5);
//    Iterable<Path> paths = finder.findAllPaths(node1, node2);
//    // these are the shortest path(s) in terms of number of hops
//    // now we need to find the most likely path based on the 
//    // sum of the rank of relationships
//    Path bestPath = null;
//    Long maxStrength = 0L;
//    for (Path path : paths) {
//      Long strength = 0L;
//      for (Iterator<PropertyContainer> it = path.iterator(); it.hasNext(); ) {
//        PropertyContainer pc = it.next();
//        if (pc instanceof Relationship) {
//          strength += (Long) ((Relationship) pc).getProperty("mrank"); 
//        }
//      }
//      if (strength > maxStrength) {
//        maxStrength = strength;
//        bestPath = path;
//      }
//    }
//    return bestPath;
//  }

//  private String showPath(Path path) {
//    if (path == null) return "NONE";
//    StringBuilder buf = new StringBuilder();
//    for (Iterator<PropertyContainer> it = path.iterator(); it.hasNext(); ) {
//      PropertyContainer pc = it.next();
//      if (pc instanceof Node) {
//        Node npc = (Node) pc;
//        buf.append((String) npc.getProperty("pname")).
//          append("(").
//          append((Integer) npc.getProperty("oid")).
//          append(")");
//      } else if (pc instanceof Relationship) {
//        Relationship rpc = (Relationship) pc;
//        buf.append("--(").
//          append(rpc.getType().name()).
//          append("[").
//          append((Long) rpc.getProperty("mrank")).
//          append("])-->");
//      }
//    }
//    return buf.toString();
//  }


  //  Acute renal failure
  //  http://www.nlm.nih.gov/medlineplus/ency/article/000501.htm
  private static final String[][] CONCEPTS_FOR_ACUTE_RENAL_FAILURE_DOC = new String[][] {
    new String[] {"8208372", "Urinary system", "1"},
//    new String[] {"9724140", "Up", "1"},
//    new String[] {"9090659", "Needed (qualifier value)", "3"},
//    new String[] {"8110653", "Reading", "1"},
    new String[] {"5356344", "Dialysis", "2"},
//    new String[] {"9723548", "High", "2"},
    new String[] {"5356301", "decreased blood flow", "1"},
//    new String[] {"8865327", "Complicated", "1"},
//    new String[] {"8938733", "Serious (qualifier value)", "1"},
    new String[] {"8001798", "Veins", "1"},
//    new String[] {"9072301", "Used by", "2"},
    new String[] {"8184150", "Alcohol", "1"},
//    new String[] {"2791257", "Employment", "1"},
    new String[] {"2795743", "Blood Clotting", "1"},
//    new String[] {"8876592", "per hour (qualifier value)", "1"},
//    new String[] {"8934321", "Has patient", "1"},
//    new String[] {"8961973", "Contact person (person)", "1"},
    new String[] {"5356309", "Acute pyelonephritis", "1"},
    new String[] {"2790854", "aminosalicylic acid", "1"},
//    new String[] {"2801780", "Hospitals", "1"},
    new String[] {"8117827", "Urination", "3"},
//    new String[] {"8918116", "Preventive action", "3"},
    new String[] {"8001560", "Heart", "3"},
//    new String[] {"8865194", "Type of restoration (attribute)", "1"},
    new String[] {"5047801", "Blood Test", "1"},
    new String[] {"8119493", "Blood Potassium Test", "1"},
//    new String[] {"5216597", "Treatment Procedures", "4"},
    new String[] {"8101556", "Neoplasms", "1"},
//    new String[] {"8864047", "Decrease", "1"},
    new String[] {"8109768", "Rib Pain", "1"},
//    new String[] {"9063723", "Started", "1"},
//    new String[] {"8864048", "Decreasing", "1"},
    new String[] {"8001937", "Intestines", "2"},
//    new String[] {"8860830", "H+", "1"},
    new String[] {"3815073", "Dehydration", "2"},
//    new String[] {"8115127", "Laboratory test finding", "1"},
//    new String[] {"9072816", "Slowly (qualifier value)", "2"},
//    new String[] {"8978232", "Common (qualifier value)", "2"},
    new String[] {"8106247", "Proteins", "1"},
    new String[] {"8002027", "Foot", "2"},
    new String[] {"9285726", "N-acetyltyrosine 1-naphthyl ester", "1"},
//    new String[] {"9311815", "TOOS", "1"},
    new String[] {"8001688", "Serum", "1"},
    new String[] {"2791526", "Kidney Stones", "1"},
    new String[] {"3815700", "Seizures", "2"},
    new String[] {"5356241", "Electrolytes", "2"},
    new String[] {"8107443", "Interstitial Nephritis", "1"},
//    new String[] {"5349129", "Lung Diseases", "1"},
    new String[] {"2796231", "Hemolytic Uremic Syndrome", "2"},
//    new String[] {"9232250", "Physician Executives", "1"},
    new String[] {"5363507", "Serum Potassium Test", "1"},
    new String[] {"8128491", "Hemolytic transfusion reaction", "1"},
//    new String[] {"8208192", "Soup", "1"},
//    new String[] {"5304448", "Diagnosis", "1"},
    new String[] {"8114926", "Fluid Retention", "1"},
//    new String[] {"8877131", "Does eat", "1"},
//    new String[] {"9723315", "Friendliness", "1"},
//    new String[] {"5047350", "Cause of disease", "5"},
//    new String[] {"8859435", "-2", "1"},
    new String[] {"8109563", "Swollen Ankle", "1"},
//    new String[] {"9168223", "Generalization (Psychology)", "2"},
//    new String[] {"8857313", "Quantity (attribute)", "3"},
//    new String[] {"8957070", "Frequent (qualifier value)", "1"},
    new String[] {"2791897", "Potassium", "1"},
    new String[] {"2791410", "Heart Murmurs", "1"},
    new String[] {"5357652", "Impaired Sensation", "1"},
    new String[] {"2791408", "Heart Disease", "1"},
//    new String[] {"5344477", "Surgical procedure", "2"},
    new String[] {"9294277", "Protactinium", "1"},
//    new String[] {"8107890", "Eating", "1"},
//    new String[] {"9191497", "MedlinePlus", "2"},
    new String[] {"3814967", "Acute Kidney Tubular Necrosis", "2"},
    new String[] {"2791496", "insulin", "1"},
//    new String[] {"9301158", "Private Practice", "1"},
//    new String[] {"8877130", "Able to eat", "1"},
//    new String[] {"8889245", "Intermediate", "1"},
//    new String[] {"8868121", "Hip unstable (finding)", "1"},
    new String[] {"8617171", "Kidney Damage Causes", "1"},
    new String[] {"9055346", "Finding of potassium level", "1"},
//    new String[] {"8867212", "Reducible (qualifier value)", "1"},
    new String[] {"3815677", "Rhabdomyolysis", "1"},
//    new String[] {"8954471", "Including", "4"},
    new String[] {"8212409", "Body building", "1"},
//    new String[] {"8918620", "Removes from (attribute)", "1"},
    new String[] {"9147116", "Nitrogenous waste agent", "1"},
    new String[] {"8094541", "Abdominal X-Ray", "1"},
    new String[] {"5348908", "diuretics", "2"},
    new String[] {"8897411", "Able to hear", "1"},
//    new String[] {"9376843", "development", "2"},
//    new String[] {"8963194", "Low", "1"},
    new String[] {"2805584", "Kidney Disease", "1"},
//    new String[] {"8862800", "Increasing", "2"},
    new String[] {"8109898", "Elderly", "1"},
    new String[] {"2791974", "Kidney Failure", "8"},
//    new String[] {"8867612", "per day (qualifier value)", "1"},
    new String[] {"2790889", "antibiotics", "1"},
//    new String[] {"9723871", "Linear", "1"},
//    new String[] {"8918841", "For which was done (attribute)", "3"},
//    new String[] {"8109393", "Doctor", "1"},
//    new String[] {"8111724", "Result", "1"},
    new String[] {"8118388", "Septicemia", "2"},
//    new String[] {"8100238", "Handling", "1"},
//    new String[] {"8111861", "Patient", "1"},
    new String[] {"2791721", "Nephrology", "2"},
    new String[] {"2805669", "Disorders of Blood and Blood-forming Organs", "1"},
//    new String[] {"5343558", "Complications", "1"},
//    new String[] {"8871599", "Severities", "1"},
    new String[] {"7984406", "Healthcare Practitioners", "3"},
//    new String[] {"8113616", "Symptoms", "1"},
//    new String[] {"9724495", "Within", "1"},
    new String[] {"2791377", "glucose", "1"},
    new String[] {"8118924", "Inflammation", "1"},
    new String[] {"3815232", "Flank Pain", "1"},
    new String[] {"7984605", "Diet", "1"},
    new String[] {"5047843", "Urinalysis", "1"},
    new String[] {"8119159", "Creatinine Clearance Test", "1"},
//    new String[] {"8938113", "Better (qualifier value)", "1"},
    new String[] {"8118271", "prolonged bleeding time", "1"},
    new String[] {"5351046", "Abdominal Ultrasound", "1"},
    new String[] {"5344133", "Nausea and Vomiting", "1"},
//    new String[] {"8120383", "Excessive", "1"},
//    new String[] {"8119457", "Division procedure", "1"},
//    new String[] {"8829668", "Patient in hospital", "1"},
    new String[] {"8107656", "Acute Nephritis", "2"},
//    new String[] {"8865195", "Retainer", "1"},
//    new String[] {"3807399", "Support Groups", "2"},
//    new String[] {"8106678", "Occlusion of artery", "1"},
//    new String[] {"8879362", "Does not eat", "1"},
//    new String[] {"8816100", "Infection", "4"},
//    new String[] {"4806291", "Hiccups", "1"},
//    new String[] {"9096397", "Referral placed (situation)", "1"},
//    new String[] {"9109159", "Employed", "1"},
//    new String[] {"8863543", "Overnight (qualifier value)", "1"},
//    new String[] {"8981946", "Prognosis/outlook (observable entity)", "1"},
//    new String[] {"9724691", "Work", "1"},
//    new String[] {"8131287", "Mood", "1"},
    new String[] {"8001810", "Kidney", "11"},
//    new String[] {"8863528", "Several (qualifier value)", "1"},
    new String[] {"2791466", "High Blood Pressure", "4"},
//    new String[] {"5344419", "Anatomy", "1"},
//    new String[] {"5047455", "new york", "1"},
//    new String[] {"8001564", "Blood Vessels", "1"},
    new String[] {"8128452", "Decrease in Appetite", "1"},
//    new String[] {"8843676", "days/week", "1"},
//    new String[] {"8111522", "Stethoscopes", "1"},
//    new String[] {"8836190", "weeks/month (qualifier value)", "1"},
    new String[] {"8824918", "Transvenous approach", "1"},
    new String[] {"8623478", "Fluid Retention Causes", "1"},
    new String[] {"2792070", "Stroke", "1"},
    new String[] {"9139926", "Blood chemistry", "1"},
    new String[] {"8114665", "Blood In Stool", "1"},
//    new String[] {"9724120", "Tests (qualifier value)", "3"},
    new String[] {"8005159", "sodium chloride", "1"},
//    new String[] {"8862318", "Limited", "1"},
//    new String[] {"8861051", "Integer +2", "1"},
//    new String[] {"8903931", "Able to read", "1"},
//    new String[] {"8112666", "Craving", "1"},
//    new String[] {"9077584", "Once - dosing instruction fragment (qualifier value)", "1"},
//    new String[] {"5354974", "Global", "2"},
//    new String[] {"8857432", "Progression", "1"},
//    new String[] {"8232454", "oxygen", "21"},
//    new String[] {"9220544", "Philadelphia", "1"},
//    new String[] {"8945268", "Alternating (qualifier value)", "1"},
    new String[] {"8001502", "Nervous System", "1"},
    new String[] {"2791843", "Pericarditis", "1"},
//    new String[] {"8894733", "Days of the week", "1"},
    new String[] {"2792068", "Stress", "1"},
    new String[] {"4974847", "Acute kidney failure Symptoms", "1"},
//    new String[] {"2795416", "Diseases and Disorders", "1"},
    new String[] {"2794138", "Abruptio Placentae", "2"},
//    new String[] {"9287350", "Review", "3"},
//    new String[] {"8857434", "Chronicity (attribute)", "1"},
//    new String[] {"8208862", "Liquid substance", "4"},
//    new String[] {"8974365", "For (qualifier value)", "3"},
//    new String[] {"9121825", "Persistent", "1"},
//    new String[] {"8002021", "Hip", "1"},
    new String[] {"3816341", "Nephrocalcinosis", "1"},
//    new String[] {"8107588", "Acuteness", "2"},
//    new String[] {"5352400", "Goals", "1"},
//    new String[] {"8002019", "Hand", "1"},
//    new String[] {"9177137", "Complicity", "1"},
//    new String[] {"8109859", "Injuries", "1"},
//    new String[] {"8853075", "Through (qualifier value)", "1"},
    new String[] {"3814964", "Acute kidney failure", "4"},
    new String[] {"8096205", "Abdominal CT Scan", "1"},
//    new String[] {"8115576", "Respiratory Crackles", "1"},
    new String[] {"8120951", "Kidney Damage", "2"},
//    new String[] {"8840571", "Preferences (qualifier value)", "1"},
//    new String[] {"5352265", "Bleeding", "3"},
    new String[] {"8119543", "cytarabine/hydrocortisone/methotrexate protocol", "1"},
//    new String[] {"8959911", "Review of", "3"},
    new String[] {"2805598", "Pregnancy Complications", "1"},
//    new String[] {"8118766", "chronic", "1"},
//    new String[] {"9121766", "College", "1"},
    new String[] {"2791397", "Bad Breath", "1"},
//    new String[] {"8128975", "Use of", "2"},
//    new String[] {"8925098", "Severe", "1"},
//    new String[] {"8863556", "% normal", "1"},
    new String[] {"2791475", "Low Blood Pressure", "1"},
    new String[] {"8097048", "Kidney Disease Support Groups", "1"},
//    new String[] {"9724086", "Support group facilitation", "2"},
//    new String[] {"9724152", "Possible diagnosis", "2"},
    new String[] {"8118129", "Metallic Taste In Mouth", "1"},
    new String[] {"5047899", "Bruises Easily", "1"},
//    new String[] {"9723586", "Night time", "1"},
//    new String[] {"9076630", "Treatment intent (situation)", "3"},
    new String[] {"3815892", "Idiopathic Thrombocytopenic Purpura", "2"},
    new String[] {"8430397", "Kidney Failure Causes", "1"},
//    new String[] {"9436426", "Encyclopedias", "1"},
//    new String[] {"8107490", "Toxin", "1"},
    new String[] {"8107668", "Transfusion Reaction", "1"},
    new String[] {"9724135", "Liquid Dosage Form", "1"},
    new String[] {"5351976", "Arterial Blood Gas (ABG) Test", "1"},
//    new String[] {"8879422", "Revealed (qualifier value)", "1"},
    new String[] {"8132736", "Creatinine", "1"},
    new String[] {"5047359", "Fatigue", "1"},
//    new String[] {"8857603", "Very low (qualifier value)", "1"},
//    new String[] {"5047375", "testing", "3"},
    new String[] {"8106813", "Metabolic Acidosis", "1"},
    new String[] {"5356320", "Urinary Tract Obstruction", "1"},
    new String[] {"2791880", "Low-Lying Placenta", "2"},
    new String[] {"9153638", "Aminomethyltransferase", "1"},
    new String[] {"8100013", "Malignant Hypertension", "2"},
    new String[] {"3815176", "Nosebleed", "1"},
//    new String[] {"8945730", "Most (qualifier value)", "1"},
//    new String[] {"8113306", "Human body", "1"},
    new String[] {"5048140", "Carbohydrates", "1"},
    new String[] {"9139217", "Renal failure: [chronic] or [end stage]", "1"},
    new String[] {"9081115", "Inferior oblique overaction (disorder)", "21"},
    new String[] {"8116536", "Scleroderma", "2"},
    new String[] {"8917035", "Decreased", "1"},
//    new String[] {"8821341", "Has development (attribute)", "2"},
    new String[] {"9724574", "Hospitals", "1"},
//    new String[] {"8817788", "MAY BE A (attribute)", "2"},
//    new String[] {"8936682", "Some (qualifier value)", "1"},
//    new String[] {"8923034", "Lowing", "1"},
//    new String[] {"5047687", "Risk", "1"},
//    new String[] {"8120170", "Prevents", "3"},
//    new String[] {"8001538", "Lung", "2"},
    new String[] {"9122485", "Abdominal", "1"},
//    new String[] {"8107704", "Crushing Injury", "1"},
//    new String[] {"8950933", "Few (qualifier value)", "1"},
//    new String[] {"8965533", "Resulting in", "1"},
//    new String[] {"9376237", "Dangerousness", "2"},
//    new String[] {"8927598", "Increased", "2"},
    new String[] {"8117915", "Hand Tremor", "1"},
//    new String[] {"8832606", "With intensity (attribute)", "1"},
//    new String[] {"8856502", "Limitation (attribute)", "1"},
    new String[] {"3815643", "Thrombotic Thrombocytopenic Purpura", "1"},
    new String[] {"2795434", "Autoimmune Disease", "2"},
    new String[] {"8116540", "End Stage Kidney Disease", "2"},
    new String[] {"8129548", "Acute Bilateral Obstructive Uropathy", "1"},
    new String[] {"3815707", "Septic Shock", "2"},
    new String[] {"8129925", "Renal function", "1"},
    new String[] {"8114326", "Alteration In Consciousness", "2"},
//    new String[] {"9078848", "Retained", "1"},
//    new String[] {"5047368", "Prevention", "1"},
//    new String[] {"8118143", "Sluggishness", "1"},
    new String[] {"8119523", "Ultrasound of Kidney", "1"},
  };
  //  Acute respiratory failure
  //  http://www.nhlbi.nih.gov/health/health-topics/topics/rf/
  private static final String[][] CONCEPTS_FOR_ACUTE_RESPIRATORY_FAILURE_DOC = new String[][] {
//    new String[] {"9090659", "Needed (qualifier value)", "2"},
//    new String[] {"9723548", "High", "1"},
//    new String[] {"8130721", "Air", "3"},
//    new String[] {"3815234", "Gas", "1"},
//    new String[] {"9094066", "Times", "2"},
//    new String[] {"2791257", "Employment", "2"},
//    new String[] {"8953524", "Ophthalmic route (qualifier value)", "1"},
//    new String[] {"8893802", "Does move", "1"},
    new String[] {"8001560", "Heart", "1"},
//    new String[] {"8918116", "Preventive action", "1"},
//    new String[] {"3807096", "Long Term Health Care", "1"},
//    new String[] {"5216597", "Treatment Procedures", "1"},
//    new String[] {"8816727", "Body organ structure", "2"},
//    new String[] {"9109513", "Short-term", "1"},
    new String[] {"9103780", "Small Blood Vessels", "1"},
//    new String[] {"8860804", "Via airway (qualifier value)", "2"},
    new String[] {"8001539", "Mouth", "1"},
//    new String[] {"9072816", "Slowly (qualifier value)", "1"},
//    new String[] {"8951985", "Machine, device (physical object)", "1"},
//    new String[] {"9311815", "TOOS", "1"},
    new String[] {"8131518", "Rhenium", "1"},
    new String[] {"5349129", "Lung Diseases", "1"},
    new String[] {"8946147", "Air sacs", "5"},
    new String[] {"8113388", "Persons", "1"},
//    new String[] {"5047924", "Signs and Symptoms", "1"},
    new String[] {"2797094", "Emergency Treatment", "1"},
//    new String[] {"5047350", "Cause of disease", "1"},
//    new String[] {"8857313", "Quantity (attribute)", "1"},
    new String[] {"2792411", "venlafaxine", "1"},
    new String[] {"8636970", "Respiratory failure Causes", "1"},
//    new String[] {"8862747", "Out (qualifier value)", "1"},
//    new String[] {"8130736", "Aluminum", "1"},
    new String[] {"8232455", "carbon dioxide", "5"},
    new String[] {"8232457", "air", "3"},
//    new String[] {"8233392", "Travel", "1"},
//    new String[] {"8184606", "reaches", "1"},
//    new String[] {"8864906", "Condition (attribute)", "3"},
    new String[] {"2796152", "Chronic Obstructive Pulmonary Disease", "1"},
//    new String[] {"8954471", "Including", "3"},
//    new String[] {"8918620", "Removes from (attribute)", "2"},
    new String[] {"8927401", "Transnasal approach", "1"},
//    new String[] {"9061250", "MOVED TO", "1"},
//    new String[] {"8119030", "Comprehension", "2"},
//    new String[] {"9376843", "development", "2"},
//    new String[] {"8963194", "Low", "1"},
//    new String[] {"8825172", "SAME AS (attribute)", "2"},
//    new String[] {"8862800", "Increasing", "1"},
//    new String[] {"8856524", "Timing", "2"},
//    new String[] {"9063524", "MOVED FROM", "1"},
    new String[] {"5344093", "Shortness of Breath", "1"},
//    new String[] {"8918841", "For which was done (attribute)", "1"},
    new String[] {"5358222", "ventilator", "2"},
//    new String[] {"8945328", "Right and left", "1"},
//    new String[] {"9060048", "Dependent (qualifier value)", "1"},
//    new String[] {"8952351", "Timed", "2"},
//    new String[] {"8871599", "Severities", "2"},
//    new String[] {"8116002", "Finding of color of skin", "1"},
    new String[] {"8001713", "Pulmonary Alveoli", "1"},
//    new String[] {"8107949", "Respiratory Signs and Symptoms", "1"},
//    new String[] {"8874373", "Does control breathing", "1"},
    new String[] {"8437301", "Respiratory failure Symptoms", "1"},
//    new String[] {"8122759", "Time", "2"},
//    new String[] {"9109159", "Employed", "2"},
    new String[] {"5362178", "Chronic respiratory failure", "2"},
//    new String[] {"9724691", "Work", "2"},
//    new String[] {"8121699", "Home environment", "1"},
    new String[] {"8121001", "Nerve Damage", "1"},
    new String[] {"8829406", "Carbonic acid measurement", "1"},
    new String[] {"5047795", "nose", "1"},
//    new String[] {"5355868", "Respiratory failure", "6"},
    new String[] {"8002189", "Capillaries", "3"},
    new String[] {"8112666", "Craving", "1"},
    new String[] {"8232454", "oxygen", "6"},
    new String[] {"8118182", "Air hunger", "1"},
    new String[] {"8001737", "Brain", "1"},
//    new String[] {"2795416", "Diseases and Disorders", "1"},
//    new String[] {"8001719", "Lip", "1"},
//    new String[] {"8857434", "Chronicity (attribute)", "1"},
//    new String[] {"8974365", "For (qualifier value)", "1"},
//    new String[] {"8107588", "Acuteness", "1"},
//    new String[] {"5352400", "Goals", "2"},
//    new String[] {"8853075", "Through (qualifier value)", "3"},
    new String[] {"8891985", "Transoral approach", "1"},
    new String[] {"8098959", "Breathing control", "1"},
//    new String[] {"8001570", "Eye", "1"},
//    new String[] {"9134248", "Wealthy", "1"},
    new String[] {"9282093", "TLR1 Receptor", "1"},
    new String[] {"8001531", "Blood", "6"},
//    new String[] {"8880122", "Able to reach (finding)", "1"},
//    new String[] {"8118766", "chronic", "1"},
//    new String[] {"8925098", "Severe", "2"},
//    new String[] {"8205998", "Origin", "1"},
//    new String[] {"8936819", "Bloods", "6"},
//    new String[] {"9130564", "Failure", "1"},
//    new String[] {"8893801", "Able to move", "1"},
//    new String[] {"9076630", "Treatment intent (situation)", "3"},
    new String[] {"8132011", "Structure of nail of finger", "1"},
//    new String[] {"8106642", "Intensive Care Units", "1"},
    new String[] {"5348828", "Drowsiness", "1"},
//    new String[] {"9134442", "Moving", "1"},
//    new String[] {"8816803", "Process", "1"},
//    new String[] {"8113306", "Human body", "1"},
    new String[] {"8115275", "Rapid Breathing", "1"},
    new String[] {"8001533", "Windpipe", "3"},
//    new String[] {"8821341", "Has development (attribute)", "2"},
//    new String[] {"8121194", "Treatment of Underlying Disorders", "1"},
//    new String[] {"8923034", "Lowing", "1"},
//    new String[] {"8120170", "Prevents", "1"},
    new String[] {"8001538", "Lung", "6"},
    new String[] {"9724773", "Cell Respiration", "6"},
//    new String[] {"8184186", "Wellness", "1"},
    new String[] {"8115965", "Acute Respiratory Failure", "2"},
//    new String[] {"2804356", "Spinal Cord Injury", "2"},
    new String[] {"8094755", "Oxygen Therapy", "1"},
//    new String[] {"8927598", "Increased", "1"},
    new String[] {"8872630", "Able to control breathing", "1"},
//    new String[] {"5047411", "maine", "1"},
//    new String[] {"8926476", "Good", "1"},
//    new String[] {"8854803", "Lipping", "1"},
    new String[] {"8207937", "Sever's heel", "1"},
  };
}
