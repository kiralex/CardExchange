package com.derniamepoirier;

import com.google.appengine.api.datastore.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CreateBookServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Key groupKey = KeyFactory.createKey("Books", "bookStore");

        Entity book = new Entity("Book", groupKey);
        book.setProperty("title", "La huitième couleur");
        book.setProperty("date", "1993");

        DatastoreService datastore =
                DatastoreServiceFactory.getDatastoreService();
        datastore.put(book);
    }
}