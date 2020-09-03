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
<title>Edit User Details</title>
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
		document.forms["editUserForm"].submit();
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
		<form class="editUserForm" name="editUserForm"
			action="EditUser" method="post"
			style="border: none"> 
 
			<div class="fieldset">
				<fieldset>
					<legend>Edit User Details</legend>
					<%
					String userId = request.getParameter("user_id");
					Connection dbcon = null;
					try{
						dbcon = (new DatabaseConnection()).dbConnection();
						PreparedStatement stmt,stmt1;
							
						stmt = dbcon 
								.prepareStatement("SELECT * FROM xdata_users WHERE internal_user_id = ?");
						stmt.setString(1,userId);
						ResultSet rs = stmt.executeQuery();
						while(rs.next()){
							String role = rs.getString("role").trim();
					%>
					
					<div>
						<span>User Id</span>
						<input name="userId" disabled="disabled" value="<%=rs.getString("internal_user_Id")%>"/> 
					</div>
					<div>
						<span>Login Id</span>
						<input name="LoginId" disabled="disabled" value="<%=rs.getString("login_user_Id")%>"/> 
					</div>
					<div>
						<span>Login Name</span>
						<input type ="hidden" name="userIdToEdit" value="<%=userId%>">
						
						<input name="userName" value="<%=rs.getString("user_name")%>"/> 
					</div>	
					<div>
						<span>Login Password</span>
						<input name="password" disabled="disabled" value="<%=rs.getString("password")%>"/> <br/>
						<span>&nbsp;&nbsp;</span>
						<a style='color:#3D5783' href="resetPassword.jsp?userId=<%=rs.getString("login_user_id")%>">Reset Password</a>
					</div>					 
				 
					<!--<div>
					 <span>Role</span> 
				
						<select name="role" id="role">
							<option value="Select">Select</option>
							<option value="admin"  <%//if(role.equalsIgnoreCase("admin")){%>selected<%//}else{%> <%//} %>>Administrator</option>
							<option value="instructor" <%//if(role.equalsIgnoreCase("instructor")){%>selected<%//}else{%> <%//} %>>Instructor</option>
							<option value="student" <%//if(role.equalsIgnoreCase("student")){%>selected<%//}else{%> <%//} %>>Student</option>
						</select>
					</div> -->
					
					<div> 
						<span>Email Id</span>
					<input placeholder="Specify the user's mail Id"
							name="email" value="<%=rs.getString("email")%>"/> 
					</div>
				<!-- 	<div>
					<span>Course Id</span>
					<input placeholder="Specify course id to which user belongs to"
							name="courseId" value="<%=rs.getString("course_id")%>"/> 
					</div> -->
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
					
				<!-- <input type="button" onclick="chkRoleSelected();"  value="Update"> -->
				<input type="submit" value="Update">
</fieldset>
</div>
</form>
</body>
</html>