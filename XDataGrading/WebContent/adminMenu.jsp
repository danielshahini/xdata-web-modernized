<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Admin Menu</title>
<style>
html{
	border: 0px; 
	margin: 0px;
}

a{
	color: #fff;
}

a:visited{
	color: #fff;
}

#navigation{
	border: 1px solid #A6A6B6;
	height: 85%;
	border-radius: 14px;
	background: #E4E4E4;
	padding: 25px 5px 5px;
	margin-top: 20px;
	position:absolute;
	width:175px;
}

.vertical_menu {
	float: left;
	/*background-color: #EEFFFF;*/
	padding: 5px;
	margin: 5px;
	display: block;

	/* border: 1px dotted blue;
	background-color: #EEFFFF;
	margin: 5px;
	padding: 5px; */
}

.vertical_menu li ul{
	padding-top: 7px;
}

.vertical_menu li a {
	text-decoration: none;
	color: #353275;
}

.vertical_menu li{
	text-decoration: none;
	color: #353275;
	margin-bottom: 10px;
	font-size: 15px;
	list-style: none;
}

.vertical_menu lh{
	text-decoration: none;
	color: #353275;
	/*color:#fff;*/
	margin-bottom: 10px;
	font-size: 18px; 
	font-weight:bold;
	list-style:disc;
	/*background-color: #3B5998; */
}  

.vertical_menu a:hover {
	text-decoration: underline;
}

#navigation div {
	font-size: 11px;
	bottom:0px;
	position:absolute;
}

#navigation div a {
	text-decoration: none;
	color: #353275;
	border-bottom: 1px dotted black;
}

#breadcrumbs
{
  position: absolute;
  left: 20px;
  top: 20px;
}

</style>

</head>
<body style="font-family: helvetica;text-decoration: none;">
<%
if (session.getAttribute("LOGIN_USER") == null ) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}
else if(session.getAttribute("LOGIN_USER") != null && !session.getAttribute("LOGIN_USER").equals("ADMIN")
		&& session.getAttribute("role") != null && !session.getAttribute("role").equals("admin")
		){
	response.sendRedirect("index.jsp?NotAuthorised=true");
	session.invalidate();
	return;
}
%>
	<div id="navigation">
		    <ul class="vertical_menu">
		<li></li> 
			<li><a class="header" target="rightPage" href="CreateUser.jsp">Create User</a></li>
			<li><a class="header" target="rightPage" href="NewCourse.jsp">Create Course</a></li>
			<li><a class="header" target="rightPage" href="AssignRoles.jsp">Assign Role</a></li>
			<li><a class="header" target="rightPage" href="ViewUsers.jsp">View Users</a></li>
			<li><a class="header" target="rightPage" href="ViewCourseListForAdmin.jsp">View Courses</a></li>
				<li><a class="header" target="rightPage" href="ltisetup.jsp">LMS setup</a></li>
		  
<!-- <li><a class="header" target="rightPage" href="ViewUsers.jsp">File Upload</a></li> -->
 		</ul> 		 
		<div> 
		<a href='http://www.cse.iitb.ac.in/infolab/xdata/' target='_blank'>About XData</a>
		<p class="copyright">© 2015 IIT Bombay. All rights reserved</p>
		</div>
	</div>
</body>
</html>