<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="../errorPage.jsp"%>
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
<title>Insert title here</title>

<style> 

label {
	font-size: 18px;
	font-weight: bold;
	color: #666;
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
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}
if(! Boolean.parseBoolean(session.getAttribute("ltiIntegration").toString())){%>
<div id="breadcrumbs">
  <a id="bcrumb_link" style='color:#353275;text-decoration: none;' href="../CourseHome.jsp" target="_top">Home</a> &nbsp; >> &nbsp;
   <a  id="bcrumb_link" style='color:#353275;text-decoration: none;' href="ListAllAssignments.jsp" target="_self">Assignment List</a>&nbsp; >> &nbsp;
   	<a  id="bcrumb_link" style='color:#353275;text-decoration: none;' href="ListOfQuestions.jsp?assignmentid=<%=request.getParameter("AssignmentID") %>" target="_self">Question List</a>&nbsp; >> &nbsp;
   <a style='color:#0E0E0E;text-decoration: none;font-weight: normal;' id="bcrumb_no_link" href="#">View Grades</a>
  </div> 
<%}else{ %>
   <a  id="bcrumb_link" href="ListOfQuestions.jsp?assignmentid=<%=request.getParameter("AssignmentID") %>" target="_self">Question List</a>&nbsp; >> &nbsp;
   <a id="bcrumb_no_link" href="#">View Grades</a>
<%} %> 
  
	test page<div>
		<br /> <br />
		<div class="fieldset">

			<fieldset>
				<legend> Grades</legend>
				<%
					String courseID = (String) request.getSession().getAttribute(
									"context_label");
 
							int assignID =Integer.parseInt( request.getParameter("AssignmentID") );
									//Integer.parseInt((String)request.getSession().getAttribute(
									//"resource_link_id"));
							String studentId = (String) request.getSession().getAttribute(
									"user_id");
 
							//get connection
							Connection dbcon = (new DatabaseConnection()).dbConnection();

							try {
								PreparedStatement stmt;
								stmt = dbcon
										.prepareStatement("SELECT * FROM xdata_assignment where assignment_id = ? and course_id = ?");
								stmt.setInt(1,assignID);
								stmt.setString(2,courseID);
								ResultSet rs;
								rs = stmt.executeQuery();
								String endTime = "";
								SimpleDateFormat formatter = new SimpleDateFormat(
										"yyyy-MM-dd HH:mm:ss");
								formatter.setLenient(false);
								if (rs.next()) {
									endTime = formatter.format(rs.getTimestamp("endtime"));
									rs.close();
									stmt.close();
								} else {
									out.println("No assignment exists");
									rs.close();
									stmt.close();
									dbcon.close();
									return;
								} 
								//get current date
								Calendar c = Calendar.getInstance();

								String currentDate = formatter.format(c.getTime());
								java.util.Date current = formatter.parse(currentDate);
								//compare times
 
								java.util.Date oldDate = formatter.parse(endTime);
								//now check whether current time is more than end time.
								//Then only assignment can be graded
								//This should also check whether the assignment is graded or not.
								 
								if (oldDate.compareTo(current) < 0) {
									String remoteLink = request.getContextPath()+"/ViewAssignment?assignmentid="
											+ assignID+ "V";
									response.sendRedirect(remoteLink);
								} else { 
									out.println("<label>Assignment is not yet graded</label>");
								}
								rs.close();
								stmt.close();
							} catch (Exception err) {			
								err.printStackTrace();
								throw new ServletException(err);
							}
							dbcon.close();
				%>
			</fieldset>
		</div>
	</div>
</body>
</html>