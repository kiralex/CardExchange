package com.derniamepoirier;

import com.derniamepoirier.CardGeneration.Card;
import com.derniamepoirier.Utils.DatastoreGetter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "BuildCardImageServlet", value="/buildImage")
public class BuildCardImageServlet extends HttpServlet {

    private void datastoreUnavailable(HttpServletResponse response)throws ServletException, IOException{
        response.sendError(503, "Datastore not available");
        return;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            if(request.getParameter("cardId") == null){
                response.sendError(400, "invalid parameters");
                return;
            }
            long cardId = Long.parseLong(request.getParameter("cardId"));

            Card card = null;
            try {
                card = Card.restoreFromStore(cardId);
            } catch (DatastoreGetter.DataStoreNotAvailableException e) {
                this.datastoreUnavailable(response);
            }

            if(card == null){
                response.sendError(404, "Card not found");
                return;
            }

            try {
                card.generateCardImage();
                response.getWriter().write("<img src=\"" + card.getCardImageURL() + "\"></img>");
            } catch (DatastoreGetter.DataStoreNotAvailableException e) {
                this.datastoreUnavailable(response);
            }

    }
}
