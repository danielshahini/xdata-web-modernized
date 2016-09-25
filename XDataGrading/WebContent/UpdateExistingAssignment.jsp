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
	}
	
		int asgnmentID = Integer.parseInt(request.getParameter("AssignmentID"));
			String courseID = (String) (String) request.getSession()
			.getAttribute("context_label");
			
		//getting parameters
		String name = request.getParameter("assignment_name");
		String startDate = request.getParameter("start");
		String endDate = request.getParameter("end");
		String description = request.getParameter("description");
		String[] defaultDS = request.getParameterValues("defaultDSId");
		
		Gson gson = new Gson();
		String json = gson.toJson(defaultDS);
		
		int schemaId = Integer.parseInt(request.getParameter("schemaid"));
		int connectionId = Integer.parseInt(request.getParameter("dbConnection"));
		boolean interactive = false;
		
		if(request.getParameter("interactive") != null){
			interactive = true;	
		}

		
		//get connection
		Connection dbcon = (new DatabaseConnection()).dbConnection();

		try {
		PreparedStatement stmt; 
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm");
	    java.util.Date parsedDate = dateFormat.parse(startDate);
	    System.out.println(parsedDate.toString());
	    Timestamp startTimeStamp = new java.sql.Timestamp(parsedDate.getTime());
	    
	    parsedDate = dateFormat.parse(endDate);
	    Timestamp endTimeStamp = new java.sql.Timestamp(parsedDate.getTime());
 
		stmt = dbcon.prepareStatement
				("UPDATE xdata_assignment SET starttime=? ,endtime=?, connection_id = ?, defaultschemaid = ?, description=?, assignmentName=?, learning_mode=?, defaultDSetId =? WHERE assignment_id=? and course_id = ?");

		stmt.setTimestamp(1, startTimeStamp); 
		stmt.setTimestamp(2, endTimeStamp);
		stmt.setInt(3, connectionId);
		stmt.setInt(4, schemaId);
		stmt.setString(5,description);
		stmt.setString(6,name);
		stmt.setBoolean(7, interactive); 
		stmt.setString(8,json);
		stmt.setInt(9, asgnmentID);
		stmt.setString(10, courseID); 
		
		stmt.executeUpdate();
		
		String url="";
		
		if(!((String)request.getSession().getAttribute("LOGIN_USER")).equalsIgnoreCase("tester")){
			url = "asgnmentList.jsp?assignmentId="+ asgnmentID;
		}else{ 
			url = "ListOfTesterQuestions.jsp?AssignmentID="+ asgnmentID;
		}
		response.sendRedirect(url);
		
		//out.println("<p align=\"center\" style=\"font-size:18px;\"><marquee>Assignment Updated Successfully </marquee></p>");
			}

			catch (SQLException sep) {
		System.out.println("Could not connect to database: " + sep);
		out.println("Error in uploading assignment");
		throw new ServletException(sep);
		}
			dbcon.close();
	%>
</body>
</html>