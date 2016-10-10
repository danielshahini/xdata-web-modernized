<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%> 
<%@ page import="java.text.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.CommonFunctions"%>
<%@page import="database.DatabaseProperties"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 
 <link rel="stylesheet" href="../css/structure.css" type="text/css"/>
 <link rel="stylesheet" href="../highlight/styles/xcode.css">  
<link rel="stylesheet" href="../highlight/styles/default.css">
<script type="text/javascript" src = "../scripts/jquery.js"></script>
<script src="../highlight/highlight.pack.js"></script>  
<script type="text/javascript">
hljs.initHighlightingOnLoad();
</script> 
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>List Of Questions</title>
<style>
textarea,select {
	font: 12px/12px Arial, Helvetica, sans-serif;
	padding: 0;
}
.questionelement .answer .editbutton a{
	text-decoration:none;
	color: #353275;
	float:left;
}
.separator{ 
	border-right:1px solid black; 
	margin:0px; 
	float: right; 
	margin-right: 3px;
	width:1px;
	margin-left: 2px;
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

.stop-scrolling {
	height: 100%;
	/* overflow: hidden; */
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
  <a style='color:#353275;text-decoration: none;' href="../CourseHome.jsp" target="_top">Home</a> &nbsp; >> &nbsp;
  
   <a style='color:#353275;text-decoration: none;' href="ListAllAssignments.jsp" target="_self">Assignment List</a>&nbsp; >> &nbsp;
   <a href="#" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>Question List</a>
  </div>
  <%} %>
 
	<div><br/>
		<div class="fieldset">
			<fieldset>
			<%if(((String)session.getAttribute("LOGIN_USER")).equalsIgnoreCase("guest") ){%>
					<!-- <legend> Assignment Details</legend> -->
			<%}else{%>	
				<legend> Assignment Details</legend>
			<%}%>
				
				<%			
					String courseID = (String) request.getSession().getAttribute(
								"context_label");
				String user =(String) request.getSession().getAttribute(
						"LOGIN_USER"); 
 						int assignID = Integer.parseInt(request.getParameter("assignmentid"));					
						String instructions = (new CommonFunctions()).getStudentAssignmentInstructions(courseID, assignID, user);
						out.println(instructions);
				%>		
			</fieldset>
			<fieldset>
				<legend> Assignment Instructions</legend>
			<%if(((String)session.getAttribute("LOGIN_USER")).equalsIgnoreCase("guest") ){%>
				<ul> 
					<li>Click edit to enter your answer. The selection conditions are case sensitive</li>
					<li>Please take a look on <a href='../images/universitySchema.pdf' target='_blank'>Schema diagram</a> of the default schema being used</li>
				</ul>
			<%}else{%>
						<ul> 
					<li>Click edit to enter your answer</li>
					<li>Be cautious, the selection conditions are case sensitive</li>
					<li>On editing, the query will be run against datasets</li>
					<li>You will be forwarded to a page where the result of submission would be shown</li>
					<li>Please take a look on <a href='../images/universitySchema.pdf' target='_blank'>Schema diagram</a> of the default schema being used</li>
				</ul>
			<%}%>
			<p></p>
			</fieldset> 
			<br/>
			<fieldset>
				<legend> List of Questions</legend>
				<%
						List <Integer> schemaIdList = new ArrayList();
						int defaultSchemaID=0;
						boolean questionGraded = false;
						int optionalSchemaID =0;
						boolean isShowQuestions = false;
						boolean isInteractiveAssignment = false;
						String schemaFileName = "";
						String dataFileName="";
						String studentId = (String) request.getParameter("studentId");							
							String status = request.getParameter("status");												
							if (studentId == null)
								studentId = (String) request.getSession().getAttribute(
										"user_id");
							//get connection
							Connection dbcon = (new DatabaseConnection()).dbConnection();
 
							Timestamp start = null;
							Timestamp end = null;
							try {
								PreparedStatement stmt;
								ResultSet rs = null;
								stmt = dbcon
										.prepareStatement("SELECT * FROM xdata_assignment where assignment_id=? and course_id=?");
								stmt.setInt(1, assignID);
								stmt.setString(2, courseID.trim());
								rs = stmt.executeQuery();
								while (rs.next()) {
									start = rs.getTimestamp("starttime");
									end = rs.getTimestamp("endtime");
									defaultSchemaID = rs.getInt("defaultschemaid");
									isInteractiveAssignment = rs.getBoolean("learning_mode");
									//start=rs.getString("end_date");
								}
								schemaIdList.add(defaultSchemaID);
								rs.close();
								stmt.close();
							} catch (Exception err) {
								err.printStackTrace();
								throw new ServletException(err);

							}
							
							System.out.println("Start :" + start);
							System.out.println("End :" + end);
							//now check whether current time is less than start time.Then only assignment can be edited
							boolean yes = false;
							SimpleDateFormat formatter = new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss");
							formatter.setLenient(false);
							String ending = formatter.format(end);
							String starting = formatter.format(start);
							//String oldTime = "2012-07-11 10:55:21";
							java.util.Date oldDate = formatter.parse(ending);
							java.util.Date strtDate = formatter.parse(starting); 
							//get current date
							Calendar c = Calendar.getInstance();

							String currentDate = formatter.format(c.getTime());
							java.util.Date current = formatter.parse(currentDate);

							//compare times
							if (current.compareTo(oldDate) > 0) {
								yes = true;
							}
							//Is current time is less than assignment strt time, dont show the questions.
							if(current.compareTo(strtDate) > 0){
								isShowQuestions = true;
							}
							//String output = "<table  cellspacing=\"20\"  class=\"authors-list\" id=\"queryTable\" align=\"center\"> <tr> <th >Question ID</th>       <th >Question Description</th>  <th >Your Answer</th> <th> </th></tr>";
							String output= "";
						
							try {
								if(isShowQuestions){
									
								int q_index=0;
								PreparedStatement stmt1;
								//stmt = dbcon.prepareStatement("SELECT * FROM  xdata_qinfo ,assignment where qinfo.assignment_id=? AND qinfo.assignment_id=assignment.assignment_id");
								stmt1 = dbcon
										.prepareStatement("SELECT * FROM xdata_qinfo  where assignment_id=? and course_id=? order by question_id");
								stmt1.setInt(1, assignID);
								stmt1.setString(2, courseID.trim());
								
								ResultSet rs1 = stmt1.executeQuery();
								
								while (rs1.next()) {

									int qID = rs1.getInt("question_id");
									q_index++;
									System.out.println(qID);
									String desc = rs1.getString("querytext");
									optionalSchemaID = rs1.getInt("optionalschemaid");
									
									if(optionalSchemaID != defaultSchemaID){%>
										<script>
										document.getElementById(<%=assignID%>).style.display='block';
										</script> 
									<%}
									stmt1 = dbcon
											.prepareStatement("SELECT * FROM xdata_student_queries  where assignment_id=? and question_id=? and rollnum=?");
									
									String queryId = "A" + assignID + "Q"+qID+"S1";
									stmt1.setInt(1, assignID);
									stmt1.setInt(2,qID);
									stmt1.setString(3, studentId.trim());
									ResultSet rs2 = stmt1.executeQuery();
									
									boolean verified = false; 
									String studentAnswer = "";
									if (rs2.next()){
										studentAnswer = rs2.getString("querystring");
										if(rs2.getString("verifiedcorrect") != null){
											verified = rs2.getBoolean("verifiedcorrect");
											questionGraded = true;
										}else{
										//if(rs2.getString("result") != null){
											verified = false;
											questionGraded = false;
										} 
									}
									String isCorrect = "Incorrect";
									if(verified) isCorrect = "Correct";
									String remote = "QuestionDetails.jsp?AssignmentID="
											+ assignID + "&&courseId=" + courseID
											+ "&&questionId=" + qID + "&&studentId="
											+ studentId + "\" target = \"rightPage\"";

									String viewGrade = "ViewGradesOfAssignment.jsp?AssignmentID="
											+ assignID + "&&courseId=" + courseID 
											+ "&&questionId=" + qID + "&&studentId=" 
											+ studentId + "\" target = \"rightPage\"";
									if (yes == false){
										output += "<tr><td>Question: "
												+ qID 
												+ "</td>"
												+ "<td>"
												+ desc
												+ "</td>"
												+ "<td>"
												+ CommonFunctions.encodeHTML(studentAnswer)
												+ "</td>"
												+ "<td><input type=\"button\" onClick=\"window.location.href='"
												+ remote + " value=\"Edit\" > <br/> \n ";
									}
									else{
										output += CommonFunctions.encodeHTML(studentAnswer)
											//	+ "</td><br/> \n";
												+ "<td><input type=\"button\" onClick=\"window.location.href='"
												+ viewGrade
												+ " value=\"View Grade\" > <br/> \n";
									}  
									rs2.close();
									
								
									%>
								
							<div class="questionelement">
								<div class="question"><span>Q<%= q_index %>. </span><%= desc %>
								<!-- Test optionalSchema = <%//=optionalSchemaID %> --> 
								<%if(optionalSchemaID != defaultSchemaID && !schemaIdList.contains(optionalSchemaID)){
									//Add schema id to the list if it is encountered first time and show link to download
									schemaIdList.add(optionalSchemaID);
									PreparedStatement state = dbcon.prepareStatement("select sample_data_name from xdata_sampledata where schema_id=? and course_id=?");
									state.setInt(1,optionalSchemaID);
									state.setString(2,courseID);
									ResultSet result= state.executeQuery();
									
									PreparedStatement statemnt = dbcon.prepareStatement("select schema_name from xdata_schemainfo where schema_id=? and course_id=?");
									statemnt.setInt(1,optionalSchemaID);
									statemnt.setString(2,courseID);
									ResultSet resultrs= statemnt.executeQuery();
									
									
									if(resultrs.next()){
									%><div>
									<a class="getFile"  href="showSchemaFile.jsp?schema_id=<%=optionalSchemaID%>" >
									<%=resultrs.getString("schema_name") %></a>&nbsp;&nbsp;&nbsp;&nbsp;
									<%if(result.next()){%>
									<a class="getFile"  href="showSampleData.jsp?schema_id=<%=optionalSchemaID%>" >
									<%=result.getString("sample_data_name") %></a>
									<%}%>
									</div>
								<%}
									}%>
								</div>
								<div class="answer">
								<pre><code class="sql"><label style="font-style:bold;font-weight: bold">Ans. </label><%= studentAnswer %></code></pre>
								</div></br>
								<%if(user.equalsIgnoreCase("guest")){ %>
									<div class="editbutton" ><a style="text-decoration:none;color: #353275;float:left;padding-left:20px;" href="<%=yes?viewGrade:remote %>"><%=yes?"View Grade":"Edit" %></a>
								<%}else{ %>
										<div class="editbutton" ><a style="text-decoration:none;color: #353275;float:right;padding-left:20px;" href="<%=yes?viewGrade:remote %>"><%=yes?"View Grade":"Edit" %></a>
								<%} %>
							
								<% if(studentAnswer != null && !yes && studentAnswer.length() > 0 && isInteractiveAssignment && !user.equalsIgnoreCase("guest")){ %>
									
									<a href='../StudentTestCase?user_id=<%=studentId%>&&assignment_id=<%=assignID %>&&question_id=<%=qID %>&&status=<%=isCorrect%>&&query=<%=CommonFunctions.encodeURIComponent(studentAnswer)%>&&Error=Incorrect Query Syntax'>Show Result</a>
									
								<%}
								
								if(!user.equalsIgnoreCase("guest")) {%> 
								
								<div class = "status"><span style='font-weight:bold;'>Status:</span>
								 
								<% if(verified && questionGraded){ %> 
									<font color='green'> Ok 
								<%} 
								else if(!questionGraded){
									%>
									<font color='#353275'> Not Graded
									<% 
								}
								else if(!verified &&
										(studentAnswer == null ||(studentAnswer !=null && studentAnswer.isEmpty())))
										{ %>
										<font color='#353275'> Not Graded
										<%}else {%>
											<font color='red'> Incorrect 
										<%}
										%>
								</font></div>
								</div>							
						
						<% }else{%>
							</div>
						<%}
									}
								rs1.close();
								stmt1.close();
								output = "";
								}
							else {
								%>
									<div class="questionelement">
										<label>
										<font color='#353275'>The assignment starts on <%=strtDate%>. Please wait till the start time to view the question. 
										</font>
										</label>
									</div>
								<%
							}
								
						
						if(status != null){
							if(status.equals("NoDataset")){
								
							}
							else if(status.equals("Correct")){
								output += "<p>Passed test cases</p>";
							}
							else if(status.equals("Error")){
								output += "<p style=\"color:red;\">Error occurred. Please ensure:</p>"
										+"<ul>"
										+ "<li>The query syntax is correct</li>"
										+"<ul>";
							}
							else if(status.equals("Incorrect")){
								output += "<p style=\"color:red;\">Test case failed. Please ensure:</p>"
										+"<ul><li>You have submitted only the query without any comments or	results</li>"
										+ "<li>The query syntax is correct</li>"
										+ "<li>The query does not use any specific schema name</li>"
										+ "<li>The order of projected columns</li>"
										+ "<li>Check query predicates</li>"
										+"<ul>";
							}
							else if(status.equals("expired")){
								output += "<p>Submission not saved. Assignment expired.</p>";
							}
						}
						
						out.println(output);

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
	</div>
</body>
</html>