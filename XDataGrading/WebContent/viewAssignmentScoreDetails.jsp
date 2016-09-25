<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Assignment List</title>
<!-- <script type="text/javascript" src="scripts/wufoo.js"></script>-->
<link rel="stylesheet" href="highlight/styles/xcode.css"/>  
<link rel="stylesheet" href="highlight/styles/default.css"/>
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>
<script src="highlight/highlight.pack.js"></script>
<script type="text/javascript" src = "scripts/jquery.js"></script>
<script type="text/javascript" src="scripts/zeroclipboard/ZeroClipboard.js"></script>

<script type="text/javascript">
hljs.initHighlightingOnLoad(); 
function getParameterByName(name) { 		
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
} 
</script>
</head>
<body>
<br/>
	<div>
		<div class="fieldset">
			<fieldset>
				<legend>Score Details</legend>
				<%
				String  assignment_id = request.getParameter("assignment_id");
				
				
				Connection dbcon = null;
				try{
					dbcon = (new DatabaseConnection()).dbConnection(); 
					int NoOfQuestionsAnswered = 0;
					String noOfQuestionsAnswered="select count(*) as answered from xdata_student_queries queries where queries.assignment_id =? and " +
							"rollnum=? and querystring is not null";
					PreparedStatement stmtNoOfAnsweredQuestions=dbcon.prepareStatement(noOfQuestionsAnswered);
					stmtNoOfAnsweredQuestions.setInt(1,Integer.parseInt(assignment_id));
					stmtNoOfAnsweredQuestions.setString(2,request.getParameter("rollnum"));
					
					ResultSet NoOfQuestionAnsweredRs=stmtNoOfAnsweredQuestions.executeQuery();
					if(NoOfQuestionAnsweredRs.next()){
						
						NoOfQuestionsAnswered =(NoOfQuestionAnsweredRs.getInt("answered")) ;
						System.out.println("No Of Questions Answered = " + NoOfQuestionsAnswered);
					}
					%> 
						
						<br>
						<fieldset>
						<legend>Student Details</legend><br/>
						 
						 <lable><b>Name:</b></label><label style='color:#353275'><b><%=request.getParameter("user_name")%> </b><br/></label>
						 <lable><b>Roll no:</b></label><label style='color:#353275'><b><%=request.getParameter("rollnum") %></b><br/></label>
						 <label><b>Email : </b></label><label style='color:#353275'><b><%=request.getParameter("email") %></b><br/></label>
						 <lable><b>Score :</b></label><label style='color:#353275'><b><%=request.getParameter("score") %>/ <%=request.getParameter("totalScore") %></b><br/></label>
						 <lable><b>No of questions Answered :</b></label><label style='color:#353275'><b><%=NoOfQuestionsAnswered %></b></label>
						 
						</fieldset>
						<br/>
						<fieldset>
						<legend>Marks</legend><br/>
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
							String course_id = (String) request.getSession().getAttribute("context_label");
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
							stmt.setString(3,request.getParameter("rollnum"));
							//System.out.println(stmt.toString());
							ResultSet rset=stmt.executeQuery();
							while(rset.next()){
								float marksAwarded = 0.0f;
								int maxMarks = 0;
								PreparedStatement stmt1=dbcon.prepareStatement(marks);
								stmt1.setInt(1,Integer.parseInt(assignment_id));
								stmt1.setString(2, request.getParameter("rollnum"));
								stmt1.setInt(3,rset.getInt("question_id"));
								maxMarks = rset.getInt("totalmarks");
								ResultSet rs1=stmt1.executeQuery();
								 
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
									%><!-- Unanswered questions will not be shown  -->
									
										<%if(rset.getString("querystring") != null && !rset.getString("querystring").isEmpty()){%>
												<tr><td class="wrapword">Question <%=rset.getInt("question_id")%></td>
												<td class="wrapword"><pre><code class="sql"><%=rset.getString("querystring").replaceAll("''", "'")%></code></pre></td>
										
											<td class="wrapword"><%=status%></td>
											<td class="wrapword"><%=marksAwarded%></td>	
											<td class="wrapword">
											<!-- <a class='loadDiv1' id='<%//=rs.getString("rollnum")%>'  href="FailedTestCases?assignment_id=<%//=assignment_id %>&question_id=<%//=rset.getInt("question_id") %>&user_id=<%//= rs.getString("rollnum")%>">Test cases</a></td> -->
											<!-- <a class="text-warning" data-toggle="modal" data-target="#errorModal"
												     href="FailedTestCases?assignment_id=<%//=assignment_id %>&question_id=<%//=rset.getInt("question_id") %>&user_id=<%//= rs.getString("rollnum")%>">Test Cases</a>
												-->
	 												<a id="testCase" style='color: #00f;'  href="FailedTestCases?assignment_id=<%=assignment_id %>&question_id=<%=rset.getInt("question_id") %>&user_id=<%=request.getParameter("rollnum")%>" 
	 												target="_blank" type="new_tab">Test Cases</a>
	 										</td>
											<td class="wrapword">
											<!-- <a class="text-warning" data-toggle="modal" data-target="#marksModal"
	 												href="PartialMarkDetails.jsp?reqFrom=popUp&assignment_id=<%//=assignment_id %>&question_id=<%//=rset.getInt("question_id") %>&user_id=<%= request.getParameter("rollnum")%>">Mark Details</a>
	 												-->
	 												<a id="marks" style='color: #00f;' href="PartialMarkDetails.jsp?reqFrom=popUp&assignment_id=<%=assignment_id %>&question_id=<%=rset.getInt("question_id") %>&user_id=<%= request.getParameter("rollnum")%>" target="_blank" type="new_tab"> Mark Details</a>
	 										</td>
											
											</tr>
										<% }
												 
									}
									else if(status.equalsIgnoreCase("Correct"))
									{
									%> <tr> 
												<td class="wrapword">Question<%=rset.getInt("question_id")%></td>
												<td class="wrapword"><pre><code class="sql"><%=rset.getString("querystring").replaceAll("''", "'")%></code></pre></td>
												<td class="wrapword"><%=status%></td>
												<td class="wrapword"><%=+ marksAwarded%></td>
												<td class="wrapword">Correct</td>
												<td class="wrapword">Full Marks</td>
												</tr>
								
									<%} 
							}
								// If verified correct is null, it could be unanswered query. Show the answer as not answered and marks as 0
								else{%>
									<tr> 
									<td class="wrapword">Question<%=rset.getInt("question_id")%></td>
									<td class="wrapword"><pre><code class="sql"><%="Not answered"%></code></pre></td>
									<td class="wrapword"><%="Not answered"%></td>
									<td class="wrapword"><%="0"%></td>
									<td class="wrapword">Not answered</td>
									<td class="wrapword">Not answered</td>
									</tr>
							<%	}
								
						}%>
						</table> 
					<%
						}catch(Exception e){
							e.printStackTrace();
							throw new ServletException();
					}%>
				</div>

				
				</div></div>	
						<%
		}
		finally{
			dbcon.close();
		}
		%>
				
</body>
</html>