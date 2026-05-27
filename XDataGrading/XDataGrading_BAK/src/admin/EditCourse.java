package admin;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import database.DatabaseConnection;

/**
 * Servlet implementation class EditCourse
 */
//@WebServlet("/EditCourse")
public class EditCourse extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(EditCourse.class.getName());
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public EditCourse() {
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
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String instr_course_id = request.getParameter("instrcourseId");
		String courseId = request.getParameter("courseId");
		String courseName = request.getParameter("courseName");
		int year = Integer.parseInt(request.getParameter("year"));
		String semester = request.getParameter("semester");
		String description = request.getParameter("desc");
		// Update course_id in all tables of XDAta as it is a part of primary key 
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){
			try(PreparedStatement stmt = dbcon
					.prepareStatement("update xdata_course set instructor_course_id=?,course_name=?,year=?,semester=?,description=? where course_id=?")){
				stmt.setString(1, instr_course_id);
				stmt.setString(2, courseName);
				stmt.setInt(3, year);
				stmt.setString(4, semester);
				stmt.setString(5, description);
				stmt.setInt(6, Integer.parseInt(courseId));
				stmt.executeUpdate();
			}//try block to close prepared statement ends
			response.sendRedirect("ViewCourseListForAdmin.jsp");
		} catch (SQLException sep) {
			logger.log(Level.SEVERE,"Could not connect to database: " + sep.getMessage(),sep);
			throw new ServletException(sep);
		} 

	}

}
