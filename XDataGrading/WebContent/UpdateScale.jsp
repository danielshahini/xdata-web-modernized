<%@page import="com.google.gson.Gson"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%> 
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Updating the assignment</title>
<style>




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
.stop-scrolling {
	height: 100%;
	/*overflow: hidden;*/
}
</style>
</head>
<body>

	<%
	if (session.getAttribute("LOGIN_USER") == null) {
		response.sendRedirect("index.jsp?TimeOut=true");
		return;
	}else if(session.getAttribute("LOGIN_USER") != null && !session.getAttribute("LOGIN_USER").equals("ADMIN")
	 		&& session.getAttribute("role") != null && (!session.getAttribute("role").equals("instructor") || !session.getAttribute("role").equals("tester"))){
		response.sendRedirect("index.jsp?NotAuthorised=true");
		session.invalidate();
		return;
	}
	//System.out.println("hey>>>>>>>>>>>");
		int assignmentID = Integer.parseInt(request.getParameter("AssignmentID"));
			String courseID = (String) (String) request.getSession()
			.getAttribute("context_label");
			
		//get connection
		
		Connection dbcon = (new DatabaseConnection()).dbConnection();
		try {
			//System.out.println("hey>>>>>>>>>>>");
		PreparedStatement stmt; 
	    
		stmt = dbcon.prepareStatement
				("select * from xdata_qinfo where course_id = ? and assignment_id = ?"
						+"order by assignment_id,question_id");
		stmt.setString(1, courseID);
		stmt.setInt(2, assignmentID);
		ResultSet rs = stmt.executeQuery();
		while(rs.next())
		{
			int qID = rs.getInt("question_id");
			float weight = Float.parseFloat(request.getParameter("weight"+qID));
			PreparedStatement stmt1;
			stmt1 = dbcon.prepareStatement("update xdata_qinfo set scale=? where course_id = ? and assignment_id = ? and question_id = ?");
			stmt1.setFloat(1, weight);
			stmt1.setString(2, courseID);
			stmt1.setInt(3, assignmentID);
			stmt1.setInt(4, qID);
			stmt1.executeUpdate();
			stmt1.close();
		}
		stmt.close();
		rs.close();
		String url="";
		
		if(!((String)request.getSession().getAttribute("LOGIN_USER")).equalsIgnoreCase("tester")){
			url = "asgnmentList.jsp?assignmentId="+ assignmentID;
		}else{ 
			url = "ListOfTesterQuestions.jsp?AssignmentID="+ assignmentID;
		}
		response.sendRedirect(url);
		
		//out.println("<p align=\"center\" style=\"font-size:18px;\"><marquee>Assignment Updated Successfully </marquee></p>");
			}

		catch (Exception sep) {
		System.out.println("Could not connect to database: " + sep);
		out.println("Error in uploading assignment");
		throw new ServletException(sep);
		}
		finally
		{
			if(dbcon!=null) dbcon.close();
		}
	%>
</body>
</html>