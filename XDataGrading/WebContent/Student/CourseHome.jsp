<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Welcome to XData</title> 
<script type="text/javascript">
function getParameterByName(name) { 
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}  
function LoadPage(){
		document.getElementById('right2').src = "Student/ViewCourseList.jsp";
		if(getParameterByName("viewList") != ""){
			document.getElementById('right2').src = "ViewCourseList.jsp";
			document.getElementById('leftname').src = "CourseMenu.jsp";
		}
} 
</script> 
</head>
<frameset rows=50px,* border=0 frameborder=0 framespacing=0  onload="LoadPage();" id="leftframeset">
	<frame src="Header.jsp"  name="rightPage1" id="right1" tabindex="60" scrolling="no" style="border-bottom: 1px solid black">
	<frameset cols=21%,* border=0 frameborder=0 framespacing=0 >
		<frame  src = "Student/CourseMenu.jsp" name="leftPage" id="leftname" tabindex="1" >
		<frame src="Welcome.jsp"  name="rightPage" scrolling="yes" id="right2" tabindex="60" style="padding:15px 0 0 0px;">
	</frameset>	
</frameset> 
 

<body>
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
		}
%>
</body>
</html>