<%@ page import="com.derniamepoirier.CardGeneration.Card" %>
<%@page contentType="text/html" pageEncoding="UTF-8" language="java" %>

<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<t:userpage>
    <jsp:attribute name="title">
      Mes cartes
    </jsp:attribute>


    <jsp:body>
        <h1 class="mb-2">Mes cartes</h1>

        <div class="row mb-3">
            <c:if test="${not empty cards}">
                <c:set var="keyCard" value="${cards.keySet().toArray()}"></c:set>
                <c:forEach begin="0" end="${fn:length(keyCard)-1}" var="i">

                    <c:set var="card" value="${keyCard[i]}"></c:set>
                    <c:set var="nbInstance" value="${cards.get(keyCard[i])}"></c:set>

                    <div class="card col-md-4">
                        <div class="card-header px-0 py-0 my-1 pb-1 text-center align-middle font-weight-bold" style="height: 3rem;">
                            {${fn:join(card.getTags(), ", ")}}
                        </div>
                        <div class="card-body row text-center px-2 py-2">
                            <div class="col-sm-12 my-auto mb-2">
                                <c:set var="imageURL" value="${card.getCardImageURL().toString()}"></c:set>
                                <c:choose>
                                    <c:when test="${not empty imageURL}">
                                        <img src="${imageURL}" style="object-fit: contain;width: 100%; height: 100%;" />
                                    </c:when>
                                    <c:otherwise>
                                        <img src="${card.getPixabayImageURL()}" style="object-fit: contain;width: 100%; height: 100%;" />
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <h5 class="card-subtitle text-muted col-sm-12 my-auto">#${card.getId()}</h5>
                            <strong class="card-subtitle text-right col-sm-12">x ${nbInstance}</strong>
                        </div>
                    </div>
                </c:forEach>
            </c:if>
        </div>
    </jsp:body>
</t:userpage>
