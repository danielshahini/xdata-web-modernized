<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
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
<script>
function checkValue(){
	
		document.forms["updateScalingFactor"].submit();
		
	}
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

if(! Boolean.parseBoolean(session.getAttribute("ltiIntegration").toString())){
		%>
<div id="breadcrumbs"> 
  <a style='color:#353275;text-decoration: none;' href="CourseHome.jsp" target="_top">Home</a> &nbsp; >> &nbsp;
   <a href="InstructorHome.jsp?contextLabel=<%=(String) request.getSession().getAttribute("context_label")%>" style='color:#353275;text-decoration: none;' target="_top"><%=(String) request.getSession().getAttribute("context_label")%></a>&nbsp; >> &nbsp;
   <a href="ListAllAssignments.jsp" style='color:#353275;text-decoration: none;' target="_self">Assignment List</a>&nbsp; >> &nbsp;
   <a href="asgnmentList.jsp?assignmentId=<%=request.getParameter("AssignmentID") %>" target="_self" style='color:#353275;text-decoration: none;'>Assignment Details</a>&nbsp; >> &nbsp;    
   <a href="#" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>Set Weights</a>
 
  </div> 
<%}else{ %>
<div id="breadcrumbs"> 
   <a href="asgnmentList.jsp?assignmentId=<%=request.getParameter("AssignmentID") %>" target="_self" style='color:#353275;text-decoration: none;'>Assignment Details</a>&nbsp; >> &nbsp;    
   <a href="ListOfQuestions.jsp?AssignmentID=<%=request.getParameter("AssignmentID") %>" style='color:#353275;text-decoration: none;'>Question List</a>&nbsp; >> &nbsp;
   <a href="#" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>Set Weights</a>
 
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
				<form  name="updateScalingFactor" action="UpdateScale.jsp" method="post" style="border: none">
				<table align="center" text-align="center" border="1">
				<tr style='background: #E4E4E4; text-align: center;font-weight: bold;'> 
				<th><label>Question Id</label></th>
				<!-- <th><label>Question Description</label></th> -->
				<th><label>Question Description</label></th>
				<th><label>Weight</label></th>
				</tr>
				
				<% 
							Connection dbcon = (new DatabaseConnection()).dbConnection();
							try{
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
								float scale = 100f;
								if (rs.getObject("scale") != null && !rs.wasNull()) {
								    scale = rs.getFloat("scale");
								  }
								%>
								 <tr><td><%=qID%></td>
								 <!-- <td><pre><%=desc%></pre></td>-->
								<td>
								<span><b>Question Text:</b></span> </b>
								<%=desc%> </td>
								<td>
								<input name = "weight<%=qID%>" type="text" value=<%=scale%> >	
								<input name = "AssignmentID" type="hidden" value=<%=assignId%> >		
								</td>
							 	</tr>
						
								<%}	
								rs.close();
							}
							catch (Exception sep) {
								System.out.println("Could not connect to database: " + sep);
								out.println("Error in getting questions");
								throw new ServletException(sep);
								}
							finally
							{
								if(dbcon!=null) dbcon.close();
							}
				%>	
				</table>
				<input type="button" onclick="checkValue()" value="Submit">
				</form>
			</fieldset>
		</div>
	</div>
</body>
</html>