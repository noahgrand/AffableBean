<%--
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software
 * except in compliance with the terms of the license at:
 * http://developer.sun.com/berkeley_license.html

 * author: tgiunipero
--%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Registration</title>
</head>
<body>
<form action="<c:url value='Register'/>" method=post>
    <h1>Registration</h1>
    <div id="loginBox">
        <p><strong><fmt:message key="email"/>:</strong>
            <input type="email" size="20" name="email" value="${param.email}"</p>
        
        <p><strong><fmt:message key="password"/></strong>
            <input type="password" size="20" name="newpassword" value="${param.newpassword}"></p>
        
        <p>
            <input type="submit" value="register">
        </p>
    </div>
</form>
</body>
</html>