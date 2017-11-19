<%@ page import="com.derniamepoirier.User.UserManagment" %>
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
        
        <c:if test="${earnPoints == true}">
            <div class="alert alert-success" role="alert">
                Vous avez gagné ${UserManagment.NB_POINTS_PER_PERIOD} points !
            </div>
        </c:if>

        <h4>
            Vous avez <span class="badge badge-success">${nbPoints}</span> points.
            <c:choose>
                <c:when test="${nbPoints > 10}">
                    Vous pouvez obtenir des cartes <a href="/drawCards" class="btn btn-success btn-sm" >ici</a> si vous le souhaitez.
                </c:when>
                <c:otherwise>
                    Vous n'en avez pas assez pour obtenir une carte. Un peu de patience.
                </c:otherwise>
            </c:choose>
        </h4>
        <c:choose>
            <c:when test="${canEarnPoints == true}">
                <h5>
                    Vous avez débloqué ${UserManagment.NB_POINTS_PER_PERIOD} points. Pour les obtenir, <a href="/earnPoints" class="btn btn-success btn-sm">cliquez ici</a>
                </h5>
            </c:when>
            <c:otherwise>
                <h5>
                    Il est encore trop tôt. Vous pourrez obtenir vos points dans <strong id="countdown"></strong>
                    <script>
                        var date = new Date();
                        $('#countdown').countdown(new Date(${nextPointEarnDate}), function(event) {
                            $(this).html(event.strftime('%Hh %Mmin %Ssec'));
                        });
                    </script>

                </h5>
            </c:otherwise>
        </c:choose>

        <h5>
            <c:if test="${not empty admin}">
                Comme vous êtes administrateur, vous pouvez vous ajouter des points manuellement.
                <form class="form-inline" method="GET" action="earnPoints">
                    <label for="nbPoints">Nombre de points :</label>
                    <input type="number" class="form-control ml-2" min="0" id="nbPoints" name="nbPoints"/>
                    <input type="submit" class="btn btn-primary ml-2"></input>
                </form>
            </c:if>
        </h5>
    </jsp:body>


</t:userpage>
