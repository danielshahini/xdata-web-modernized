<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@ page import="java.io.*"%> 
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@ page import="java.text.*"%>
<%@page import="database.*"%>
<%@page import="com.google.gson.Gson"%>
<%@page import="java.lang.reflect.Type"%>
<%@page import="com.google.gson.reflect.TypeToken"%>
<%@page import="com.google.gson.JsonArray"%>
<%@page import="evaluation.FailedDataSetValues" %>
<%@page import="testDataGen.PopulateTestDataGrading" %>
<%@page import="java.util.logging.Logger"%>
<%@page import="evaluation.TestAnswer"%>
<%@page import="parsing.QueryParser"%>
<%@page import="util.TesterDatasource"%>
<%@page import="util.DataSetValue"%>
<%@page import="java.io.PrintWriter"%>
 
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 
<link rel="stylesheet" href="css/structure.css" type="text/css"/> 
<link rel="stylesheet" href="css/form.css" type="text/css"/>
<script type="text/javascript" src="scripts/wufoo.js"></script>
<link rel="stylesheet" href="highlight/styles/xcode_white.css"/>  
<link rel="stylesheet" href="highlight/styles/default_white.css"/> 

<script src="highlight/highlight.pack.js"></script>
  
<script type="text/javascript" src = "scripts/jquery.js"></script>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript">
hljs.initHighlightingOnLoad();


function toggleRefTables(id){
	$(id).toggle();

	if($(id).parent().children()[0].innerHTML=="View Referenced Tables"){
		$(id).parent().children()[0].innerHTML="Hide Referenced Tables";
	}
	else{
		$(id).parent().children()[0].innerHTML="View Referenced Tables";
	}
}

