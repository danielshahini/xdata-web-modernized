<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@ page import="java.io.*"%> 
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@ page import="java.text.*"%>
<%@page import="database.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}else if(session.getAttribute("LOGIN_USER") != null && !session.getAttribute("LOGIN_USER").equals("ADMIN")
 		&& session.getAttribute("role") != null && !session.getAttribute("role").equals("instructor")){
	response.sendRedirect("index.jsp?NotAuthorised=true");
	session.invalidate();
	return;
}


String s= request.getParameter("schema_id");

String courseID = (String) request.getSession().getAttribute("context_label");
//get connection
try {
	Connection dbcon = (new DatabaseConnection()).dbConnection();		
	PreparedStatement stmt,stmt1;
	//for(String s: checkedIds){
		  int i=Integer.parseInt(s);
			stmt = dbcon
					.prepareStatement("delete FROM xdata_schemainfo where course_id = ? and schema_id= ?");
			stmt.setString(1, courseID);
			stmt.setInt(2,i);
			stmt.execute();
		 
			System.out.println("Deleted : Schema Id : "+s);
			
	//	} 
	dbcon.close(); 
	}catch (Exception err) {
		err.printStackTrace();
		throw new ServletException(err); 
	} 							
PrintWriter out_print=response.getWriter();
response.sendRedirect("schemaUpload.jsp?Delete=true");

%>
</body>
</html>