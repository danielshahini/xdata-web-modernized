<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>
<html> 
 <link rel="stylesheet" href="../css/structure.css" type="text/css"/>
 <script type="text/javascript" src = "../scripts/jquery.js"></script>
 <link rel="canonical"
	href="http://www.wufoo.com/gallery/designs/template.html">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Download data files</title>
</head>
<body>
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}
%>
	<div class="fieldset">
		<fieldset>
			<legend> File Download </legend>
<form name="downloadForm" id="downloadForm" action="DownloadFiles" method="post">
<table align="left" width="75%">
<br>
 Please click on the file name to download
 <p></p>
 <tr align="center" style='background-color: #f0f0f0;'>
 <td>Assignment Name</td> <td> Schema File</td> <td>Data File</td></tr>
  
<%
String courseId = (String) request.getSession().getAttribute(
		"context_label");
int assignment_id = 0;
String name = "";
int defaultschemaid = 0;
int schemaIdPresent = 0;
int schemaId = 0;
int optionalschemaid =0;
Connection dbcon = (new DatabaseConnection()).dbConnection();
try {

	PreparedStatement stmt,stmt1,stmt2;
	
	stmt = dbcon.prepareStatement("select assignment_id, assignmentname, defaultschemaid from assignment where course_id = ?");
	stmt2 = dbcon.prepareStatement("select optionalschemaid from qinfo where course_id = ? and assignment_id = ?");
	
	stmt.setString(1,courseId);
	
	ResultSet rs = stmt.executeQuery();
	
	while(rs.next()){
		
		assignment_id = rs.getInt("assignment_id");
		defaultschemaid = rs.getInt("defaultschemaid");
		name = rs.getString("assignmentname");
		schemaId = defaultschemaid;
		stmt1= dbcon.prepareStatement("SELECT schema_name,ddltext,sample_data_name,sample_data from schemainfo where course_id=? and schema_id = ?");
		stmt1.setString(1,courseId);
		stmt1.setInt(2,schemaId);
		ResultSet rs1 = stmt1.executeQuery();
		rs1.next();
		%>
	<tr>
		<td><%=name %></td>
		<td><a href="DownloadFile.jsp?schemaId=<%=schemaId%>&download=schema"><%=rs1.getString("schema_name")%></a>
				<td><a href = "DownloadFile.jsp?schemaId=<%=schemaId%>&download=sampledata"><%=rs1.getString("sample_data_name") %></a>
				</td>
		</tr>
		<%
		stmt2.setString(1,courseId);
		stmt2.setInt(2,assignment_id);
		ResultSet rs2 = stmt2.executeQuery();
		
		while(rs2.next()){
			optionalschemaid =rs2.getInt("optionalschemaid");
			if(optionalschemaid != defaultschemaid){
				stmt1= dbcon.prepareStatement("SELECT schema_name,ddltext,sample_data_name,sample_data from schemainfo where course_id=? and schema_id = ?");
				stmt1.setString(1,courseId);
				stmt1.setInt(2,optionalschemaid);
				ResultSet rs3 = stmt1.executeQuery();
				rs3.next();
				
		%><tr><td ></td>
		
				<td><a href="DownloadFile.jsp?schemaId=<%=optionalschemaid%>&download=schema"><%=rs3.getString("schema_name")%></a>
				<td><a href = "DownloadFile.jsp?schemaId=<%=optionalschemaid%>&download=sampledata"><%=rs3.getString("sample_data_name") %></a>
				</td>
		</tr>	
				<%
			}
		}
	}		
		
		
	} catch (Exception err) {
		err.printStackTrace();
		throw new ServletException(err);
	}
	finally{
		
		dbcon.close();
	}

			
	/*ResultSet rs1 = stmt1.executeQuery();
	while(rs1.next()){
		schemaIdPresent = rs1.getInt("schema_id");
		stmt = dbcon
				.prepareStatement("SELECT a.assignment_id,a.assignmentname,a.defaultschemaid,q.optionalschemaid from assignment a,qinfo q where a.course_id = ? and a.assignment_id = q.assignment_id");
		stmt.setString(1, courseId);
		String output = "";
		ResultSet rs = stmt.executeQuery();
		
		while(rs.next()){
			assignment_id = rs.getInt("assignment_id");
			name = rs.getString("assignmentname");
			defaultschemaid = rs.getInt("defaultschemaid");
			optionalschemaid = rs.getInt("optionalschemaid");
			if(defaultschemaid != optionalschemaid){
				schemaId = optionalschemaid;
			}
			else{
				schemaId = defaultschemaid;
			}*/
	
%>
 
 </table>
</form> 
</fieldset>
</div>
</body>
</html>