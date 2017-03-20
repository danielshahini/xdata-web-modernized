<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>
 <script type="text/javascript" src = "scripts/jquery-ui.js"></script>
<script type="text/javascript" src = "scripts/jquery.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Delete Course</title>
</head>
<body>
<%		
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}else if(session.getAttribute("LOGIN_USER") != null && !session.getAttribute("LOGIN_USER").equals("ADMIN")
 		&& session.getAttribute("role") != null && !session.getAttribute("role").equals("admin")){
	response.sendRedirect("index.jsp?NotAuthorised=true");
	session.invalidate();
	return;
}

		String courseId = request.getParameter("courseId");
		Connection dbcon = null;
		try {
			dbcon = (new DatabaseConnection()).dbConnection();
			PreparedStatement stmt,stmt1;
			 int id=Integer.parseInt(courseId);
				stmt = dbcon
						.prepareStatement("delete FROM  xdata_course where course_id = ?");
				stmt.setInt(1, id);
				stmt.execute();
					
				System.out.println("Deleted Course Id : "+id);
			dbcon.close(); 
			}catch (Exception err) {
				err.printStackTrace();
				throw new ServletException(err);
			} 
		finally{
			dbcon.close();
		}
		response.sendRedirect("ViewCourseList.jsp");
		%>
</body>
</html>