package com.derniamepoirier;

import com.derniamepoirier.CardGeneration.Card;
import com.derniamepoirier.User.CardAssignmentHelper;
import com.derniamepoirier.Utils.DatastoreGetter;
import com.derniamepoirier.User.UserManagment;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "DrawCardsServlet", value="/drawCards")
public class DrawCardsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            long nbPoints = UserManagment.getNbPoints();
            int nbCardsMax = (int) (nbPoints / UserManagment.NB_POINTS_PER_CARD);

            request.setAttribute("nbPoints", nbPoints);
            request.setAttribute("nbCardsMax", nbCardsMax);

            if(nbCardsMax > 0 && request.getParameter("nbCardsToDraw") != null && !request.getParameter("nbCardsToDraw").equals("")){
                int nbCardsToDraw = Integer.valueOf(request.getParameter("nbCardsToDraw"));

                if(nbCardsToDraw <= 0){
                    request.setAttribute("errorMessage", "Nombre de cartes à collecter incorrect. Il doit être supérieur à zéro.");
                    RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
                    rd.forward(request,response);
                    return;
                }else if(nbCardsToDraw > nbCardsMax){
                    request.setAttribute("errorMessage", "Nombre de cartes à collecter incorrect. Il doit être inférieur ou égal à " + nbCardsMax + " cartes.");
                    RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
                    rd.forward(request,response);
                    return;
                }

                if(Card.countAllCards() <= 0){
                    request.setAttribute("errorMessage", "Aucune carte dans la bibliothèque. Attendez que l'Administreur en crée.");
                    RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
                    rd.forward(request, response);
                    return;
                }

                Card cards[] = new Card[nbCardsToDraw];

                for(int i = 0; i < nbCardsToDraw; i++) {
                    UserManagment.spendPoints();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    cards[i] = Card.drawFromStore();
                    CardAssignmentHelper.assignCardInstanceToUser(cards[i]);
                }


                request.setAttribute("cards", cards);
                request.setAttribute("nbPoints", nbPoints-nbCardsToDraw);
            }

            RequestDispatcher rd = request.getRequestDispatcher("drawCards.jsp");
            rd.forward(request,response);
            return;


        } catch (DatastoreGetter.DataStoreNotAvailableException e) {
            request.setAttribute("errorMessage", "Datastore non disponible. Réessayez plus tard");
            RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
            rd.forward(request,response);
        } catch (UserManagment.UserNotLoggedInException e) {
            request.setAttribute("errorMessage", "Vous devez être connecté pour acheter des cartes");
            RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
            rd.forward(request,response);
        } 
        catch (CardAssignmentHelper.NullCardPointerException e) { /* allready managed in servlet */ }
        catch (UserManagment.NotEnoughPointsToSpendException e) { /* already managed in servlet */ }
    }


}
