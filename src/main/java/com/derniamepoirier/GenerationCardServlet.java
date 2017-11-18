package com.derniamepoirier;

import com.derniamepoirier.CardGeneration.Card;
import com.derniamepoirier.CardGeneration.CardGenerator;
import com.derniamepoirier.CardGeneration.PixabayAPIExceptions;
import com.derniamepoirier.CardGeneration.PixabayFetcher;
import com.derniamepoirier.Utils.DatastoreGetter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "GenerationCardServlet", value = "/generateCard")
public class GenerationCardServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String query = request.getParameter("query");
        int nbCards = 1;
        String lang = request.getParameter("selectLang");
        String imageType = request.getParameter("imageTypeOptions");
        String orientation = request.getParameter("orientationOptions");
        String category = request.getParameter("selectCategory");
        String order = request.getParameter("orderOptions");
        String editorChoice = request.getParameter("editorChoiceOptions");

        ArrayList<PixabayFetcher.PixabayAPIOptions> options = new ArrayList<PixabayFetcher.PixabayAPIOptions>();


        try {
            nbCards = Integer.valueOf(request.getParameter("nbCards"));
            if (lang != null && !lang.equals(""))
                options.add(PixabayFetcher.Lang.valueOf(lang));

            if (imageType != null && !imageType.equals(""))
                options.add(PixabayFetcher.ImageType.valueOf(imageType));

            if (orientation != null && !orientation.equals(""))
                options.add(PixabayFetcher.Orientation.valueOf(orientation));

            if (category != null && !category.equals(""))
                options.add(PixabayFetcher.Category.valueOf(category));

            if (order != null && !order.equals(""))
                options.add(PixabayFetcher.Order.valueOf(order));

            if (editorChoice != null && !editorChoice.equals(""))
                options.add(PixabayFetcher.EditorChoice.valueOf(editorChoice));
        }catch (Exception e){
            request.setAttribute("errorMessage", "Les valeurs soumises dans le formulaire ne sont pas des options Pixabay valides");
            RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
            rd.forward(request,response);
            return;
        }


        PixabayFetcher.PixabayAPIOptions optionsTab[] = options.toArray(new PixabayFetcher.PixabayAPIOptions[options.size()]);

        try {
            Card[] cards = CardGenerator.generate(query, optionsTab, nbCards);
            request.setAttribute("nbCards", cards.length);
            request.setAttribute("cards", cards);
        } catch (PixabayAPIExceptions.PixabayIncorrectParameterException | PixabayAPIExceptions.PixabayApiKeyMissingException | PixabayAPIExceptions.PixabayResponseCodeException e) {
            request.setAttribute("errorMessage", "Erreur lors de la récupération des images");
            RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
            rd.forward(request,response);
            return;
        } catch (DatastoreGetter.DataStoreNotAvailableException e) {
            request.setAttribute("errorMessage", "Datastore non disponible. Réessayez plus tard");
            RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
            rd.forward(request,response);
            return;
        }


        RequestDispatcher rd = request.getRequestDispatcher("generateCardsResult.jsp");
        rd.forward(request,response);
    }
}
