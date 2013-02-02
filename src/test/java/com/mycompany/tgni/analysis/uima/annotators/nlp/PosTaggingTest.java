package com.mycompany.tgni.analysis.uima.annotators.nlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 * Tests for checking out the OpenNLP API for noun-phrase 
 * generation.
 */
public class PosTaggingTest {

  private static final String[] INPUTS = new String[] { 
//    "The U.S. LEI continued to increase in July. However, with the exception of the money supply and interest rate components, other leading indicators show greater weakness – consistent with increasing concerns about the health of the economic expansion. Despite rising volatility, the leading indicators still suggest economic activity should be slowly expanding through the end of the year.",
//    "The composite economic indexes are the key elements in an analytic system designed to signal peaks and troughs in the business cycle. The leading, coincident, and lagging economic indexes are essentially composite averages of several individual leading, coincident, or lagging indicators. They are constructed to summarize and reveal common turning point patterns in economic data in a clearer and more convincing manner than any individual component – primarily because they smooth out some of the volatility of individual components.",
//    "Last week Professor Herbert Davenport Kay & associates of Toronto suggested in The Journal of Nutrition that beryllium, a metal related to calcium and now coming into industrial use (it strengthens and hardens aluminum alloys), may be an obscure cause of rickets.",
//    "Beryllium foil remains indispensible for high-resolution medical radiography, including CT scanning and mammography. Beryllium in newer generation mammography equipment enables a lower radiation dose scan with significantly finer tumor resolution, enabling breast cancer detection at its early, most treatable stages.",
//    "Arsenic, element 33, has a long and nefarious history; its very name has become synonymous with poison. In the 15th and 16th centuries, the Italian family of Borgias used arsenic as their favorite poison for political assassinations. Some even have suggested that Napoleon was poisoned by arsenic-tainted wine served to him while in exile.",
//    // and some of my own internal test cases
//    "Dr Johnson was leading the team.",
//    "Lead was used in pencils in the olden days.",
//    "Dr Johnson will lead the team.",
//    "Lead is the lead cause of lead poisoning",
//    "A precisely engineered and encased battery, capacitor, lead connectors, computer chip with memory, and software (known as “the can”) are typically implanted in the prepectoral space; a combined insulated pacing and defibrillator lead typically passes through the subclavian vein into the right ventricle. Many implantable cardiovert-defibrillator (ICD) leads also contain a second shocking coil, which is typically located in the superior vena cava or right atrium by virtue of its distance from the lead tip, and it can be readily located under fluoroscopy or routine chest radiography. The lead transmits the local electrographic reading at the myocardial contact site back to the can, where the information is processed. If any tachyarrhythmia is detected, the device can deliver both pacing and shock therapies designed to terminate the arrhythmia in response to a wide array of programmable parameters. If a shock is indicated, the capacitor charges to a prespecified voltage and delivers an electrical shock current by means of a prespecified vector. The shock vector is usually determined at the time of implantation, when the defibrillation threshold is checked. It can be configured between the right ventricle coil and the superior vena cava coil or between the right ventricle coil and the can, or it can include additional separate implantable shocking coils.",
//    "Kaplan-Meier analysis of the time to death from any cause in patients receiving conventional therapy for heart failure, conventional therapy plus amiodarone, or conventional therapy plus a conservatively programmed, shock-only, single-lead implantable cardioverter-defibrillator (ICD) in the Sudden Cardiac Death in Heart Failure Trial (SCD-HeFT). CI, confidence interval.",
    // home grown coordinate expansions
//    "AIDS epidemics are usually preceded by an increased onset of Hepatitis B and C",
//    "Patients with Hepatitis B/C are usually a greater risk for AIDS.",
//    "The patient in the Dr House show was diagnosed with lung, kidney and stomach cancer.",
//    "Hypo- and Hyperglicemia are diseases associated with the endocrine system.",
//    "Synthetic mi- and snoRNA are under active development in research labs around the world.",
    // from google using exact search on examples
//    "Viral hepatitis, including hepatitis A, B, and C, are distinct diseases that affect the liver.", //(webmd.com)
//    "This page contains late breaking information, as well as an archival record of updates on safety and regulatory issues related to Hepatitis A and B, including product approvals, significant labeling changes, safety warnings, notices of upcoming public meetings, and notices about proposed regulatory guidances.", // (fda.gov)
//    "Lead poisoning can cause an increased risk of brain, lung, stomach and kidney cancer.", // (cleanupblackwell.com)
    "Before we look at the difference between diabetes type-I and II, let's firstly look at diabaetes in general.", // (medicalnewstoday.com)
    "Restricting and purging anorexia are two different types of anorexia that people suffer from.", // (anorexiasurvivalguidaae.com)
//    "Here are some tips on pre and post surgery nutrition.", // (bestofmotherearth.com)
//    "A computer-based register linked to thyroid diagnostic laboratories was used to continuously identify all new cases of overt hyper- and hypothyroidism in two population cohorts with moderate and mild ID, respectively (Aalborg, n = 310,124; urinary iodine, 45 micro g/liter; and Copenhagen, n = 225,707; urinary iodine, 61 micro g/liter).", // (nlm.nih.gov)
//    "Medical and assistive devices are taxable for the most part, unconditionally zero-rated in certain cases, and conditionally zero-rated in certain cases.", //(revenuequebec.ca)
//    "These regions correspond to the least well conserved regions of the whole miRNA/snoRNA molecules.", // (ploscompbiol.org)
//    "Hetero- and Homogeneous mixtures are alike because they are both compounds, and both made up of different elements.", // (answers.com)
//    "Heterogeneous and Homogeneous mixtures are alike because they are both compounds, and both made up of different elements.", // (answers.com)

  };    
    
//  @Test
//  public void testSentenceSplitting() throws Exception {
//    SentenceDetector sd = null;
//    InputStream mis = null;
//    try {
//      mis = new FileInputStream(new File("src/main/resources/models/en_sent.bin"));
//      SentenceModel sm = new SentenceModel(mis);
//      mis.close();
//      sd = new SentenceDetectorME(sm);
//      String[] sentences = sd.sentDetect(SENTENCES);
//      for (int i = 0; i < sentences.length; i++) {
//        System.out.println("sentence[" + i + "]: " + sentences[i]);
//        Span[] spans = sd.sentPosDetect(sentences[i]);
//        for (int j = 0; j < spans.length; j++) {
//          System.out.println("span[" + j + "]: (" + 
//            spans[j].getStart() + "," + spans[j].getEnd() + "): " + 
//            spans[j].getCoveredText(sentences[j]) + "/" + 
//            spans[j].getType());
//        }
//      }
//    } catch (IOException e) {
//      throw e;
//    } finally {
//      IOUtils.closeQuietly(mis);
//    }
//  }
  
//  @Test
//  public void testPosTagging() throws Exception {
//    Tokenizer tokenizer = null;
//    POSTagger postagger = null;
//    InputStream tmis = null;
//    InputStream pmis = null;
//    try {
//      tmis = new FileInputStream("src/main/resources/models/en_token.bin");
//      TokenizerModel tm = new TokenizerModel(tmis);
//      tmis.close();
//      tokenizer = new TokenizerME(tm);
//      pmis = new FileInputStream("src/main/resources/models/en_pos_maxent.bin");
//      POSModel pm = new POSModel(pmis);
//      pmis.close();
//      postagger = new POSTaggerME(pm);
//      List<String> tokens = Arrays.asList(tokenizer.tokenize("Some leaders have died of lead poisoning while leading."));
//      List<String> poss = postagger.tag(tokens);
//      int ntok = tokens.size();
//      for (int i = 0; i < ntok; i++) {
//        System.out.println(tokens.get(i) + "/" + poss.get(i));
//      }
//    } catch (IOException e) {
//      throw e;
//    } finally {
//      IOUtils.closeQuietly(tmis);
//      IOUtils.closeQuietly(pmis);
//    }
//  }
  
//  @Test
//  public void testChunking() throws Exception {
//    SentenceDetector sd = null;
//    Tokenizer tokenizer = null;
//    POSTagger posTagger = null;
//    Chunker chunker = null;
//    InputStream smis = null;
//    InputStream tmis = null;
//    InputStream pmis = null;
//    InputStream cmis = null;
//    try {
//      smis = new FileInputStream("/prod/web/data/tgni/conf/models/en_sent.bin");
//      SentenceModel sm = new SentenceModel(smis);
//      smis.close();
//      sd = new SentenceDetectorME(sm);
//      tmis = new FileInputStream("/prod/web/data/tgni/conf/models/en_token.bin");
//      TokenizerModel tm = new TokenizerModel(tmis);
//      tmis.close();
//      tokenizer = new TokenizerME(tm);
//      pmis = new FileInputStream("/prod/web/data/tgni/conf/models/en_pos_maxent.bin");
//      POSModel pm = new POSModel(pmis);
//      pmis.close();
//      posTagger = new POSTaggerME(pm);
//      cmis = new FileInputStream("/prod/web/data/tgni/conf/models/en_chunker.bin");
//      ChunkerModel cm = new ChunkerModel(cmis);
//      cmis.close();
//      chunker = new ChunkerME(cm);
//      // break input into sentences
//      for (String input : INPUTS) {
//        System.out.println("input=" + input);
//        String[] sentences = sd.sentDetect(input);
//        for (int si = 0; si < sentences.length; si++) {
////          System.out.println("..sentence=" + sentences[si]);
//          // tokenize each sentence
//          Span[] tokenSpans = tokenizer.tokenizePos(sentences[si]);
//          String[] tokens = new String[tokenSpans.length];
//          for (int ti = 0; ti < tokenSpans.length; ti++) {
//            tokens[ti] = tokenSpans[ti].getCoveredText(sentences[si]).toString();
//          }
//          // find POS for each token and set them back into the
//          // PosToken array
//          final String[] tags = posTagger.tag(tokens);
////          System.out.println(ArrayUtils.toString(zip(tokens, tags, "/")));
//          // now build chunks and partition noun phrases and
//          // non noun phrases
//          Span[] chunks = chunker.chunkAsSpans(tokens, tags);
//          List<PosToken> nounPhrases = new ArrayList<PosToken>();
//          List<PosToken> nounWords = new ArrayList<PosToken>();
//          for (Span chunk : chunks) {
////            System.out.println("chunk: (" + chunk.getStart() + "," +
////              chunk.getEnd() + "): [" + StringUtils.join(
////              ArrayUtils.subarray(tokens, chunk.getStart(), chunk.getEnd()), " ") +
////              "]/" + chunk.getType());
//            PosToken phrase = new PosToken();
//            if ("NP".equals(chunk.getType())) {
//              phrase.start = chunk.getStart();
//              phrase.end = chunk.getEnd();
//              phrase.coveredText = StringUtils.join(
//                ArrayUtils.subarray(tokens, phrase.start, phrase.end), " ");
//              phrase.tag = chunk.getType();
//              nounPhrases.add(phrase);
//            } else {
//              for (int ti = chunk.getStart(); ti < chunk.getEnd(); ti++) {
//                if ("NN".equals(tags[ti]) || "NNP".equals(tags[ti])) {
//                  PosToken word = new PosToken();
//                  word.start = tokenSpans[ti].getStart();
//                  word.end = tokenSpans[ti].getEnd();
//                  word.coveredText = tokens[ti];
//                  word.tag = tags[ti];
//                  nounWords.add(word);
//                }
//              }
//            }
//          }
//          // print the noun phrases
//          System.out.println("..noun phrases");
//          for (PosToken nounPhrase : nounPhrases) {
//            String text = StringUtils.lowerCase(
//              nounPhrase.coveredText.toString());
//            if (text.contains(" and ") || 
//                text.contains(" or ") ||
//                text.contains("/")) {
//              System.out.println("...." + nounPhrase);
//            }
//          }
////          // print the noun words in non-noun phrases
////          System.out.println("..noun words");
////          for (PosToken nounWord : nounWords) {
////            System.out.println("...." + nounWord);
////          }
//        }
//      }
//    } catch (IOException e) {
//      throw e;
//    } finally {
//      IOUtils.closeQuietly(smis);
//      IOUtils.closeQuietly(tmis);
//      IOUtils.closeQuietly(pmis);
//      IOUtils.closeQuietly(cmis);
//    }
//  }

//  private class PosToken {
//    public int start;
//    public int end;
//    public CharSequence coveredText;
//    public String tag;
//    
//    public String toString() {
//      return "(" + start + "," + end + "): [" + coveredText + "]/" + tag;
//    }
//  };
//  
//  @Test
//  public void testTreeParsing() throws Exception {
//    InputStream tmis = new FileInputStream("/prod/web/data/tgni/conf/models/en_token.bin");
//    TokenizerModel tm = new TokenizerModel(tmis);
//    tmis.close();
//    Tokenizer tokenizer = new TokenizerME(tm);
//
//    InputStream pmis = new FileInputStream(
//      "/prod/web/data/tgni/conf/models/en_parser_chunking.bin");
//    ParserModel pm = new ParserModel(pmis);
//    pmis.close();
//    Parser parser = ParserFactory.create(pm);
//    
//    for (String sentence : INPUTS) {
//      System.out.println("sentence=" + sentence);
//      Parse[] topParses = ParserTool.parseLine(sentence, parser, 1);
//      for (Parse topParse : topParses) {
//        walk_r(sentence, topParse, 0);
//      }
//    }
//  }
//  
//  private void walk_r(String sentence, Parse parse, int depth) {
//    String label = parse.getLabel();
//    Span span = parse.getSpan();
//    String text = span.getCoveredText(sentence).toString();
//    if ("and".equalsIgnoreCase(text) || "or".equalsIgnoreCase(text)) {
//      Parse p = parse.getCommonParent(parse);
//      Span sp = p.getSpan();
//      String chunk = sp.getCoveredText(sentence).toString();
//      if (!("and".equalsIgnoreCase(chunk) || 
//          "or".equalsIgnoreCase(chunk))) {
//        System.out.println(chunk);
//      }
//    }
////    System.out.println(StringUtils.repeat(".", depth) + 
////      span.getCoveredText(sentence) + " /" + span.getType());
//    for (Parse child : parse.getChildren()) {
//      walk_r(sentence, child, depth + 1);
//    }
//    
//  }

