<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="erroPage.html"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page import="java.io.*"%> 
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@ page import="java.text.*"%>
<%@page import="database.*"%>
<%
	
    int index = 1;
    int newQueryID = 1;
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>  
<script type="text/javascript" src = "scripts/jquery.js"></script>
<script type="text/javascript" src = "scripts/jquery-2.1.4.js"></script>
<script  type="text/javascript">
/*window.onload = function() { 
	   
	  var mime = 'text/x-mariadb';
	  // get mime type
	  if (window.location.href.indexOf('mime=') > -1) {
	    mime = window.location.href.substr(window.location.href.indexOf('mime=') + 5);
	  }
	  //Initialize code mirro text area
	 $('.textForSQL').each(function(index) {		
         $(this).attr('id', 'textarea-' + index);  
    
        window.editor =  CodeMirror.fromTextArea(document.getElementById('textarea-' + index), {
       	mode: mime,
     	    indentWithTabs: true,
     	    smartIndent: true,
     	    matchBrackets : false, 
     	    lineWrapping: true,
     	    autofocus: true,
     	  extraKeys: {"Ctrl-Space": "autocomplete"},
 	  	  hintOptions: {tables: {
 	      users: {name: null, score: null, birthDate: null},
 	      countries: {name: null, population: null, size: null}
 	    }}
          } ); 
        editor.on("blur", function() {editor.save();});
     });  
	 CodeMirror.commands.autocomplete = function(cm) {};
};*/

/*$(document).ready(function(){
	
	 var correctId=parseInt(this.id);
 	 var txtBoxId = "query " +
 	 
 	 
 var mime = 'text/x-mariadb';
  if (window.location.href.indexOf('mime=') > -1) {
		mime = window.location.href.substr(window.location.href.indexOf('mime=') + 5);
 }
 window.editor = CodeMirror.fromTextArea( document.getElementById(txtBoxId) , {
	mode: mime,
	    indentWithTabs: true,
	    smartIndent: true,
	    matchBrackets : true, 
	    lineWrapping: true,
	    autofocus: true,
	    extraKeys: {"Ctrl-Space": "autocomplete"},
hintOptions: {tables: {
     users: {name: null, score: null, birthDate: null},
     countries: {name: null, population: null, size: null}
   }}
 }); 
CodeMirror.commands.autocomplete = function(cm) {};
editor.on("blur", function() {editor.save();});         


}); */
</script>
<style>

.fieldset div input{
	width: 70px;
	height: 20px;
}
textarea{
	width: 100%;
	height: 100px;
}
</style>
</head>
<body>

<%

//Get assignment_questions from xdata_instructor_queries table

//show the questions with codemirror text box for writing answers and final submit button to save.
String user_name = request.getParameter("user_name");
String rollnum = request.getParameter("rollnum");
String course_id = request.getParameter("course_id");
String assignment_id = request.getParameter("assignment_id");
String email = request.getParameter("email");

String loginUsr= (String)session.getAttribute("LOGIN_USER");
//int assignID = Integer.parseInt(request.getParameter("AssignmentID"));
String courseID = (String) request.getSession().getAttribute(
		"context_label");
boolean isInteractiveAssignment = false;
int qIndexDisplay = 1;
%>

<div>
		<form class="lateSubmission" name=""LateSubmission""
			action="LateSubmission" method="post"
			style="border: none"> 
	
		<div class="fieldset">
				<fieldset>
					<legend>Student Details</legend><br>
					<div>
						<label><b>Course Id:</b></label><label><%=course_id %></label><br>
						<label><b>Assignment Id:</b></label><label><%=assignment_id %></label><br>
						<label><b>Student Name:</b></label><label><%=user_name %></label><br>
						<label><b>XData Id:</b></label><label><%=rollnum%></label><br>
						<label><b>Email:</b></label><label><%=email%></label><br>
						
					</div><br>
		</fieldset>
		<fieldset>
		
					<legend>List Of Questions</legend><br>
			<div>
						<span>Percentage of Marks to be reduced for late submission : </span>
						<input placeholder="Marks to be reduced"
							name="marksToBeReduced" value = '0' required/> 
					</div><br>
							
			<%Connection dbcon = (new DatabaseConnection()).dbConnection();		
			//Get list of question texts
			try {
								
								PreparedStatement stmt;
								stmt = dbcon
										.prepareStatement("SELECT * FROM xdata_qinfo  where assignment_id=? and course_id=? order by question_id");
								stmt.setInt(1, Integer.parseInt(assignment_id));
								stmt.setString(2, course_id);
								ResultSet rs = stmt.executeQuery();
									
								while (rs.next()) {
									qIndexDisplay++;
									int queryIndex=1;
									int qID = rs.getInt("question_id");
									String desc = rs.getString("querytext");
									%>
				<div class="question"><span>Question Id:<%= qID %>. </span><%= desc %></div>
				<div class="answer"> 	
								
								<input type ="hidden" name="assignment_id" id="assgnID" value=<%=assignment_id %> >
								<input type ="hidden" name="courseId" id="courseId" value=<%=course_id %> >
									<input type ="hidden" name="username" id="username" value=<%=user_name %> >
									<input type ="hidden" name="rollnum" id="rollnum" value=<%=rollnum %> >
									<input type ="hidden" name="email" id="email" value=<%=email %> >
												
					<textarea name='newQuery<%=qID%>' class="textForSQL" id='query<%=qID%>'%></textarea></div>
					<script type ="text/javascript">
					  var mime = 'text/x-mariadb';
					  // get mime type
					  if (window.location.href.indexOf('mime=') > -1) {
					    mime = window.location.href.substr(window.location.href.indexOf('mime=') + 5);
					  }
					  //Initialize code mirro text area
					 $('.textForSQL').each(function(<%=qID%>) {		
				         $(this).attr('id', 'textarea-' + <%=qID%>);  
				    
				        window.editor =  CodeMirror.fromTextArea(document.getElementById('textarea-' + <%=qID%>), {
				       	mode: mime,
				     	    indentWithTabs: true,
				     	    smartIndent: true,
				     	    matchBrackets : false, 
				     	    lineWrapping: true,
				     	    autofocus: true,
				     	  extraKeys: {"Ctrl-Space": "autocomplete"},
				 	  	  hintOptions: {tables: {
				 	      users: {name: null, score: null, birthDate: null},
				 	      countries: {name: null, population: null, size: null}
				 	    }}
				          } ); 
				        editor.on("blur", function() {editor.save();});
				     });  
					 CodeMirror.commands.autocomplete = function(cm) {};
					 
					</script>
					
								<%
								}
				%>			
					<input type ="hidden" name="noOfQuestions" id="noOfQuestions" value=<%=qIndexDisplay %> >	
				<input class="button" name="LateSubmission" type="submit" id="LateSubmission" value="Save and Evaluate"></div>
				<%				
			}catch(Exception e){
				dbcon.close();
			}
		%>	
		</fieldset>
		
		</div>
		</form>
		</div>			
</body>
</html>