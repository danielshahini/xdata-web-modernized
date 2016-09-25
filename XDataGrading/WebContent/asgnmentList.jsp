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
 <style>
.zeroclipboard-is-active{
	color: #fff;
}
</style>
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
//alert(getParameterByName("showQuestions"));
function chkOnload(){
if(getParameterByName("showQuestions") != "" && getParameterByName("showQuestions") == "true"){
	var href="ListOfQuestions.jsp?AssignmentID="+getParameterByName("assignmentId");
	
	//$('#viewQuestions').trigger('click');
	//alert("Comes to show on history: ");
	
	$("#loadPage").load(href);
	$("#loadPage").show(); 
	
	$('html,body').animate({ scrollTop: $("#loadPage").offset().top-10});
}
}
$( document ).ready(function() {

	var client = new ZeroClipboard( $("button#my-button") );
	
	
	$('#viewQuestions').click(function(e){
		
		e.preventDefault(); 
		$("#loadPage").show();
		$("#loadPage").load(this.href);
		//$("#loadPage").focus();
		 $('html,body').animate({ scrollTop: $("#loadPage").offset().top-10});
	});
});

</script>
<style>
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
<body onload="chkOnload();">
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}

if(session.getAttribute("ltiIntegration")!= null && ! Boolean.parseBoolean(session.getAttribute("ltiIntegration").toString())){%> 
<div id="breadcrumbs">
  <a style='color:#353275;text-decoration: none;' href="CourseHome.jsp" target="_top">Home</a> &nbsp; >> &nbsp;
  <a href="InstructorHome.jsp?contextLabel=<%=(String) request.getSession().getAttribute("context_label")%>" style='color:#353275;text-decoration: none;' target="_top"><%=(String) request.getSession().getAttribute("context_label")%></a>&nbsp; >> &nbsp;
   <a href="ListAllAssignments.jsp" style='color:#353275;text-decoration: none;' target="_self">Assignment List</a>&nbsp; >> &nbsp;
   <a href="#" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>Assignment Options</a>
  </div>
<%} %> 
<br/>
	<div>
		<div class="fieldset">
			<fieldset>
				<legend> Assignment Info</legend>
					<%
						String courseID = (String) request.getSession().getAttribute("context_label");
						int assignID = Integer.parseInt(request.getParameter("assignmentId"));
						//LtiProperties ltiData = new LtiProperties();
						String outcomeURL = (String)session.getAttribute("lis_outcome_service_url");
						String consumerKey = "";
						String secretKey = "";
						Connection dbcon = (new DatabaseConnection()).dbConnection();

				 		PreparedStatement ltiStmt = dbcon
				 									.prepareStatement("SELECT * FROM xdata_lti_credentials where requesting_url=?");	
				 		ltiStmt.setString(1, outcomeURL);
				 		ResultSet rs_lti = ltiStmt.executeQuery(); 
				 		if(rs_lti.next()){
				 			consumerKey=rs_lti.getString("consumer_key");
				 			secretKey = rs_lti.getString("secret_key");
				 		}
				 		
					    // Link to be used for LTI Integration
					    // Note: Link with query parameters will not work with older moodle versions: https://tracker.moodle.org/browse/MDL-39090 
					    String url = request.getScheme() +"://"+request.getServerName() + ":"+ request.getServerPort() 
					    				+ request.getContextPath() + "/tool.jsp?assignmentId=" + assignID;					   					
						String instructions = (new CommonFunctions()).getAssignmentInstructions(courseID, assignID, url);						
						
						instructions += "<input name=\"Edit\" type=\"button\" value=\"Edit Details\" onclick=\"window.location.href='EditAssignment.jsp?AssignmentID="+assignID+"'\">&nbsp;&nbsp;";
						
						instructions += "<button id='my-button' onclick='alert(\" The following url is copied to the clipboard. \\n\\n "+url+ " \\n\\nPlease note the details below for connecting to XData from external learning system. \\n\\n \\t\\t Consumer Key : "+consumerKey+" \\n \\t\\t Secret Key : "+secretKey+"  \\n \");'  data-clipboard-text='"+ url+ "' title='Click to copy to clipboard.'>Copy external tool link</button>"; 
						out.println(instructions);
					%>
			</fieldset> 
			<br/>
			<div>
				<div class="fieldset">
					<fieldset>
						<legend> Assignment Options</legend>
						<%		String output = "";
								//int assignID;
								boolean start = false;

								ArrayList<String> listOfIDs = new ArrayList<String>();
								ArrayList<String> endTimes = new ArrayList<String>();

								SimpleDateFormat formatter = new SimpleDateFormat(
										"yyyy-MM-dd HH:mm:ss");
								formatter.setLenient(false);
								//String starting=formatter.format(start);

								try {
									PreparedStatement stmt;
									stmt = dbcon
											.prepareStatement("SELECT * FROM xdata_assignment where assignment_id = ? and course_id = ?");
									stmt.setInt(1, assignID);
									stmt.setString(2, courseID);

									ResultSet rs;
									rs = stmt.executeQuery();
									String endTime = "";
									boolean noASsign = false;
									if (rs.next()) {
										endTime = formatter.format(rs.getTimestamp("endtime"));
									} else {
										noASsign = true;
									}
									//stmt.close();
									//rs.close();
									
									if(!noASsign){
									
										//get current date
										Calendar c = Calendar.getInstance();
				
										String currentDate = formatter.format(c.getTime());
										java.util.Date current = formatter.parse(currentDate);
										//compare times
				
										java.util.Date oldDate = formatter.parse(endTime);
				
										//now check whether current time is more than end time.Then only assignment can be graded
										if (oldDate.compareTo(current) < 0) {
											start = true;
										}
				 
										output += "<ul>"
												//+ "<li><a href=\"EditAssignment.jsp?AssignmentID="
												//+ assignID
												//+ "\" target = \"rightPage\"><span > Edit Assignment </span>  </a></li>"
												+ "<li><a id='viewQuestions' href=\"ListOfQuestions.jsp?AssignmentID="
												+ Integer.parseInt(request.getParameter("assignmentId"))
												+ "\" target = \"rightPage\"><span > View/Edit Questions </span> </a>"
												//+ "</a> </li> <li><a href=\"gradeAssignment.jsp?AssignmentID="
											//	+ assignID
												//+ "\" target = \"rightPage\"><span >Grade Questions</span> </a> "
												//+ "</a> </li>" 
												+ "<li><a href=\"ViewResults.jsp?AssignmentID="
												+ assignID
												+ "\" target = \"rightPage\"><span >Result Summary By Question</span> </a> </li>"
												+ "<li>"
												//+"<a href=\"AssignmentScores?AssignmentID="+assignID
												//+ "\" target = \"rightPage\">
												+"<a href=\"assignmentScores.jsp?AssignmentID="+assignID
												+ "\" target = \"rightPage\">"
												+"<span > View Scores </span> "
												+ "</a> </li></ul>";
				 
										start = false;
										
										out.println(output);
										//out.println("<input type=\"button\" id=\"home\" value=\"Home\" onClick=\"document.location.href='instructorOptions.html'\"><br>");
									}
									
									else {
										out.println(" <h3>There are no assignments</h3>");
									}					
								
						%>
					</fieldset>
				</div>	</div>
				<div>
				<div class="fieldset">
					<fieldset>
						<legend> Assignment Description</legend>
						<%
						out.println("<label>"+rs.getString("description")+"</label>");
								} 
								catch (Exception err) {
									err.printStackTrace();
									throw new ServletException(err);
								}
								finally{
									dbcon.close();
								}
						%>
			</div>
			<div id="loadPage" style='display:none;'></div>
			
</body>
</html>
