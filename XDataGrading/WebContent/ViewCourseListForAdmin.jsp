<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%> 
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "https://www.w3.org/TR/html4/loose.dtd">
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
	window.top.location.href = path+"/selectMode.jsp?contextLabel="+courseId;
} 
 
function setSessionParam(id){	
	document.getElementById("hdnContextLabel").value = id;
	//alert("--"+id+"--"); 
	document.forms[0].submit();
}

</script>
</head>
<body>
<%
if(session.getAttribute("LOGIN_USER") == null){
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}
else if(session.getAttribute("LOGIN_USER") != null && !session.getAttribute("LOGIN_USER").equals("ADMIN")
	&& session.getAttribute("role") != null && !session.getAttribute("role").equals("admin")){
	response.sendRedirect("index.jsp?NotAuthorised=true");
	session.invalidate();
	return;
}
%>
<div>
		<div class="fieldset">
			<fieldset>
				<legend>Course List</legend>
				<form name="form1" method="post" action="selectMode.jsp" target="_top">
				<%
			//	Connection dbcon = null;
				int index=0; 
				String output = "";
				String userId = (String)session.getAttribute("user_id");
				String role = (String)session.getAttribute("role");
				try(Connection dbcon = (new DatabaseConnection()).dbConnection()){
				//dbcon = (new DatabaseConnection()).dbConnection();
				PreparedStatement stmt;
				//Select courses to display for the logged in user
				if( role!= null && role.equalsIgnoreCase("admin")){
					stmt = dbcon.prepareStatement("select * from xdata_course;");
				} 
				else{
					stmt = dbcon
							.prepareStatement("select * from  xdata_course xc inner join xdata_roles xr on xc.instructor_course_id=xr.course_id  where internal_user_id=?");
						stmt.setString(1,userId); 
				}
				
				ResultSet rs = stmt.executeQuery();
				while(rs.next()){
					session.removeAttribute("contextLabel");
				//	 request.getSession().setAttribute("context_label",rs.getString("course_id"));
					index++;
					output +="<table border=\"0\" style=\"table-layout:fixed;\" width=\"100%\">";
					//Row 1 - column 1- index value, column 2- Course Id, column 3-course name,
					//column 4 - buttons
					output +=	"<tr><td width =\"2%\">"+index +".</td>";
					output += "<input type = \"hidden\" id=\"hdnContextLabel\" name=\"contextLabel\">";
				//	output += "**"+rs.getString("instructor_course_id") + "**";
					output += "<td class=\"wrapword\" width=\"36%\"><b>Course Id: </b>"+rs.getString("instructor_course_id")+"</td>";
					output +=   "<td></td><td class=\"wrapword\" width=\"35%\"> <b>Name: </b>"+rs.getString("course_name")+"</td>";
					 
								//+"<br><b>Description: </b>"+rs.getString("description") +"</td>";
				//	if(role.equalsIgnoreCase("instructor") || role.equalsIgnoreCase("admin")){			
				//output += "<td></td><td width=\"8%\"><input name=\"View\" type=\"button\" id=\""+rs.getString("instructor_course_id")+"\" value=\"View\" onclick=\"setSessionParam(this.id)\"></td>";
				//	} 
				//	else{
					//	output += "<td></td><td width=\"8%\">";
						//output += "<input name=\"View\" type=\"button\" value=\"View\" onclick=\"\"></td>";
					//} 
					//if(role.equalsIgnoreCase("instructor") || role.equalsIgnoreCase("admin")){
					   output += "<td></td><td width=\"8%\"><input name=\"Edit\" type=\"button\" value=\"Edit\" onclick=\"window.location.href='editCourseDetails.jsp?course_id="+rs.getString("course_id")+"'\"></td>";
					   output += "<td></td><td width=\"8%\"><input name=\"Delete\" type=\"button\" value=\"Delete\" id=\""+rs.getInt("course_id")+"\" onclick=\"onSubmit(this.id);\"></td>";
					//} 
					//window.location.href='asgnmentList.jsp?assignment_id="+ rs.getString("assignment_id")
					output+="</tr>"; 
					//Row 2
					output +=   "<tr><td></td><td class=\"wrapword\"><b>Year: </b>"+rs.getString("year") +"&nbsp;&nbsp;<b>Semester: </b>"+rs.getString("semester") +"</td>";
					output +=   "<td></td><td class=\"wrapword\"><b>Description: </b>"+rs.getString("description") +"</td></tr>";
					   
				  
				} 
				out.println(output);
				rs.close();  
			} catch (Exception err) {

				err.printStackTrace();
				//out.println("Error in getting list of assignments");
				throw new ServletException(err);
				
			}
			//finally{
				//dbcon.close();
			//}
			%>

				
	</fieldset></div></div>									

</body>
</html>