<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>
<%@page import="java.io.*"%>
<%@page import="java.text.*"%>
<%@page import="java.sql.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>UPdate Database Connection</title>
<style>
</style>
</head> 
<body>

	<%
		if (session.getAttribute("LOGIN_USER") == null || !session.getAttribute("LOGIN_USER").equals("ADMIN")) {
		response.sendRedirect("index.jsp");
		return;
			}else if(session.getAttribute("LOGIN_USER") != null && !session.getAttribute("LOGIN_USER").equals("ADMIN")
			 		&& session.getAttribute("role") != null && (!session.getAttribute("role").equals("instructor") || !session.getAttribute("role").equals("tester"))){
				response.sendRedirect("index.jsp?NotAuthorised=true");
				session.invalidate();
				return;
			}
		
			/**get parameters*/
			String courseId = (String) request.getSession().getAttribute(
			"context_label");
			String dbName = (String) request.getParameter("dbName");
			String databaseType = (String) request.getParameter("databaseType");
			//String schemaid = (String) request.getParameter("schemaid");
			String jdbcurl = (String) request.getParameter("jdbcurl");
			String dbuserName = (String) request.getParameter("dbuserName");
			String dbPassword = (String) request.getParameter("dbPassword");
			String testUserName = (String) request.getParameter("testUserName");
			String testPassword = (String) request.getParameter("testPassword");
			//String port = (String)request.getParameter("port");
			
			Connection dbcon = null;

			dbcon = (new DatabaseConnection()).dbConnection();

		try {
		PreparedStatement stmt;
		stmt = dbcon
				.prepareStatement("INSERT INTO xdata_database_connection VALUES (?,DEFAULT,?,?,?,?,?,?,?)");

		stmt.setString(1, courseId);
		stmt.setString(2, dbName);
		stmt.setString(3, databaseType);
		stmt.setString(4, jdbcurl);
		stmt.setString(5, dbuserName);
		stmt.setString(6, dbPassword);
		stmt.setString(7, testUserName);
		stmt.setString(8, testPassword);
		

		stmt.executeUpdate();
		out.println("<marquee>Updated Successfully</marquee>");

		
			} catch (SQLException sep) {
		System.out.println("Could not connect to database: " + sep);
		out.println("<marquee>Error in updating</marquee>");
		throw new ServletException(sep);
		//System.exit(1);
			}
		dbcon.close();
	%>


</body>
</html>
