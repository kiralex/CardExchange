<%@ tag description="User Page template" pageEncoding="UTF-8"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ attribute name="userName" required="true"%>

<t:genericPage>
    <jsp:attribute name="header">
      <h1>Welcome ${userName}</h1>
    </jsp:attribute>
    <jsp:attribute name="footer">
      <p id="copyright">Copyright 1927, Future Bits When There Be Bits Inc.</p>
    </jsp:attribute>
    <jsp:body>
        <body-content>JSP</body-content>
        <jsp:doBody/>
    </jsp:body>
</t:genericPage>