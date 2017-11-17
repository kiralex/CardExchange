package com.derniamepoirier;

import com.derniamepoirier.CardGeneration.*;
import com.derniamepoirier.Utils.DatastoreGetter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "generationCardServlet", value = "/generateCard")
public class generationCardServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String query = request.getParameter("query");
        String nbCards = request.getParameter("nbCards");
        String lang = request.getParameter("selectLang");
        String imageType = request.getParameter("imageTypeOptions");
        String orientation = request.getParameter("orientationOptions");
        String category = request.getParameter("selectCategory");
        String order = request.getParameter("orderOptions");
        String editorChoice = request.getParameter("editorChoiceOptions");


//        response.setContentType("text/plain");
//        response.getWriter().println(query);
//        response.getWriter().println(nbCards);
//        response.getWriter().println(lang);
//        response.getWriter().println(imageType);
//        response.getWriter().println(orientation);
//        response.getWriter().println(category);
//        response.getWriter().println(order);
//        response.getWriter().println(editorChoice);


//        PixabayFetcher.PixabayAPIOptions options[] = new PixabayFetcher.PixabayAPIOptions[]{
//                PixabayFetcher.Lang.valueOf(lang),
//                PixabayFetcher.ImageType.valueOf(imageType),
//                PixabayFetcher.Orientation.valueOf(orientation),
//                PixabayFetcher.Category.valueOf(category),
//                PixabayFetcher.Order.valueOf(order),
//                PixabayFetcher.EditorChoice.valueOf(editorChoice)
//        };

        PixabayFetcher.PixabayAPIOptions options[] = new PixabayFetcher.PixabayAPIOptions[]{PixabayFetcher.ImageType.PHOTO, PixabayFetcher.Order.POPULAR, PixabayFetcher.Orientation.VERTICAL};
        try {
            Card[] cards = CardGenerator.generate("poney", options, 100);
            request.setAttribute("nbCards", nbCards);
            request.setAttribute("cards", cards);
        } catch (PixabayIncorrectParameterException e) {
            e.printStackTrace();
        } catch (PixabayApiKeyMissingException e) {
            e.printStackTrace();
        } catch (PixabayResponseCodeException e) {
            e.printStackTrace();
        } catch (DatastoreGetter.DataStoreNotAvailableException e) {
            request.setAttribute("errorMessage", "Datastore non disponible. Réessayez plus tard");
            RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
            rd.forward(request,response);
        }


        RequestDispatcher rd = request.getRequestDispatcher("generateCardsResult.jsp");
        rd.forward(request,response);
    }
}
