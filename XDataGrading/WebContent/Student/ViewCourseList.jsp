<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%> 
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head> 
<script type="text/javascript" src = "scripts/jquery.js"></script>
<link rel="stylesheet" href="css/structure.css" type="text/css" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>View Courses</title>
<style>
table, tr, td {
    border: 0px;
}
</style> 
<script>
function onSubmit(id){
	
	var a = confirm('Are you sure you want to delete the course?');
	if(a==1) 
		window.location.href="deleteCourse.jsp?courseId="+id;
	else
		return false; 
}  
function sendToStudent(courseId,path){ 
	window.top.location.href = path+"/StudentHome.jsp?contextLabel="+courseId;
} 
</script>

</head>
<body>
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}
%>
<div>
		<div class="fieldset">
			<fieldset>
				<legend>Course List</legend>
				<form name="form1" method="post" action="StudentHome.jsp" target="_top">
				<% 
				if (session.getAttribute("LOGIN_USER") == null) {
					response.sendRedirect("index.jsp?TimeOut=true");
					return;
				}
				
				Connection dbcon = null;
				int index=0; 
				String output = "";
				String role = (String)session.getAttribute("roles");
				try{
				dbcon = (new DatabaseConnection()).dbConnection();
				PreparedStatement stmt;
				stmt = dbcon 
						.prepareStatement("SELECT * FROM  xdata_course");
		
				ResultSet rs = stmt.executeQuery();
				session.removeAttribute("contextLabel");
				while(rs.next()){
					
				//	 request.getSession().setAttribute("context_label",rs.getString("course_id"));
					index++;
					output +="<table border=\"0\" style=\"table-layout:fixed;\" width=\"100%\">";
					//Row 1 - column 1- index value, column 2- Course Id, column 3-course name,
					//column 4 - buttons
					output +=	"<tr><td width =\"2%\">"+index +".</td>";
					output += "<input type = \"hidden\" name=\"contextLabel\" value=\"" +rs.getString("instructor_course_id")+"\">";
					  
					output += "<td class=\"wrapword\" width=\"40%\"><b>Course Id: </b>"+rs.getString("instructor_course_id")+"</td>";
					output +=   "<td></td><td class=\"wrapword\" width=\"35%\"> <b>Name: </b>"+rs.getString("course_name")+"</td>";
					 
								//+"<br><b>Description: </b>"+rs.getString("description") +"</td>";
					output += "<td></td><td width=\"8%\"><input name=\"View\" type=\"button\" value=\"View\" onclick=\"document.form1.submit()\"></td>";
					
			 
					//System.out.println("COURSE _ID IN VIEW : "+rs.getString("course_id"));
				
					//window.location.href='asgnmentList.jsp?assignment_id="+ rs.getString("assignment_id")
					output+="</tr>"; 
					//Row 2
					output +=   "<tr><td></td><td class=\"wrapword\"><b>Year: </b>"+rs.getString("year") +"&nbsp;&nbsp;<b>Semester: </b>"+rs.getString("semester") +"</td>";
					output +=   "<td></td><td class=\"wrapword\"><b>Description: </b>"+rs.getString("description") +"</td></tr>";
					   
					//System.out.println("output String Val === "+output);
				  
				} 
				out.println(output);
				rs.close(); 
				stmt.close();
			} catch (Exception err) {

				err.printStackTrace();
				//out.println("Error in getting list of assignments");
				throw new ServletException(err);
				
			}
			finally{
				dbcon.close();
			}%>

				
	</fieldset></div></div>									

</body>
</html>