

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

import database.*;

import com.google.gson.Gson;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import testDataGen.PopulateTestData;
import util.DataSetValue;
import util.FailedDataSetValues;
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
		float marks = Float.parseFloat((String)request.getParameter("marks"));
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
	    	    testcon = (new util.DatabaseConnection()).getTesterConnection(assignment_id);
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
		+"}"+"function toggleInstrAnswer(id){"+"$(id).toggle();if($(id).parent().children()[0].innerHTML==\"I give it up. Show me the answer.\"){	$(id).parent().children()[0].innerHTML=\"Hide Answer\";}"
			+" else{$(id).parent().children()[0].innerHTML=\"I give it up. Show me the answer.\";	}}"
		+"</script>"+
		"<link rel=\"canonical\" href=\"http://www.wufoo.com/gallery/designs/template.html\">"+
		"<style> html,body {background: #fff;} fieldset {background: #f2f2e6; padding: 10px;	border: 1px solid #fff;	border-color: #fff #666661 #666661 #fff;	margin-bottom: 36px;}"+
		"#breadcrumbs{  position: absolute;  padding-left:10px;  padding-right:10px;  left: 5px;  top: 10px;  font: 13px/13px Arial, Helvetica, sans-serif;  background-color: #f0f0f0;  font-weight: bold;}</style>"+
		"</head>"+

		"<body id=\"public\">");
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
		if(status == null || (status != null && status.isEmpty()) || (status != null && status.equalsIgnoreCase("error"))){
			status ="Incorrect";
		}

		try(PreparedStatement stment=dbCon.prepareStatement("select sql from xdata_instructor_query where course_id = ? and assignment_id=? and question_id=? ")){
	        stment.setString(1, course_id);
	        stment.setInt(2, assignment_id);
	        stment.setInt(3, question_id); 
				try(ResultSet rs2 = stment.executeQuery()){
					out_assignment.println("<div>");
					out_assignment.println("<a class='showhidelink' href = 'javascript:void(0);' onclick=\"toggleInstrAnswer('#answer')\">I give it up! Show me the answer</a>");
					out_assignment.println("<div class='detail' id='answer'>");
					
					String out = "<p align=\"left\"> <strong>Instructor's Answer: </strong>";
					while(rs2.next()){
						out += "<pre><code class=\"sql\">"+CommonFunctions.encodeHTML(rs2.getString("sql"))+"</code></pre>";				
					}
					out += "</p></div></div>";
					out_assignment.println(out);
				}
        }catch (Exception e) {
				logger.log(Level.SEVERE,e.getMessage(),e);
		}
		
			
		
		out_assignment.println("</form></div><!-- End Page Content --></body></html>");
		out_assignment.close();

		session.removeAttribute("dbConn");
		session.removeAttribute("testConn");
		session.removeAttribute("displayTestCase");
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
