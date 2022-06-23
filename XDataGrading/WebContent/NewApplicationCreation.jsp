<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@page import="database.CommonFunctions"%>
<%@ page import="java.io.*"%>
<%@page import="java.text.*"%>
<%@ page import="java.util.Date"%>
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>

<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "https://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title> &middot; Application</title>

<link rel="stylesheet" type="text/css" href="scripts/datetimepicker/jquery.datetimepicker.css"/ >
<script src="scripts/datetimepicker/jquery.js"></script>
<script src="scripts/datetimepicker/jquery.datetimepicker.js"></script>

<!-- JavaScript -->
<script type="text/javascript" src="scripts/wufoo.js"></script>
<!-- <script src="jQuery.ui.datepicker.js"></script>
 <script src="jquery.ui.datepicker.mobile.js"></script>
-->
 

<!-- CSS -->
<link rel="stylesheet" href="css/structure.css" type="text/css" />
<link rel="canonical"
	href="https://www.wufoo.com/gallery/designs/template.html">
<style>

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
.fieldset #loadDefaultDataSets{
	height:20px;
	width:100%;
	
}
.fieldset  #loadDefaultDataSets input{
	width: 3%;
	height: 10px;
	margin-left: 30%;
	
}
.fieldset #loadDefaultDataSets label{
	float:left;
	height: 20px;
	width: 200px;

}
.fieldset #loadDefaultDataSets label#dsname{
	width:50%;
	
}
.fieldset #loadDefaultDataSets div#dSet{
	height:10px;
	width:100%;
	float:right;
}
.showhidelink a:hover div#showHelp{
    display: block;
}​
</style>

<script type="text/javascript">
	function defaultDate() {
		jQuery("#startdatetimepicker").datetimepicker();		
		jQuery('#enddatetimepicker').datetimepicker();
	}	
	function getParameterByName(name) { 		
	    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
	    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
	        results = regex.exec(location.search);
	    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
	}
	
	function toggleDiv(id){
		$(id).toggle();		
	}
	
$( document ).ready(function() { 
	$('#showHelp').hide();
	
	
	$('#helpInteractive').on('click',function(e){
		$('#showHelp').show();
	});
	
	 if(getParameterByName("selectedOption") != "" && !getParameterByName("selectedOption") != "select"){
		 var value = getParameterByName("selectedOption");
		 $('.schemaId').val(value).prop('selected',true);
		 $('#loadDefaultDataSets').show();	 
		 //$('#showUpload').show();
	 }else{
		 $('.schemaId').val('select').prop('selected',true);
		 $('#loadDefaultDataSets').hide();
		 //$('#showUpload').hide();
	 }

	$('.schemaId').on('change',function(e){
		//alert("OnChange---");
		schemaSel = $(this).val(); 
		if(schemaSel != "select"){
		//window.location = "NewAssignmentCreation.jsp?selectedOption=" + $(this).val();
		var course_id=$(this).attr('id');
		var dataString = "schemaId="+schemaSel+"&&course_id="+course_id;
		//alert("--DataString--"+dataString);
		$.ajax({ 
	        type: "POST",  
	        url: 'GetDefaultDataSets', 
	        data: dataString,
	        context:$(this),        
	        success: function(data) {
	        	try{
	        		//alert("Success Function");
	        		 $('#loadDefaultDataSets').html(data);
	        		// alert(data);
	        		 $('#loadDefaultDataSets').show();
	        	}catch(err){ 
		        	 alert("Error in loading default datasets.");
	        	}
	        }	       
	      });
		}else{
			$('#loadDefaultDataSets').hide();
		}
	});

});
function checkValue(){
	if($('select[name=schemaid]').val()==='select'){
		alert("Select schema");
		return false;
	}else if($('#startdatetimepicker').val() === ''){
		alert("Select start date");
		return false;
	}else if($('#enddatetimepicker').val() === ''){
		alert("Select start date");
		return false;
	}else if($('#assignmentName').val() === ''){
		alert("Please enter name of the assignment");
		return false;
	}
	else{
		document.forms["assignmentForm"].submit();
		}
	}
