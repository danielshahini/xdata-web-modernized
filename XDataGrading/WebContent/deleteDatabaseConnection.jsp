<%@ page language="java" contentType="text/html; charset=UTF-8"
      pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<head>
<script type="text/javascript" src = "scripts/jquery-ui.js"></script>
<script type="text/javascript" src = "scripts/jquery.js"></script>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript" src="scripts/wufoo.js"></script>
<link rel="stylesheet" href="css/structure.css" type="text/css" />
<link rel="canonical"
	href="http://www.wufoo.com/gallery/designs/template.html">
	 
<title>Delete Database Connection</title>
<script>
$(document).ready(function(){
	$('#dbDeleteForm').submit(function() {
	    
	    if( $("input.select:checked").length > 0){
	    	return true;
	    	} 
	    else {alert("Please select DB connections to delete"); return false;}

	});  
}); 
</script>
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
}else if(session.getAttribute("LOGIN_USER") != null && !session.getAttribute("LOGIN_USER").equals("ADMIN")
 		&& session.getAttribute("role") != null && !session.getAttribute("role").equals("instructor")){
	response.sendRedirect("index.jsp?NotAuthorised=true");
	session.invalidate();
	return;
}


		//String[] checkedIds = request.getParameterValues("deleteConnection");
		String s = request.getParameter("connection_id");
		String courseID = (String) request.getSession().getAttribute("context_label");
		//get connection
		try {
			Connection dbcon = (new DatabaseConnection()).dbConnection();		
			PreparedStatement stmt,stmt1;
		//	for(String s: checkedIds){
				  int i=Integer.parseInt(s);
					stmt = dbcon 
							.prepareStatement("delete FROM xdata_database_connection where course_id = ? and connection_id= ?");
					stmt.setString(1, courseID); 
					stmt.setInt(2,i);
					stmt.execute(); 					
				//}  
			dbcon.close(); 
			}catch (Exception err) {
				err.printStackTrace();
				throw new ServletException(err);
			} 							
		PrintWriter out_print=response.getWriter();
		response.sendRedirect("NewDatabaseConnection.jsp?Delete=true");
	
	%>
</body>
</html>