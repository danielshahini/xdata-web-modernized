package instructor;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import database.*;

/**
 * Servlet implementation class QueryStatus
 */
//@WebServlet("/QueryStatus")
public class QueryStatus extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(QueryStatus.class.getName());
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public QueryStatus() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();
		String course_id = (String)session.getAttribute("context_label"); 
		response.setContentType("text/html");
		PrintWriter out_assignment = response.getWriter();
		out_assignment
				.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""
						+ "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
						+"<html xmlns=\"http://www.w3.org/1999/xhtml\">"
						+ "<head>"
						+"<title>"
						+ "XData &middot; Assignment"
						+ "</title>"
						+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"
						+"<script type=\"text/javascript\" src=\"scripts/wufoo.js\"></script>"
						+"<link rel=\"stylesheet\" href=\"css/structure.css\" type=\"text/css\" />"
						+"<script type=\"text/javascript\" src = \"scripts/jquery.js\"></script>"
						+"<script type=\"text/javascript\" src = \"scripts/jquery-ui.min.js\"></script>"
						+"<script src=\"scripts/bootstrap/dist/js/bootstrap.js\"></script>"
						+"<link href=\"scripts/bootstrap/dist/css/bootstrap.css\" rel=\"stylesheet\"/>"
						+ "<link rel=\"stylesheet\" href=\"highlight/styles/xcode.css\">  "
						+ "<link rel=\"stylesheet\" href=\"highlight/styles/default.css\">"
						+ "<script src=\"highlight/highlight.pack.js\"></script>"
						+ "<script type=\"text/javascript\"> hljs.initHighlightingOnLoad();" 
						+"$(document).ready(function () {  " 
						+"$(\"#errorModal\").on(\"show.bs.modal\", function(e) {"
						+"var link = $(e.relatedTarget);"
						+"$(this).find(\".modal-body\").load(link.attr(\"href\"));"
						+"});"
						+"$(\"#marksModal\").on(\"show.bs.modal\", function(e) {"
          					+"var link = $(e.relatedTarget);"
          					+"$(this).find(\".modal-body\").load(link.attr(\"href\"));"
          					+"});});"
          				+"</script>"
						+ "<link rel=\"canonical\" href=\"http://www.wufoo.com/gallery/designs/template.html\">"
						+"<link rel=\"stylesheet\" href=\"css/structure.css\" type=\"text/css\" />"+
						"<style>#breadcrumbs{position: absolute;padding-left:10px;padding-right:10px;left: 5px;top: 10px;font: 13px/13px Arial, Helvetica, sans-serif;background-color: #f0f0f0;font-weight: bold;}"+
						"</style>"
						+ "</head>"
						+"<body id=\"public\">"); 
		if(! Boolean.parseBoolean(session.getAttribute("ltiIntegration").toString())){
			out_assignment.println("<div id=\"breadcrumbs\">"
			   +"<a style='color:#353275;text-decoration: none;' href=\"CourseHome.jsp\" target=\"_top\">Home</a> &nbsp; >> &nbsp;"
			   +"<a href=\"InstructorHome.jsp?contextLabel="+(String) request.getSession().getAttribute("context_label")+"\" style='color:#353275;text-decoration: none;' target=\"_top\">"+(String) request.getSession().getAttribute("context_label")+"</a>&nbsp; >> &nbsp;"
			   +"<a href=\"ListAllAssignments.jsp\" style='color:#353275;text-decoration: none;' target=\"_self\">Assignment List</a>&nbsp; >> &nbsp;"
			   +"<a href=\"asgnmentList.jsp?assignmentId="+request.getParameter("assignment_id")+"&&showQuestions=true\" target=\"_self\" style='color:#353275;text-decoration: none;'>Assignment Details</a>&nbsp; >> &nbsp;"
			 //  +"<a href=\"gradeAssignment.jsp?AssignmentID="+request.getParameter("assignment_id")+"\" style='color:#353275;text-decoration: none;'>Question List</a>&nbsp; >> &nbsp;"
			   +"<a href=\"#\" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>Query Status</a></div>");			  
		}else{
			out_assignment.println("<div id=\"breadcrumbs\">" 
			   +"<a href=\"asgnmentList.jsp?assignmentId="+request.getParameter("assignment_id")+"&&showQuestions=true\" target=\"_self\" style='color:#353275;text-decoration: none;'>Assignment Details</a>&nbsp; >> &nbsp;"    
			  // +"<a href=\"gradeAssignment.jsp?AssignmentID="+request.getParameter("assignment_id")+"\" style='color:#353275;text-decoration: none;'>Question List</a>&nbsp; >> &nbsp;"
			   +"<a href=\"#\" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>Query Status</a>"
			   +"</div>"); 
		} 
		  
		out_assignment.println("<br/><div id=\"fieldset\">"
						+"<fieldset><legend>Result</legend>"
						+ "<form class=\"wufoo\" action=\"TestCaseDataset\" method=\"get\">");
 
		String assignment_id = request.getParameter("assignment_id");
		String question_id = request.getParameter("question_id");

		/*String result = "select user_name, email, querystring, totalmarks, verifiedcorrect, " +
				"q.rollnum,s.result " +
				"from xdata_student_queries q inner join score s on q.rollnum = s.rollnum and " +
				"q.assignment_id = s.assignment_id and q.question_id = s.question_id inner " +
				"join xdata_qinfo qi on q.assignment_id = qi.assignment_id and q.question_id = qi.question_id " +
				"left outer join xdata_users u on q.rollnum = u.internal_user_id " +
				"where q.assignment_id = ? and q.question_id = ? order by q.rollnum, q.queryid";*/
		/*String result = "select user_name,email,querystring, totalmarks, verifiedcorrect,q.rollnum,s.result  from users u " +
				"left outer join xdata_student_queries q on  q.rollnum = u.internal_user_id " +
				"inner join score s on q.rollnum = s.rollnum and q.assignment_id = s.assignment_id and q.question_id = s.question_id inner join xdata_qinfo qi on q.assignment_id = qi.assignment_id and q.question_id = qi.question_id  where q.assignment_id = ? and q.question_id = ? order by q.rollnum, q.queryid";*/
		String result = "select user_name,email,querystring, totalmarks, verifiedcorrect," +
				"rollnum,info.score from xdata_users u left outer join (select q.course_id,q.rollnum,q.score," +
				"qi.totalmarks,q.verifiedcorrect,q.querystring from xdata_student_queries q " +
				"inner join xdata_qinfo qi on q.assignment_id = qi.assignment_id " +
				"and q.question_id = qi.question_id  and q.course_id = qi.course_id " +
				"where q.assignment_id = ? and q.question_id = ? and q.course_id = ? " +
				"order by q.rollnum, q.queryid) info " +
				"on u.internal_user_id = info.rollnum "+
				" inner join xdata_roles xdr on " +
				"xdr.internal_user_id=u.internal_user_id" +
				" and xdr.role='student' and xdr.course_id=?";

		int index = 0;
		// String 
		// result="select * from xdata_student_queries where queryid=? order by rollnum,queryid";
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){
			try(PreparedStatement pstmt = dbcon.prepareStatement(result)){
			pstmt.setInt(1, Integer.parseInt(assignment_id));
			pstmt.setInt(2, Integer.parseInt(question_id));
			pstmt.setString(3,course_id);
			pstmt.setString(4,course_id);
			// pstmt.setInt(2, question_id);
			try(ResultSet rs = pstmt.executeQuery()){
			boolean present = false;
			
			out_assignment.println("<table>");

			out_assignment
					.println("<tr style='background: #E4E4E4; text-align: center;font-weight: bold;'>"
							+ "<td>Name</td>"
							+ "<td>Email</td>"
							+ "<td style = 'width: 500px;'>Student Answer</td>"
							+ "<td>Status</td>"
							+ "<td>Score</td>"
							+ "<td>Error Details</td>"
							+ "<td>Mark Details</td>" + "</tr>");
			
			while (rs.next()) {
				present = true;
				index ++;
				if(rs.getString("querystring") != null && !rs.getString("querystring").isEmpty()){
				if (rs.getString("verifiedcorrect") != null && !rs.getBoolean("verifiedcorrect")) {
					out_assignment.println("<tr>" + "<td>"
							+ rs.getString("user_name") + "</td>" + "<td>"
							+ rs.getString("email") + "</td>"
							+ "<td> <pre><code class=\"sql\">"
							+ rs.getString("querystring")
							+ "</code></pre></td>" + "<td>" + "Wrong" + "</td>"
							+ "<td>" + rs.getFloat("score") + "</td>"
												
							+"<td>"
							//+"<a data-toggle=\"modal\" data-target=\"#errorModal\" " +
								//	"href='FailedTestCases?user_id="
							+"<a id=\"testCase\" href=\'FailedTestCases?user_id="
							+ rs.getString("rollnum") + "&assignment_id="
							+ assignment_id + "&question_id=" + question_id+ "' target=\"_blank\" type=\"new_tab\">Test Cases</a></td>"
							
							/*data-toggle="modal" data-target="#marksModal"
		 					data-remote="PartialMarkDetails.jsp?reqFrom=popUp&assignment_id=" +
		 					"<%=assignment_id %>&question_id=<%=rset.getInt("question_id") %>" +
		 					"&user_id=<%= rs.getString("rollnum")%>">Mark Details</a></td>*/
		 												
							+ "<td>"
							/*+"<a data-toggle=\"modal\" data-target=\"#marksModal\" " +
							"href='PartialMarkDetails.jsp?user_id="
							+ rs.getString("rollnum") + "&reqFrom=popUp&assignment_id="
							+ assignment_id + "&question_id=" + question_id
							+ "'>Mark Details</a>"*/
							+"<a id=\"marks\" href=\"PartialMarkDetails.jsp?user_id="
							+ rs.getString("rollnum") + "&reqFrom=popUp&assignment_id="
							+ assignment_id + "&question_id=" + question_id
							+ "\" target=\"_blank\" type=\"new_tab\"> Mark Details</a>"
							+"</td>" + "</tr>");
				} else if(rs.getString("verifiedcorrect") != null && rs.getBoolean("verifiedcorrect")){
					out_assignment.println("<tr>" + "<td>"
							+ rs.getString("user_name") + "</td>" + "<td>"
							+ rs.getString("email") + "</td>"
							+ "<td> <pre><code class=\"sql\">"
							+ rs.getString("querystring")
							+ "</code></pre></td>" + "<td>" + "Correct"
							+ "</td>" + "<td>" + Math.round(rs.getFloat("score"))
							+ "</td>" +
							"<td>No error</td>"+
							"<td>"
							+"<a id=\"marks\" href=\"PartialMarkDetails.jsp?user_id="
							+ rs.getString("rollnum") + "&reqFrom=popUp&assignment_id="
							+ assignment_id + "&question_id=" + question_id
							+ "\" target=\"_blank\" type=\"new_tab\"> Mark Details</a>"
							+ "</td>" + "</tr>");
				}
				else{
					out_assignment.println("<tr>" + "<td>"
							+ rs.getString("user_name") + "</td>" + "<td>"
							+ rs.getString("email") + "</td>"
							+ "<td> <pre><code class=\"sql\">"
							+ rs.getString("querystring")
							+ "</code></pre></td>" + "<td>" + "Not Graded"
							+ "</td>" + "<td>" 
							+ "</td>" + 
							/* "<td> </td>"+ */
							"<td></td>" + "</tr>");
				}
				}else{//If student has not answered
					out_assignment.println("<tr>" + "<td>"
							+ rs.getString("user_name") + "</td>" + "<td>"
							+ rs.getString("email") + "</td>"
							+ "<td> <pre><code class=\"sql\">"
							+ rs.getString("querystring")
							+ "</code></pre></td>" + "<td>" + "Not Answered"
							+ "</td>" + "<td> 0" 
							+ "</td>" 
							+"<td> </td>"+ 
							"<td></td>" + "</tr>");
				}
				
			}
			if (present)
				out_assignment.println("</table>");
			else
				out_assignment.println("Not solved");
			}//try block for resultset ends
		}//try block for prepared statement ends
		
		} catch (SQLException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw new ServletException(e);
		}
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
		out_assignment.println("</form></fieldset>" +
		"</div>" +
		"<!-- End Page Content -->" +
		"</body>" +
		"</html>");
		out_assignment.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}

