<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="errorPage.jsp"%> 
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Course Details</title>
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
</style>
</head>
<body style="font-family: helvetica;text-decoration: none;">
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}
%>
	<div id="navigation">
		<ul class="vertical_menu">
		<li></li> 
		 	<%
		 	
			 	String role = (String) session.getAttribute("role");
		 		String userId = (String) session.getAttribute("user_id");
		 		//New course will be created by admin - remove comments
			 
		 	%>
		<!-- 	<li><a class="header" target="rightPage" href="NewCourse.jsp">Create New</a></li> -->
			<%//} %>
			<li><label >View Courses</label></li>
			<!-- <a class="header" target="rightPage" href="ViewCourseList.jsp">View Courses</a></li> -->
			<ul>
			<%
				Connection dbcon = null;
				String output = "";
				boolean isCourseExist = false;
				try{
					
					dbcon = (new DatabaseConnection()).dbConnection();
					PreparedStatement stmt;
					//stmt = dbcon.prepareStatement("SELECT * FROM  xdata_course");
					stmt = dbcon.prepareStatement("select instructor_course_id, year from xdata_course xc inner join xdata_roles xr on xc.instructor_course_id = xr.course_id where internal_user_id =? and role = ? order by year desc");
					stmt.setString(1,userId);
					stmt.setString(2,role);
					ResultSet rs = stmt.executeQuery();
					Calendar c = Calendar.getInstance();
					int year = c.get(Calendar.YEAR);
					while(rs.next())
					{
							//output +="<input name=\"View\" type=\"button\" id=\""
								//	+rs.getString("instructor_course_id")+"\" value=\"View\" onclick=\"setSessionParam(this.id)\">";
						
					//Show course ids for all years in reverse order of year
					%>
					<li><a href="selectMode.jsp?contextLabel=<%=rs.getString("instructor_course_id")%>" target="_top"><%=rs.getString("instructor_course_id")%> (<%=rs.getInt("year") %>)</a></li>									
					<% 
						
					}
				}catch (Exception err) {
	
					err.printStackTrace();
					//out.println("Error in getting list of assignments");
					throw new ServletException(err);
					
				}
				finally{
					dbcon.close();
				}
				%>
			
			<li></li>
			</ul>
		</ul>
		<div>
		<a href='http://www.cse.iitb.ac.in/infolab/xdata/' target='_blank'>About XData</a>
		<p class="copyright">© 2015 IIT Bombay. All rights reserved</p>
		</div>
	</div>
</body>
</html>
