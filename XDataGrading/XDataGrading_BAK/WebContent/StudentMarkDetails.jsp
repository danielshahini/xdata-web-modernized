<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "https://www.w3.org/TR/html4/loose.dtd">
<%@page import="partialMarking.MarkInfo"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.*"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="partialMarking.*" %>
<%@page import = "com.google.gson.Gson" %>
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
<html> 
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

 <link rel="stylesheet" href="css/structure.css" type="text/css"/>
<script type="text/javascript" src = "scripts/jquery.js"></script>

<title>Student Mark Details</title>
<style>
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


.querypartialmark{
	  margin: 0;
  	  padding: 0;
      margin-bottom: 20px; 
}

.querypartialmark ul{
	  margin: 0;
  	  padding: 0;
      padding-left: 20px;
}

.querypartialmark ul li{
      padding-bottom: 5px;
      list-style: circle;
}

.querypartialmark .number{
	text-align: right;
}

.querypartialmark h4{
	margin: 5px;
}

.emph{
	font-weight: bold;
}

</style>
</head>
<body>
<body>
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}
%>
	<div>
		<div class="fieldset">
		<fieldset>
		<legend>Mark Details</legend> 
  		<div align = "left">  
   		       <%
   		       int assignID = Integer.parseInt(request.getParameter("assignment_id"));
   		       int questionID = Integer.parseInt(request.getParameter("question_id"));
   		       String userId = request.getParameter("user_id");
   		      try( Connection conn = (new DatabaseConnection()).dbConnection()){
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
 		    	  <p><h4>Instructor Query: </h4><pre><code class="sql"><%= CommonFunctions.encodeHTML(CommonFunctions.decodeURIComponent(rs.getString("sql")))%></code></pre></p>
 		       <%
 		       } 		       

  		       stmt = conn.prepareStatement("select * from xdata_student_queries where rollnum = ? and assignment_id = ? and question_id = ?");
  		       stmt.setString(1, userId);
  		       stmt.setInt(2, assignID);
  		       stmt.setInt(3, questionID);
  		       rs = stmt.executeQuery();
  		       
  		       if(rs.next()){
  		    	%> 
  		    		  <p><h4>Student Query: </h4><pre><code class="sql"><%= CommonFunctions.encodeHTML(CommonFunctions.decodeURIComponent(rs.getString("querystring")))%></code></pre></p>
  		    		   <% }
   		       
   		       stmt = conn.prepareStatement("select * from xdata_student_queries where rollnum = ? and assignment_id = ? and question_id = ?");
   		       stmt.setString(1, userId);
   		       stmt.setInt(2, assignID);
   		       stmt.setInt(3, questionID);
   		       rs = stmt.executeQuery();
   		       if(rs.next()){
   		    	   String data = rs.getString("markinfo");
   		    	if(rs.getString("feedback") != null && rs.getString("feedback").isEmpty()==false){
   		    		%>
					<p>
					
					<h4>
						Marks: <label id='queryMarks'><%= rs.getFloat("score") %></label>
					</h4>
					</p>
						<h4>Feedback:</h4> <label id='queryFeedback'><pre><%= rs.getString("feedback") %></pre> </label>
					<%
   		    	}
   		    	else
   		    	{
   		    	 %>
					<p>
					<h4>Marks: <label id='queryMarks'><%= rs.getFloat("score") %></label>
					</h4>	
					</p> <%
   		    	}
  
   		    	   if(!data.isEmpty()){
   		    	   
   		    	   Gson gson = new Gson();
   		    	   MarkInfo marks = gson.fromJson(data, MarkInfo.class);
   		    	   List<QueryInfo> queryInfo = marks.SubqueryData;
   		    	     		    	   
   		    	   Collections.sort(queryInfo, new Comparator<QueryInfo>(){
   		    		public int compare(QueryInfo o1, QueryInfo o2) {
   		    			 return (o1.Level - o2.Level);
   		    	   }});
   		       %>
   		        <fieldset>
				<legend>Query Details</legend> 
		  		<div align = "left">
		  		<%
		  		for(QueryInfo q: queryInfo)
		  		{
		  			 //CALCULATE the number of items and scale the score accordingly
		  			//COUNT the number of items available to calculate scaling factor
		  			int countScale = 0;
		  			if(q.StudentPredicates!= null && q.StudentPredicates.size() > 0
			  			|| (q.InstructorPredicates != null && q.InstructorPredicates.size() > 0)){
		  				countScale++;
		  			}if(q.StudentProjections!= null && q.StudentProjections.size() > 0
				  			|| (q.InstructorProjections != null && q.InstructorProjections.size() > 0)){
		  				countScale++;
		  			}if(q.StudentRelations!= null && q.StudentRelations.size() > 0
				  			|| (q.InstructorRelations != null && q.InstructorRelations.size() > 0)){
		  				countScale++;
		  			}if(q.StudentGroupBy != null && q.StudentGroupBy.size() > 0
				  			|| (q.InstructorGroupBy != null && q.InstructorGroupBy.size() > 0)){ 
		  				countScale++;
		  			}if(q.StudentHavingClause != null && q.StudentHavingClause.size() > 0
				  			|| (q.InstructorHavingClause != null && q.InstructorHavingClause.size() > 0)){
		  				countScale++;
		  			}if(q.StudentSubQConnective != null && q.StudentSubQConnective.size() > 0
				  			|| (q.InstructorSubQConnective != null && q.InstructorSubQConnective.size() > 0)){ 
		  				countScale++;
		  			}if(q.StudentSetOperators != null && q.StudentSetOperators.size() > 0
				  			||( q.InstructorSetOperators != null && q.InstructorSetOperators.size() > 0)){
		  				countScale++;
		  			}if(q.studentDistinct || q.instructorDistinct){
		  				countScale++;
		  			}//if(q.StudentInnerJoins  > 0 || q.InstructorInnerJoins > 0){ 
		  				//countScale++;
		  			//}if(q.StudentOuterJoins  > 0 || q.StudentOuterJoins > 0){
		  			//	countScale++;
		  			//}
		  			System.out.println("Count Scale ******* = "+countScale);
		  			//Usually marks are calculated based on 100.
		  			int scalingFactor = 100/countScale;
		  			/*System.out.println("******scaling factr = "+scalingFactor);
		  			
		  			System.out.println("q.studentPredicateMarks = "+q.studentPredicateMarks);
		  			System.out.println("q.instructorPredicateMark = "+q.instructorPredicateMarks);
		  			System.out.println("q.studentProjectionMarks = "+q.studentProjectionMarks);
		  			System.out.println("q.instructorProjection = "+q.instructorProjectionMarks);
		  			System.out.println("studentRelationsMarks = "+q.studentRelationsMarks);
		  			System.out.println("instr relation marks = "+q.instructorRelationMarks);
		  			System.out.println("studentGroupbyMarks = "+q.studentGroupbyMarks);
		  			System.out.println("instr grp by marks = "+ q.instructorGroupbyMarks);
		  			System.out.println("studentHavingMarks = "+q.studentHavingMarks);
		  			System.out.println("instr havng marks = "+q.instructorHavingMarks);
		  			System.out.println("studentSubqMarks = "+q.studentSubqMarks);
		  			System.out.println("instrSubqMarks"+ q.instructorSubqMarks);
		  			System.out.println("studentSetOperatorMarks = "+q.studentSetOperatorMarks);
		  			System.out.println("instructor set op = "+q.instructorSetOperatorMarks);
		  			System.out.println("studentDistinctMarks = "+q.studentDistinctMarks);
		  			System.out.println("instr distinct = "+q.instructorDistinctMarks);
		  			System.out.println("studentinnserjoin = "+q.studentInnerJoinMarks);
		  			System.out.println("inst inner join  = "+q.instructorInnerJoinMarks);
		  			System.out.println("studentouterjoin = "+q.studentOuterJoinMarks);
		  			System.out.println("inst outer join  = "+q.instructorOuterJoinMarks);
		  			*/
			  		%>
			  		<div class="querypartialmark" style="margin-left:<%=(q.Level) *30 %>px;">
			  		<%	if(queryInfo.size() > 1){%>
			  			<h4>Level: <%= q.Level %></h4>
					<%} %>
			  		<table class="queryTable" width="70%" cellpadding="3" cellspacing="1">
			  		<tr>
			  		<th width="20%">&nbsp;</th>
			  		<th width="20%">Student</th>
			  		<th width="20%">Instructor</th>
			  		<!-- <th width="20%">Student Marks</th>
			  		<th width="20%">Total Marks</th> -->
			  		</tr> 
			  		<%if(q.StudentPredicates!= null && q.StudentPredicates.size() > 0
			  			|| (q.InstructorPredicates != null && q.InstructorPredicates.size() > 0)){ %>
			  		<tr>
			  		<td class="emph">Predicates</td>
			  		<td width="20%"><%= listToString(q.StudentPredicates,q.InstructorPredicates)%></td>
			  		<td width="20%"><%= listToString(q.InstructorPredicates,q.StudentPredicates)%></td>
			  		
			  		<!-- <td width="20%"><%//if(q.studentPredicateMarks != 0.0f && q.instructorPredicateMarks!=0.0f){ %><%//=roundToDecimal(q.studentPredicateMarks*(scalingFactor/ q.instructorPredicateMarks))%><%//}else{%><%//=roundToDecimal(0.0f)%><%//}%></td>
			  		<td width="20%"><%//if(q.instructorPredicateMarks != 0.0f){ %><%//=roundToDecimal(q.instructorPredicateMarks*( scalingFactor/ q.instructorPredicateMarks))%><%//}else{%><%//=roundToDecimal(0.0f)%><%//} %></td> -->
			  		
			  		
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
   		    	   }
   		       }
   		      }//try block
		  		%>
		  		</div>
		  		</fieldset> 
		  		</div>
</body>
</html>
