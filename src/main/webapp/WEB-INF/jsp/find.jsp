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
    <title>Find Concepts</title>
  </head>
  <body>
    <c:import url="header.jsp"/>
    <hr/>
    <form method="POST" action="/tgni/find.html">
      <table cellspacing="0" cellpadding="0" border="0" width="100%">
        <tr>
          <td align="right" valign="top" width="20%">
            <b>Query/OID:</b>
          </td>
          <td align="left" valign="top" width="80%">
            <input type="text" name="q" value="${q}" size="80"/>
          </td>
        </tr>
        <tr>
          <td colspan="2" align="center">
            <input type="submit" value="Find"/>
          </td>
        </tr>
      </table>
    </form>
    <hr/>
    <c:if test="${not empty error}">
      <font color="red"><b>${error}</b></font><br/>
    </c:if>
    <c:if test="${not empty concepts}">
      <table cellspacing="3" cellpadding="3" border="1" width="100%">
        <tr bgcolor="gray">
          <td><b>OID</b></td>
          <td><b>PName</b></td>
          <td><b>QName</b></td>
          <td><b>Synonyms</b></td>
          <td><b>StyGroup</b></td>
          <td><b>StyCodes</b></td>
          <td><b>MRank</b></td>
          <td><b>ARank</b></td>
          <td><b>TID</b></td>
        </tr>
        <c:forEach items="${concepts}" var="concept">
          <tr>
            <td><a href="/tgni/show.html?q=${concept.oid}">${concept.oid}</a></td>
            <td>${concept.pname}</td>
            <td>${concept.qname}</td>
            <td>
              <c:forEach items="${concept.synonyms}" var="synonym" varStatus="status">
                <c:if test="${status.index gt 0}">, </c:if>
                ${synonym}
              </c:forEach>
            </td>
            <td>${concept.stygrp}</td>
            <td>
              <c:forEach items="${concept.stycodes}" var="stycode" varStatus="status">
                <c:if test="${status.index gt 0}">, </c:if>
                ${stycode.value} (${stycode.key})
              </c:forEach>
            </td>
            <td>${concept.mrank}</td>
            <td>${concept.arank}</td>
            <td>${concept.tid}</td>
          </tr>
        </c:forEach>
      </table>
    </c:if>
    <c:if test="${not empty elapsed}">
      <p align="left">Elapsed time: ${elapsed} ms</p>
    </c:if>
  </body>
</html>