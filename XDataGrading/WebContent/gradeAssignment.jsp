<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%> 
<%@ page import="java.text.*"%>
<%@page import="database.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>   
<link rel="stylesheet" href="highlight/styles/xcode.css">  
<link rel="stylesheet" href="highlight/styles/default.css">
<script src="highlight/highlight.pack.js"></script>  

<script type="text/javascript" src = "scripts/jquery-ui.js"></script>
<script type="text/javascript" src = "scripts/jquery.js"></script>

<link rel="stylesheet" href="css/progressbars/jquery-ui.theme.css" type="text/css" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<script type="text/javascript">   
hljs.initHighlightingOnLoad(); 
$( document ).ready(function() {
	$('.evaluate').click(function(e){
		e.preventDefault(); 
		var destination =this.href;
		var dataString=this.id;
		var self = this;
		var idVal = this.name;
		//alert("Name = " + idVal);
		$.ajax({ 
	        type: "GET", 
	        url: "EvaluateQuestion",
	        data: dataString,
	        context:this,  
	        beforeSend : function() {
	        	//alert("Comes to B4 send");
	        	($('#evaluate'+ this.name)).hide();
	        	($('#progress'+ this.name)).show();
	        	 ($('#status'+ this.name)).hide();
	       }, complete: function(){  
        	 try{ 
        		 ($('#evaluate'+ this.name)).show();
        		 ($('#progress'+ this.name)).hide();
        		 ($('#status'+ this.name)).show();
        		
        	 }catch(err)
     		{
        		 //alert("Comes to complete error");
 	        	($('#evaluate'+ this.name)).show();
 	        	 ($('#progress'+ this.name)).hide();
 	        	($('#evError'+ this.name)).show();
	        	 setTimeout( function(){
	        		 ($('#evError'+ this.name)).hide();
	    			  }, 10000*10); 
	        }
        	  
          },
	        success: function(data) { 
	        	try{
	        		if(this.success){
				    	//alert($(self).parent().get( 0 ).tagName);
				    	// alert("Success : "+($('#evaluate'+ this.name)));
	        			 ($('#evaluate'+ this.name)).show();
	            		 ($('#progress'+ this.name)).hide();
	            		 ($('#status'+ this.name)).show();
	        		}
	        	}
	        		catch(err)
	        		{
	        			($('#evaluate'+ this.name)).show();
	    	        	($('#progress'+ this.name)).hide();
	    	        	($('#evError'+ this.name)).show();
	    	        	 ($('#status'+ this.name)).hide();
	   	        	 setTimeout( function(){
	   	        		 ($('#evError'+ this.name)).hide();
	   	    				  }, 10000*10); 
	   	       		 }
	    	        
	        	
	        }, 
	        error : function(xhr, ajaxOptions, thrownError){
	        	($('#evaluate'+ this.name)).show();
	        	 ($('#progress'+ this.name)).hide();
	        	 ($('#status'+ this.name)).hide();
	        	($('#evError'+ this.name)).show();
	        	 setTimeout( function(){
	        		 ($('#evError'+ this.name)).hide();
	    			  }, 10000*10); 
	        }
	      }); 
	      return false; 
	      
	});
});
</script>
<title>Grade Assignment</title>
<style>

li {
	text-align: left
}

textarea,select {
	font: 12px/12px Arial, Helvetica, sans-serif;
	padding: 0;
}

input {
	font: 15px/15px Arial, Helvetica, sans-serif;
	padding: 0;
}

fieldset.action {
	background: #9da2a6;
	border-color: #e5e5e5 #797c80 #797c80 #e5e5e5;
	margin-top: -20px;
}

label {
	font-size: 15px;
	font-weight: bold;
	color: #666;
}

label span,.required {
	color: red;
	font-weight: bold;
	font-size: 17px;
}

td {
	text-align: center;
	vertical-align: middle;
}

a:link {
	color: #E96D63;
	font: 15px/15px Arial, Helvetica, sans-serif;
} /* unvisited link */
a:hover {
	color: #7FCA9F;
	font: 15px/15px Arial, Helvetica, sans-serif;
} /* mouse over link */
.stop-scrolling {
	height: 100%;
	/*overflow: hidden;*/
}

