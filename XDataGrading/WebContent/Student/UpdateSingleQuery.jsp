<%@page import="com.sun.mail.imap.protocol.Status"%>
<%@page import="util.TesterDatasource"%>
<%@page import="database.CommonFunctions"%>
<%@page import="evaluation.TestAssignment"%> 
<%@page import="evaluation.QueryStatusData" %>
<%@page import="evaluation.QueryStatusData.QueryStatus" %>
<%@page import = "evaluation.FailedDataSetValues" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page import="java.text.*"%>
<%@page import="java.util.Date"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 
 <link rel="stylesheet" href="../css/structure.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Update a single query</title>

<style>

</style>

</head>
<body>

<%!
//Storing in student log
public void logStore(Connection dbcon,String courseID,int assignmentID,String questionID,String studentID,Timestamp subTimeStamp,String stdquery) throws Exception
{
	PreparedStatement stmt = dbcon.prepareStatement("INSERT INTO xdata_student_log (course_id, assignment_id, question_id, rollnum, eventtime, querytext) VALUES(?,?,?,?,?,?)");
	stmt.setString(1,courseID);
	stmt.setInt(2,assignmentID);
	stmt.setInt(3, Integer.parseInt(questionID));
	stmt.setString(4, studentID);
	stmt.setTimestamp(5, subTimeStamp);
	stmt.setString(6, stdquery);
	stmt.executeUpdate();
}

