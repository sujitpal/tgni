package com.mycompany.tgni.beans;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Models relation between two TConcept objects.
 */
public class TRelation implements Serializable {

  private static final long serialVersionUID = 6586205899763875754L;

  private Integer fromOid;
  private TRelTypes relType;
  private Integer toOid;
  private Long mrank;
  private Long arank;
  private boolean mstip;
  private Long rmrank;
  private Long rarank;
  
  public Integer getFromOid() {
    return fromOid;
  }

  public void setFromOid(Integer fromImuid) {
    this.fromOid = fromImuid;
  }

  public TRelTypes getRelType() {
    return relType;
  }

  public void setRelType(TRelTypes relType) {
    this.relType = relType;
  }

  public Integer getToOid() {
    return toOid;
  }

  public void setToOid(Integer toImuid) {
    this.toOid = toImuid;
  }

  public Long getMrank() {
    return mrank == null ? 0L : mrank;
  }

  public void setMrank(Long mrank) {
    this.mrank = mrank;
  }

  public Long getArank() {
    return arank == null ? 0L : arank;
  }

  public void setArank(Long arank) {
    this.arank = arank;
  }

  public boolean getMstip() {
    return mstip;
  }
  
  public void setMstip(boolean mstip) {
    this.mstip = mstip;
  }
  
  public Long getRmrank() {
    return rmrank == null ? 0L : rmrank;
  }

  public void setRmrank(Long rmrank) {
    this.rmrank = rmrank;
  }

  public Long getRarank() {
    return rarank == null ? 0L : rarank;
  }

  public void setRarank(Long rarank) {
    this.rarank = rarank;
  }

  public String toString() {
    return ReflectionToStringBuilder.reflectionToString(
      this, ToStringStyle.DEFAULT_STYLE);
  }
}
