<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@ page session="false" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
 
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="Cache-control" content="no-cache">
    <meta http-equiv="pragma" content="no-cache" /> 
    <meta http-equiv="Cache-control" content="no-store"> 
    <meta http-equiv="Window-Target" content="_top">
	<link rel="stylesheet" href="css/structure.css"/>  
	<link rel="stylesheet" href="css/reset.css"/>
	<link rel="stylesheet" href="css/animate.css"/>
	<link rel="stylesheet" href="css/styles.css"/> 
	<script type="text/javascript" src = "scripts/jquery.js"></script>
	<script type="text/javascript" src = "scripts/jquery.js"></script>
	<script type="text/javascript" src = "scripts/jquery-ui.js"></script>
	<title>Login</title>  
	<script type="text/javascript">
	  
	  function getParameterByName(name) { 
		    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
		    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
		        results = regex.exec(location.search);
		    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
		}  		    
		function chkSession(){	
			
			if(getParameterByName("TimeOut") != ""){ 
				alert("The session has expired, Please relogin.");	
				
			} 
		}    
		$( document ).ready(function() {
			
			if(getParameterByName("Login") != ""){
				$('#loginMessage').show();
			} 			  
			else{ 
				$('#loginMessage').hide();  
			}
			if(getParameterByName("pwdStatus") != ""){
				alert("Password changed. You can continue to login");
			}
		});
	</script>	
</head>  
<body onLoad = 'chkSession()'> 
<% 
		HttpSession checkUserSession = request.getSession(false);
		if (checkUserSession!= null && checkUserSession .isNew() && checkUserSession.getAttribute( "uname" ) == null ) {
		 //Its a new session. So it can proceed	   
		} else if (checkUserSession!= null && !checkUserSession .isNew() && checkUserSession.getAttribute( "uname" ) != null ) {
			checkUserSession.invalidate();
		}
		else if(checkUserSession != null){ 			  
		  // If a session exists, get the user name and password for reloading the same session
		  String uname = (String)checkUserSession.getAttribute( "uname" );
		  String pwd=   (String)checkUserSession.getAttribute("pwd"); 
		 if(uname != null && pwd != null){
		   
				response.setContentType("text/html");
				if(uname.equalsIgnoreCase("instructor")){
					checkUserSession.setAttribute("LOGIN_USER", "ADMIN"); 
				}
				if(uname.equalsIgnoreCase("admin")){
					checkUserSession.setAttribute("LOGIN_USER", "ADMIN"); 
				}
				if(uname.equalsIgnoreCase("tester")){
					checkUserSession.setAttribute("LOGIN_USER", "Tester"); 
				}
				if(uname.equalsIgnoreCase("student")){
					checkUserSession.setAttribute("LOGIN_USER", "student");
				} 
				if(uname.equalsIgnoreCase("guest")){
					checkUserSession.setAttribute("LOGIN_USER", "guest");
				}
				//Redirect the user to the main page of the application
				response.sendRedirect("Empty.html");
				return;			 	
			}    
		  
	   
		}  
%>	
	<!-- Begin Page Content -->	 
	<div id="container">
		<form action="LoginChecker" method="post" target="_top">
			<label for="name">Username:</label> 
			<input type="name" name="name">
			<label for="username">Password:</label>
			<input type="password" name="password">
			<label id="loginMessage">The username or password you entered is incorrect.</label>		
			<div id ="lower">
			<div>			
			<label style='font: 14px/14px Arial, Lucida Grande;'>To login as guest, enter user name and password as 'guest'.</label>
			<label style='font: 14px/14px Arial, Lucida Grande;'>To login as instructor, please drop a mail to xdata@cse.iitb.ac.in</label>
			
			<input type="submit" id="login" value="Login"/>
			
			
			</div>
			<div>
	<!-- <label><a style='color:#353275;font-size: 15px;' href="forgotPwd.jsp">Forgot your password?</a></label> --> 
			</div>
				
				
			</div>	
			
		</form>		
	</div>
	<!-- End Page Content -->
</body>
</html>
