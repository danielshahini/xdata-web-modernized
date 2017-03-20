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
<link rel="stylesheet" type="text/css" href="scripts/datetimepicker/jquery.datetimepicker.css"/ >
<script src="scripts/datetimepicker/jquery.js"></script>
<script src="scripts/datetimepicker/jquery.datetimepicker.js"></script>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Create New Course</title>
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

function onSubmit(id){
	
	var a = confirm('Are you sure you want to delete the assignment?');
	alert(a);
	if(a==1) 
		window.location.href="deleteCourse.jsp?connection_id="+id;
	else 
		return false; 
}  

function defaultDate() { 
	jQuery("#startdatetimepicker").datetimepicker({changeMonth: true, changeYear: true, yearRange: '2015:2025'});;
	
	jQuery('#enddatetimepicker').datetimepicker({changeMonth: true, changeYear: true, yearRange: '2015:2025'});;
}

function populateYearSelect()
{
    d = new Date();
    curr_year = d.getFullYear();
  
    for(j=0;j<10;j++){
    	document.getElementById('year').options[j] = new Option(curr_year+j,curr_year+j);
    }
}

</script> 
</head>
<body  onload="populateYearSelect()">
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

%>
	<div>
		<form class="NewCourseForm" name="NewCourseForm"
			action="CreateNewCourse" method="post"
			style="border: none"> 
 
			<div class="fieldset">
				<fieldset>
					<legend> Create a New Course</legend>
					<div>
						<span>Course Id</span>
						<input placeholder="Specify the Course Id"
							name="courseId" required/> 
					</div>
					
					<div>
						<span>Course Name</span>
						<input placeholder="Specify name of the Course"
							name="courseName" required/> 
					</div>
			
					<div> 
					<span>Year</span> 
						<select name="year" id="year">
						<!-- options generated dynamically by javascript -->
						</select>
					</div>
					
					<div>
					<span>Semester</span>
					<input placeholder="Specify semester of the course"
							name="semester" required/> 
					</div>
					<div>
					<span>Description</span>
					<input placeholder="Specify course description if any"
							name="desc"/> 
					</div>
				<input type="submit" value="Create">
</fieldset>
</div>
</form>
</body>
</html>