<%@page contentType="text/html" pageEncoding="UTF-8" language="java" %>

<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="user" value="${param.user}" scope="page" />

<t:userpage userName="${user}">
    User: ${user}
    
    <c:if test="${not empty requestScope.card}">
        <c:out value="${requestScope.card.getPixabayPageURL()}" />
        <img src="${requestScope.card.getPixabayImageURL()}" />
    </c:if>

    <c:forEach var = "i" begin = "1" end = "5">
        Item <c:out value = "Itération : ${i}"/><p>
    </c:forEach>

</t:userpage>