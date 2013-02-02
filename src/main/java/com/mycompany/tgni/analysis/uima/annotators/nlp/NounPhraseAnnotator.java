package com.mycompany.tgni.analysis.uima.annotators.nlp;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.parser.Parser;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Pair;
import opennlp.tools.util.Span;

import org.apache.commons.collections15.Closure;
import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.IntRange;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.mycompany.tgni.utils.AnnotatorUtils;
import com.mycompany.tgni.utils.NlpUtils;
import com.mycompany.tgni.utils.UimaUtils;

/**
 * Annotate noun phrases in sentences from within blocks of
 * text (marked up with TextAnnotation) from either HTML or
 * plain text documents. Using the OpenNLP library and models,
 * the incoming text is tokenized into sentences, then each 
 * sentence is tokenized to words and POS tagged, and finally
 * tokens are grouped together into chunks. Of these chunks,
 * only the noun phrases are annotated. 
 */
public class NounPhraseAnnotator extends JCasAnnotator_ImplBase {

  private TokenizerME tokenizer;
  private POSTaggerME posTagger;
  private ChunkerME chunker;
  private Parser parser;
  
  @Override
  public void initialize(UimaContext ctx) 
      throws ResourceInitializationException {
    super.initialize(ctx);
    InputStream tmis = null;
    InputStream pmis = null;
    InputStream cmis = null;
    InputStream rmis = null;
    try {
      tmis = getContext().getResourceAsStream("TokenizerModel");
      TokenizerModel tmodel = new TokenizerModel(tmis);
      tokenizer = new TokenizerME(tmodel);
      tmis.close();
      pmis = getContext().getResourceAsStream("POSModel");
      POSModel pmodel = new POSModel(pmis);
      posTagger = new POSTaggerME(pmodel);
      pmis.close();
      cmis = getContext().getResourceAsStream("ChunkerModel");
      ChunkerModel cmodel = new ChunkerModel(cmis);
      chunker = new ChunkerME(cmodel);
      cmis.close();
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    } finally {
      IOUtils.closeQuietly(rmis);
      IOUtils.closeQuietly(cmis);
      IOUtils.closeQuietly(pmis);
      IOUtils.closeQuietly(tmis);
    }
  }
  
  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    FSIndex index = jcas.getAnnotationIndex(SentenceAnnotation.type);
    for (Iterator<SentenceAnnotation> it = index.iterator(); it.hasNext(); ) {
      SentenceAnnotation inputAnnotation = it.next();
      if (UimaUtils.MIMETYPE_STRING.equals(jcas.getSofaMimeType())) {
        addNounPhraseAnnotation(jcas, inputAnnotation.getBegin(), 
          inputAnnotation.getEnd());
      } else {
        String sentence = inputAnnotation.getCoveredText();
        sentence = AnnotatorUtils.whiteout(sentence, new char[] {'-'});
        int sentStart = inputAnnotation.getBegin();
        Span[] tokSpans = tokenizer.tokenizePos(sentence);
        String[] tokens = new String[tokSpans.length];
        for (int i = 0; i < tokens.length; i++) {
          tokens[i] = tokSpans[i].getCoveredText(sentence).toString();
        }
        String[] tags = posTagger.tag(tokens);
        Set<IntRange> nounPhraseRanges = new HashSet<IntRange>();
        // shallow parse using chunker
        Span[] chunks = chunker.chunkAsSpans(tokens, tags);
//        System.out.println(NlpUtils.getChunkingDebugInfo(tokens, tags, chunks));
        for (Span chunk : chunks) {
          if ("NP".equals(chunk.getType())) {
            addNounPhraseAnnotation(jcas, 
              sentStart + tokSpans[chunk.getStart()].getStart(), 
              sentStart + tokSpans[chunk.getEnd() - 1].getEnd());
          }
        }
        // rule based extraction of NP (based on test results
        // from coordinate expansion
        // NP (CC NP)+, (VP containing CC) NP, NP+ CC NP, NP CC NP+
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
//        System.out.println(chunkTagStr);
//        System.out.println(chunkTagRanges);
        // rule 1: NP := (NP )+CC NP
        Pattern p1 = Pattern.compile("(NP )+CC NP"); 
        Matcher m1 = p1.matcher(chunkTagStr);
        int pos = 0;
        while (m1.find(pos)) {
          int start = m1.start();
          int end = m1.end();
          IntRange npr = NlpUtils.getPhraseRange(sentence, 
            chunkTagStr, chunkTagRanges, tokSpans, start, end);
          addNounPhraseAnnotation(jcas, npr.getMinimumInteger(), 
            npr.getMaximumInteger());
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
          IntRange npr = NlpUtils.getPhraseRange(sentence, 
            chunkTagStr, chunkTagRanges, tokSpans, start, end);
          addNounPhraseAnnotation(jcas, npr.getMinimumInteger(), 
            npr.getMaximumInteger());
          pos = end;
        }
      }
    }
  }

  private void addNounPhraseAnnotation(JCas jcas, 
      int start, int end) {
    NounPhraseAnnotation annotation = new NounPhraseAnnotation(jcas);
    annotation.setBegin(start);
    annotation.setEnd(end);
    annotation.addToIndexes(jcas);
  }
}
