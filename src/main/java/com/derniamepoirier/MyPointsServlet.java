package com.derniamepoirier;

import com.derniamepoirier.Utils.DatastoreGetter;
import com.derniamepoirier.Utils.UserManagment;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@WebServlet(name = "MyPointsServlet", value="/myPoints")
public class MyPointsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long nbPoints = 0;
        Date nextPointEarnDate = null;
        boolean canEarnPoints = false;

        try {
            nbPoints = UserManagment.getNbPoints();
            nextPointEarnDate = UserManagment.getNextPointEarnDate();
            canEarnPoints = UserManagment.canEarnPoints();

            request.setAttribute("nbPoints", nbPoints);
            request.setAttribute("nextPointEarnDate", nextPointEarnDate.getTime());
            request.setAttribute("canEarnPoints", true);
            RequestDispatcher rd = request.getRequestDispatcher("myPoints.jsp");
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
