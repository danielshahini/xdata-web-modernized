<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<!DOCTYPE html>
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta charset="UTF-8">
<title>Students Menu</title>
<style>
h1{
	float: left;
	color:#fff;
	font-size: 25px;
	padding:0; margin:0;
	padding-left:100px;
	padding-top:5px;
}

body{
	background-repeat: repeat;
	background-size: auto;
	background-position: 0 0;
	background-color: #3B5998;
	border: 0;
	/*background-image: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAAqCAMAAABFoMFOAAAAWlBMVEVOaaJDYJxCX5xBXptIZJ9MZ6E/XJpFYZ1KZqA9W5lGYp5HY55MaKFJZZ9LZqBEYZ1NaaJNaKJNaKFAXZtAXZpLZ6E+XJo+W5lJZaA9Wpk8Wpk8Wpg8WZg7WZj2xcGWAAAANElEQVR42lWGSQoAIBDDHCjo0f8/UxBxQDQuFwlpqgBZBq6+P+unVY1GnDgwqbD2zGz5e1lBdwvGGPE6OgAAAABJRU5ErkJggg==) ;*/
	font-family: helvetica;text-decoration: none;
}

.logout{
	
	float: right;
	font-size: 11.5px;
	color:#fff;
	padding-top: 2px;
	
}

.logout a {
	text-decoration: none;
	color:#B1A39E;
}

.logout a:visited {
	text-decoration: none;
}

</style>
</head>
<body>
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}
%>
<div> 
<label style="font-size: 25px;font-family:helvetica;color:#fff;font-weight: bold; padding-left: 40px;padding-top: 0px;">XData</label>
<label class = 'logout'>You are logged in as 

<% out.println(session.getAttribute("lis_person_name_full")); %>
	(<a href='Logout' target = "_parent">Logout</a>)
</label>
</div>
</body>