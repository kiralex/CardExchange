<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ tag description="Overall Page template" pageEncoding="UTF-8"%>
<%@ attribute name="header" fragment="true" %>
<%@ attribute name="footer" fragment="true" %>
<%@ attribute name="title" fragment="true" %>
<!doctype html>
<html lang="fr">
<head>
    <meta charset="utf-8">

    <title>
        <c:choose>
            <c:when test="${not empty title}">
                <jsp:invoke fragment="title"/>
            </c:when>
            <c:otherwise>
                Default title
            </c:otherwise>
        </c:choose>
        </title>
    <style>
        body {
            font-family: sans-serif;
        }
    </style>
</head>
<body>

    <div id="pageheader">
        <c:choose>
            <c:when test="${not empty header}">
                <jsp:invoke fragment="header"/>
            </c:when>
        </c:choose>
    </div>
    <div id="body">
        <jsp:doBody/>
    </div>
    <div id="pagefooter">
        <c:choose>
            <c:when test="${not empty footer}">
                <jsp:invoke fragment="footer"/>
            </c:when>
        </c:choose>
    </div>
</body>
</html>