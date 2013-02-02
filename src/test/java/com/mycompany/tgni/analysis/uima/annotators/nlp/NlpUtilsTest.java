package com.mycompany.tgni.analysis.uima.annotators.nlp;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.chunker.Chunker;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Pair;
import opennlp.tools.util.Span;

import org.apache.commons.collections15.Closure;
import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.IntRange;
import org.junit.Test;

import com.mycompany.tgni.utils.AnnotatorUtils;
import com.mycompany.tgni.utils.NlpUtils;

/**
 * TODO: class level javadocs
 */
public class NlpUtilsTest {

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

  @Test
  public void testNlpUtilsPosViews() throws Exception {
    Tokenizer tokenizer = null;
    POSTagger posTagger = null;
    Chunker chunker = null;
    InputStream tmis = null;
    InputStream pmis = null;
    InputStream cmis = null;
    InputStream pamis = null;
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
      for (String sentence : TEST_SENTENCES) {
        System.out.println("sentence: " + sentence);
        sentence = AnnotatorUtils.whiteout(sentence, new char[] {'-'});
        Span[] tokenSpans = tokenizer.tokenizePos(sentence);
        String[] tokens = new String[tokenSpans.length];
        int i = 0;
        for (Span tokenSpan : tokenSpans) {
          tokens[i++] = tokenSpan.getCoveredText(sentence).toString();
        }
        String[] tags = posTagger.tag(tokens);
        Span[] chunks = chunker.chunkAsSpans(tokens, tags);
        System.out.println(NlpUtils.getChunkingDebugInfo(tokens, tags, chunks));
        List<Pair<String,IntRange>> chunkTags = NlpUtils.getChunkTags(tokens, tags, chunks);
        final List<String> chunkTagValues = new ArrayList<String>();
        final Map<Integer,IntRange> chunkTagRanges = new HashMap<Integer,IntRange>();
        CollectionUtils.forAllDo(chunkTags, 
          new Closure<Pair<String,IntRange>>() {
            private int idx = 0;
            private int pos = 0;
            @Override
            public void execute(Pair<String,IntRange> p) {
              if (idx > 0) {
                pos++; // for space
              }
              chunkTagValues.add(p.a);
              chunkTagRanges.put(pos, p.b);
              pos += p.a.length();
              idx++;
            }
        });
        String chunkTagStr = StringUtils.join(chunkTagValues.iterator(), " ");
        System.out.println(chunkTagStr);
        System.out.println(chunkTagRanges);
        // rules filtering
        
        // rule 1: NP := (NP )+CC NP
        Pattern p1 = Pattern.compile("(NP )+CC NP"); 
        Matcher m1 = p1.matcher(chunkTagStr);
        int pos = 0;
        while (m1.find(pos)) {
          int start = m1.start();
          int end = m1.end();
          String additionalNp = NlpUtils.getPhrase(sentence, 
            chunkTagStr, chunkTagRanges, tokenSpans, start, end); 
          System.out.println("additional NP=[" + additionalNp + "]");
          pos = end;  
        }
        // rule 2: NP := (VP containing CC) NP
        Pattern p2 = Pattern.compile("VP NP"); // if VP contains CC
        Matcher m2 = p2.matcher(chunkTagStr);
        pos = 0;
        if (m2.find(pos)) {
          // check if the VP contains a CC else skip
          int start = m2.start();
          int end = m2.end();
          int vpChunkId = StringUtils.countMatches(
            chunkTagStr.substring(0, start), " ");
          Set<String> tokenTags = new HashSet<String>(
            NlpUtils.getTokenTags(tokens, tags, chunks[vpChunkId]));
          if (! tokenTags.contains("CC")) {
            continue;
          }
          String additionalNp = NlpUtils.getPhrase(sentence, 
            chunkTagStr, chunkTagRanges, tokenSpans, start, end);
          System.out.println("additional NP=[" + additionalNp + "]");
          pos = end;
        }
      }
    } finally {
      IOUtils.closeQuietly(tmis);
      IOUtils.closeQuietly(pmis);
      IOUtils.closeQuietly(cmis);
    }
  }
}
