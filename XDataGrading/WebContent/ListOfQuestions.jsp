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
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="highlight/styles/xcode.css"/>  
<link rel="stylesheet" href="highlight/styles/default.css"/>
<link rel="stylesheet" href="css/structure.css" type="text/css"/>
<script src="highlight/highlight.pack.js"></script>
<script type="text/javascript" src = "scripts/jquery.js"></script>


<script type="text/javascript">   
//hljs.initHighlightingOnLoad();

hljs.initHighlighting.called = false;
hljs.initHighlighting();

function getParameterByName(name) { 		
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
} 
var asId = getParameterByName("AssignmentID");
$( document ).ready(function() {	
	//hljs.initHighlightingOnLoad(); 
	$('.generate').click(function(e){
		e.preventDefault();
		var destination =this.href;
		var dataString=this.id;
		var index = this.name;
		var self = this;
		//alert($(self).parent().get( 0 ).tagName);  
		$.ajax({ 
	        type: "GET", 
	        url: "AssignmentChecker",
	        data: dataString,
	        context:this,  
	        beforeSend : function() {
	        	//$(self).parent().parent().find($('.generateData')).hide();
	        	$('#generateData'+index).hide();
	        	$('#showData'+index).hide();
				$('#progress'+index).show();
	         }, /*complete: function(){ 
        	 try{ 
        		 //alert("Comes to complete success");
        		
        			$(self).parent().parent().find($('.generateData')).show(); 
	        		$(self).parent().parent().find($('.showData')).show();
	        		$(self).parent().parent().find($('.progress')).hide();
        		 
        	 }catch(err)
     		{
        		// alert("Comes to complete error");
     			$(self).parent().parent().find($('.generateData')).show(); 
 	        	$(self).parent().parent().find($('.showData')).hide(); 
 	        	$(self).parent().parent().find($('.progress')).hide();
 	        	$(self).parent().parent().find($('.dataGenError')).show(); 
	        	 setTimeout( function(){
	        		 $(self).parent().parent().find($('.dataGenError')).hide();
	    			  }, 60000*10); 
	        }
        	  
          },*/
	        success: function(data) { 
	        	try{
	        		//if(this.success){
				    	//alert($(self).parent().get( 0 ).tagName);
				    	$('#generateData'+index).show();
			        	//$(self).parent().parent().find($('.generateData')).show(); 
			        	$('#showData'+index).show();
						$('#progress'+index).hide();
					        	
	        		//}
	        	}
	        		catch(err)
	        		{
	        			//alert("Error occurred during data generation success fnunc." + err.toString());
	        			$('#generateData'+index).show();
	        			//$(self).parent().parent().find($('.generateData')).show(); 
	        			$('#showData'+index).hide(); 
						$('#progress'+index).hide();
						$('#dataGenError'+index).show(); 
			   	        	 setTimeout( function(){
			   	        		$('#dataGenError'+index).hide();
			   	    			  }, 60000*10); 
	    	        	
	    	        	//$(self).parent().parent().parent().find($('.edit')).show(); 
	    	        
	    	        	//alert("Internal Error occurred during Data Generation." + err); 
	    	        	
	        		} 
	        	
	        }, 
	        error : function(xhr, ajaxOptions, thrownError){
	        	//alert("plain error" + $(self).parent().parent().get( 0 ).tagName); 
	        	//alert("plain error" +thrownError);
	        	$('#generateData'+index).show();
	        //	$(self).parent().parent().find($('.generateData')).show(); 
	        	$('#showData'+index).hide(); 
				$('#progress'+index).hide();
				$('#dataGenError'+index).show(); 
	        	 setTimeout( function(){ 
	        		 $('#dataGenError'+index).hide();
	    			  }, 60000*10); 	        		        	  
            }
	      }); 
	      return false; 
	});
	//Method to evaluate all questions in Assignment 
	$('.evaluateAssignment').click(function(e){
		e.preventDefault();
		var dataString="assignment_id="+this.id;
		var self = this;
		//alert("getParameterByName = "+asId);
		var surl ="asgnmentList.jsp?showQuestions=true&&assignmentId="+this.id;
	
		//alert("ajax call" + dataString);
		$.ajax({ 
	        type: "GET",
	        url: "EvaluateAssignment",
	        data: dataString,
	        context:this,  
	        beforeSend : function() {
	        	//alert("Comes to B4 send");
	        	
	        	 ($('#evaluateAssignment')).hide();
    			 ($('#evaluatedAssignment')).hide();
    			 ($('#asgnEvaluationStatus')).show();
    			 ($('#reEvaluateAssignment')).hide();
    			 
	        	
	       }, complete: function(){  
        	 try{
        		// alert("Comes to complete");
        		 ($('#evaluatedAssignment')).show();
        		 ($('#asgnEvaluationStatus')).hide();
        		 ($('#reEvaluateAssignment')).show();
        		 ($('#evaluateAssignment')).hide();
        		 window.location.replace(surl);
        	 }catch(err)
     		{
        		// alert("Comes to complete error");
        		 ($('#evaluateAssignment')).show();
    			 ($('#evaluatedAssignment')).hide();
    			 ($('#asgnEvaluationStatus')).hide();
    			 ($('#reEvaluateAssignment')).hide();
	        	 ($('#asgnEvalStatus'+ this.name)).hide();
	        	 setTimeout( function(){
	        		 ($('#evError'+ this.name)).hide();
	    				  }, 10000*10); 
	       		 }
	        
        	  
          },
	        success: function(data) { 
	        	try{
	        		if(this.success){
				    	//alert($(self).parent().get( 0 ).tagName);
				    	// alert("Success : ");
	        			 ($('#evaluatedAssignment')).show();
	            		 ($('#evaluateAssignment')).hide();
	            		 ($('#asgnEvaluationStatus')).hide();
	            		 ($('#reEvaluateAssignment')).hide();
	            		 window.location.replace(surl);
	        		}
	        	}
	        		catch(err)
	        		{
	        			 ($('#evaluateAssignment')).show();
	        			 ($('#evaluatedAssignment')).hide();
	        			 ($('#asgnEvaluationStatus')).hide();
	        			 ($('#reEvaluateAssignment')).hide();
	    	        	 ($('#asgnEvalStatus'+ this.name)).hide();
	   	        	 setTimeout( function(){
	   	        		 ($('#evError'+ this.name)).hide();
	   	    				  }, 10000*10); 
	   	       		 }
	    	        
	        	
	        }, 
	        error : function(xhr, ajaxOptions, thrownError){
	          	 ($('#evaluateAssignment')).show();
	   			 ($('#evaluatedAssignment')).hide();
	   			 ($('#asgnEvaluationStatus')).hide();
	   			 ($('#reEvaluateAssignment')).hide();
		        	 ($('#asgnEvalStatus'+ this.name)).hide();
		        	 setTimeout( function(){
		        		 ($('#evError'+ this.name)).hide();
		    				  }, 10000*10); 
		       		 }
	      }); 
	      return false; 
	});
	//Method to evaluate single question
	$('.evaluateQ').click(function(e){
		e.preventDefault(); 
		var destination =this.href;
		var dataString=this.id;
		var indexVal = this.name;
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
	        	($('#evaluate'+ indexVal)).hide();
	        	($('#eprogress'+ indexVal)).show();
	        	 ($('#status'+ indexVal)).hide();
	       }, complete: function(){  
        	 try{ 
        		 ($('#evaluate'+ indexVal)).show();
        		 ($('#eprogress'+ indexVal)).hide();
        		 ($('#status'+ indexVal)).show();
        		
        	 }catch(err)
     		{
        		 //alert("Comes to complete error");
 	        	($('#evaluate'+ indexVal)).show();
 	        	 ($('#eprogress'+ indexVal)).hide();
 	        	($('#evError'+ indexVal)).show();
	        	 setTimeout( function(){
	        		 ($('#evError'+ indexVal)).hide();
	    			  }, 10000*10); 
	        }
        	  
          },
	        success: function(data) { 
	        	try{
	        		if(this.success){
				    	//alert($(self).parent().get( 0 ).tagName);
				    	// alert("Success : "+($('#evaluate'+ this.name)));
	        			 ($('#evaluate'+ indexVal)).show();
	            		 ($('#eprogress'+ indexVal)).hide();
	            		 ($('#status'+ indexVal)).show();
	        		}
	        	}
	        		catch(err)
	        		{
	        			($('#evaluate'+ indexVal)).show();
	    	        	($('#eprogress'+ indexVal)).hide();
	    	        	($('#evError'+ indexVal)).show();
	    	        	 ($('#status'+indexVal)).hide();
	   	        	 setTimeout( function(){
	   	        		 ($('#evError'+ indexVal)).hide();
	   	    				  }, 10000*10); 
	   	       		 }
	    	        
	        	
	        }, 
	        error : function(xhr, ajaxOptions, thrownError){
	        	($('#evaluate'+ indexVal)).show();
	        	 ($('#eprogress'+ indexVal)).hide();
	        	 ($('#status'+ indexVal)).hide();
	        	($('#evError'+ indexVal)).show();
	        	 setTimeout( function(){
	        		 ($('#evError'+ indexVal)).hide();
	    			  }, 10000*10); 
	        }
	      }); 
	      return false; 
	      
	});
	
});

