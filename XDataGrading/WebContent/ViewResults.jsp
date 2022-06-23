<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "https://www.w3.org/TR/html4/loose.dtd">
<html>
<head>  
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>
 <link rel="stylesheet" href="highlight/styles/xcode_white.css">  
<link rel="stylesheet" href="highlight/styles/default_white.css">
<script src="highlight/highlight.pack.js"></script>  
<script src="https://code.jquery.com/ui/1.11.0/jquery-ui.min.js"></script>
<script type="text/javascript" src = "scripts/jquery.js"></script> 
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Get Results Of Assignment</title>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@ page import="java.text.*"%>
<%@page import="database.*"%>
<%@page import="java.sql.PreparedStatement"%>

<script type="text/javascript">   
hljs.initHighlightingOnLoad();
</script> 
<style>
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
}else if(session.getAttribute("LOGIN_USER") != null && !session.getAttribute("LOGIN_USER").equals("ADMIN")
 		&& session.getAttribute("role") != null && (!session.getAttribute("role").equals("instructor") || !session.getAttribute("role").equals("tester"))){
	response.sendRedirect("index.jsp?NotAuthorised=true");
	session.invalidate();
	return;
}

if(! Boolean.parseBoolean(session.getAttribute("ltiIntegration").toString())){
		%>
<div id="breadcrumbs"> 
  <a style='color:#353275;text-decoration: none;' href="CourseHome.jsp" target="_top">Home</a> &nbsp; >> &nbsp;
   <a href="InstructorHome.jsp?contextLabel=<%=(String) request.getSession().getAttribute("context_label")%>" style='color:#353275;text-decoration: none;' target="_top"><%=(String) request.getSession().getAttribute("context_label")%></a>&nbsp; >> &nbsp;
   <a href="ListAllAssignments.jsp" style='color:#353275;text-decoration: none;' target="_self">Assignment List</a>&nbsp; >> &nbsp;
   <a href="asgnmentList.jsp?assignmentId=<%=request.getParameter("AssignmentID") %>" target="_self" style='color:#353275;text-decoration: none;'>Assignment Details</a>&nbsp; >> &nbsp;    
   <a href="#" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>View Results</a>
 
  </div> 
<%}else{ %>
<div id="breadcrumbs"> 
   <a href="asgnmentList.jsp?assignmentId=<%=request.getParameter("AssignmentID") %>" target="_self" style='color:#353275;text-decoration: none;'>Assignment Details</a>&nbsp; >> &nbsp;    
   <a href="ListOfQuestions.jsp?AssignmentID=<%=request.getParameter("AssignmentID") %>" style='color:#353275;text-decoration: none;'>Question List</a>&nbsp; >> &nbsp;
   <a href="#" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>View Results</a>
 
 </div> 
<%} %>
<br/>
	<div>
		<br /> <br />
		<div class="fieldset">
			<%
				
				String courseId = (String) request.getSession().getAttribute(
						"context_label");
				int assignId = Integer.parseInt(request.getParameter("AssignmentID"));
		
			%>
			<fieldset> 
				<legend> Details of Assignment <%=assignId%>
				</legend>
				<table align="center" text-align="center" border="1">
				<tr style='background: #E4E4E4; text-align: center;font-weight: bold;'> 
				<th><label>Question</label></th>
				<!-- <th><label>Question Description</label></th> -->
				<th><label>Correct Query</label></th>
				<th><label>Correct Answers</label></th>
				<th><label>Incorrect Answers</label></th>
				</tr>
				<% 
					String output = "";
					boolean matchAll;
					String matchOption = "";
 
							try(Connection dbcon = (new DatabaseConnection()).dbConnection()){

							//get list of questions in assignment
							String questions = "select * from xdata_qinfo where course_id = ? and assignment_id = ?"
									+"order by assignment_id,question_id";
							PreparedStatement stmt = dbcon.prepareStatement(questions);
							stmt.setString(1, courseId);
							stmt.setInt(2, assignId);
							
							ResultSet rs = stmt.executeQuery();
							while (rs.next()) {
								int queryindex = 1;
								
								int qID = rs.getInt("question_id");
								//String correct = rs.getString("correctquery");
								String desc = rs.getString("querytext");
								matchAll = rs.getBoolean("matchallqueries");
								if(matchAll){
									matchOption = "Match all results";
								}
								else{
									matchOption = "Match any one result";
								}
								String getCount = "select count(distinct rollnum) as count from xdata_student_queries where assignment_id = ? and question_id = ? and verifiedcorrect = 'f'";
								stmt = dbcon.prepareStatement(getCount);

								stmt.setInt(1, assignId);
								stmt.setInt(2,qID);
								ResultSet rs1 = stmt.executeQuery();
								int incorrectcount = 0;
								int correctcount=0;
								if (rs1.next())
									incorrectcount = Integer.parseInt(rs1.getString("count"));
								//Get correct count
								String getCorrectCount = "select count(distinct rollnum) as count from xdata_student_queries where assignment_id = ? and question_id = ? and verifiedcorrect = 't'";
								stmt = dbcon.prepareStatement(getCorrectCount);
				 				stmt.setInt(1, assignId);
								stmt.setInt(2,qID);
								ResultSet rset = stmt.executeQuery();
								if (rset.next())
									correctcount = Integer.parseInt(rset.getString("count"));
								
								String queries = "select sql from xdata_instructor_query where course_id = ? and assignment_id = ? and question_id=?";
								PreparedStatement stmt1 = dbcon.prepareStatement(queries);
								stmt1.setString(1, courseId);
								stmt1.setInt(2, assignId);
								stmt1.setInt(3,qID);
								
								ResultSet rs2 = stmt1.executeQuery();
								%>
								 <tr><td><%=qID%></td>
								 <!-- <td><%=desc%></td>-->
								<td>
								<span><b>Question Text:</b></span> </b>
								<%=desc%> <br>
								<div class="matchOpt" id="matchOpt<%=qID %>" style='display:none;font-family: helvetica'>
								<b>Multiple SQL Option: <%=matchOption %></b>
								</div>
								<br/>
						<%		while(rs2.next()){ 
									if( queryindex > 1) { %>  									
								<script>
									if(document.getElementById('matchOpt'+<%=qID%>).style.display == "none"){
										document.getElementById('matchOpt'+<%=qID%>).style.display='block';
									}
								</script> 
									<%} %>
									
									<span><b> Ans.<%=queryindex%>.</b></span>
											  <pre><code class="sql">
											   <%=CommonFunctions.encodeHTML(rs2.getString("sql"))%>
									</code></pre><br>
									<%queryindex++; 
									} 	
									  %>
									  
								</td><td><%=correctcount%></td><td> <%=incorrectcount%></td></tr>
						
							 
								<%}
								
							rs.close();
							dbcon.close();
							}
				%>
			</fieldset>
		</div>
	</div>
</body>
</html>