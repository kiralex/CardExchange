import com.google.appengine.api.datastore.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class DeleteBookServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Key groupKey = KeyFactory.createKey("Books", "bookStore");

        Query query = new Query("Book", groupKey);
        query.addSort("title", Query.SortDirection.DESCENDING);

        DatastoreService datastore =
                DatastoreServiceFactory.getDatastoreService();

        List<Entity> bookList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(1));
        for (Entity l: bookList) {
            datastore.delete(l.getKey());
        }


        response.sendRedirect("/");
    }
}
