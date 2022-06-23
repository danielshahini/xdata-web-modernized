


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import database.CommonFunctions;
import database.DatabaseConnection;

/**
 * Servlet implementation class ViewAssignment
 */
//@WebServlet("/ViewAssignment")
public class ViewAssignment extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(ViewAssignment.class.getName());
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	private Connection dbCon;
	public ViewAssignment() {
		super();
		// TODO Auto-generated constructor stub
		dbCon=null;
	}

	@Override
	public void init(ServletConfig c) throws ServletException {
		super.init(c);
		//Open the connection here
		
	}
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */

	private void editAssignment(String assignment_id, String uname, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		String map[] = new String[100];
		HttpSession session = request.getSession(false);
		/*try {
			String submit="select * from xdata_instructor_query where user_id =? and assignment_id =?";
			try(PreparedStatement stmt=dbCon.prepareStatement(submit)){
			stmt.setString(1, uname);
			stmt.setInt(2, Integer.parseInt(assignment_id));
			logger.log(Level.FINE,"Uname is :"+uname);
			logger.log(Level.FINE,"assignment_id is :"+assignment_id);
			ResultSet rst=stmt.executeQuery();
			while(rst.next())
			{
				String temp=rst.getString("sql");
				map[rst.getInt("q_id")]=temp;
			}
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""+
					"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"+

			"<html xmlns=\"http://www.w3.org/1999/xhtml\">"+
			"<head>"+"<title>"+
			"XData &middot; Assignment"+"</title>"+
			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"+
			"<script type=\"text/javascript\" src=\"scripts/wufoo.js\"></script>"+
			"<link rel=\"stylesheet\" href=\"css/structure.css\" type=\"text/css\" />"+
			"<link rel=\"stylesheet\" href=\"css/form.css\" type=\"text/css\" />"+
			"<link rel=\"stylesheet\" href=\"css/theme.css\" type=\"text/css\" />"+
			"<link rel=\"canonical\" href=\"http://www.wufoo.com/gallery/designs/template.html\">"+
			"<style> html,body {background: #ccc;} fieldset { padding: 10px;	border: 1px solid #fff;	border-color: #fff #666661 #666661 #fff;	margin-bottom: 36px;}"+
			"#breadcrumbs{position: absolute;padding-left:10px;padding-right:10px;left: 5px;top: 10px;font: 13px/13px Arial, Helvetica, sans-serif;background-color: #f0f0f0;font-weight: bold;}"+
			"</style>"+"</head>"+
			"<body id=\"public\">"); 
			if(! Boolean.parseBoolean(session.getAttribute("ltiIntegration").toString())){
				out.println("<div id=\"breadcrumbs\">"+
						   "<a id=\"bcrumb_link\" style='color:#353275;text-decoration: none;' href=\"../CourseHome.jsp\" target=\"_top\">Home</a> &nbsp; >> &nbsp;"+
						   "<a  id=\"bcrumb_link\" style='color:#353275;text-decoration: none;' href=\"Student/ListAllAssignments.jsp\" target=\"_self\">Assignment List</a>&nbsp; >> &nbsp;"+
						   "<a  id=\"bcrumb_link\" style='color:#353275;text-decoration: none;' href=\"Student/ListOfQuestions.jsp?assignmentid="+request.getParameter("AssignmentID")+"\" target=\"_self\">Question List</a>&nbsp; >> &nbsp;"+
						   "<a style='color:#0E0E0E;text-decoration: none;font-weight: normal;' id=\"bcrumb_no_link\" href=\"#\">View Grade</a>"+
						   "</div> ");
			}else{
			out.println("<a  id=\"bcrumb_link\" href=\"ListOfQuestions.jsp?assignmentid="+request.getParameter("AssignmentID")+"\" target=\"_self\">Question List</a>&nbsp; >> &nbsp;"+
						"<a id=\"bcrumb_no_link\" href=\"#\">View Grade</a>");
			} 
			 
			out.println("<div><div id=\"fieldset\"><fieldset><legend> Grades - testng</legend>"+
			"<form class=\"wufoo\" action=\"SQLChecker\" method=\"get\"><div class=\"info\">"+
			"<h2>Assignment</h2></div>");
			String getSQL="SELECT * from xdata_qinfo where xdata_qinfo.assignment_id=?";
			try{
				try(PreparedStatement stmt1 =dbCon.prepareStatement(getSQL)){
					stmt1.setString(1,assignment_id);
					try(ResultSet rs1=stmt1.executeQuery()){		  
						while(rs1.next()){
							if(map[rs1.getInt("question_id")]==null)
							{
								map[rs1.getInt("question_id")]=new String("");
							}
							out.println("<label for="+rs1.getInt("question_id")+">"+rs1.getString("querytext")+"</label>");
							out.println("<br>");
							out.println("<div>");
							out.println("<textarea class=\"field textarea small\" name="+
							rs1.getInt("question_id")+">"
									+map[Integer.parseInt(rs1.getString("questionid"))]+"</textarea>");
							out.println("</div>");
							out.println("<p></p>");
						}
					}
			}
			}catch (SQLException e) {
				logger.log(Level.SEVERE,e.getMessage(),e);
				throw new ServletException(e);
			}		
			out.println("<input type=\"submit\">");
			out.println("</form></div></fieldset></div><!-- End Page Content --></body></html>");
			out.close();
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw new ServletException(e);
		} catch (IOException e1) { 
			logger.log(Level.SEVERE,e1.getMessage(),e1);
			throw new ServletException(e1);
		}*/

	}

	private void viewAssignment(int assignment_id, String uname, HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException
	{
		HttpSession session = request.getSession(false);
		session.removeAttribute("displayTCForGraded");
		response.setContentType("text/html"); 
		PrintWriter out_assignment = response.getWriter();
		out_assignment.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""+
				"\"https://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"+
				"<html xmlns=\"https://www.w3.org/1999/xhtml\">"+
				"<head><title>XData &middot; View &middot; Assignment</title>"+ 	
				"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"+
				"<link rel=\"stylesheet\" href=\"css/structure.css\" type=\"text/css\" />"
				+"<script type=\"text/javascript\" src = \"scripts/jquery.js\"></script>"
				+"<script type=\"text/javascript\" src = \"scripts/jquery-ui.min.js\"></script>"
				+"<script src=\"scripts/bootstrap/dist/js/bootstrap.js\"></script>"+
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
				"<link rel=\"stylesheet\" href=\"../highlight/styles/default.css\">"
				+"<link href=\"scripts/bootstrap/dist/css/bootstrap.css\" rel=\"stylesheet\"/>"
				+ "<script type=\"text/javascript\"> " +
				"hljs.initHighlightingOnLoad();" 
				+"$(document).ready(function () {  " 
				+"$(\"#errorModal\").on(\"show.bs.modal\", function(e) {"
				+"var link = $(e.relatedTarget);"
				+"$(this).find(\".modal-body\").load(link.attr(\"href\"));"
				+"});"
				+"$(\"#marksModal\").on(\"show.bs.modal\", function(e) {"
  					+"var link = $(e.relatedTarget);"
  					+"$(this).find(\".modal-body\").load(link.attr(\"href\"));"
  					+"});});"
  				+"</script>"+
				
				"<style>#breadcrumbs{position: absolute;padding-left:10px;padding-right:10px;left: 5px;top: 10px;font: 13px/13px Arial, Helvetica, sans-serif;background-color: #f0f0f0;font-weight: bold;}"+
				"</style></head><body>");
		
			if(! Boolean.parseBoolean(session.getAttribute("ltiIntegration").toString())){
				out_assignment.println("<div id=\"breadcrumbs\">"+
						   "<a id=\"bcrumb_link\" style='color:#353275;text-decoration: none;' href=\"../CourseHome.jsp\" target=\"_top\">Home</a> &nbsp; >> &nbsp;"+ 
						   "<a  id=\"bcrumb_link\" style='color:#353275;text-decoration: none;' href=\"Student/ListAllAssignments.jsp\" target=\"_self\">Assignment List</a>&nbsp; >> &nbsp;"+
						   "<a  id=\"bcrumb_link\" style='color:#353275;text-decoration: none;' href=\"Student/ListOfQuestions.jsp?assignmentid="+assignment_id+"\" target=\"_self\">Question List</a>&nbsp; >> &nbsp;"+
						   "<a style='color:#0E0E0E;text-decoration: none;font-weight: normal;' id=\"bcrumb_no_link\" href=\"#\">View Grade</a>"+
						   "</div> ");
			}else{
				out_assignment.println("<a  id=\"bcrumb_link\" href=\"Student/ListOfQuestions.jsp?assignmentid="+assignment_id+"\" target=\"_self\">Question List</a>&nbsp; >> &nbsp;"+
						"<a id=\"bcrumb_no_link\" href=\"#\">View Grade</a>");
			} 
			 
			out_assignment.println("<div>"+
		"<div class=\"fieldset\">"+
		"<br/><br/>"+
		"<fieldset>"+ 
		"<legend>Grades</legend><div class=\"info\">"+
			"<h2>Assignment: "+assignment_id+"</h2></div>");

		String output = "";
		try(Connection dbCon=(new database.DatabaseConnection()).dbConnection()) {
			String assignment="select * from xdata_student_queries queries natural join xdata_qinfo qinfo " +
					" where queries.assignment_id =? and " +
					"qinfo.assignment_id=? and queries.assignment_id=qinfo.assignment_id " +
					"and rollnum=? order by queries.question_id";
			
			//String marks = "Select result from score where assignment_id=? and rollnum= ? and question_id= ?";
			String marks = "Select score from xdata_student_queries where assignment_id = ? and rollnum=? and question_id=?";
			
			PreparedStatement stmt=dbCon.prepareStatement(assignment);
			stmt.setInt(1,assignment_id);
			stmt.setInt(2,assignment_id);
			stmt.setString(3, uname);
			ResultSet rs=stmt.executeQuery();
		
				output += "<table border=\"1\">";
				output += "<tr>"+
						"<th> Question Number</th>"+
						"<th> Question</th>"+
						"<th> Your response</th>"+
						"<th> Status</th>"+
						"<th> Marks Obtained </th>"+
						"<th> Max Marks Allotted</th>"+
						"<th> Test Case</th>"+
						"<th> Mark Details</th>"+
						"</tr>";
				//String path = request.getSession().getServletContext().getRealPath("/Student/StudentMarkDetails.jsp");
	  
				boolean present = false;
				
				while(rs.next())
				{
					float marksAwarded = 0.0f;
					int maxMarks = rs.getInt("totalmarks");
					PreparedStatement stmt1=dbCon.prepareStatement(marks);
					stmt1.setInt(1,assignment_id);
					stmt1.setString(2, uname);
					stmt1.setInt(3,rs.getInt("question_id"));
					ResultSet rs1=stmt1.executeQuery();
					if(rs1.next()){
						marksAwarded = rs1.getFloat("score");
					}
					
					if(rs.getString("verifiedcorrect") != null){
					
					present = true;
					String status="";
				
					if(rs.getString("verifiedcorrect") != null && rs.getBoolean("verifiedcorrect") == false)
					{
						status="Wrong";
					}
					//else if(rs.getBoolean("verifiedcorrect") == true){
					else if(rs.getString("verifiedcorrect") != null && rs.getBoolean("verifiedcorrect") == true){
						status="Correct";
					}
					
					if(status.equalsIgnoreCase("wrong"))
					{
						
						output += "<tr>"+
								"<td>Question "+rs.getInt("question_id") +"</td>"+
								"<td>"+rs.getString("querytext").replaceAll("''", "'")+"</td>"+
								"<td><pre><code class=\"sql\">"+CommonFunctions.encodeHTML(CommonFunctions.decodeURIComponent(rs.getString("querystring")))+"</code></pre></td>"+
								"<td>"+status+"</td>"+
								"<td>"+ marksAwarded+"</td>"+	
								"<td>"+ maxMarks+"</td>"+
								//"<td>"+"<a id=\"testCase\" href=\" "+request.getContextPath()+"/StudentTestCase?user_id="+uname+"&assignment_id="+assignment_id+"&status=incorrect&question_id="+rs.getInt("question_id")+"&query="+CommonFunctions.encodeURIComponent(rs.getString("querystring"))+"\" target=\"_blank\" type=\"new_tab\">Test Cases</a></td>"+
								"<td>"+"<a id=\"testCase\" href=\"StudentTestCase?user_id="+uname+"&assignment_id="+assignment_id+"&status=incorrect&question_id="+rs.getInt("question_id")+"&query="+CommonFunctions.encodeURIComponent(rs.getString("querystring"))+"\" target=\"_blank\" type=\"new_tab\"><u> Test Cases </u> </a></td>"+
								//"<a data-toggle=\"modal\" data-target=\"#errorModal\" href=\"StudentTestCase?user_id="+uname+"&assignment_id="+assignment_id+"&status=incorrect&question_id="+rs.getInt("question_id")+"&query="+CommonFunctions.encodeURIComponent(rs.getString("querystring"))+"\">Test Case</a></td>"+
								"<td>"+
								"<a id=\"marks\" href=\"StudentMarkDetails.jsp?user_id="+uname+"&assignment_id="+assignment_id+"&question_id="+rs.getInt("question_id")+"\" target=\"_blank\" type=\"new_tab\"><u> Mark Details </u> </a></td></tr>";
								//"<a data-toggle=\"modal\" data-target=\"#marksModal\" href=\"StudentMarkDetails.jsp?user_id="+uname+"&assignment_id="+assignment_id+"&question_id="+rs.getInt("question_id")+"\">Mark Details</a></td></tr>";
								session.setAttribute("displayTestCase", true);
								session.setAttribute("displayTCForGraded",true);
					}
					else if(status.equalsIgnoreCase("Correct"))
					{
						output += "<tr>"+
								"<td>Question "+rs.getInt("question_id") +"</td>"+
								"<td>"+rs.getString("querytext").replaceAll("''", "'")+"</td>"+
								"<td><pre><code class=\"sql\">"+CommonFunctions.encodeHTML(CommonFunctions.decodeURIComponent(rs.getString("querystring")))+"</code></pre></td>"+
								"<td>"+status+"</td>"+
								"<td>"+ marksAwarded +"</td>"+	
								"<td>"+ maxMarks+"</td>"+	
								"<td></td>"+
								"<td><a id=\"marks\" href=\"StudentMarkDetails.jsp?user_id="+uname+"&assignment_id="+assignment_id+"&question_id="+rs.getInt("question_id")+"\" target=\"_blank\" type=\"new_tab\">Mark Details</a></td>"+
								"</tr>";
					} 
					else{
						output += "<tr>"+
								"<td>Question "+rs.getInt("question_id") +"</td>"+
								"<td>"+rs.getString("querytext").replaceAll("''", "'")+"</td>"+
								"<td><pre><code class=\"sql\">"+CommonFunctions.encodeHTML(CommonFunctions.decodeURIComponent(rs.getString("querystring")))+"</code></pre></td>"+
								"<td>"+"Question not yet graded."+"</td>"+
								"<td></td>"+"<td></td>"+ "<td></td>"+ "<td></td>"+
								"</tr>";
					}
	 
				}
					else{
						output += "<strong>Assignment not yet graded.</strong>";
								
					}
				}
				if(present) output += "</table>";
				else output = "<strong>Not solved the assignment</strong>";
				
				out_assignment.println(output);
				
				out_assignment.println("<!-- MODAL CODE STARTS -->"
						+"<div class=\"modal fade\" id=\"errorModal\" tabindex=\"-1\" role=\"dialog\" aria-labelledby=\"errorModal\" aria-hidden=\"true\">"
						+"<div class=\"modal-dialog\">"
						+"<div class=\"modal-header\">"
						+"<button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-hidden=\"true\"><img src=\"images/close_teal.jpeg\" style=\"width:20px;height:20px;\"/></button>"
						+"<!--<h4 class=\"modal-title\" id=\"myModalLabel\">Test Cases</h4>-->"
						+"</div>"
						+"<div class=\"modal-content\">"
						
						+"<div class=\"modal-body\">"		          
						+"</div><!-- <div class=\"modal-footer\">"
						+"<button type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\">Close</button>"
						+"</div>-->"
						+"</div></div></div>"
						+"<div class=\"modal fade\" id=\"marksModal\" tabindex=\"-1\" role=\"dialog\" aria-labelledby=\"marksModal\" aria-hidden=\"true\">"
						+"<div class=\"modal-dialog\">"
						+"<div class=\"modal-header\">"
						+" <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-hidden=\"true\"><img src=\"images/close_teal.jpeg\" style=\"width:20px;height:20px;\"/></button>"
						+"<!--<h4 class=\"modal-title\" id=\"myModalLabel\">Mark Details</h4>-->"
						+"</div>"
						+"<div class=\"modal-content\">"
						
						+"<div class=\"modal-body\">"
						+"</div>"
						+"<!--  <div class=\"modal-footer\">"
						+"<button type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\">Close</button>"
						+"</div>-->"
						+"</div>"
						+"</div>"
						+"</div>");
				//</form></div>
				out_assignment.println("</fieldset></div><!-- End Page Content --></body></html>");
				out_assignment.close();
		} catch (SQLException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw new ServletException(e);
		}
	}
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		DatabaseConnection db = new DatabaseConnection();
		
		try (Connection dbCon = db.dbConnection()){
			//dbCon = db.dbConnection();
			if(dbCon!=null){
				logger.log(Level.FINE,"Connected successfullly");
			}
		}catch (Exception ex) {
			logger.log(Level.SEVERE,ex.getMessage(),ex);
			throw new ServletException(ex);
		}
		
		HttpSession session=request.getSession();
		String uname=(String) session.getAttribute("user_id");
		String assgnid = request.getParameter("assignmentid");
		String assignments="Select * from xdata_assignment";
		//Connection dbCon=(Connection) session.getAttribute("dbConnection");
		try (Connection dbCon=(new database.DatabaseConnection()).dbConnection()){
			//if(dbCon==null)
				//dbCon=(new database.DatabaseConnection()).dbConnection();
	
			PreparedStatement pstmt=dbCon.prepareStatement(assignments);
			ResultSet rs=pstmt.executeQuery();
			while(rs.next())
			{
				if(request.getParameter(rs.getString("assignment_id").trim()+"E")!=null)
				{
					session.setAttribute("assignment_id", rs.getString("assignment_id"));
					editAssignment(rs.getString("assignment_id"),uname, request, response);
				}
				//else if(request.getParameter(rs.getString("assignment_id").trim()+"V")!=null)
				else if(assgnid != null && assgnid .equals(rs.getString("assignment_id").trim()+"V"))
				{
					viewAssignment(Integer.parseInt(rs.getString("assignment_id")),uname, request,response);
				}
			}
			rs.close();
		} catch (SQLException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw new ServletException(e);
		}
		try{
			dbCon.close();
		} catch(Exception e){
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw new ServletException(e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
