<!DOCTYPE html>
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta charset="UTF-8">
<title>Students Menu</title>
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
	width:200px;
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
</style> 
</head> 
<body style="font-family: helvetica;text-decoration: none;">

	<div id="navigation">
		<ul class="vertical_menu">
		<%
			if(Boolean.parseBoolean(session.getAttribute("ltiIntegration").toString())){
				 if(session.getAttribute("allowedAssignment")  != null && 
						 !(((Integer)session.getAttribute("allowedAssignment") ) == 0 )){%>
				<li> <a class="header" target="rightPage" href="ListOfQuestions.jsp?assignmentid=<%=session.getAttribute("allowedAssignment") %>&&studentId=<%=session.getAttribute("user_id")%>">Go to assignment</a> </li>
				<%} %>
			    <li> <a class="header" target="rightPage" href="ListAllAssignments.jsp">View All Assignments</a> </li> 
	 
			<% } else { %>		
				<li> <a class="header" target="rightPage" href="ListAllAssignments.jsp">View All Assignments</a> </li>
	 
		<li><a style='color: #353275;' href="javascript:window.top.location.href ='../CourseHome.jsp?viewList=true'">Back to Course List</a></li> 
	<%} %>
			</ul>
		<div> 
		<a href='http://www.cse.iitb.ac.in/infolab/xdata/' target='_blank'>About XData</a>
		<p class="copyright"> (c) 2015-2021 IIT Bombay. All rights reserved</p>
		</div>
	</div>
	
</body></html>

