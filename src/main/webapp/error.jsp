<%@page contentType="text/html" pageEncoding="UTF-8" language="java" %>

<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<t:userpage>
    <jsp:attribute name="title">
      Erreur
    </jsp:attribute>

    <jsp:body>
        <div class="alert alert-danger" role="alert">
            <span class="alert-heading"><span class="badge badge-danger">Erreur</span></span>
            <h4>Opps ! Une erreur a eu lieu !
            <h6>${requestScope.errorMessage}</h6>
        </div>
    </jsp:body>
</t:userpage>