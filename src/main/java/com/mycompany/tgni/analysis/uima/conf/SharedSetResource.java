package com.mycompany.tgni.analysis.uima.conf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;

/**
 * Converts the specified text file of property values into a Set.
 * Values must start at the first character of a line and be 
 * terminated by tab or newline.
 */
public class SharedSetResource implements SharedResourceObject {

  private final Set<String> configs = new HashSet<String>();
  
  @Override
  public void load(DataResource res) 
      throws ResourceInitializationException {
    InputStream istream = null;
    try {
      istream = res.getInputStream();
      BufferedReader reader = new BufferedReader(
        new InputStreamReader(istream));
      String line;
      while ((line = reader.readLine()) != null) {
        if (StringUtils.isEmpty(line) || line.startsWith("#")) {
          continue;
        }
        if (line.indexOf('\t') > 0) {
          String[] cols = StringUtils.split(line, "\t");
          configs.add(StringUtils.trim(cols[0]));
        } else {
          configs.add(StringUtils.trim(line));
        }
      }
      reader.close();
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    } finally {
      IOUtils.closeQuietly(istream);
    }
  }
  
  public Set<String> getConfig() {
    return Collections.unmodifiableSet(configs);
  }
}
