

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import testDataGen.PopulateTestData;
import util.DataSetValue;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import database.CommonFunctions;
import database.DatabaseConnection;
import evaluation.FailedColumnValues;
import evaluation.FailedDataSetValues;

/**
 * Servlet implementation class FailedTestCases
 */
@WebServlet("/FailedTestCases")
public class FailedTestCases extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(FailedTestCases.class.getName());   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FailedTestCases() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session=request.getSession();
		Connection dbCon = null, testcon = null;
		logger.log(Level.FINE,"Request is from Pop-Up Modal : "+request.getParameter("fromPopup"));
		int assignment_id=Integer.parseInt(request.getParameter("assignment_id"));
		int question_id=Integer.parseInt(request.getParameter("question_id"));
		int query_id=1;
		String course_id = (String) request.getSession().getAttribute("context_label");
		String user_id=request.getParameter("user_id");
		try {
    	    testcon = (new util.DatabaseConnection()).getTesterConnection(assignment_id);
    	    dbCon = (new DatabaseConnection()).dbConnection();
    	      
    	}catch (Exception ex) {
    		   logger.log(Level.SEVERE,"SQLException: " + ex.getMessage());
    	       throw new ServletException(ex);
    	}	

		response.setContentType("text/html");
		PrintWriter out_assignment = response.getWriter();
		out_assignment.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""+
		"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"+

		"<html xmlns=\"http://www.w3.org/1999/xhtml\">"+
		"<head>"+ 

		"<title>"+
		"XData &middot; Assignment"+ 
		"</title>"+
		"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"+
		"<script type=\"text/javascript\" src=\"scripts/jquery.js\"></script>"+
		"<script type=\"text/javascript\" src=\"scripts/wufoo.js\"></script>"+
 		"<script src=\"highlight/highlight.pack.js\"></script>  "+
		
		"<link rel=\"stylesheet\" href=\"css/structure.css\" type=\"text/css\" />"+
		"<link rel=\"stylesheet\" href=\"css/form.css\" type=\"text/css\" />"+
		"<link rel=\"stylesheet\" href=\"css/theme.css\" type=\"text/css\" />"+
		"<link rel=\"stylesheet\" href=\"css/structure.css\" type=\"text/css\"/> "+   
		"<link rel=\"stylesheet\" href=\"highlight/styles/xcode_white.css\">  "+
		"<link rel=\"stylesheet\" href=\"highlight/styles/default_white.css\">"+
		
		
		"<script type=\"text/javascript\" src=\"../scripts/jquery.js\"></script>"+
		"<script type=\"text/javascript\" src=\"../scripts/wufoo.js\"></script>"+
 		"<script src=\"../highlight/highlight.pack.js\"></script>  "+
		
		"<link rel=\"stylesheet\" href=\"../css/structure.css\" type=\"text/css\" />"+ 
		"<link rel=\"stylesheet\" href=\"../css/form.css\" type=\"text/css\" />"+
		"<link rel=\"stylesheet\" href=\"../css/theme.css\" type=\"text/css\" />"+
		"<link rel=\"stylesheet\" href=\"../css/structure.css\" type=\"text/css\"/> "+   
		"<link rel=\"stylesheet\" href=\"../highlight/styles/xcode.css\">  "+
		"<link rel=\"stylesheet\" href=\"../highlight/styles/default.css\">"+
		
		"<script type=\"text/javascript\">"+  "hljs.initHighlightingOnLoad();" 
		+" $().ready(function(){ $(document).keydown(function(e) {if (e.keyCode == 27) {window.close();}}); });"
		+"function toggleRefTables(id){"
		+"$(id).toggle();"

		+"if($(id).parent().children()[0].innerHTML==\"View Referenced Tables\"){"
			+"$(id).parent().children()[0].innerHTML=\"Hide Referenced Tables\";"
		+"}"
		+"else{"
			+"$(id).parent().children()[0].innerHTML=\"View Referenced Tables\";"
		+"}"
	+"}"
		+"</script>"+
		"<link rel=\"canonical\" href=\"http://www.wufoo.com/gallery/designs/template.html\">"+
		"<style> html,body {background: #fff;} "
		+"fieldset.action {background: #9da2a6;	border-color: #e5e5e5 #797c80 #797c80 #e5e5e5;"
		+"margin-top: -20px;}legend {background: -webkit-linear-gradient(top, #657B9E, #4E5663);background: -moz-linear-gradient(top, #657B9E, #4E5663);color: #fff;font: 17px/21px Calibri, Arial, Helvetica, sans-serif;padding: 0 10px;	margin: 0 0 0 -11px;"
		+"border: 1px solid #fff;border-color: #3D5783 #3D5783 #3D5783 #3D5783;text-align: left;}"
		+"fieldset {background: white; padding: 10px;	border: 1px solid #0f0f0f;	margin-bottom: 36px;}"
		+"</style>"+
		"</head>"+

		"<body id=\"public\">"+

		"<div class=\"fieldset\">"+

		"<fieldset>"+"<legend>Failed Test Cases</legend>"+"<div align = \"left\">"+  
		"<p><h4>Assignment: <label id='assignId'>"+assignment_id+"</label></h4></p>"
	     +"<p><h4>Question: <label id='questionId'>"+question_id+"</label></h4></p>"
	       +"<p><h4>Roll Number: <label id='userId'>"+user_id+"</label></h4></p>"
);
		//+"<form class=\"wufoo\" action=\"LoginChecker\" method=\"post\">"); 
		
		  try{
		
			  
			String failedDataSets = "select result,querystring from xdata_student_queries where rollnum =? and assignment_id= ? and question_id = ?";
			//	out_assignment.println("<br/><div style = 'font-weight:bold'>Message: <span style='font-weight:normal'>Your query did not pass the datasets shown below.</span></div>");
			//out_assignment.println("<br/><hr>");
			String sel_dataset = "select tag,value from xdata_datasetvalue where datasetid =? and assignment_id= ? and question_id=? and query_id=?";
			
			PreparedStatement stmt1=dbCon.prepareStatement(failedDataSets);
			stmt1.setString(1, user_id);
			stmt1.setInt(2,assignment_id); 
			stmt1.setInt(3,question_id);
			
			ResultSet resultSet = stmt1.executeQuery();	
			resultSet.next();
			String studQuery = resultSet.getString("querystring");
			out_assignment.println("<p><h4>Student Answer: " +
					"</h4></p>"+ "<pre><code class=\"sql\">"
					+CommonFunctions.encodeHTML(CommonFunctions.decodeURIComponent(studQuery))
					+"</code></pre>");
			
	        PreparedStatement stment=dbCon.prepareStatement("select sql from xdata_instructor_query where course_id = ? and assignment_id=? and question_id=? ");
	        stment.setString(1, course_id);
	        stment.setInt(2, assignment_id);
	        stment.setInt(3, question_id); 
				ResultSet rs2 = stment.executeQuery();
				//out_assignment.println("<div>");
				//out_assignment.println("<a class='showhidelink' href = 'javascript:void(0);' onclick=\"toggleInstructorAnswer('#answer')\">View Instructor Answer</a>");
				//out_assignment.println("<div>");
				
				String out = "<p><h4>Instructor's Answer:";
				out += "</h4></p>";
				while(rs2.next()){
					out += "<pre><code class=\"sql\">"+CommonFunctions.encodeHTML(rs2.getString("sql"))+"</code></pre>";				
				}
				//</div></div>";
				out_assignment.println(out);
				rs2.close();
				stment.close();
				
				String resultdsf = "";
				//Get resultSet and divide it into datasets 
				String ans = resultSet.getString("result");
				
				Gson gson1 = new Gson();
				//ArrayList dsList = gson.fromJson(value,ArrayList.class);
				Type listType1 = new TypeToken<FailedDataSetValues>() {
                }.getType();
                  
                FailedDataSetValues failedDSValues = new Gson().fromJson(ans, listType1);
               // Iterator<String> setIterator = hs.iterator();
				out_assignment.println("<h3>"+"Student query failed for the following datasets."+"</h3><hr></div>");
				PopulateTestData populateTestData = new PopulateTestData();
				populateTestData.deleteAllTempTablesFromTestUser(testcon);
				populateTestData.createTempTables(testcon, assignment_id, question_id);
				if(failedDSValues != null && failedDSValues.getDataSetIdList() != null){
				for(int ds=0;ds<failedDSValues.getDataSetIdList().size();ds++){
					
					String dataSetId = failedDSValues.getDataSetIdList().get(ds);
					ArrayList<FailedColumnValues> dsValueList = failedDSValues.getDsValueMap().get(dataSetId);
					logger.log(Level.FINE,"Data set IDs = "+dataSetId);
					PreparedStatement stmt2=dbCon.prepareStatement(sel_dataset);
					stmt2.setString(1, dataSetId);
					stmt2.setInt(2,assignment_id); 
					stmt2.setInt(3,question_id);
					stmt2.setInt(4,1); 
					ResultSet resultSet1 = stmt2.executeQuery(); 
					if(dataSetId.startsWith("DS")){
						if(resultSet1.next()){				 
							out_assignment.println("<div style='align:left;'><h4>"+resultSet1.getString("tag")+"</h4>");
							out_assignment.println("</div>");
						}
					}else{
						out_assignment.println("<div style='align:left;'><h4>Default sample data Id:"+dataSetId+"</h4>");
						out_assignment.println("</div>");
					}
				//Display the actual result of executing the student query on this dataset
					resultSet1.close();
					stmt2.close();
					/*populateTestData.populateDataset(assignment_id, question_id,query_id, course_id, dataSetId, dbCon, testcon);

			    String qstmt1 = CommonFunctions.decodeURIComponent(studQuery);				    
			    PreparedStatement stment1 =  testcon.prepareStatement(qstmt1);
			    ResultSet rq= stment1.executeQuery();
			     ResultSetMetaData metadataq= rq.getMetaData(); 
			     int  no_of_columns1=metadataq.getColumnCount();*/
			     out_assignment.println("");
			     out_assignment.println("<div style='margin-right: 40%;'>");
			     out_assignment.println("<span> <b>Student query Result</b></span>");
			     out_assignment.println("<table border=\"1\">");
			     out_assignment.println("<tr>");
			     Map<String, ArrayList<String>> studDataFailed = failedDSValues.getStudentQueryOutput().get(dataSetId);
			     Iterator it = studDataFailed.keySet().iterator();
			     while(it.hasNext()){ 
			    	 out_assignment.println("<th>"+ (String)it.next()+"</th>");			    	 
			     }
			     out_assignment.println("</tr>");
			     it = studDataFailed.keySet().iterator();
			     int colSize = studDataFailed.keySet().size();
			     int valSize = 0;
			     out_assignment.println("<tr>");
			     //Iterate to find the no: of rows requires - number of values each column has
				     while(it.hasNext()){
				    	 int valCnt = 0;
				    	 ArrayList Values = studDataFailed.get(it.next());
				    	 valSize = Values.size();
				     }
				     if(valSize== 0){
				    	 while(it.hasNext()){
				    		 out_assignment.println("<td></td>");
				    	 }
				    	   out_assignment.println("</tr>");
				     }else{
					     for(int v = 0 ; v< valSize;v++){
					    	 it = studDataFailed.keySet().iterator();
						     while(it.hasNext()){
						    	 int valCnt = 0;
						    	 ArrayList Values = studDataFailed.get(it.next());
						    	 out_assignment.println("<td>"+Values.get(v)+"</td>");
						     }
						     out_assignment.println("</tr>");
					     }
				     }
				    	// valCnt++;
				     
			     
			  
			    /* for(int dsIndex = 0 ; dsIndex<dsValueList.size(); dsIndex++){
			    	 FailedColumnValues fCol = dsValueList.get(dsIndex);
			    	 if(fCol.getColumnName() != null || !fCol.getColumnName().isEmpty()){ 
				    	 out_assignment.println("<th>"+ fCol.getColumnName()+"</th>");
				    	 out_assignment.println("</tr>");
			    	 }
			    	 
			    	 for(int val=0; val < fCol.getValues().size(); val++){
			    		 out_assignment.println("<tr>");
				    	 out_assignment.println("<td>"+fCol.getValues().get(val)+"</td>");	
				    	 out_assignment.println("</tr>");		
			    	 }
			    		    	
			     }*/
			   /*  for(int cl=1;cl<=no_of_columns1;cl++)
			     {
			    	 out_assignment.println("<th>"+metadataq.getColumnLabel(cl)+"</th>");
			     }
			     out_assignment.println("</tr>");
			     while(rq.next())
			     {
			    	 out_assignment.println("<tr>");
			    	 for(int j=1;j<=no_of_columns1;j++)
			    	 { 
			    		 int type = metadataq.getColumnType(j);
			             if (type == Types.VARCHAR || type == Types.CHAR) {
			            	 out_assignment.println("<td>"+rq.getString(j)+"</td>");
			             } else {
			            	 out_assignment.println("<td>"+rq.getLong(j)+"</td>");
			             }
			    		
			    	 }
			    	 out_assignment.println("</tr>");
			     }
			     rq.close();
			     stment1.close();*/
			     
			     out_assignment.println("</table>");
			     out_assignment.println("</div>");
				
   
				//Display actual result for executing the query	 
				/*String sel_query = "select sql from xdata_instructor_query where assignment_id=? and question_id=?";
			     PreparedStatement qstmt=dbCon.prepareStatement(sel_query);
			     qstmt.setInt(1,assignment_id);
			     qstmt.setInt(2, question_id);
			     ResultSet rr=qstmt.executeQuery();
			     while (rr.next()){
			    	 PreparedStatement stmt=testcon.prepareStatement(rr.getString("sql"));
			     //rr.close();
			     ResultSet r=stmt.executeQuery();
			     ResultSetMetaData metadata = r.getMetaData();
			     int  no_of_columns=metadata.getColumnCount();*/
			    out_assignment.println("<p></p>");
			     out_assignment.println("<div style='margin-right: 20%;'>");
			     out_assignment.println("<span><b> Expected Result</b></span>");
			     out_assignment.println("<table border=\"1\">");
			     out_assignment.println("<tr>");
			     Map<String, ArrayList<String>> instrDataFailed = failedDSValues.getInstrQueryOutput().get(dataSetId);
			     Iterator it1 = instrDataFailed.keySet().iterator();
			     while(it1.hasNext()){ 
			    	 out_assignment.println("<th>"+ (String)it1.next()+"</th>");			    	 
			     }
			     out_assignment.println("</tr>");
			     it1 = instrDataFailed.keySet().iterator();
			     int colSize1 = instrDataFailed.keySet().size();
			     int valSize1 = 0;
			     out_assignment.println("<tr>");
				     while(it1.hasNext()){
				    	 int valCnt = 0;
				    	 ArrayList Values = instrDataFailed.get(it1.next());
				    	 valSize1 = Values.size();
				     }
				     if(valSize1 == 0){
				    	 while(it1.hasNext()){
				    		 out_assignment.println("<td></td>");
				    	 } 
				    	  out_assignment.println("</tr>");
				     }else{
					     for(int v = 0 ; v< valSize1;v++){
					    	 it1 = instrDataFailed.keySet().iterator();
						     while(it1.hasNext()){						    	
						    	 ArrayList Values = instrDataFailed.get(it1.next());
						    	 out_assignment.println("<td>"+Values.get(v)+"</td>");
						     }
						     out_assignment.println("</tr>"); 
					     }
					    
				     }
				    	// valCnt++;
				     
			     
			   
			     /*for(int dsIndex = 0 ; dsIndex<dsValueList.size(); dsIndex++){
			    	 FailedColumnValues fCol = dsValueList.get(dsIndex);			    	 
			    	 if(fCol.getInstrColumnName() != null || !fCol.getInstrColumnName().isEmpty()){ 
				    	 out_assignment.println("<th>"+ fCol.getInstrColumnName()+"</th>");
				    	 out_assignment.println("</tr>");
			    	 }
			    	 
			    	 for(int val=0; val < fCol.getInstrValues().size(); val++){
			    		 out_assignment.println("<tr>");
				    	 out_assignment.println("<td>"+fCol.getInstrValues().get(val)+"</td>");	
				    	 out_assignment.println("</tr>");		
			    	 }
			    		    	
			     }
			     */
			    /* out_assignment.println("<tr>");
			     for(int cl=1;cl<=no_of_columns;cl++)
			     {
			    	 out_assignment.println("<th>"+metadata.getColumnLabel(cl)+"</th>");
			     }
			     out_assignment.println("</tr>");
			     while(r.next())
			     {
			    	 out_assignment.println("<tr>");
			    	 for(int j=1;j<=no_of_columns;j++)
			    	 { 
			    		 int type = metadata.getColumnType(j);
			             if (type == Types.VARCHAR || type == Types.CHAR) {
			            	 out_assignment.println("<td>"+r.getString(j)+"</td>");
			             } else {
			            	 out_assignment.println("<td>"+r.getLong(j)+"</td>");
			             }
			    		
			    	 }
			    	 out_assignment.println("</tr>");
			     }
			     r.close();
			     stmt.close();*/
			     out_assignment.println("</table>");
			     out_assignment.println("</div>");
			     //}
			    // rr.close(); 
			    // qstmt.close();
			     
			     out_assignment.println("<p></p>");
			     if(dataSetId.startsWith("DS")){
					     //Select and show the  datasets
						PreparedStatement pstmt=dbCon.prepareStatement("select value from xdata_datasetvalue where assignment_id=? and question_id = ? and datasetid=? and course_id = ?");
						pstmt.setInt(1, assignment_id);
						pstmt.setInt(2,question_id);
						pstmt.setString(3,dataSetId);
						pstmt.setString(4,course_id);
						ResultSet rsi=pstmt.executeQuery();
						
						while(rsi.next()){
						String value=rsi.getString("value");
						
						//It holds JSON obj tat has list of Datasetvalue class
							Gson gson = new Gson();
							Type listType = new TypeToken<ArrayList<DataSetValue>>() {
		                    }.getType();
		                    
							List<DataSetValue> dsList = new Gson().fromJson(value, listType);
							
					 
						out_assignment.println("<div style='margin-right: 20%;'>");
						out_assignment.println("<a class='showhidelink' href = 'javascript:void(0);' onclick=\"toggleDataset('#"+dataSetId+"')\">View Dataset</a>");
					
						out_assignment.println("<div class='detail' id='"+dataSetId+"'>");
						boolean refTableExists = false;
						for(int i = 0 ; i < dsList.size();i++ ){
							DataSetValue dsValue = (DataSetValue)dsList.get(i);
							String tname="",values;
							if(dsValue.getFilename().contains(".ref")){
								refTableExists = true;
							}
							if(!(dsValue.getFilename().contains(".ref"))){
								tname = dsValue.getFilename().substring(0,dsValue.getFilename().indexOf(".copy"));
							
							//tname = dsValue.getFilename().substring(0,dsValue.getFilename().indexOf(".copy"));
							PreparedStatement detailStmt = testcon.prepareStatement("select * from " + tname + " where 1 = 0");
							ResultSetMetaData columnDetail = detailStmt.executeQuery().getMetaData();
							out_assignment.println("<table border=\"1\">");
							out_assignment.println("<caption>"+tname+"</caption>");
							out_assignment.println("<tr>");
							//Column names get from metadata
							 for(int cl=1; cl <= columnDetail.getColumnCount(); cl++)
							    {
								 out_assignment.println("<th>" + columnDetail.getColumnLabel(cl)+"</th>");
							  } 
							 out_assignment.println("</tr>");
							//Get Column values
							for(String dsv: dsValue.getDataForColumn()){
								String columns[]=dsv.split("\\|");
								out_assignment.println("<tr>");
								for(String column: columns)
								{
									out_assignment.println("<td>"+column+"</td>");
								}
								out_assignment.println("</tr>");
							}
							
							detailStmt.close();
							}
						} 
						out_assignment.println("</table>");
		
						  out_assignment.println("<p></p>");
						  
						  //***To show referenced tables
							/**Code to toggle Reference Tables - START**/
							if(refTableExists){
								
								out_assignment.println("<p></p><div style='margin-right: 0%;'>");
								out_assignment.println("<a class='showhidelink' href = 'javascript:void(0);' onclick=\"toggleRefTables('#"+failedDSValues.getQuery_id()+"R"+dataSetId+"')\">View Referenced Tables</a>");
			
								out_assignment.println("<p></p><div class='detail' id='"+failedDSValues.getQuery_id()+"R"+dataSetId+"'>");
							
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
									
									out_assignment.println("<table border=\"1\">");
									out_assignment.println("<caption>"+tname+"</caption>");
									out_assignment.println("<tr>");
									//Column names get from metadata
									 for(int cl=1; cl <= columnDetail.getColumnCount(); cl++)
									    {
										 out_assignment.println("<th>"+columnDetail.getColumnLabel(cl)+"</th>");
									 } 
									out_assignment.println("</tr>");
									//Get Column values
									for(String dsv: dsValue.getDataForColumn()){
										String columns[]=dsv.split("\\|");
										out_assignment.println("<tr>");
										for(String column: columns)
										{
											out_assignment.println("<td>"+column+"</td>");
										}
										out_assignment.println("</tr>");
									}													
								detailStmt.close();
								 
								 out_assignment.println("</table>");
							}
							}
							out_assignment.println("</div></div>");
							}
							
							
							out_assignment.println("</div></div>");
						}
						rsi.close();
						pstmt.close();
					
				}
			     else{//If dsId is sampledataId, then show sample data as tables
			    	 
			    	 	//PreparedStatement pstmt=dbCon.prepareStatement("select sample_data from xdata_sampledata where sampledata_id=? and course_id = ?");
						
						//pstmt.setString(1,dataSetId);
						//pstmt.setString(2,course_id);
						//ResultSet rsi=pstmt.executeQuery();
						//rsi.next();
						out_assignment.println("<div style='margin-right: 20%;'>");
						out_assignment.println("<a class='showhidelink' href = 'javascript:void(0);' onclick=\"toggleDataset('#"+dataSetId+"')\">View Dataset</a>");
					
						out_assignment.println("<div class='detail' id='"+dataSetId+"'>");
						
						populateTestData.deleteAllTablesFromTestUser(testcon);
			            populateTestData.createTempTableWithDefaultData(dbCon, testcon, assignment_id, question_id, course_id, dataSetId);			    	 
			               
						DatabaseMetaData meta = testcon.getMetaData();
						String tableFilter[] = {"TEMPORARY TABLE"};
	                    
			            ResultSet r = meta.getTables(testcon.getCatalog(), null, "%", tableFilter);
			            while (r.next()) {                          
			               String tableName = r.getString("TABLE_NAME").toUpperCase();
			               if(tableName.equalsIgnoreCase("dataset")){
			            	   continue;
			               }
			               PreparedStatement p = testcon.prepareStatement("select * from "+tableName);
			              
			               ResultSetMetaData columnDetail = p.executeQuery().getMetaData();
							out_assignment.println("<table border=\"1\">");
							out_assignment.println("<caption>"+tableName+"</caption>");
							out_assignment.println("<tr>");
							//Column names get from metadata
							 for(int cl=1; cl <= columnDetail.getColumnCount(); cl++)
							    {
								 out_assignment.println("<th>" + columnDetail.getColumnLabel(cl)+"</th>");
							  } 
							 out_assignment.println("</tr>");
							  ResultSetMetaData columnDetail1 = p.executeQuery().getMetaData();
							  ResultSet rrset = p.executeQuery();
							 while(rrset.next()){
								 out_assignment.println("<tr>");
								 for(int cl=1; cl <= columnDetail1.getColumnCount(); cl++)
								    {	
									 out_assignment.println("<td>"+rrset.getString(cl)+"</td>");
								    }
								 out_assignment.println("</tr>");
								 }
							    }
			        	out_assignment.println("</table></div>");
			    		
						  out_assignment.println("<p></p>");
						  
			            }	
			     out_assignment.println("<hr>");
				}
				}
				 out_assignment.println("</div>");
				 
				 out_assignment.println("</div>");
			     out_assignment.println("<p></p>");					 
				out_assignment.println("<hr>");

		  
		  }catch(Exception e){
			  logger.log(Level.SEVERE,e.getMessage(),e);
			  throw new ServletException(e);
		  }finally{
			  
			  try {
				dbCon.close();
				testcon.close();
			} catch (SQLException e) {
				  logger.log(Level.SEVERE,e.getMessage(),e);
				  throw new ServletException(e);
			}
			  
		  }
	}
 
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

}
