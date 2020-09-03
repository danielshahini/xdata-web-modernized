<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"  errorPage="erroPage.html"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "https://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Late Submission</title>
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>  
<script type="text/javascript" src = "scripts/jquery.js"></script>
<script type="text/javascript" src = "scripts/jquery-2.1.4.js"></script>
<script>

function getParameterByName(name) { 
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$( document ).ready(function() {
	
	if(getParameterByName("value") != ""){
		$('#errorMessage').show();
		 setTimeout( function(){
			 $('#errorMessage').hide();
			  }, 60000*10);
	}  
	else{  
		$('#errorMessage').hide();   
	}
});
</script>
<style>

.fieldset fieldset{
	padding-left: 30px;
	padding-top:30px;
}

.fieldset div span{
	float:left;
	width: 120px;
}

.fieldset div{
	margin-bottom: 20px;
}

.fieldset div input{
	width: 200px;
	height: 20px;
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
%>
<div>
		<form class="lateSubmission" name="GetAssignmentQuestions"
			action="GetQuestionsForLateSumbission" method="post"
			style="border: none"> 
	
		<div class="fieldset">
				<fieldset>
					<legend>Student Details</legend><br>
					<div>
						<span>Student E-mail (Moodle mail Id )</span>
						<input placeholder="Student email id"
							name="email" required/> 
					</div><br>
					
						<div>
						<span>Assignment Id </span>
						<input placeholder="Assignment Id"
							name="assignment_id" required/> 
					</div>
					<br>
					<div id="errorMessage"><font color="red">The email Id does not match. Please enter correct and valid email id.</font></div>
					<div class="getAssignmentQuestions" id="getAssignmentQuestions" style='display:block'>
					<input class="button" name="getAssignmentQuestions" type="submit" id="getAssignmentQuestions" value="Get Questions"></div>	
	</form>
	
	

</body>
</html>