<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="erroPage.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "https://www.w3.org/TR/html4/loose.dtd">
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@ page import="java.text.*"%>
<%@page import="database.*"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>

<script type="text/javascript" src="scripts/wufoo.js"></script>
<link rel="stylesheet" href="css/structure.css" type="text/css" />
<script type="text/javascript" src = "scripts/jquery.js"></script>
<script type="text/javascript" src = "scripts/jquery-ui.min.js"></script>
<script src="scripts/bootstrap/dist/js/bootstrap.js"></script>
<link href="scripts/bootstrap/dist/css/bootstrap.css" rel="stylesheet"/>
<link rel="stylesheet" href="highlight/styles/xcode.css">  
<link rel="stylesheet" href="highlight/styles/default.css">
<script src="highlight/highlight.pack.js"></script>
<script type="text/javascript">
 hljs.initHighlightingOnLoad();
$(document).ready(function () { 
$("#errorModal").on("show.bs.modal", function(e) {
var link = $(e.relatedTarget);
$(this).find(".modal-body").load(link.attr("href"));
});
$("#marksModal").on("show.bs.modal", function(e) {
        var link = $(e.relatedTarget);
        $(this).find(".modal-body").load(link.attr("href"));
 });});
 
</script>
<link rel="canonical" href="https://www.wufoo.com/gallery/designs/template.html">
<link rel="stylesheet" href="css/structure.css" type="text/css" />
<style>
#breadcrumbs

{position: absolute;
padding-left:10px;
padding-right:10px;left: 5px;
top: 10px;font: 13px/13px Arial, Helvetica, sans-serif;background-color: #f0f0f0;
font-weight: bold;}
</style>
</head>
<body id="public">
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}
else if(session.getAttribute("LOGIN_USER") != null && !session.getAttribute("LOGIN_USER").equals("ADMIN")
	&& session.getAttribute("role") != null && !session.getAttribute("role").equals("instructor")){
response.sendRedirect("index.jsp?NotAuthorised=true");
session.invalidate();
return;
}
%>
<br/>

<div id="fieldset">
						<fieldset><legend>Result</legend>
						<form class="wufoo" action="TestCaseDataset" method="get">
						
<%
/*This JSP evaluates the late submission answers of a particular student - Added for DB LAB SUBMISSIONS during 2016-July. 
To be deleted after late submissions are handled properly  */

int assignment_id=Integer.parseInt((String)request.getParameter("assignment_id"));
String course_id=request.getParameter("course_id");
String rollnum = request.getParameter("rollnum");
String userName = request.getParameter("user_name");
String email = request.getParameter("email");


