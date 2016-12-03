
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import database.DatabaseConnection;

/**
 * Servlet implementation class AssignmentOptions
 */
// @WebServlet("/AssignmentOptions")
public class AssignmentOptions extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(AssignmentOptions.class.getName());
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	private Connection dbcon;

	public AssignmentOptions() {
		super();
		dbcon = null;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(ServletConfig c) throws ServletException {

	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// Open the connection here
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){

				String courseId = "";
				String full_name = "";
				String assignmentId = "";
				String role = "";
				// Student role
				if (role.equalsIgnoreCase("student")) {
				response.setContentType("text/html");
				PrintWriter out_assignment = response.getWriter();
				out_assignment.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""
								+"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
								+"<html xmlns=\"http://www.w3.org/1999/xhtml\">"
								+"<head>"
								+"<title>"
								+"XData &middot; Assignment"
								+"</title>"
								+"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"
								+"<script type=\"text/javascript\" src=\"scripts/wufoo.js\"></script>"
								+"<link rel=\"stylesheet\" href=\"css/structure.css\" type=\"text/css\" />"
								+ "<link rel=\"stylesheet\" href=\"css/form.css\" type=\"text/css\" />"
								+ "<link rel=\"stylesheet\" href=\"css/theme.css\" type=\"text/css\" />"
								+"<link rel=\"canonical\" href=\"http://www.wufoo.com/gallery/designs/template.html\">"
								+"</head>"+"<body id=\"public\">"
								+"<div id=\"container\">"
								+"<form class=\"wufoo\" action=\"ViewAssignment\" method=\"get\">"
								+"<div class=\"info\">"
								+ "<h2>Assignments</h2>"
								+ "</div>");
	
				String assignments = "select * from xdata_assignment where assignment_id=? and course_id=?";
				//Get assignments to display
				try(PreparedStatement pstmt = dbcon.prepareStatement(assignments)){
					pstmt.setString(1,(String) request.getSession().getAttribute("assignment_id"));
					pstmt.setString(2,(String) request.getSession().getAttribute("course_id"));
					try(ResultSet rst = pstmt.executeQuery()){
						if (rst.next()) {
							rst.beforeFirst();
							out_assignment.println("<table border=\"1\">");
							out_assignment.println("<tr>" + "<td>Assignment Id</td>"
									+ "<td> Start time</td>" + "<td> End time</td>"
									+ "<td> </td>" + "<td> </td>" + "</tr>");
							while (rst.next()) {
									String edit = "";
									java.util.Date date = new java.util.Date();
									Timestamp ts = new Timestamp(date.getTime());
									if (ts.after(rst.getTimestamp("end_time"))) {
										edit = "";
				
									} else {
										edit = "<input type=\"submit\" name="
												+ rst.getInt("assignment_id")
												+ "E value=\"Solve\" /> ";
									}
										out_assignment.println("<tr>" + "<td>Assignment "
											+ rst.getInt("assignment_id") + "</td>"
											+ "<td>" + rst.getTimestamp("starttime")
											+ "</td>" + "<td>"
											+ rst.getTimestamp("endtime") + "</td>"
											+ "<td> " + edit + "</td>"
											+ "<td> <input type=\"submit\" name="
											+ rst.getInt("assignment_id")
											+ "V Value=\"Result\" /> </td>" + "</tr>");
								}
							out_assignment.println("</table>");
						} else {
							out_assignment
									.println("<h2>Assignment not yet uploaded</h2>");
						}
						}//try block for resultset ends
					
				}//try block for statement ends
			out_assignment
					.println("<p><p><p><p><p><p><a href=\"index.jsp\">logout</a>");
			out_assignment.println("</form>" +
	
			"</div><!-- End Page Content --></body></html>");
			out_assignment.close();
			}
		// Instructor role
		else {
			// assignment.html
			RequestDispatcher rd = request
					.getRequestDispatcher("/asgnmentCreation.html");
			rd.include(request, response);
		}
	}catch (Exception ex) {
		logger.log(Level.SEVERE,"SQLException: " + ex.getMessage(),ex);
		throw new ServletException(ex);
	}
}
	

}
