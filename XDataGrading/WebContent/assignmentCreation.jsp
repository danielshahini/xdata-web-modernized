<%@ page language="java" %>
<%@page import="java.io.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.*" %>

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="errorPage.jsp"%> 
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Add Assignment</title>
</head>
<body>

<!-- Add the assignment details into assignment table -->
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}

	String asgnmentName=request.getParameter("AssignmentName");
	String noOfQuestions=request.getParameter("NumberOfQuestions");
	String startTime=request.getParameter("starttime");
	String endTime=request.getParameter("endtime");
	//get the query related information each in an array
	String[] queryIDs=(String[])request.getParameterValues("quesID");
	//String[] queryIDs={"bob","riche","jacky","rosy"};
	String[] queryText=(String[])request.getParameterValues("quesTxt");
	String[] correctQuery=(String[])request.getParameterValues("query");
/*	
	out.println("asgn Name: "+asgnmentName+"<br>");
	out.println("No Of quest: "+noOfQuestions+"<br>");
	out.println("start Time: "+startTime+"<br>");
	out.println("end Time: "+endTime+"<br>");
	out.println("<br>");
	
	for(int i=0;i<queryIDs.length ; i++){
		out.println("Query ID:  "+queryIDs[i]+"<br>");
		out.println("Query Text:  "+queryText[i]+"<br>");
		out.println("Correct Query:  "+correctQuery[i]+"<br>");
	}
*/

	//Load the PostgreSQL JDBC driver class
	try{
    	Class.forName("org.postgresql.Driver");
	} 
	catch (ClassNotFoundException cnfe){
    	System.out.println("Could not find the JDBC driver!");
    	System.exit(1);
	}

	// JDBC Connection establishment
	Connection dbcon=null;
	try {
     // Class.forName("org.postgresql.Driver");
      	dbcon = (new DatabaseConnection()).dbConnection();
      	if(dbcon!=null){
    	  System.out.println("Connected successfullly");
   	   }
	}
	catch (Exception ex) {
       System.err.println("SQLException: " + ex.getMessage());
	}
	//insert the assignment details into the table
	try{
		PreparedStatement stmt,stmt1,stmt2;
		//assignment schema is:
		//assignID,assignName,noOfQuestions,start time, end time
		//start time and end time have to be of type timestamp
		//FIXME AssignId has to be incremented automatically- TO increment automatically define the attribute as SERIAL 

		stmt = dbcon.prepareStatement("INSERT INTO xdata_assignment VALUES (DEFAULT,?,?,?,?,?)");
		//stmt.setInt(1, asgnmentID);
		stmt.setString(2,asgnmentName);
		stmt.setString(2, noOfQuestions);
		stmt.setString(3, startTime);
		stmt.setString(4, endTime);
		stmt.setString(5, "NC");

		ResultSet rs=stmt.executeQuery();		  
 		//get the assignment id for this
 		stmt1=dbcon.prepareStatement("SELECT max(assignment_id) as assign_id FROM xdata_assignment");
 		/* stmt1.setString(1,asgnmentName);
		stmt1.setString(2, noOfQuestions);
		stmt1.setString(3, startTime);
		stmt1.setString(4, endTime); */
	
		ResultSet rs1=stmt1.executeQuery();
		String assignID;
		//while(rs1.next())
		assignID=rs1.getString("assign_id");
		//now insert query details into query info table
		//query info schema: assignID,queryID,queryText,correctQuery
		for(int i=0;i<queryIDs.length ; i++){
			stmt2=dbcon.prepareStatement("INSERT INTO xdata_instructor_query VALUES (?,?,?,?)");
			stmt2.setString(1,assignID);
			stmt2.setString(2, queryIDs[i]);
			stmt2.setString(3, queryText[i]);
			stmt2.setString(4, correctQuery[i]);
		}
		
		rs.close();
		rs1.close();
	}
	catch(SQLException sep){
	System.out.println("Could not connect to database");
	System.exit(1);
	} 
	finally{
		dbcon.close();
	}

%>
<!--  BELOW CODE IS NOT NEEDED -->
<!-- After adding assignment details, give the form for the instructor to enter the query details -->
<!--  
<hr>
<h3>Add Assignment Question</h3>
<!-- action should add the details into queryinfo table -->
<!-- 
  <Form Method="post" action="addQueryDetails.jsp"> 
  	<table>
  		<tr><td>Question ID</td>
  	  			<td><input type="text" name="quesID"  size="50"></td>
  		</tr>
  		<tr><td>Question Text</td>
  	  			<td><input type="text" name="quesTxt" size="50"></td>
  		</tr>
  		<tr><td>Correct Query</td>
  	  			<td><textarea  name="query" rows="6" cols="57"></textarea></td>
  		</tr>
  </table> 
  		<tr>
  		<!-- <td><input type="button" name="next"  value="Add Question" onclick=""></td> -->
  <!-- 		
  			<td><input type="submit" value="submit"></td>
  		</tr>
  </Form>
 -->

</body>
</html>