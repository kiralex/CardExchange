<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ tag description="Overall Page template" pageEncoding="UTF-8"%>
<%@ attribute name="header" fragment="true" %>
<%@ attribute name="footer" fragment="true" %>
<%@ attribute name="title" fragment="true" %>
<%@ attribute name="css" fragment="true" %>

<!doctype html>
<html lang="fr">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>
        CardExchange
        <c:if test="${not empty title}">
            - <jsp:invoke fragment="title"/>
        </c:if>
        </title>
    <style>
        <c:choose>
            <c:when test="${not empty css}">
                <jsp:invoke fragment="title"/>
            </c:when>
            <c:otherwise>
                Default title
            </c:otherwise>
        </c:choose>
    </style>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta.2/css/bootstrap.min.css" integrity="sha384-PsH8R72JQ3SOdhVi3uxftmaW6Vc51MKb0q5P2rRUpPvrszuE4W1povHYgTpBfshb" crossorigin="anonymous">
    <script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.3/umd/popper.min.js" integrity="sha384-vFJXuSJphROIrBnz7yo7oB41mKfc8JzQZiCq4NCceLEaO4IHwicKwpJf9c9IpFgh" crossorigin="anonymous"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta.2/js/bootstrap.min.js" integrity="sha384-alpBpkh1PFOepccYVYDB4do5UnbKysX5WZXm3XxPqe5iKTfUKjNkCk9SaVuEZflJ" crossorigin="anonymous"></script>
    <script src="https://use.fontawesome.com/c55fdca20a.js"></script>
</head>
<body>
    <div>
        <c:choose>
            <c:when test="${not empty header}">
                <jsp:invoke fragment="header"/>
            </c:when>
        </c:choose>
    </div>
    <div class="container-fluid">
        <div class="row">
            <div class="col-md-8 offset-md-2 ">
                <jsp:doBody/>
            </div>
        </div>
    </div>
    <div class="row">
        <c:choose>
            <c:when test="${not empty footer}">
                <jsp:invoke fragment="footer"/>
            </c:when>
        </c:choose>
    </div>
</body>
</html>