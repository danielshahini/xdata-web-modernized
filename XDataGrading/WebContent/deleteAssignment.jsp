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
<script>
$(document).ready(function(){
$('#assgnmentDelForm').submit(function() {
    
    if( $("input.select:checked").length > 0){return true;} 
    else {alert("Please select assignments to delete"); return false;}

}); 
}); 
</script>
<title>Delete Assignment</title>
<style>
table, tr, td {
    border: 0px;
}
</style>

</head>

<body>
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}

	//[] checkedIds = request.getParameterValues("deleteAssignment");
 
		String s = request.getParameter("assignmentid"); 
		String courseID = (String) request.getSession().getAttribute("context_label");
		Connection dbcon = null;
		//get connection
		try {
			dbcon = (new DatabaseConnection()).dbConnection();
			String output = "<table border=\"0\">";
			PreparedStatement stmt,stmt1;
		//	for(String s: checkedIds){
				  int i=Integer.parseInt(s);
					stmt = dbcon
							.prepareStatement("delete FROM xdata_assignment where course_id = ? and assignment_id= ?");
					stmt.setString(1, courseID);
					stmt.setInt(2,i);  
					stmt.execute();
					//Delete queries related to the deleted assignment
					stmt1 = dbcon.prepareStatement("delete from xdata_qinfo where course_id = ? and assignment_id= ?");
					stmt1.setString(1, courseID);
					stmt1.setInt(2,i); 
					stmt1.execute(); 
				
					stmt1 = dbcon.prepareStatement("delete from xdata_instructor_query where course_id = ? and assignment_id= ?");
					stmt1.setString(1, courseID);
					stmt1.setInt(2,i); 
					stmt1.execute(); 
					
					//stmt1 = dbcon.prepareStatement("delete from queryinfo where assignment_id= ?");
					//stmt1.setInt(1, i);
					//stmt1.setInt(2,i); 
					//stmt1.execute(); 
				
					
					System.out.println("Deleted : Assignment Id : "+s);
					 
				//}
			dbcon.close(); 
			}catch (Exception err) {
				err.printStackTrace();
				throw new ServletException(err);
			} 			
		finally{
			dbcon.close();
		}
		PrintWriter out_print=response.getWriter();
		response.sendRedirect("ListAllAssignments.jsp");
		%>
</body>
</html>