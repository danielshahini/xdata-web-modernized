<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
    <%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "https://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript" src = "scripts/jquery.js"></script>
<link rel="stylesheet" href="css/structure.css" type="text/css" />
<link rel="canonical"
	href="https://www.wufoo.com/gallery/designs/template.html">
 
<title>Select DB Connection</title>
<style>
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
<script>

function getParameterByName(name) { 
	
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}  

$( document ).ready(function() { 		
 $('#dbConnection').val('select').prop('selected',true);
 
 $('#uploadTest').submit(function() {
	    var data = $('#dbConnection option:selected').val();
	   
	    if(data == "select"){
	    	alert("Please select a connection.");
	   	return false;
	    }
	    else{
	    	return true;
	    }
	});
});
</script> 
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

%>
<div id="breadcrumbs"> 
  <a style='color:#353275;text-decoration: none;' href="../CourseHome.jsp" target="_top">Home</a> &nbsp; >> &nbsp;
  <a href="InstructorHome.jsp?contextLabel=<%=(String) request.getSession().getAttribute("context_label")%>" style='color:#0E0E0E;text-decoration: none;font-weight: normal;' target="_top"><%=(String) request.getSession().getAttribute("context_label")%></a>&nbsp; >> &nbsp;
  
  <%if(request.getParameter("requestingPage") != null && request.getParameter("requestingPage").equals("schemaUpload")){ %>
   <a style='color:#353275;text-decoration: none;' href="schemaUpload.jsp" target="_self">Schema Upload</a>&nbsp; >> &nbsp;
   <%}else if(request.getParameter("requestingPage") != null && request.getParameter("requestingPage").equals("sampleDataUpload")){ %>
    <a style='color:#353275;text-decoration: none;' href="javascript:history.go(-1)">Sample Data Upload</a>&nbsp; >> &nbsp;
   <%} %>
   <a href="#" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>Test DB Connection for upload</a>
 </div>

<br/>
	<div class="fieldset">
				<fieldset>
					<legend> Select Connection </legend>
					<br/>
					 <form name="uploadTest" id="uploadTest" action="TestUploadedFile" method="post">
					<%
						String courseId = (String) request.getSession().getAttribute(
											"context_label");
					   String s = request.getParameter("schema_id");
					   String schema_id ="";
					   String request_from ="";
					   String sampledata_id = "";
					   if(s != null){
						  // System.out.println("SCHEMA_ID in connectionToTestSQL = "+s);
						   schema_id = s.substring(0, s.indexOf("-"));
						   sampledata_id = s.substring(s.indexOf("-"),s.lastIndexOf("-"));
						   request_from = s.substring(s.lastIndexOf("-")+1);
					   }
					   //String request_from = request.getParameter("requestingPage");
									//get the connection for testing1
									Connection dbcon = (new DatabaseConnection()).dbConnection();
									try {

										PreparedStatement stmt;
										stmt = dbcon
												.prepareStatement("SELECT connection_id,connection_name FROM "+
														" xdata_database_connection WHERE course_id = ?");
										stmt.setString(1, courseId);

										String output = "";
										ResultSet rs = stmt.executeQuery();
										output += "<option value=\"select\">Select</option>";
										//TODO -  Get DB user and Test user and show it in the drop down
										// CLARIFY : How it will b shown for various Schema's
										// On select schema, show the user name options??????
										while (rs.next()) { 
											output += " <option value = \""
													+ rs.getInt("connection_id") + "\"> "
													+ rs.getInt("connection_id") + "-"
													+ rs.getString("connection_name") + " </option> ";
										}
									 	 
										rs.close();
 			
										out.println("<div><label>Database Connection</label><br/> <br><select id=\"dbConnection\" name=\"dbConnection\" style=\"clear:both;\"> "
												+ output + "</select></div><br/>");
										out.println("<input type =\"hidden\" name=\"schema_id\" value=\" "+schema_id+" \">");
										out.println("<input type =\"hidden\" name=\"sampledata_id\" value=\" "+sampledata_id+" \">");
										
										out.println("<input type = \"hidden\" name=\"requestingPage\" value =\" "+request_from+"\">");
										out.println("<input type = \"submit\" value=\"Test\">");
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