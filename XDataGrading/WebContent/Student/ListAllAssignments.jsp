<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 
 <link rel="stylesheet" href="../css/structure.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<style>

li {
	text-align: left
}



textarea,select {
	font: 12px/12px Arial, Helvetica, sans-serif;
	padding: 0;
}

input {
	font: 15px/15px Arial, Helvetica, sans-serif;
	padding: 0;
}

fieldset.action {
	background: #9da2a6;
	border-color: #e5e5e5 #797c80 #797c80 #e5e5e5;
	margin-top: -20px;
}


label {
	font-size: 15px;
	font-weight: bold;
	color: #666;
}

label
span,.required {
	color: red;
	font-weight: bold;
	font-size: 17px;
}
 /* mouse over link */
.stop-scrolling {
	height: 100%;
	/* overflow: hidden; */
}

#breadcrumbs
{
  position: absolute;
  padding-left:10px;
  padding-right:10px;
  left: 5px;
  top: 10px;
  font: 13px/13px Arial, Helvetica, sans-serif;
  background-color: #f0f0f0;
  font-weight: bold;
}
  
</style>

</head>
<body>
<body>
<%

if(! Boolean.parseBoolean(session.getAttribute("ltiIntegration").toString())){
		%>
<div id="breadcrumbs"> 
  <a style='color:#353275;text-decoration: none;' href="../CourseHome.jsp" target="_top">Home</a> &nbsp; >> &nbsp;
  <a style='color:#0f0f0f;text-decoration: none;font-weight: normal;' href="#">Assignment List</a> 
  </div>
  <%} %>
  <br/>
	<div>	
		<div class="fieldset">
			<fieldset>
				<legend> List of Assignments</legend>
				<%
					String courseID = (String) request.getSession().getAttribute(
									"context_label");
							String user_id = (String) request.getSession().getAttribute(
									"user_id");
							
							//get connection
							Connection dbcon = (new DatabaseConnection()).dbConnection();
							String output = "<ul>";
 
							try {
								PreparedStatement stmt;
								stmt = dbcon
										.prepareStatement("SELECT * FROM xdata_assignment where course_id = ? AND assignment_id > 0");
								//	stmt.setString(2, (String)request.getSession().getAttribute("context_label"));
								stmt.setString(1, courseID);
								ResultSet rs;
								rs = stmt.executeQuery();
								while (rs.next()) {	
									output += "<a class=\"header\" target=\"rightPage\" href=\"ListOfQuestions.jsp?assignmentid="
											+ rs.getString("assignment_id")
											+ "&&studentId="
											+ user_id
											+ "\" ><li> ";
											if(rs.getString("assignmentname") != null){
												output += rs.getString("assignmentname") + "</li></a>";
											}
											else{ 
												output += rs.getString("assignment_id") + "</li></a>";
											}
								}
								rs.close();
								stmt.close();
								output += "</ul>";

								out.println(output);
							} catch (Exception err) {

								err.printStackTrace();
								throw new ServletException(err);
							}
							finally{
								if(dbcon != null)
								dbcon.close();
							}
				%>
			</fieldset>
		</div>
	</div>

</body>
</html>