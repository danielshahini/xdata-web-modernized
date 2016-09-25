<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@ page import="java.io.*"%> 
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@ page import="java.text.*"%>
<%@page import="database.*"%>
<%@page import = "com.google.gson.Gson" %>
<%@page import = "com.google.gson.internal.*" %>
<%@page import="javax.xml.bind.JAXBException"%>
<%@page import="javax.xml.bind.PropertyException"%>
<%@page import="javax.xml.bind.Marshaller"%>
<%@page import="javax.xml.bind.JAXBContext"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
	
<script type="text/javascript" src = "scripts/jquery.js"></script>
<script type="text/javascript" src = "scripts/jquery-ui.min.js"></script>
<script src="scripts/bootstrap/dist/js/bootstrap.js"></script>
<link href="scripts/bootstrap/dist/css/bootstrap.css" rel="stylesheet"/>
<link rel="stylesheet" href="css/structure.css" type="text/css"/> 
<link rel="stylesheet" href="highlight/styles/xcode.css"/>  
<link rel="stylesheet" href="highlight/styles/default.css"/>
<script src="highlight/highlight.pack.js"></script> 

<script src="dhtmlxTree_v46_std/codebase/dhtmlxtree.js" type="text/javascript"></script>
 <script src="dhtmlxTree_v46_std/codebase/dhtmlxtree.js"></script>
 
<script type="text/javascript">   
hljs.initHighlightingOnLoad(); 
 $(document).ready(function () {
	 var index = '';
        //on click show the hide div and the message
        $(".showhidelink").click(function () {
        	 $('[id^="detailsDiv"]').fadeOut("slow");
        	 $('[id^="tableData"]').fadeOut("slow");
        	index = this.id;
        
        	
            $('#detailsDiv'+index).fadeIn("slow");
            $('#tableData'+index).fadeIn("slow");
        	//$('#detailsDiv').draggable();
        	//$('#tableData').draggable();
        });
  
        //on click hide the message and the
        $(".buttonClose").click(function () {
			index=this.name;
			 $('#detailsDiv'+index).fadeOut("slow");
            $('#tableData'+index).fadeOut("slow");

        });  
         
        $('#marksModal').on('hide.bs.modal', function (e){
        	window.location.reload(true);	   
     	});
     
          $("#errorModal").on("show.bs.modal", function(e) {
        	    var link = $(e.relatedTarget);
        	    $(this).find(".modal-body").load(link.attr("href"));
        });
          $("#marksModal").on("show.bs.modal", function(e) {
      	    var link = $(e.relatedTarget);
      	  
      	    $(this).find(".modal-body").load(link.attr("href"));
      	    
      });

        	 $("#marksModal").draggable({
        	      handle: ".modal-header"
        	  });
        	 $('.modal-dialog').resizable();
}); 

 
</script>
<style>


.modal-dialog{
    margin-right: 0;
    margin-left: 0;
     position: relative;
     overflow-y: auto;  
      overflow-x: auto;
    display: table; /important ;

    width: auto;
    min-width: 300px; 
}
.modal-content{
overflow-y: auto;  
overflow-x: auto;
}
#breadcrumbs
{
  position: relative;
  padding-left:10px;
  padding-right:10px;
  left: 5px;
  top: 10px;
  font: 13px/13px Arial, Helvetica, sans-serif;
  background-color: #f0f0f0;
  font-weight: bold;
}
 .detailsModal
    {   position:relative;
       	top: 30%;
        left: 52%;
        width:55%;
        height:100%;
        /*margin-top: -800px; /*set to a negative number 1/2 of your height*/
       /* margin-left: -500px; /*set to a negative number 1/2 of your width*/
         margin-top: -5em; /*set to a negative number 1/2 of your height*/
        margin-left: -5em; /*set to a negative number 1/2 of your width*/
        
        z-index: 99;
      background-color:white;
      overflow: auto;
       /*for transparency*/
       opacity:1; 
    }

    .popupModal
    {
    	position:relative;
        top: 30%;
        left: 52%;
        width:65%;
        height:auto;
        margin-top: -5em; /*set to a negative number 1/2 of your height*/
        margin-left: -5em; /*set to a negative number 1/2 of your width*/
        border: 1px solid #ccc;
        border:  2px solid black;
        z-index:100; 

    }


</style>

</head>

<body id="public">
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}

