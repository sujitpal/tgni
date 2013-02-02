<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<html>
  <head>
    <title>Concept View [OID: ${concept.oid}]</title>
  </head>
  <body>
    <c:import url="header.jsp"/>
    <hr/>
    <table cellspacing="3" cellpadding="3" border="1" width="100%">
      <tr>
        <td width="50%" valign="top" align="right"><b>OID:</b></td>
        <td width="50%" valign="top" align="left">${concept.oid}</td>
      </tr>
      <tr>
        <td width="50%" valign="top" align="right"><b>PName:</b></td>
        <td width="50%" valign="top" align="left">${concept.pname}</td>
      </tr>
      <tr>
        <td width="50%" valign="top" align="right"><b>QName:</b></td>
        <td width="50%" valign="top" align="left">${concept.qname}</td>
      </tr>
      <tr>
        <td width="50%" valign="top" align="right"><b>Synonyms</b></td>
        <td width="50%" valign="top" align="left">
          <c:forEach items="${concept.synonyms}" var="synonym" varStatus="status">
            <c:if test="${status.index gt 0}">, </c:if>
            ${synonym}
          </c:forEach>
        </td>
      </tr>
      <tr>
        <td width="50%" valign="top" align="right"><b>StyGroup:</b></td>
        <td width="50%" valign="top" align="left">${concept.stygrp}</td>
      </tr>
      <tr>
        <td width="50%" valign="top" align="right"><b>StyCodes:</b></td>
        <td width="50%" valign="top" align="left">${concept.stycodes}</td>
      </tr>
    </table>
    <table cellspacing="3" cellpadding="3" border="1" width="100%">
      <c:forEach items="${relmap}" var="relmapEntry">
        <tr bgcolor="gray">
          <td width="50%"><b>${relmapEntry.key} (${fn:length(relmapEntry.value)})</b></td>
          <td width="15%"><b>MRank</b></td>
          <td width="15%"><b>ARank</b></td>
          <td width="20%"><b>MStip</b></td>
        </tr>
        <c:forEach items="${relmapEntry.value}" var="relation">
          <c:set var="toOid" value="${relation.toOid}"/>
          <tr>
            <td width="50%">${oidmap[toOid]} (<a href="/tgni/show.html?q=${relation.toOid}">${relation.toOid}</a>)</td>
            <td width="15%">${relation.mrank}</td>
            <td width="15%">${relation.arank}</td>
            <td width="20%">${relation.mstip}</td>
          </tr>
        </c:forEach>
      </c:forEach>
    </table>
    <c:if test="${not empty elapsed}">
      <p align="left">Elapsed time: ${elapsed} ms</p>
    </c:if>
  </body>
</html>