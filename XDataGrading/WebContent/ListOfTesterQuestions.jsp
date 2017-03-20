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

<link rel="stylesheet" href="css/structure.css" type="text/css"/>
<link rel="stylesheet" href="highlight/styles/xcode.css"/>  
<link rel="stylesheet" href="highlight/styles/default.css"/>
<script src="highlight/highlight.pack.js"></script> 
<script type="text/javascript" src = "scripts/jquery.js"></script>


<script type="text/javascript">   
hljs.initHighlightingOnLoad();

//hljs.initHighlighting();

function getParameterByName(name) { 		
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
} 
var asId = getParameterByName("AssignmentID");
$( document ).ready(function() {	

	
	$('.generate').click(function(e){
		e.preventDefault(); 
		var destination =this.href;
		var dataString=this.id;
		var self = this;
		var qid= this.name;
			//alert($(self).parent().get( 0 ).tagName);  
		$.ajax({ 
	        type: "GET", 
	        url: "AssignmentChecker",
	        data: dataString,
	        context:this,  
	        beforeSend : function() {
	        	$(self).parent().parent().find($('.generateData')).hide(); 
	        	$(self).parent().parent().find($('.showData')).hide();
	        	$(self).parent().parent().find($('.progress')).show();
	        	($('#equivalenceGenDataMessage'+qid)).hide();
	       }, 
	        success: function(data) { 
	        	try{
	        		//if(this.success){
				    	//alert($(self).parent().get( 0 ).tagName);  
			        	$(self).parent().parent().find($('.generateData')).show(); 
			        	$(self).parent().parent().find($('.showData')).show();
			        	$(self).parent().parent().find($('.progress')).hide();
			        	($('#equivalenceGenDataMessage'+qid)).hide();
			        	
	        		//}
	        	}
	        		catch(err)
	        		{
	        			//alert("Error occurred during data generation success fnunc." + $(self).parent().parent().find($('.showData')));
	        			$(self).parent().parent().find($('.generateData')).show(); 
	        			$(self).parent().parent().find($('.showData')).hide(); 
	    	        	$(self).parent().parent().find($('.progress')).hide();
	        			$(self).parent().parent().find($('.dataGenError')).show(); 
	   	        	 setTimeout( function(){
	   	        		$(self).parent().parent().find($('.dataGenError')).hide();
	   	    			  }, 60000*10); 
	    	        	
	    	        	//$(self).parent().parent().parent().find($('.edit')).show(); 
	    	        
	    	        	//alert("Internal Error occurred during Data Generation." + err); 
	    	        	
	        		} 
	        	
	        }, 
	        error : function(xhr, ajaxOptions, thrownError){
	        	//alert("plain error" + $(self).parent().parent().get( 0 ).tagName); 
	        	$(self).parent().parent().find($('.generateData')).show(); 
	        	$(self).parent().parent().find($('.showData')).hide(); 
	        	$(self).parent().parent().find($('.progress')).hide();
	        	$(self).parent().parent().find($('.dataGenError')).show(); 
	        	 setTimeout( function(){ 
	        		 $(self).parent().parent().find($('.dataGenError')).hide();
	    			  }, 60000*10); 	        		        	  
            }
	      }); 
	      return false; 
	});
	//Method to evaluate all questions in Assignment 
	$('.chkEquivalence').click(function(e){
		e.preventDefault();
		var dataString= this.id;
		var qid = this.name;
		var self = this;

		$.ajax({ 
	        type: "POST", 
	        url: "CheckQueryEquivalence",
	        data: dataString,
	        context:this,  
	        success:function(data){
	        	//alert("Come to success");
	        	try{
	        		$.ajax({ 
				        type: "GET",
				        url: "CheckQueryEquivalence",
				        data: dataString,
				        context:this,  
				        beforeSend : function() {
				        	//alert("Comes to B4 send");
				        	 
				        	 ($('#equivalenceFailMessage'+qid)).hide();
				        	 ($('#equivalenceSuccessMessage'+qid)).hide();
			    			 ($('#matchOption'+qid)).hide();
			    			 ($('#eqvProgress'+qid)).show();
				
				       },success: function(data) { 
				        	try{
				        		// alert("Success : ");
				        		 ($('#equivalenceFailMessage'+qid)).hide();
				   	        	 ($('#equivalenceSuccessMessage'+qid)).show();
				       			 ($('#matchOption'+qid)).show();
				       			 ($('#eqvProgress'+qid)).hide();
				        	}
				        		catch(err)
				        		{
				        			//alert("Catch in Success : ");
				        			($('#equivalenceGenDataMessage'+qid)).hide();
				        			 ($('#equivalenceSuccessMessage'+qid)).hide();
				        			 ($('#matchOption'+qid)).show();
				        			 ($('#eqvProgress'+qid)).hide();
				        			 ($('#equivalenceFailMessage'+qid)).show();
				    	        	 setTimeout( function(){
				    	        		 ($('#equivalenceFailMessage'+qid)).hide();
				    	    				  }, 60000*10); 
				    	       		 }	    	        
				        	
				        },error : function(xhr, ajaxOptions, thrownError){
				        //	alert("Error func : ");
				        ($('#equivalenceGenDataMessage'+qid)).hide();
				          	 ($('#equivalenceSuccessMessage'+qid)).hide();
			    			 ($('#matchOption'+qid)).show();
			    			 ($('#eqvProgress'+qid)).hide();
			    			 ($('#equivalenceFailMessage'+qid)).show();
				        	 setTimeout( function(){
				        		 ($('#equivalenceFailMessage'+qid)).hide();
				    				  }, 60000*10); 
				       		 }	        
				      });
	        	}
	        		catch(err)
	        		{
	        			($('#equivalenceGenDataMessage'+qid)).show();
	        			 setTimeout( function(){ 
	    	        		$('#equivalenceGenDataMessage').hide();
	    	    			  }, 60000*10); 
	    	       		 }	   
	        }, error: function(xhr, ajaxOptions, thrownError){
	        		//alert("Comes to error...");
	        	 	($('#equivalenceGenDataMessage'+qid)).show();
	        	 	setTimeout( function(){ 
    	        		$('#equivalenceGenDataMessage').hide();
    	    			  }, 60000*10); 
    	       		 	
	        	
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
%>
<br/>
	<div>
		<div class="fieldset">

			<fieldset>
				<legend> Application Details</legend>
				<%			//Get header part 
							String loginUsr= (String)session.getAttribute("LOGIN_USER");
							int assignID = Integer.parseInt(request.getParameter("AssignmentID"));
							String courseID = (String) request.getSession().getAttribute(
									"context_label");
							String instructions = (new CommonFunctions())
									.getTesterAssignmentInstructions(courseID, assignID);
							instructions += "<br/><br/><input name=\"Edit\" type=\"button\" value=\"Edit Details\" onclick=\"window.location.href='EditApplication.jsp?AssignmentID="+assignID+"'\">&nbsp;&nbsp;";
							out.println(instructions);
							//String asgnEvalParams = (Integer.toString(assignID)) ;
							//String evaluateAsgn = "EvaluateAssignment?assignment_id="+ assignID;
								
				%>
			</fieldset>
			<br/>
		<th style='font-style:italic;font-weight: bold;font-size: 15;'>
			<fieldset>
				<legend> List of Queries</legend>

			
				<%
							//get connection
							Connection dbcon = (new DatabaseConnection()).dbConnection();
							String asgnName = "";
							boolean asgnEvaluated = false;
						
							try{
								PreparedStatement stmt1;
								ResultSet rs1 = null;
								stmt1 = dbcon
										.prepareStatement("SELECT * FROM xdata_assignment where assignment_id=? and course_id=?");
								stmt1.setInt(1, assignID);
								stmt1.setString(2, courseID);
								rs1 = stmt1.executeQuery();
								if (rs1.next()) {
									asgnName = rs1.getString("assignmentname");
									//asgnEvaluated = rs1.getBoolean("evaluationstatus");
									//start=rs.getString("end_date");
								}						
								rs1.close(); 
							} catch (Exception err) {
								err.printStackTrace();
								throw new ServletException(err);

							}
							//now check whether current time is less than start time.Then only assignment can be edited
							boolean yes = false;
							String matchOption = "";
							boolean matchAll;
							boolean equvStatus = false;
							String equivalence_failed_datasets = null;
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
									equvStatus = rs.getBoolean("equivalenceStatus");
									String correctQuery = rs.getString("correctquery");
									equivalence_failed_datasets = rs.getString("equivalence_failed_datasets");
									//int marksAllotted = rs.getInt("totalmarks");
									matchAll = rs.getBoolean("matchallqueries");
									if(matchAll){
										matchOption = "Match all results";
									}
									else{
										matchOption = "Match any one result";
									}
									System.out.println(qID + ": " + correctQuery);

									String remote = "TesterQuestionDetails.jsp?AssignmentID="
											+ assignID +"&&assignmentName="+asgnName+ "&&courseId=" + courseID
											+ "&&questionId=" + qID;
									
									//String evaluate = "EvaluateQuestion?assignment_id="
										//	+ assignID + "&&question_id=" + qID;
											//+ "\" target = \"rightPageBottom\"";
									String params = "assignment_id="
											+ assignID + "&&question_id=" + qID ;
									 
									//String status = "QueryStatus?assignment_id=" + assignID
											//+ "&&question_id=" + qID;
											 
				%>   <!-- RENDER THE PAGE FOR ALL EXISTING QUESTIONS -->
					<div class="questionelement"> 						
					<div class="question" id="Question"><span>Q<%= qIndexDisplay %>. </span><%= desc %><br/>
					
					</div>
						 			 
					<div class="answer" >
						
						
					<div class="matchOption" id="matchOption<%=qID%>" style='display:none;font-family: helvetica'>
					<!-- <b>Multiple SQL Option: <%//=matchOption %></b> -->
					<a class= "chkEquivalence" name="<%=qID %>" id="<%=params %>" href="CheckQueryEquivalence?assignment_id=<%=assignID %>&&question_id=<%=qID %>" style="color:#353275;float:right;font-weight:bold;text-decoration:none;">Check Query Match</a>
					<!-- #353275 --><br/>
					</div>
					<div id='eqvProgress<%=qID %>' align='right' class='eqvProgress' style='display:none;font-family:Courier;color:#353275;float:right;width=60%;'>Checking equivalence
 						<img src="images/bluebar_dots_ani.gif"  border="0" height ="5%" width ="10%"/> 
					</div>
       			  			
					<div id="equivalenceFailMessage<%=qID %>" style='display:none;font-family: helvetica;color:red;'>
						The queries are not equivalent.
						<a href="failedEquivalenceData.jsp?assignment_id=<%=assignID %>&&question_id=<%=qID %>" style='font-family: helvetica;color:red;'>View Failed DataSets</a>
					</div>
					<div id="equivalenceSuccessMessage<%=qID %>" style='display:none;font-family: helvetica;color:green;'>The queries are equivalent
					</div>
					<div id="equivalenceGenDataMessage<%=qID %>" style='display:none;font-family: helvetica;color:red;'>Please generate data for the queries before checking for equivalence
					</div>
						<br/>
						<%if(equivalence_failed_datasets != null && !equvStatus){ %>
						 		<script>
       			  				 var i= <%=qID%> ;
							   	 document.getElementById('equivalenceFailMessage'+i).style.display='block';      
							   </script> 	
							   
						<%}else if(equvStatus){ %>
						
								<script>
       			  				 var i= <%=qID%> ;
							   	 document.getElementById('equivalenceSuccessMessage'+i).style.display='block';      
							   </script> 
							   
						<%}//else{ %>
						     <script>
       			  				 // var i= <%//=qID%> ;
							   	 //document.getElementById('matchOption'+i).style.display='block';      
							   </script> 
						<%//} %>
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
						int query_id = rs1.getInt("query_id");
						//int marksPerQuery = rs1.getInt("marks");
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
						
						String expectedResult = "TestSqlOnDataset.jsp?assignment_id="
								+ assignID + "&&question_id=" + qID +"&&query_id="+query_id+ "&&query="
								+ CommonFunctions.encodeURIComponent(queries)+"&&question_text="+CommonFunctions.encodeURIComponent(desc)
								+" '\"target = \"rightPage\"";
						%>
						  <%if(queryIndex > 1) {%>  
						<script>
						if(document.getElementById('matchOption'+<%=qID%>).style.display == "none"){
							document.getElementById('matchOption'+<%=qID%>).style.display='block';
						}
						</script> 
						<%} %>			
							<pre><code class="sql"><span>Ans <%=queryIndex%>. </span>
							<%=CommonFunctions.encodeHTML(queries)%>
							</code></pre> <p></p>
			   				
							<p></p>
							<div class="editbutton">
							
							 <div id ='generateData<%=index %>' class = 'generateData' style='display:none;text-decoration:none;font-family:Helvetica;color:#353275;float:right;width:12%;height:25px;padding-left:1px;'>  
	 <a class='generate' name='<%=qID %>' id='<%=parameters%>' href= '<%=generate %>'>Generate Dataset</a> 
									 
									 
							</div>  
								   
							<div id='progress<%=index %>' align='right' class='progress' style='display:none;;font-family:Courier;color:#353275;float:right;width=60%;'>Data Generation in progress. Please wait
 								<img src="images/bluebar_dots_ani.gif"  border="0" height ="5%" width ="10%"/> 
							 	<form name="stopForm" id="stopForm" method="post" action="<%=stopProcess%>">   
        			  			 <a href='#' onclick="$(this).closest('form').submit();" >Stop</a> 
        			  		</form>      
       			  			</div>
     			  		
     			  			<!-- <div id = 'expectedResult<%//=index %>' class='expectedResult' style='font-family:Helvetica;color:#353275;display:block;' >
       			  				<span class = "separator">&nbsp;</span> 	
       			  				<a href='<%//=expectedResult%>'>Expected Result on Dataset&nbsp;&nbsp;</a>
       			  			</div> -->
       			  			
       			  			<div id = 'showData<%=index %>' class='showData' style='font-family:Helvetica;color:#353275;display:none;' >
       			  				<span class = "separator">&nbsp;</span> 	
       			  				<a href='<%=showDataSet%>'>Show Result On Dataset&nbsp;&nbsp;</a>
       			  				<!-- <span class = "separator">&nbsp;</span> 	
       			  					 <a href='<%//=expectedResult%>'>Test Output&nbsp;&nbsp;</a> -->  
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
								var i= <%=index%> ;
							   document.getElementById('generateData'+i).style.display='block';
						        document.getElementById('edit'+i).style.display='block'; 
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
						 else{%>
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
       			  		 
       			  		  </div>	
					  </div>								  
				<%  }	%>
						
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
					 
					String add = "TesterQuestionDetails.jsp?AssignmentID=" + assignID +"&&questionId=" + newQId + "&&courseId=" + courseID + "&&new=true&&assignmentName="+asgnName+ "'\"target = \"rightPage\"";
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
