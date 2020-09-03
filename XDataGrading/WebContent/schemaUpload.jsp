<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%> 
<%@ page import="java.text.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "https://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

<script type="text/javascript" src = "scripts/jquery.js"></script>
<!-- FIX TODO - check and remove for alignment -->
<!-- <link rel="stylesheet" href="css/styles.css"/> --> 
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="css/structure.css" type="text/css" />
<link rel="canonical"
	href="https://www.wufoo.com/gallery/designs/template.html">

<title>Upload New Schema</title> 
 <script type="text/javascript" src="scripts/wufoo.js"></script> 
<script>
function getParameterByName(name) { 
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}  

$(document).on('click', '.deleteId' ,function (event) {
	
	//alert( $(this).attr('id') );
	var a = $(this).attr('id');
	dataString = "schema_id="+a;
	//alert(dataString);
    //Process button click event
	var opt = confirm("Do you want to delete the schema?");	
	if(opt == true){
		window.location = "deleteSchema.jsp?schema_id="+a;		 
	} 
	else{
		return false;	}
    });
  
$( document ).ready(function() { 	
 
$('.test').click(function (event) {

	var self = this;  
	
	//alert( $(this).attr('id') );
	var a = $(this).attr('id');
	var dString = "schema_id="+a;
	
	$(self).parent().find($('.test')).hide();
	//alert($(self).parent().parent().get(0).tagName);
		  
	$(self).parent().parent().find($('.selectConnection')).show();
	var sel = $(self).parent().parent().find($('.dbConnection'));
	$(sel).val('select').prop('selected',true);
	 
	//alert("self.3 parent = "+$(self).parent().parent().parent().get(0).tagName);
//	alert("self.4 parent ="+$(self).parent().parent().parent().parent().parent().get(0).tagName);
	
	 
	$(sel).on('change',function(e){
  	
		e.preventDefault();	 
		//alert($(sel).val());
		if($(sel).val() != "select"){
			dataString=$(sel).attr('id') +"&dbConnection="+$(sel).val();
			
		}else{ 
			alert("Please select/add database connection to test");
		}
		 
		$.ajax({ 
	        type: "POST",  
	        url: "TestUploadedFile", 
	        data: dataString,
	        context:$(this),        
	        success: function(data) {
	        	//alert("Database Connection test successful.");
	        	try{
	        		//Self is select first parent is div and next parent is td
	        		$(self).parent().parent().find($('.selectConnection')).hide(); 
	        		$(self).parent().parent().find($('.test')).show();
	        		$(self).parent().parent().parent().parent().parent().find($('.schemaUploadTestMessage')).show();
	        	}catch(err){
	        		$(self).parent().parent().find($('.test')).show();   
	        		$(self).parent().parent().find($('.selectConnection')).hide();
	        		$(self).parent().parent().parent().parent().parent().find($('.schemaUploadTestFailedMessage')).show();
		        	 
	        	}
	        }, 
	        error : function(xhr, status, thrownError){
	        	
	        	$(self).parent().parent().find($('.test')).show();   
	        	$(self).parent().parent().find($('.selectConnection')).hide();
	        	$(self).parent().parent().parent().parent().parent().find($('.schemaUploadTestFailedMessage')).show();
	        	    		         	    	 
	        }
	        
	      });
	});    
});
	
	//alert(dataString); 
    //Process button click event
	//window.location = "connectionToTestUpload.jsp?schema_id="+a;

	
if(getParameterByName("test") != ""){
		$('.schemaUploadTestMessage').show();
		 setTimeout( function(){
			 $('.schemaUploadTestMessage').hide();
			  }, 60000*10);
	}  
	else{ 
		$('.schemaUploadTestMessage').hide();   
	}
	 
	if(getParameterByName("testerror") != ""){
		
		$('.schemaUploadTestFailedMessage').show();
		 setTimeout( function(){
			 $('.schemaUploadTestFailedMessage').hide();
			  }, 60000*10);
	}  
	else{ 
		$('.schemaUploadTestFailedMessage').hide();   
	
	}
	
if(getParameterByName("error") != ""){
		
		$('.schemaUploadErrorMessage').show();
		 setTimeout( function(){
			 $('.schemaUploadErrorMessage').hide();
			  }, 60000*10);
	}  
	else{ 
		$('.schemaUploadErrorMessage').hide();   
	
	}
	
	if(getParameterByName("Delete") != ""){
		$('#schemaDeleteMessage').show();
		 setTimeout( function(){
			 $('#schemaDeleteMessage').hide();
			  }, 60000*10);
	}  
	else{ 
		$('#schemaDeleteMessage').hide();   
	}
	
	if(getParameterByName("upload") != ""){
		$('#schemaUploadMessage').show();
		 setTimeout( function(){
			 $('#schemaUploadMessage').hide();
			  }, 60000*10);
	}  
	else{ 
		$('#schemaUploadMessage').hide(); 
	}
}); 





	function checkValue1() {

		if (document.getElementById("upload").value != "") {
			return true;
		} else {
			alert("Please upload file");
			return false;
		} 
	}
	function trim(str, chars) {
		return ltrim(rtrim(str, chars), chars);
	}
	function ltrim(str, chars) {
		chars = chars || "\\s";
		return str.replace(new RegExp("^[" + chars + "]+", "g"), "");
	}
	function rtrim(str, chars) {
		chars = chars || "\\s";
		return str.replace(new RegExp("[" + chars + "]+$", "g"), "");
	}
