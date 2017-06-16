<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>
<style>
table, tr, td {
    border: 0px;
}
.wrapword{
white-space: -moz-pre-wrap !important;  /* Mozilla, since 1999 */
white-space: -webkit-pre-wrap; /*Chrome & Safari */ 
white-space: -pre-wrap;      /* Opera 4-6 */
white-space: -o-pre-wrap;    /* Opera 7 */
white-space: pre-wrap;       /* css-3 */
word-wrap: break-word;       /* Internet Explorer 5.5+ */
word-break: break-all;
white-space: normal;
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
<script>
function onSubmit(id){
	
	var a = confirm('Are you sure you want to delete the assignment?');
	alert(a);
	if(a==1) 
		window.location.href="deleteAssignment.jsp?assignmentid="+id;
	else
		return false; 
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
  <a href="#" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>Assignment List</a>
  </div>
  <%} %>
  <br/> 
	<div>	 		
		<div class="fieldset">
			<fieldset>
				<legend> List of Assignments</legend>		
	<br>
				<%
						String courseID = (String) request.getSession().getAttribute("context_label");
						//get connection
						Connection dbcon = (new DatabaseConnection()).dbConnection();
						 
						String output = "";
						try {
							PreparedStatement stmt;
							stmt = dbcon
									.prepareStatement("SELECT * FROM xdata_assignment where course_id = ? AND assignment_id >0 ORDER By endtime, assignment_id");
							//	stmt.setString(2, (String)request.getSession().getAttribute("context_label"));
							stmt.setString(1, courseID);
							ResultSet rs;
		 					int index =0;
							rs = stmt.executeQuery();
							while (rs. next()) {
								index++;
								output +="<table border=\"0\" style=\"table-layout:fixed;\" width=\"100%\">";
								//Row 1 - column 1- index value, column 2- Assignment Id, column 3-alignment, column 4-description
								//column 5 - buttons
								output +=	"<tr><td width=\"2%\">"+index +".</td>";
								output += "<td width=\"25%\" class=\"wrapword\"><a href=\"asgnmentList.jsp?assignmentId="+
								rs.getString("assignment_id")+"\"><b>";
								if(rs.getString("assignmentname") != null && !rs.getString("assignmentname").isEmpty())
									output += rs.getString("assignmentname");
								else{
									output += rs.getString("assignment_id"); 
								}
								output += "</b></a></td>";
								output +=   "<td></td><td width=\"35%\" class=\"wrapword\"><b> Start Time: </b>"+rs.getString("starttime") +"</td>";
								output +=   "<td></td><td width=\"35%\" class=\"wrapword\"><b> End Time: </b>"+rs.getString("endtime") +"</td></tr>";
								
								
								output+="</tr><tr>"; 
								//Row 2
								output +=   "<td></td><td colspan=\"3\" class=\"wrapword\"> <b>Description: </b>"+rs.getString("description") +"</td>";
								output +="</tr><tr><td colspan=\"4\"></td></tr><tr><td colspan=\"4\"></td></tr>";
								   
								
								} 
								out.println(output);
								rs.close();
							} catch (Exception err) {

								err.printStackTrace();
								//out.println("Error in getting list of assignments");
								throw new ServletException(err);
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