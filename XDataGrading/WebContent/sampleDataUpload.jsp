<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html> 
<head> 
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript" src = "scripts/jquery.js"></script>
<script type="text/javascript" src="scripts/wufoo.js"></script> 
<link rel="canonical" 
	href="http://www.wufoo.com/gallery/designs/template.html">
<title>Upload Sample Data</title>

<script>
var schemaSel;
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
	var opt = confirm("Do you want to delete the sample data?");	
	if(opt == true){
		window.location = "deleteSampleData.jsp?schema_id="+a;		 
	} 
	else{
		return false;	
	}
    });

$( document ).ready(function() { 		
	
	$('.test').click(function (event) {

		var self = this;  
		var a = $(this).attr('id');
		//alert("a = "+a);
		var dataString = "schema_id="+a;
		//alert("dataString = " + dataString);
		$(self).parent().find($('.test')).hide();
		$(self).parent().parent().find($('.selectConnection')).val('select').prop('selected',true);
		$(self).parent().parent().find($('.selectConnection')).show();		 
		var sel = $(self).parent().parent().find($('.dbConnection'));
		$(sel).val('select').prop('selected',true);
		
		//alert("self.3 parent = "+$(self).parent().parent().parent().get(0).tagName);
		$(sel).on('change',function(e){
			e.preventDefault();	 
			if($(sel).val() != "select"){
				dataString= $(sel).attr('id') +"&dbConnection="+$(sel).val();
			}else{ 
				alert("Please select/add database connection to test");
			} 
			 
			$.ajax({ 
		        type: "POST",  
		        url: "TestUploadedFile", 
		        data: dataString,
		        context:$(this),        
		        success: function(data) {
		        	try{
		        		//Self is select first parent is div and next parent is td
		        		$(self).parent().parent().find($('.selectConnection')).hide(); 
		        		$(self).parent().parent().find($('.test')).show();
		        		$(self).parent().parent().parent().parent().parent().find($('.dataUploadTestMessage')).show();
		        	}catch(err){ 
		        		$(self).parent().parent().find($('.test')).show();   
		        		$(self).parent().parent().find($('.selectConnection')).hide();
		        		$(self).parent().parent().parent().parent().parent().find($('.dataUploadTestFailMessage')).show();
			        	 
		        	}
		        }, 
		        error : function(xhr, status, thrownError){
		        	
		        	$(self).parent().parent().find($('.test')).show();   
		        	$(self).parent().parent().find($('.selectConnection')).hide();
		        	$(self).parent().parent().parent().parent().parent().find($('.dataUploadTestFailMessage')).show();
		        	    		         	    	 
		        }
		        
		      });
		});    
	});

	if(getParameterByName("error") != ""){
		$('#dataUploadErrorMessage').show();
		 setTimeout( function(){
			 $('#dataUploadErrorMessage').hide();
			  }, 60000*10);
	}  
	else{ 
		$('#dataUploadErrorMessage').hide();   
	}

	
	
	if(getParameterByName("Delete") != ""){
		$('#dataDeleteMessage').show();
		 setTimeout( function(){ 
			 $('#dataDeleteMessage').hide();
			  }, 60000*10);
	} 
	else{ 
		$('#dataDeleteMessage').hide();   
	}
	
	if(getParameterByName("upload") != ""){
		$('#dataUploadMessage').show();
		 setTimeout( function(){
			 $('#dataUploadMessage').hide();
			  }, 60000*10);
	}  
	else{ 
		$('#dataUploadMessage').hide();   
	}
	
	
if(getParameterByName("noCorrectSchema") != ""){
	
	$('#dataUploadSchemaFailedMessage').show();
		 setTimeout( function(){
			 $('#dataUploadSchemaFailedMessage').hide();
			  }, 60000*10);
	}  
	else{
		$('#dataUploadSchemaFailedMessage').hide();   
	}

	
	if(getParameterByName("test") != ""){
		$('#dataUploadTestMessage').show();
		 setTimeout( function(){
			 $('#dataUploadTestMessage').hide();
			  }, 60000*10);
	}  
	else{ 
		$('#dataUploadTestMessage').hide();   
		
	}

	
	 if(getParameterByName("selectedOption") != "" && !getParameterByName("selectedOption") != "select"){
		 var value = getParameterByName("selectedOption");
		 $('#schemaId').val(value).prop('selected',true);
		 $('#loadExistingDataFile').show();	 
		 $('#showUpload').show();
	 } 
	 else{
		 $('#schemaId').val('select').prop('selected',true);
		 $('#loadExistingDataFile').hide();
		 $('#showUpload').hide();
	 }

	$('#schemaId').on('change',function(e){
		schemaSel = $(this).val(); 
		window.location = "sampleDataUpload.jsp?selectedOption=" + $(this).val();
	}); 
}); 

	function checkValue1() {
		if (document.getElementById("upload").value != "") {
			return true;
		}// else if(document.){
		else {
		
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
</style>
</head>
<body class="public" id="public">
<%
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
	<div class="fieldset">
	<fieldset>
		<legend>Sample Data</legend><br/>
		Select Schema to upload new data file or to view existing data files.
		<br><br>
		
		<%						
					//get the connection for testing1
					Connection dbcon = (new DatabaseConnection()).dbConnection();
					String output = "";
					int selectedSchemaId = 0;	
					
					try {
 
						PreparedStatement stmt;
				 		stmt = dbcon
								.prepareStatement("SELECT schema_id,schema_name FROM xdata_schemainfo WHERE course_id = ?");
						stmt.setString(1, (String) (String) request.getSession()
								.getAttribute("context_label"));
						 

						output +="<option value=\"select\" selected> Select schema</option>";
						ResultSet rs = stmt.executeQuery();
						while (rs.next()) {
							output += " <option value = \"" + rs.getInt("schema_id")
									+ "\"> " + rs.getInt("schema_id") + "-"
									+ rs.getString("schema_name") + " </option> ";	
						} 						
						rs.close();
					} catch (Exception err) {
						err.printStackTrace();
						out.println("<p style=\"color:red;font-size: 17px;\">Error in retrieving schema file details<p>");
						//throw new ServletException(err); 
					}
					finally{
						//dbcon.close();
					}
					%> 
					<label style="text-align:left;float:left;"><strong>Schema Name</strong></label> <br/>
					<p></p> 
					<select class="schemaId" id="schemaId" name="schemaid" required><%=output%>
					</select>  
					
			<div class="statusMessage">
				<br>
				<label class="dataUploadMessage" id="dataUploadMessage" style='display:none;color:green;'>File upload successful</label>
				<label class="dataUploadErrorMessage" id="dataUploadErrorMessage" style='display:none;color:red;'>File upload failed.</label>
				
				<label class="dataUploadSchemaFailedMessage" id="dataUploadSchemaFailedMessage" style='display:none;color:red;'>Testing failed due to incorrect schema or data file. <br> &nbsp;Please check the log file for details.</label>
				
				  
				</div> 
					<div class="statusMessage" > 
						<label class="dataDeleteMessage" id="dataDeleteMessage"
							style='display: none; color: green;'>Selected
							sample data file(s) deleted.</label> 
					</div>
	</fieldset>
	</div>  
	</div>
	<br> 
	<%
				String selectedSchema =  request.getParameter("selectedOption");
					System.out.println("SelectedSchemaId === "+selectedSchema);
					if(selectedSchema != null  && !selectedSchema.equals("select")){
						
						 selectedSchemaId = Integer.parseInt(selectedSchema);
						 
					%> 
	<div class="showUpload" id="showUpload" style="display:none"> 
		<form name="dataForm" enctype="multipart/form-data"
			action="SampleDataUpload" method="post"> 
			<div class="fieldset">
				<fieldset>
					<legend> Upload Data File</legend>
					<p>
						<label class="field">Upload the SQL script containing the
							initial data. The file should contain scripts for loading data into selected schema. </label>
					</p>
					<input type="hidden" name="schemaID" value="<%=selectedSchemaId%>"> 
					
					<input type="hidden" name="requestingPage" value="sampleDataUpload"> 
									 		  
					<label class="field">Choose File: </label> <input	type="file" required name="dataFile" size="20">
					<input name="testupload" onclick="" type="submit"	id="testupload" value="Test and Upload">
 				  
					<input name="submit" onclick="return checkValue1();" type="submit"	id="upload" value="Upload">
					
				</fieldset> 
			</div>  
		</form>  
	</div> 
	<% } %> 
	<br> 
	<div class = "loadExistingDataFile" id="loadExistingDataFile" style="display:none;">
		<form name="dataDeleteForm"	action="deleteSampleData.jsp" method="post">
			<div class="fieldset"> 
				<fieldset> 
					<legend>Existing Data Files</legend>				
					<p></p> 
					
					<%
					String courseID = (String) request.getSession().getAttribute("context_label");
					//get connection
					try { 
							PreparedStatement stmt,stmt1;
							stmt = dbcon
									.prepareStatement("SELECT schema_id,schema_name  FROM xdata_schemainfo where course_id = ? order by schema_id");
							stmt.setString(1, courseID);
					 		ResultSet rs;
							int index =0; 
							rs = stmt.executeQuery();
						
							//Get connection details for testing

									
							stmt1 = dbcon
									.prepareStatement("SELECT connection_id,connection_name FROM xdata_database_connection WHERE course_id = ?");
							stmt1.setString(1, courseID);

							String outp = "";
							ResultSet rs1 = stmt1.executeQuery();
							outp += "<option value=\"select\" selected>Select DB connection to test</option>";
							//TODO -  Get DB user and Test user and show it in the drop down
							// CLARIFY : How it will b shown for various Schema's
							// On select schema, show the user name options??????
							while (rs1.next()) { 
								outp += " <option value = \""
										+ rs1.getInt("connection_id") + "\"> "
										+ rs1.getInt("connection_id") + "-"
										+ rs1.getString("connection_name") + " </option> ";
							}
						 	 
						rs1.close();
 						session.setAttribute("selectedSchemaId",Integer.valueOf(selectedSchemaId).toString());
						while (rs.next()) { 
							int schemaName = rs.getInt("schema_id");													
					
							
							//Show data files for selected schema only
							if(schemaName == selectedSchemaId){
								PreparedStatement statement = dbcon.prepareStatement("select sampledata_id, sample_data_name, octet_length(sample_data) from xdata_sampledata where course_id=? and schema_id=? order by sampledata_id");
								statement.setString(1, courseID);
								statement.setInt(2,schemaName );
								ResultSet rSet = statement.executeQuery();
								
							index++; 
						
							while(rSet.next()){
								String parameters = "schema_id=" + rs.getInt("schema_id")+
										"&&sample_data_id=" + rSet.getInt("sampledata_id")+
										"&&requestingPage=sampleDataUpload";
							%>
							<table style="table-layout:fixed; width:90%;">
								<!-- Row 1-->
								<tr>
								<td width="30%" class="wrapword">
								<input type="hidden" name="schema_id" value="<%=rs.getInt("schema_id")%>">  
								<input type="hidden" name="sample_data_id" value="<%=rSet.getInt("sampledata_id")%>">  
								 <input type="hidden" name="requestingPage" value="sampleDataUpload"> 
								<%if(rSet.getString("sample_data_name") != null || rs.getInt(5) !=0){ %>
								  Data File: <%=rSet.getString("sample_data_name")%></td>
								  <td width="7%"> 
									<input class="view" name="viewsubmit" type="button" 
									onclick="window.location.href='showSampleData.jsp?schema_id=<%=rs.getInt("schema_id")%>&&sampledata_id=<%=rSet.getInt("sampledata_id")%>'" 
									id="<%=rs.getInt("schema_id") %>-rSet.getInt("sampledata_id")-sampleDataUpload" value="View">  
								  </td>
										<!-- <a  href="showSampleData.jsp?schema_id=<%//=rs.getInt("schema_id")%>"></a></td> -->
										
								<td width="25%" align="center"> 
								<div class="testButton" style='display:block;'>
									<input class="test" name="testsubmit" type="button" 
									id="<%=rs.getInt("schema_id")%>-<%=rSet.getInt("sampledata_id")%>-sampleDataUpload" value="Test">
 								</div>
 								<div class="selectConnection" style='display:none;'>
								<select class ="dbConnection" id="<%=parameters%>" name="dbConnection"  style="clear:both;"> 
									 <%=outp %></select>
								</div>
								</td>
 								<td width="8%"> 
									<input class="deleteId" type="button" name="delete" id="<%=rs.getInt("schema_id")%>-<%=rSet.getInt("sampledata_id")%>" value="Delete">
								</td>  
								<%} else{%> 
								Not Available</td>
								<%   }
									} %>
															 
								</tr>  
								<tr> 
								<td colspan = '4'>
								<label class="dataUploadTestMessage" id="dataUploadTestMessage" style='display:none;color:green;'>File test on selected connection succcessful.</label>
								<label class="dataUploadTestFailMessage" id="dataUploadTestFailMessage" style='display:none;color:red;'>File test on selected connection failed.</label>
								</td>
								</tr>
							
								</table>
								<br>		
								 
						<%} //If loop ends 
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
		</form>
	</div>	
	

</body>
</html>