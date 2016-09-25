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
<title>Assign Role</title>
<script>
 
$( document ).ready(function() {	
	$('.assignRole').click(function(e){
		e.preventDefault();
		if($('#Name').val() != ""){
			($('.courseDetails')).show();
			($('.roleDetails')).show();
			
		}
		else{ 
			alert("Please enter the user id");
		}
		
	});
	
	$('.add').click(function(e){
		
		//alert("add evnt triggered");
		e.preventDefault();
		 
		$('.courseDetails').clone().appendTo('.courseDetails');
		$('.roleDetails').clone().appendTo('.roleDetails');
	//	$('.AddButton').clone().appendTo('.AddButton');
		
		//get original selects into a jq object
		//var $originalSelects = $orginalDiv.find('select');

		//$clonedDiv.find('select').each(function(index, item) {

		     //set new select to value of old select
		   //  $(item).val( $originalSelects.eq(index).val() );		 
	});
		
	$('.assign').click(function(e){

		var isCourseOk = false;
		var isRoleOk = false;
		var isNameOk = false;
		
		//alert("Name = "+ $('#Name').val());
		if($('#Name').val().trim() != ""){			
			isNameOk = true;
		} 
		else{ 
			alert("Please enter valid user id.");
			return false;
		}
		if($('#courseId').val() != "Select") {
			isCourseOk = true;	
		} 
		else {  
			alert("Please select the course.");
			return false; 
		}
		if ($('#role').val() != "Select") {
			isRoleOk  =true;		
		} 
		else { 
			alert("Please select role for the User.");
			return false;
		}	
		if(isNameOk && isCourseOk && isRoleOk){
			document.forms["AssignRole"].submit();	
			
		}	
	});
	
	/*$('.assign').click(function(e){
		e.preventDefault(); 
		
		var dataString="Id="+document.getElementsByName("Id")[this.id].value+"&&CourseId="+document.getElementsByName("CourseId")[this.id].value+
						"&&role="+document.getElementsByName("role")[this.id].value;
		alert(dataString);
		var self = this;
		
		$.ajax({ 
	        type: "POST", 
	        url: "AssignRole",
	        data: dataString,
	        context:this,  
	        beforeSend : function() {
		
	        	if(document.getElementsByName("Id")[this.id].value.trim() == ""){
	        		alert("Please enter valid user id.");
	        		return false;
	        	}
	        	if (document.getElementsByName("CourseId")[this.id].value == "Select") {
	        		alert("Please select the course.");
	        		return false;
	        	}	        	
	        	if (document.getElementsByName("role")[this.id].value == "Select") {
	        		alert("Please select role for the User.");
	        		return false;		
	        	} 
	        	
	       }, complete: function(){ 
        	 try{ 
        		
        		 ($('#statusMsg'+this.id)).show();
        		 ($('#statusErrMsg'+this.id)).hide();
        	 }catch(err) 
     		{
        			($('#statusErrMsg'+this.id)).show();
        			($('#statusMsg'+this.id)).hide();
	        }
          },
	        success: function(data) { 
	        	try{
	        		
	        		 ($('#statusMsg'+this.id)).show();
	        		 ($('#statusErrMsg'+this.id)).hide();
	        	}	        	
	        		catch(err)
	        		{
	        			($('#statusErrMsg'+this.id)).show();
	        			($('#statusMsg'+this.id)).hide();
	        		}        	
	        }     
	      }); 
	      return false;        
	});*/
	
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

.fieldset div span{
	float:left;
	width: 50px;
}

.fieldset div{
	margin-bottom: 20px;
}

.fieldset div input{
	width: 125px;
	height: 25px;
}

</style>
</head>
<body>
<% 
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}
%>	<div>
		<form class="AssignRole" name="AssignRole"
			action="AssignRole" method="post"
			style="border: none"> 
			<div class="fieldset"> 
				<fieldset>
					<legend>Assign Role</legend>
					<br/>
					Enter user id to assign role.
					<br/>	<br/>
					<div>
						<label>Login Id: </label> 
						<input id="Name" name="loginId" required/>		
											
						<a href="#" class="assignRole" ><button>Submit</button></a>
					</div>					
						<%	
						Connection dbcon = null;
						String output="",roleDropDown = "";
						int index=0,i=1;
						String role = "",course="";
					
						try {
							dbcon = (new DatabaseConnection()).dbConnection();
							PreparedStatement stmt,statement;							
							stmt = dbcon
									.prepareStatement("SELECT * from xdata_course");
							ResultSet rs = stmt.executeQuery();
							output += "<option value=\"Select\">Select</option>";
							while (rs.next()) { 
									output += " <option value = \""
										+ rs.getString("instructor_course_id") + "\"> "
										+ rs.getString("instructor_course_id") + " </option> ";
							}
							rs.close();
							%>	
							<table>
							<tr><td>
								<div class="courseDetails" style='display:none' >
								<span>Course:</span> 
								<select id="courseId" name="CourseId" style='clear:both;'>
									<%=output%></select>
								</div>
							</td>
							<td>
								<div class="roleDetails" style='display:none' >
								<span>&nbsp;&nbsp;&nbsp;&nbsp;Role:</span>
										<select name="role" id="role" style='clear:both;'>
											<option value="Select"  <%if(role.equalsIgnoreCase("")){%>selected<%}%>>Select</option>
											<option value="admin" <%if(role.equalsIgnoreCase("admin")){%>selected<%}else{%> <%} %>>Administrator</option>
											<option value="instructor" <%if(role.equalsIgnoreCase("instructor")){%>selected<%}else{%> <%} %>>Instructor</option>
											<option value="student" <%if(role.equalsIgnoreCase("student")){%>selected<%}else{%> <%} %>>Student</option>
											<option value="tester" <%if(role.equalsIgnoreCase("tester")){%>selected<%}else{%> <%} %>>Tester</option>
										</select> 									
								</div>
							</td>
							<!--<td>
							 <div class="AddButton" style='display:none'>
							<a href="#" style='color:#353275;' class="add" id=''>Add another role.</a>
							<a href="#" style='color:#353275;' class="remove" id=''>remove</a>
							</div> 	</td> -->
							</tr></table>
									
							<div class="newDropDowns"></div>						
							<div>
							<input type="button" class="assign" value="Assign"/>
							</div>
						<%	}catch (Exception err) { 
						 
						err.printStackTrace();
						throw new ServletException(err);					
					}
					finally{
						dbcon.close();
						
					}
						%>
		</table>
	</fieldset></div></form></div>
</body>
</html>