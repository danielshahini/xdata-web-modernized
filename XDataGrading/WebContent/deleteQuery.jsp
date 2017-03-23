<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
    <%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}else if(session.getAttribute("LOGIN_USER") != null && !session.getAttribute("LOGIN_USER").equals("ADMIN")
 		&& session.getAttribute("role") != null &&  (!session.getAttribute("role").equals("instructor") || !session.getAttribute("role").equals("tester"))){
	response.sendRedirect("index.jsp?NotAuthorised=true");
	session.invalidate();
	return;
}


String loginUsr="";

int queryIdStr = Integer.parseInt(request.getParameter("queryIdToDelete")); 
//System.out.println("------queryIdStr-----------" + queryIdStr);
int assignID =  Integer.parseInt(request.getParameter("assignment_id"));
String courseID = (String) request.getSession().getAttribute(
		"context_label");
int questionId =  Integer.parseInt(request.getParameter("question_id"));
Hashtable <Integer,String> existingSQL = (Hashtable)session.getAttribute("existingSQL");
ArrayList dataSetGeneratedList = (ArrayList)session.getAttribute("DataGenerationCompleted");
Connection dbcon = null;
 
try{		
	dbcon = (new DatabaseConnection()).dbConnection();
	PreparedStatement stmt; 
	stmt = dbcon 
			.prepareStatement("delete from xdata_instructor_query where course_id=? and assignment_id=? and question_id=? and query_id=?");
	 
	stmt.setString(1, courseID);
	stmt.setInt(2, assignID);
	stmt.setInt(3, questionId);
	stmt.setInt(4,queryIdStr);
	stmt.executeUpdate();
	
	stmt = dbcon
			.prepareStatement("delete from xdata_qinfo where assignment_id=? and question_id=? and query_id=? and course_id = ?");

	stmt.setInt(1, assignID);
	stmt.setInt(2, questionId);
	stmt.setInt(3,queryIdStr);
	stmt.setString(4,courseID);
	stmt.executeUpdate();
	
	stmt = dbcon 
			.prepareStatement("delete from xdata_datasetvalue where assignment_id=? and question_id=? and query_id=? and course_id = ?");
	  
	stmt.setInt(1, assignID);
	stmt.setInt(2, questionId);
	stmt.setInt(3,queryIdStr);
	stmt.setString(4,courseID);
	stmt.executeUpdate();
	
	dbcon.close();
	
	existingSQL.remove(queryIdStr);
	session.setAttribute("existingSQL",existingSQL); 
	String keyId = loginUsr+"&"+assignID+"&"+questionId+"&"+queryIdStr;
	if(dataSetGeneratedList   != null && dataSetGeneratedList.size() > 0){
		dataSetGeneratedList.remove(keyId);
		session.setAttribute("DataGenerationCompleted",dataSetGeneratedList);
	}
	String link = "QuestionDetails.jsp?AssignmentID="+assignID+"&questionId="+questionId;
	if(((String)session.getAttribute("LOGIN_USER")).equalsIgnoreCase("tester")){
		//link = "TesterQuestionDetails.jsp?AssignmentID="+assignID+"&questionId="+questionId; 
		%>
<jsp:forward page="TesterQuestionDetails.jsp" > 
<jsp:param name="AssignmentID" value="<%=assignID %>" />
<jsp:param name="questionId" value="<%=questionId %>" />
</jsp:forward> 
		
	<%}else{
	//response.sendRedirect(link);
	%>
	
	<jsp:forward page="QuestionDetails.jsp"> 
<jsp:param name="AssignmentID" value="<%=assignID %>" />
<jsp:param name="questionId" value="<%=questionId %>" />
</jsp:forward> 

	<%}
	
	} catch(Exception e){
		e.printStackTrace();
		throw new ServletException();
	}
%>
</body>
</html>