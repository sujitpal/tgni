package com.mycompany.tgni.services;

import java.util.List;

import org.apache.commons.collections15.Bag;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.Query;

import com.mycompany.tgni.beans.TConcept;
import com.mycompany.tgni.beans.TRelTypes;
import com.mycompany.tgni.beans.TRelation;
import com.mycompany.tgni.utils.DbConnectionPool;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * Provides convenience methods to put and get objects from
 * an embedded EHCache cache.
 */
class CacheService {

  private static final String CACHE_NAME = "node-cache";
  
  private String cacheDescriptor;
  private DbConnectionPool mysqlPool;
  
  private CacheManager cacheManager;
  private Cache cache;
  
  public void setCacheDescriptor(String cacheDescriptor) {
    this.cacheDescriptor = cacheDescriptor;
  }
  
  public void setMySqlPool(DbConnectionPool mysqlPool) {
    this.mysqlPool = mysqlPool;
  }
  
  protected void init() throws Exception {
    this.cacheManager = CacheManager.create(cacheDescriptor);
    this.cache = this.cacheManager.getCache(CACHE_NAME);
  }
  
  protected void destroy() throws Exception {
    this.cache.flush();
    this.cacheManager.shutdown();
  }

  public TConcept getConcept(Integer oid) {
    Element e = cache.get(getConceptKey(oid));
    if (e == null) {
      return null;
    }
    return (TConcept) e.getValue();
  }

  public void putConcept(Integer oid, TConcept concept) {
    Element e = new Element(getConceptKey(oid), concept);
    cache.put(e);
  }
  
  private String getConceptKey(Integer oid) {
    return StringUtils.join(new String[] {
      "concept",
      String.valueOf(oid)
    }, ":");
  }

  @SuppressWarnings("unchecked")
  public List<TConcept> getConcepts(String name) {
    Element e = cache.get(getConceptsByNameKey(name));
    if (e == null) {
      return null;
    }
    return (List<TConcept>) e.getValue();
  }

  public void putConcepts(String name, List<TConcept> concepts) {
    Element e = new Element(
      getConceptsByNameKey(name), concepts);
    cache.put(e);
  }
  
  private String getConceptsByNameKey(String name) {
    return StringUtils.join(new String[] {
      "concepts_name",
      StringUtils.replace(name, " ", "_"),
    }, ":");
  }

  @SuppressWarnings("unchecked")
  public List<TConcept> getConcepts(Query query, int maxDocs, 
      float minScore) {
    Element e = cache.get(getConceptsByQueryKey(query, maxDocs, minScore));
    if (e == null) {
      return null;
    }
    return (List<TConcept>) e.getValue();
  }

  public void putConcepts(Query query, int maxDocs, float minScore,
      List<TConcept> concepts) {
    Element e = new Element(
      getConceptsByQueryKey(query, maxDocs, minScore), concepts);
    cache.put(e);
  }
  
  private String getConceptsByQueryKey(Query query, int maxDocs, 
      float minScore) {
    return StringUtils.join(new String[] {
      "concepts_query",
      StringUtils.replace(query.toString(), " ", "_"),
      String.valueOf(maxDocs),
      String.valueOf(minScore)
    }, ":");
  }

  @SuppressWarnings("unchecked")
  public Bag<TRelTypes> getRelationCounts(TConcept concept) {
    Element e = cache.get(getRelationCountsKey(concept));
    if (e == null) {
      return null;
    }
    return (Bag<TRelTypes>) e.getValue();
  }

  public void putRelationCounts(TConcept concept, Bag<TRelTypes> counts) {
    Element e = new Element(getRelationCountsKey(concept), counts);
    cache.put(e);
  }

  private String getRelationCountsKey(TConcept concept) {
    return StringUtils.join(new String[] {
      "rel_counts",
      String.valueOf(concept.getOid())
    }, ":");
  }
  
  @SuppressWarnings("unchecked")
  public List<TRelation> getRelatedConcepts(TConcept concept, TRelTypes type) {
    Element e = cache.get(getRelatedConceptsKey(concept, type));
    if (e == null) {
      return null;
    }
    return (List<TRelation>) e.getValue();
  }

  public void putRelatedConcepts(TConcept concept, TRelTypes type,
      List<TRelation> rels) {
    Element e = new Element(getRelatedConceptsKey(concept, type), rels);
    cache.put(e);
  }
  
  private String getRelatedConceptsKey(TConcept concept, TRelTypes type) {
    return StringUtils.join(new String[] {
      "related",
      String.valueOf(concept.getOid()),
      String.valueOf(type.oid)
    }, ":");
  }
}
