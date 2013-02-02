package com.mycompany.tgni.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections15.Closure;
import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.StringUtils;

/**
 * Provides convenience classes for converting collection
 * objects (stored as Node properties) to and fro from
 * Strings.
 */
public class JsonUtils {

  public static List<String> stringToList(String s) {
    JSONArray json = new JSONArray(s);
    List<String> l = new ArrayList<String>();
    for (Iterator<String> it = json.iterator(); it.hasNext(); ) {
      l.add(it.next());
    }
    return l;
  }
  
  public static String listToString(List<String> l) {
    CollectionUtils.transform(l, new Transformer<String,String>() {
      @Override
      public String transform(String s) {
        return escapeForJson(s);
      }
    });
    JSONArray json = JSONArray.fromObject(l);
    return json.toString();
  }
  
  public static Map<String,String> stringToMap(String s) {
    Map<String,String> m = new HashMap<String,String>();
    JSONObject json = new JSONObject(s);
    for (Iterator<String> it = json.keys(); it.hasNext(); ) {
      String key = it.next();
      String value = json.getString(key);
      m.put(key, value);
    }
    return m;
  }
  
  public static String mapToString(final Map<String,String> m) {
    final Map<String,String> cm = new HashMap<String,String>();
    CollectionUtils.forAllDo(m.keySet(), new Closure<String>() {
      @Override
      public void execute(String key) {
        String value = escapeForJson(m.get(key));
        String ckey = escapeForJson(key);
        cm.put(ckey, value);
      }
    });
    JSONObject json = JSONObject.fromObject(cm);
    return json.toString();
  }
  
  private static Pattern JSON_ESCAPE_REMOVE_PATTERN = 
    Pattern.compile("[\\[\\]{}]");
  
  private static String escapeForJson(String s) {
    if (StringUtils.isNotEmpty(s)) {
      Matcher m = JSON_ESCAPE_REMOVE_PATTERN.matcher(s);
      s = m.replaceAll("");
    }
    return s;
  }
}
