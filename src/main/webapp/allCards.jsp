<%@ page import="com.derniamepoirier.CardGeneration.Card" %>
<%@page contentType="text/html" pageEncoding="UTF-8" language="java" %>

<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<t:userpage>
    <jsp:attribute name="title">
      Toutes les cartes
    </jsp:attribute>


    <jsp:body>
        <h1 class="mb-2">Liste des cartes</h1>

        <c:if test="${not empty param.page && param.page>nbPages}">
            <div class="alert alert-danger" role="alert">
                <h4 class="alert-heading"><span class="badge badge-danger">Numéro de page incorrrect</span></h4>
                <p>Opps ! Le numéro de page est trop grand !</p>
            </div>
        </c:if>

        <div class="row mb-3">
            <c:if test="${not empty cards}">
                <c:forEach begin="0" end="${fn:length(cards)-1}" var="i">
                    <div class="card col-md-3">
                        <div class="card-header px-0 py-0 my-1 pb-1 text-center font-weight-bold">
                            {${fn:join(cards[i].getTags(), ", ")}}
                        </div>
                        <div class="card-body row text-center">
                            <div class="col-sm-12 my-auto mb-2">
                                <c:set var="imageURL" value="${cards[i].getCardImageURL().toString()}"></c:set>
                                <c:choose>
                                    <c:when test="${not empty imageURL}">
                                        <img src="${imageURL}" style="object-fit: contain;width: 100%; height: 100%;" />
                                    </c:when>
                                    <c:otherwise>
                                        <img src="${cards[i].getPixabayImageURL()}" style="object-fit: contain;width: 100%; height: 100%;" />
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <h5 class="card-subtitle text-muted col-sm-12 my-auto">#${cards[i].getId()}</h5>
                        </div>
                    </div>
                </c:forEach>
            </c:if>
        </div>

        <c:set var="req" value="${pageContext.request}" />
        <c:set var="requestPath" value="${requestScope['javax.servlet.forward.request_uri']}"/>
        <c:set var="baseURL" value="${fn:replace(req.requestURL, req.requestURI, '')}" />
        <c:set var="pageUrlNoParam" value="${ baseURL }${ requestPath }${ not empty params?'?'+=params:'' }"/>

        <div class="row">
            <ul class="pagination mx-auto">
                <li class="page-item">
                    <c:choose>
                        <c:when test="${page <=1}">
                            <a class="page-link disabled" href="#" aria-label="Précédent">
                                <span aria-hidden="true">&laquo;</span>
                                <span class="sr-only">Précédent</span>
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a class="page-link" href="${pageUrlNoParam}?page=${page-1}" aria-label="Previous">
                                <span aria-hidden="true">&laquo;</span>
                                <span class="sr-only">Previous</span>
                            </a>
                        </c:otherwise>
                    </c:choose>
                </li>

                <c:forEach begin="1" end="${nbPages}" var="i">
                    <li class="page-item
                    <c:if test="${page == i}">
                         active
                    </c:if>"
                    >
                        <a class="page-link" href="${pageUrlNoParam}?page=${i}">${i}</a>
                    </li>
                </c:forEach>


                <li class="page-item">
                    <c:choose>
                        <c:when test="${page >= nbPages}">
                            <a class="page-link disabled" href="#" aria-label="Suivant">
                                <span aria-hidden="true">&raquo;</span>
                                <span class="sr-only">Next</span>
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a class="page-link" href="${pageUrlNoParam}?page=${page+1}" aria-label="Suivant">
                                <span aria-hidden="true">&raquo;</span>
                                <span class="sr-only">Suivant</span>
                            </a>
                        </c:otherwise>
                    </c:choose>
                </li>
            </ul>
        </div>
    </jsp:body>


</t:userpage>
