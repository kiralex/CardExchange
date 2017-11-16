<%@page contentType="text/html" pageEncoding="UTF-8" language="java" %>

<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<t:userpage>
    This is an empty user page !


      <c:if test="${not empty requestScope.card}">
    +        ID de la carte<br/>
    +
    +        <c:out value="${requestScope.card.getId()}" />
    +    </c:if>
</t:userpage>