
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
 * Servlet implementation class EditUser
 */
@WebServlet("/EditUser")
public class EditUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(EditUser.class.getName());
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public EditUser() {
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

		String userName = request.getParameter("userName");
		String userPwd = request.getParameter("password");
		String email = request.getParameter("email");
		String userId = request.getParameter("userIdToEdit");
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){

			try(PreparedStatement stmt = dbcon
					.prepareStatement("update xdata_users set user_name=?,password=?,email=? where internal_user_id=?")){
				stmt.setString(1, userName);
				stmt.setString(2, userPwd);
				stmt.setString(3, email);
				stmt.setString(4,userId);
				stmt.executeUpdate();
				
			}//try block for statement ends
			response.sendRedirect("ViewUsers.jsp");
		} catch (SQLException sep) {
			logger.log(Level.SEVERE,"Could not connect to database: " + sep.getMessage(),sep);
			throw new ServletException(sep);
		} 
	}

}
