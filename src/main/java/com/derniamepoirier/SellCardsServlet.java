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
import java.util.Enumeration;
import java.util.HashMap;

@WebServlet(name = "SellCardsServlet", value="sellCards")
public class SellCardsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            boolean sell = false;
            Enumeration<String> paramNames = request.getParameterNames();
            long totalEarnedPoints = 0;
            while (paramNames.hasMoreElements()) {
                String param = paramNames.nextElement();
                if(param.matches("card-[0-9]+") && request.getParameter(param).matches("[0-9]+")){
                    long cardId = Long.parseLong(param.substring(5, param.length()));
                    long quantity = Long.parseLong(request.getParameter(param));
                    if(quantity > 0) {
                        sell = true;
                        totalEarnedPoints += CardAssignmentHelper.sellCardInstance(cardId, quantity);
                    }
                }
            }

            HashMap<Card, Long> cards = CardAssignmentHelper.getAllCards();

            request.setAttribute("cards", cards);
            if(sell)
                request.setAttribute("sellOK", totalEarnedPoints);
            RequestDispatcher rd = request.getRequestDispatcher("sellCards.jsp");
            rd.forward(request,response);
        } catch (UserManagment.UserNotLoggedInException e) {
            request.setAttribute("errorMessage", "Vous devez être connecté pour vendre des cartes.");
            RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
            rd.forward(request,response);
        } catch (DatastoreGetter.DataStoreNotAvailableException e) {
            request.setAttribute("errorMessage", "Datastore non disponible. Réessayez plus tard");
            RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
            rd.forward(request,response);
        }
    }
}
