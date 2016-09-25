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
	<script type="text/javascript" src = "scripts/jquery-ui.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Forgot Password</title>
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
function onSubmit(){
	//alert(document.getElementById("name").value);
	if(document.getElementById("name").value == ""){
	alert("Please enter the user Id");	
	return false;
	}
	else if(document.getElementById("mail").value == ""){
		alert("Please enter the Email Id");	
		return false;
	}
	//else if(document.getElementById("newPwd").value == ""){
	//	alert("Please enter the new password");	
	//	return false;
	//}
	else{
		//alert("Please check your registered email for the new password.");
		document.forms[0].submit();
	} 
}
function getParameterByName(name) { 
	
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$( document ).ready(function() { 
	var status = getParameterByName("status");

	if(status=="error"){ 
		$('#error').show();
	}
});

</script>  
</head>
<body>
	<div>
		<form class="forgotPwdForm" name="forgotPwdForm"
			action="ForgotPassword" method="post"
			>

			<div class="fieldset">
				<fieldset>
					<legend> Reset Password</legend>
					 <div>
						<span>Login user Id</span>
						<input id="name" placeholder="Enter the login user id"
							name="userId" required/> 
					</div>
					<div> 
						<span>Enter mail Id</span>
						<input id="mail" placeholder="Enter the e-mail id to which password will be e-mailed"
							name="mailId" required/>  
					</div>
					<!-- <div> 
						<span>Enter new password</span>
						<input id="newPwd" placeholder="Enter the new password"
							name="newPwd" type="password" required/>  
					</div>-->
				<input type="button" onclick="onSubmit();"  value="Reset Password">
				
				<br/>
			<!-- <div id="success" style='display:none;color:green;'>Password changed. <a href='index.jsp'> click here to login</a>.</div> -->
	<div id="error" style='display:none;color:red;'>Password reset failed. Given user id and email does not match with the existing data. <br>
				Please enter valid information or contact your xdata system administrator.</>
			
			
</fieldset>
	
</div>
</form>
</div>
</body>
</html>