if(! Boolean.parseBoolean(session.getAttribute("ltiIntegration").toString())){%> 

<div id="breadcrumbs">
  <a style='color:#353275;text-decoration: none;' href="CourseHome.jsp" target="_top">Home</a> &nbsp; >> &nbsp;
  <a href="InstructorHome.jsp?contextLabel=<%=(String) request.getSession().getAttribute("context_label")%>" style='color:#353275;text-decoration: none;' target="_top"><%=(String) request.getSession().getAttribute("context_label")%></a>&nbsp; >> &nbsp;
   <a href="ListAllAssignments.jsp" style='color:#353275;text-decoration: none;' target="_self">Assignment List</a>&nbsp; >> &nbsp;
   <a href="asgnmentList.jsp?assignmentId=<%=request.getParameter("AssignmentID").trim()%>" target="_self" style='color:#353275;text-decoration: none;'>Assignment Details</a>&nbsp; >> &nbsp;    
	<a href="#" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>View Scores</a></div>
	 </div>							
 
<%}else{%>
<div id="breadcrumbs">
  <a href="asgnmentList.jsp?assignmentId="<%=request.getParameter("AssignmentID").trim()%>" target="_self" style='color:#353275;text-decoration: none;'>Assignment Details</a>&nbsp; >> &nbsp;
  <!-- <a href="ListOfQuestions.jsp?AssignmentID="<%=request.getParameter("AssignmentID").trim()%>" target="_self" style='color:#353275;text-decoration: none;'>Question List</a>&nbsp; >> &nbsp; -->
  <a href="#" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>View Scores</a>
  </div> 
<%}%> 
					
