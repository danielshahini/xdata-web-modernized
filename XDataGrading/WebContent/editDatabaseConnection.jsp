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
 <script type="text/javascript" src="scripts/jquery.dialog.min.js"></script> 
<script src="scripts/jquery-ui-dialog.js" type="text/javascript"></script>
<link href="css/jquery-ui-dialog.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src = "scripts/jquery.js"></script>
<script type="text/javascript" src = "scripts/jquery-2.1.4.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
 
<title>Edit Database Connection</title>

<script>
$( document ).ready(function() {
	
	$('#test').click(function(e){
		e.preventDefault(); 
		var self = this; 
	    $.ajax({ 
	        type: "GET",  
	        url: "EditDatabaseConnection", 
	        data: $(".databaseForm").serialize(), 
	        context:$(this),        
	        success: function(data) {
	        	//alert("Database Connection test successful.");
	        	$('.connError').hide(); 
	        	$('.connSuccess').show();   
	        	//$('.savec').show();  
	    		//$('.testc').hide(); 
	    		//$("#dialog").html();
	    		//$("#dialog").dialog("open");
	     	   var a = confirm("Database Connection test successful."+"\n\n"+"Do you want to save changes?"); 
	    	   if(a == true){
	    			 $.ajax({
	    			        type: "POST",   
	    			        url: "EditDatabaseConnection", 
	    			        data: $(".databaseForm").serialize(), 
	    			        context:this, 
	    			        success: function(data) {
	    			        	
	    			        	//alert("Database connection saved.");	    			        	
	    			    		window.location = "NewDatabaseConnection.jsp?edit=true";	            
	    			        },
	    			        error : function(xhr, status, thrownError){
	    			        	//alert("fails");	        	  
	    			        	//alert("Connection failed. Please check the connection details.");
	    			        	$('.testc').show();
	    			        	$('.connSuccess').hide();
	    			        	$('.connError').show();
	    			        	$('.savec').hide(); 		        	    	 
	    		         },     
	    			      });  
	    			 
	    		}else{
	    			
	    			$ (self).dialog('close'); 
	    		} 
	    		
	        }, 
	        error : function(xhr, status, thrownError){
	        	$('.testc').show();   
	        	$('.connError').show(); 
	        	$('.savec').hide();	    		         	    	 
            },
            
	        async:false
	      });  
	      return false;      
});	
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
<body>
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}
else if(session.getAttribute("LOGIN_USER") != null && !session.getAttribute("LOGIN_USER").equals("ADMIN")
	&& session.getAttribute("role") != null &&  (!session.getAttribute("role").equals("instructor") || !session.getAttribute("role").equals("tester"))){
response.sendRedirect("index.jsp?NotAuthorised=true");
session.invalidate();
return;
}
%>
<div id="breadcrumbs"> 
  <a style='color:#353275;text-decoration: none;' href="CourseHome.jsp" target="_top">Home</a> &nbsp; >> &nbsp;
  <a href="InstructorHome.jsp?contextLabel=<%=(String) request.getSession().getAttribute("context_label")%>" style='color:#353275;text-decoration: none;' target="_top"><%=(String) request.getSession().getAttribute("context_label")%></a>&nbsp; >> &nbsp;
    <a style='color:#353275;text-decoration: none;' href="NewDatabaseConnection.jsp" target="_self">Create DB Connection</a>&nbsp; >> &nbsp;
   <a href="#" style='color:#353275;text-decoration: none;font-weight: normal;'>Edit DB connection</a>
 </div> 
 <br/>
	<div>
		<form class="databaseForm" name="databaseForm"
			action="EditDatabaseConnection" method="post"
			style="border: none">
 
			<div class="fieldset"> 
				<fieldset>
					<legend> Edit Database Connection</legend>
					<%
					String connectionID = request.getParameter("DBconnectionId");
					String dbType=null;
					//System.out.println("CONNECTION ID + = "+connectionID);
					//get the connection for testing1
					Connection dbcon = (new DatabaseConnection()).dbConnection();
					int conId = Integer.parseInt(connectionID);
					String courseId = (String) (String) request.getSession().getAttribute("context_label"); 
					try {  

						PreparedStatement stmt;
						stmt = dbcon
								.prepareStatement("SELECT * from xdata_database_connection where course_id=? and connection_id= ?");
						stmt.setString(1, courseId);
						stmt.setInt(2, conId); 
						ResultSet rs = stmt.executeQuery();
						while(rs.next()){ 
							//Get jdbc URL to edit in format hostName:portNumber
							//TODO : Change based on DB used
							/*String jdbcurl = rs.getString("jdbc_url");
						    String url = jdbcurl.substring(jdbcurl.indexOf("://")+3);
						    System.out.println("URL = " + url);
							String urlToEdit = url.substring(0,url.indexOf("/"));
							System.out.println("URL TO EDIT = " +urlToEdit);*/
							dbType = rs.getString("database_type");
				
					%>
					<div>
						<span>Database Connection Name</span>
						<input name="dbConnectionName" value="<%=rs.getString("connection_name") %>" required/> 
					</div> 
					<div>
						<span>Database Name</span>
						<input name="dbName" value="<%=rs.getString("database_name") %>" required/> 
					</div>
					<input type="hidden" name = "course_id"  value="<%=courseId %>">
					<input type="hidden" name = "connection_id"  value="<%=conId %>"> 
					<div>  
					<span>Database Type</span> <select
						id="dbType" name="databaseType" style="clear: both;" required>
						<option value="01" <%if( dbType != null && dbType.equalsIgnoreCase("01")){%>selected<%}%>>PostgreSQL(default)</option>
						<option value="02" <%if( dbType != null && dbType.equalsIgnoreCase("02")){%>selected<%}%>><!-- MySql -->MicrosoftSQLServer</option>
						<option value="03" <%if( dbType != null && dbType.equalsIgnoreCase("03")){%>selected<%}%>>Oracle</option>
					</select> 
					</div>
					 	
					<div> 
					<span>JDBC URL</span>
					<input onfocus="<h5>Format: host_name: port_number</h5>" name="jdbcurl" value="<%=rs.getString("jdbcData") %>" />
					</div>
									 
					<div>
					<span>Database User Name</span>
					<input name="dbuserName" value="<%=rs.getString("database_user") %>"/>
					</div>
					
					<div>
					<span>Database Password</span>  
					<input type="password" name="dbPassword" value="<%=rs.getString("database_password") %>"/> 
					</div> 
					
					<div> 
					<span>Test User Name</span>
					<input name="testUserName" value="<%=rs.getString("test_user") %>"/> 
					</div> 
					
					<div>
					<span>Test User Password</span> <input
						type="password" name="testPassword" value="<%=rs.getString("test_password") %>"/>
					</div>
					<%} 
					}
					finally{
						dbcon.close();
					}
					 
					%>			 
					<div class="testc" id="testc" style='display:block'>
					<input class="button" name="test" type="submit" id="test" value="Test connection"></div>
					 <br>  
					<div class ="connError" id="connError"  style='display:none'> 
				   		 <label id="connError"> <font color="red">The Database connection failed. Please check connection details.</font></label>
				    </div>  
		     		 <div class ="connSuccess" id="connSuccess"  style='display:none'> 
				   		 <label id="connSuccess"><font color="Green">Database Connection tested successfully.  </font></label>				    
				    </div>	<br>  
				    <!-- <div class="savec" id="savec" style='display:none'> -->
				    <!-- <input class="button" name="save" type="button" id="save" value="Save connection"></div> 	-->
					 
					<!-- <div id="dialog" class="dialog"  style="display: none">Database connection test successful. Do you want to save the connection details?</div> -->
			</fieldset>  
			</div>			
		 </form> 	
	</div>




</body>
</html>