String result = "select querystring, totalmarks, verifiedcorrect,qi.question_id as id,qi.querytext as desc, "+
				" q.rollnum,score from  xdata_student_queries q "+
				" left outer join xdata_qinfo qi on ( q.assignment_id = qi.assignment_id"+
				" and q.course_id = qi.course_id and q.question_id=qi.question_id) "+ 
				"where q.assignment_id =? and q.course_id =? and q.rollnum= ? order by qi.question_id";
 	Connection dbcon = (new DatabaseConnection()).dbConnection();
	PreparedStatement pstmt = dbcon.prepareStatement(result);
	pstmt.setInt(1,assignment_id);
	pstmt.setString(2,course_id);
	pstmt.setString(3,rollnum); 

	// pstmt.setInt(2, question_id);
	ResultSet rs = pstmt.executeQuery();  
	boolean present = false;
	
	%>
	<fieldset><legend>Student Details</legend>
	
	<div>
						<label><b>XData Course Id:</b></label>&nbsp;&nbsp;<label><%=course_id %></label><br>
						<label><b>Assignment Id:</b></label>&nbsp;&nbsp;<label><%=assignment_id %></label><br>
						<label><b>Student Name:</b></label>&nbsp;&nbsp;<label><%=userName %></label><br>
						<label><b>XData Student Id:</b></label>&nbsp;&nbsp;<label><%=rollnum%></label><br>
						<label><b>Email:</b></label>&nbsp;&nbsp;<label><%=email%></label><br>
						
					</div><br>
	</fieldset> 
	
	<table>
	
	<tr style='background: #E4E4E4; text-align: center;font-weight: bold;'>
		<td>Question Id</td><td>Question Text</td><td style = 'width: 500px;'>Student Answer</td>
	<td>Status</td><td>Score</td><td>Error Details</td><td>Mark Details</td></tr>
	
	<%  
	PrintWriter out_assignment = response.getWriter();
	while (rs.next()) {
					present = true;
					
					//index ++;
					if(rs.getString("querystring") != null && !rs.getString("querystring").isEmpty()){
					if (rs.getString("verifiedcorrect") != null && !rs.getBoolean("verifiedcorrect")) {
						%>
						<tr>
						<td> <%= rs.getInt("id")%></td>
						<td><%= rs.getString("desc")%></td>
						<td> <pre><code class="sql"> <%=rs.getString("querystring")%>
								</code></pre></td>
							<td>Wrong</td>"
							<td><%=Math.round(rs.getFloat("score")) %></td>"							
							<td>"					
							<a id="testCase" href="FailedTestCases?user_id='<%=rs.getString("rollnum")%>'&&assignment_id='<%=assignment_id%>'
									&&question_id='<%=rs.getInt("id")%>' "  target="_blank" type="new_tab">Test Cases</a></td>												
								<td>
								<a id="marks" href="PartialMarkDetails.jsp?user_id='<%=rs.getString("rollnum")%>'&&reqFrom='popUp'&&assignment_id='<%=assignment_id%>'&&question_id='<%=rs.getInt("id")%>"
								  target="_blank" type="new_tab"> Mark Details</a>"
								</td></tr>
				<%	} else if(rs.getString("verifiedcorrect") != null && rs.getBoolean("verifiedcorrect")){
					%>
						<tr><td>
								<%=rs.getInt("id")%></td><td>
								<%=rs.getString("desc")%></td>
								<td> <pre><code class=\"sql\">
								   <%=rs.getString("querystring")%>
								</code></pre></td><td>Correct
								</td><td><%= Math.round(rs.getFloat("score"))%></td>
								<td>No error</td>
								<td>Full marks</td></tr>
					<%}
					else{%> 
						<tr><td>
								<%=rs.getInt("id")%></td><td>
								<%=rs.getString("desc")%></td>
								<td> <pre><code class="sql">%>
								<%=rs.getString("querystring")%>
								</code></pre></td><td>Not Graded
								</td><td> 
								</td> 
								<td></td></tr>
					<%}
					}else{//If student has not answered
						%>
						<tr><td>
							<%=rs.getInt("id")%></td><td>
							<%=rs.getString("desc")%></td>
							<td> <pre><code class="sql">
								<%= rs.getString("querystring")%>
								</code></pre></td><td>Not Answered
								</td><td> 0 
								</td>
								<td> </td> 
								<td></td></tr>
					<%}
				}
				
				
				%> 
					</table>
					
			   
				
				
				<!-- MODAL CODE STARTS -->
						<div class="modal fade" id="errorModal" tabindex="-1" role="dialog" aria-labelledby="errorModal" aria-hidden="true">
						<div class="modal-dialog">
						<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal" aria-hidden="true"><img src="images/close_teal.jpeg" style="width:20px;height:20px;"/></button>
						<!--<h4 class=\"modal-title\" id=\"myModalLabel\">Test Cases</h4>-->
						</div>
						<div class="modal-content">
						
						<div class="modal-body">   
						</div><!-- <div class=\"modal-footer\">
						<button type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\">Close</button>
						</div>-->
						</div></div></div>
						<div class="modal fade" id="marksModal" tabindex="-1" role="dialog" aria-labelledby="marksModal" aria-hidden="true">
						<div class="modal-dialog">
						<div class="modal-header">
						 <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><img src="images/close_teal.jpeg" style="width:20px;height:20px;"/></button>
						<!--<h4 class=\"modal-title\" id=\"myModalLabel\">Mark Details</h4>-->
						</div>
						<div class="modal-content">
						
						<div class="modal-body">
						</div>
						
						</div>
						</div>
						</div>
				

</body>
</html>