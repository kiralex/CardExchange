<%@ page import="com.derniamepoirier.CardGeneration.Card" %>
<%@page contentType="text/html" pageEncoding="UTF-8" language="java" %>

<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<t:userpage>
    <jsp:attribute name="title">
      Acheter des cartes
    </jsp:attribute>


    <jsp:body>
        <h1 class="mb-2">Tirer des cartes</h1>
        <h5>Nombre de points disponibles : <strong>${nbPoints}</strong></h5>
        <h5>Vous pouvez acheter au maximum <strong>${nbCardsMax}</strong> cartes.</h5>


        <c:if test="${nbCardsMax > 0}">
            <form method="GET" class="form-inline" action="">
                <label class="mr-sm-2" for="nbCardsToDraw">Nombre de cartes à acheter</label>
                <select class="custom-select mb-2 mr-sm-2 mb-sm-0" name="nbCardsToDraw" id="nbCardsToDraw">
                    <option selected name="nbCardsToDraw">Choisir une valeur</option>
                    <c:forEach begin="1" end="${nbCardsMax}" var="i">
                        <option value="${i}">${i}</option>
                    </c:forEach>
                </select>

                <button type="submit" class="btn btn-primary">Submit</button>
            </form>
        </c:if>

        <c:if test="${not empty cards}">
            <h4>Cartes achetées : </h4>
            <div class="row mb-3">
                <c:forEach begin="0" end="${fn:length(cards)-1}" var="i">
                    <div class="card col-md-4">
                        <div class="card-header px-0 py-0 my-1 pb-1 text-center font-weight-bold">
                            {${fn:join(cards[i].getTags(), ", ")}}
                        </div>
                        <div class="card-body row text-center align-bottom">
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
            </div>
        </c:if>
    </jsp:body>
</t:userpage>