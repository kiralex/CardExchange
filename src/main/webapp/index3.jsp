<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="user" value="${param.user}" scope="page" />

<t:userpage userName="${user}">
    User: ${user}
    
    <c:if test="${not empty requestScope.card}">
        toto la mer ! <br/>

        <c:out value="${requestScope.card.getPixabayPageURL()}" />
    </c:if>

</t:userpage>