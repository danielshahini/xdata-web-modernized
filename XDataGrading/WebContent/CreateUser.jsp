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
<title>Create New User</title>
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
function chkRoleSelected(){
	if (document.getElementById("role").value != "Select") {
		document.forms["CreatUserForm"].submit();
	} 
	else {
		alert("Please select role for the User.");
		return false;
	}
}
</script> 
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
%>
	<div>
		<form class="CreatUserForm" name="CreatUserForm"
			action="CreateUser" method="post"
			style="border: none">
 
			<div class="fieldset">
				<fieldset>
					<legend> Create New User</legend>
					<div> 
						<span>User Name</span>
						<input placeholder="Specify the User name"
							name="userName" required/> 
					</div>	
					<div>
						<span>Login Id</span>
						<input placeholder="Specify the user id for login"
							name="LoginUserId" required/> 
					</div>
					<div>
						<span>Login Password</span>
						<input placeholder="Specify the password for the user"
							name="password" required/> 
					</div>
				<!-- 	<div> 
					<span>Role</span> 
						<select name="role" id="role">
							<option value="Select">Select</option>
							<option value="admin">Administrator</option>
							<option value="instructor">Instructor</option>
							<option value="student">Student</option>
						</select>
					</div> -->
					<div>
					<span>Email Id</span>
					<input placeholder="Specify the user's mail Id"
							name="email" required/> 
					</div>
				<!-- <div>
					<span>Course Id</span> 
					<input placeholder="Specify course id to which user belongs to"
							name="courseId" required/> 
					</div> -->
				<input type="submit" value="Create">
</fieldset>
</div>
</form>
</body>
</html>