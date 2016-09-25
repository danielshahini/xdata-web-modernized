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
<title>List of assignments graded</title>
</head>
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

label span,.required {
	color: red;
	font-weight: bold;
	font-size: 17px;
}

a:link {
	color: #E96D63;
	font: 15px/15px Arial, Helvetica, sans-serif;
} /* unvisited link */
a:hover {
	color: #7FCA9F;
	font: 15px/15px Arial, Helvetica, sans-serif;
} /* mouse over link */
</style>
<body>
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
		}
%>
	<div>
		<br /> <br />
		<div class="fieldset">


			<fieldset>
				<legend> List of Completed Assignments</legend>
				<%
					String courseID = (String) request.getSession().getAttribute(
									"context_label");
							String user_id = (String) request.getSession().getAttribute(
									"user_id");

							//get connection
							Connection dbcon = (new DatabaseConnection()).dbConnection();
							String output = "<ul>";
							SimpleDateFormat formatter = new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss");
							formatter.setLenient(false);
							try{
								PreparedStatement stmt;
								stmt = dbcon
										.prepareStatement("SELECT * FROM xdata_assignment where course_id = ?");
								//	stmt.setString(2, (String)request.getSession().getAttribute("context_label"));
								stmt.setString(1, courseID);
								ResultSet rs;
								rs = stmt.executeQuery();
								while (rs.next()) {

									/**check if its deadline is over or not*/
									String endTime = "";

									endTime = formatter.format(rs.getTimestamp("endtime"));

									//get current date
									Calendar c = Calendar.getInstance();

									String currentDate = formatter.format(c.getTime());
									java.util.Date current = formatter.parse(currentDate);
									//compare times

									java.util.Date oldDate = formatter.parse(endTime);

									
									//now check whether current time is more than end time.Then only assignment can be graded
									if (oldDate.compareTo(current) < 0) {

										output += "<a href="+request.getContextPath() +"/ViewAssignment?assignmentid="
												+ rs.getString("assignment_id").trim()
												+ "V"
												+ "\" target = \"rightPage\">"
												+ "<li> Assignment "
												+ rs.getString("assignment_id") + "</li></a>";
									} 
									/* 			
										output += "<a class=\"header\" target=\"rightPage\" href=\"asgnmentList.jsp?assignmentId="
												+ rs.getString("assignment_id")
												+ " &&studentId="
												+ user_id
												+ "\" ><li> Assignment "
												+ rs.getString("assignment_id") + "</li></a>"; */
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
								dbcon.close();
							}
				%>
			</fieldset>
		</div>
	</div>
</body>
</html>