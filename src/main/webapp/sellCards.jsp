<%@ page import="com.derniamepoirier.CardGeneration.Card" %>
<%@page contentType="text/html" pageEncoding="UTF-8" language="java" %>

<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<t:userpage>
    <jsp:attribute name="title">
        Vendre des cartes
    </jsp:attribute>

    <jsp:body>

        <c:if test="${not empty sellOK}">
            <div class="alert alert-success" role="alert">
                <span class="alert-heading"><span class="badge badge-success">Succès !</span></span>
                <h6>Vos cartes ont bien été vendues. Vous avez gagné <strong>${sellOK} points</strong>.</h6>
            </div>
        </c:if>

        <h1 class="mb-2">Vendre des cartes</h1>
        <h5>Veuillez sélectionner le nombre de cartes à vendre. Chaque étoile sur une carte vous rapportera 1 points.</h5>

        <form method="GET" action="" class="form-inline">
            <c:if test="${not empty cards}">
                <div class="row mb-3">
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
                            <div class="card-subtitle form-group col-sm-12 mt-1">
                                <label for="card-${card.getId()}" class="font-weight-bold" >Quantité : </label>
                                <input type="number" value=0 min=0 max=${nbInstance} class="form-control ml-2" name="card-${card.getId()}" id="card-${card.getId()}"/>
                                <span class="ml-1">sur ${nbInstance}</span>
                            </div>
                        </div>
                    </div>
                </c:forEach>

                </div>
                <div class="form-group row">
                    <button type="submit" class="form-control btn btn-primary">Valider</button>
                </div>
            </c:if>
        </form>
    </jsp:body>
</t:userpage>