  private static final String[] CANDIDATE_PHRASES = new String[] {
    "hepatitis A, B and C",
    "safety and regulatory issues",
    "Hepatitis A and B",
    "upcoming public meetings, and notices",
    "brain, lung, stomach and kidney cancer",
    "diabetes type I and II",
    "Restricting and purging anorexia",
    "pre and post surgery nutrition",
    "310,124; urinary iodine, 45 micro g/liter; and Copenhagen, n",
    "moderate and mild ID",
    "overt hyperthyroidism and hypothyroidism",
    "Medical and assistive devices",
    "Medical and assistive devices are taxable for the most part, unconditionally zero rated in certain cases, and conditionally zero rated in certain cases",
    "the whole miRNA/snoRNA molecules",
    "heterogeneous and Homogeneous mixtures",
  };
  
  @Test
  public void testCoordinateExpansion() throws Exception {
    for (String candidatePhrase : CANDIDATE_PHRASES) {
      System.out.println("phrase=" + candidatePhrase);
      String[] tokens = StringUtils.split(candidatePhrase, " ");
      int ccpos = 0;
      String cctype = null;
      // first pass, find the position of the conjunction
      for (int i = 0; i < tokens.length; i++) {
        if ("and".equalsIgnoreCase(tokens[i]) ||
            "or".equalsIgnoreCase(tokens[i])) {
          ccpos = i;
          cctype = tokens[i];
          break;
        }
        if (tokens[i].indexOf('/') > -1) {
          ccpos = i;
          cctype = "/";
          break;
        }
      }
      System.out.println("ccpos=" + ccpos + ", cctype=" + cctype);
      if (ccpos > 0) {
        String phrasePre = "";
        String phrasePost = "";
        List<String> ccwords = new ArrayList<String>();
        if ("/".equals(cctype)) {
          // handles the following cases:
          // xx A/B/.. yy => xx A yy, xx B yy, ...
          ccwords.addAll(
            Arrays.asList(StringUtils.split(tokens[ccpos], "/")));
          phrasePre = StringUtils.join(
            Arrays.asList(
            ArrayUtils.subarray(tokens, 0, ccpos)).iterator(), " ");
          phrasePost = StringUtils.join(
            Arrays.asList(
            ArrayUtils.subarray(tokens, ccpos+1, tokens.length)).iterator(), 
            " ");
        } else {
          if (ccpos > 0 && ccpos < tokens.length - 1) {
            // handles the following cases:
            // xx A (and|or) B C yy => xx A C yy, xx B C yy
            // xx A B (and|or) C yy => xx A C yy, xx B C yy
            ccwords.add(tokens[ccpos - 1]);
            ccwords.add(tokens[ccpos + 1]);
            // look back from ccpos-1 until we stop seeing
            // words with trailing commas
            int currpos = ccpos - 2;
            while (currpos >= 0) {
              if (tokens[currpos].endsWith(",")) {
                ccwords.add(tokens[currpos].substring(
                  0, tokens[currpos].length() - 1));
                currpos--;
              } else {
                break;
              }
            }
            if (currpos >= 0) {
              phrasePre = StringUtils.join(
                Arrays.asList(ArrayUtils.subarray(
                tokens, 0, currpos+1)), " ");
            }
            phrasePost = StringUtils.join(
              Arrays.asList(ArrayUtils.subarray(
              tokens, ccpos + 2, tokens.length)), " ");
          }
        }
        for (String ccword : ccwords) {
          System.out.println("EXP:" + StringUtils.join(new String[] {
            phrasePre, ccword, phrasePost}, " "));
        }
      }
    }
  }
  
  //  // NN, NNP = noun, proper noun
//  // NP = noun phrase
//  
  private String[] zip(String[] tokens, String[] tags, String zipper) 
      throws Exception {
    if (tokens == null || tags == null || 
        tokens.length != tags.length) {
      throw new Exception("unable to zip, check inputs");
    }
    int n = tokens.length;
    String[] zipped = new String[n];
    for (int i = 0; i < n; i++) {
      zipped[i] = StringUtils.join(
        new String[] {tokens[i], tags[i]}, zipper);
    }
    return zipped;
  }
}
