<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 
 
<link rel="stylesheet" href="css/structure.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Welcome to XData</title>
</head> 

<frameset rows=50px,* border=0 frameborder=0 framespacing=0  id="leftframeset">
	<frame src="Header.jsp"  name="rightPage1" id="right1" tabindex="60" scrolling="no" style="border-bottom: 1px solid black">

	<frameset cols=21%,* border=0 frameborder=0 framespacing=0 >
		<frame  src = "testerMenu.jsp" name="leftPage" id="leftname" tabindex="1" >
		<!-- <frame src="ListAllAssignments.jsp"  name="rightPage" id="right2" tabindex="60" style="padding:15px 0 0 20px;"> -->
		 <frame src="Welcome.jsp"  name="rightPage" scrolling="yes" id="right2" tabindex="60" style="padding:15px 0 0 0px;">
	</frameset>
</frameset>

<body>
<%
String courseId = (String) request.getSession().getAttribute("context_label");//request.getParameter("contextLabel");
System.out.println("Tester Home - context label value:"+ courseId);
if(! ((Boolean)session.getAttribute("ltiIntegration"))){
	session.setAttribute("context_label",courseId);
}
%>
</body>
</html>