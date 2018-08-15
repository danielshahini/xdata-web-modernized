<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="errorPage.jsp"%> 
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.*"%>

<%
//Set session with role value for that course for tat user ID  and 
//then forward to that home page based on course selected and user role on that course
String userId = (String)session.getAttribute("user_id");
String course = (String)request.getParameter("contextLabel");
session.setAttribute("context_label",course);
//System.out.println("Data for forwarding request , userId = " + userId + " - course = "+course);		
Connection dbcon = null;
String role = "";
if(session.getAttribute("role") != null){
	session.removeAttribute("role");
}

try{
	dbcon = (new DatabaseConnection()).dbConnection();
	PreparedStatement stmt;
	stmt = dbcon 
			.prepareStatement("SELECT * FROM  xdata_roles where internal_user_id =? and course_id = ?");
	stmt.setString(1,userId);
	stmt.setString(2,course);
	ResultSet rs = stmt.executeQuery();
	if(rs.next()){
		role = rs.getString("role");		
		
	}
	session.setAttribute("role", role);
	if(role.equalsIgnoreCase("instructor")){ 
		RequestDispatcher rd = request.getRequestDispatcher("InstructorHome.jsp");
 		rd.forward(request, response);
		
	}
	else if(role.equalsIgnoreCase("tester")){
		RequestDispatcher rd = request.getRequestDispatcher("TesterHome.jsp");
 		rd.forward(request, response);
	}
	else if(role.equalsIgnoreCase("student")){
		RequestDispatcher rd = request.getRequestDispatcher("StudentHome.jsp?contextLabel="+course);
 		rd.forward(request, response);
	
	}
} catch (Exception err) {

				err.printStackTrace();
				//out.println("Error in getting list of assignments");
				throw new ServletException(err);
				
			}
			finally{
				dbcon.close();
}%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Select Home page to forward request</title>
<style>
</style>
</head>
<body>
<% 
String courseId = (String)request.getParameter("contextLabel");%>
<input type ="hidden" id="cntxtLabel" name ="contextLabel" value="<%=courseId%>">
</body>
</html>