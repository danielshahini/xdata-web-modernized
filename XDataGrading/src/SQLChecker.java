
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import database.*;

//import testDataGen.TestAnswer;

/**
 * Servlet implementation class SQLChecker
 */
// @WebServlet("/SQLChecker")
public class SQLChecker extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(SQLChecker.class.getName());
	/**
	 * Default constructor.
	 */
	private Connection dbCon;
	public SQLChecker() {
		// TODO Auto-generated constructor stub
		dbCon = null;
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		DatabaseConnection db = new DatabaseConnection();
		try {
			dbCon = db.dbConnection();
			if (dbCon != null) {
				logger.log(Level.FINE,"Connected Successfully");
			}
		} catch (Exception ex) {
			logger.log(Level.SEVERE,"SQLException: " + ex.getMessage(),ex);
			throw new ServletException(ex);
		}
		HttpSession session = request.getSession();
		try {
			if (dbCon == null) {
				dbCon = db.dbConnection();
			}
			String del = "delete from xdata_instructor_query where user_id=? and assignment_id=?";
			String sql = "select question_id from xdata_qinfo where assignment_id=? and course_id = ?";

			try(PreparedStatement pstmt = dbCon.prepareStatement(del)){
				pstmt.setString(1, (String) session.getAttribute("user_id"));
				pstmt.setInt(2, Integer.parseInt((String) session
						.getAttribute("resource_link_id")));
				pstmt.executeUpdate();
				try(PreparedStatement stmt = dbCon.prepareStatement(sql)){
					stmt.setString(1, (String) session.getAttribute("resource_link_id"));
					stmt.setString(2, (String) session.getAttribute("context_label"));
					try(ResultSet rs = stmt.executeQuery()){
						while (rs.next()) {
							String insert = "INSERT INTO xdata_instructor_query values (?,?,?,?,'NC')";
							PreparedStatement instmt = dbCon.prepareStatement(insert);
							instmt.setInt(1, Integer.parseInt((String) session
									.getAttribute("resource_link_id")));
							instmt.setInt(2, rs.getInt("question_id"));
						//	instmt.setString(3, (String) session.getAttribute("user_id"));
							int q_id = rs.getInt("question_id");
							String query = request.getParameter(Integer.toString(q_id));
							query = query.replaceAll("'", "''");
							query = query.trim().replaceAll("\r\n+", " ");
							query = query.trim().replaceAll("\n+", " ");
							query = query.trim().replaceAll(" +", " ");
							instmt.setString(3, query);
							instmt.executeUpdate();
						}
					}
				}
			}			
			response.setContentType("text/html");
			PrintWriter out2 = response.getWriter();
			out2.println("<html>" + "<header><title>Success</title></header>"
					+ "<body>" + "Assignment updated successfully" + "</body>"
					+ "</html>");
			out2.close();
		} catch (SQLException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw new ServletException(e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
