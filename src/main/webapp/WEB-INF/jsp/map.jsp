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
    <title>Map String or Text</title>
  </head>
  <body>
    <c:import url="header.jsp"/>
    <hr/>
    <form method="POST" action="/tgni/map.html">
      <table cellspacing="0" cellpadding="0" border="0" width="100%">
        <tr>
          <td align="right" valign="top" width="20%">
            <b>Query/OID:</b>
          </td>
          <td align="left" valign="top" width="80%">
            <input type="text" name="q1" value="${q1}" size="80"/>
          </td>
        </tr>
        <tr>
          <td align="right" valign="top" width="20%"><b>or URL:</b></td>
          <td align="left" valign="top" width="70%">
            <input type="text" name="q2" value="${q2}" size="80"/>
          </td>
        </tr>
        <tr>
          <td align="right" valign="top" width="20%">
            <b>or Text:</b><br/>
            <font size="-2">(Copy-Paste or enter text to concept-map here)</font>
          </td>
          <td align="left" valign="top" width="80%">
            <textarea name="q3" rows=10" cols="80">${q3}</textarea><br/>
          </td>
        </tr>
        <tr>
          <td align="right" valign="top" width="20%">
            <b>Input-Format:</b>
          </td>
          <td align="left" valign="top" width="80%">
            <input type="radio" name="if" value="string/plain" checked>String</input>&nbsp;
            <input type="radio" name="if" value="text/html">HTML-Text</input>&nbsp;
            <input type="radio" name="if" value="text/plain">Plain-Text</input>
          </td>
        </tr>
        <tr>
          <td align="right" valign="top" width="20%">
            <b>Output-Format:</b>
          </td>
          <td align="left" valign="top" width="100%">
            <input type="radio" name="of" value="html" checked>HTML</input>
            <input type="radio" name="of" value="xml">XML</input>
            <input type="radio" name="of" value="json">JSON</input>
            <input type="radio" name="of" value="jsonp">JSON-P</input>
          </td>
        </tr>
        <tr>
          <td colspan="2" align="center">
            <input type="submit" value="Map"/>
          </td>
        </tr>
      </table>
    </form>
    <hr/>

    <c:if test="${not empty error}">
      <font color="red"><b>${error}</b></font><br/>
    </c:if>
    <c:if test="${not empty annotations}">
      <table cellspacing="3" cellpadding="3" border="1" width="100%">
        <tr bgcolor="gray">
          <td><b>Start</b></td>
          <td><b>End</b></td>
          <td><b>Matched</b></td>
          <td><b>OID</b></td>
          <td><b>PName</b></td>
          <td><b>StyGroup</b></td>
          <td><b>StyCodes</b></td>
        </tr>
        <c:forEach items="${annotations}" var="annotation">
          <tr>
            <td>${annotation.begin}</td>
            <td>${annotation.end}</td>
            <td>${annotation.coveredText}</td>
            <td><a href="/tgni/show.html?q=${annotation.oid}">${annotation.oid}</a></td>
            <td>${annotation.pname}</td>
            <td>${annotation.stygroup}</td>
            <td>${annotation.stycodes}</td>
          </tr>
        </c:forEach>
      </table>      
    </c:if>
    <c:if test="${not empty elapsed}">
      <p align="left">Elapsed Time: ${elapsed} ms</p>
    </c:if>
  </body>
</html>
