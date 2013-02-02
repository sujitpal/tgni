<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<style type="text/css">
#tabs ul {
  list-style: none;
  padding:0;
  margin:0;
}
#tabs li {
  float: left;
  border: 1px solid;
  border-bottom-width: 0;
  margin: 0 0.5em 0 0;
}
#tabs li a {
  padding: 0 1em;
}    
#tabs #selected {
  position: relative;
  top: 1px;
  background: white;
}
</style>
<table cellspacing="0" cellpadding="0" border="0" width="100%">
  <tr>
    <td width="90%" align="center"><font face="Arial Black" size="5">Taxonomy Genie</font></td>
    <td width="10%"><img src="images/Genie.jpg"/></td>
  </tr>
</table>
<div id="tabs"> 
  <ul>
    <c:choose>
      <c:when test="${operation eq 'map'}">
        <li id="selected"><a href="/tgni/map.html">Map Concepts</a></li>
      </c:when>
      <c:otherwise>
        <li><a href="/tgni/map.html">Map Concepts</a></li>
      </c:otherwise>
    </c:choose>
    <c:choose>
      <c:when test="${operation eq 'find'}">
        <li id="selected"><a href="/tgni/find.html">Find Concepts</a></li>
      </c:when>
      <c:otherwise>
        <li><a href="/tgni/find.html">Find Concepts</a></li>
      </c:otherwise>
    </c:choose>
  </ul>
</div><br/>