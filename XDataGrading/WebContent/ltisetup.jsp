<%@page import="database.DBConnectionInfo"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"  errorPage="errorPage.jsp"%>
<%@page import="database.CommonFunctions"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "https://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<script>
function getParameterByName(name) { 
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}  



function onSubmit(id){
	
	var a = confirm('Are you sure you want to delete the LMS details?');
	//alert(a);
	if(a==1) 
		window.location.href="DeleteLtiSetup?instanceUrl="+id;
	else 
		return false; 
}
$( document ).ready(function() {
if(getParameterByName("Delete") != ""){
	$('#deleteMessage').show();
	 setTimeout( function(){
		 $('#deleteMessage').hide();
		  }, 60000*10);
}  
else{  
	$('#deleteMessage').hide();   
}

if(getParameterByName("edit") != ""){
	$('#saveEdit').show();
	 setTimeout( function(){
		 $('#saveEdit').hide();
		  }, 60000*10);
} 
else{ 
	$('#saveEdit').hide();   
}


});

</script>
<style>
table, tr, td {
    border: 0px;
}
.fieldset fieldset{
	padding-left: 30px;
	padding-top:30px;
}
.fieldset div label{
	float:left;
	width: 250px;
}
.fieldset div{
	margin-bottom: 20px;
	height:20px;
	width:100%;
}
.fieldset div input{
	width: 400px;
	height: 20px;
	float:left;
}
.fieldset div #submitButton{
	width: 200px;
	height: 30px;
	float:center;
}
</style>
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
<form class="lmsForm" name="lmsForm"
			action="NewLmsCredential" method="post" style="border: none">
<div class="fieldset">
				<fieldset>
					<legend>LMS Credentials</legend>	
					<div>	
						<label>Moodle Instance URL</label>
						<input placeholder="Enter the moodle instance Url. Ex: https://www.aaa.com/moodle/mod/lti/service.php" id="ltiUrl" name="ltiUrl"
							required/>
					</div> 
					<div>	
						<label>Consumer Key</label>
						<input placeholder="Enter the consumer key for this moodle instance" id="consumerKey" name="consumerKey"
							required/>
					</div> 
					<div>	
						<label>Secret Key</label>
						<input placeholder="Enter the secret key for this moodle instance" id="secretKey" name="secretKey"
							required/>
					</div> 
					
					<div><input  type="submit" id="submitButton"  value="Submit"></div>
				</fieldset>
				<fieldset>
					<legend>Existing LMS Setup</legend>	
						 <form name="editLtiForm" id="editLtiForm" method="post" 
		 action="editLtiSetup.jsp">
						<div class="statusMessage">
			 			<label class="deleteMessage" id="deleteMessage"
  							style='display: none; color: green;'>Selected 
							LMS information deleted.</label>
	 	
			 			<label class="saveEdit" id="saveEdit"
  							style='display: none; color: Green;'>LMS information updated.</label>	
  												
			</div>			
			<p></p> 
			
					<table style="border:'0';table-layout: fixed;" width="80%">
					<!-- <tr><th>Instance URL</th><th>Consumer Key</th><th>Secret Key</th></tr> -->
					
					<%Connection dbcon = (new DatabaseConnection()).dbConnection();
					int index = 0;
					try {
						PreparedStatement stmt1 = dbcon
								.prepareStatement("SELECT * FROM xdata_lti_credentials");
						
						ResultSet rs1 = stmt1.executeQuery();
						while (rs1.next()) {
								index ++;
					%>	
					
					<tr><td width='3px;'><%=rs1.getString("lti_id") %></td><td colspan="4">
					<b>Instance Url: </b><%=rs1.getString("requesting_url")%>
						</td>
					</tr>
					
					<tr> <td width='3px;'></td><td width="50%">
						 <b>Consumer Key:</b> <%=rs1.getString("consumer_key")%>
						</td> 
						<td><b>Secret Key:</b> <%=rs1.getString("secret_key")%>
						</td>
							<td><input name="Edit" type="button" value="Edit" onclick="window.location.href='editLtiSetup.jsp?lti_id=<%=rs1.getString("lti_id")%>'"></td>
							<td><input name="Delete" type="button" value="Delete" id="<%=rs1.getString("requesting_url")%>" onclick="onSubmit(this.id)"></td>								
							 
					</tr>
					
					<tr><td></td><td></td><td></td><td></td><td></td></tr>
					<tr><td></td><td></td><td></td><td></td><td></td></tr>
					<%} 
						}catch (Exception err) {
							err.printStackTrace();
							throw new ServletException(err);	
						}finally{dbcon.close();}%>
				</table>
				</fieldset>
</div>
</form>
</div>
</body>
</html>