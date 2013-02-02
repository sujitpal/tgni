package com.mycompany.tgni.utils;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A simple data structure that behaves like a StringBuilder,
 * except you can add inserts into it at specific character
 * positions (0-based) containing strings to be inserted at 
 * these positions. The toString() method will merge the inserts
 * and the string and return the full buffer.
 */
public class InsertableStringBuilder {

  private StringBuilder buf;
  private SortedMap<Integer,String> inserts;
  
  /**
   * Default ctor.
   */
  public InsertableStringBuilder() {
    this.buf = new StringBuilder();
    this.inserts = new TreeMap<Integer,String>();
  }

  /**
   * Ctor to instantiate this object with an input String.
   * @param s the input String to append.
   */
  public InsertableStringBuilder(String s) {
    this();
    this.buf.append(s);
  }

  /**
   * Similar to StringBuilder.append(String). Allows appending
   * strings to the main input String.
   * @param s the String to append.
   */
  public void append(String s) {
    this.buf.append(s);
  }
  
  /**
   * Add an insert string at the specified position. If
   * an attempt is made to insert past the end of the current
   * input String an ArrayIndexOutOfBoundsException will be
   * thrown. If an insert already exists at the requested 
   * position, the replace parameter controls whether the
   * new insert overwrites the older one or is ignored. 
   * @param pos the position to insert into.
   * @param s the insert string.
   * @param replace if true, older insert at this position,
   *        if present, will be replaced by this newer one.
   */
  public void insert(int pos, String s, boolean replace) {
    if (pos > buf.length()) {
      throw new IndexOutOfBoundsException(
        "Can't insert past end of string (pos=" + 
        pos + ", len=" + buf.length() + ")");
    }
    if (! replace) {
      if (! this.inserts.containsKey(pos)) {
        this.inserts.put(pos, s);
      }
    } else {
      this.inserts.put(pos, s);
    }
  }
  
  /**
   * Merges the input String and all the insert Strings
   * to create the merged string.
   * @return the merged string.
   */
  @Override
  public String toString() {
    StringBuilder obuf = new StringBuilder();
    int pos = 0;
    for (int ipos : inserts.keySet()) {
      if (pos < ipos) {
        obuf.append(buf.subSequence(pos, ipos));
      }
      obuf.append(inserts.get(ipos));
      pos = ipos;
    }
    if (pos < buf.length()) {
      obuf.append(buf.subSequence(pos, buf.length()));
    }
    return obuf.toString();
  }
}
