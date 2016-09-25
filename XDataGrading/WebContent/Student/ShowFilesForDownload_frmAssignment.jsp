<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>
<html>
<head> 
 <link rel="stylesheet" href="../css/structure.css" type="text/css"/>
 <script type="text/javascript" src = "../scripts/jquery.js"></script>
<link rel="canonical"
	href="http://www.wufoo.com/gallery/designs/template.html">

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script>
$( document ).ready(function() { 		
	 $('#assignment').val('select').prop('selected',true);
	 
	 $('#downloadForm').submit(function() {
		    var data = $('#assignment option:selected').val();
		   
		    if(data == "select"){
		    	alert("Please select a assignment.");
		   	return false;
		    }
		    else{
		    	return true;
		    }
		});
	});


</script>
<title>Download data files</title>
</head>
<body>
	<div class="fieldset">
				<fieldset>
					<legend> Select Assignment </legend>
					 <form name="downloadForm" id="downloadForm" action="DownloadFiles" method="post">
					<%
						String courseId = (String) request.getSession().getAttribute(
											"context_label");
					   String schema_id ="";
					   String request_from ="";
					   Connection dbcon = (new DatabaseConnection()).dbConnection();
									try {

										PreparedStatement stmt;
										stmt = dbcon
												.prepareStatement("SELECT assignment_id, assignmentname from assignment where course_id = ?");
										stmt.setString(1, courseId);

										String output = "";
										ResultSet rs = stmt.executeQuery();
										output += "<option value=\"select\">Select</option>";
										//TODO -  Get DB user and Test user and show it in the drop down
										// CLARIFY : How it will b shown for various Schema's
										// On select schema, show the user name options??????
										while (rs.next()) { 
											output += " <option value = \""
													+ rs.getInt("assignment_id") + "\"> "
													+ rs.getInt("assignment_id") + "-"
													+ rs.getString("assignmentname") + " </option> ";
										}			 	 
										rs.close();
										out.println("<div><label>Assignment: </label><br/> <br><select id=\"assignment\" name=\"assignment\" style=\"clear:both;\"> "
												+ output + "</select></div><br/>");
									
										out.println("<input type = \"submit\" value=\"Submit\">");
									} catch (Exception err) {
										err.printStackTrace();
										throw new ServletException(err);
									}
									finally{
										dbcon.close();
									}
					%>
					</form> 
		</fieldset></div>
</body>
</html>