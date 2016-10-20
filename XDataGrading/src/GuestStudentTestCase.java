

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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import parsing.Node;
import partialMarking.QueryData;
import partialMarking.TestPartialMarking;

import database.*;
import evaluation.FailedDataSetValues;

import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.math.BigDecimal;

import com.google.gson.reflect.TypeToken;

import testDataGen.PopulateTestDataGrading;
import util.DataSetValue;
import util.DatabaseConnectionDetails;
/**
 * Servlet implementation class GuestStudentTestCase
 */
@WebServlet("/GuestStudentTestCase")
public class GuestStudentTestCase extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(StudentTestCase.class.getName());   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GuestStudentTestCase() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//Write new method here
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
		int marks = Integer.parseInt((String)request.getParameter("marks"));
		int maxMarks = Integer.parseInt((String)request.getParameter("maxMarks"));
		Boolean learningMode = false;

		//Instead of getting it from sessin, get it from student table - tajudgement attribute
		//If evaluation status of the assignment is true, then the assignment is evaluated, set this label to true.
		
		if(session.getAttribute("displayTestCase") != null && Boolean.valueOf(session.getAttribute("displayTestCase").toString()) == true){
			dbCon = (Connection) session.getAttribute("dbConn");
			testcon = (Connection) session.getAttribute("testConn");
			session.setAttribute("displayTestCase", false);
		}
		//Get connections if they are closed
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
		
		 
		HashSet<String> hs=new HashSet<String>();
		response.setContentType("text/html;charset=UTF-8");
	
       	
       	
       	/*	PrintWriter out_assignment = response.getWriter();
		
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
		+"}"+" $(document).on('click','#showAnswer',function (event) { event.preventDefault();$('#answer').show(); $('#showAnswer').hide(); });"
			
		+"</script>"+
		"<link rel=\"canonical\" href=\"http://www.wufoo.com/gallery/designs/template.html\">"+
		"<style> html,body {background: #fff;} fieldset {background: #f2f2e6; padding: 10px;	border: 1px solid #fff;	border-color: #fff #666661 #666661 #fff;	margin-bottom: 36px;}"+
		"#breadcrumbs{  position: absolute;  padding-left:10px;  padding-right:10px;  left: 5px;  top: 10px;  font: 13px/13px Arial, Helvetica, sans-serif;  background-color: #f0f0f0;  font-weight: bold;}</style>"+
		"</head>"+

		"<body id=\"public\">");
		String studAnswer = CommonFunctions.decodeURIComponent((String)request.getParameter("query"));
		FailedDataSetValues failedDS = (FailedDataSetValues)session.getAttribute("failedDS");
		out_assignment.println("<div id=\"fieldset\">"+ 
				"<form class=\"wufoo\" action=\"LoginChecker\" method=\"post\">"+

					"<div class=\"info\">"+
					"<h2>Question: "+question_id+"</h2>"+
					"</div>"   
					+"<p align=\"left\"> <strong> Your Answer: </strong>"+ "<pre><code class=\"sql\">"+CommonFunctions.encodeHTML(CommonFunctions.decodeURIComponent((String)request.getParameter("query")))+"</code></pre></p>");

			
		if(status.equals("Error")){
			out_assignment.println("<div style = 'font-weight: bold'>Status: <label style = 'color:red;'>Error</label></div>");
			out_assignment.println("<br/><div style = 'font-weight:bold'>Message: <span style='font-weight:normal;'>Sorry, your query could not be executed. Please check the syntax and try again.</span></div>");
			String message = request.getParameter("Error");
			
			if(!message.isEmpty()){
				out_assignment.println("<br/><div style = 'font-weight:bold'> Details: <span style='font-weight:normal;'>" + CommonFunctions.decodeURIComponent(message) + "</span></div>");
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
		else if(!learningMode && status.equals("Failed")){
			out_assignment.println("<div style = 'font-weight: bold'>Status: <label style = 'color:red'>Incorrect  </label><label style='font-weight:normal;'> - Your query has failed.</label> </div>");
			out_assignment.println("<br/><div style = 'font-weight: bold'>Marks: <label style = 'color:red;'>"+marks+"</label></div>"); 
			//out_assignment.println("<div style = 'font-weight: bold'>Status: <label style = 'color:red'>Incorrect  </label><label style='font-weight:normal;'> Some parsing error occurred. Please check the answer.</label> </div>");
		}  
		else if(status.equals("passed")){
			//This part of code wont be reached. This can be removed after proper testing
			out_assignment.println("<div style = 'font-weight: bold'>Status: <label style = 'color:green'> Ok </label><label style='font-weight:normal;'> - Your query has passed the test cases.</label> </div>");
		}
		else if(status.equals("Failed")){
			out_assignment.println("<br/><div style = 'font-weight: bold'>Status: <label style = 'color:red;'>Incorrect</label></div>");
			out_assignment.println("<br/><div style = 'font-weight: bold'>Marks: <label style = 'color:red;'>"+marks+"</label></div>"); 
		}
		out_assignment.println("<br/>");
		if(status == null || (status != null && status.isEmpty()) || (status != null && status.equalsIgnoreCase("error"))){
			status ="Incorrect";
		}

		if(status.equals("Failed")){
				ArrayList <String>instructorQueries = new ArrayList<String>(); 
				String out = "";
				// Show instructor answer and partial marks awarded and then on click of I give up link, show the partial mark details and DS0.
				try(PreparedStatement stment=dbCon.prepareStatement("select sql from xdata_instructor_query where course_id = ? and assignment_id=? and question_id=? ")){
			        stment.setString(1, course_id);
			        stment.setInt(2, assignment_id);
			        stment.setInt(3, question_id); 
			        out_assignment.println("<div>");
					//out_assignment.println("<a class='showhidelink' href = 'javascript:void(0);' onclick=\"toggleInstrAnswer('#answer')\">I give it up! Show me the answer</a>");
			        
			        //Show Failed Datasets, student and instructor result against failed DS.
			        out_assignment.println(this.showFailedDataSets(failedDS,testcon,dbCon,assignment_id,question_id,course_id));
			        out_assignment.println("<br/><input type='button' style='float:center;display:block;'  id='showAnswer' value='I give it up! Show me the answer'>");
					out_assignment.println("<div class='detail' id='answer'>");
					
					 out = "<p align=\"left\"> <strong>Instructor's Answer: </strong>";
					 
						try(ResultSet rs2 = stment.executeQuery()){
							while(rs2.next()){
								out += "<pre><code class=\"sql\">"+CommonFunctions.encodeHTML(rs2.getString("sql"))+"</code></pre>";
								instructorQueries.add(rs2.getString("sql"));
							}
							out += "</p>";
							//out_assignment.println(out);
						}
		     
			 out += this.getPartialMarkDetails(instructorQueries, studAnswer);
			//out += this.showFailedDataSets(failedDS,testcon,dbCon,assignment_id,question_id,course_id);
			 
			 //Then close the toggling DIV
			 out += "</div></div>";
			 out_assignment.println(out);

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
		+"}"+" $(document).on('click','#showAnswer',function (event) { event.preventDefault();$('#answer').show(); $('#showAnswer').hide(); });"
			
		+"</script>"+
		"<link rel=\"canonical\" href=\"http://www.wufoo.com/gallery/designs/template.html\">"+
		"<style> html,body {background: #fff;} fieldset {background: #f2f2e6; padding: 10px;	border: 1px solid #fff;	border-color: #fff #666661 #666661 #fff;	margin-bottom: 36px;}"+
		"#breadcrumbs{  position: absolute;  padding-left:10px;  padding-right:10px;  left: 5px;  top: 10px;  font: 13px/13px Arial, Helvetica, sans-serif;  background-color: #f0f0f0;  font-weight: bold;}</style>"+
		"</head>"+

		"<body id=\"public\">");
		String studAnswer = CommonFunctions.decodeURIComponent((String)request.getParameter("query"));
		FailedDataSetValues failedDS = (FailedDataSetValues)session.getAttribute("failedDS");
		out_assignment.println("<div id=\"fieldset\">"+ 
				"<form class=\"wufoo\" action=\"LoginChecker\" method=\"post\">"+

					"<div class=\"info\">"+
					"<h2>Question: "+question_id+"</h2>"+
					"</div>"   
					+"<p align=\"left\"> <strong> Your Answer: </strong>"+ "<pre><code class=\"sql\">"+CommonFunctions.encodeHTML(CommonFunctions.decodeURIComponent((String)request.getParameter("query")))+"</code></pre></p>");

			
		if(status.equals("Error")){
			out_assignment.println("<div style = 'font-weight: bold'>Status: <label style = 'color:red;'>Error</label></div>");
			out_assignment.println("<br/><div style = 'font-weight:bold'>Message: <span style='font-weight:normal;'>Sorry, your query could not be executed. Please check the syntax and try again.</span></div>");
			String message = request.getParameter("Error");
			
			if(!message.isEmpty()){
				out_assignment.println("<br/><div style = 'font-weight:bold'> Details: <span style='font-weight:normal;'>" + CommonFunctions.decodeURIComponent(message) + "</span></div>");
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
		else if(!learningMode && status.equals("Failed")){
			out_assignment.println("<div style = 'font-weight: bold'>Status: <label style = 'color:red'>Incorrect  </label><label style='font-weight:normal;'> - Your query has failed.</label> </div>");
			out_assignment.println("<br/><div style = 'font-weight: bold'>Marks: <label style = 'color:red;'>"+marks+"</label></div>"); 
			//out_assignment.println("<div style = 'font-weight: bold'>Status: <label style = 'color:red'>Incorrect  </label><label style='font-weight:normal;'> Some parsing error occurred. Please check the answer.</label> </div>");
		}  
		else if(status.equals("passed")){
			//This part of code wont be reached. This can be removed after proper testing
			out_assignment.println("<div style = 'font-weight: bold'>Status: <label style = 'color:green'> Ok </label><label style='font-weight:normal;'> - Your query has passed the test cases.</label> </div>");
		}
		else if(status.equals("Failed")){
			out_assignment.println("<br/><div style = 'font-weight: bold'>Status: <label style = 'color:red;'>Incorrect</label></div>");
			out_assignment.println("<br/><div style = 'font-weight: bold'>Marks: <label style = 'color:red;'>"+marks+"</label></div>"); 
		}
		out_assignment.println("<br/>");
		if(status == null || (status != null && status.isEmpty()) || (status != null && status.equalsIgnoreCase("error"))){
			status ="Incorrect";
		}

		if(status.equals("Failed")){
				ArrayList <String>instructorQueries = new ArrayList<String>(); 
				String out = "";
				// Show instructor answer and partial marks awarded and then on click of I give up link, show the partial mark details and DS0.
				try(PreparedStatement stment=dbCon.prepareStatement("select sql from xdata_instructor_query where course_id = ? and assignment_id=? and question_id=? ")){
			        stment.setString(1, course_id);
			        stment.setInt(2, assignment_id);
			        stment.setInt(3, question_id); 
			        out_assignment.println("<div>");
					//out_assignment.println("<a class='showhidelink' href = 'javascript:void(0);' onclick=\"toggleInstrAnswer('#answer')\">I give it up! Show me the answer</a>");
			        
			        //Show Failed Datasets, student and instructor result against failed DS.
			        out_assignment.println(this.showFailedDataSets(failedDS,testcon,dbCon,assignment_id,question_id,course_id));
			        out_assignment.println("<br/><input type='button' style='float:center;display:block;'  id='showAnswer' value='I give it up! Show me the answer'>");
					out_assignment.println("<div class='detail' id='answer'>");
					
					 out = "<p align=\"left\"> <strong>Instructor's Answer: </strong>";
					 
						try(ResultSet rs2 = stment.executeQuery()){
							while(rs2.next()){
								out += "<pre><code class=\"sql\">"+CommonFunctions.encodeHTML(rs2.getString("sql"))+"</code></pre>";
								instructorQueries.add(rs2.getString("sql"));
							}
							out += "</p>";
							//out_assignment.println(out);
						}
		     
			 out += this.getPartialMarkDetails(instructorQueries, studAnswer);
			//out += this.showFailedDataSets(failedDS,testcon,dbCon,assignment_id,question_id,course_id);
			 
			 //Then close the toggling DIV
			 out += "</div></div>";
			 out_assignment.println(out)
				/** taken from PartialMarkDemo Page - End**/	
       //	String out_assignment="";
		/*out_assignment += "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""+
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
		"<link rel=\"stylesheet\" href=\"../highlight/styles/default.css\">"+*/
       	String out_assignment="";
