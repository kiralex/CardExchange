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

@WebServlet(name = "RandomCardServlet", value="/card")
public class RandomCardServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setAttribute("card", Card.drawFromStore());

            RequestDispatcher rd = request.getRequestDispatcher("index3.jsp");
            rd.forward(request,response);
        } catch (DatastoreGetter.DataStoreNotAvailableException e) {
            e.printStackTrace();
        }
    }
}