.separator{
	border-right:1px solid black; 
	margin:0px; 
	float: right; 
	margin-right: 3px;
	width:1px;
	margin-left: 2px;
}
#breadcrumbs
{
  position: absolute;
  padding-left:10px;
  padding-right:10px;
  left: 5px;
  top: 5px;
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

if(! Boolean.parseBoolean(session.getAttribute("ltiIntegration").toString())){
		%> 
<div id="breadcrumbs"> 
  <a style='color:#353275;text-decoration: none;' href="CourseHome.jsp" target="_top">Home</a> &nbsp; >> &nbsp;
   <a href="InstructorHome.jsp?contextLabel=<%=(String) request.getSession().getAttribute("context_label")%>" style='color:#353275;text-decoration: none;' target="_top"><%=(String) request.getSession().getAttribute("context_label")%></a>&nbsp; >> &nbsp;
   <a href="ListAllAssignments.jsp" style='color:#353275;text-decoration: none;' target="_self">Assignment List</a>&nbsp; >> &nbsp;
   <a href="asgnmentList.jsp?assignmentId=<%=request.getParameter("AssignmentID") %>" target="_self" style='color:#353275;text-decoration: none;'>Assignment Details</a>&nbsp; >> &nbsp;    
   <a href="#" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>Grade Question</a>
 
  </div> 
<%}else{ %>
<div id="breadcrumbs"> 
   <a href="asgnmentList.jsp?assignmentId=<%=request.getParameter("AssignmentID") %>" target="_self" style='color:#353275;text-decoration: none;'>Assignment Details</a>&nbsp; >> &nbsp;    
   <a href="#" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>Grade Question</a>
 
 </div> 
<%} %>
<br/>
<div>
	<div class="fieldset">
		<fieldset>
			<legend> Assignment Instructions</legend>
			
			<%
							String loginUsr=(String)session.getAttribute("LOGIN_USER");
							
							
									String courseID = (String) request.getSession().getAttribute(
										"context_label");
									int assignID = Integer.parseInt(request.getParameter("AssignmentID"));
									String instructions = (new CommonFunctions())
											.getAssignmentInstructions(courseID, assignID);
									
									out.println(instructions);
						%>
		</fieldset> 
		<fieldset>
			<legend> List of Questions</legend>
<!-- 			<form class="wufoo" name="Form" action="evaluateAssignment.jsp"	method="post"> -->
				<%						
							//get connection
							Connection dbcon = (new DatabaseConnection()).dbConnection();
							Timestamp start = null;
							Timestamp end = null;
				
				
					String output = "<table cellspacing=\"20\"  class=\"authors-list\" id=\"queryTable\" align=\"center\">  <tr> <th >Query ID</th> <th >Question Description</th>  <th >Correct Query</th> <th> </th><th> </th></tr>"
							+ "\n";
					int qID = 0;
					String text = "";
					boolean matchAll;
					String matchOption="";
				//	String correct = "";
					int index=0;
					int count=0;
					//execute queryinfo table to get qIDs
					try {
						PreparedStatement stmt;
						stmt = dbcon
								.prepareStatement("SELECT * FROM xdata_qinfo  where assignment_id=? and course_id = ? order by question_id");
						stmt.setInt(1, assignID);
						stmt.setString(2, courseID);
						ResultSet rs;
						rs = stmt.executeQuery(); 
						while (rs.next()) {
							count=0; 
							qID = Integer.parseInt(rs.getString("question_id").trim());
							text = rs.getString("querytext");
							matchAll = rs.getBoolean("matchallqueries");
							if(matchAll){
								matchOption = "Match all results";
							}
							else{
								matchOption = "Match any one result";
							} 
							//correct = rs.getString("correctquery");							
							String evaluate = "EvaluateQuestion?assignment_id="
									+ assignID + "&&question_id=" + qID
									+ "'\"target = \"rightPageBottom\"";
							String params = "assignment_id="
									+ assignID + "&&question_id=" + qID + "'\"target = \"rightPageBottom\"";;
							
							String status = "QueryStatus?assignment_id=" + assignID
									+ "&&question_id=" + qID
									+ "'\"target = \"rightPageBottom\"";
						
							%>
							
							<div class="questionelement">
								<div class="question"><span>Q<%= qID %>. </span><%= text %></div>
								<div class="answer">
									<div class="matchOption" id="matchOption<%=qID %>" style='display:none;font-family: helvetica'>
										<b>Multiple SQL Option: <%=matchOption %></b>
									</div>
									<br/>			
									<%
									String queries = "select sql from xdata_instructor_query where course_id = ? and assignment_id = ? and question_id=?";
									PreparedStatement stmt1 = dbcon.prepareStatement(queries);
									stmt1.setString(1, courseID);
									stmt1.setInt(2, assignID);
									stmt1.setInt(3,qID);
									 
									ResultSet rs2 = stmt1.executeQuery();
									while(rs2.next()){	
										count++;
									%> 
									<%if(count > 1) {%>  
										<script>
										if(document.getElementById('matchOption'+<%=qID%>).style.display == "none"){
											document.getElementById('matchOption'+<%=qID%>).style.display='block';
										}
										</script> 
									<%} %>		
									<pre><code class="sql">
									<span>Ans. </span><%= CommonFunctions.encodeHTML(rs2.getString("sql")) %></code></pre>
									<br/>
									
									<%} %>
								</div> 
								<div class="editbutton"> 
	        			  			 
	        			  			<div id="evaluate<%=index %>" class="evaluate" 
	        			  			style='display:block;font-family:Courier;color:#353275;width:90%;'>
		        			  			<a class="evaluate" name='<%=index %>' href=' <%=evaluate %>' id='<%=params%>'>
		        			  			<%="Evaluate" %></a>
												
									</div>  
						
								<div id="status<%=index%>" class="status" style='font-family:Courier;color:#353275;display:none;'>
								<span class = "separator">&nbsp;</span>
										<a href='<%=status %>'>Evaluation Result </a>
											
									</div>
									
									<div id='progress<%=index %>' align="right" class='progress' 
									style='display:none;font-family:Courier;color:#353275;width:90%;'>
										
									Evaluating student answers 
		 							<img src="images/bluebar_dots_ani.gif"  border="0" height ="4%" width="8%"/>
		 						<!-- <span class = "separator">&nbsp;</span> -->
						  			</div>
       			  				<label class="evError" id="evError<%=index%>" 
		        			  		style='display:none;float:right;color:red;font-family: Helvetica;font-size:13px;'>Error during evaluation. Please Check the server log for details.</label>	  		    
	
								 </div>								 	
							</div>
					
						<%	
						index++;
						} 					
						rs.close();
						output = ""; 
					} catch (Exception err) {
						err.printStackTrace();
						throw err;
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