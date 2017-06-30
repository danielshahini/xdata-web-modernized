
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>
<%@page import="java.sql.*"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="database.*"%>
<%@page import="partialMarking.*" %>
<%@page import = "com.google.gson.Gson" %>
<%@page import = "com.google.gson.internal.*" %>
<%@page import="partialMarking.MarkInfo"%>
<%@page import="com.google.gson.JsonParser"%>
<%@page import="com.google.gson.JsonObject"%>
<%!
	public String listToString(List<String> list1, List<String> list2){
		String ret = "<ul>";
		for(String s:list1){
			if(list2.contains(s)){
				ret += "<li>" + s + "</li>";
			}else{
				ret += "<li style='color:red;'>" + s + "</li>";
			}		 
		}
		ret += "</ul>";
		return ret;
	}

    public float roundToDecimal(float marks){
	return BigDecimal.valueOf(marks).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>  
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta http-equiv="X-UA-Compatible" content="IE=edge"/>	
<link rel="stylesheet" href="css/structure.css" type="text/css"/>

<script type="text/javascript" src = "scripts/jquery.js"></script>
<script src="scripts/rangeslider/rangeslider.min.js"></script>
<link rel="stylesheet" href="scripts/rangeslider/rangeslider.css">
<link rel="stylesheet" href="css/structure.css" type="text/css"/>
<link rel="stylesheet" href="highlight/styles/xcode.css"> 
<link rel="stylesheet" href="highlight/styles/default.css">
<link rel="stylesheet" href="highlight/styles/default.css">
<!-- <link rel="stylesheet" href="scripts/jquery.jsonbrowser.css" type="text/css"/> -->
<script src="//code.jquery.com/jquery-1.12.0.min.js"></script>
<!--  <script src="scripts/jquery.jsonbrowser.js" type="text/javascript"></script> --> 

<script src="scripts/xml2json.js"></script>
  
<script src="highlight/highlight.pack.js"></script>
<script type="text/javascript">
hljs.initHighlightingOnLoad();

$().ready(function(){
	$("#newMarks").hide();
	$(".updateMarks").click(function() {
	    if($(this).is(":checked")) {
	    	$("#newMarks").show();
	    } else {
	    	$("#newMarks").hide();
	    }
	});
	
	  $(document).keydown(function(e) {        
		    if (e.keyCode == 27) {
		        window.close();
		    }
		});
	  
	$('#update').click(function(event) {  
        var username=$('#userId').text();
        var assignId = $('#assignId').text();
        var quesId = $('#questionId').text();
        var updatedMarks = $('#marks').val();
     	$.get('UpdateMarks', {userId:username, assignmentId:assignId, questionId:quesId, marks:updatedMarks},function(responseText) { 
     		if (responseText.trim()) {
     			alert("Update Failed:" + responseText);    
     		} else {
     			$('#queryMarks').text(updatedMarks);
     		}     		
        });
    }); 
	$('#showSteps').click(function(event){ 
		
		$('#canonicalization').toggle();
		
		});
});
</script> 

<title>Partial Mark Details</title>
<style> 
.web_dialog_overlay
 {
     position: fixed;
     top: 0;
     right: 0;
     bottom: 0;
     left: 0;
     height: 100%;
     width: 100%;
     margin: 0;
     padding: 0;
     background: #000000;
     opacity: .15;
     filter: alpha(opacity=15);
     -moz-opacity: .15;
     z-index: 101;
     display: none;
}
 
.web_dialog
{
    display: none;
    position: fixed;
    width: 500px;
    height: 500px;
    top: 50%;
    left: 50%;
    margin-left: -190px;
    margin-top: -100px;
    background-color: #ffffff;
    border: 2px solid #ffffff;
    padding: 0px;
    z-index: 102;
    font-family: Verdana;
    font-size: 10pt;
}

.web_dialog_title
{
    border-bottom: solid 2px #3B5998;
    background-color: #3B5998;
    padding: 4px;
    color: White;
    font-weight:bold;
}

.web_dialog_title a
{
    color: White;
    text-decoration: none;
}

.align_right
{
    text-align: right;
}

.web_dialog table
{
  border: 0;
}
.web_dialog th
{
  border: 0;
}
.web_dialog td
{
  border: 0;
}

label {
	font-size: 15px;
	font-weight: bold; 
	color: #666;
} 

fieldset.action {
	background: #9da2a6;
	border-color: #e5e5e5 #797c80 #797c80 #e5e5e5;
	margin-top: -20px;
}
legend {
	background: -webkit-linear-gradient(top, #657B9E, #4E5663);
	background: -moz-linear-gradient(top, #657B9E, #4E5663);
	color: #fff;
	font: 17px/21px Calibri, Arial, Helvetica, sans-serif;
	padding: 0 10px;
	margin: 0 0 0 -11px;
	/* font-weight: bold; */
	border: 1px solid #fff;
	border-color: #3D5783 #3D5783 #3D5783 #3D5783;
	text-align: left;
}
fieldset{
width:auto;

}
label
span,.required {
	color: red;
	font-weight: bold;
	font-size: 17px;
}

font{
font: 15px/16px Arial, Helvetica, sans-serif;
padding: 0;
}

.topDiv{
	float: left; 
	width: 400px;
	padding-top: 15px;
}
 /*.queryTable{
  border: 0px;
  
}

tr:nth-of-type(odd) {
      background-color:#ccc;
    }
 tr:nth-of-type(even) {
  background-color:#fff;
}*/

.querypartialmark{
	  margin: 0;
  	  padding: 0;
  	  width:auto;
      margin-bottom: 20px; 
}

.querypartialmark ul{
	  margin: 0;
  	  padding: 0;
      padding-left: 20px;
}

.querypartialmark ul li{
      padding-bottom: 5px;
      list-style: disc;
}

.querypartialmark .number{
	text-align: right;
}

.querypartialmark h4{
	margin: 5px;
}

.emph{
	font-weight: bold;
	widht:20%;
}

</style> 
</head>  
<body onLoad='showTable()'>
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}else if(session.getAttribute("LOGIN_USER") != null && !session.getAttribute("LOGIN_USER").equals("ADMIN")
 		&& session.getAttribute("role") != null && !session.getAttribute("role").equals("instructor")){
	response.sendRedirect("index.jsp?NotAuthorised=true");
	session.invalidate();
	return;
}

%>
	<div>
		<div class="fieldset">
		<fieldset>
		<legend>Query Details</legend> 
  		<div align = "left">  
   		       <%
   		       int assignID = Integer.parseInt(request.getParameter("assignment_id"));
   		       int questionID = Integer.parseInt(request.getParameter("question_id"));
   		       String requestingPage = request.getParameter("reqFrom");
   		       String userId = request.getParameter("user_id");
   		       Connection conn = (new DatabaseConnection()).dbConnection();
   		       %>
   		       	<p><h4>Assignment: <label id='assignId'><%= assignID %></label></h4></p>
   		        <p><h4>Question: <label id='questionId'><%= questionID %></label></h4></p>
   		        <p><h4>Roll Number: <label id='userId'><%= userId %></label></h4></p>
   		        	    		    	
   		       <%
  		       PreparedStatement stmt = conn.prepareStatement("select * from xdata_instructor_query where assignment_id = ? and question_id = ?");
  		       stmt.setInt(1, assignID);
		       stmt.setInt(2, questionID);
  		       ResultSet rs = stmt.executeQuery();
  		       
  		       while(rs.next()){
  		    	  %>
  		    	 <p><h4>Instructor Query: <pre><code class=\"sql\"><%= rs.getString("sql")%></code></pre></h4></p>
  		    	 <input type='hidden' id='hdnInstrQuery' value='<%= rs.getString("sql")%>'/>
  		       <%
  		       } 		       

   		       stmt = conn.prepareStatement("select * from xdata_student_queries where rollnum = ? and assignment_id = ? and question_id = ?");
   		       stmt.setString(1, userId);
   		       stmt.setInt(2, assignID);
   		       stmt.setInt(3, questionID);
   		       rs = stmt.executeQuery();
   		       
   		       if(rs.next()){
   		    	%> 
   		    		  <p><h4>Student Query: <pre><code class=\"sql\"><%= rs.getString("querystring")%></code></pre>
   		    		  </h4></p>
   		    		   <input type='hidden' id='hdnStudentQuery' value='<%= rs.getString("querystring")%>'/>
   		    		  <p><label>Do you want to change the marks?</label>
   		    		  <input class="updateMarks" type="checkbox" name="updateMarks" value="10" />
   		       			<div id = 'newMarks'><input id = 'marks' type="text" placeholder="Enter the marks"/><input type="button" value="Update" id = "update"/></div>
   		       		</p>	
   		      <% }
   		       
   		      // stmt = conn.prepareStatement("select * from score where rollnum = ? and assignment_id = ? and question_id = ?");
   		       stmt = conn.prepareStatement("select * from xdata_student_queries where rollnum = ? and assignment_id = ? and question_id = ?");
   		       stmt.setString(1, userId);
   		       stmt.setInt(2, assignID);
   		       stmt.setInt(3, questionID);
   		       rs = stmt.executeQuery();
   		       if(rs.next()){
   		    	   String data = rs.getString("markinfo");
   		    	   %>
   		    	   <p> <h4>Marks: <label id = 'queryMarks'><%= rs.getFloat("score")%></label></h4> </p>  
   		    	   <%
   		    	   if(data!= null && !data.isEmpty()){
   		    	   
   		    	   Gson gson = new Gson();
   		    	   MarkInfo marks = gson.fromJson(data, MarkInfo.class);
   		    	   System.out.println("DATA JSON structure = "+ data);
   		    	
   		    	   List<QueryInfo> queryInfo = marks.SubqueryData;
   		    	   if(queryInfo != null){  		    	   
	   		    	   Collections.sort(queryInfo, new Comparator<QueryInfo>(){
	   		    		public int compare(QueryInfo o1, QueryInfo o2) {
	   		    			 return (o1.Level - o2.Level);
	   		    		
	   		    	   }});
   		    	   }
   		       %>     		    
   		        <fieldset>
				<legend>Partial Marking Details</legend> 
		  		<div align = "left">
		  		<%
		  		String result = "";
		  		for(QueryInfo q: queryInfo)
		  		{  
		  			%>

			  		<div class="querypartialmark" style="margin-left:<%=(q.Level) *30 %>px;">
			  	<%	if(queryInfo!= null && queryInfo.size() > 1){%>
			  			<h4>Level: <%= q.Level %></h4>
					<%} %>
			  		<table class="queryTable" width="70%" cellpadding="3" cellspacing="1">
			  		<tr>
			  		<th width="20%">&nbsp;</th>
			  		<th width="20%">Student</th>
			  		<th width="20%">Instructor</th>
			  		
			  		</tr>  
			  			<%if(q.StudentPredicates!= null && q.StudentPredicates.size() > 0
			  			|| (q.InstructorPredicates != null && q.InstructorPredicates.size() > 0)){ %>
			  		<tr>
			  		<td class="emph">Predicates</td>
			  		<td width="20%"><%= listToString(q.StudentPredicates,q.InstructorPredicates)%></td>
			  		<td width="20%"><%= listToString(q.InstructorPredicates,q.StudentPredicates)%></td>
			  		
			  		</tr>
			  		<%} %>
			  		<%if(q.StudentProjections!= null && q.StudentProjections.size() > 0
			  			|| (q.InstructorProjections != null && q.InstructorProjections.size() > 0)){ %>
			  		<tr> 
			  		<td class="emph">Projections</td>
			  		<td><%= listToString(q.StudentProjections,q.InstructorProjections)%></td>
			  		<td><%= listToString(q.InstructorProjections,q.StudentProjections)%></td>
			  		</tr>
			  		<tr>
			  		
			  		<%}if(q.studentDistinct || q.instructorDistinct){ %>
			  		<tr>
			  		<td class="emph">Distinct</td>
			  		<% if(q.studentDistinct != q.instructorDistinct){%>
			  			<td  style="color: red;">
			  			<%}else{ %>
			  			<td >
			  			<%} %>
			  			<% if(q.studentDistinct){%>1<%}else { %>0<%} %> </td>
			  		
			  		
			  		<%if(q.studentDistinct != q.instructorDistinct){%>
			  			<td  style="color: red;">
			  			<%}else{ %>
			  			<td >
			  			<%} %>
			  			
			  			<%if(q.instructorDistinct) {%>1<%}else {%>0<%} %></td>
			  			</tr>
			  		
			  		<%} %> 
			  		</tr>
			  		
			  		<%if(q.StudentRelations!= null && q.StudentRelations.size() > 0
			  			|| (q.InstructorRelations != null && q.InstructorRelations.size() > 0)){ %>
			  		<tr>
			  		<td class="emph">Relations</td>
			  		<td><%= listToString(q.StudentRelations,q.InstructorRelations)%></td>
			  		<td><%= listToString(q.InstructorRelations,q.StudentRelations)%></td>
			  			</tr>
			  		<%} %>
			  		<%if(q.StudentGroupBy != null && q.StudentGroupBy.size() > 0
			  			|| (q.InstructorGroupBy != null && q.InstructorGroupBy.size() > 0)){ %>
			  		<tr>
			  		<td class="emph">Group By</td>
			  		<td><%= listToString(q.StudentGroupBy,q.InstructorGroupBy)%></td>
			  		<td><%= listToString(q.InstructorGroupBy,q.StudentGroupBy)%></td>
			  		</tr>
			  			<%} %>
			  		<%if(q.StudentHavingClause != null && q.StudentHavingClause.size() > 0
			  			|| (q.InstructorHavingClause != null && q.InstructorHavingClause.size() > 0)){ %>
			  		<tr>
			  		<td class="emph">Having Clause</td>
			  		<td><%= listToString(q.StudentHavingClause,q.InstructorHavingClause)%></td>
			  		<td><%= listToString(q.InstructorHavingClause,q.StudentHavingClause)%></td>
			  		</tr>
			  		<%} %>
			  		<%if(q.StudentSubQConnective != null && q.StudentSubQConnective.size() > 0
			  			|| (q.InstructorSubQConnective != null && q.InstructorSubQConnective.size() > 0)){ %>
			  		<tr>
			  		<td class="emph">SubQuery Connective</td>
			  		<td><%= listToString(q.StudentSubQConnective,q.InstructorSubQConnective)%></td>
			  		<td><%= listToString(q.InstructorSubQConnective,q.StudentSubQConnective)%></td>
			  		</tr>
			  		<%} %>
			  		<!-- <tr>
			  		<td class="emph">Aggregates</td>
			  		<td><%//= listToString(q.StudentAggregates,q.InstructorAggregates)%></td>
			  		<td><%//= listToString(q.InstructorAggregates,q.StudentAggregates)%></td>
			  		</tr>
			  		-->
			  		<%if(q.StudentSetOperators != null && q.StudentSetOperators.size() > 0
			  			||( q.InstructorSetOperators != null && q.InstructorSetOperators.size() > 0)){ %>
			  		<tr>
			  		<td class="emph">Set Operators</td>
			  		<td ><%= listToString(q.StudentSetOperators,q.InstructorSetOperators)%></td>
			  		<td ><%= listToString(q.InstructorSetOperators,q.StudentSetOperators)%></td>
			  		</tr>
			  		<%} %>
			  		<%if(q.studentDistinct || q.instructorDistinct){ %>
			  		<tr>
			  		<td class="emph">Distinct</td>
			  		<% if(q.studentDistinct != q.instructorDistinct){%>
			  			<td  style="color: red;">
			  			<%}else{ %>
			  			<td >
			  			<%} %>
			  			<% if(q.studentDistinct){%>1<%}else { %>0<%} %> </td>
			  		
			  		
			  		<%if(q.studentDistinct != q.instructorDistinct){%>
			  			<td  style="color: red;">
			  			<%}else{ %>
			  			<td >
			  			<%} %>
			  			
			  			<%if(q.instructorDistinct) {%>1<%}else {%>0<%} %></td>
			  	
			  		</tr>
			  		<%} %> 
			  		
			  		<%if(q.StudentInnerJoins != null && q.StudentInnerJoins.size() > 0
			  			||( q.InstructorInnerJoins != null && q.InstructorInnerJoins.size() > 0)){ %>
			  		<tr>
			  		<td class="emph">Inner Join Conditions</td>
			  		<td ><%= listToString(q.StudentInnerJoins,q.InstructorInnerJoins)%></td>
			  		<td ><%= listToString(q.InstructorInnerJoins,q.StudentInnerJoins)%></td>
			  		</tr>
			  		<%} %>
			  		<%if(q.StudentOuterJoins != null && q.StudentOuterJoins.size() > 0
			  			||( q.InstructorOuterJoins != null && q.InstructorOuterJoins.size() > 0)){ %>
			  		<tr>
			  		<td class="emph">Outer Join Conditions</td>
			  		<td ><%= listToString(q.StudentOuterJoins,q.InstructorOuterJoins)%></td>
			  		<td ><%= listToString(q.InstructorOuterJoins,q.StudentOuterJoins)%></td>
			  		</tr>
			  		<%} %>
			  		</table>
			  		
			  		</div>
			  		<%
		  		}
		  		%>
		  		</div>
		  		</fieldset></div>
		  		</div>
		  		
		  		
		  		
   		       <% 
   		    	   }
   		       stmt.close();
   		       rs.close();
   		       conn.close();
   		       }
   		       %>
 	    </div>
 	     <%if(requestingPage != null && !requestingPage.equalsIgnoreCase("popUp")) {%>
      		  <a class="header" href ="javascript:history.go(-1)"> Back</a>
        <%} %>
	</fieldset>
		</div>
	</div>

</body>
</html>