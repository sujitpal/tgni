package com.mycompany.tgni.analysis.uima.annotators.nlp;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import opennlp.tools.chunker.Chunker;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.mycompany.tgni.utils.NlpUtils;

/**
 * TODO: class level javadocs
 */
public class CoordinateConstructionTest {

  private final String[] TEST_SENTENCES = new String[] {
    "Viral hepatitis, including hepatitis A, B, and C, are distinct diseases that affect the liver.", //(webmd.com)
    "This page contains late breaking information, as well as an archival record of updates on safety and regulatory issues related to Hepatitis A and B, including product approvals, significant labeling changes, safety warnings, notices of upcoming public meetings, and notices about proposed regulatory guidances.", // (fda.gov)
    "Lead poisoning can cause an increased risk of brain, lung, stomach and kidney cancer.", // (cleanupblackwell.com)
    "Before we look at the difference between diabetes type-I and II, let's firstly look at diabaetes in general.", // (medicalnewstoday.com)
    "Restricting and purging anorexia are two different types of anorexia that people suffer from.", // (anorexiasurvivalguidaae.com)
    "Here are some tips on pre and post surgery nutrition.", // (bestofmotherearth.com)
    "A computer-based register linked to thyroid diagnostic laboratories was used to continuously identify all new cases of overt hyper- and hypothyroidism in two population cohorts with moderate and mild ID, respectively (Aalborg, n = 310,124; urinary iodine, 45 micro g/liter; and Copenhagen, n = 225,707; urinary iodine, 61 micro g/liter).", // (nlm.nih.gov)
    "Medical and assistive devices are taxable for the most part, unconditionally zero-rated in certain cases, and conditionally zero-rated in certain cases.", //(revenuequebec.ca)
    "These regions correspond to the least well conserved regions of the whole miRNA/snoRNA molecules.", // (ploscompbiol.org)
    "Hetero- and Homogeneous mixtures are alike because they are both compounds, and both made up of different elements.", // (answers.com)
  };
  
  private static final String[] CONJ_WORDS = new String[] {
    "and", "or",
  };
  private static final String[] PREFIX_WORDS = new String[] {
    "pre", "post", "hypo", "hyper", "inter", "intra", 
    "over", "under", "infra", "ultra", "hetero", "homo",
  };
  
