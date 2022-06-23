<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@ page import="java.text.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>
<%@page import="database.CommonFunctions"%>
<%@page import="com.google.gson.Gson"%>
<%@page import="java.lang.reflect.Type"%>
<%@page import="com.google.gson.reflect.TypeToken"%>
<%@page import="com.google.gson.JsonArray"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "https://www.w3.org/TR/html4/loose.dtd">
<html>
<head>  
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>
<link rel="stylesheet" href="css/structure.css" type="text/css" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript" src = "scripts/jquery.js"></script>
<title>Edit Assignment</title>

<link rel="stylesheet" type="text/css" href="scripts/datetimepicker/jquery.datetimepicker.css"/ >
<script src="scripts/datetimepicker/jquery.js"></script>
<script src="scripts/datetimepicker/jquery.datetimepicker.js"></script>
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
	padding-top:5px;
}
.fieldset #loadDefaultDataSets div#dSet{
	height:10px;
	width:100%;
	float:right;
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
<script type="text/javascript" src="scripts/ManageQuery.js"></script>
<script type="text/javascript">



	function sendToViewEditPage(edit){
		var a = $(edit).attr('id');
		window.location.href="ListOfTesterQuestions.jsp?AssignmentID="+a; 
	} 

	function onSubmit(id){
		
		var a = confirm('Are you sure you want to delete the application?');
		//alert(a);
		if(a==1) 
			window.location.href="deleteAssignment.jsp?assignmentid="+id;
		else
			return false; 
	}function toggleDiv(id){
		$(id).toggle();		
	}
	
	$( document ).ready(function() { 
		$('#showHelp').hide();
		
		
		$('#helpInteractive').on('click',function(e){
			$('#showHelp').show();
		});
		
	$('.schemaId').on('change',function(e){
		//alert("OnChange---");
		schemaSel = $(this).val(); 
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
	});

});
</script> 
</head>
<body>
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}
%>
  <br/>
	<div>
		<%
	 /*This JSP is used for TESTER login to show the application - where the user can give a query and test the correctness*/
	 
			int assignID = Integer.parseInt(request.getParameter("AssignmentID"));
			String courseID = (String) request.getSession().getAttribute(
					"context_label");
			String output = "";
			int connection_id = 0;
			int schema_id = 0;
			output += "<form class=\"assgnmentForm\" name=\"assgnmentForm\" action=\"UpdateExistingAssignment.jsp?AssignmentID="
					+ assignID
					+ "\" method=\"post\" onsubmit=\"return(validate());\" > \n"

					+ "<div class=\"fieldset\">	<fieldset>	<legend> Editing Application Details</legend> \n"
					+ "<label  style='font-weight:bold;'>Application ID: <label>"
					+ assignID
					+ "</label></label> <br/><br/>";

			//get connection
			//Connection dbcon = (new DatabaseConnection()).dbConnection();

			/**store details of assignment*/
			String asDescription = "", dbType = "", jdbcUrl = "", dbUser = "", dbPassword = "", schemaId = "";
			/**get time stamp details*/
			Timestamp start = null;
			Timestamp end = null;
			String chk = null;
			String dsSet="<div><div id=\"loadDefaultDataSets\" style='display:none;'>";
			
			try(Connection dbcon = (new DatabaseConnection()).dbConnection()) {
				PreparedStatement stmt1 = dbcon
						.prepareStatement("SELECT * FROM xdata_assignment where assignment_id=? and course_id=?");
				stmt1.setInt(1, assignID);
				stmt1.setString(2, courseID);
				ResultSet rs1 = stmt1.executeQuery();
				if (rs1.next()) {
					output += "<div><label >Application Name</label>"  
								+ "<input value = '"+rs1.getString("assignmentName") + "' name='assignment_name'/></div>";
					connection_id = rs1.getInt("connection_id");
	 					schema_id = rs1.getInt("defaultschemaid");	
	 					String value = rs1.getString("defaultDSetId");
	 					//System.out.println("Conn and Schema ID = " + connection_id + "--" +schema_id );
					output+= "<div></div><div><label >Application Description</label>"
						+ "<input value = '"+rs1.getString("description") + "' name='description'/></div>";
	 				

						//start=rs.getString("end_date");
						PreparedStatement stmt2 = dbcon
						.prepareStatement("SELECT * from xdata_sampledata where course_id=? and schema_id=?");
						stmt2.setString(1, courseID);
						stmt2.setInt(2,schema_id);
						ResultSet rs2= stmt2.executeQuery();
						String sampleDataName = "";
						int sampleDataId = 0;
						if(value != null){
							Gson gson = new Gson();
							Type listType = new TypeToken<String[]>() {}.getType();
		                    String[] dsList = new Gson().fromJson(value, listType);
		                	dsSet += "<label>Please select the default datasets for evaluation.</label>"; 
		                    if(dsList != null && dsList.length != 0){
		                    	for(int i=0;i<dsList.length;i++){
		                    		while(rs2.next()){
		                    			if(rs2.getString("sampledata_id").equalsIgnoreCase(dsList[i])){
		                    				sampleDataName = rs2.getString("sample_data_name");
		                    				dsSet += "<div id='dSet'><input type=\"checkbox\" name=\"defaultDSId\" checked value=\""+rs2.getString("sampledata_id")+"\"/><label id=\"dsname\">"+rs2.getString("sample_data_name")+"</label>";
		                    				dsSet+="</div><br/>";
		                    				break;
		                    				//continue;
		                    			}
		                    			else{
		                    				dsSet += "<div id='dSet'><input type=\"checkbox\" name=\"defaultDSId\" value=\""+rs2.getString("sampledata_id")+"\"/><label id=\"dsname\">"+rs2.getString("sample_data_name")+"</label>";
		                    				dsSet+="</div><br/>";
		                    			}
		                    		}
		                    	}
		                    }
		                	else{
		                		while(rs2.next()){
		                		dsSet += "<div id='dSet'><input type=\"checkbox\" name=\"defaultDSId\" value=\""+rs2.getString("sampledata_id")+"\"/><label id=\"dsname\">"+rs2.getString("sample_data_name")+"</label>";
	            				dsSet+="</div><br/>";}
	            			}
		                    dsSet += "<p></p></div></div>";
					}  else{
						while(rs2.next()){
							dsSet += "<label>Please select the default datasets for evaluation.</label>"; 
							dsSet += "<div id='dSet'><input type=\"checkbox\" name=\"defaultDSId\" value=\""+rs2.getString("sampledata_id")+"\"/><label id=\"dsname\">"+rs2.getString("sample_data_name")+"</label>";
        					dsSet+="</div><br/>";
        					}
						dsSet += "<p></p></div></div>";
					}
				}
			
			PreparedStatement stmt = dbcon
					.prepareStatement("SELECT connection_id,connection_name FROM xdata_database_connection WHERE course_id = ?");
			stmt.setString(1, courseID);
			String outp = "";
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				outp += " <option value = \"" + rs.getInt("connection_id")
						+ "\" ";
				if(rs.getInt("connection_id")== connection_id){
					outp += "selected";
				}
				outp += "> " + rs.getInt("connection_id") + "-"
						+ rs.getString("connection_name") + " </option> ";
			}
			
			rs.close();

			output += "<div><label>Database Connection</label>  <select id=\"connID\" name=\"dbConnection\"> "
					+ outp + "</select></div>";
			
			//output += outp;
			outp = "";

			stmt = dbcon
					.prepareStatement("SELECT schema_id,schema_name FROM xdata_schemainfo WHERE course_id = ?");
			stmt.setString(1, courseID);
			rs = stmt.executeQuery();
			
			
			while (rs.next()) {
				outp += " <option value = \"" + rs.getInt("schema_id")
						+ "\" ";
				if(rs.getInt("schema_id")== schema_id){
					outp += "selected";
				}
				outp+= "> " + rs.getInt("schema_id") + "-"
						+ rs.getString("schema_name") + " </option> ";
			}
			
			output += "<div><label>Database Schema</label>  <select class=\"schemaId\" id = \""+courseID+"\" name=\"schemaid\"> "
					+ outp + "</select></div>";
				//	System.out.println("-----DSSET ----- " + dsSet);
			output += dsSet;
			
			database.CommonFunctions util = new database.CommonFunctions();
			DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			 Calendar calobj = Calendar.getInstance();
		     System.out.println(df.format(calobj.getTime()));
		
			output += "<input name = \"start\" type=\"hidden\" value='"+df.format(calobj.getTime()) +"' />";
			output += "<input name = \"end\" type=\"hidden\" value= \"9999/12/31 00:00\" /> ";
			output += "<br/><input  type=\"submit\" id=\"sub\" value=\"Update\">";
			output += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
			output +="<input name=\"editQuestions\" type = \"button\" id="+assignID+" onClick=\"sendToViewEditPage(this);\" value=\"Edit Questions\">";
			output += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
			
			output += "&nbsp;&nbsp;<input name=\"Delete\" type=\"button\" value=\"Delete Application\" id=\""+assignID+"\" onclick=\"onSubmit(this.id);\" </div>";
			
			out.println(output); 
			dbcon.close();
			} catch (Exception err) {
				err.printStackTrace();
				throw new ServletException(err);	
			}
			
		%>
	
		</fieldset>
	</div>
	</div>
</body>
</html>