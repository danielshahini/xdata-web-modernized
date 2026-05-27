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
 <script type="text/javascript" src="scripts/jquery.dialog.min.js"></script> 
<script src="scripts/jquery-ui-dialog.js" type="text/javascript"></script>
<link href="css/jquery-ui-dialog.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src = "scripts/jquery.js"></script>
<script type="text/javascript" src = "scripts/jquery-2.1.4.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><html>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
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
		<form class="editLtiForm" name="editLtiForm"
			action="EditLmsCredential" method="post"
			style="border: none">
 
			<div class="fieldset"> 
				<fieldset>
					<%					//String url = request.getParameter("instanceUrl");
					int id= Integer.parseInt(request.getParameter("lti_id"));
					//get the connection for testing1
					//Connection dbcon = (new DatabaseConnection()).dbConnection();
					try (Connection dbcon = (new DatabaseConnection()).dbConnection()){  

						PreparedStatement stmt;
						stmt = dbcon
								.prepareStatement("SELECT * from xdata_lti_credentials where lti_id=?");
						stmt.setInt(1, id);
						
						ResultSet rs = stmt.executeQuery();
						if(rs.next()){ 
						
				
					%>
					<legend>LMS Credentials</legend>	
					<div>	
						<label>Moodle Instance URL</label>
						<input id="ltiUrl" name="ltiUrl" value="<%=rs.getString("requesting_url")%>"  disabled="disabled"/>
					</div> 
					<div>	
						<label>Consumer Key</label>
						<input id="consumerKey" name="consumerKey" value="<%=rs.getString("consumer_key")%>"/>
					</div> 
					<div>	
						<label>Secret Key</label>
						<input id="secretKey" name="secretKey" value="<%=rs.getString("secret_key")%>"/>
					</div> 
					
					<div>
					<input type ="hidden" id="lti_id" name="lti_id" value="<%=rs.getString("lti_id")%>"/>
					<input id="submitButton" type="submit" value="Update"></div>
					<%} 
					} 
					//finally{
					//	dbcon.close();
					//}
					 
					%>			 
					</fieldset>  
			</div>			
		 </form> 	
	</div>

</body>
</html>