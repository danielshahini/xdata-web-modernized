<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 
 <link rel="stylesheet" href="../css/structure.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Get grades of present assignment</title>
</head>
<body>

<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
		}
String assignID = (String)request.getParameter("assignmentid");
if (assignID == null)
	assignID = (String) request.getSession().getAttribute(
			"resource_link_id");
String remoteLink = request.getContextPath()+"/ViewAssignment?assignmentid="
		+ assignID.trim() 
		+"V";
		
//+ "\" target = \"rightPage\">
response.sendRedirect(remoteLink);
%>
</body>
</html>