</script>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<!-- <META HTTP-EQUIV="REFRESH" CONTENT="0"> --> 
<title>List Of Questions</title>
<style>

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

if(! Boolean.parseBoolean(session.getAttribute("ltiIntegration").toString())){
		%> 
<div id="breadcrumbs"> 
  <a style='color:#353275;text-decoration: none;' href="CourseHome.jsp" target="_top">Home</a> &nbsp; >> &nbsp;
   <a href="InstructorHome.jsp?contextLabel=<%=(String) request.getSession().getAttribute("context_label")%>" style='color:#353275;text-decoration: none;' target="_top"><%=(String) request.getSession().getAttribute("context_label")%></a>&nbsp; >> &nbsp;
  <a href="ListAllAssignments.jsp" style='color:#353275;text-decoration: none;' target="_self">Assignment List</a>&nbsp; >> &nbsp;
   <a href="asgnmentList.jsp?assignmentId=<%=request.getParameter("AssignmentID") %>" target="_self" style='color:#353275;text-decoration: none;'>Assignment Details</a>&nbsp; >> &nbsp;    
   <a href="#" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>Question List</a>
  </div> 
<%}else{ %>
<div id="breadcrumbs"> 
   <a href="asgnmentList.jsp?assignmentId=<%=request.getParameter("AssignmentID") %>" target="_self" style='color:#353275;text-decoration: none;'>Assignment Details</a>&nbsp; >> &nbsp;    
   <a href="#" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>Question List</a>
  </div> 
<%} %>
<br/>
	<div>
		<div class="fieldset">

			<!-- <fieldset>
				<legend> Assignment Instructions</legend>
				<%			//Get header part
							String loginUsr= (String)session.getAttribute("LOGIN_USER");
							int assignID = Integer.parseInt(request.getParameter("AssignmentID"));
							String courseID = (String) request.getSession().getAttribute(
									"context_label");
							String instructions = (new CommonFunctions())
									.getAssignmentInstructions(courseID, assignID);							
							//out.println(instructions);
							String asgnEvalParams = (Integer.toString(assignID)) ;
							String evaluateAsgn = "EvaluateAssignment?assignment_id="+ assignID;
							boolean isInteractiveAssignment = false;
				%>
			</fieldset>-->
			<br/>
		<th style='font-style:italic;font-weight: bold;font-size: 15;'>
			<fieldset>
				<legend> List of Questions</legend>

			
				<%
							//get connection
							Connection dbcon = (new DatabaseConnection()).dbConnection();
							String asgnName = "";
							boolean asgnEvaluated = false;
							Timestamp start = null;
							Timestamp end = null;
							try{
								PreparedStatement stmt1;
								ResultSet rs1 = null;
								stmt1 = dbcon
										.prepareStatement("SELECT * FROM xdata_assignment where assignment_id=? and course_id=?");
								stmt1.setInt(1, assignID);
								stmt1.setString(2, courseID);
								rs1 = stmt1.executeQuery();
								if (rs1.next()) {
									start = rs1.getTimestamp("starttime");
									end = rs1.getTimestamp("endtime");
									asgnName = rs1.getString("assignmentname");
									asgnEvaluated = rs1.getBoolean("evaluationstatus");
									if(rs1.getBoolean("learning_mode")){
										isInteractiveAssignment = true;
									}
									//start=rs.getString("end_date");
								}						
								rs1.close(); 
							} catch (Exception err) {
								err.printStackTrace();
								throw new ServletException(err);

							}%>
							<!-- Render Assignment Evaluation link -->
							
							<div id='asgnEvaluationStatus' style='display:none;'>
								Evaluating Assignment<img src="images/bluebar_dots_ani.gif"  border="0" height ="4%" width="8%"/>
							</div>
							<%if(asgnEvaluated){%>
									<!-- <div id='evaluatedAssignment' style='display:block;'><label>Assignment Evaluated.</label></div>-->
									<div id='reEvaluateAssignment' style='display:block;'>
										<label>Assignment is evaluated. &nbsp;&nbsp;<a class='evaluateAssignment' href='<%=evaluateAsgn %>' id='<%=asgnEvalParams%>' style='color:blue;font-size: 14;font-weight: bold;'>Re-evaluate Assignment</a></label>
									</div>
							<%}else{%>
								<div id='evaluateAssignment' style='display:block'>
									<label>Assignment Evaluation: 
									<a class='evaluateAssignment' href='<%=evaluateAsgn %>' id='<%=asgnEvalParams%>' style='color:blue;font-size: 14;font-weight: bold;'>
										Evaluate all questions
									</a>
									</label>
								</div>
							<%}%>
							
							<div id='asgnEvalStatus' style='display:none;float:right;color:red;font-family: Helvetica;font-size:13px;'><label>Error in assignment evaluation. Please check log files for details.</label></div>
							</div>
						<%	//System.out.println("Start :" + start);
							//System.out.println("End :" + end);
							//now check whether current time is less than start time.Then only assignment can be edited
							boolean yes = false;
							String matchOption = "";
							boolean matchAll;
							SimpleDateFormat formatter = new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss");
							formatter.setLenient(false);
							
							Calendar c = Calendar.getInstance();

							String currentDate = formatter.format(c.getTime());
							java.util.Date current = formatter.parse(currentDate);
							int newQId = 1;
							int index=0;
							int qIndexDisplay =0;
							String output = "<table  cellspacing=\"20\"  class=\"authors-list\" id=\"queryTable\" align=\"center\"> <tr> <th >Question ID</th>       <th >Question Description</th>  <th >Correct Query</th> <th> </th></tr>";
							try {
								
								PreparedStatement stmt;
								stmt = dbcon
										.prepareStatement("SELECT * FROM xdata_qinfo  where assignment_id=? and course_id=? order by question_id");
								stmt.setInt(1, assignID);
								stmt.setString(2, courseID);
								ResultSet rs = stmt.executeQuery();
									
								while (rs.next()) {
									qIndexDisplay++;
									int queryIndex=1;
									int qID = rs.getInt("question_id");
									newQId = qID+1;
									
									String desc = rs.getString("querytext");
									String correctQuery = rs.getString("correctquery");
									int marksAllotted = rs.getInt("totalmarks");
									matchAll = rs.getBoolean("matchallqueries");
									if(matchAll){
										matchOption = "Match all results";
									}
									else{
										matchOption = "Match any one result";
									}
									//System.out.println(qID + ": " + correctQuery);

									String remote = "QuestionDetails.jsp?AssignmentID="
											+ assignID +"&&assignmentName="+asgnName+ "&&courseId=" + courseID
											+ "&&questionId=" + qID;
									
									String evaluate = "EvaluateQuestion?assignment_id="
											+ assignID + "&&question_id=" + qID;
											//+ "\" target = \"rightPageBottom\"";
									String params = "assignment_id="
											+ assignID + "&&question_id=" + qID ;
									 
									String status = "QueryStatus?assignment_id=" + assignID
											+ "&&question_id=" + qID;
											 
				%>   <!-- RENDER THE PAGE FOR ALL EXISTING QUESTIONS -->
					<div class="questionelement"> 						
					<div class="question" id="Question"><span>Q<%= qIndexDisplay %>. </span><%= desc %><br/>
					<span>Marks Allotted : </span><%= marksAllotted %>
					</div>
						 			 
					<div class="answer">
					<div class="matchOption" id="matchOption<%=qID %>" style='display:none;font-family: helvetica'>
						<b>Multiple SQL Option: <%=matchOption %></b>
					</div>
						
						<%/**Added for multiple queries**/
						int question_id =qID;
						PreparedStatement stmt1;
						boolean isQuestionEvaluated = false;
						stmt1 = dbcon 
								.prepareStatement("SELECT * FROM xdata_instructor_query  where assignment_id=? and course_id=? and question_id=? order by query_id");
						stmt1.setInt(1, assignID);
						stmt1.setString(2, courseID);
						stmt1.setInt(3,question_id);
						
						ResultSet rs1 = stmt1.executeQuery();%>
							
							 
						<%while (rs1.next()) {
						String queries = rs1.getString("sql");
						String savedQuery = queries;
						//System.out.println(" Saved Query = " + queries);
						int query_id = rs1.getInt("query_id");
						int marksPerQuery = rs1.getInt("marks");
						isQuestionEvaluated = rs1.getBoolean("evaluationstatus");
						//Check if datasets are existing for the queries -reqd for showing 'show dataset' link
						String datasets="Select datasetid from xdata_datasetvalue where assignment_id=? and question_id=? and query_id=? and course_id = ?";
						PreparedStatement pstmt1=dbcon.prepareStatement(datasets);
						
						pstmt1.setInt(1, assignID);
						pstmt1.setInt(2,qID);
						pstmt1.setInt(3,query_id);
						pstmt1.setString(4,courseID);
						ResultSet rset=pstmt1.executeQuery();
						
						if(rset.next()){
							String questId = loginUsr+"&"+assignID+"&"+qID+"&"+query_id;
							List <String> dataGenCompleted = (ArrayList)  session.getAttribute("DataGenerationCompleted");
							if(dataGenCompleted != null && !dataGenCompleted.contains(questId)){ 
								dataGenCompleted.add(questId); 	
							}
							else if(dataGenCompleted == null || (dataGenCompleted != null && dataGenCompleted.size()== 0)){
								dataGenCompleted = new ArrayList();
								dataGenCompleted.add(questId);  

							}
							session.setAttribute("DataGenerationCompleted",dataGenCompleted); 	  
						}
						
						String generate = "AssignmentChecker?assignment_id=" 
								+ assignID + "&&question_id=" + qID +"&&query_id="+query_id
								+ "&&query="
								+ CommonFunctions.encodeURIComponent(queries)
								+"&&stop=false"
								+ "'\"target = \"rightPage\"";
				 
						String showDataSet= "showGeneratedData.jsp?AssignmentID="
								+ assignID + "&&question_id=" + qID +"&&query_id="+query_id + "&&query=" 
								+ CommonFunctions.encodeURIComponent(queries) +"&&question_text="
								+ CommonFunctions.encodeURIComponent(desc)
								+"'\"target = \"rightPage\"";
						
						String parameters = "assignment_id="
								+ assignID + "&&question_id=" + qID +"&&query_id="+query_id + "&&query="
								+ CommonFunctions.encodeURIComponent(queries);
						  
						String stopProcess = "AssignmentChecker?assignment_id=" 
								+ assignID + "&&question_id=" + qID + "&&query_id="+query_id+ "&&query=" 
								+ CommonFunctions.encodeURIComponent(queries) 
								+  "&&stop=true"
										+"'\"target = \"rightPage\"";
						
						String stopParams=  "assignment_id="
								+ assignID + "&&question_id=" + qID +"&&query_id="+query_id+ "&&query="
								+ CommonFunctions.encodeURIComponent(queries) +  "&&stop=true";
						%>
						  <%if(queryIndex > 1) {%>  
						<script>
						if(document.getElementById('matchOption'+<%=qID%>).style.display == "none"){
							document.getElementById('matchOption'+<%=qID%>).style.display='block';
						}
						</script>  
						<%} %>	
						
						<!-- <span>Ans <% //= queryIndex%>.</span> -->	
						 <pre><code class="sql"><label style="font-style:bold;font-weight: bold">Ans <%=queryIndex%>. </label><%=savedQuery%></code></pre>
							
			   				<div><label>Marks for this answer:</label>
							<%= marksPerQuery%> 
							</div>
							
							<div class="editbutton">
							
							 <div id ='generateData<%=index %>' class = 'generateData' style='display:none;text-decoration:none;font-family:Helvetica;color:#353275;float:right;width:12%;height:25px;padding-left:1px;'>   
									 <a class='generate' id='<%=parameters%>' name="<%=index %>" href= '<%=generate %>'>Generate Dataset</a>
									 
							</div>  
								   
							<div id='progress<%=index %>' align='right' class='progress' style='display:none;;font-family:Courier;color:#353275;float:right;width=60%;'>Data Generation in progress. Please wait
 								<img src="images/bluebar_dots_ani.gif"  border="0" height ="5%" width ="10%"/> 
							 	<form name="stopForm" id="stopForm" method="post" action="<%=stopProcess%>">   
        			  			 <a href='#' onclick="$(this).closest('form').submit();" >Stop</a> 
        			  		</form>      
       			  			</div>
     			  		
       			  			<div id = 'showData<%=index %>' class='showData' style='font-family:Helvetica;color:#353275;display:none;' >
       			  				<span class = "separator">&nbsp;</span> 	
       			  				<a href='<%=showDataSet%>'>Show Result On Dataset&nbsp;&nbsp;</a>
       			  			</div>  
       			  						  			
       			  			<div  id="dataGenError<%=index%>" class="dataGenError" style='display:none;float:right;color:red;font-family: Helvetica;font-size:13px;'>Error in Data Generation.Please Check the log file for details.
       			  			<br> System will use default sample dataset provided.
       			  			</div>	
       			  			  
	    
       			  		 </div>
  		 					<br/>
        			  		<%
        			  		/************CHANGED FOR MULTIPLE QUERIES -TO BE REVISITED ************/
        			  		//Display the data genration progress image and show dataset buttons
        			  		// based on session information
        					queryIndex++;
        					String generatingStatus = loginUsr+"&"+assignID+"&"+qID+"&"+query_id;
						 	Hashtable <String,Long> dataGenStarted = (Hashtable)session.getAttribute("DataGeneratingInProgressMap");
						 	List <String> dataGenCompletedList = (ArrayList) session.getAttribute("DataGenerationCompleted");
						  
					      	if(dataGenStarted != null && dataGenStarted.containsKey(generatingStatus)){ 
					      		
       			  			 %>  
       			  			  <script>
       			  				 var i= <%=index%> ;
							   	 document.getElementById('progress'+i).style.display='block';      
							   </script> 		
					 	  <% 
							} 
							 else if(dataGenCompletedList!= null && 
								 dataGenCompletedList.contains(generatingStatus)){
								
							%>  
				 			 <script>		
							   var i= <%=index%> ;
							  document.getElementById('showData'+i).style.display='block';
							  
							   document.getElementById('generateData'+i).style.display='block';
						      //  document.getElementById('edit'+i).style.display='block';  
							 </script>  
							 	<%
							 }  
							 else if((dataGenStarted == null && dataGenCompletedList == null)){
								
							 %> 						 		 	  
						 	<script>      
								var i= <%=index%>;
							   document.getElementById('generateData'+i).style.display='block';
						       // document.getElementById('edit'+i).style.display='block'; 
							</script>
							<%	  
						 } 
						 else if((dataGenStarted != null && ! dataGenStarted.containsKey(generatingStatus))
								 && dataGenCompletedList == null){	  		
							
							  %> 
							   <script>	  
							   var i= <%=index%> ;
							   document.getElementById('generateData'+i).style.display='block';
						       // document.getElementById('edit'+i).style.display='block'; 
						        </script>	 							        
					      	<%
						 }
						 else if(dataGenStarted==null && dataGenCompletedList !=null && ! dataGenCompletedList.contains(generatingStatus)){
							 
						 %>  
						   <script>	  
						   var i= <%=index%> ;     		  
						   document.getElementById('generateData'+i).style.display='block';
					      //  document.getElementById('edit'+i).style.display='block'; 
					        </script>
						 <%
						 } 
						 else{
						 %>
							 <script>	  
							   var i= <%=index%> ;
							   document.getElementById('generateData'+i).style.display='block';
						       // document.getElementById('edit'+i).style.display='block'; 
						        </script>
						<% } 
					       index++;
        			  		/*************CHANGED FOR MULTIPLE QUERIES ENDS *********/
        			  	}
							rs1.close(); 
						/**Added for multiple Queries Ends**/
						%>	 	
						</div>
       			  		<div id = 'editDelete' class='editbutton'>
       			  		<form name="deleteForm" id="deleteForm" method="post" action="DeleteQuestionWithQueries?assignment_id=<%=assignID%>&question_id=<%=question_id%>">
        			  	 <a href='#' onclick="var a = confirm('Are you sure you want to delete the question with answers?'); if(a==1) $(this).closest('form').submit();">Delete</a> 
       			  		<span class = "separator">&nbsp;</span> 
       			  		<a href='<%=remote %>'><%="Edit" %></a>
       			  		 </form> 
       			  		 
       			  		  <!-- Changes for adding Grade questions and Result start -->
     			<div id="evaluate<%=qIndexDisplay %>" class="evaluate" 
     	     		style='display:block;font-family:Helvetica;color:#353275;width:92%;height:25px;'>
	  			  <span class = "separator">&nbsp;</span> 	
 			  			<a class="evaluateQ" name='<%=qIndexDisplay %>' href='<%=evaluate %>' id='<%=params%>'>
 			  			<%="Evaluate" %></a>
				 
				 
				 
       			  		 <%if(isQuestionEvaluated){ %>
					<div id="status<%=qIndexDisplay %>" class="statusr" 
						style='display:block;font-family:Helvetica;color:#353275;width:92%;height:25px;'>
    			  			<!-- style='font-family:Helvetica;color:#353275;display:none;' -->
						
							<span class = "separator">&nbsp;</span>
								<a href='<%=status %>' ><%="Evaluation Result"%> </a>
								
					</div> 		
				<%}else{ %>
						<div id="status<%=qIndexDisplay%>" class="statusr"  
						style='display:none;font-family:Helvetica;color:#353275;width:92%;height:25px;'>
    			  			<!-- style='font-family:Helvetica;color:#353275;display:none;' -->
						
							<span class = "separator">&nbsp;</span>
								<a href='<%=status %>'><%="Evaluation Result"%> </a>
								 </div> 
							     
							<%} %>
							
       			</div> 
									<div id='eprogress<%=qIndexDisplay %>' align="right" class='eprogress' 
									style='display:none;font-family:Helvetica;color:#353275;width:90%;'>
										 
									Evaluating student answers 
		 							<img src="images/bluebar_dots_ani.gif"  border="0" height ="4%" width="8%"/>
		 							<span class = "separator">&nbsp;</span> 	
						  			</div>
       			  				<label class="evError" id="evError<%=qIndexDisplay%>" 
		        			  		style='display:none;float:right;color:red;font-family: Helvetica;font-size:13px;'>Error during evaluation. Please Check the server log for details.
		        			  		</label>	  		    
       			  			  <!-- Changes for adding Grade questions and Result ends -->	
       			  		</div>	
					  </div>								  
				<%  }	
						if(qIndexDisplay == 0){%>
						 <script>document.getElementById('evaluateAssignment').style.display='none';
						        </script>
						<%} %>
						</div> 
						<%						
						rs.close();
						output = "";
					}  catch (Exception err) {
						err.printStackTrace();
						throw new ServletException(err);
					}
					finally{
						dbcon.close();
					}  
					 
					String add = "QuestionDetails.jsp?AssignmentID=" + assignID +"&&questionId=" + newQId + "&&courseId=" + courseID + "&&new=true&&assignmentName="+asgnName+ "'\"target = \"rightPage\"";
					//output += "<input  type=\"button\" id=\"quer\" onClick=\"addRow(" + assignID + ",'queryTable')\" value=\"Add Question\" align=\"right\">";
					output += "<input  type=\"button\" id=\"quer\" onClick=\"window.location.href='" + 
						add + "value=\"Add Question\" align=\"right\">";
					out.println(output); 
				%>
			</fieldset>
		</div>
	</div>
</body>
</html>
