package com.derniamepoirier;

import com.derniamepoirier.CardGeneration.Card;
import com.derniamepoirier.Utils.DatastoreGetter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AllCardsServlet", value = "/allCards")
public class AllCardsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Card cards[] = null;
        int nbCards = 0;

        int nbPerPage = 16;

        int page = 1;
        if(request.getParameter("page") != null){
            page = Integer.valueOf((String) request.getParameter("page"));
            if(page < 1){
                page = 1;
            }
        }

        try {
            cards = Card.restoreMultipleFromStore(nbPerPage, page);
            nbCards = Card.countAllCards();
        } catch (DatastoreGetter.DataStoreNotAvailableException e) {
            request.setAttribute("errorMessage", "Datastore non disponible. Réessayez plus tard");
            RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
            rd.forward(request,response);
        }

        request.setAttribute("cards", cards);
        request.setAttribute("nbPages", Math.ceil((nbCards+0.0)/nbPerPage));
        request.setAttribute("page", page);
        RequestDispatcher rd = request.getRequestDispatcher("allCards.jsp");
        rd.forward(request,response);
    }
}
