<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@ page import="java.text.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.CommonFunctions"%>
<%@page import="database.DatabaseProperties"%> 

<html> 
<head>
<link rel="stylesheet" href="../css/structure.css" type="text/css"/>
<link rel="stylesheet" href="../scripts/codemirror/lib/codemirror.css" />

<link rel="stylesheet" href="../scripts/codemirror/addon/hint/show-hint.css" />
<script src="../scripts/codemirror/addon/hint/show-hint.js"></script>
<script src="../scripts/codemirror/addon/hint/sql-hint.js"></script>
<script type="text/javascript" src = "../scripts/jquery.js"></script>
<script type="text/javascript" src = "../scripts/wufoo.js"></script>
<script type="text/javascript" src = "../scripts/jquery-ui-min.js"></script>
<script src="../scripts/codemirror/lib/codemirror.js"></script>
<script src="../scripts/codemirror/mode/sql/sql.js"></script>
 
<link rel="stylesheet" href="../highlight/styles/xcode.css"> 
<link rel="stylesheet" href="../highlight/styles/default.css">
<script src="../highlight/highlight.pack.js"></script>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Details of the Question</title>

<style> 
table {
	border: 0px;
	border-collapse: collapse;
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

textarea,select {
	font: 12px/12px Arial, Helvetica, sans-serif;
	padding: 0;
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


label {
	font-size: 15px;
	font-weight: bold;
	color: #666;
}

label span,.required {
	color: red;
	font-weight: bold;
	font-size: 17px;
}


.stop-scrolling {
	height: 100%;
	/*overflow: hidden;*/
}
</style>
<script type="text/javascript" src="../scripts/ManageQuery.js"></script>

<script>
hljs.initHighlightingOnLoad();

function getParameterByName(name) { 		
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
} 


$(document).ready(function(e) {
    var $input = $('#refresh');

    $input.val() == 'yes' ? location.reload(true) : $input.val('yes');
    
    window.onload = function() {
  	  var mime = 'text/x-mariadb';
  	  // get mime type
  	  if (window.location.href.indexOf('mime=') > -1) {
  	    mime = window.location.href.substr(window.location.href.indexOf('mime=') + 5);
  	  }
  	  window.editor = CodeMirror.fromTextArea(document.getElementById('query<%=(String) request.getParameter("questionId")%>'), {
  	    mode: mime,
  	    indentWithTabs: true,
  	    smartIndent: true,
  	    lineNumbers: true,
  	    matchBrackets : false,
  	    lineWrapping: true,
  	    autofocus: true,
  	    extraKeys: {"Ctrl-Space": "autocomplete"}, 
  	    hintOptions: {tables: {
  	      users: {name: null, score: null, birthDate: null},
  	      countries: {name: null, population: null, size: null}
  	    }}
  	  });
  	  CodeMirror.commands.autocomplete = function(cm) {
  	  }
  	};
  	
  	
   //alert("Comes to onload");
   var user = '<%=session.getAttribute("LOGIN_USER")%>';
   var peramQuery = getParameterByName("query");

  
    if(user == 'guest'){
    	//alert("user == "+user);
    	//alert("asgn Id = "+getParameterByName("AssignmentID "));
	    var dataString = "user_id=" + getParameterByName("studentID")
	    		+"&&assignment_id=" + getParameterByName("AssignmentID") 
			+ "&&question_id=" + getParameterByName("questionId")
			+"&&query=" +encodeURI(peramQuery) + 
			"&&status=" + getParameterByName("status")
			+"&&marks="+getParameterByName("marks")
			+"&&maxMarks="+getParameterByName("maxMarks")
			+"&&Error="+getParameterByName("Error");
			
	    //alert("DataString = "+dataString);
	  	var questionID = "query"+getParameterByName("questionId");
	  	//alert("questionID == "+ questionID);
	    
	    var urlt = "../GuestStudentTestCase";
	 //  alert("URL = "+ urlt);
		if(getParameterByName("isGuestUser")){
			
		 $.ajax({ 
			        type: "GET",
			        url: urlt,
			        data: dataString,
			        context:$(this),        
			        success: function(data) {
			        	//alert("Comes to success");
			        	$('#showGuestUserDetails').show();
			        	$('#showGuestUserDetails').html(data);
			        	 $('html,body').animate({ scrollTop: $("#showGuestUserDetails").offset().top-10});
			        	 editor.setValue(peramQuery);
			        	
			        }
			});
			
		}else{
			$('#showGuestUserDetails').hide();
		}
    }
	
});

	
	
	
	function report(btn, selected) {
		if(window.editor != undefined){
			document.getElementById('query<%=(String) request.getParameter("questionId")%>').innerHTML = window.editor.getValue();
		}
		var sid = btn.name.split(" ")[1];
		var asID = btn.name.split(" ")[2];
		var qid = btn.name.split(" ")[3];
		var quer = "query".concat(qid);
		var que = document.getElementById(quer).value;
		if (selected == "1") {
			
			var out = "UpdateSingleQuery.jsp?assignmentId=" + encodeURIComponent(asID)
					+ "&questionId=" + encodeURIComponent(qid) + "&query=" + encodeURIComponent(que) + "&studentId="
					+ encodeURIComponent(sid);
			window.location.href = out;
		} else if (selected == 2) {
			alert("To be done support for grading");
		} else if (selected == 3) {
			var out = "ListOfQuestions.jsp?AssignmentID=" + encodeURIComponent(asID)
					+ "&&studentId=" + encodeURIComponent(sid);
			window.location.href = out;
		}
	}
</script>
</head>
<body > 
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("../index.jsp?TimeOut=true");
	return;
}

if(! Boolean.parseBoolean(session.getAttribute("ltiIntegration").toString())){%>
<div id="breadcrumbs">
  <a id="bcrumb_link" style='color:#353275;text-decoration: none;' href="../CourseHome.jsp" target="_top">Home</a> &nbsp; >> &nbsp;
   <a  id="bcrumb_link" style='color:#353275;text-decoration: none;' href="ListAllAssignments.jsp" target="_self">Assignment List</a>&nbsp; >> &nbsp;
   	<a  id="bcrumb_link" style='color:#353275;text-decoration: none;' href="ListOfQuestions.jsp?assignmentid=<%=request.getParameter("AssignmentID") %>" target="_self">Question List</a>&nbsp; >> &nbsp;
   <a style='color:#0E0E0E;text-decoration: none;font-weight: normal;' id="bcrumb_no_link" href="#">Answer</a>
  </div> 
<%}else{ %>
   <a  id="bcrumb_link" href="ListOfQuestions.jsp?assignmentid=<%=request.getParameter("AssignmentID") %>" target="_self">Question List</a>&nbsp; >> &nbsp;
   <a id="bcrumb_no_link" href="#">Answer</a>
<%} %> 
	<div>
		<br />
		<div class="fieldset">
					<fieldset>
				<!-- <legend> Assignment Details</legend> -->
				
				<%
					int assignID = Integer.parseInt(request.getParameter("AssignmentID"));
						String questionId = (String) request.getParameter("questionId")
								.trim();
						String courseID = (String) request.getParameter("courseId").trim();
						String studentId = (String) request.getParameter("studentId")
								.trim();
						String user_id = (String) request.getSession().getAttribute(
								"user_id");
						String user = (String)session.getAttribute("LOGIN_USER");
								String instructions = (new CommonFunctions()).getStudentAssignmentInstructions(courseID, assignID,user);
								out.println(instructions);
				%>
			</fieldset><br /> 
			<fieldset> 
				<legend> Assignment Instructions</legend>
				<ul>
					<li>Click submit after entering the answer to see the result and mark details</li>
				</ul>
				<p></p>
				<p></p>
			</fieldset>
			<br/>
			<fieldset>
				<legend> Question Details</legend>
<input type="hidden" id="refresh" value="no">
				<%
				

							String output = "";

							String listButton = "<input type=\"button\" name=\"button "
									+ studentId
									+ " "
									+ assignID
									+ " "
									+ questionId
									+ "\" "
									+ " onClick=\"report(this,3)\""
									+ " value=\"List Of Questions\" style=\"float:right;\"><br/>\n";

							output += listButton
									+ "<table  cellspacing=\"10\"  class=\"authors-list\" id=\"queryTable\" align=\"center\"> <tr> <th >Question ID</th>       <th >Question Text</th>  <th >Correct Query</th> <th> </th></tr>";
							//get query details
							Connection dbcon = (new DatabaseConnection()).dbConnection();
							try {
								PreparedStatement stmt;
								//stmt = dbcon.prepareStatement("SELECT * FROM  qinfo ,assignment where qinfo.assignment_id=? AND qinfo.assignment_id=assignment.assignment_id");
	 							stmt = dbcon
										.prepareStatement("SELECT * FROM xdata_qinfo  where xdata_qinfo.assignment_id=? and xdata_qinfo.course_id=? and xdata_qinfo.question_id=?");
								stmt.setInt(1, assignID);
								stmt.setString(2, (String) request.getSession().getAttribute("context_label"));
								stmt.setInt(3, Integer.parseInt(request.getParameter("questionId").trim()));
								System.out.println("QId :"	+ (String) request.getParameter("questionId"));
								System.out.println("Course Id: " + (String) request.getSession().getAttribute("context_label"));
								System.out.println("AssId :" + assignID);

								ResultSet rs = stmt.executeQuery();
								String qID = (String) request.getParameter("questionId");
								while (rs.next()) {

									String description = rs.getString("querytext");

									/**check if student can edit his assignment*/
									stmt = dbcon
											.prepareStatement("SELECT * FROM xdata_assignment where assignment_id=? and course_id=?");
									stmt.setInt(1, assignID);
									stmt.setString(2, courseID.trim());
									ResultSet rs2 = stmt.executeQuery();
									Timestamp start = null, end = null;
									if (rs2.next()) {
										start = rs2.getTimestamp("starttime");
										end = rs2.getTimestamp("endtime");
									}

									boolean readOnly = false;
									SimpleDateFormat formatter = new SimpleDateFormat(
											"yyyy-MM-dd HH:mm:ss");
									formatter.setLenient(false);
									String ending = formatter.format(end);
									String starting = formatter.format(start);
									//String oldTime = "2012-07-11 10:55:21";
									java.util.Date oldDate = formatter.parse(ending);
									//get current date
									Calendar c = Calendar.getInstance();

									String currentDate = formatter.format(c.getTime());
									java.util.Date current = formatter.parse(currentDate);

									//compare times
									if (oldDate.compareTo(current) < 0)
										readOnly = true;

									/**get student answers*/
									
									try {
										stmt = dbcon
												.prepareStatement("SELECT * FROM xdata_student_queries  where assignment_id= ? and question_id=? and rollnum=?");
										
										String queryId = "A"+assignID+"Q"+questionId.trim()+"S1";
										
										stmt.setInt(1, assignID);
										stmt.setInt(2,Integer.parseInt(questionId));
										stmt.setString(3, studentId);
										ResultSet rs1 = stmt.executeQuery();
										String studentAnswer = "";
										while(rs1.next()) 
											studentAnswer = rs1.getString("querystring");
										if (readOnly) {
				%>
									<div class="questionelement">
										<div class="question"><span>Q<%= qID %>. </span><%= description %></div>
										<div class="answer"><label name='query' id='query<%=qID %>'><%=studentAnswer %></label></div>
										<div class="editbutton">
										<input type="button" onClick="report(this,2)"  value="View Grades" name="button <%=studentId+" "+assignID+" "+qID%>"/>
										</div>
									</div>
								<% } else {%>
									<div class="questionelement">
										<div class="question"><span>Q<%= qID %>. </span><%= description %></div>
										<div class="answer"><textarea name='query' id='query<%=qID %>'><%=studentAnswer %></textarea></div>
										<div class="editbutton">
										<br/>
										<input type="button" onClick="report(this,1)"  value="Submit Answer" name="button <%=studentId+" "+assignID+" "+qID%>"/>
										<p></p>
										</div>
										<br/>
									</div>
								<%
										}
								rs1.close();
								} catch (Exception err) {
									err.printStackTrace();
									throw new ServletException(err);
								}
									
							rs2.close();
						}							
						rs.close();
						stmt.close();
					}
					catch (Exception err) {
						err.printStackTrace();
						throw new ServletException(err);
					}
					finally{
						if(dbcon != null)
							dbcon.close();
					}
							
				%>
			</fieldset>
		</div>
	</div>
<div id="showGuestUserDetails" style="display:none;">
<label>Show the details</label>
</div>
</body>
</html>