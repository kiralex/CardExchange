<%@page contentType="text/html" pageEncoding="UTF-8" language="java" %>

<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>

<c:set var="cards" value="${requestScope.cards}" scope="page" />

<t:userpage>
    <%-- Success card generate --%>
    <h1 class="mb-2">Cartes générées</h1>
    <c:choose>
        <c:when test="${!empty requestScope.nbCards && requestScope.nbCards > 0}" >
            <h5>Vos cartes ont été créées. Les images des cartes vont être générées à partir des image Pixabay. Lorsqu'elles seront prêtes, les images de cartes générées s'afficheront à la place des images Pixabay.</h5>

            <div class="row mb-3">
                <c:if test="${not empty cards}">
                    <c:forEach begin="0" end="${fn:length(cards)-1}" var="i">
                        <div class="card col-md-3">
                            <div class="card-header px-0 py-0 my-1 pb-1 text-center font-weight-bold">
                                {${fn:join(cards[i].getTags(), ", ")}}
                            </div>
                            <div class="card-body row text-center align-bottom">
                                <div class="col-sm-12 my-auto mb-2">
                                    <img src="${cards[i].getPixabayImageURL()}" style="object-fit: contain;width: 100%; height: 100%;"
                                    />
                                </div>
                                <h5 class="card-subtitle text-muted col-sm-12 my-auto">#${cards[i].getId()}</h5>
                            </div>
                        </div>
                    </c:forEach>
                </c:if>
            </div>
        </c:when>
        <c:otherwise>
            <h5>Malheureusement aucune image de Pixabay ne correspond à vos critères. Essayez de changer les paramètres de la génération d'image.</h5>
        </c:otherwise>
    </c:choose>


</t:userpage>