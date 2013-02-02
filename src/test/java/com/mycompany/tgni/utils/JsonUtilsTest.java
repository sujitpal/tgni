package com.mycompany.tgni.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests to figure out how to use JSON Lib to serialize
 * and deserialize a List and Map.
 */
public class JsonUtilsTest {

  private static final Logger logger = 
    LoggerFactory.getLogger(JsonUtilsTest.class);
  
  @Test
  public void testMapToString() throws Exception {
    Map<String,String> map = new HashMap<String,String>();
    map.put("ST1", "Larry");
    map.put("ST2", "Curly");
    map.put("ST3", "{Moe}");
    String s = JsonUtils.mapToString(map);
    logger.debug("str=" + s);
    Assert.assertTrue(s.startsWith("{") && s.endsWith("}"));
  }
  
  @Test
  public void testStringToMap() throws Exception {
    String s = "{\"ST2\":\"Curly\",\"ST1\":\"Larry\",\"ST3\":\"Moe\"}";
    Map<String,String> map = JsonUtils.stringToMap(s);
    logger.debug("map=" + map);
    Assert.assertEquals(3, map.size());
    Assert.assertTrue(map.containsKey("ST1"));
  }
  
  @Test
  public void testListToString() throws Exception {
    List<String> list = new ArrayList<String>();
    list.add("Larry");
    list.add("Curly");
    list.add("{Moe}");
    String s = JsonUtils.listToString(list);
    logger.debug("str=" + s);
    Assert.assertEquals("[\"Larry\",\"Curly\",\"Moe\"]", s);
  }
  
  @Test
  public void testStringToList() throws Exception {
    String s = "[\"Larry\",\"Curly\",\"Moe\"]";
    List<String> list = JsonUtils.stringToList(s);
    logger.debug("list=" + list);
    Assert.assertEquals(3, list.size());
  }
}