out_assignment += "<link rel=\"stylesheet\" href=\"../highlight/styles/xcode.css\">  "+
		"<link rel=\"stylesheet\" href=\"../highlight/styles/default.css\">" +
		"<script src=\"../highlight/highlight.pack.js\"></script> "
		+"<script type=\"text/javascript\">" 
		+  "hljs.initHighlighting.called = false;hljs.initHighlighting();" 
		+"function toggleRefTables(id){"
			+"$(id).toggle();"
 
			+"if($(id).parent().children()[0].innerHTML==\"View Referenced Tables\"){"
				+"$(id).parent().children()[0].innerHTML=\"Hide Referenced Tables\";"
			+"}"
			+"else{"
				+"$(id).parent().children()[0].innerHTML=\"View Referenced Tables\";"
			+"}"
		+"}"+" $(document).on('click','#showAnswer',function (event) { event.preventDefault();$('#answer').show(); $('#showAnswer').hide(); });"
		+" $(document).on('click','#passedDataSets',function (event) " +
		"{ event.preventDefault();$('#ShowPassedDataSets').show(); " +
		"$('html,body').animate({ scrollTop: $('#ShowPassedDataSets').offset().top-10});     " +
				"});"
		+"</script>";
		
		//"</head>"+

		//"<body id=\"public\">";
		String studAnswer = CommonFunctions.decodeURIComponent((String)request.getParameter("query"));
		FailedDataSetValues failedDS = (FailedDataSetValues)session.getAttribute("failedDS");
		out_assignment += "<div class=\"fieldset\"><legend>Result</legend><fieldset>"+ 
				"<div><div class=\"info\">"+
					"<h2>Question: "+question_id+"</h2>"+
					"</div>"   
					+"<p align=\"left\"> <strong> Your Answer: </strong>"+ "<pre><code class=\"sql\">"+CommonFunctions.encodeHTML(CommonFunctions.decodeURIComponent((String)request.getParameter("query")))+"</code></pre></p>";

		if(status.equals("Error")){
			out_assignment += "<div style = 'font-weight: bold'>Status: <label style = 'color:red;'>Error</label></div>";
			out_assignment += "<br/><div style = 'font-weight:bold'>Message: <span style='font-weight:normal;'>Sorry, your query could not be executed. Please check the syntax and try again.</span></div>";
			String message = request.getParameter("Error");
			
			if(!message.isEmpty()){
				out_assignment += "<br/><div style = 'font-weight:bold'> Details: <span style='font-weight:normal;'>" + CommonFunctions.decodeURIComponent(message) + "</span></div>";
			}
		}
		if(status.equals("NoDataset")){
			out_assignment += "<div style = 'font-weight: bold'>Status: <label style = 'color:red;'>Error</label></div>";
			out_assignment += "<br/><div style = 'font-weight:bold'>Message: <span style='font-weight:normal;'>Not answered</span></div>";
			String message = "Please answer the question.";
			 
			if(!message.isEmpty()){
				//out_assignment.println("<br/><div style = 'font-weight:bold'> Error Message: <span style='font-weight:normal;'>" + CommonFunctions.decodeURIComponent(message) + "</span></div>");
			}
		} 
		else if(!learningMode && status.equals("Failed")){
			out_assignment += "<div style = 'font-weight: bold'>Status: <label style = 'color:red'>Incorrect  </label><label style='font-weight:normal;'> - Your query has failed.</label> </div>";
			out_assignment += "<br/><div style = 'font-weight: bold'>Marks awarded: <label style = 'color:red;'>"+marks+"</label><label style='font-weight:normal;font-size:12;'> - Details shown with correct answer</label></div>"; 
			//out_assignment.println("<div style = 'font-weight: bold'>Status: <label style = 'color:red'>Incorrect  </label><label style='font-weight:normal;'> Some parsing error occurred. Please check the answer.</label> </div>");
		}  
		else if(status.equals("passed")){
			//This part of code wont be reached. This can be removed after proper testing
			out_assignment += "<div style = 'font-weight: bold'>Status: <label style = 'color:green'> Ok </label><label style='font-weight:normal;'> - Your query has passed the test cases.</label> </div>";
		}
		else if(status.equals("Failed")){
			out_assignment += "<br/><div style = 'font-weight: bold'>Status: <label style = 'color:red;'>Incorrect</label></div>";
			out_assignment += "<br/><div style = 'font-weight: bold'>Marks awarded: <label style = 'color:red;'>"+marks+"</label><label style='font-weight:normal;font-size:12;'> - Details shown with correct answer</label></div>"; 
		}
		out_assignment += "<br/>";
		if(status == null || (status != null && status.isEmpty()) || (status != null && status.equalsIgnoreCase("error"))){
			status ="Incorrect";
		}

		if(status.equals("Failed")){
				ArrayList <String>instructorQueries = new ArrayList<String>(); 
				String out = "";
				// Show instructor answer and partial marks awarded and then on click of I give up link, show the partial mark details and DS0.
				try(PreparedStatement stment=dbCon.prepareStatement("select sql from xdata_instructor_query where course_id = ? and assignment_id=? and question_id=? ")){
			        stment.setString(1, course_id);
			        stment.setInt(2, assignment_id);
			        stment.setInt(3, question_id); 
			        out_assignment += "<div>";
					//out_assignment.println("<a class='showhidelink' href = 'javascript:void(0);' onclick=\"toggleInstrAnswer('#answer')\">I give it up! Show me the answer</a>");
			        
			        //Show Failed Datasets, student and instructor result against failed DS.
			    	out_assignment += "<h3><a href='#passedDataSets' id='passedDataSets' style='color:green;'>View passed datasets</a></h3>";
			        out_assignment += this.showFailedDataSets(failedDS,testcon,dbCon,assignment_id,question_id,course_id);
			           
			        out_assignment += "<br/><input type='button' style='float:center;display:block;color:blue;font-weight:bold;background-color:#blue;'  id='showAnswer' value='I give it up! Show me the answer'/><br/>";
			        out_assignment += this.showPassedDataSets(failedDS,testcon,dbCon,assignment_id,question_id,course_id);
				    
					out_assignment += "<div class='detail' id='answer'>";
					
					
					 
						try(ResultSet rs2 = stment.executeQuery()){
							while(rs2.next()){
								 out = "<p align=\"left\"> <strong>Instructor's Answer: </strong>";
								out += "<pre><code class=\"sql\">"+CommonFunctions.encodeHTML(rs2.getString("sql"))+"</code></pre>";
								instructorQueries.add(rs2.getString("sql"));
								out += "</p>";
							}
							
							out_assignment += out;
						}
		     
			 out = this.getPartialMarkDetails(instructorQueries, studAnswer);
			//out += this.showFailedDataSets(failedDS,testcon,dbCon,assignment_id,question_id,course_id);
			  
			 //Then close the toggling DIV
			 out += "</div></div>";
			 out_assignment += out;
				
				 }catch (Exception e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
						e.printStackTrace();
				}finally{
					try{
					dbCon.close();
					testcon.close();
					}catch(Exception e){
						e.printStackTrace();	
					}
				}
		}
		
		//out_assignment += "</form></div><!-- End Page Content --></body></html>";
		out_assignment += "</fieldset></div>";
		response.getWriter().write(out_assignment);	
		
		//out_assignment.close();

	//	session.removeAttribute("dbConn");
	//	session.removeAttribute("testConn");
	//	session.removeAttribute("displayTestCase");
		

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	
	
	/**
	 * This function compares the items in the list and returns required html element
	 * if they are same (with default color  black)/ different(with color red)
	 * 
	 * @param list1
	 * @param list2
	 * @return
	 */
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
	
	/**
	 * Same as previous function, but compares array list of Nodes
	 * @param list1
	 * @param list2
	 * @return
	 */
	public String listToString(ArrayList <Node> list1, ArrayList<Node> list2){
		String ret = "<ul>";
		for(Node s:list1){
			if(list2.toString().contains(s.toString())){
				ret += "<li>" + s + "</li>";
			}else{
				ret += "<li style='color:red;'>" + s + "</li>";
			}		 
		}
		ret += "</ul>";
		return ret;
	}

	/**
	 * This method is used to round of marks
	 * @param marks
	 * @return
	 */
    public float roundToDecimal(float marks){
	return BigDecimal.valueOf(marks).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
}

    /**
     * This method returns a partial mark details string that holds the HTML data to display partail mark details
     * @param instructorQueries
     * @param studAnswer
     * @return
     */
    public String getPartialMarkDetails(ArrayList <String> instructorQueries, String studAnswer) throws Exception{
    		
    	  /** taken from PartialMarkDemo Page - Start to show partial mark details for unsaved query **/
		TestPartialMarking testObj=new TestPartialMarking();
		//Call partial mark with instructor and student query and display the details on click of show me! I give up link. Dont toggle
		testObj.StudentQuery=testObj.processCanonicalize(testObj.StudentQuery, studAnswer);
		QueryData bestInstructorQueryData=null;
		String bestInstructorQueryString="";
		float marks = 0.0f;
		String out = "";
		int i = 0;
		
		for(String instQuery:instructorQueries){
			
			testObj.InstructorQuery=testObj.processCanonicalize(testObj.InstructorQuery, instQuery);		
			//Initialize the values
			if(i == 0 ){
				bestInstructorQueryData=testObj.InstructorQuery.OuterQuery;
				bestInstructorQueryString=instQuery;
			}
			Float studMarks=testObj.calculateScore(false, testObj.InstructorQuery.OuterQuery, testObj.StudentQuery.OuterQuery, 0).Marks;
			Float instMarks=testObj.calculateScore(false, testObj.InstructorQuery.OuterQuery, testObj.InstructorQuery.OuterQuery, 0).Marks;
			i++;
			Float newMarks=studMarks*100/instMarks;

			if(newMarks> marks){
				marks=newMarks;
				bestInstructorQueryData=testObj.InstructorQuery.OuterQuery;
				bestInstructorQueryString=instQuery;
			}
			
		}
		QueryData instrData = bestInstructorQueryData;
		QueryData studentData = testObj.StudentQuery.OuterQuery;
		
		
		out += "<div  style='background-color:#FFF'>";
		out +="<br/>";
		out+="<table class='queryTable' width='70%' cellpadding='3' cellspacing='1'><tr>"+
					"<th width='20%'>&nbsp;</th><th width='20%' align='center'>Student</th><th width='20%' align='center'>Instructor</th></tr>";
		
		if( (instrData != null && instrData.getRelations().size() > 0)
			|| (studentData != null && studentData.getRelations().size() > 0)){
			out += "<tr><td class='emph''>Relations</td>" +
					"<td width=\"20%\">"+listToString(studentData.getRelations(),instrData.getRelations())+"</td>"+
					"<td width=\"20%\">"+listToString(instrData.getRelations(), studentData.getRelations())+"</td></tr>";
		
		}
		
		if( (instrData != null && instrData.getProjectionList().size() > 0)
	  			|| (studentData != null && studentData.getProjectionList().size() > 0)){
			out += "<tr><td class='emph''>Projections</td>" +
							"<td width=\"20%\">"+listToString(studentData.getProjectionList(),instrData.getProjectionList())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData.getProjectionList(), studentData.getProjectionList())+"</td></tr>";
	  		
				}
		
		if( (instrData != null && instrData.hasDistinct)
	  			|| (studentData != null && studentData.hasDistinct)){
			out += "<tr><td class='emph''>Distinct</td>" ;
			
			int instDistinct = 0;
			int studDistinct = 0;
			if(instrData.hasDistinct){
				instDistinct =1;
			}if(studentData.hasDistinct){
				studDistinct = 1;
			}
			if(studentData.hasDistinct && !instrData.hasDistinct){
				
				out += "<td width=\"20%\" align='center' class=\"number\" style=\"color: red;\">"+studDistinct+"</td>";
			}else{
				out += "<td width=\"20%\" align='center' class='number'>"+studDistinct+"</td>";
			}
				

			if((instrData.hasDistinct && !studentData.hasDistinct)){
				out += "<td width=\"20%\" align='center' class=\"number\" style=\"color: red;\">"+instDistinct+"</td></tr>";
				
			}else{
				out += "<td width=\"20%\" align='center' class='number'>"+instDistinct+"</td></tr>";
			}
			}
		
		
		if( (instrData != null && instrData.GroupByNodes.size() > 0)
	  			|| (studentData != null && studentData.GroupByNodes.size() > 0)){
			out += "<tr><td class='emph''>Group By</td>" +
							"<td width=\"20%\">"+listToString(studentData.GroupByNodes,instrData.GroupByNodes)+"</td>"+
							"<td width=\"20%\">"+listToString(instrData.GroupByNodes, studentData.GroupByNodes)+"</td></tr>";
	  		
			}
			
			if( (instrData != null && instrData.orderByNodes.size() > 0)
	  			|| (studentData != null && studentData.orderByNodes.size() > 0)){
			
				out += "<tr><td class='emph''>Order By</td>" +
							"<td width=\"20%\">"+listToString(studentData.orderByNodes,instrData.orderByNodes)+"</td>"+
							"<td width=\"20%\">"+listToString(instrData.orderByNodes, studentData.orderByNodes)+"</td></tr>";
			}
			
			if( (instrData != null && instrData.getHavingClause().size() > 0)
	  			|| (studentData != null && studentData.getHavingClause().size() > 0)){
				out += "<tr><td class='emph''>Having Clause</td>" +
						  "<td width=\"20%\">"+listToString(studentData.getHavingClause(),instrData.getHavingClause())+"</td>"+
						  "<td width=\"20%\">"+listToString(instrData.getHavingClause(), studentData.getHavingClause())+"</td></tr>";

	}
		if( (instrData != null && instrData.getSubQConnectives().size() > 0)
	  			|| (studentData != null && studentData.getSubQConnectives().size() > 0)){
	  			
			out += "<tr><td class='emph''>SubQuery Connectives</td>" +
							"<td width=\"20%\">"+listToString(studentData.getSubQConnectives(),instrData.getSubQConnectives())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData.getSubQConnectives(), studentData.getSubQConnectives())+"</td></tr>";
	  			
	  			}
	
	if( (instrData != null && instrData.getSetOpetators().size() > 0)
	  			|| (studentData != null && studentData.getSetOpetators().size() > 0)){
	  			
		out += "<tr><td class='emph''>Set Operators</td>" +
							"<td width=\"20%\">"+listToString(studentData.getSetOpetators(),instrData.getSetOpetators())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData.getSetOpetators(), studentData.getSetOpetators())+"</td></tr>";
	  			
	  			
	  			}
	if( (instrData != null && instrData.getSelectionConditions().size() > 0)
	  			|| (studentData != null && studentData.getSelectionConditions().size() > 0)){
	  			
		out += "<tr><td class='emph''>Selection Conditions</td>" +
							"<td width=\"20%\">"+listToString(studentData.getSelectionConditions(),instrData.getSelectionConditions())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData.getSelectionConditions(), studentData.getSelectionConditions())+"</td></tr>";
	  		
	  		
	  			}
	
	if( (instrData != null && instrData.getJoinTables().size() > 0)
			  			|| (studentData != null && studentData.getJoinTables().size() > 0)){
			  			
		out += "<tr><td class='emph''>Join Tables</td>" +
									"<td width=\"20%\">"+listToString(studentData.getJoinTables(),instrData.getJoinTables())+"</td>"+
									"<td width=\"20%\">"+listToString(instrData.getJoinTables(), studentData.getJoinTables())+"</td></tr>";
			  		
			  		
			  			}
			  			
	if( (instrData != null && instrData.getJoinConditions().size() > 0)
			  			|| (studentData != null && studentData.getJoinConditions().size() > 0)){
			
		out += "<tr><td class='emph'>Join Conditions </td>" +
									"<td width=\"20%\">"+listToString(studentData.getJoinConditions(),instrData.getJoinConditions())+"</td>"+
									"<td width=\"20%\">"+listToString(instrData.getJoinConditions(), studentData.getJoinConditions())+"</td></tr>";
			  		
			  		}
	out += "</table></div>";
	
	return out;
    }
    
    
    /**
     * This method is used to show the failed datasets 
     * 
     * @param failedDSValues
     * @param testcon
     * @param dbCon
     * @param assignment_id
     * @param question_id
     * @param course_id
     * @return
     * @throws Exception
     */
    public String showFailedDataSets(FailedDataSetValues failedDSValues, Connection testcon, Connection dbCon, int assignment_id, 
    		int question_id, String course_id) throws Exception{
    	
    	String out_assignment = "";
    	String sel_dataset = "select tag,value from xdata_datasetvalue where datasetid =? and assignment_id= ? and question_id=? and query_id=?";
    	//logger.log(Level.FINE,"Ans : "+ ans);
		out_assignment += "<h3>"+"Your query did not match the following datasets."+"</h3><hr>";
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
								out_assignment += "<div style='align:left;'><h4>"+resultSet1.getString("tag")+"</h4>";
								out_assignment += "</div>";
							}
						}
					}
					//If the DataSet is not generated, then show sample data
					else{
							out_assignment += "<div style='align:left;'><h4>Default sample data Id:"+dataSetId+"</h4>";
							out_assignment += "</div>";
						}
			}
				
			 out_assignment += "";
		     out_assignment +="<div style='margin-right: 40%;'>";
		     out_assignment += "<span> <b>Your Result</b></span>";
		     out_assignment += "<table border=\"1\">";
		     out_assignment += "<tr>";
		     Map<String, ArrayList<String>> studDataFailed = failedDSValues.getStudentQueryOutput().get(dataSetId);
		     Iterator it = studDataFailed.keySet().iterator();
		     while(it.hasNext()){ 
		    	 out_assignment += "<th>"+ (String)it.next()+"</th>";			    	 
		     }
		     out_assignment += "</tr>";
		     it = studDataFailed.keySet().iterator();
		     int colSize = studDataFailed.keySet().size();
		     int valSize = 0;
		     out_assignment += "<tr>";
			     while(it.hasNext()){
			    	 int valCnt = 0;
			    	 ArrayList Values = studDataFailed.get(it.next());
			    	 valSize = Values.size();
			     }
			     if(valSize== 0){
			    	 while(it.hasNext()){
			    		 out_assignment += "<td></td>";
			    	 }
			    	   out_assignment += "</tr>";
			     }else{
				     for(int v = 0 ; v< valSize;v++){
				    	 it = studDataFailed.keySet().iterator();
					     while(it.hasNext()){
					    	 int valCnt = 0;
					    	 ArrayList Values = studDataFailed.get(it.next());
					    	 out_assignment += "<td>"+Values.get(v)+"</td>";
					     }
					     out_assignment += "</tr>";
				     }
			     }
   
		     out_assignment += "</table>";
		     out_assignment += "</div>";
		     out_assignment += "<p></p>";
		     /**    SHOW EXPECTED RESULT    **/
		     out_assignment += "<div style='margin-right: 20%;'>";
		     out_assignment += "<span><b> Expected Result</b></span>";
		     out_assignment += "<table border=\"1\">";
		     out_assignment += "<tr>";
		     Map<String, ArrayList<String>> instrDataFailed = failedDSValues.getInstrQueryOutput().get(dataSetId);
		     Iterator it1 = instrDataFailed.keySet().iterator();
		     while(it1.hasNext()){ 
		    	 out_assignment += "<th>"+ (String)it1.next()+"</th>";			    	 
		     }
		     out_assignment += "</tr>";
		     it1 = instrDataFailed.keySet().iterator();
		     int colSize1 = instrDataFailed.keySet().size();
		     int valSize1 = 0;
		     out_assignment += "<tr>";
			     while(it1.hasNext()){
			    	 int valCnt = 0;
			    	 ArrayList Values = instrDataFailed.get(it1.next());
			    	 valSize1 = Values.size();
			     }
			     if(valSize1 == 0){
			    	 while(it1.hasNext()){
			    		 out_assignment += "<td></td>";
			    	 } 
			    	  out_assignment += "</tr>";
			     }else{
				     for(int v = 0 ; v< valSize1;v++){
				    	 it1 = instrDataFailed.keySet().iterator();
					     while(it1.hasNext()){						    	
					    	 ArrayList Values = instrDataFailed.get(it1.next());
					    	 out_assignment += "<td>"+Values.get(v)+"</td>";
					     }
					     out_assignment += "</tr>"; 
				     }
				    
			     }
			     out_assignment += "</table>";
			     out_assignment += "</div>";
		     out_assignment += "<p></p>";
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
						 
							out_assignment += "<div style='margin-right: 20%;'>";
							out_assignment += "<a class='showhidelink' href = 'javascript:void(0);' onclick=\"toggleDataset('#"+dataSetId+"')\">View Dataset</a>";
							out_assignment += "<div class='detail' id='"+dataSetId+"'>";
							boolean refTableExists = false;
							for(int i = 0 ; i < dsList.size();i++ ){
								DataSetValue dsValue = (DataSetValue)dsList.get(i);
								String tname = "",values;
								
								if(dsValue.getFilename().contains(".ref")){
									refTableExists = true;
								}
								if(!(dsValue.getFilename().contains(".ref"))){
									tname = dsValue.getFilename().substring(0,dsValue.getFilename().indexOf(".copy"));
								
								//tname = dsValue.getFilename().substring(0,dsValue.getFilename().indexOf(".copy"));
								PreparedStatement detailStmt = testcon.prepareStatement("select * from " + tname + " where 1 = 0");
								ResultSetMetaData columnDetail = detailStmt.executeQuery().getMetaData();
								out_assignment += "<table border=\"1\">";
								out_assignment += "<caption>"+tname+"</caption>";
								out_assignment +="<tr>";
								//Column names get from metadata
								 for(int cl=1; cl <= columnDetail.getColumnCount(); cl++)
								    {
									 out_assignment += "<th>" + columnDetail.getColumnLabel(cl)+"</th>";
								  } 
								 out_assignment += "</tr>";
								//Get Column values
								for(String dsv: dsValue.getDataForColumn()){
									String columns[]=dsv.split("\\|");
									out_assignment += "<tr>";
									for(String column: columns)
									{
										out_assignment += "<td>"+column+"</td>";
									}
									out_assignment += "</tr>";
								}
								
								detailStmt.close();
							} 
							}
							
							out_assignment += "</table>";
							  out_assignment += "<p></p>";
							  
							  //***To show referenced tables
								/**Code to toggle Reference Tables **/
								if(refTableExists){
									
									out_assignment += "<p></p><div style='margin-right: 0%;'>";
									out_assignment += "<a class='showhidelink' href = 'javascript:void(0);' onclick=\"toggleRefTables('#"+failedDSValues.getQuery_id()+"R"+dataSetId+"')\">View Referenced Tables</a>";
				
									out_assignment += "<p></p><div class='detail' id='"+failedDSValues.getQuery_id()+"R"+dataSetId+"'>";
								
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
										
										out_assignment += "<table border=\"1\">";
										out_assignment += "<caption>"+tname+"</caption>";
										out_assignment += "<tr>";
										//Column names get from metadata
										 for(int cl=1; cl <= columnDetail.getColumnCount(); cl++)
										    {
											 out_assignment += "<th>"+columnDetail.getColumnLabel(cl)+"</th>";
										 } 
										out_assignment += "</tr>";
										//Get Column values
										for(String dsv: dsValue.getDataForColumn()){
											String columns[]=dsv.split("\\|");
											out_assignment += "<tr>";
											for(String column: columns)
											{
												out_assignment += "<td>"+column+"</td>";
											}
											out_assignment += "</tr>";
										}													
									detailStmt.close();
									 
									 out_assignment += "</table>";
								}
								}
								out_assignment +="</div></div>";
								}
								out_assignment += "</div></div>";
							}
							//out_assignment += "</div></div>";
							}//rsi ends
						}//pstmt ends
				     } else{
				    	 //If dsId is sampledataId, then show sample data as tables
					out_assignment += "<div style='margin-right: 20%;'>";
					out_assignment += "<a class='showhidelink' href = 'javascript:void(0);' onclick=\"toggleDataset('#"+dataSetId+"')\">View Dataset</a>";
				
					out_assignment += "<div class='detail' id='"+dataSetId+"'>";
					
					populateTestData.deleteAllTablesFromTestUser(testcon);
					populateTestData.createTempTableWithDefaultData(dbCon, testcon, assignment_id, question_id, course_id, dataSetId);			    	 
		        	PreparedStatement p1 = testcon.prepareStatement("drop table if exists DATASET");
		            p1.execute();
					
					DatabaseMetaData meta = testcon.getMetaData();
					String tableFilter[] = {"TEMPORARY TABLE"};
                    
		            ResultSet r = meta.getTables(dbCon.getCatalog(), "", "%", tableFilter);
		            while (r.next()) {                          
		               String tableName = r.getString("TABLE_NAME").toUpperCase();
		               System.out.println("Table : "+ tableName);
		               if(!tableName.equalsIgnoreCase("dataset")){ 
		               PreparedStatement p = testcon.prepareStatement("select * from "+tableName);
		              
		               ResultSetMetaData columnDetail = p.executeQuery().getMetaData();
						out_assignment += "<table border=\"1\">";
						out_assignment += "<caption>"+tableName+"</caption>";
						out_assignment += "<tr>";
						//Column names get from metadata
						 for(int cl=1; cl <= columnDetail.getColumnCount(); cl++)
						    {
							 out_assignment += "<th>" + columnDetail.getColumnLabel(cl)+"</th>";
						  } 
						 out_assignment +="</tr>";
						  ResultSetMetaData columnDetail1 = p.executeQuery().getMetaData();
						  ResultSet rrset = p.executeQuery();
						 while(rrset.next()){
							 out_assignment += "<tr>";
							 for(int cl=1; cl <= columnDetail1.getColumnCount(); cl++)
							    {	
								 out_assignment += "<td>"+rrset.getString(cl)+"</td>";
							    }
							 out_assignment += "</tr>";
							 }
						    }
		        	out_assignment += "</table>";
		    		out_assignment += "<p></p>";	
		            }
		            out_assignment += "</div></div>";
		            }			     
		     out_assignment += "<hr>";
		     //out_assignment += "</div>";
		}
		 
		 out_assignment += "</div></div>";
	     out_assignment += "<p></p>";					 
		//out_assignment += "<hr>";
		
		return out_assignment;
    }
    
    
    
    
    
    /**
     * This method is used to show the passed datasets for guest user 
     * 
     * @param failedDSValues
     * @param testcon
     * @param dbCon
     * @param assignment_id
     * @param question_id
     * @param course_id
     * @return
     * @throws Exception
     */
    public String showPassedDataSets(FailedDataSetValues failedDSValues, Connection testcon, Connection dbCon, int assignment_id, 
    		int question_id, String course_id) throws Exception{
    	
    	String out_assignment = "";
    	String sel_dataset = "select tag,value,datasetid from xdata_datasetvalue where course_id=? and assignment_id= ? and question_id=? and query_id=?";
    	//logger.log(Level.FINE,"Ans : "+ ans);
    	out_assignment += "<a name='passedDataSets'></a><div id=\"ShowPassedDataSets\" style='display:none;'><h3>"+"Your query matches the following datasets."+"</h3>";
		out_assignment +="<hr>";
		PopulateTestDataGrading populateTestData = new PopulateTestDataGrading();
		populateTestData.deleteAllTempTablesFromTestUser(testcon);
		populateTestData.createTempTables(testcon, assignment_id, question_id);
		 
		ArrayList <String> failedDataSets = failedDSValues.getDataSetIdList();
		try(PreparedStatement stmt2=dbCon.prepareStatement(sel_dataset)){
			stmt2.setString(1, course_id);
			stmt2.setInt(2,assignment_id); 
			stmt2.setInt(3,question_id);
			stmt2.setInt(4,1); 
			
				//If it is dataset generated by XData
				try(ResultSet resultSet1 = stmt2.executeQuery()){ 
					while(resultSet1.next()){				 
						
						String dataSetId = resultSet1.getString("datasetid");
						boolean refTableExists = false;
						//When DataSet is not in Failed DataSet List
						if(!failedDataSets.contains(dataSetId)){
							
							out_assignment += "<div style='align:left;'><h4>"+resultSet1.getString("tag")+"</h4>";
							out_assignment += "</div>";
							
								String value=resultSet1.getString("value");
								//It holds JSON obj tat has list of Datasetvalue class
								Gson gson = new Gson();
								//ArrayList dsList = gson.fromJson(value,ArrayList.class);
								Type listType = new TypeToken<ArrayList<DataSetValue>>() {}.getType();
				                    
								List<DataSetValue> dsList = new Gson().fromJson(value, listType);	
								for(int i = 0 ; i < dsList.size();i++ ){
											
										DataSetValue dsValue = (DataSetValue)dsList.get(i);
											
										out_assignment += "<div >";
										String tname = "",values;
										
										if(dsValue.getFilename().contains(".ref")){
											refTableExists = true;
										}
										if(!(dsValue.getFilename().contains(".ref"))){
												tname = dsValue.getFilename().substring(0,dsValue.getFilename().indexOf(".copy"));
											
											//tname = dsValue.getFilename().substring(0,dsValue.getFilename().indexOf(".copy"));
											PreparedStatement detailStmt = testcon.prepareStatement("select * from " + tname + " where 1 = 0");
											ResultSetMetaData columnDetail = detailStmt.executeQuery().getMetaData();
											out_assignment += "<table border=\"1\">";
											out_assignment += "<caption>"+tname+"</caption>";
											out_assignment +="<tr>";
											//Column names get from metadata
											 for(int cl=1; cl <= columnDetail.getColumnCount(); cl++)
											    {
												 out_assignment += "<th>" + columnDetail.getColumnLabel(cl)+"</th>";
											  } 
											 out_assignment += "</tr>";
											//Get Column values
											for(String dsv: dsValue.getDataForColumn()){
												String columns[]=dsv.split("\\|");
												out_assignment += "<tr>";
												for(String column: columns)
												{
													out_assignment += "<td>"+column+"</td>";
												}
												out_assignment += "</tr>";
											}
											out_assignment += "</table>";
											 detailStmt.close();
										} 
										
										  out_assignment += "<p></p>";
									}
										  //***To show referenced tables
											/**Code to toggle Reference Tables **/
											if(refTableExists){
												
												out_assignment += "<p></p><div >";
												out_assignment += "<a class='showhidelink' href = 'javascript:void(0);' onclick=\"toggleRefTables('#"+failedDSValues.getQuery_id()+"R"+dataSetId+"')\">View Referenced Tables</a>";
							
												out_assignment += "<p></p><div class='detail' id='"+failedDSValues.getQuery_id()+"R"+dataSetId+"'>";
											
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
													
													out_assignment += "<table border=\"1\">";
													out_assignment += "<caption>"+tname+"</caption>";
													out_assignment += "<tr>";
													//Column names get from metadata
													 for(int cl=1; cl <= columnDetail.getColumnCount(); cl++)
													    {
														 out_assignment += "<th>"+columnDetail.getColumnLabel(cl)+"</th>";
													 } 
													out_assignment += "</tr>";
													//Get Column values
													for(String dsv: dsValue.getDataForColumn()){
														String columns[]=dsv.split("\\|");
														out_assignment += "<tr>";
														for(String column: columns)
														{
															out_assignment += "<td>"+column+"</td>";
														}
														out_assignment += "</tr>";
													}													
												detailStmt.close();
												 
												 out_assignment += "</table>";
											}
											}
											out_assignment +="</div></div>";
											}
											out_assignment += "</div><hr>";
										}
							}
		}
		
		}
		out_assignment += "</div>"; 
		return out_assignment;
    }
}