<br/>
	<div>
		<div class="fieldset">
		<fieldset><legend>Result</legend>
		<%
		Boolean ltiIntegration = Boolean.parseBoolean(session.getAttribute(
				"ltiIntegration").toString());
		String assignment_id = request.getParameter("AssignmentID");
		
		String total = "select sum(totalmarks) total, count(*) as numberOfQuestions from xdata_qinfo where assignment_id = ? and course_id=?";
		String result = "select sum(score) score, user_name, email, rollnum from xdata_users u left join xdata_student_queries s on u.internal_user_id = s.rollnum "
				+"where assignment_id = ? group by user_name, email, rollnum order by rollnum";
		String course_id = (String) request.getSession().getAttribute("context_label");
		Connection dbcon = null;
		try{
			dbcon = (new DatabaseConnection()).dbConnection();
			int noOfQuestions = 0;
		
			Float totalMarks = 100F;
			double totRoundedMarks = 0F;
			PreparedStatement pstmt = dbcon.prepareStatement(total);
			pstmt.setInt(1, Integer.parseInt(assignment_id));
			pstmt.setString(2, course_id);
			ResultSet rs1 = pstmt.executeQuery();
			if (rs1.next()) {
				totalMarks = rs1.getFloat("total");
				noOfQuestions = rs1.getInt("numberOfQuestions");
			}
			totRoundedMarks = Math.round(totalMarks*100.0)/100.0;
				//If Req is from moodle or any other external learning tool
				if (ltiIntegration) {
				%> 
				<div>Upload assignment marks for all students to moodle:
				<!-- Link to show upload assigment marks for all students -->
				<a href='UploadScore?singleUpload=false&AssignmentID=<%=request.getParameter("AssignmentID")%>'
									  style='text-decoration:none'><button type='button'>Upload</button></a>
				</div><br>
				<%}
					boolean present = false;
					PreparedStatement pstmt1 = dbcon.prepareStatement(result);
					pstmt1.setInt(1, Integer.parseInt(assignment_id));
					ResultSet rs = pstmt1.executeQuery();
					System.out.println("pstmt1 = "+pstmt1.toString());
						%>
					<table>
					<%String uploadHeader = "";
					String rollNumIndex  = "";
					if (ltiIntegration) {
						uploadHeader = "<td>Upload</td>";
					}%>
					<tr style='background: #E4E4E4; text-align: center;font-weight: bold;'>
							<td >Name</td>
							<td>Email</td>
							<td>Score</td> 
							<td>Mark and Error Details</td><%= uploadHeader%></tr>
							
		
						<%if(request.getParameter("ResultUploadStatus") != null 
						&& request.getParameter("ResultUploadStatus").equalsIgnoreCase("success")){%>
						
					<label style='display:block;color:green;'>Scores uploaded to Learning tool.</label><br>
						<%}
							else if(request.getParameter("ResultUploadStatus") != null 
									&& request.getParameter("ResultUploadStatus").equalsIgnoreCase("fail")){%>
						<label style='display:block;color:red;'>
										Uploading marks for the assignment failed.</label><br>
							<%}
				
						while (rs.next()) {
							present = true;
							double totRoundedScores = 0F;
							if(rs.getString("rollnum").contains(".")){
								rollNumIndex = (rs.getString("rollnum")).replaceAll(".", "");
							}else{
								rollNumIndex = rs.getString("rollnum");
							}
								//System.out.println("rollNumIndex ="+rollNumIndex);
							totRoundedScores = Math.round(rs.getFloat("score")*100.0)/100.0;
							String uploadLink = "";
							if (ltiIntegration) {
								uploadLink = "<td class=\"wrapword\"><a href='UploadScore?userId="
										+ rs.getString("rollnum") + "&score="
										+ totRoundedScores + "&max=" + totRoundedMarks+ "&singleUpload=true"
										+ "&AssignmentID="+request.getParameter("AssignmentID")+"'>Upload</a></td>";
							}
							String detailsLink= "<td class=\"wrapword\"> " +
							"<a class='detailsLink' style='color: #00f;' href='viewAssignmentScoreDetails.jsp?assignment_id="+assignment_id+"&&user_name="+rs.getString("user_name")+"&&email="+rs.getString("email")+"&&rollnum="
							+rs.getString("rollnum")+"&&rollnumindex ="+rollNumIndex+"&&score="+totRoundedScores+"&&totalScore="+totRoundedMarks+"&&noOfQuestions="+noOfQuestions+"' target='_blank' type='new_tab''> Details</a>"
							//"<a class='showhidelink' id='"+rollNumIndex+"'>Details</a>"
							+"</td>";
							%>
							<tr><td class="wrapword"><%=rs.getString("user_name")%>
									</td><td class="wrapword"><%= rs.getString("email")%></td> 
									<td class="wrapword"><%=totRoundedScores%> / <%= totRoundedMarks %></td>
									<%= detailsLink%><%=uploadLink%>
									</tr>
							
							<%
						}rs.close();//for each student
						if (present)%></table><%
						else%>Not solved
						
						<%
						/**For showing details link start**/
						
						PreparedStatement pstmt2 = dbcon.prepareStatement(result);
						pstmt2.setInt(1, Integer.parseInt(assignment_id));
						rs = pstmt2.executeQuery();
						String output= "";
						while(rs.next()){
							
							if(rs.getString("rollnum").contains(".")){
								rollNumIndex = (rs.getString("rollnum")).replaceAll(".", "");
							}else{
								rollNumIndex = rs.getString("rollnum");
							}//System.out.println("rollNumIndex on second loop="+rollNumIndex);
						%>
						
						<!-- Outer dialog window for transparency -->
						<div class='detailsModal' id='detailsDiv<%=rollNumIndex%>' style="display:none">
						</div>
						<!-- Inner dialog to load contents -->
						<div class='popupModal' id='tableData<%=rollNumIndex%>' style='display:none'>
								<a class='buttonClose'  id='buttonClose<%=rollNumIndex%>' 
								style='float:right;padding-right: 20px;' name=<%=rollNumIndex%>><img src="images/close_teal.jpeg" style="width:20px;height:20px;"/></a>
								<br><label style="background: #FFF;"><b>Details of user : <%=rs.getString("user_name")%> &nbsp; &nbsp;&nbsp;&nbsp;Roll no:<%=rs.getString("rollnum") %></b></label>
								<table border="1" style="background-color: #FFF;" width="auto">
									<tr><th>Question Number</th>
										<th> Student response</th>
												<th> Status</th>
												<th> Marks Obtained </th>
												<th> Error Details</th>
												<th> Mark Details</th>
												</tr> 
												 
								<%
								try { 
			 						String assignment="select * from xdata_student_queries queries natural join xdata_qinfo qinfo" +
											" where queries.assignment_id =? and " +
											"qinfo.assignment_id=? and queries.assignment_id=qinfo.assignment_id " +
											"and rollnum=? order by queries.question_id";
									
									//String marks = "Select result,max_marks from score where assignment_id=? and rollnum= ? " +
											//"and question_id= ?";
								String marks = "Select score,max_marks from xdata_student_queries where assignment_id=? and rollnum= ? " +
										"and question_id= ?";
									PreparedStatement stmt=dbcon.prepareStatement(assignment);
									stmt.setInt(1,Integer.parseInt(assignment_id));
									stmt.setInt(2,Integer.parseInt(assignment_id));
									stmt.setString(3,rs.getString("rollnum"));
									//System.out.println(stmt.toString());
									ResultSet rset=stmt.executeQuery();
									while(rset.next()){
										float marksAwarded = 0.0f;
										int maxMarks = 0;
										PreparedStatement stmt1=dbcon.prepareStatement(marks);
										stmt1.setInt(1,Integer.parseInt(assignment_id));
										stmt1.setString(2, rs.getString("rollnum"));
										stmt1.setInt(3,rset.getInt("question_id"));
										maxMarks = rset.getInt("totalmarks");
										rs1=stmt1.executeQuery();
										if(rs1.next()){
											marksAwarded = Math.round(rs1.getFloat("score"));
											
										}
										if(rset.getString("verifiedcorrect") != null){
											
											String status="";
										
											if(rset.getString("verifiedcorrect") != null && rset.getBoolean("verifiedcorrect") == false)
											{
												status="Wrong";
											}
											else if(rset.getString("verifiedcorrect") != null && rset.getBoolean("verifiedcorrect") == true){
												status="Correct";
											}
											if(status.equalsIgnoreCase("wrong"))
											{
											%>
												<tr><td class="wrapword">Question<%=rset.getInt("question_id")%></td>
												<td class="wrapword"><%=rset.getString("querystring").replaceAll("''", "'")%></td>
												<td class="wrapword"><%=status%></td>
												<td class="wrapword"><%=marksAwarded%></td>	
												<td class="wrapword">
												<!-- <a class='loadDiv1' id='<%//=rs.getString("rollnum")%>'  href="FailedTestCases?assignment_id=<%//=assignment_id %>&question_id=<%//=rset.getInt("question_id") %>&user_id=<%//= rs.getString("rollnum")%>">Test cases</a></td> -->
												<!-- <a class="text-warning" data-toggle="modal" data-target="#errorModal"
 												     href="FailedTestCases?assignment_id=<%//=assignment_id %>&question_id=<%//=rset.getInt("question_id") %>&user_id=<%//= rs.getString("rollnum")%>">Test Cases</a>
 												-->
		 												<a id="testCase" href="FailedTestCases?assignment_id=<%=assignment_id %>&question_id=<%=rset.getInt("question_id") %>&user_id=<%= rs.getString("rollnum")%>" 
		 												target="_blank" type="new_tab">Test Cases</a>
		 										</td>
												<td class="wrapword">
												<!-- <a class="text-warning" data-toggle="modal" data-target="#marksModal"
		 												href="PartialMarkDetails.jsp?reqFrom=popUp&assignment_id=<%//=assignment_id %>&question_id=<%//=rset.getInt("question_id") %>&user_id=<%= rs.getString("rollnum")%>">Mark Details</a>
		 												-->
		 												<a id="marks" href="PartialMarkDetails.jsp?reqFrom=popUp&assignment_id=<%=assignment_id %>&question_id=<%=rset.getInt("question_id") %>&user_id=<%= rs.getString("rollnum")%>" target="_blank" type="new_tab"> Mark Details</a>
		 										</td>
												
												</tr>
												<% 
														 
											}
											else if(status.equalsIgnoreCase("Correct"))
											{
											%> <tr> 
														<td class="wrapword">Question<%=rset.getInt("question_id")%></td>
														<td class="wrapword"><%=rset.getString("querystring").replaceAll("''", "'")%></td>
														<td class="wrapword"><%=status%></td>
														<td class="wrapword"><%=+ marksAwarded%></td>
														<td class="wrapword">Correct</td>
														<td class="wrapword">Full Marks</td>
														</tr>
										
											<%} 
									}												
								}%>
								</table> 
							<%
								}catch(Exception e){
									e.printStackTrace();
									throw new ServletException();
							}%>
						</div>

						<%	}//for each student loop Ends %>
						</div></div>	
						<%
		}
		finally{
			dbcon.close();
		}
		%>
		<!-- MODAL CODE STARTS -->
		<div class="modal fade" id="errorModal" tabindex="-1" role="dialog" aria-labelledby="errorModal" aria-hidden="true">
   			 <div class="modal-dialog">
   			  <div class="modal-header">
		     <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><img src="images/close_teal.jpeg" style="width:20px;height:20px;"/></button>
		     
		     
   			 
		          <!--  <button type="button" class="close" data-dismiss="modal" aria-hidden="true">Close</button> -->
		            <!--<h4 class="modal-title" id="myModalLabel">Test Cases</h4>-->
		            </div>
   			  <div class="modal-content">
		    
		            <div class="modal-body">
		            
		       
		            </div>
		          <!-- <div class="modal-footer">
		                 <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
		        </div>-->
		    </div>
		  </div>
		</div>
		<div class="modal fade" id="marksModal" tabindex="-1" role="dialog" aria-labelledby="marksModal" aria-hidden="true">
		    <div class="modal-dialog" role="document">
		       <div class="modal-header">
		              <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><img src="images/close_teal.jpeg" style="width:20px;height:20px;"/></button>
		        
		            <!-- <button type="button" class="close" data-dismiss="modal" aria-hidden="true">Close</button>
		              <h4 class="modal-title" id="myModalLabel">Mark Details</h4>-->
		            </div>
		   <div class="modal-content">
		         
		            <div class="modal-body">
		            </div>
		          <!--  <div class="modal-footer">
		                 <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
		        </div>-->
		    </div>
		  </div>
		</div>
		<!-- MODAL CODE ENDS -->
		<!-- These DIVs are used for DETAILS pop-up -->
		<div id='details1' style='display:none'>
		 <div id='popup1'>
		</div></div>
		
		<div id='details2' style='display:none'>
		<div id='popup2' >
	  </div></div>				
	</fieldset>
  </div>
</body>
</html>