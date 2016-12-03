import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import database.*;
import evaluation.FailedDataSetValues;

import com.google.gson.Gson;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import testDataGen.PopulateTestDataGrading;
import util.DataSetValue;
import util.DatabaseConnectionDetails;

/**
 * Servlet implementation class StudentTestCase
 */
//@WebServlet("/StudentTestCase")
public class StudentTestCase extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(StudentTestCase.class.getName());   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public StudentTestCase() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		boolean isForView = false;
		
		HttpSession session=request.getSession(false);
		if (session.getAttribute("LOGIN_USER") == null) {
			response.sendRedirect("index.jsp?TimeOut=true");
			return;
		}
		
		Connection dbCon = null, testcon = null;
		boolean isViewGradedAssignment = false;
		int assignment_id=Integer.parseInt(request.getParameter("assignment_id"));
		int question_id=Integer.parseInt(request.getParameter("question_id"));
		int query_id=1;
		String course_id = (String) request.getSession().getAttribute("context_label");
		String user_id=request.getParameter("user_id");
		String status = request.getParameter("status");
		Boolean learningMode = false;
			  
	
		//Instead of getting it from sessin, get it from student table - tajudgement attribute
		//If evaluation status of the assignment is true, then the assignment is evaluated, set this label to true.
		
		if(session.getAttribute("displayTestCase") != null && Boolean.valueOf(session.getAttribute("displayTestCase").toString()) == true){
			dbCon = (Connection) session.getAttribute("dbConn");
			testcon = (Connection) session.getAttribute("testConn");
			session.setAttribute("displayTestCase", false);
		}
		 
  		if(testcon==null){
		  	try {
		  		DatabaseConnectionDetails dbConnDetails = (new util.DatabaseConnection()).getTesterConnection(assignment_id);
				testcon = dbConnDetails.getTesterConn();
				
	    	      if(testcon!=null){
	    	    	  logger.log(Level.FINE,"Connected successfullly");
	    	      }
	    	}catch (Exception ex) {
	    		logger.log(Level.SEVERE,"SQLException: " + ex.getMessage());
	    	       throw new ServletException(ex);
	    	}	
  		}

  		logger.log(Level.FINE,"Assignment_id :"+assignment_id);
  		logger.log(Level.FINE,"Question_id :"+question_id);
  		logger.log(Level.FINE,"User id : "+user_id);

		if(dbCon == null){ 
			dbCon=(Connection) session.getAttribute("dbConnection");
		}
		if(dbCon==null)
		{
			try {
		    	     // Class.forName("org.postgresql.Driver");
		       		dbCon = (new DatabaseConnection()).dbConnection();
		    	      if(dbCon!=null){
		    	    	  logger.log(Level.FINE,"Connected successfullly");
		    	    	  //session.setAttribute("TestConnection", testcon); 
		    	      }
		    	}catch (Exception ex) {
		    		logger.log(Level.SEVERE,"SQLException: " + ex.getMessage(),ex);
		    	       throw new ServletException(ex);
		    	}	
		}
		
		try (PreparedStatement statement = dbCon.prepareStatement("select * from xdata_assignment where course_id = ? and assignment_id=?")){
			
			statement.setString(1, course_id);
			statement.setInt(2, assignment_id);
			try(ResultSet resultSet = statement.executeQuery()){
				if(resultSet.next()){
					learningMode = resultSet.getBoolean("learning_mode");
					//If evaluation status of the assignment is true, then the assignment is evaluated, set this label to true.
					isViewGradedAssignment = resultSet.getBoolean("evaluationstatus");
				}  
			}
		} 
		catch (SQLException ex) {
			logger.log(Level.SEVERE,"SQLException: " + ex.getMessage(),ex);
	       throw new ServletException(ex);
		}	
		HashSet<String> hs=new HashSet<String>();
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
		
		"<script type=\"text/javascript\" src=\"../scripts/jquery.js\"></script>"+
		"<script type=\"text/javascript\" src=\"../scripts/wufoo.js\"></script>"+
 		"<script src=\"../highlight/highlight.pack.js\"></script>  "+

		"<link rel=\"stylesheet\" href=\"css/structure.css\" type=\"text/css\" />"+
		"<link rel=\"stylesheet\" href=\"css/form.css\" type=\"text/css\" />"+
		"<link rel=\"stylesheet\" href=\"css/theme.css\" type=\"text/css\" />"+
		"<link rel=\"stylesheet\" href=\"css/structure.css\" type=\"text/css\"/> "+   
		"<link rel=\"stylesheet\" href=\"highlight/styles/xcode.css\">  "+
		"<link rel=\"stylesheet\" href=\"highlight/styles/default.css\">"+
				
		"<link rel=\"stylesheet\" href=\"../css/structure.css\" type=\"text/css\" />"+
		"<link rel=\"stylesheet\" href=\"../css/form.css\" type=\"text/css\" />"+
		"<link rel=\"stylesheet\" href=\"../css/theme.css\" type=\"text/css\" />"+
		"<link rel=\"stylesheet\" href=\"../css/structure.css\" type=\"text/css\"/> "+   
		"<link rel=\"stylesheet\" href=\"../highlight/styles/xcode.css\">  "+
		"<link rel=\"stylesheet\" href=\"../highlight/styles/default.css\">"+
		
		"<script type=\"text/javascript\">"+  "hljs.initHighlightingOnLoad();" 
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
		"<style> html,body {background: #fff;} fieldset {background: #f2f2e6; padding: 10px;	border: 1px solid #fff;	border-color: #fff #666661 #666661 #fff;	margin-bottom: 36px;}"+
		"#breadcrumbs{  position: absolute;  padding-left:10px;  padding-right:10px;  left: 5px;  top: 10px;  font: 13px/13px Arial, Helvetica, sans-serif;  background-color: #f0f0f0;  font-weight: bold;}</style>"+
		"</head>"+

		"<body id=\"public\">");
		if(! Boolean.parseBoolean(session.getAttribute("ltiIntegration").toString())){
			out_assignment.println("<div id=\"breadcrumbs\">"
						+"<a style='color:#353275;text-decoration: none;' href=\"Student/ListAllAssignments.jsp\" target=\"_self\">Assignment List</a>&nbsp; >> &nbsp;"
						+"<a style='color:#353275;text-decoration: none;' href=\"Student/ListOfQuestions.jsp?assignmentid="+assignment_id+"&&studentId="+user_id+"\" target=\"_self\">Question List</a>&nbsp; >> &nbsp;"
						+"<a style='color:#353275;text-decoration: none;' href=\"Student/QuestionDetails.jsp?AssignmentID="
								+ assignment_id + "&&courseId=" + course_id
								+ "&&questionId=" + question_id + "&&studentId="
								+ user_id +"\" target=\"_self\">Edit Question</a>&nbsp; >> &nbsp;"
						+"<a style='color:#0E0E0E;text-decoration: none;font-weight: normal;' href=\"#\">Student Result</a>"
						+"</div>");
		}			
		out_assignment.println("<div id=\"fieldset\">"+ 
		"<form class=\"wufoo\" action=\"LoginChecker\" method=\"post\">"+

			"<div class=\"info\">"+
			"<h2>Question: "+question_id+"</h2>"+
			"</div>"   
			+"<p align=\"left\"> <strong> Your Answer: </strong>"+ "<pre><code class=\"sql\">"+CommonFunctions.encodeHTML(CommonFunctions.decodeURIComponent(request.getParameter("query")))+"</code></pre></p>");

		if(status.equals("Error")){
			out_assignment.println("<div style = 'font-weight: bold'>Status: <label style = 'color:red;'>Error</label></div>");
			out_assignment.println("<br/><div style = 'font-weight:bold'>Message: <span style='font-weight:normal;'>Sorry, your query could not be executed. Please check the syntax and try again.</span></div>");
			String message = request.getParameter("Error");
			
			if(!message.isEmpty()){
				out_assignment.println("<br/><div style = 'font-weight:bold'> Error Message: <span style='font-weight:normal;'>" + CommonFunctions.decodeURIComponent(message) + "</span></div>");
			}
		}
		if(status.equals("NoDataset")){
			out_assignment.println("<div style = 'font-weight: bold'>Status: <label style = 'color:red;'>Error</label></div>");
			out_assignment.println("<br/><div style = 'font-weight:bold'>Message: <span style='font-weight:normal;'>Not answered</span></div>");
			String message = "Please answer the question.";
			
			if(!message.isEmpty()){
				//out_assignment.println("<br/><div style = 'font-weight:bold'> Error Message: <span style='font-weight:normal;'>" + CommonFunctions.decodeURIComponent(message) + "</span></div>");
			}
		} 
		else if(!learningMode && status.equals("Correct")){
			//out_assignment.println("<div style = 'font-weight: bold'>Status: <label style = 'color:green'> Submitted </label><label style='font-weight:normal;'> Your answer is submitted. </label> </div>");
			out_assignment.println("<div style = 'font-weight: bold'>Status: <label style = 'color:green'> Ok </label><label style='font-weight:normal;'> - Your query has passed the basic test case. This does not guarantee the correctness of the answer. Your answer will be graded based on result against all other test cases.</label> </div>");
		}
		else if(!learningMode && status.equals("Incorrect")){
			out_assignment.println("<div style = 'font-weight: bold'>Status: <label style = 'color:red'>Incorrect  </label><label style='font-weight:normal;'> - Your query has failed the basic test case.</label> </div>");
			//out_assignment.println("<div style = 'font-weight: bold'>Status: <label style = 'color:red'>Incorrect  </label><label style='font-weight:normal;'> Some parsing error occurred. Please check the answer.</label> </div>");
		}  
		else if(status.equals("Correct")){
			//This part of code wont be reached. This can be removed after proper testing
			out_assignment.println("<div style = 'font-weight: bold'>Status: <label style = 'color:green'> Ok </label><label style='font-weight:normal;'> - Your query has passed the test cases.</label> </div>");
		}
		else if(status.equals("Incorrect")){
			out_assignment.println("<br/><div style = 'font-weight: bold'>Status: <label style = 'color:red;'>Incorrect</label></div>");
		}
		out_assignment.println("<br/>");
		try { 
			if(status == null || (status != null && status.isEmpty()) || (status != null && status.equalsIgnoreCase("error"))){
				status ="Incorrect";
			}
			//if(isForView){
			/**Get correct Query and Student Query **/
			//If request is from graded assignment to view test cases for incorrect answers
			if((isViewGradedAssignment && status.equalsIgnoreCase("Incorrect"))
					|| (learningMode && status.equals("Incorrect"))){
				String failedDataSets = "select result from xdata_student_queries where rollnum =? and assignment_id= ? and question_id = ?";
				String sel_dataset = "select tag,value from xdata_datasetvalue where datasetid =? and assignment_id= ? and question_id=? and query_id=?";
				try(PreparedStatement stmt1=dbCon.prepareStatement(failedDataSets)){
					stmt1.setString(1, user_id);
					stmt1.setInt(2,assignment_id); 
					stmt1.setInt(3,question_id);
					try(ResultSet resultSet = stmt1.executeQuery()){	
						try(PreparedStatement stment=dbCon.prepareStatement("select sql from xdata_instructor_query where course_id = ? and assignment_id=? and question_id=? ")){
					        stment.setString(1, course_id);
					        stment.setInt(2, assignment_id);
					        stment.setInt(3, question_id); 
								try(ResultSet rs2 = stment.executeQuery()){
									out_assignment.println("<div>");
									out_assignment.println("<a class='showhidelink' href = 'javascript:void(0);' onclick=\"toggleInstructorAnswer('#answer')\">View Instructor Answer</a>");
									out_assignment.println("<div class='detail' id='answer'>");
									
									String out = "<p align=\"left\"> <strong>Instructor's Answer: </strong>";
									while(rs2.next()){
										out += "<pre><code class=\"sql\">"+CommonFunctions.encodeHTML(rs2.getString("sql"))+"</code></pre>";				
									}
									out += "</p></div></div>";
									out_assignment.println(out);
								}
				        }
					//**************  Show Failed DataSets ***********************//
					while(resultSet.next() && !status.equals("Error") && !status.equals("Correct")){
						String resultdsf = "";
						//Get resultSet and divide it into datasets 
						String ans = resultSet.getString("result");
						Type listType1 = new TypeToken<FailedDataSetValues>() {
	                    }.getType();
	                      
	                    FailedDataSetValues failedDSValues = new Gson().fromJson(ans, listType1);					
	                    logger.log(Level.FINE,"Ans : "+ ans);
						out_assignment.println("<h3>"+"Your query did not match the following datasets."+"</h3><hr></div>");
						PopulateTestDataGrading populateTestData = new PopulateTestDataGrading();
						populateTestData.deleteAllTempTablesFromTestUser(testcon);
						populateTestData.createTempTables(testcon, assignment_id, question_id);
						
						//while(setIterator.hasNext()){
						for(int ds=0;ds<failedDSValues.getDataSetIdList().size();ds++){
								//String dataSetId = setIterator.next();
								String dataSetId = failedDSValues.getDataSetIdList().get(ds);
								logger.log(Level.FINE,"Data st IDs = "+dataSetId);
								try(PreparedStatement stmt2=dbCon.prepareStatement(sel_dataset)){
									stmt2.setString(1, dataSetId);
									stmt2.setInt(2,assignment_id); 
									stmt2.setInt(3,question_id);
									stmt2.setInt(4,1); 
									if(dataSetId.startsWith("DS")){
										//If it is dataset generated by XData
										try(ResultSet resultSet1 = stmt2.executeQuery()){ 
											if(resultSet1.next()){				 
												out_assignment.println("<div style='align:left;'><h4>"+resultSet1.getString("tag")+"</h4>");
												out_assignment.println("</div>");
											}
										}
									}
									//If the DataSet is not generated, then show sample data
									else{
											out_assignment.println("<div style='align:left;'><h4>Default sample data Id:"+dataSetId+"</h4>");
											out_assignment.println("</div>");
										}
							}
								
							 out_assignment.println("");
						     out_assignment.println("<div style='margin-right: 40%;'>");
						     out_assignment.println("<span> <b>Your Result</b></span>");
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
				   
						     out_assignment.println("</table>");
						     out_assignment.println("</div>");
						     out_assignment.println("<p></p>");
						     /**    SHOW EXPECTED RESULT    **/
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
							     out_assignment.println("</table>");
							     out_assignment.println("</div>");
						     out_assignment.println("<p></p>");
						     //Select and show the  datasets
						     if(dataSetId.startsWith("DS")){
									try(PreparedStatement pstmt=dbCon.prepareStatement("select value from xdata_datasetvalue where assignment_id=? and question_id = ? and datasetid=? and course_id = ?")){
										pstmt.setInt(1, assignment_id);
										pstmt.setInt(2,question_id);
										pstmt.setString(3,dataSetId);
										pstmt.setString(4,course_id);
										try(ResultSet rsi=pstmt.executeQuery()){
											while(rsi.next()){
											String value=rsi.getString("value");
											//It holds JSON obj tat has list of Datasetvalue class
											Gson gson = new Gson();
											//ArrayList dsList = gson.fromJson(value,ArrayList.class);
											Type listType = new TypeToken<ArrayList<DataSetValue>>() {}.getType();
							                    
											List<DataSetValue> dsList = new Gson().fromJson(value, listType);	
										 
											out_assignment.println("<div style='margin-right: 20%;'>");
											out_assignment.println("<a class='showhidelink' href = 'javascript:void(0);' onclick=\"toggleDataset('#"+dataSetId+"')\">View Dataset</a>");
											out_assignment.println("<div class='detail' id='"+dataSetId+"'>");
											boolean refTableExists = false;
											for(int i = 0 ; i < dsList.size();i++ ){
												DataSetValue dsValue = dsList.get(i);
												String tname = "",values;
												
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
												/**Code to toggle Reference Tables **/
												if(refTableExists){
													
													out_assignment.println("<p></p><div style='margin-right: 0%;'>");
													out_assignment.println("<a class='showhidelink' href = 'javascript:void(0);' onclick=\"toggleRefTables('#"+failedDSValues.getQuery_id()+"R"+dataSetId+"')\">View Referenced Tables</a>");
								
													out_assignment.println("<p></p><div class='detail' id='"+failedDSValues.getQuery_id()+"R"+dataSetId+"'>");
												
												for(int i = 0 ; i < dsList.size();i++ ){ 
													DataSetValue dsValue = dsList.get(i);
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
											
											}//rsi ends
										}//pstmt ends
								     } else{
								    	 //If dsId is sampledataId, then show sample data as tables
									out_assignment.println("<div style='margin-right: 20%;'>");
									out_assignment.println("<a class='showhidelink' href = 'javascript:void(0);' onclick=\"toggleDataset('#"+dataSetId+"')\">View Dataset</a>");
								
									out_assignment.println("<div class='detail' id='"+dataSetId+"'>");
									
									populateTestData.deleteAllTablesFromTestUser(testcon);
									 PreparedStatement p1 = testcon.prepareStatement("drop table if exists DATASET;");
						              p1.execute();
						            populateTestData.createTempTableWithDefaultData(dbCon, testcon, assignment_id, question_id, course_id, dataSetId);			    	 
						               
									DatabaseMetaData meta = testcon.getMetaData();
									String tableFilter[] = {"TEMPORARY TABLE"};
				                    
						            ResultSet r = meta.getTables(dbCon.getCatalog(), null, "%", tableFilter);
						            while (r.next()) {                          
						               String tableName = r.getString("TABLE_NAME").toUpperCase();
						               System.out.println("Table : "+ tableName);
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
						 out_assignment.println("</div>");
						 out_assignment.println("</div>");
					     out_assignment.println("<p></p>");					 
						out_assignment.println("<hr>");
	
					}
				}//Resultset closed
				}//Prepared statement closed

			}//If Incorrect and not Learning mode
			//}
			out_assignment.println("</form></div><!-- End Page Content --></body></html>");
			out_assignment.close();
			
		} catch (SQLException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			//throw new ServletException(e);
		}
		catch(Exception ex){
			logger.log(Level.SEVERE,ex.getMessage(),ex);
			//throw new ServletException(ex);
		} 
		finally{	
			try{ 
					testcon.close();
					dbCon.close();
			}catch(SQLException e){
				logger.log(Level.SEVERE,e.getMessage(),e);
				throw new ServletException(e);
			}
		}
		session.removeAttribute("dbConn");
		session.removeAttribute("testConn");
		session.removeAttribute("displayTestCase");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
