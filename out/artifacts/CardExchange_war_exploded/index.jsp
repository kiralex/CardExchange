<%@ page import="com.google.appengine.api.datastore.*" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>$Title$</title>
</head>
<body>
<p>
    kouerhisrhuezgejrezyu
  acceuil de rousseau dans la mer
    <%
        Key keyGroup = KeyFactory.createKey("Books", "bookStore");

        Query query = new Query("Book", keyGroup);
        query.addSort("title", Query.SortDirection.DESCENDING);

        DatastoreService datastore =
                DatastoreServiceFactory.getDatastoreService();

        List<Entity> books = datastore.prepare(query).asList(
                FetchOptions.Builder.withLimit(5));



        for(Entity l : books) {
    %>
<p>
  <%= l.getProperty("title") %>
</p>

<%
  }
%>
</p>
</body>
</html>