  @Test
  public void findCandidatePhrases() throws Exception {
    Tokenizer tokenizer = null;
    POSTagger posTagger = null;
    Chunker chunker = null;
    Parser parser = null;
    InputStream tmis = null;
    InputStream pmis = null;
    InputStream cmis = null;
    InputStream pamis = null;
    Set<String> ccs = new HashSet<String>(Arrays.asList(CONJ_WORDS));
    Set<String> prefixes = new HashSet<String>(Arrays.asList(PREFIX_WORDS));
    try {
      tmis = new FileInputStream("/prod/web/data/tgni/conf/models/en_token.bin");
      TokenizerModel tm = new TokenizerModel(tmis);
      tokenizer = new TokenizerME(tm);
      pmis = new FileInputStream("/prod/web/data/tgni/conf/models/en_pos_maxent.bin");
      POSModel pm = new POSModel(pmis);
      posTagger = new POSTaggerME(pm);
      cmis = new FileInputStream("/prod/web/data/tgni/conf/models/en_chunker.bin");
      ChunkerModel cm = new ChunkerModel(cmis);
      chunker = new ChunkerME(cm);
      pamis = new FileInputStream("/prod/web/data/tgni/conf/models/en_parser_chunking.bin");
      ParserModel pam = new ParserModel(pamis);
      parser = ParserFactory.create(pam);
      for (String sentence : TEST_SENTENCES) {
        System.out.println("sentence: " + sentence);
        sentence = removeCoordinatePrefixHyphens(sentence);
        int expectedNumPhrases = countConjWords(sentence);
        int actualNumPhrases = 0;
        Span[] tokenSpans = tokenizer.tokenizePos(sentence);
        String[] tokens = new String[tokenSpans.length];
//        TagInfo[] tagInfos = new TagInfo[tokenSpans.length];
        for (int i = 0; i < tokenSpans.length; i++) {
          tokens[i] = tokenSpans[i].getCoveredText(sentence).toString();
//          tagInfos[i] = new TagInfo();
//          tagInfos[i].start = tokenSpans[i].getStart();
//          tagInfos[i].end = tokenSpans[i].getEnd();
//          tagInfos[i].text = tokens[i];
        }
        // preprocess tokens to rewrite prefix words
        preprocessPrefixes(prefixes, ccs, tokens);
        String[] tags = posTagger.tag(tokens);
//        for (int i = 0; i < tokenSpans.length; i++) {
//          tagInfos[i].tag = tags[i];
//        }
        Set<String> candidatePhrases = new HashSet<String>();
        // shallow parse using chunker
        Span[] chunks = chunker.chunkAsSpans(tokens, tags);
        System.out.println(NlpUtils.getChunkingDebugInfo(tokens, tags, chunks));
        for (Span chunk : chunks) {
          int chunkTokenStart = chunk.getStart();
          int chunkTokenEnd = chunk.getEnd();
          if ("NP".equals(chunk.getType()) &&
              containsToken(tokens, chunkTokenStart, chunkTokenEnd, ccs)) {
            String np = StringUtils.join(
              ArrayUtils.subarray(tokens, chunkTokenStart, chunkTokenEnd),
              " ");
            if (! hasOverlapOnSlashSeparator(np)) {
              np = np.replace(" , ", ", ").
                replace(", and ", " and ").
                replace("-", " "); // clean up
              candidatePhrases.add(np);
              expectedNumPhrases--;
            } else {
              actualNumPhrases++;
            }
          }
        }
        // fallback to deep parse using parser
        if (actualNumPhrases < expectedNumPhrases) {
          Parse[] parses = ParserTool.parseLine(sentence, parser, 1);
          for (Parse parse : parses) {
            walkParseTree_r(sentence, parse, candidatePhrases);
          }
        }
        // print candidate phrases
        for (String candidatePhrase : candidatePhrases) {
          System.out.println(".. " + candidatePhrase);
          List<String> expandedPhrases = expandPhrase(candidatePhrase);
          for (String expandedPhrase : expandedPhrases) {
            System.out.println(".... " + expandedPhrase);
          }
        }
        // match phrases against taxonomy
      }
    } finally {
      IOUtils.closeQuietly(tmis);
      IOUtils.closeQuietly(pmis);
      IOUtils.closeQuietly(cmis);
    }
  }

