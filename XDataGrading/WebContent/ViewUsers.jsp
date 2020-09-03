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
<title>View Users</title>
<style>
table, tr, td {
    border: 0px;
}
</style> 
<script>
function onSubmit(id){	
	var a = confirm('Are you sure you want to delete the User?');
	if(a==1) 
		window.location.href="deleteUser.jsp?userId="+id;
	else
		return false; 
}  

function getParameterByName(name) { 
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}
$( document ).ready(function() { 	
if(getParameterByName("status") == "success"){
	 
	$('.resetPwdSuccessMsg').show();
	 setTimeout( function(){
		 $('.resetPwdSuccessMsg').hide();
		  }, 25000*10);
}   
else{ 
	$('.resetPwdSuccessMsg').hide();   
}
});

</script>
</head>
<body> 
<%
if (session.getAttribute("LOGIN_USER") == null) {
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
				<legend>List of Users</legend>
				<form name="form1" method="post" action="editUserDetails.jsp">
				<div class="resetPwdSuccessMsg" style='color:green;display:none;'>Password reset successful</div>
				<%Connection dbcon = null;
				int index=0;  
				String output = "";
				try{
				dbcon = (new DatabaseConnection()).dbConnection();
				PreparedStatement stmt;
				stmt = dbcon 
						.prepareStatement("SELECT * FROM xdata_users");
				ResultSet rs = stmt.executeQuery();
				while(rs.next()){ 
				//	 request.getSession().setAttribute("context_label",rs.getString("course_id"));
					index++;
					output +="<table border=\"0\" style=\"table-layout:fixed;\" width=\"100%\">";
					//Row 1 - column 1- index value, column 2- User Id,,name column 3-role,
					//column 4 - buttons
					output +=	"<tr><td width=\"2%\">"+index +". </td>";
					 
					output += "<td class=\"wrapword\" width=\"30%\"><b>Login Id: </b>"+rs.getString("login_user_id")+"</td>";
					output +=   "<td></td><td class=\"wrapword\" width=\"30%\"> <b>Login Name: </b>"+rs.getString("user_name")+"</td>";
					 
				//		output += "<td></td><td><input name=\"View\" type=\"button\" value=\"View\" onclick=\"document.form1.submit()\"></td>";
					System.out.println("Role of the user : "+rs.getString("role"));

					output += "<td></td><td width=\"20%\"><input name=\"ResetPwd\" type=\"button\" value=\"Reset Password\" onclick=\"window.location.href='resetPassword.jsp?userId="+rs.getString("login_user_id")+"'\"></td>"; 
				    output += "<td></td><td width=\"8%\"><input name=\"Edit\" type=\"button\" value=\"Edit\" onclick=\"window.location.href='editUserDetails.jsp?user_id="+rs.getString("internal_user_id")+"'\"></td>";
					output += "<td width=\"8%\"><input name=\"Delete\" type=\"button\" value=\"Delete\" id=\""+rs.getString("internal_user_id")+"\" onclick=\"onSubmit(this.id);\"></td>";
					    //window.location.href='asgnmentList.jsp?assignment_id="+ rs.getString("assignment_id")
					output+="</tr>";
					//Row 2
					output +=   "<tr><td></td><td class=\"wrapword\"><b>Email: </b>"+rs.getString("email")+"</td>";
					//output +=   "<td></td><td class=\"wrapword\"><b>Course Id: </b>"+rs.getString("course_id") +"</td></tr>";
					output += "<tr><td colspan=\"6\"></td></tr>";
					output += "<tr><td colspan=\"6\"></td></tr>";
								  
				} 
				out.println(output);
				rs.close();  
			} catch (Exception err) {

				err.printStackTrace();
				//out.println("Error in getting list of assignments");
				throw new ServletException(err);
				
			}
			finally{
				dbcon.close();
			}%>

				
	</fieldset></div></div>									

</body>
</html>