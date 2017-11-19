<%@page contentType="text/html" pageEncoding="UTF-8" language="java" %>

<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<t:userpage>
    <jsp:attribute name="title">
      Acceuil
    </jsp:attribute>
    <jsp:body>
        <h1 class="mb-3">Bienvenue sur le nouveau jeu de collection et d'échange de cartes :
            <span class="mx-0 my-0 px-0 py-0" style="color: #ff5c5c; font-family: Yellowtail; line-height: normal; font-size:150%;">
                Card Exchange
            </span>
        </h1>

        <div class = "text-justify">
            <p>Nous sommes deux étudiants en dernière année de Master Informatique à Reims, parcours Développement d'applicaitons Réparties (DAR).</p>
            <p></p>

            <p>Nous avons développé
                <a href="/">
                    <span class="mx-0 my-0 px-0 py-0" style="color: #ff5c5c; font-family: Yellowtail; line-height: normal;">
                        Card Exchange
                    </span>
                </a>
                dans le cadre du projet du module INFO0927 <span class="font-italic">Programmation pour le Cloud Computing</span>.
            </p>

            <p>Le but de ce projet est la  réalisation d'une application <span class="font-italic">Google App Engine</span> sur le thème <span class="font-italic">Cartes à collectionner</span>, dans lequel nous devions utiliser différentes API proposées par la plateforme <span class="font-italic">Google App Engine</span>
            </p>

            <p >
                Le but de ce jeu est de collectionner toutes les cartes existantes, pour cela vous pouvez :
            <ul class="list-group">
                <li class="list-group-item">tirer de nouvelles cartes chaque jour</li>
                <li class="list-group-item">vendre à la boutique pour en tirer encore plus chaque jour</>
            </ul>
            Bientôt vous pourrez même en échangez avec vos amis et avec le monde entier.
            Régulièrement de nouvelles cartes seront créées.
            </p>
        </div>




        <div class="mt-5">
            <p class="font-weight-bold">Mots clés :
                <a href="/">
                    <span class="mx-0 my-0 px-0 py-0" style="color: #ff5c5c; font-family: Yellowtail; line-height: normal;">
                    Card Exchange
                    </span>
                </a>,
                <a href="http://www.cyril-rabat.fr/enseignement/Info0927/">INFO0927</a>,
                <a href="https://cloud.google.com/appengine/">Google App Engine</a>,
                <a href="https://www.jetbrains.com/idea/">IntelliJ IDEA</a>,
                <a href="https://maven.apache.org/">Maven</a>,
                <a href="http://json.org/">JSON.org</a>,
                <a href="https://github.com/Harium/dotenv">DotEnv/</a>
            </p>
        </div>
    </jsp:body>
</t:userpage>