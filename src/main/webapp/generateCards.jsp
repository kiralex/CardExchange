<%@ page import="com.derniamepoirier.CardGeneration.PixabayFetcher" %>
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
        <form method="post" action ="/generateCardServlet">
            <div class="form-group row">
                <label for="formControlSelectLang" class="col-sm-2 col-form-label">Séléctionner la langue</label>
                <select class="form-control col-sm-10 text-capitalize" id="formControlSelectLang">
                    <c:forEach items="${ langs }" var="lang">
                        <option class="text-capitalize">${lang.toString()}</option>
                    </c:forEach>
                </select>
            </div>


            <fieldset class="form-group">
                <div class="row">
                    <legend class="col-form-legend col-sm-2">Type(s) d'image</legend>
                    <div class="col-sm-10">
                        <c:forEach items="${ imageTypes }" var="imageType">
                            <div class="form-check">
                                <label class="form-check-label text-capitalize">
                                    <input class="form-check-input" type="radio" name="imageTypeOptions" id="imageType_${imageType}" value="${imageType}">${imageType.toString()}
                                </label>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </fieldset>

            <fieldset class="form-group">
                <div class="row">
                    <legend class="col-form-legend col-sm-2">Orientation</legend>
                    <div class="col-sm-10">
                        <c:forEach items="${ orientations }" var="orientation">
                            <div class="form-check">
                                <label class="form-check-label text-capitalize">
                                    <input class="form-check-input" type="radio" name="orientationOptions" id="orientation_${orientation}" value="${orientation}">${orientation.toString()}
                                </label>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </fieldset>

            <div class="form-group row">
                <label for="formControlSelectLang" class="col-sm-2 col-form-label">Catégories</label>
                <select class="form-control col-sm-10 text-capitalize" id="formControlSelectCategorie">
                    <c:forEach items="${ categorys }" var="category">
                        <option class="text-capitalize">${category.toString()}</option>
                    </c:forEach>
                </select>
            </div>

            <fieldset class="form-group">
                <div class="row">
                    <legend class="col-form-legend col-sm-2">Ordre</legend>
                    <div class="col-sm-10">
                        <c:forEach items="${ orders }" var="order">
                            <div class="form-check">
                                <label class="form-check-label text-capitalize">
                                    <input class="form-check-input" type="radio" name="orderOptions" id="order_${order}" value="${order}">${order.toString()}
                                </label>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </fieldset>

            <fieldset class="form-group">
                <div class="row">
                    <legend class="col-form-legend col-sm-2">Safe search</legend>
                    <div class="col-sm-10">
                        <c:forEach items="${ safeSearch }" var="safeSearch">
                            <div class="form-check">
                                <label class="form-check-label text-capitalize">
                                    <input class="form-check-input" type="radio" name="safeSearchOptions" id="safeSearch_${safeSearch}" value="${safeSearch}">${safeSearch.toString()}
                                </label>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </fieldset>

            <fieldset class="form-group">
                <div class="row">
                    <legend class="col-form-legend col-sm-2">Choix de l'éditeur</legend>
                    <div class="col-sm-10">
                        <c:forEach items="${ editorChoices }" var="editorChoice">
                            <div class="form-check">
                                <label class="form-check-label text-capitalize">
                                    <input class="form-check-input" type="radio" name="editorChoiceOptions" id="editorChoice_${editorChoice}" value="${editorChoice}">${editorChoice.toString()}
                                </label>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </fieldset>
        </form>

        <div class="form-group row">
            <div class="col-sm-10">
                <button type="submit" class="btn btn-primary">Générer les cartes</button>
            </div>
        </div>



    </jsp:body>


</t:userpage>
