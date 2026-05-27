<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "https://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 
<link rel="stylesheet" href="css/structure.css" type="text/css"/> 
<script type="text/javascript" src = "scripts/jquery.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Reset Password</title>
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

</head>
<body>
	<div>
		<form class="resetPwdForm" name="resetPwdForm"
			action="ResetPwd" method="post"
			style="border: none">

			<div class="fieldset">
				<fieldset>
					<legend> Reset Password</legend>
					<div>
					<label> Login Id : <%=request.getParameter("userId") %></label>
					<input type ="hidden" name = "userId" value="<%=request.getParameter("userId")%>">
					</div>
					<!-- <div>
						<span>Enter Old Password</span>
						<input type="password" placeholder="Enter the existing password for the user"
							name="oldPassword" required/> 
					</div> --> 					
					<div>  
						<span>Enter New Password</span>
						<input type="password" placeholder="Enter the new Password for the user"
							name="newPassword" required/>  
					</div>
				<input type="submit" value="Reset">
</fieldset>
</div>
</form>
</body>
</html>