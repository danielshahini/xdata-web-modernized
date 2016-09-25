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
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript" src = "scripts/jquery-ui.js"></script>
<script type="text/javascript" src = "scripts/jquery.js"></script>

<script type="text/javascript" src="scripts/wufoo.js"></script>
<link rel="stylesheet" href="css/structure.css" type="text/css" />
<link rel="canonical"
	href="http://www.wufoo.com/gallery/designs/template.html">
<title>Delete Data Files</title>
<script>
$(document).ready(function(){
	$('#sampleDataDelForm').submit(function() {
	    
	    if( $("input.select:checked").length > 0){
	    	return true;
	    	} 
	    else {alert("Please select data to delete"); return false;}

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
}

		//String [] checkedIds = request.getParameterValues("deleteSampleData");
String ids =  request.getParameter("schema_id"); 
		String s = ids.substring(0,ids.indexOf("-"));
		String sampledata_id = ids.substring(ids.indexOf("-")+1);
		System.out.println("SAMPLE DATA ID = " + sampledata_id);
		String courseID = (String) request.getSession().getAttribute("context_label");
		//get connection
		try {
			Connection dbcon = (new DatabaseConnection()).dbConnection();		
			PreparedStatement stmt,stmt1;
			//for(String s: checkedIds){
				  int i=Integer.parseInt(s);
					//stmt = dbcon
					//		.prepareStatement("update xdata_schemainfo set sample_data = null, sample_data_name=null where course_id = ? and schema_id = ?");
				  stmt = dbcon.prepareStatement("delete from xdata_sampledata where course_id = ? and schema_id = ? and sampledata_id = ?");
					stmt.setString(1, courseID);
					stmt.setInt(2, i);
					stmt.setInt(3,Integer.parseInt(sampledata_id));
					 stmt.executeUpdate(); 
				
					System.out.println("Deleted : Schema Id : "+s +"  :: sampledata_id :: "+sampledata_id);
					
		//		} 
			dbcon.close(); 
			}catch (Exception err) { 
				err.printStackTrace();
				throw new ServletException(err); 
			} 							
		PrintWriter out_print=response.getWriter(); 
		response.sendRedirect("sampleDataUpload.jsp?Delete=true");

	%>
</body>
</html>