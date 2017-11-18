package com.derniamepoirier;

import com.derniamepoirier.Utils.DatastoreGetter;
import com.derniamepoirier.User.UserManagment;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "EarnPointsServlet", value="earnPoints")
public class EarnPointsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String nbPointsS = request.getParameter("nbPoints");
            if(UserManagment.getUserService().isUserAdmin() && nbPointsS != null && nbPointsS.matches("[0-9]*"))
                    UserManagment.earnPointsWithSell(Long.parseLong(nbPointsS));
            else
                UserManagment.earnPoints();
            RequestDispatcher rd = request.getRequestDispatcher("/myPoints");
            rd.forward(request,response);

        } catch (UserManagment.UserNotLoggedInException e) {
            request.setAttribute("errorMessage", "Vous devez être connecté pour récupérer des points");
            RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
            rd.forward(request,response);
        } catch (DatastoreGetter.DataStoreNotAvailableException e) {
            request.setAttribute("errorMessage", "Datastore non disponible. Réessayez plus tard");
            RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
            rd.forward(request,response);
        } catch (UserManagment.NoPointsToEarnException e) {
            request.setAttribute("errorMessage", "Vous ne pouvez pas encore gagner de points. Attendez encore un peu !");
            RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
            rd.forward(request,response);
        }

    }
}