%>
	<%
	
	
	if (session.getAttribute("LOGIN_USER") == null) {
		response.sendRedirect("index.jsp?TimeOut=true");
		return;
	}
	
		Boolean interactiveMode = false;
			String questionID = (String) request.getParameter("questionId");
			int asID = Integer.parseInt(request.getParameter("assignmentId"));
			String courseID = (String) request.getSession().getAttribute(
			"context_label");
			String studentID = (String) request.getParameter("studentId");
			String correctquery = (String) request.getParameter("query");
			String query_id= "A"+asID+"Q"+questionID+"S1";
			//correctquery = correctquery;
			//.replaceAll("[ ;]+$", "");
			
			Connection dbcon = null;

			dbcon = (new DatabaseConnection()).dbConnection();
			
			
			Connection testConn = ((new util.DatabaseConnection()).getTesterConnection(asID)).getTesterConn();
			
	try{
			PreparedStatement stmt;
			ResultSet rs = null;
			
			Timestamp start = null;
			Timestamp end = null;
	 
	try {
		stmt = dbcon.prepareStatement("SELECT * FROM xdata_assignment where assignment_id=? and course_id=?");
		stmt.setInt(1, asID);
		stmt.setString(2, courseID.trim());
		rs = stmt.executeQuery();
		while (rs.next()) {
			start = rs.getTimestamp("starttime");
			end = rs.getTimestamp("endtime");
			interactiveMode = rs.getBoolean("learning_mode");
		}
	
	} catch (Exception err) {
		err.printStackTrace();
		throw new ServletException(err);
			}
	
			
			System.out.println("Start :" + start);
			System.out.println("End :" + end);
			//now check whether current time is less than start time.Then only assignment can be edited
			boolean expired = false;
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			formatter.setLenient(false);
			String ending = formatter.format(end);
			String starting = formatter.format(start);

			java.util.Date oldDate = formatter.parse(ending);
			
			//get current date
			Calendar c = Calendar.getInstance();

			String currentDate = formatter.format(c.getTime());
			java.util.Date current = formatter.parse(currentDate);

			//compare times
			if (current.compareTo(oldDate) > 0) {
					expired = true;
			}
		String role = (String)session.getAttribute("LOGIN_USER");
			
		if(role.equalsIgnoreCase("guest") ){//&& interactiveMode){
			if(expired){
				
				String remoteLink = request.getContextPath()+ "/ListOfQuestions.jsp?AssignmentID=" + asID + "&&studentId=" + studentID
						+ "&&status=" + "expired";
				response.sendRedirect(remoteLink);
				
			}else{
				TestAssignment ta = new TestAssignment();
				String args[] = {String.valueOf(asID), String.valueOf(questionID.trim()), studentID,courseID, correctquery,role};
				
				FailedDataSetValues failedDSValue;
				failedDSValue = ta.evaluateGuestAnswer(dbcon, testConn, args);
			    String status = failedDSValue.getStatus();
			    if(interactiveMode){ 
					session.setAttribute("dbConn", dbcon);
					session.setAttribute("testConn", testConn);	
					session.setAttribute("displayTestCase", true);
				} 
				else{
					session.setAttribute("displayTestCase", false);						
				}
			    
			    Float marks1 = failedDSValue.getMarks();
			    int marks = marks1.intValue();
			    Float maxMarks1 = failedDSValue.getMaxMarks();
			    int maxMarks = maxMarks1.intValue();
			    if(status.equals("Failed")){
				  
				    //forward to new student test case page to show result
				   /* String remoteLink = request.getContextPath() + "/GuestStudentTestCase?user_id=" + studentID +"&assignment_id=" + asID 
							+ "&question_id=" + questionID +"&query=" + CommonFunctions.encodeURIComponent(correctquery) + 
							"&status=" + status.toString()+"&marks="+marks;*/
							
					String remoteLink = "QuestionDetails.jsp?isGuestUser=true&&AssignmentID="+asID+"&&questionId="+questionID+"&&courseId="+courseID+"&&studentId="+studentID
   						+"&&marks="+marks+"&&maxMarks="+maxMarks+"&&status="+status+"&&query="+ CommonFunctions.encodeURIComponent(correctquery);		
							
					session.setAttribute("failedDS", failedDSValue) ;
					//System.out.println(remoteLink);
					
					if(status.equals(QueryStatus.Error) && failedDSValue.getErrorMessage() != null){
						remoteLink += "&Error=" + CommonFunctions.encodeURIComponent(failedDSValue.getErrorMessage());
					}
					
					response.sendRedirect(remoteLink);
					
			    }
			    else if(status.equals("Error")){
			    	
			    	 /* String remoteLink = request.getContextPath() + "/GuestStudentTestCase?user_id=" + studentID +"&assignment_id=" + asID 
								+ "&question_id=" + questionID +"&query=" + CommonFunctions.encodeURIComponent(correctquery) + 
								"&status=" + status.toString()+"&marks="+marks;*/
				
						String remoteLink = "QuestionDetails.jsp?isGuestUser=true&&AssignmentID="+asID+"&&questionId="+questionID+"&&courseId="+courseID+"&&studentId="+studentID
	    						+"&&marks="+marks+"&&maxMarks="+maxMarks+"&&status="+status+"&&query="+ CommonFunctions.encodeURIComponent(correctquery);
			    if(failedDSValue.getErrorMessage() != null){
			    	remoteLink += "&&Error=" + CommonFunctions.encodeURIComponent(failedDSValue.getErrorMessage());
			    }
			    	response.sendRedirect(remoteLink);
			    }
			    	else{
			    
			    	//forward to  new student test case page if status is correct
			    	/* String remoteLink = request.getContextPath() + "/GuestStudentTestCase?user_id=" + studentID +"&assignment_id=" + asID 
								+ "&question_id=" + questionID +"&query=" + CommonFunctions.encodeURIComponent(correctquery) + 
								"&status=" + status.toString()+"&marks=100.00";
			    	*/
			    	String remoteLink = "QuestionDetails.jsp?isGuestUser=true&&AssignmentID="+asID+"&&questionId="+questionID+"&&courseId="+courseID+"&&studentId="+studentID
			    						+"&&marks="+marks+"&&maxMarks="+maxMarks+"&&status="+status+"&&query="+ CommonFunctions.encodeURIComponent(correctquery);
			    	
			    	 if(status.equals(QueryStatus.Error) && failedDSValue.getErrorMessage() != null){
							remoteLink += "&&Error=" + CommonFunctions.encodeURIComponent(failedDSValue.getErrorMessage());
						}
			    	 response.sendRedirect(remoteLink);
			    }
			    
			  
			    
			}
			
		}else{
			//student	
			if(expired){
		
				String remoteLink = request.getContextPath()+ "/ListOfQuestions.jsp?AssignmentID=" + asID + "&&studentId=" + studentID
						+ "&&status=" + "expired";
				response.sendRedirect(remoteLink);
				
			}
			else{
				try {
					
				//String questionId = "A"+asID+"Q"+queryID.trim()+"S1";			
				System.out.println("questionId:::::"+questionID);
				stmt = dbcon.prepareStatement("select * from xdata_student_queries where assignment_id=? and question_id = ? and rollnum = ? and course_id=?");
				stmt.setInt(1, asID);
				stmt.setInt(2,Integer.parseInt(questionID));
				stmt.setString(3, studentID);
				stmt.setString(4,courseID);
				System.out.println(stmt.toString());
				
			//	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm");
				//Date parsedDate = dateFormat.parse(currentDate);
				Timestamp subTimeStamp = new java.sql.Timestamp(current.getTime());
				
				logStore(dbcon,courseID,asID,questionID,studentID,subTimeStamp,correctquery);
				rs = stmt.executeQuery();
				if(!rs.next()){
				
					stmt = dbcon.prepareStatement("INSERT INTO xdata_student_queries (dbid, queryid, rollnum, querystring, tajudgement,verifiedcorrect, assignment_id, question_id,course_id,submissiontime) VALUES(?,?,?,?,?,?,?,?,?,?)");
					stmt.setString(1, "dbid");/**FIXME: */
					stmt.setString(2, query_id);
					stmt.setString(3, studentID);
					stmt.setString(4, correctquery);
					stmt.setBoolean(5,true);
					stmt.setNull(6, Types.BOOLEAN);
					stmt.setInt(7,asID);
					stmt.setInt(8,Integer.parseInt(questionID));
					stmt.setString(9,courseID);
					stmt.setTimestamp(10, subTimeStamp);
				}
				else{
					stmt = dbcon.prepareStatement("UPDATE xdata_student_queries SET querystring=?,submissiontime=? WHERE assignment_id=? and question_id = ? AND rollnum=? and course_id =?");
					
					stmt.setString(1, correctquery);
					stmt.setTimestamp(2, subTimeStamp);
					stmt.setInt(3,asID);
					stmt.setInt(4, Integer.parseInt(questionID));
					stmt.setString(5, studentID);
					stmt.setString(6, courseID);
				} 
				
				stmt.executeUpdate();
				//rs.close();
				//dbcon.close();
				
				TestAssignment ta = new TestAssignment();
				String args[] = {String.valueOf(asID), String.valueOf(questionID.trim()), studentID,courseID};
				
				QueryStatusData status;
				if(interactiveMode){
					System.out.println(args[0]+"  "+args[1]+"   "+args[2]);
					 status = ta.evaluateQuestion(dbcon, testConn, args);
					 System.out.println("InteractiveStatus:" + status.Status.toString());
				}
				else{ 
					status = ta.testQuery(args);
				} 
				
				//System.out.println("Student Mode - UpdateSingleQuery - "+ro.kifs.diagnostic.Connection.getStillOpenedConnsStackTraces());
				//System.out.println("TestQueryOuput" + status.Status.toString());
				String remoteLink = "";
				if(interactiveMode){ 
					session.setAttribute("dbConn", dbcon);
					session.setAttribute("testConn", testConn);	
					session.setAttribute("displayTestCase", true);
				} 
				else{
					session.setAttribute("displayTestCase", false);						
				}
				
				remoteLink = request.getContextPath() + "/StudentTestCase?user_id=" + studentID +"&assignment_id=" + asID 
						+ "&question_id=" + questionID +"&query=" + CommonFunctions.encodeURIComponent(correctquery) + "&status=" + status.Status.toString();
				
				if(status.Status == QueryStatus.Error){
					remoteLink += "&Error=" + CommonFunctions.encodeURIComponent(status.ErrorMessage);
				}
				
				response.sendRedirect(remoteLink);
				}
				catch (SQLException sep) {
					
					sep.printStackTrace();
					throw new ServletException(sep);
					//System.exit(1); 
				}finally{
					rs.close();
					stmt.close();
					
					 
				}
						
			}
		}
		}
		finally{
			if(dbcon != null)
				dbcon.close();
			if(testConn != null)
				testConn.close();
		}
	%>
</body>
</html>