  private List<String> expandPhrase(String candidatePhrase) {
    List<String> expandedPhrases = new ArrayList<String>();
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
          if (ccpos + 2 < tokens.length) {
            phrasePost = StringUtils.join(
              Arrays.asList(ArrayUtils.subarray(
              tokens, ccpos + 2, tokens.length)), " ");
          }
        }
      }
      for (String ccword : ccwords) {
        expandedPhrases.add(StringUtils.join(new String[] {
          phrasePre, ccword, phrasePost}, " "));
      }
    }
    return expandedPhrases;
  }

  private void walkParseTree_r(String sentence, Parse parse,
      Set<String> candidatePhrases) {
    Span span = parse.getSpan();
    try {
      String text = span.getCoveredText(sentence).toString();
      if ("and".equalsIgnoreCase(text) || "or".equalsIgnoreCase(text)) {
        Parse pparse = parse.getCommonParent(parse);
        Span pspan = pparse.getSpan();
        String chunk = pspan.getCoveredText(sentence).toString();
        if (! ("and".equalsIgnoreCase(chunk) ||
            "or".equalsIgnoreCase(chunk))) {
          // remove trailing punctuation
          if (chunk.matches("^.*\\p{Punct}$")) {
            chunk = chunk.substring(0, chunk.length() - 1);
          }
          chunk = chunk.replace("-", " ");
          candidatePhrases.add(chunk);
        }
      }
    } catch (Exception e) {
      // attempt to ignore IllegalArgumentException caused by
      // the span.getCoveredText() attempting to lookup a larger
      // span than the sentence length. Nothing much I can do 
      // here, this is probably an issue with OpenNLP.
    }
    for (Parse child : parse.getChildren()) {
      walkParseTree_r(sentence, child, candidatePhrases);
    }
  }

  private int countConjWords(String sentence) {
    int numConjWords = 0;
    for (String conjWord : CONJ_WORDS) {
      numConjWords += StringUtils.countMatches(sentence, 
        " " + conjWord + " ");
    }
    numConjWords += StringUtils.countMatches(sentence, "/");
    return numConjWords;
  }

  private String removeCoordinatePrefixHyphens(String sentence) {
    return sentence.replaceAll("- and ", "  and ").
      replaceAll("- or ", "  or ");
  }
  
  private void preprocessPrefixes(Set<String> prefixes, 
      Set<String> ccs, String[] tokens) {
    for (int i = 0; i < tokens.length; i++) {
      if (ccs.contains(StringUtils.lowerCase(tokens[i]))) {
        // check before and after
        String preWord = (i > 0) ? 
          StringUtils.lowerCase(tokens[i-1]) : null;
        String postWord = (i < tokens.length - 1) ? 
          StringUtils.lowerCase(tokens[i+1]) : null;
        if (preWord != null && postWord != null) {
          if (preWord.endsWith("-")) preWord = preWord.substring(0, preWord.length() - 1);
          if (prefixes.contains(preWord)) {
            // attempt to split postWord along one of the prefixes
            String bareWord = null;
            for (String prefix : prefixes) {
              if (postWord.startsWith(prefix)) {
                bareWord = postWord.substring(prefix.length());
                break;
              }
            }
            if (StringUtils.isNotEmpty(bareWord)) {
              tokens[i-1] = preWord + bareWord;
            }
          }
        }
      }
    }
  }

  private boolean containsToken(String[] tokens, int start,
      int end, Set<String> ccs) {
    String[] chunkTokens = (String[]) ArrayUtils.subarray(
      tokens, start, end);
    for (String chunkToken : chunkTokens) {
      if (ccs.contains(StringUtils.lowerCase(chunkToken))) {
        return true;
      }
      if (chunkToken.contains("/")) {
        return true;
      }
    }
    return false;
  }

  private boolean hasOverlapOnSlashSeparator(String np) {
    int slashPos = np.indexOf('/');
    if (slashPos > -1) {
      int start = np.lastIndexOf(' ', slashPos-1) + 1;
      if (start == -1) start = 0;
      int end = np.indexOf(' ', slashPos+1);
      if (end == -1) end = np.length();
      String prevWord = np.substring(start, slashPos);
      String nextWord = np.substring(slashPos+1, end);
      double similarity = computeStringSimilarity(prevWord, nextWord);
      return similarity > 0.0D;
    }
    return true;
  }

  public double computeStringSimilarity(String s1, String s2) {
    ArrayList<String> pairs1 = letterPairs(s1);
    ArrayList<String> pairs2 = letterPairs(s2);
    int intersection = 0;
    int union = pairs1.size() + pairs2.size();
    for (int i = 0; i < pairs1.size(); i++) {
      Object pair1=pairs1.get(i);
      for(int j = 0; j < pairs2.size(); j++) {
        Object pair2=pairs2.get(j);
        if (pair1.equals(pair2)) {
          intersection++;
          pairs2.remove(j);
          break;
        }
      }
    }
    return (2.0 * intersection) / union;
  }

  private ArrayList<String> letterPairs(String str) {
    int numPairs = str.length()-1;
    ArrayList<String> pairs = new ArrayList<String>();
    for (int i = 0; i < numPairs; i++) {
        pairs.add(str.substring(i, i + 2));
    }
    return pairs;
  }

//  private class TagInfo {
//    public int start;
//    public int end;
//    public String text;
//    public String tag;
//  }
}
