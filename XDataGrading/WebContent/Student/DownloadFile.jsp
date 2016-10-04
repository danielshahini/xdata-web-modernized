<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@ page import="java.io.*"%>
<%@page import="javax.servlet.*"%>
<%@page import="javax.servlet.ServletOutputStream"%>
 <%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>
 
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script>
</script>
<title>Insert title here</title>
</head>
<body>

<%

String courseId = (String) request.getSession().getAttribute(
		"context_label");
String download = request.getParameter("download"); 
String sampledataText = "";
String schemaText = "";
String downloadString = "";
String downloadFileName = "";
int schemaId = Integer.parseInt(request.getParameter("schemaId"));
int BUFSIZE = 4096;
InputStream inputStream = null;
Connection dbcon = (new DatabaseConnection()).dbConnection();
try {

	PreparedStatement stmt1;
	stmt1= dbcon.prepareStatement("SELECT schema_name,ddltext from xdata_schemainfo where course_id=? and schema_id =?");
	stmt1.setString(1,courseId);
	stmt1.setInt(2,schemaId);
	ResultSet rs1 = stmt1.executeQuery();
	if(rs1.next()){
		if(download.equalsIgnoreCase("schema")){
			downloadString=rs1.getString("ddltext");
			inputStream = rs1.getBinaryStream("ddltext");
			downloadFileName = rs1.getString("schema_name");
		}
		else if(download.equalsIgnoreCase("sampledata")){
			
				try(PreparedStatement stmt2= dbcon.prepareStatement("SELECT sample_data,sample_data_name from xdata_sampledata where course_id=? and schema_id =?")){
					stmt2.setString(1,courseId);
					stmt2.setInt(2,schemaId); 
					try(ResultSet rs2 = stmt2.executeQuery()){
					while(rs2.next()){
						//Get data file String, create a file and put the contents.Add to list
						downloadString=rs2.getString("sample_data");
						downloadFileName = rs2.getString("sample_data_name"); 
						inputStream = rs2.getBinaryStream("sample_data");
					}
					}
				}
			}
	}
	rs1.close();
	stmt1.close();
ServletOutputStream outStream = response.getOutputStream();
response.setContentType("text/html");
response.setContentLength((int)downloadString.length());
int length = downloadString.length();
String fileName = downloadFileName;
response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
 
byte[] byteBuffer = new byte[BUFSIZE];
DataInputStream in = new DataInputStream(inputStream);
 
while ((in != null) && ((length = in.read(byteBuffer)) != -1))
{
	outStream.write(byteBuffer,0,length);
}
 
in.close();
outStream.close();
}catch(Exception e){
	throw new ServletException(e);
}
finally{
	
	dbcon.close();
}
%>


</body>
</html>