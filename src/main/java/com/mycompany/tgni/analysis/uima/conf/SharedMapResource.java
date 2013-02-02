package com.mycompany.tgni.analysis.uima.conf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;

/**
 * Converts the specified properties file into a Map. Key and
 * value must be tab separated.
 */
public class SharedMapResource implements SharedResourceObject {

  private Map<String,String> configs = new HashMap<String,String>();
  
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
        if (StringUtils.isEmpty(line) ||
            line.startsWith("#")) {
          continue;
        }
        String[] kv = StringUtils.split(line, "\t");
        configs.put(kv[0], kv[1]);
      }
      reader.close();
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    } finally {
      IOUtils.closeQuietly(istream);
    }
  }
  
  public Map<String,String> getConfig() {
    return Collections.unmodifiableMap(configs);
  }
  
  public List<String> asList(String value) {
    if (value == null) {
      return Collections.emptyList();
    } else {
      String[] vals = value.split("\\s*,\\s*");
      return Arrays.asList(vals);
    }
  }
}