</script>
<style>
table, tr, td {
    border: 0px;
}

html, body{ 
	width: 100%;
	height: 100%;
	font-family: "Helvetica Neue", Helvetica, sans-serif;
	color: #444;
	-webkit-font-smoothing: antialiased;
	background: #fff
	
}

.wrapword{
white-space: -moz-pre-wrap !important;  /* Mozilla, since 1999 */
white-space: -webkit-pre-wrap; /*Chrome & Safari */ 
white-space: -pre-wrap;      /* Opera 4-6 */
white-space: -o-pre-wrap;    /* Opera 7 */
white-space: pre-wrap;       /* css-3 */
word-wrap: break-word;       /* Internet Explorer 5.5+ */
word-break: break-all;
white-space: normal;

}
#breadcrumbs
{
  position: absolute;
  padding-left:10px;
  padding-right:10px;
  left: 5px;
  top: 10px;
  font: 13px/13px Arial, Helvetica, sans-serif;
  background-color: #f0f0f0;
  font-weight: bold;
}
#tabSpace{
 padding-left:500px;
}
</style>
</head>
<body class="public" id="public">
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}

else if(session.getAttribute("LOGIN_USER") != null && !session.getAttribute("LOGIN_USER").equals("ADMIN")
 		&& session.getAttribute("role") != null && (!session.getAttribute("role").equals("instructor") || !session.getAttribute("role").equals("tester"))){
	response.sendRedirect("index.jsp?NotAuthorised=true");
	session.invalidate();
	return;
}

