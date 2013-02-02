package com.mycompany.tgni.beans;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Models single concept.
 */
public class TConcept implements Serializable {

  private static final long serialVersionUID = 9090069476479965620L;

  private Integer oid;
  private String pname;
  private String qname;
  private List<String> synonyms;
  private Map<String,String> stycodes;
  private String stygrp;
  private Long mrank;
  private Long arank;
  private Integer tid;
  private Long nid;
  
  public Integer getOid() {
    return oid;
  }

  public void setOid(Integer oid) {
    this.oid = oid;
  }

  public String getPname() {
    return pname;
  }

  public void setPname(String pname) {
    this.pname = pname;
  }

  public String getQname() {
    return qname;
  }

  public void setQname(String qname) {
    this.qname = qname;
  }

  public List<String> getSynonyms() {
    return synonyms;
  }

  public void setSynonyms(List<String> synonyms) {
    this.synonyms = synonyms;
  }

  public Map<String,String> getStycodes() {
    return stycodes;
  }

  public void setStycodes(Map<String,String> stycodes) {
    this.stycodes = stycodes;
  }

  public String getStygrp() {
    return stygrp;
  }

  public void setStygrp(String stygrp) {
    this.stygrp = stygrp;
  }

  public Long getMrank() {
    return mrank;
  }

  public void setMrank(Long mrank) {
    this.mrank = mrank;
  }

  public Long getArank() {
    return arank;
  }

  public void setArank(Long arank) {
    this.arank = arank;
  }

  public Integer getTid() {
    return tid;
  }

  public void setTid(Integer tid) {
    this.tid = tid;
  }

  public Long getNid() {
    return nid;
  }
  
  public void setNid(Long nid) {
    this.nid = nid;
  }
  
  public String toString() {
    return ReflectionToStringBuilder.reflectionToString(
      this, ToStringStyle.DEFAULT_STYLE);
  }
}
