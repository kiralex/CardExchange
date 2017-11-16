<%@ page import="com.derniamepoirier.CardGeneration.Card" %>
<%@page contentType="text/html" pageEncoding="UTF-8" language="java" %>

<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<t:userpage>
    <jsp:attribute name="title">
      Mes points
    </jsp:attribute>


    <jsp:body>
        <h1>Mes points</h1>
        <h4>
            Vous avez <span class="badge badge-success">${nbPoints}</span> points.
            <c:choose>
                <c:when test="${nbPoints > 10}">
                    Vous pouvez otenir des cartes <a href="/drawCards" class="btn btn-success btn-sm" >ici</a> si vous le souhaitez.
                </c:when>
                <c:otherwise>
                    Vous n'en avez pas assez pour obtenir une carte. Un peu de patience.
                </c:otherwise>
            </c:choose>
        </h4>
        <c:choose>
            <c:when test="${canEarnPoints == true}">
                <h3>
                    Vous avez débloqué 30 points. Pour les obtenir, <a href="/earnPoints" class="btn btn-success btn-sm">cliquez ici</a>
                </h3>
            </c:when>
            <c:otherwise>
                <h4>
                    Il est encore trop tôt. Vous pourrez obtenir vos points dans <strong id="countdown"></strong>
                    <script>
                        var date = new Date();
                        $('#countdown').countdown(new Date(${nextPointEarnDate}), function(event) {
                            $(this).html(event.strftime('%Hh %Mmin %Ssec'));
                        });
                    </script>
                </h4>
            </c:otherwise>
        </c:choose>
    </jsp:body>


</t:userpage>