%>
  <br/>
	<div class="name">   
	
		<form name="schemaForm" enctype="multipart/form-data" action="SchemaUpload" method="post">
			<div class="fieldset">
				<fieldset>
					<legend> Upload Schema File</legend>
								<input type="hidden" name="requestingPage" value="schemaUpload"/>
					<br/>
						<label class="field">Upload the SQL script containing the
							schema. The file should contain scripts to create schema with drop commands before Create commands.
							<a href='sampleDataUpload.jsp' style="text-decoration:none">Data file </a> to be uploaded separately </label>
					<p></p>
					<input style="width: 250px; height: 20px;" placeholder="Give name of schema, default will be file name" name="schemaname" id="schemaname"/>
					<label class="field">Choose File: </label> 
					<input	type="file" required name="schemaFile" size="20">
					<br/><br/> <span id='tabSpace'></span>
					<input name="testupload" class="testupload" onclick="" type="submit" id="schemaUpload" value="Test and Upload"/>
					<input name="upload" onclick="return checkValue1();" type="submit" id="upload" value="Upload"/>
		 					 
				<div class="statusMessage"> 
				<br> 
				<label class="schemaUploadMessage" id="schemaUploadMessage" style='display:none;color:green;'>File upload successful.</label>
				<label class="schemaUploadErrorMessage" id="schemaUploadErrorMessage" style='display:none;color:red;'>Please upload valid script file.</label>
			</div>
				</fieldset>
  
			</div> 

		</form>
	</div>
	
	
	<div class="name"> 
		
			<div class="fieldset">
				<fieldset>
					<legend>Existing Schema</legend>
  				<input type="hidden" name="requestingPage" value="schemaUpload">
				<div class="statusMessage">	
				<label class="schemaDeleteMessage" id="schemaDeleteMessage" 
				style='display:none;color:green;'>Selected schema(s) deleted.</label>
				</div>			
				<p></p>
					<%
					String courseID = (String) request.getSession().getAttribute("context_label");
					System.out.println("**************************COURSE ID IN LOADING SCHEMA *********"+courseID);
					//get connection
					Connection dbcon = (new DatabaseConnection()).dbConnection();
					try { 
							PreparedStatement stmt,stmt1;
							stmt = dbcon
									.prepareStatement("SELECT schema_id,schema_name, octet_length(ddltext)  FROM xdata_schemainfo where course_id = ? order by schema_id");
							stmt.setString(1, courseID);
					 		ResultSet rs;
							int index =0; 
							rs = stmt.executeQuery();
							
							//Get connection details for testing
							System.out.println("**************************COURSE ID IN LOADING SCHEMA *********"+stmt.toString());								
							stmt1 = dbcon
									.prepareStatement("SELECT connection_id,connection_name FROM xdata_database_connection WHERE course_id = ?");
							stmt1.setString(1, courseID);

							String output = "";
							ResultSet rs1 = stmt1.executeQuery();
							output += "<option value=\"select\" selected>Select DB connection to test</option>";
							//TODO -  Get DB user and Test user and show it in the drop down
							// CLARIFY : How it will b shown for various Schema's
							// On select schema, show the user name options??????
							while (rs1.next()) { 
								output += " <option value = \""
										+ rs1.getInt("connection_id") + "\"> "
										+ rs1.getInt("connection_id") + "-"
										+ rs1.getString("connection_name") + " </option> ";
							}
						 	 
							rs1.close();
 							//Get Connection details ends
							while (rs. next()) {
								index++;
								String parameters = "schema_id=" + rs.getInt("schema_id")+"&&requestingPage=schemaUpload";
								%>
							 <table style="table-layout:fixed;" width="90%">
							 <tr>
							<!-- <td width="1%"><%=index%>.</td> -->
							 <td width="35%"> 
							   <div> 
							   <input type="hidden" name="schema_id" value="<%=rs.getInt("schema_id")%>">
								   <input type="hidden" name="requestingPage" value="schemaUpload">
								   Schema Id: <%=rs.getInt("schema_id") %>&nbsp;
									Name: <%=rs.getString("schema_name")%>
		   					    </div>
								</td>
								<td width="5%">
								<input class="view" name="viewsubmit" 
									type="button" id="<%=rs.getInt("schema_id") %>-schemaUpload" 
									value="View" onclick="window.location.href='showSchemaFile.jsp?schema_id=<%=rs.getInt("schema_id")%>'">
								</div>
								</td> 
								<td width="22%" align="center">
								<div class="testButton" style='display:block;'>
									<input class="test" name="testsubmit" 
									type="button" id="<%=rs.getInt("schema_id") %>-schemaUpload" 
									value="Test">
								</div> 
								<div class="selectConnection" style='display:none;'>
								<select class ="dbConnection" id="<%=parameters%>" name="dbConnection"  style="clear:both;"> 
									 <%=output %></select>
								</div>
				
 								</td><td width="8%"> 
									<input class="deleteId" type="button" name="delete" id="<%=rs.getInt("schema_id")%>" value="Delete">
								</td>
								</tr>  
								<!-- <div class="statusMessage"> -->
								<tr> 
								<td colspan='3'>
								<label class="schemaUploadTestMessage" id="schemaUploadTestMessage<%=rs.getInt("schema_id") %>" style='display:none;color:green;'>File test on selected connection successful.</label>
								<label class="schemaUploadTestFailedMessage" id="schemaUploadTestFailedMessage<%=rs.getInt("schema_id") %>" style='display:none;color:red;'>File test failed.</label>
								</td>
								</tr>
								
							</table> 
							<!-- <div class="statusMessage">
								<div class="schemaUploadTestMessage" id="schemaUploadTestMessage" style='display:none;color:green;'>File test successful.</div>
								<div class="schemaUploadTestFailedMessage" id="schemaUploadTestFailedMessage" style='display:none;color:red;'>Uploaded file test failed.
								</div> -->
								<%
							}
							rs.close();
						} catch (Exception err) {
							err.printStackTrace();
							throw new ServletException(err); 
						}
						finally{
							dbcon.close();
						}		
					%>
					
					</fieldset>
			</div>
	
		
	</div>
	
</body>
</html>