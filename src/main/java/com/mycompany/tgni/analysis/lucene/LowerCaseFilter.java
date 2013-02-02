//package org.apache.lucene.analysis.core;
package com.mycompany.tgni.analysis.lucene;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.util.CharacterUtils;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Normalizes token text to lower case.
 * <a name="version"/>
 * <p>You must specify the required {@link Version}
 * compatibility when creating LowerCaseFilter:
 * <ul>
 *   <li> As of 3.1, supplementary characters are properly lowercased.
 * </ul>
 */
public final class LowerCaseFilter extends TokenFilter {
  
  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  private final CharacterUtils charUtils;
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);
  
  private boolean ignoreKeyword = false;

  /**
   * Create a new LowerCaseFilter, that normalizes token text to lower case.
   * 
   * @param matchVersion See <a href="#version">above</a>
   * @param in TokenStream to filter
   * @param ignoreKeyword if true, keywords will NOT be lowercased,
   *                      default behavior is false.
   */
  public LowerCaseFilter(Version matchVersion, TokenStream in, boolean ignoreKeyword) {
    super(in);
    charUtils = CharacterUtils.getInstance(matchVersion);
    this.ignoreKeyword = ignoreKeyword;
  }

  /**
   * Create a new LowerCaseFilter, that normalizes token text to lower case.
   * 
   * @param matchVersion See <a href="#version">above</a>
   * @param in TokenStream to filter
   */
  public LowerCaseFilter(Version matchVersion, TokenStream in) {
    this(matchVersion, in, false);
  }
  
  @Override
  public final boolean incrementToken() throws IOException {
    logger.debug("Lowercase filter.incrementToken");
    if (input.incrementToken()) {
      if (ignoreKeyword && keywordAtt.isKeyword()) {
        // do nothing
        return true;
      }
      final char[] buffer = termAtt.buffer();
      final int length = termAtt.length();
      for (int i = 0; i < length;) {
       i += Character.toChars(
               Character.toLowerCase(
                   charUtils.codePointAt(buffer, i)), buffer, i);
      }
      return true;
    } else
      return false;
  }
}
