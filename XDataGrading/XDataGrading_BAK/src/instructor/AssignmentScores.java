package instructor;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import database.*;

/**
 * Servlet implementation class QueryStatus
 */
//@WebServlet("/AssignmentScores")
@Deprecated
public class AssignmentScores extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(AssignmentScores.class.getName());
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AssignmentScores() {
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


		response.setContentType("text/html");
		PrintWriter out_assignment = response.getWriter();
		out_assignment
				.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""
						+ "\"https://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
						+

						"<html xmlns=\"https://www.w3.org/1999/xhtml\">"
						+ "<head>"
						+

						"<title>"
						+ "XData &middot; Assignment"
						+ "</title>"
						+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"
						+

						"<script type=\"text/javascript\" src=\"scripts/wufoo.js\"></script>"
						+

						"<link rel=\"stylesheet\" href=\"css/structure.css\" type=\"text/css\" />"
						+ "<link rel=\"stylesheet\" href=\"highlight/styles/xcode.css\">  "
						+ "<link rel=\"stylesheet\" href=\"highlight/styles/default.css\">"
						+ "<script src=\"highlight/highlight.pack.js\"></script>"
						+ "<script type=\"text/javascript\"> hljs.initHighlightingOnLoad();</script>"
						+ "<link rel=\"canonical\" href=\"http://www.wufoo.com/gallery/designs/template.html\">"
						+"<link rel=\"stylesheet\" href=\"css/structure.css\" type=\"text/css\" />"+
						"<style>#breadcrumbs{position: absolute;padding-left:10px;padding-right:10px;left: 5px;top: 10px;font: 13px/13px Arial, Helvetica, sans-serif;background-color: #f0f0f0;font-weight: bold;}"+
						"</style>"+"</head>"+"<body id=\"public\">");
						
					if(! Boolean.parseBoolean(session.getAttribute("ltiIntegration").toString())){
							out_assignment.println("<div id=\"breadcrumbs\">"
							   +"<a style='color:#353275;text-decoration: none;' href=\"CourseHome.jsp\" target=\"_top\">Home</a> &nbsp; >> &nbsp;"
							   +"<a href=\"InstructorHome.jsp?contextLabel="+(String) request.getSession().getAttribute("context_label")+"\" style='color:#353275;text-decoration: none;' target=\"_top\">"+(String) request.getSession().getAttribute("context_label")+"</a>&nbsp; >> &nbsp;"
							   +"<a href=\"ListAllAssignments.jsp\" style='color:#353275;text-decoration: none;' target=\"_self\">Assignment List</a>&nbsp; >> &nbsp;"
							   +"<a href=\"asgnmentList.jsp?assignmentId="+request.getParameter("AssignmentID")+"\" target=\"_self\" style='color:#353275;text-decoration: none;'>Assignment Details</a>&nbsp; >> &nbsp;"    
							   +"<a href=\"#\" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>View Scores</a></div>");
							  
						}else{
							out_assignment.println("<div id=\"breadcrumbs\">" 
							   +"<a href=\"asgnmentList.jsp?assignmentId="+request.getParameter("AssignmentID")+"\" target=\"_self\" style='color:#353275;text-decoration: none;'>Assignment Details</a>&nbsp; >> &nbsp;"    
							   +"<a href=\"ListOfQuestions.jsp?AssignmentID="+request.getParameter("AssignmentID")+"\" style='color:#353275;text-decoration: none;'>Question List</a>&nbsp; >> &nbsp;"
							   +"<a href=\"#\" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>View Scores</a>"
							   +"</div>"); 
						} 
						 
						out_assignment.println("<br/><div id=\"fieldset\">"
						+

						"<fieldset><legend>Result</legend>"
						+ "<form class=\"wufoo\" action=\"TestCaseDataset\" method=\"get\">");

		Boolean ltiIntegration = Boolean.parseBoolean(session.getAttribute(
				"ltiIntegration").toString());
		String assignment_id = request.getParameter("AssignmentID");
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){

			String total = "select sum(totalmarks) total from xdata_qinfo where assignment_id = ?";
			String result = "select sum(result) result, user_name, email, rollnum from xdata_users u " +
					"left join score s on u.internal_user_id = s.rollnum " +
					"where assignment_id = ? group by user_name, email, rollnum order by rollnum";
			Float totalMarks = 100F;
			try(PreparedStatement pstmt = dbcon.prepareStatement(total)){
			pstmt.setInt(1, Integer.parseInt(assignment_id));
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				totalMarks = rs.getFloat("total");
			}
			}
			boolean present = false;
			try(PreparedStatement pstmt = dbcon.prepareStatement(result)){
	
				pstmt.setInt(1, Integer.parseInt(assignment_id));
				try(ResultSet rs = pstmt.executeQuery()){
					out_assignment.println("<table>");
		
					String uploadHeader = "";
		
					if (ltiIntegration) {
						uploadHeader = "<td>Upload</td>";
					}
					out_assignment
							.println("<tr style='background: #E4E4E4; text-align: center;font-weight: bold;'>"
									+ "<td>Name</td>"
									+ "<td>Email</td>"
									+ "<td>Score</td>" + uploadHeader + "</tr>");
					while (rs.next()) {
						present = true;
						String uploadLink = "";
						if (ltiIntegration) {
							uploadLink = "<td><a href='UploadScore?userId="
									+ rs.getString("rollnum") + "&score="
									+ rs.getFloat("result") + "&max=" + totalMarks
									+ "&AssignmentID="+request.getParameter("AssignmentID")+"'>Upload</a></td>";
						}
						out_assignment.println("<tr>" + "<td>" + rs.getString("user_name")
								+ "</td>" + "<td>" + rs.getString("email") + "</td>"
								+ "<td>" + rs.getFloat("result") + "</td>" + uploadLink
								+ "</tr>");
					}
					}//try block for resultset ends
				if (present)
					out_assignment.println("</table>");
				else
					out_assignment.println("Not solved");	
				}//try block for statement ends
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.log(Level.SEVERE,e.getMessage(),e);
			//e.printStackTrace();
			throw new ServletException(e);

		}
		// out_assignment.println("<input type=\"submit\"  name=\"\" value=\"Home page\" />");
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
