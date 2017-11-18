<%@ tag import="com.google.appengine.api.users.UserService" %>
<%@ tag import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ tag import="java.util.logging.Logger" %>
<%@ tag import="com.derniamepoirier.CardGeneration.Card" %>
<%@ tag description="User Page template" pageEncoding="UTF-8"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="title" fragment="true" %>

<%
    UserService userService =
            UserServiceFactory.getUserService();
    boolean isConnected = userService.isUserLoggedIn();

    boolean isAdmin = false;

    request.setAttribute("isConnected", isConnected);
    if(isConnected) {
        isAdmin = userService.isUserAdmin();
        request.setAttribute("logoutURL", userService.createLogoutURL(request.getRequestURI()));
        request.setAttribute("userEmail", userService.getCurrentUser().getEmail());
    }
    else
        request.setAttribute("loginURL", userService.createLoginURL(request.getRequestURI()));

    request.setAttribute("isAdmin", isAdmin);
%>


<t:genericPage>
    <jsp:attribute name="title">
        <jsp:invoke fragment="title"/>
    </jsp:attribute>

    <jsp:attribute name="header">
        <nav class="navbar navbar-expand-sm navbar-dark bg-dark mb-3 ">
            <a class="navbar-brand" style="color: #ff5c5c;" href="">
                <img src="/logo.svg" width="30" height="30" class="d-inline-block align-top" alt="">
                <span class="mx-0 my-0 px-0 py-0" style="font-family: Yellowtail; line-height: normal; font-size:1.55rem; ">Card Exchange</span>
            </a>
            <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarSupportedContent">
                <ul class="navbar-nav mr-auto" >
                    <li class="nav-item">
                        <a class="nav-link active" href="/"><i class="fa fa-home" aria-hidden="true"></i> Accueil</a>
                    </li>
                    <li class="nav-item active">
                        <a class="nav-link" href="allCards">Toutes les cartes</a>
                    </li>
                    
                    <c:if test="${isConnected}" >
                        <li class="nav-item active">
                            <a class="nav-link" href="myPoints">Mes points</a>
                        </li>
                    </c:if>

                    <c:if test="${isConnected}" >
                        <li class="nav-item active">
                            <a class="nav-link" href="drawCards">Tirer des cartes</a>
                        </li>
                    </c:if>

                    <c:if test="${isConnected}" >
                        <li class="nav-item active">
                            <a class="nav-link" href="myCards">Mes cartes</a>
                        </li>
                    </c:if>

                    <c:if test="${isAdmin}">
                        <li class="nav-item active">
                            <a class="nav-link" href="generateCardsForm.jsp">Générer des Cartes</a>
                        </li>
                    </c:if>

                </ul>

                <%--<form class="form-inline mr-sm-3">--%>
                    <%--<input class="form-control mr-sm-2" type="search" placeholder="Search" aria-label="Search">--%>
                    <%--<button class="btn btn-secondary my-2 my-sm-0" type="submit">Search</button>--%>
                <%--</form>--%>

                <form class="form-inline">

                    <c:choose>
                        <c:when test="${isConnected}">
                            <span class="navbar-text mr-2">Utilisateur : ${userEmail}</span>
                            <a class="btn align-middle btn-danger" href="${logoutURL}" >Déconnexion</a>
                        </c:when>
                        <c:otherwise>
                            <span class="navbar-text mr-2">Non connecté</span>
                            <a class="btn align-middle btn-success" href="${loginURL}" >Connexion</a>
                        </c:otherwise>
                    </c:choose>
                </form>
            </div>
        </nav>
    </jsp:attribute>
    <jsp:body>
        <div>
            <jsp:doBody/>
        </div>
    </jsp:body>
</t:genericPage>