function toggleResult(id){
	$(id).toggle();

	if($(id).parent().children()[0].innerHTML=="View Query Result"){
		$(id).parent().children()[0].innerHTML="Hide Query Result";
	}
	else{
		$(id).parent().children()[0].innerHTML="View Query Result";
	}
}
$( document ).ready(function() {
	
	//Method onclick of queryMatch button - call servlet using ajax and show div stating output matches.
	$('.checkMatch').click(function(e){
		e.preventDefault(); 
		var destination =this.href;
		var id=this.id;
		var self = this;
		
		var textAreaVal=$('#expectedResult'+id).val();
		var dataString= this.name+"&&expectedResult="+textAreaVal;
		
		//alert('Text Area Msg : ' + textAreaVal);
		//alert('dataString = ' + dataString);
		$.ajax({ 
	        type: "POST", 
	        url: "MatchResult",
	        data: dataString,
	        context:this,  
	        beforeSend : function() {
	        	$('#OutputMatch'+id).hide();
	        	$('#OutputMismatch'+id).hide();
	       }, 
	        success: function(data) { 
	        	try{
	        		$('#OutputMatch'+id).show();
	        		$('#OutputMismatch'+id).hide();
	        		//$( '.resultMatch'+id).prop( "checked", true );
	        		$('input.resultMatch[id='+id+']').prop('checked', true);
	        	}
	        		catch(err)
	        		{
	        			$('#OutputMatch'+id).hide();
	        			$('#OutputMismatch'+id).show(); 
		   	        } 
	        	
	        }, 
	        error : function(xhr, ajaxOptions, thrownError){
	        	$('#OutputMatch'+id).hide();
    			$('#OutputMismatch'+id).show(); 
	        	 		        	  
            }
	      }); 
	      return false; 
		
	});
	
	$('.resultMatch').change(function(e) {
        this.checked =  $(this).is(":checked");
        
		e.preventDefault(); 
		var destination =this.href;
		var id=this.id;
		var self = this;
		//alert("is chekcd = " +this.checked);
		var dataString= this.name+"&&isUpdate="+this.checked;
        $.ajax({ 
	        type: "GET", 
	        url: "MatchResult",
	        data: dataString,
	        context:this,  
	        beforeSend : function() {
	        	
	       }, 
	        success: function(data) { 
	        	try{
	        		$( '.resultMatch'+id).prop( "checked", true );
	        	}catch(err)
	        		{
	        			alert("Error while updating query result.");
		   	        } 
	        }, 
	        error : function(xhr, ajaxOptions, thrownError){
	        	//$('#OutputMatch'+id).hide();
    			//$('#OutputMismatch'+id).show(); 
	        	alert("Error while updating query result.");
	        	 		        	  
            }
	      }); 
		
        return false;
    });
	//Method on click of chk Box - show a div with some message that dataset is flagged correct.
	//$('#resultMatch').click(function(e){
		
	//});
	
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
<title>XData &middot; Assignment</title>
</head>
<body > 
<%
 	if (session.getAttribute("LOGIN_USER") == null) {
 	response.sendRedirect("index.jsp?TimeOut=true");
 	return;
 }
 	else if(session.getAttribute("LOGIN_USER") != null && !session.getAttribute("LOGIN_USER").equals("ADMIN")
 	 		&& session.getAttribute("role") != null && !session.getAttribute("role").equals("instructor")){
 		response.sendRedirect("index.jsp?NotAuthorised=true");
 		session.invalidate();
 		return;
 	}else if(session.getAttribute("LOGIN_USER") != null && !session.getAttribute("LOGIN_USER").equals("ADMIN")
 	 		&& session.getAttribute("role") != null && !session.getAttribute("role").equals("tester")){
 		response.sendRedirect("index.jsp?NotAuthorised=true");
 		session.invalidate();
 		return;
 	}


 if(!((String)session.getAttribute("LOGIN_USER")).equalsIgnoreCase("tester")){
 if(! Boolean.parseBoolean(session.getAttribute("ltiIntegration").toString())){
 %> 
<div id="breadcrumbs"> 
  <a style='color:#353275;text-decoration: none;' href="CourseHome.jsp" target="_top">Home</a> &nbsp; >> &nbsp;
   <a href="InstructorHome.jsp?contextLabel=<%=(String) request.getSession().getAttribute("context_label")%>" style='color:#353275;text-decoration: none;' target="_top"><%=(String) request.getSession().getAttribute("context_label")%></a>&nbsp; >> &nbsp;
   <a href="ListAllAssignments.jsp" style='color:#353275;text-decoration: none;' target="_self">Assignment List</a>&nbsp; >> &nbsp;
   <a href="asgnmentList.jsp?assignmentId=<%=request.getParameter("AssignmentID")%>&&showQuestions=true" target="_self" style='color:#353275;text-decoration: none;'>Assignment Details</a>&nbsp; >> &nbsp;    
  <!--  <a href="ListOfQuestions.jsp?AssignmentID=<%=request.getParameter("AssignmentID")%>" style='color:#353275;text-decoration: none;'>Question List</a>&nbsp; >> &nbsp; -->    
   <a href="#" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>Generated Data</a>
 
  </div> 
<%
 	}else{
 %>
<div id="breadcrumbs">  
   <a href="asgnmentList.jsp?assignmentId=<%=request.getParameter("AssignmentID")%>&&showQuestions=true"" target="_self" style='color:#353275;text-decoration: none;'>Assignment Details</a>&nbsp; >> &nbsp;    
  <!-- <a href="ListOfQuestions.jsp?AssignmentID=<%=request.getParameter("AssignmentID")%>" style='color:#353275;text-decoration: none;'>Question List</a>&nbsp; >> &nbsp;    -->
   <a href="#" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>Generated Data</a>
 
 </div> 
<%
 	} 
 }
 %>
 
<br/>
<div>
		<div class="fieldset"><fieldset>
		<legend>Generated Datasets</legend><br/>
 <div id="container">

					<%
						int assignment_id = Integer.parseInt(request.getParameter("AssignmentID"));
									int question_id = Integer.parseInt(request.getParameter("question_id"));
									String course_id = (String) session.getAttribute("context_label");
									String query = CommonFunctions.decodeURIComponent(request.getParameter("query"));
									String question_text = CommonFunctions.decodeURIComponent(request.getParameter("question_text"));
									int query_id = Integer.parseInt(request.getParameter("query_id"));

									String loginUser = session.getAttribute("LOGIN_USER").toString();

									Connection testcon = ((new util.DatabaseConnection()).getTesterConnection(assignment_id)).getTesterConn();

									Connection dbcon = (new DatabaseConnection()).dbConnection();
									out.println("<div class=\"info\">" + "<h2>Question: " + question_id + "</h2>"
											+ "<h3><b>Question Text:</b></h3>");
									out.println("<h3>" + question_text + "</h3>" + "<h4><b>SQL:</b></h4><pre><code class=\"sql\">" + query
											+ "</code></pre></div>");
									try {
										String datasets = "Select datasetid,value,tag,isresultmatch from xdata_datasetvalue where assignment_id = ? and question_id = ? and query_id=? and course_id = ?";
										PreparedStatement pstmt = dbcon.prepareStatement(datasets);
										pstmt.setInt(1, assignment_id);
										pstmt.setInt(2, question_id);
										pstmt.setInt(3, query_id);
										pstmt.setString(4, course_id);
										ResultSet rs = pstmt.executeQuery();
										PopulateTestDataGrading populateTestData = new PopulateTestDataGrading();
										populateTestData.deleteAllTempTablesFromTestUser(testcon);

										populateTestData.createTempTables(testcon, assignment_id, question_id);
										int index = 0;
										while (rs.next()) {
											index++;
											String args1[] = { rs.getString("datasetid"), String.valueOf(assignment_id),
													String.valueOf(question_id), String.valueOf(query_id), course_id };
											populateTestData.populateDataset(assignment_id, question_id, query_id, course_id,
													rs.getString("datasetid"), dbcon, testcon);

											out.println(
													"<h4><label style=\"width:90px;\">" + index + ". " + rs.getString("tag") + "</label></h4>");
											out.println("<p></p>");

											String value = rs.getString("value");
											//It holds JSON obj tat has list of Datasetvalue class
											//Shree changed for storing DSvalue as JSON
											Gson gson = new Gson();
											//ArrayList dsList = gson.fromJson(value,ArrayList.class);
											Type listType = new TypeToken<ArrayList<DataSetValue>>() {
											}.getType();

											List<DataSetValue> dsList = new Gson().fromJson(value, listType);
											System.out.println("dsList.size() = " + dsList.size());
											boolean refTableExists = false;
											for (int i = 0; i < dsList.size(); i++) {
												DataSetValue dsValue = (DataSetValue) dsList.get(i);
												String tname = "", values;
												//if(dsValue.getFilename().contains(".ref")){
												//	tname = dsValue.getFilename().substring(0,dsValue.getFilename().indexOf(".ref"));
												//}
												if (dsValue.getFilename().contains(".ref")) {
													refTableExists = true;
												}
												if (!(dsValue.getFilename().contains(".ref"))) {
													tname = dsValue.getFilename().substring(0, dsValue.getFilename().indexOf(".copy"));

													PreparedStatement detailStmt = testcon
															.prepareStatement("select * from " + tname + " where 1 = 0");
													ResultSetMetaData columnDetail = detailStmt.executeQuery().getMetaData();

													
														out.println("<table border=\"1\">");
														out.println("<caption>" + tname + "</caption>");
														out.println("<tr>");
														//Column names get from metadata
														for (int cl = 1; cl <= columnDetail.getColumnCount(); cl++) {
															out.println("<th>" + columnDetail.getColumnLabel(cl) + "</th>");
														}
														out.println("</tr>");
														//Get Column values
														for (String dsv : dsValue.getDataForColumn()) {
															String columns[] = dsv.split("\\|");
															out.println("<tr>");
															for (String column : columns) {
																out.println("<td>" + column + "</td>");
															}
															out.println("</tr>");
														}
														out.println("</table>");
													}
												
											}
											/**Code to toggle Reference Tables - START**/
											if (refTableExists) {
												out.println("<p></p><div style='margin-right: 0%;'>");
												out.println("<a class='showhidelink' href = 'javascript:void(0);' onclick=\"toggleRefTables('#"
														+ rs.getString("datasetid") + "')\">View Referenced Tables</a>");

												out.println("<p></p><div class='detail' id='" + rs.getString("datasetid") + "'>");

												for (int i = 0; i < dsList.size(); i++) {
													DataSetValue dsValue = (DataSetValue) dsList.get(i);
													String tname = "", values;
													
													if (dsValue.getFilename().contains(".ref")) {
														tname = dsValue.getFilename().substring(0, dsValue.getFilename().indexOf(".ref"));

														PreparedStatement detailStmt = testcon
																.prepareStatement("select * from " + tname + " where 1 = 0");
														ResultSetMetaData columnDetail = detailStmt.executeQuery().getMetaData();
														out.println("<table border=\"1\">");
														out.println("<caption>" + tname + "</caption>");
														out.println("<tr>");
														//Column names get from metadata
														for (int cl = 1; cl <= columnDetail.getColumnCount(); cl++) {
															out.println("<th>" + columnDetail.getColumnLabel(cl) + "</th>");
														}
														out.println("</tr>");
														//Get Column values
														for (String dsv : dsValue.getDataForColumn()) {
															String columns[] = dsv.split("\\|");
															out.println("<tr>");
															for (String column : columns) {
																out.println("<td>" + column + "</td>");
															}
															out.println("</tr>");
														}
														out.println("</table>");
													}
												}
												out.println("</div>");
											}
											out.println("<p></p>");
											/**Code to toggle Reference Tables - End**/
											String getResults = "Select resultondataset from xdata_instructor_query where assignment_id = ? and question_id = ? and query_id=? and course_id = ?";
											PreparedStatement stmnt = dbcon.prepareStatement(getResults);
											stmnt.setInt(1, assignment_id);
											stmnt.setInt(2, question_id);
											stmnt.setInt(3, query_id);
											stmnt.setString(4, course_id);
											ResultSet rs1 = stmnt.executeQuery();
											rs1.next();
											Type listType1 = new TypeToken<FailedDataSetValues>() {
											}.getType();

											FailedDataSetValues failedDSValues = new Gson().fromJson(rs1.getString("resultondataset"),
													listType1);
											String params = "assignment_id="
													+ assignment_id + "&&question_id=" + question_id +"&&query_id="+query_id + "&&query="
													+ CommonFunctions.encodeURIComponent(query)+"&&datasetid="+rs.getString("datasetid");
											if(loginUser.equalsIgnoreCase("Tester")){
					%>
									Enter Expected Result:<br/>
									<textarea id='expectedResult<%=rs.getString("datasetid") %>' name="tester_result" rows="4" cols="40"> </textarea> <br/>
									<input type='button' id='<%=rs.getString("datasetid") %>' class='checkMatch' 
									name='<%=params %>' value='Compare With Query Result'></a>
									
									<div id='OutputMatch<%=rs.getString("datasetid") %>' style='display:none;font-family:Courier;color:green;'>
										Expected result matches with query result.
									</div>
									<div id='OutputMismatch<%=rs.getString("datasetid") %>' style='display:none;font-family:Courier;color:red;'>
										Expected result does not match with query result.
									</div>
									
									<div style='margin-right: 0%;'>
									<%
								
									
									out.println("<a class='showhidelink' href = 'javascript:void(0);' onclick=\"toggleResult('#"
											+ rs.getString("datasetid") + "result')\">View Query Result</a>");

									out.println("<p></p><div class='detail' id='" + rs.getString("datasetid") + "result'>");
								}
								
								out.println("<p></p>");
								out.println("<table border=\"1\" >");// bgcolor='#657B9E' >");
								out.println("<caption style='background:#657B9E;color: #fff;'> Result</caption>");
								out.println("<tr>");

								
								session.setAttribute("originalOutput",failedDSValues.getInstrQueryOutput());
								
								Map<String, ArrayList<String>> instrData = failedDSValues.getInstrQueryOutput()
										.get(rs.getString("datasetid"));
								if(instrData != null){
								Iterator<String> it1 = instrData.keySet().iterator();
								while (it1.hasNext()) {
									out.println("<th>" + (String) it1.next() + "</th>");
								}
								out.println("</tr>");
								it1 = instrData.keySet().iterator();
								int colSize1 = instrData.keySet().size();
								int valSize1 = 0;
								out.println("<tr>");
								while (it1.hasNext()) {
									int valCnt = 0;
									ArrayList<String> Values = instrData.get(it1.next());
									valSize1 = Values.size();
								}
								if (valSize1 == 0) {
									while (it1.hasNext()) {
										out.println("<td></td>");
									}
									out.println("</tr>");
								} else {
									for (int v = 0; v < valSize1; v++) {
										it1 = instrData.keySet().iterator();
										while (it1.hasNext()) {
											ArrayList<String> Values = instrData.get(it1.next());
											out.println("<td>" + Values.get(v) + "</td>");
										}
										out.println("</tr>");
									}

								}
								}
								out.println("</table>");
								
								if(loginUser.equalsIgnoreCase("Tester")){
									%>
									
									<input type="checkbox" name='<%=params %>' id='<%=rs.getString("datasetid") %>' 
									             class='resultMatch'
									             <%if(rs.getBoolean("isresultmatch")){ %> checked <%}else{%><%}%>>
									             Query result is same as expected</input>
									</div>
									</div>
									<% 	
								}		
								out.println("<hr>");
							}
							rs.close();

						} catch (SQLException e) {
							e.printStackTrace();
							throw new ServletException(e);
						} catch (Exception e) {
							e.printStackTrace();
							throw new ServletException(e);
						} finally {
							dbcon.close();
							testcon.close();
						}
					%>
				</div></fieldset>
				     </div></div>
</body>
</html>
