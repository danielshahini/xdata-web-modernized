
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import database.DatabaseConnection;

/**
 * Servlet implementation class CreateNewCourse
 */
@WebServlet("/CreateNewCourse")
public class CreateNewCourse extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(CreateNewCourse.class.getName()); 
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CreateNewCourse() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		int internalCourseId = 1;
		String instructorCourseId = request.getParameter("courseId");
		String courseName = request.getParameter("courseName");
		int year = Integer.parseInt(request.getParameter("year"));
		String semester = request.getParameter("semester");
		String description = request.getParameter("desc");
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){
			try(PreparedStatement stmt1 = dbcon
						.prepareStatement("SELECT MAX(course_id) as courseid from xdata_course")){
				try(ResultSet rs1 = stmt1.executeQuery()){
					if (rs1.next()) {
						internalCourseId = rs1.getInt("courseid") + 1;
					}
		
					PreparedStatement stmt;
		
					stmt = dbcon
							.prepareStatement("insert into xdata_course values(?,?,?,?,?,?)");
					stmt.setInt(1, internalCourseId);
					stmt.setString(2, instructorCourseId);
					stmt.setString(3, courseName);
					stmt.setInt(4, year);
					stmt.setString(5, semester);
					stmt.setString(6, description);
					stmt.executeUpdate();
					
					response.sendRedirect("ViewCourseListForAdmin.jsp");
				}//try block for resultset ends
			}//try block for statement ends
		} catch (SQLException sep) {
			logger.log(Level.SEVERE,"Could not connect to database: " + sep.getMessage(),sep);
			//sep.printStackTrace();
			throw new ServletException(sep);
		}
	}

}
