<%@ page import="com.derniamepoirier.CardGeneration.PixabayFetcher" %>
<%@ page import="java.util.logging.Logger" %>
<%@ page import="com.derniamepoirier.CardGeneration.Card" %>
<%@page contentType="text/html" pageEncoding="UTF-8" language="java" %>

<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
request.setAttribute("langs", PixabayFetcher.Lang.values());
    request.setAttribute("imageTypes", PixabayFetcher.ImageType.values());
    request.setAttribute("orientations", PixabayFetcher.Orientation.values());
    request.setAttribute("categorys", PixabayFetcher.Category.values());
    request.setAttribute("orders", PixabayFetcher.Order.values());
    request.setAttribute("safeSearch", PixabayFetcher.SafeSearch.values());
    request.setAttribute("editorChoices", PixabayFetcher.EditorChoice.values());
%>

<t:userpage>
    <jsp:attribute name="title">
      Ajouter des cartes dans la bibliothèque de cartes
    </jsp:attribute>


    <jsp:body>
            <h1>Choisissez les critères pour choisir le type de carte à rajouter</h1>

            <form method="post" action ="/generateCard">

                <%-- Key words --%>
                <div class="form-group row">
                    <label for="inputQuery" class="col-sm-2 col-form-label">Mots clé</label>
                    <div class="col-sm-10">
                        <input type="text" required class="form-control" id="inputQuery" placeholder="Chiens, chats" name="query">
                    </div>
                </div>

                <%-- Nb cards to generate --%>
                <div class="form-group row">
                    <label for="inputNbCards" class="col-sm-2 col-form-label">Nombre de cartes à générer</label>
                    <div class="col-sm-10">
                        <input type="number" class="form-control" id="inputNbCards" name="nbCards" value="10">
                    </div>
                </div>

                <%-- Languages --%>
                <div class="form-group row">
                    <label for="formControlSelectLang" class="col-sm-2 col-form-label">Séléctionner la langue</label>
                    <select class="form-control col-sm-10 text-capitalize" id="formControlSelectLang" name="selectLang">
                        <c:forEach          items="${ langs }" var="lang">
                            <option class="text-capitalize" value="${lang}"
                                    <c:if test = "${lang.getCode() == 'fr'}">
                                        selected
                                    </c:if>
                            >${lang.toString()}</option>
                        </c:forEach>
                    </select>
                </div>


                <%-- Image type --%>
                <fieldset class="form-group">
                    <div class="row">
                        <legend class="col-form-legend col-sm-2">Type(s) d'image</legend>
                        <div class="col-sm-10">
                            <c:forEach items="${ imageTypes }" var="imageType">
                                <div class="form-check">
                                    <label class="form-check-label text-capitalize">
                                        <input class="form-check-input" type="radio" name="imageTypeOptions"
                                               <c:if test="${imageType.toString() == 'all'}">
                                                 checked="true"
                                                </c:if>
                                               id="imageType_${imageType}" value="${imageType}">${imageType.toString()}
                                    </label>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </fieldset>

                <%-- Orientation --%>
                <fieldset class="form-group">
                    <div class="row">
                        <legend class="col-form-legend col-sm-2">Orientation</legend>
                        <div class="col-sm-10">
                            <c:forEach items="${ orientations }" var="orientation">
                                <div class="form-check">
                                    <label class="form-check-label text-capitalize">
                                        <input class="form-check-input" type="radio" name="orientationOptions" id="orientation_${orientation}" value="${orientation}"
                                            <c:if test="${orientation.toString() == 'all'}">
                                                   checked="true"
                                            </c:if>
                                        >${orientation.toString()}
                                    </label>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </fieldset>

                <%-- Category --%>
                <div class="form-group row">
                    <label for="formControlSelectCategory" class="col-sm-2 col-form-label">Catégories</label>
                    <select class="form-control col-sm-10 text-capitalize" id="formControlSelectCategory" name="selectCategory">
                        <c:forEach items="${ categorys }" var="category">
                            <option class="text-capitalize" value="${category}">${category.toString()}</option>
                        </c:forEach>
                    </select>
                </div>

                <%-- Order --%>
                <fieldset class="form-group">
                    <div class="row">
                        <legend class="col-form-legend col-sm-2">Ordre</legend>
                        <div class="col-sm-10">
                            <c:forEach items="${ orders }" var="order">
                                <div class="form-check">
                                    <label class="form-check-label text-capitalize">
                                        <input class="form-check-input" type="radio" name="orderOptions"
                                        <c:if test="${order.toString() == 'popular'}">
                                               checked="true"
                                        </c:if>
                                               id="order_${order}" value="${order}">${order.toString()}
                                    </label>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </fieldset>

                <%-- Safe search --%>
                <fieldset class="form-group">
                    <div class="row">
                        <legend class="col-form-legend col-sm-2">Safe search</legend>
                        <div class="col-sm-10">
                            <c:forEach items="${ safeSearch }" var="safeSearch">
                                <div class="form-check">
                                    <label class="form-check-label text-capitalize">
                                        <input class="form-check-input" type="radio" name="safeSearchOptions"

                                        <c:if test="${safeSearch.toString() == 'true'}">
                                               checked="true"
                                        </c:if>
                                               id="safeSearch_${safeSearch}" value="${safeSearch}">${safeSearch.toString()}
                                    </label>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </fieldset>

                <%-- Editor Choice --%>
                <fieldset class="form-group">
                    <div class="row">
                        <legend class="col-form-legend col-sm-2">Choix de l'éditeur</legend>
                        <div class="col-sm-10">
                            <c:forEach items="${ editorChoices }" var="editorChoice">
                                <div class="form-check">
                                    <label class="form-check-label text-capitalize">
                                        <input class="form-check-input" type="radio" name="editorChoiceOptions"
                                            <c:if test="${editorChoice.toString() == 'false'}">
                                                   checked="true"
                                            </c:if>

                                               id="editorChoice_${editorChoice}" value="${editorChoice}">${editorChoice.toString()}
                                    </label>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </fieldset>

                <div class="form-group row">
                    <div class="col-sm-10">
                        <button type="submit" class="btn btn-primary">Générer les cartes</button>
                    </div>
                </div>
            </form>
    </jsp:body>


</t:userpage>