</script>
</head>
<body id="public" onload="defaultDate()">
<% /*This JSP is used by TESTER mode login to create new assignments*/
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}else if(session.getAttribute("LOGIN_USER") != null && !session.getAttribute("LOGIN_USER").equals("ADMIN")
 		&& session.getAttribute("role") != null && (!session.getAttribute("role").equals("instructor") || !session.getAttribute("role").equals("tester"))){
	response.sendRedirect("index.jsp?NotAuthorised=true");
	session.invalidate();
	return;
}
%>
	<div>
		<form class="assgnmentForm" name="assignmentForm"
			action="updateAssignment.jsp" method="post" style="border: none">
			<div class="fieldset">
				<fieldset>
					<legend> Create a new application</legend>	
					<div>	
						<label>Application Name</label>
						<input placeholder="Give name of this application" id="assignmentName" name="assignmentName"
							required/>
					</div> 
					<!-- <div>
							 <input style="width:30px;height:30px;" type="checkbox" title="Click to make the assignment interactive." name="interactive"/>
							 <label style="margin-top: 0px;">Interactive &nbsp;&nbsp;
							  <a class='showhidelink' href = 'javascript:void(0);' onclick="toggleDiv('#showHelp')">
							 	<img src="images/help_whit_teal blue.jpg" alt="Interactive mode assist the students to learn their mistakes by allowing them to submit answers multiple times." 
							 	title='Interactive mode assist the students to learn their mistakes by allowing them to submit answers multiple times.' width="20" height="20" border="0" />
							  </a>
							 </label>
							 <div id="showHelp" style='position: fixed;border:1px;outline:black; width: 90%;height: 5%;padding: 5px;margin-left: 150px;'>
							 <label>Interactive mode assist the students to learn their mistakes by allowing them to submit answers multiple times. This assignment will not be graded.
							 </label></div>
							
					</div> -->
									  
					<div>
						<label>Application Description</label>
						<input placeholder="Give description of this Application" name="description" required/>
					</div>
					<%
						String courseId = (String) request.getSession().getAttribute(
								"context_label");
						//get the connection for testing1
						//Connection dbcon = (new DatabaseConnection()).dbConnection();
						try(Connection dbcon = (new DatabaseConnection()).dbConnection()) {
							//PreparedStatement stmnt = dbcon.prepareStatement("select sampledata_id,sample_data_name from xdata_sampledata where course_id=? and schema_id=?");
							//stmnt.setString(1, courseId);
							//stmnt.setString(2,schema_id);
							//ResultSet rSet = stmnt.executeQuery();
							
							PreparedStatement stmt = dbcon
									.prepareStatement("SELECT connection_id,connection_name FROM xdata_database_connection WHERE course_id = ?");
							stmt.setString(1, courseId);
							String output = "";
							ResultSet rs = stmt.executeQuery();
							//TODO -  Get DB user and Test user and show it in the drop down
							// CLARIFY : How it will b shown for various Schema's
							// On select schema, show the user name options??????
							while (rs.next()) {
								output += " <option value = \""
										+ rs.getInt("connection_id") + "\"> "
										+ rs.getInt("connection_id") + "-"
										+ rs.getString("connection_name") + " </option> ";
							}							 
							rs.close();%>
							<div><label>Database Connection</label>  
							<select name="dbConnection" style='clear:both;'> 
									<%=output%> </select>
									 </div>						
							<%
							output = "";			
							output +="<option value=\"select\" selected> Select schema</option>";
							stmt = dbcon
									.prepareStatement("SELECT schema_id,schema_name FROM xdata_schemainfo WHERE course_id = ?");
							stmt.setString(1, courseId);
							rs = stmt.executeQuery();							
							while (rs.next()) {
								output += " <option value = \"" + rs.getInt("schema_id")
										+ "\"> " + rs.getInt("schema_id") + "-"
										+ rs.getString("schema_name") + " </option> ";
							}							
							%>
							
							<div><label>Default Database Schema</label>
							<select class="schemaId" id="<%=courseId %>" name="schemaid" style="clear:both;">
									<%=output%> </select>
							</div>
							<div>
								<div id="loadDefaultDataSets" style='display:none;'>
								</div>
							</div>
							<%
						} catch (Exception err) {

							err.printStackTrace();
							throw new ServletException(err);
							
						}
						//finally{
							//dbcon.close();
						//}
						DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
						 Calendar calobj = Calendar.getInstance();
					     System.out.println(df.format(calobj.getTime()));
					     
					   //  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm");
					     //Date dt = new Date()
						   // Date parsedDate = dateFormat.parse("9999/12/31 00:00");
						   // System.out.println(parsedDate.toString());
					%>
					
					<input name = "start" type="hidden" value="<%=(df.format(calobj.getTime())) %>" />
					<input name = "end" type="hidden" value="<%="9999/12/31 00:00" %>" />
				<!-- 	<div>
						<label>Starts at: </label>
						<input name = "start" id="startdatetimepicker" type="text" >
					</div>
					<br/>
					
					<div>
					<label>Ends at </label>
					<input name="end" id="enddatetimepicker" type="text" >
					</div>-->
					
					<input type="button" onclick="checkValue()" value="Submit">
				</fieldset>
			</div>
		</form>

	</div>

	<!--container-->

</body>
</html>