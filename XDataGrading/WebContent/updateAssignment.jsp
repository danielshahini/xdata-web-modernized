<%@page import="com.google.gson.Gson"%>
<%@page import="java.util.Date"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@page import="java.io.*"%>
<%@page import="java.text.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>
<%@page import="database.UpdateServlet"%>
<head>
<link rel="stylesheet" href="css/structure.css" type="text/css" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Create Assignment</title>
<script type="text/javascript" src="scripts/newrow.js"></script>
<script type="text/javascript" src="scripts/wufoo.js"></script>
<script type="text/javascript" src="scripts/ManageQuery.js"></script>

<style>
textarea, select {
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

label span, .required {
	color: red;
	font-weight: bold;
}

nav ul li {
	float: left;
}

nav ul li:hover {
	background: #4b545f;
	background: linear-gradient(top, #4f5964 0%, #5f6975 40%);
	background: -moz-linear-gradient(top, #4f5964 0%, #5f6975 40%);
	background: -webkit-linear-gradient(top, #4f5964 0%, #5f6975 40%);
}
</style>

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

	//getting parameters 
	String name = request.getParameter("assignmentName");
	String description = request.getParameter("description");
	String courseId = (String) request.getSession().getAttribute(
	"context_label");
	boolean interactive = false;
	boolean softdateselected = false;
	if(request.getParameter("interactive") != null){
		interactive = true;	
	}
	String softdate = "";
	String penalty = "";
	if(request.getParameter("softdeadlineselectname") != null){
		softdateselected = true;
		softdate=request.getParameter("soft");
		penalty=request.getParameter("penalty");
	}
	String assignID = (String) request.getSession().getAttribute(
	"resource_link_id");
	
	String startDate = request.getParameter("start");
	String endDate = request.getParameter("end");
	String[] defaultDSId = request.getParameterValues("defaultDSId");
	//System.out.println("chk values : " + defaultDSId.length + "--"+defaultDSId[0]);
	int schemaId = 0;
	if(request.getParameter("schemaid") != null && ! request.getParameter("schemaid").equalsIgnoreCase("select")){
		schemaId = Integer.parseInt(request.getParameter("schemaid"));
	}else{
		throw new ServletException("Please select the schema");
	}
	int connectionId = Integer.parseInt(request.getParameter("dbConnection"));
	int newAssignmentId = 1;
	Connection dbcon = null;

	dbcon = (new DatabaseConnection()).dbConnection();

	try {
		PreparedStatement stmt;
		
		//Get assignment id 
		stmt =  dbcon.prepareStatement("SELECT MAX(assignment_id) as assignId from xdata_assignment");
		ResultSet rs = stmt.executeQuery();
		
		if(rs.next()){
			newAssignmentId = rs.getInt("assignId") + 1;
		}
		
		Gson gson = new Gson();
		String json = "";
		if(defaultDSId != null){
			gson.toJson(defaultDSId);
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm");
	    Date parsedDate = dateFormat.parse(startDate);
	    System.out.println(parsedDate.toString());
	    Timestamp startTimeStamp = new java.sql.Timestamp(parsedDate.getTime());
	    
	    parsedDate = dateFormat.parse(endDate);
	    Timestamp endTimeStamp = new java.sql.Timestamp(parsedDate.getTime()); 
	  
		//insert into assignment
		if(softdateselected == true)
		{
			parsedDate = dateFormat.parse(softdate);
		    Timestamp softTimeStamp = new java.sql.Timestamp(parsedDate.getTime());
			stmt = dbcon
			.prepareStatement("INSERT INTO xdata_assignment VALUES (?,?,?, ?, ?, ?, ?, ?, ?,?,?,?,?)");
	
			stmt.setString(1, courseId); 
			stmt.setInt(2, newAssignmentId);
			stmt.setString(3, description);
			stmt.setTimestamp(4, startTimeStamp);
			stmt.setTimestamp(5, endTimeStamp);
			stmt.setBoolean(6, interactive);
			stmt.setInt(7, connectionId);
			stmt.setInt(8, schemaId); 
			stmt.setString(9,name);
	 		stmt.setString(10,json);
	 		stmt.setBoolean(11,false);
	 		stmt.setTimestamp(12,softTimeStamp);
	 		stmt.setString(13,penalty);
			stmt.executeUpdate();
			stmt.close();
		}
		else
		{
			stmt = dbcon.prepareStatement("INSERT INTO xdata_assignment VALUES (?,?,?, ?, ?, ?, ?, ?, ?,?)");
			
					stmt.setString(1, courseId); 
					stmt.setInt(2, newAssignmentId);
					stmt.setString(3, description);
					stmt.setTimestamp(4, startTimeStamp);
					stmt.setTimestamp(5, endTimeStamp);
					stmt.setBoolean(6, interactive);
					stmt.setInt(7, connectionId);
					stmt.setInt(8, schemaId); 
					stmt.setString(9,name);
			 		stmt.setString(10,json);
					stmt.executeUpdate();
					stmt.close();
		}
		rs.close();

	}
	catch (SQLException sep) {
		System.out.println("Could not connect to database: " + sep);
		sep.printStackTrace();
		throw new ServletException(sep);
		//System.exit(1);
	}
	finally{
		dbcon.close();
	}
	String url = "";
	if(!((String)request.getSession().getAttribute("LOGIN_USER")).equalsIgnoreCase("tester")){
		url = "asgnmentList.jsp?assignmentId="+ newAssignmentId;
	}else{
		url = "ListOfTesterQuestions.jsp?AssignmentID="+ newAssignmentId;
	}
	response.sendRedirect(url);
%>

</body>
</html>