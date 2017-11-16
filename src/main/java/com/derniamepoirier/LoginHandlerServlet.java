package com.derniamepoirier;

import com.derniamepoirier.Utils.DatastoreGetter;
import com.derniamepoirier.Utils.UserManagment;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "LoginHandlerServlet", value="test")
public class LoginHandlerServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            if(UserManagment.earnPoints()){
                response.getWriter().write("J'ai gagné des points");
            }else{
                response.getWriter().write("Je dois attendre ...");
            }
        } catch (UserManagment.UserNotLoggedInException e) {
            e.printStackTrace();
        } catch (DatastoreGetter.DataStoreNotAvailableException e) {
            e.printStackTrace();
        }

    }
}
