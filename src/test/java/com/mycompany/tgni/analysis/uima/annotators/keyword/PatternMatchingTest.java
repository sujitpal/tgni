package com.mycompany.tgni.analysis.uima.annotators.keyword;

import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit test to test patterns in the pattern_*.txt files
 * to ensure regression.
 */
public class PatternMatchingTest {

  @Test
  public void testAbbreviationsPattern() throws Exception {
    String[] testAbbreviations = new String[] {
      "AAMI", "AAA", "AARP", "ABC"
    };
    Pattern p = Pattern.compile("[A-Z]{2}[A-Za-z0-9-]*");
    for (String testAbbreviation : testAbbreviations) {
      Matcher m = p.matcher(testAbbreviation);
      System.out.println(testAbbreviation + " ... " + 
          (m.matches() ? "OK" : "NO-MATCH"));
      Assert.assertTrue(m.matches());
    }
  }
  
  @Test
  public void testAcronymPluralPattern() throws Exception {
    String[] testNegativePluralAbbreviations = new String[] {
      "Asthma", "AIDS", "Diabetes",
    };
    String[] testPositivePluralAbbreviations = new String[] {
        "AARPs", "ABCs"
    };
    Pattern p = Pattern.compile("^[A-Z]+s$");
    for (String s : testNegativePluralAbbreviations) {
      Matcher m = p.matcher(s);
      System.out.println(s + " ..." + 
        (m.matches() ? "OK" : "NO-MATCH"));
      Assert.assertFalse(m.matches());
    }
    for (String s : testPositivePluralAbbreviations) {
      Matcher m = p.matcher(s);
      System.out.println(s + " ..." + 
        (m.matches() ? "OK" : "NO-MATCH"));
      Assert.assertTrue(m.matches());
    }
  }

  @Test
  public void testChemicalNamesPattern() throws Exception {
    String[] testChemicalNames = new String[] {
      "5-ethyl-3-methyl-5-phenylhydantoin",
      "5-aminosalicyl",
      "5fuacrnm",
      "1alpha-ohd2",
      "5FC",
      "5-FU",
      "2,4,6-triethylimino-1,3,5-triazin"
    };
    Pattern p = Pattern.compile("[1-9]+[A-Za-z-,][A-Za-z0-9-,]*");
    for (String testChemicalName : testChemicalNames) {
      Matcher m = p.matcher(testChemicalName);
      System.out.println(testChemicalName + " ... " + 
        (m.matches() ? "OK" : "NO-MATCH"));
      Assert.assertTrue(m.matches());
    }
  }
  
  @Test
  public void testPorterStemmingEffect() throws Exception {
    String[] testPossibleStemPreservations = new String[] {
      "abdomen",
      "abacavir",
    };
    for (String s : testPossibleStemPreservations) {
      StringReader reader = new StringReader(s);
      WhitespaceTokenizer tokenizer = new WhitespaceTokenizer(Version.LUCENE_40, reader);
      PorterStemFilter f = new PorterStemFilter(tokenizer);
      StringBuilder buf = new StringBuilder();
      int i = 0;
      while (f.incrementToken()) {
        if (i > 0) {
          buf.append(" ");
        }
        CharTermAttribute term = f.getAttribute(CharTermAttribute.class);
        buf.append(new String(term.buffer()));
        i++;
      }
      System.out.println("orig=" + s + ", stemmed=" + buf.toString());
    }
  }
}
