package com.derniamepoirier;

import com.derniamepoirier.CardGeneration.Card;
import com.derniamepoirier.User.CardAssignmentHelper;
import com.derniamepoirier.User.UserManagment;
import com.derniamepoirier.Utils.DatastoreGetter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

@WebServlet(name = "SellCardsServlet", value="sellCards")
public class SellCardsServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            HashMap<Card, Long> cards = CardAssignmentHelper.getAllCards();

            request.setAttribute("cards", cards);
            RequestDispatcher rd = request.getRequestDispatcher("sellCards.jsp");
            rd.forward(request,response);
        } catch (UserManagment.UserNotLoggedInException e) {
            request.setAttribute("errorMessage", "Vous devez être connecté pour connaître votre nombre de points");
            RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
            rd.forward(request,response);
        } catch (DatastoreGetter.DataStoreNotAvailableException e) {
            request.setAttribute("errorMessage", "Datastore non disponible. Réessayez plus tard");
            RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
            rd.forward(request,response);
        }
    }
}
