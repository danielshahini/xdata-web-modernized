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
<%@page import="util.FailedDataSetValues" %>
<%@page import="testDataGen.PopulateTestData" %>
<%@page import="testDataGen.TestAnswer"%>
<%@page import="parsing.QueryParser"%>
<%@page import="util.TesterDatasource"%>
<%@page import="util.DataSetValue"%>
<%@page import="java.io.PrintWriter"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<head>
		<title>Equivalence Result</title>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<script type="text/javascript" src="scripts/jquery.js"></script>
		<script type="text/javascript" src="scripts/wufoo.js"></script>
 		<script src="highlight/highlight.pack.js"></script> 
		<script type="text/javascript" src = "scripts/jquery-2.1.4.js"></script>


		<link rel="stylesheet" href="css/structure.css" type="text/css" />
		<link rel="stylesheet" href="css/form.css" type="text/css" />
		<link rel="stylesheet" href="css/theme.css" type="text/css" />
		<link rel="stylesheet" href="css/structure.css" type="text/css"/>  
		<link rel="stylesheet" href="highlight/styles/xcode.css"> 
		<link rel="stylesheet" href="highlight/styles/default.css">
		
		<script type="text/javascript">hljs.initHighlightingOnLoad();
		
		function toggleRefTables(id){
			$(id).toggle();

			if($(id).parent().children()[0].innerHTML=="View Referenced Tables"){
				$(id).parent().children()[0].innerHTML="Hide Referenced Tables";
			}
			else{
				$(id).parent().children()[0].innerHTML="View Referenced Tables";
			}
		}
		$( document ).ready(function() {
			$('#showMatchingResults').click(function(e){
				
				//alert("calls show div");
				 e.preventDefault();
				$('#showMatchingDatasetDiv').show();
				//$("#showMatchingDatasetDiv").scrollintoview(true);
				//window.scroll(0,findPos(document.getElementById("showMatchingDatasetDiv")));
				//$j('html, body').animate({ scrollTop: $j("#"+showMatchingDatasetDiv).offset().top }, 1500);
				
					var offset = $("#showMatchingDatasetDiv").offset();
					var top = ($("#showMatchingDatasetDiv").offset() || { "top": NaN }).top;
					if (isNaN(top)) {
					   return true;
					} else {
					  
						 $('html, body').animate({
						       scrollTop: $("#showMatchingDatasetDiv").offset().top}, 2000);
					 
							}
			
			});
		 
		});
		
	
		</script>
		<link rel="canonical" href="http://www.wufoo.com/gallery/designs/template.html">
		<style> html,body {background: #fff;} fieldset {background: #f2f2e6; padding: 10px;	border: 1px solid #fff;	border-color: #fff #666661 #666661 #fff;	margin-bottom: 36px;}</style>
		</head>

		<body id="public">
		<%
		if (session.getAttribute("LOGIN_USER") == null) {
			response.sendRedirect("index.jsp?TimeOut=true");
			return;
		}
		
Connection dbCon = null, testcon = null;
int assignment_id=Integer.parseInt(request.getParameter("assignment_id"));
int question_id=Integer.parseInt(request.getParameter("question_id"));
String course_id = (String) request.getSession().getAttribute("context_label");
testcon = (new util.DatabaseConnection()).getTesterConnection(assignment_id);

dbCon=dbCon = (new DatabaseConnection()).dbConnection();
String sqlQuery = "";
int index = 0;

%>
		<div id="fieldset">
			<div class="info">
			<h2>Test Case: <%=question_id%></h2>
			</div>
			<%
			PreparedStatement stmt=dbCon.prepareStatement("select sql,query_id from xdata_instructor_query where course_id = ? and assignment_id=? and question_id=? ");
			stmt.setString(1,course_id);
			stmt.setInt(2,assignment_id);
			stmt.setInt(3,question_id);
			ResultSet rs2 = stmt.executeQuery();
			%>
			<p align="left"> <strong>Application Queries: </strong><br/>
			<%while(rs2.next()){
				sqlQuery = rs2.getString("sql");
				index++;
				%>
				 <pre><code class="sql"><%=index %>.<%=sqlQuery %></code></pre>
				
									
			<%}%>
			 <a id='showMatchingResults' href='#' style='float:right;color: #00f;text-decoration: none;font-weight: bold;'>Show Matcing Datasets</a>
			<%
			String sel_dataset = "select tag,value from xdata_datasetvalue where datasetid =? and assignment_id= ? and question_id=? and query_id=?";
			String failedDataSets = "select equivalence_failed_datasets from xdata_qinfo where course_id = ? and assignment_id= ? and question_id = ?";
			
			PreparedStatement stmt1=dbCon.prepareStatement(failedDataSets);
			stmt1.setString(1, course_id);
			stmt1.setInt(2,assignment_id); 
			stmt1.setInt(3,question_id);
			
			ResultSet resultSet = stmt1.executeQuery();
			while(resultSet.next()){
				Map<Integer,List<String>> usedDataSetMap = new HashMap<Integer,List<String>>();
				ArrayList<String> usedDataset = new ArrayList<String>();
				String failedeqv = resultSet.getString(1);
				Type listTypes = new TypeToken<ArrayList<FailedDataSetValues>>() {
                }.getType();
                
				List<FailedDataSetValues> dsfailedList = new Gson().fromJson(failedeqv, listTypes);
				PopulateTestData populateTestData = new PopulateTestData();
				
				for(FailedDataSetValues failedDSValues : dsfailedList){
					
						
						String dataSetId = failedDSValues.getDataSetId();
						if(! usedDataSetMap.containsKey(failedDSValues.getQuery_id())){
							usedDataset = new ArrayList();
						}
						
						usedDataset.add(dataSetId);
						populateTestData.deleteAllTempTablesFromTestUser(testcon);
						populateTestData.createTempTables(testcon, assignment_id, question_id);
						
						
						PreparedStatement stmt2=dbCon.prepareStatement(sel_dataset);
						stmt2.setString(1, dataSetId);
						stmt2.setInt(2,assignment_id); 
						stmt2.setInt(3,question_id);
						stmt2.setInt(4,failedDSValues.getQuery_id()); 
						if(dataSetId.startsWith("DS")){
							ResultSet resultSet1 = stmt2.executeQuery(); 
								if(resultSet1.next()){
									%>
									<div style='align:left;'><h4><%=resultSet1.getString("tag")%> FOR INSTRUCTOR QUERY <%=failedDSValues.getQuery_id() %></h4>
									</div>
								<%}							
							}else{ %>
								<div style='align:left;'><h4>Default sample data Id:<%=dataSetId%></h4>
								</div>
							<%}
						%>
						
					     <!-- <div style='margin-right: 40%;'> --><div>
					     <span> <b>Result of query 1:</b></span>
					     <table border="1">
					    <tr>
					     	 <%Map<String, ArrayList<String>> instrDataFailed = failedDSValues.getInstrQueryOutput().get(dataSetId);
						     Iterator it1 = instrDataFailed.keySet().iterator();
						     while(it1.hasNext()){ %>
						    	<th><%=(String)it1.next()%></th>			    	 
						     <%}%>
						     </tr>
						     <%it1 = instrDataFailed.keySet().iterator();
						     int colSize1 = instrDataFailed.keySet().size();
						     int valSize1 = 0;
						    %><tr><%
							     while(it1.hasNext()){
							    	 int valCnt = 0;
							    	 ArrayList Values = instrDataFailed.get(it1.next());
							    	 valSize1 = Values.size();
							     }
							     if(valSize1 == 0){
							    	 while(it1.hasNext()){
							    		%>
							    		<td></td>
							    	 <%} %> 
							    	  </tr>
							     <%}else{
								     for(int v = 0 ; v< valSize1;v++){
								    	 it1 = instrDataFailed.keySet().iterator();
									     while(it1.hasNext()){						    	
									    	 ArrayList Values = instrDataFailed.get(it1.next());
									    	%><td><%=Values.get(v)%></td>
									     <%} %>
									     </tr> 
								   <%  }
								  }%>
								     </table></div>
								     <p></p>
								    <!--<div style='margin-right: 20%;'> -->
								    <div>
								     <span><b> Result of Query 2:</b></span>
								     <table border="1">
								     <tr>
						
							     
					
							     <%Map<String, ArrayList<String>> studDataFailed = failedDSValues.getStudentQueryOutput().get(dataSetId);
					     Iterator it = studDataFailed.keySet().iterator();
					     while(it.hasNext()){ 
					    	%>
					    	<th><%=(String)it.next()%></th>	    	 
					     <%}%>
					     	</tr>	
					
					     <%it = studDataFailed.keySet().iterator();
					     int colSize = studDataFailed.keySet().size();
					     int valSize = 0;
					     %><tr><%
						     while(it.hasNext()){
						    	 int valCnt = 0;
						    	 ArrayList Values = studDataFailed.get(it.next());
						    	 valSize = Values.size();
						     }
						     if(valSize== 0){
						    	 while(it.hasNext()){
						    		 %><td></td>
						   <%	 }%>
						    	</tr>
						     <%}else{
							     for(int v = 0 ; v< valSize;v++){
							    	 it = studDataFailed.keySet().iterator();
								     while(it.hasNext()){
								    	 int valCnt = 0;
								    	 ArrayList Values = studDataFailed.get(it.next());
								    	%>
								    	<td><%=Values.get(v)%></td>
								    <% }%>
								     </tr>
								     <%} 
								     }%>
							     </table></div><p></p>
							      <%if(dataSetId.startsWith("DS")){
									PreparedStatement pstmt=dbCon.prepareStatement("select value from xdata_datasetvalue where assignment_id=? and question_id = ? and datasetid=? and course_id = ? and query_id=?");
										pstmt.setInt(1, assignment_id);
										pstmt.setInt(2,question_id);
										pstmt.setString(3,dataSetId);
										pstmt.setString(4,course_id);
										pstmt.setInt(5,failedDSValues.getQuery_id());
										ResultSet rsi=pstmt.executeQuery();
											while(rsi.next()){
											String value=rsi.getString("value");
											//It holds JSON obj tat has list of Datasetvalue class
											Gson gson = new Gson();
											//ArrayList dsList = gson.fromJson(value,ArrayList.class);
											Type listType = new TypeToken<ArrayList<DataSetValue>>() {}.getType();
							                    
											List<DataSetValue> dsList = new Gson().fromJson(value, listType);	
										 %>
											<!--<div style='margin-right: 20%;'> -->
											<div>
											<a class='showhidelink' href = 'javascript:void(0);' onclick="toggleDataset('#<%=failedDSValues.getQuery_id()%>A<%=dataSetId%>')">View Dataset</a>
											<div class='detail' id='<%=failedDSValues.getQuery_id() %>A<%=dataSetId%>'>
											
											<%	boolean refTableExists = false;
											for(int i = 0 ; i < dsList.size();i++ ){
												DataSetValue dsValue = (DataSetValue)dsList.get(i);
												String tname,values;
												if(dsValue.getFilename().contains(".ref")){
													refTableExists = true;
												}
												if(!(dsValue.getFilename().contains(".ref"))){
													tname = dsValue.getFilename().substring(0,dsValue.getFilename().indexOf(".copy"));	
												
												PreparedStatement detailStmt = testcon.prepareStatement("select * from " + tname + " where 1 = 0");
												ResultSetMetaData columnDetail = detailStmt.executeQuery().getMetaData();
												%>
												<table border="1">
												<caption><%=tname%></caption>
												<tr>
												<%//Column names get from metadata
												 for(int cl=1; cl <= columnDetail.getColumnCount(); cl++)
												    {
													 %><th><%=columnDetail.getColumnLabel(cl)%></th>
												 <% }%> 
												</tr>
												<%//Get Column values
												for(String dsv: dsValue.getDataForColumn()){
													String columns[]=dsv.split("\\|");
													%><tr><%
													for(String column: columns)
													{%>
														<td><%=column%></td>
													<%}%>
													</tr>
												<%}
												
												detailStmt.close();
											 %>
											</table>
											<%}//If not reference table stmnt ends%>
											
												<%}
											/**Code to toggle Reference Tables - START**/
											if(refTableExists){
												%>
											<p></p><div style='margin-right: 0%;'>
											<a class='showhidelink' href = 'javascript:void(0);' onclick="toggleRefTables('#<%=failedDSValues.getQuery_id()%>R<%=dataSetId%>')">View Referenced Tables</a>
							
											<p></p><div class='detail' id='<%=failedDSValues.getQuery_id()%>R<%=dataSetId%>'>
											<%
											for(int i = 0 ; i < dsList.size();i++ ){ 
												DataSetValue dsValue = (DataSetValue)dsList.get(i);
												String tname = "",values;
												//if(dsValue.getFilename().contains(".ref")){
												//	tname = dsValue.getFilename().substring(0,dsValue.getFilename().indexOf(".ref"));
												//}
												if(dsValue.getFilename().contains(".ref")){
													tname = dsValue.getFilename().substring(0,dsValue.getFilename().indexOf(".ref"));
												
													PreparedStatement detailStmt = testcon.prepareStatement("select * from " + tname + " where 1 = 0");
													ResultSetMetaData columnDetail = detailStmt.executeQuery().getMetaData();
													%>
													<table border="1">
													<caption><%=tname%></caption>
													<tr>
													<%//Column names get from metadata
													 for(int cl=1; cl <= columnDetail.getColumnCount(); cl++)
													    {
														 %><th><%=columnDetail.getColumnLabel(cl)%></th>
													 <% }%> 
													</tr>
													<%//Get Column values
													for(String dsv: dsValue.getDataForColumn()){
														String columns[]=dsv.split("\\|");
														%><tr><%
														for(String column: columns)
														{%>
															<td><%=column%></td>
														<%}%>
														</tr>
													<%}
													
													detailStmt.close();
												 %>
												</table>
												<%}
											}%>
											</div>
											<%}
											%>
											</div></div>
											<p></p>
											<%}
										} else{//If dsId is sampledataId, then show sample data as tables
								    	 %>
											
											<%
											populateTestData.deleteAllTablesFromTestUser(testcon);
								            populateTestData.createTempTableWithDefaultData(dbCon, testcon, assignment_id, question_id, course_id, dataSetId);			    	 
								               
											DatabaseMetaData meta = testcon.getMetaData();
											String tableFilter[] = {"TEMPORARY TABLE"};
						                    
								            ResultSet r = meta.getTables(dbCon.getCatalog(), null, "%", tableFilter);
								            while (r.next()) {                          
								               String tableName = r.getString("TABLE_NAME").toUpperCase();
								               PreparedStatement p = testcon.prepareStatement("select * from "+tableName);
								              
								               ResultSetMetaData columnDetail = p.executeQuery().getMetaData();
								               %>
												<table border="1">
												<caption>"+tableName+"</caption>
												<tr>
												<%
												//Column names get from metadata
												 for(int cl=1; cl <= columnDetail.getColumnCount(); cl++)
												    {%>
													<th><%=columnDetail.getColumnLabel(cl)%></th>
												  <%} %>
												  </tr>
												  <%ResultSetMetaData columnDetail1 = p.executeQuery().getMetaData();
												  ResultSet rrset = p.executeQuery();
												 while(rrset.next()){
													 %>
													 <tr>
													 <%for(int cl=1; cl <= columnDetail1.getColumnCount(); cl++)
													    {	 %>
														 <td><%=rrset.getString(cl)%></td>
													    <%}%>
													 </tr>
													 <%}
												    }//End of while loop
												    %>
								        	</table></div>
								    		<p></p>
						            <%}	//End of Else loop	
						            usedDataSetMap.put(failedDSValues.getQuery_id(),usedDataset);
						            }%>
						 </div>
						 </div>
					    <p></p>		
					    
					    	<hr>	
					    		 
						<div id="showMatchingDatasetDiv" style='display:none;'>
							<h2 style='float: center;'>Matched Datasets</h2>
							<%
							//Get SQL queries for the question
							PreparedStatement stmnt1=dbCon.prepareStatement("select sql,query_id from xdata_instructor_query where course_id = ? and assignment_id=? and question_id=? ");
							stmnt1.setString(1,course_id);
							stmnt1.setInt(2,assignment_id);
							stmnt1.setInt(3,question_id);
							ResultSet rset1 = stmnt1.executeQuery();
							List<Integer> queryWithNoFailedDS = new ArrayList();
							try{
							while(rset1.next()){
								
								//Get datasetlist from usedDataSetMap for this query
								List<String> usedDataList = usedDataSetMap.get(rset1.getInt("query_id"));
										
								//For each SQL query, get the datasets from datasetvalue
								String sel_tag_datasets = "select tag,value from xdata_datasetvalue where assignment_id= ? and question_id=? and query_id=? and datasetid=?";
								PreparedStatement stmnt2=dbCon.prepareStatement(sel_tag_datasets);
								stmnt2.setInt(1,assignment_id); 
								stmnt2.setInt(2,question_id);
								stmnt2.setInt(3,rset1.getInt("query_id")); 
								String sel_all_datasets = "select tag,value,datasetid from xdata_datasetvalue where assignment_id= ? and question_id=? and query_id=?";
								PreparedStatement stmt2=dbCon.prepareStatement(sel_all_datasets);
								stmt2.setInt(1,assignment_id); 
								stmt2.setInt(2,question_id);
								stmt2.setInt(3,rset1.getInt("query_id")); 
								ResultSet rset2 = stmt2.executeQuery();
								
								while(rset2.next()){ 
									String dataSetId = rset2.getString("datasetid");
									//If usedDataMap does not contain datasetId then it is a matching dataset -show the DS
									if(usedDataList == null){
										
										if(queryWithNoFailedDS != null && !queryWithNoFailedDS.contains(rset1.getInt("query_id"))){
											queryWithNoFailedDS.add(rset1.getInt("query_id"));
										}else if(( queryWithNoFailedDS == null || 
												(queryWithNoFailedDS != null && queryWithNoFailedDS.size() == 0))){
											queryWithNoFailedDS.add(rset1.getInt("query_id"));
										}
									}
									
									if((usedDataList != null && !(usedDataList.contains(dataSetId)))){
										
											if(dataSetId.startsWith("DS")){
												stmnt2.setString(4,dataSetId);
												ResultSet resultSet1 = stmnt2.executeQuery(); 
													if(resultSet1.next()){
														%>
														<div style='align:left;'><h4><%=resultSet1.getString("tag")%> FOR INSTRUCTOR QUERY <%=rset1.getInt("query_id")%></h4>
														</div>
													<%}							
												}else{ %>
													<div style='align:left;'><h4>Default sample data Id:<%=dataSetId%></h4>
													</div>
												<%}
											String args[] = {dataSetId,Integer.toString(assignment_id),Integer.toString(question_id),Integer.toString(rset1.getInt("query_id")),course_id};
											try {
												PopulateTestData.entry(args);
											} catch (Exception e) {
												throw new ServletException(e);
											}
											//Result of executing query 1 on dataset
											String sel_query = "select sql,query_id from xdata_instructor_query where assignment_id=? and question_id=? and course_id = ?";
											 PreparedStatement qstmt=dbCon.prepareStatement(sel_query);
											     qstmt.setInt(1,assignment_id);
											     qstmt.setInt(2, question_id);
											     qstmt.setString(3,course_id);
											      ResultSet rr=qstmt.executeQuery();
											      //For each SQL query
													  while (rr.next()){
													  PreparedStatement stmnt=testcon.prepareStatement(rr.getString("sql"));
													    
													     ResultSet r=stmnt.executeQuery();
													     
													     ResultSetMetaData metadata = r.getMetaData();
													     int no_of_columns=metadata.getColumnCount();
													     %>
													     <p></p>
													     <div style='margin-right: 20%;'>
													     <span> Result of Query :<%= rr.getInt("query_id")%></span>
													     <table border="1">
													    <tr>
													     <%
													     for(int cl=1;cl<=no_of_columns;cl++)
													     {%>
													    	 <th><%=metadata.getColumnLabel(cl)%></th>
													     <%}%>
													    </tr>
													    <% while(r.next())
													     { %>
													    	 <tr>
													    	<% for(int j=1;j<=no_of_columns;j++)
													    	 {
													    		 int type = metadata.getColumnType(j);
													             if (type == Types.VARCHAR || type == Types.CHAR) {
													            	 %>
													            	 <td><%=r.getString(j)%></td>
													             <%} else {
													             %>
													            	 <td><%=r.getLong(j)%></td>
													             <%}
													    		
													    	 }%>
													    	 </tr>
													   <% }//for each row loop
											     r.close();
											     stmt.close();%>
											     </table>
											     </div>
											    
									<%		     
									  }//while rr.next ends
							      
							
											     
											
											if(dataSetId.startsWith("DS")){

												PreparedStatement pstmt=dbCon.prepareStatement("select value from xdata_datasetvalue where assignment_id=? and question_id = ? and datasetid=? and course_id = ? and query_id=?");
													pstmt.setInt(1, assignment_id);
													pstmt.setInt(2,question_id);
													pstmt.setString(3,dataSetId);
													pstmt.setString(4,course_id);
													pstmt.setInt(5,rset1.getInt("query_id"));
													ResultSet rsi=pstmt.executeQuery();
														while(rsi.next()){
														String value=rsi.getString("value");
														//It holds JSON obj tat has list of Datasetvalue class
														Gson gson = new Gson();
														//ArrayList dsList = gson.fromJson(value,ArrayList.class);
														Type listType = new TypeToken<ArrayList<DataSetValue>>() {}.getType();
										                    
														List<DataSetValue> dsList = new Gson().fromJson(value, listType);	
													 %>
														<!--<div style='margin-right: 20%;'> -->
														<div>
														<a class='showhidelink' href = 'javascript:void(0);' onclick="toggleDataset('#<%=rset1.getInt("query_id")%>A<%=dataSetId%>')">View Dataset</a>
														<div class='detail' id='<%=rset1.getInt("query_id") %>A<%=dataSetId%>'>
														
														<%	boolean refTableExists = false;
														for(int i = 0 ; i < dsList.size();i++ ){
															DataSetValue dsValue = (DataSetValue)dsList.get(i);
															String tname,values;
															if(dsValue.getFilename().contains(".ref")){
																refTableExists = true;
															}
															if(!(dsValue.getFilename().contains(".ref"))){
																tname = dsValue.getFilename().substring(0,dsValue.getFilename().indexOf(".copy"));	
															
															PreparedStatement detailStmt = testcon.prepareStatement("select * from " + tname + " where 1 = 0");
															ResultSetMetaData columnDetail = detailStmt.executeQuery().getMetaData();
															%>
															<table border="1">
															<caption><%=tname%></caption>
															<tr>
															<%//Column names get from metadata
															 for(int cl=1; cl <= columnDetail.getColumnCount(); cl++)
															    {
																 %><th><%=columnDetail.getColumnLabel(cl)%></th>
															 <% }%> 
															</tr>
															<%//Get Column values
															for(String dsv: dsValue.getDataForColumn()){
																String columns[]=dsv.split("\\|");
																%><tr><%
																for(String column: columns)
																{%>
																	<td><%=column%></td>
																<%}%>
																</tr>
															<%}
															
															
														 %>
														</table>
														<%}//If not reference table stmnt ends%>
														
															<%}
														/**Code to toggle Reference Tables - START**/
														if(refTableExists){
															%>
														<p></p><div style='margin-right: 0%;'>
														<a class='showhidelink' href = 'javascript:void(0);' onclick="toggleRefTables('#<%=rset1.getInt("query_id")%>R<%=dataSetId%>')">View Referenced Tables</a>
										
														<p></p><div class='detail' id='<%=rset1.getInt("query_id")%>R<%=dataSetId%>'>
														<%
														for(int i = 0 ; i < dsList.size();i++ ){ 
															DataSetValue dsValue = (DataSetValue)dsList.get(i);
															String tname = "",values;
															//if(dsValue.getFilename().contains(".ref")){
															//	tname = dsValue.getFilename().substring(0,dsValue.getFilename().indexOf(".ref"));
															//}
															if(dsValue.getFilename().contains(".ref")){
																tname = dsValue.getFilename().substring(0,dsValue.getFilename().indexOf(".ref"));
															
																PreparedStatement detailStmt = testcon.prepareStatement("select * from " + tname + " where 1 = 0");
																ResultSetMetaData columnDetail = detailStmt.executeQuery().getMetaData();
																%>
																<table border="1">
																<caption><%= tname%></caption>
																<tr>
																<%//Column names get from metadata
																 for(int cl=1; cl <= columnDetail.getColumnCount(); cl++)
																    {
																	 %><th><%=columnDetail.getColumnLabel(cl)%></th>
																 <% }%> 
																</tr>
																<%//Get Column values
																for(String dsv: dsValue.getDataForColumn()){
																	String columns[]=dsv.split("\\|");
																	%><tr><%
																	for(String column: columns)
																	{%>
																		<td><%=column%></td>
																	<%}%>
																	</tr>
																<%}
																
																
															 %>
															</table>
															<%}
														}%>
														</div>
														<%}
														%>
														</div></div></div>
														<p></p>
														<%}
													
											}
											
									}//If map not contains datasetID loop ends
									
									else{
										//if map contains dataset then it is failed DS - no need to display in this div
										continue;
									}
							} //While each dataset loop exits
							}
							//For queryWithNoFailedDS - load all datasets for these query ids and show the output of the queries
							if(queryWithNoFailedDS != null && queryWithNoFailedDS.size()>0){
								
								System.out.println("queryWithNoFailedDS size = " + queryWithNoFailedDS.size());
								//***********Code REPEATD - try to change - start
							for(int lm=0;lm<queryWithNoFailedDS.size();lm++){ 
								
								int id=queryWithNoFailedDS.get(lm);
								String sel_tag_datasets1 = "select tag,value from xdata_datasetvalue where assignment_id= ? and question_id=? and query_id=? and datasetid=?";
								PreparedStatement stmntt2=dbCon.prepareStatement(sel_tag_datasets1);
								stmntt2.setInt(1,assignment_id); 
								stmntt2.setInt(2,question_id);
								stmntt2.setInt(3,id); 
								String sel_all_datasets1 = "select tag,value,datasetid from xdata_datasetvalue where assignment_id= ? and question_id=? and query_id=?";
								PreparedStatement stmtt2=dbCon.prepareStatement(sel_all_datasets1);
								stmtt2.setInt(1,assignment_id); 
								stmtt2.setInt(2,question_id);
								stmtt2.setInt(3,id); 
								ResultSet rsett2 = stmtt2.executeQuery();
								while(rsett2.next()){
									String dataSetId = rsett2.getString("datasetid");
									
								if(dataSetId.startsWith("DS")){
									stmntt2.setString(4,dataSetId);
									ResultSet resultSet1 = stmntt2.executeQuery(); 
										if(resultSet1.next()){
											%>
											<div style='align:left;'><h4><%=resultSet1.getString("tag")%> FOR INSTRUCTOR QUERY <%=id %></h4>
											</div>
										<%}							
									}else{ %>
										<div style='align:left;'><h4>Default sample data Id:<%=dataSetId%></h4>
										</div>
									<%}
								String args[] = {dataSetId,Integer.toString(assignment_id),Integer.toString(question_id),Integer.toString(id),course_id};
								try {
									PopulateTestData.entry(args);
								} catch (Exception e) {
									throw new ServletException(e);
								}
								//Result of executing query 1 on dataset
								String sel_query = "select sql,query_id from xdata_instructor_query where assignment_id=? and question_id=? and course_id = ?";
								 PreparedStatement qstmt=dbCon.prepareStatement(sel_query);
								     qstmt.setInt(1,assignment_id);
								     qstmt.setInt(2, question_id);
								     qstmt.setString(3,course_id);
								      ResultSet rr=qstmt.executeQuery();
								      //For each SQL query
										  while (rr.next()){
										  PreparedStatement stmnt=testcon.prepareStatement(rr.getString("sql"));
										    
										     ResultSet r=stmnt.executeQuery();
										     
										     ResultSetMetaData metadata = r.getMetaData();
										     int no_of_columns=metadata.getColumnCount();
										     %>
										     <p></p>
										     <div style='margin-right: 20%;'>
										     <span> Result of Query :<%= rr.getInt("query_id")%></span>
										     <table border="1">
										    <tr>
										     <%
										     for(int cl=1;cl<=no_of_columns;cl++)
										     {%>
										    	 <th><%=metadata.getColumnLabel(cl)%></th>
										     <%}%>
										    </tr>
										    <% while(r.next())
										     { %>
										    	 <tr>
										    	<% for(int j=1;j<=no_of_columns;j++)
										    	 {
										    		 int type = metadata.getColumnType(j);
										             if (type == Types.VARCHAR || type == Types.CHAR) {
										            	 %>
										            	 <td><%=r.getString(j)%></td>
										             <%} else {
										             %>
										            	 <td><%=r.getLong(j)%></td>
										             <%}
										    		
										    	 }%>
										    	 </tr>
										   <% }//for each row loop
								     r.close();
								     stmt.close();%>
								     </table>
								     </div>
								    
						<%		     
						  }//while rr.next ends
				      
				
								     
								
								if(dataSetId.startsWith("DS")){
									PreparedStatement pstmt=dbCon.prepareStatement("select value from xdata_datasetvalue where assignment_id=? and question_id = ? and datasetid=? and course_id = ? and query_id=?");
										pstmt.setInt(1, assignment_id);
										pstmt.setInt(2,question_id);
										pstmt.setString(3,dataSetId);
										pstmt.setString(4,course_id);
										pstmt.setInt(5,id);
										ResultSet rsi=pstmt.executeQuery();
											while(rsi.next()){
											String value=rsi.getString("value");
											//It holds JSON obj tat has list of Datasetvalue class
											Gson gson = new Gson();
											//ArrayList dsList = gson.fromJson(value,ArrayList.class);
											Type listType = new TypeToken<ArrayList<DataSetValue>>() {}.getType();
							                    
											List<DataSetValue> dsList = new Gson().fromJson(value, listType);	
										 %>
											<!--<div style='margin-right: 20%;'> -->
											<div>
											<a class='showhidelink' href = 'javascript:void(0);' onclick="toggleDataset('#<%=id%>A<%=dataSetId%>')">View Dataset</a>
											<div class='detail' id='<%=id %>A<%=dataSetId%>'>
											
											<%	boolean refTableExists = false;
											for(int i = 0 ; i < dsList.size();i++ ){
												DataSetValue dsValue = (DataSetValue)dsList.get(i);
												String tname,values;
												if(dsValue.getFilename().contains(".ref")){
													refTableExists = true;
												}
												if(!(dsValue.getFilename().contains(".ref"))){
													tname = dsValue.getFilename().substring(0,dsValue.getFilename().indexOf(".copy"));	
												
												PreparedStatement detailStmt = testcon.prepareStatement("select * from " + tname + " where 1 = 0");
												ResultSetMetaData columnDetail = detailStmt.executeQuery().getMetaData();
												%>
												<table border="1">
												<caption><%=tname%></caption>
												<tr>
												<%//Column names get from metadata
												 for(int cl=1; cl <= columnDetail.getColumnCount(); cl++)
												    {
													 %><th><%=columnDetail.getColumnLabel(cl)%></th>
												 <% }%> 
												</tr>
												<%//Get Column values
												for(String dsv: dsValue.getDataForColumn()){
													String columns[]=dsv.split("\\|");
													%><tr><% 
													for(String column: columns)
													{%>
														<td><%=column%></td>
													<%}%>
													</tr>
												<%}
												
												
											 %>
											</table>
											<%}//If not reference table stmnt ends%>
											
												<%}
											/**Code to toggle Reference Tables - START**/
											if(refTableExists){
												%>
											<p></p><div style='margin-right: 0%;'>
											<a class='showhidelink' href = 'javascript:void(0);' onclick="toggleRefTables('#<%=id%>R<%=dataSetId%>')">View Referenced Tables</a>
							
											<p></p><div class='detail' id='<%=id%>R<%=dataSetId%>'>
											<%
											for(int i = 0 ; i < dsList.size();i++ ){ 
												DataSetValue dsValue = (DataSetValue)dsList.get(i);
												String tname = "",values;
												//if(dsValue.getFilename().contains(".ref")){
												//	tname = dsValue.getFilename().substring(0,dsValue.getFilename().indexOf(".ref"));
												//}
												if(dsValue.getFilename().contains(".ref")){
													tname = dsValue.getFilename().substring(0,dsValue.getFilename().indexOf(".ref"));
												
													PreparedStatement detailStmt = testcon.prepareStatement("select * from " + tname + " where 1 = 0");
													ResultSetMetaData columnDetail = detailStmt.executeQuery().getMetaData();
													%>
													<table border="1">
													<caption><%= tname%></caption>
													<tr>
													<%//Column names get from metadata
													 for(int cl=1; cl <= columnDetail.getColumnCount(); cl++)
													    {
														 %><th><%=columnDetail.getColumnLabel(cl)%></th>
													 <% }%> 
													</tr>
													<%//Get Column values
													for(String dsv: dsValue.getDataForColumn()){
														String columns[]=dsv.split("\\|");
														%><tr><%
														for(String column: columns)
														{%>
															<td><%=column%></td>
														<%}%>
														</tr>
													<%}
													
													
												 %>
												</table>
												<%}
											}%>
											</div>
											<%}
											%>
											</div></div></div>
											<p></p>
											<%}
										
								}
								
						
						  
								//Code repeated - try to change - ends
								
							}}
							}//For each query id
							}catch(Exception e){
								e.printStackTrace();
							}
							%>
							
							</div>
							
						<% 
							}//For each equivalence Failed Dataset%>
							
							
							
							
							
			
	

</body>
</html>