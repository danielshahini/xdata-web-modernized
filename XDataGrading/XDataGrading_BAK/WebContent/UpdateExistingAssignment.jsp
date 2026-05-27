<%@page import="com.google.gson.Gson"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%> 
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "https://www.w3.org/TR/html4/loose.dtd">
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
	
		int asgnmentID = Integer.parseInt(request.getParameter("AssignmentID"));
			String courseID = (String) (String) request.getSession()
			.getAttribute("context_label");
			
		//getting parameters
		String name = request.getParameter("assignment_name");
		String startDate = request.getParameter("start");
		String endDate = "";
		String description = request.getParameter("description");
		String[] defaultDS = request.getParameterValues("defaultDSId");
		
		Gson gson = new Gson();
		String json = gson.toJson(defaultDS);
		
		int schemaId = Integer.parseInt(request.getParameter("schemaid"));
		int connectionId = Integer.parseInt(request.getParameter("dbConnection"));
		boolean interactive = false;
		boolean showmarks = false;
		if(request.getParameter("interactive") != null){
			interactive = true;	
		}
		if(request.getParameter("showmarks") != null){
			showmarks = true;	
		}
		Boolean softdateselected = false;
		String softdate = "";
		String penalty = "";
		Timestamp softTimeStamp = null;
		if(request.getParameter("softdeadlineselectname") != null)   // selected
		{
			softdateselected = true;
			softdate=request.getParameter("end");
			endDate=request.getParameter("soft");
			penalty=request.getParameter("penalty");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			java.util.Date parsedDate = dateFormat.parse(softdate);
		    softTimeStamp = new java.sql.Timestamp(parsedDate.getTime());
		}
		else
		{
			softdateselected = false;
			endDate=request.getParameter("end");
			penalty="10";
		}
		//get connection
		//Connection dbcon = (new DatabaseConnection()).dbConnection();

		try(Connection dbcon = (new DatabaseConnection()).dbConnection()) {
		PreparedStatement stmt; 
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		java.util.Date parsedDate = dateFormat.parse(startDate);
	    System.out.println(parsedDate.toString());
	    Timestamp startTimeStamp = new java.sql.Timestamp(parsedDate.getTime());
	    
	    parsedDate = dateFormat.parse(endDate);
	    Timestamp endTimeStamp = new java.sql.Timestamp(parsedDate.getTime());
	    //System.out.println("endDate: "+ endDate);
	    //System.out.println("parsedDate: "+ parsedDate);
	    //System.out.println("endTimeStamp: "+ endTimeStamp);
 		
	    
	    
		stmt = dbcon.prepareStatement
				("UPDATE xdata_assignment SET showmarks=?, starttime=? ,endtime=?, connection_id = ?, defaultschemaid = ?, description=?, assignmentName=?, learning_mode=?, defaultDSetId =?, penalty =? , softtime =? WHERE assignment_id=? and course_id = ?");
		stmt.setBoolean(1, showmarks);
		stmt.setTimestamp(2, startTimeStamp); 
		stmt.setTimestamp(3, endTimeStamp);
		stmt.setInt(4, connectionId);
		stmt.setInt(5, schemaId);
		stmt.setString(6,description);
		stmt.setString(7,name);
		stmt.setBoolean(8, interactive); 
		stmt.setString(9,json);
		stmt.setString(10, penalty);
		stmt.setTimestamp(11, softTimeStamp); 
		stmt.setInt(12, asgnmentID);
		stmt.setString(13, courseID); 
		
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
			//dbcon.close();
	%>
</body>
</html>