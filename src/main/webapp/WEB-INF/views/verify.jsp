<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" pageEncoding="utf-8"%>
<html>
<head>
    <title>ChatGame</title>
</head>
<body>
    <div class="success">
    <c:choose>
        <c:when test="verified">
            <a href="cgtp://verify?code=${code}">Verify Success</a>
        </c:when>
        <c:otherwise>
            Invalidate
        </c:otherwise>
    </c:choose>
</body>
</html>
