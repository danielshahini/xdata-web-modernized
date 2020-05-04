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
<script type="text/javascript" src = "scripts/jquery-2.1.4.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Create New Database Connection</title>

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

function getParameterByName(name) { 
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}  



function onSubmit(id){
	
	var a = confirm('Are you sure you want to delete the assignment?');
	//alert(a);
	if(a==1) 
		window.location.href="deleteDatabaseConnection.jsp?connection_id="+id;
	else 
		return false; 
}  

$( document ).ready(function() {
	
	if(getParameterByName("Delete") != ""){
		$('#deleteMessage').show();
		 setTimeout( function(){
			 $('#deleteMessage').hide();
			  }, 60000*10);
	}  
	else{  
		$('#deleteMessage').hide();   
	}
  
	if(getParameterByName("edit") != ""){
		$('#saveEdit').show();
		 setTimeout( function(){
			 $('#saveEdit').hide();
			  }, 60000*10);
	} 
	else{ 
		$('#saveEdit').hide();   
	}

	$('#jdbc').focusin(function(){
		$('#showMessage').show();
		
	}).focusout(function(){
		$('#showMessage').hide();
	});
	
	$('#test').click(function(e){
		e.preventDefault(); 
		var self = this; 
	    $.ajax({ 
	        type: "GET",  
	        url: "UpdateDatabaseConnection", 
	        data: $(".databaseForm").serialize(),
	        context:$(this),        
	        success: function(data) {
	        	//alert("Database Connection test successful.");
	        	$('.connError').hide(); 
	        	$('.connSuccess').show();
	        	
	    	 	var a = confirm("Database Connection test successful."+"\n\n"+"Do you want to save the connection?"); 
	    		if(a == true){ 
	    			 $.ajax({
	    			        type: "POST",   
	    			        url: "UpdateDatabaseConnection", 
	    			        data: $(".databaseForm").serialize(), 
	    			        context:this, 
	    			        success: function(data) {
	    			        	
	    			        	//alert("Database connection saved.");	    			        	
	    			    		location.reload(true);	            
	    			        },
	    			        error : function(xhr, status, thrownError){
	    			        	//alert("fails");	        	  
	    			        	//alert("Connection failed. Please check the connection details.");
	    			        	$('.testc').show();   
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
</head>
<body>
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
		<form class="databaseForm" name="databaseForm"
			action="UpdateDatabaseConnection" method="post"
			style="border: none"> 
 
			<div class="fieldset">
				<fieldset>
					<legend> Create a New Database Connection</legend>
					<div>
						<span>Database Connection Name</span>
						<input placeholder="Specify database connection name"
							name="dbConnectionName" required/> 
					</div>
					
					<div>
						<span>Database Name</span>
						<input placeholder="Specify name of the database for connection"
							name="dbName" required/> 
					</div>
					
					<input type="hidden" name = "course_id"  value="<%= (String)request.getSession().getAttribute("context_label")%>">
					   
					<div> 
					<span>Database Type</span> <select
						name="databaseType" style="clear: both;" required>
						<option value="01">PostgreSQL(default)</option>
						<option value="02"><!--  MySql -->MicrosoftSQLServer</option>
						<option value="03">Oracle</option>
						<option value="04">SQLite</option>
						<option value="05">MySql</option>
					</select>
					</div>
					
					<div>
					<span>JDBC URL	</span>
					<input placeholder="host_name:port_number" id="jdbc" name="jdbcurl"/>
					</div>
					<div> 
					Specify the user name and password for the database where instructor query will be run for generating Datasets.
					</div>
					<div>
					<span>Database User Name</span>
					<input placeholder="Specify database user name" name="dbuserName"/>
					</div>
					 
					<div>
					<span>Database Password</span>  
					<input type="password" name="dbPassword" placeholder="Give Password"/> 
					</div> 
					<div> 
					Specify user name and password for the database where student queries will be evaluated.
					</div>
					<div> 
					<span>Test User Name</span>
					<input placeholder="Specify test user name" name="testUserName"/>
					</div>
					
					<div>
					<span>Test User Password</span> <input
						type="password" name="testPassword" placeholder="Give Password"/>
					</div>
					
					<!-- <div> 
					<span>Port Number</span> <input
						name="port" placeholder="Give Port Number">
					</div>--> 
				  
					<div class="testc" id="testc" style='display:block'>
					<input class="button" name="test" type="submit" id="test" value="Test connection"></div>
					  
					 <div class ="connError" id="connError"  style='display:none'> 
				   		 <label id="connError"> <font color="red">The Database connection failed. Please check connection details.</font></label>
				    </div>
		     		 <div class ="connSuccess" id="connSuccess"  style='display:none'> 
				   		 <label id="connSuccess"><font color="Green">Database Connection tested successfully.  </font></label>				    
				    </div>
				  <!--  <div class="savec" id="savec" style='display:none'>
				    <input class="button" name="save" type="button" id="save" value="Save connection"></div> --> 	
					
					<!-- <div id="dialog" class="dialog"  style="display: none">Database connection test successful. Do you want to save the connection details?</div> -->
			</fieldset> 
			</div>			 
		</form>		
	</div>
	<br> 
	<div>
		 <form name="editDBconnForm" id="editDBConnForm" method="post" 
		 action="editDatabaseConnection.jsp">
			<div class="fieldset"> 
				<fieldset>
					<legend>Existing Connection</legend>
			<div class="statusMessage">
			 			<label class="deleteMessage" id="deleteMessage"
  							style='display: none; color: green;'>Selected 
							database connection(s) deleted.</label>
	 	
			 			<label class="saveEdit" id="saveEdit"
  							style='display: none; color: Green;'>Database connection updated.</label>	
  												
			</div>			
			<p></p> 
			<%						 
					//get the connection for testing1
					Connection dbcon = (new DatabaseConnection()).dbConnection(); 		
					try {
 
						PreparedStatement stmt;
						stmt = dbcon
								.prepareStatement("SELECT * from xdata_database_connection where course_id=? order by connection_id");
						stmt.setString(1, (String) (String) request.getSession()
								.getAttribute("context_label"));
						ResultSet rs = stmt.executeQuery();
						int index =0;  
						
						while (rs.next()) { 
							index++; 
							int conId= rs.getInt("connection_id");
							String dbType = "";
							if(rs.getString("database_type").equals("01")){
								dbType = "PostgreSQL";}
							else if(rs.getString("database_type").equals("02")){
								 dbType="MicrosoftSQLServer";}
								//dbType="MySQL";}
							else if(rs.getString("database_type").equals("03")){
								dbType="Oracle";}
							else if(rs.getString("database_type").equals("04")){
								dbType="SQLite";}
							else if(rs.getString("database_type").equals("05")){
								dbType="MySql";}								 
							%>
						<!-- 	<div class="info" style="float:right">
			   					<a href='deleteDatabaseConnection.jsp?connection_id=<%//=rs.getInt("connection_id")%>' onclick="return confirm('Are you sure you want to delete the connection?')">Delete connection</a> </form>		
						     </div> -->
							<table style="table-layout:fixed; width:98%;">
								<!-- Row 1-->
								<tr> 
								<td width="2%"><%=index%></td>
								<td width="25%"> 
								<!-- <a href="editDatabaseConnection.jsp?DBconnectionId=<%//=rs.getInt("connection_id")%>"> -->
								<b>Connection Id: <%=conId%></b>   
								</a> 
								</td>  
								 
								<td width="53%" class="wrapword">
								Connection Name: <%=rs.getString("connection_name")%>
								</td>
								
							<!-- <td><input name="View" type="button" value="View" onclick="window.location.href='asgnmentList.jsp?assignmentId=<%//=rs.getString("assignment_id")%>"></td> -->
							<td><input name="Edit" type="button" value="Edit" onclick="window.location.href='editDatabaseConnection.jsp?DBconnectionId=<%=rs.getInt("connection_id")%>'"></td>
							<td><input name="Delete" type="button" value="Delete" id="<%=rs.getInt("connection_id")%>" onclick="onSubmit(this.id)"></td>								
									
								</tr>
							 	<!-- Row 2 -->
								<tr>
								<td width="2%"></td>
								<td width="30%">
								Database: <%=dbType%></td><!-- empty cell For alignment -->
								<td width="53%" class="wrapword">
								JDBC URL: <%=rs.getString("jdbc_url")%>
								</td> 
								
								</tr> 
															
								</table> 
								<br>		 
								  
						<%}
						
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