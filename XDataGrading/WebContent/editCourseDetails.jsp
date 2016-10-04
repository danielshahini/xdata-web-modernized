<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 
<link rel="stylesheet" href="css/structure.css" type="text/css"/> 
<script type="text/javascript" src = "scripts/jquery.js"></script>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Edit Course Details</title>
<script src="../crumb.js"></script>
<script language="JavaScript">
breadcrumbs();
</script>
<style> 

table, tr, td {
    border: 0px;
}

.fieldset fieldset{
	padding-left: 30px;
	padding-top:30px;
}

.fieldset div span{
	float:left;
	width: 250px;
}

.fieldset div{
	margin-bottom: 20px;
}

.fieldset div input{
	width: 400px;
	height: 20px;
}

#test{

	padding-bottom:20px;
	height:25px;	
	width: 150px;
	height: 20px;
}
#save{
	padding-bottom:20px;
	height:25px;	
	width: 150px;
	height: 20px;
}

</style>
<script>

function populateYearSelect()
{
    d = new Date();
    curr_year = d.getFullYear();
   // document.getElementById('year').options[0] = new Option("Select","Select");
    for(j=0;j<10;j++){
    	document.getElementById('year').options[j] = new Option(curr_year+j,curr_year+j);
    }
}

function chkYearSelected(){
	if (document.getElementById("year").value != "Select") {
		document.forms["editCourseForm"].submit();
	} 
	else {
		alert("Please select year for the Course.");
		return false;
	}
}
</script> 
</head>
<body  onload="populateYearSelect()">
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}
%>
	<div>
		<form class="editCourseForm" name="editCourseForm"
			action="EditCourse" method="post"
			style="border: none"> 
 
			<div class="fieldset">
				<fieldset>
					<legend> Create a New Course</legend>
					<%
					
					String courseId = request.getParameter("course_id");
					Connection dbcon = null;
					try{
						dbcon = (new DatabaseConnection()).dbConnection();
						PreparedStatement stmt,stmt1;
							
						stmt = dbcon 
								.prepareStatement("SELECT * FROM xdata_course WHERE course_id = ?");
						stmt.setInt(1,Integer.parseInt(courseId));
						ResultSet rs = stmt.executeQuery();
						while(rs.next()){
							 
					%>
					<div>
						<span>Course Id</span>
						<input type ="hidden" name="courseId" value="<%=courseId%>">
						<input placeholder="Specify the Course Id"
							name="instrcourseId" value="<%=rs.getString("instructor_course_id")%>" disabled="disabled"/> 
					</div>					
					<div>
						<span>Course Name</span>
						<input placeholder="Specify name of the Course"
							name="courseName" value="<%=rs.getString("course_name")%>"/> 
					</div>
			
					<div>  
					<span>Year</span> 
						<select name="year" id="year">	
						  <option value="<%=rs.getInt("year")%>" selected></option>
						<!-- options generated dynamically by javascript -->
						</select>
					</div>
					
					<div>
					<span>Semester</span>
					<input placeholder="Specify semester of the course"
							name="semester" value="<%=rs.getString("semester")%>"/> 
					</div>
					<div>
					<span>Description</span>
					<input placeholder="Specify course description if any"
							name="desc" value="<%=rs.getString("description")%>"/> 
					</div>
					<%}
						rs.close();  
					} catch (Exception err) {

						err.printStackTrace();
						//out.println("Error in getting list of assignments");
						throw new ServletException(err);
						
					}
					finally{
						dbcon.close();
					}%>
				<input type="button" onclick="chkYearSelected();"  value="Update">
</fieldset>
</div>
</form>
</body>
</html>