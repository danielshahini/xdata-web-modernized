import java.io.IOException;
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

import org.apache.commons.codec.digest.DigestUtils;

import database.DatabaseConnection;

/**
 * Servlet implementation class ResetPwd
 */
@WebServlet("/ResetPwd")
public class ResetPwd extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ResetPwd.class.getName());
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ResetPwd() {
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

		String userId = request.getParameter("userId");
		//String oldPwd = request.getParameter("oldPassword");
		//String hashedOldPassword = DigestUtils.md5Hex(oldPwd) ;
		String newPwd = request.getParameter("newPassword");
		String hashedNewPassword = DigestUtils.md5Hex(newPwd);
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){
			logger.log(Level.FINE,"user_d for pwd reset == " + userId);
				try(PreparedStatement stmt = dbcon
							.prepareStatement("SELECT password from xdata_users where login_user_id=?")){
					stmt.setString(1, userId);
					try(ResultSet rs = stmt.executeQuery()){
						if (rs.next()) { 
							//System.out.println("hashedOldPassword == "+hashedOldPassword);
							System.out.println("password == " + rs.getString("password"));
			
						//if(hashedOldPassword.equals(rs.getString("password"))){
							try(PreparedStatement stmt1 = dbcon
									.prepareStatement("update xdata_users set password=? where login_user_id=?")){
								stmt1.setString(1, hashedNewPassword);
								stmt1.setString(2, userId);
								stmt1.executeUpdate();
							}//try block for stmt1 ends
							response.sendRedirect("ViewUsers.jsp?status=success");
						//}else{
							//throw new ServletException("Old password did not match the existing records."); 
						//}
						} else { 
							throw new ServletException(
									"User id does not match with existing records.");
						}
						}//try block for resultset ends
					}//try block for prepared statement ends
			} catch (SQLException sep) {
				logger.log(Level.SEVERE,"Could not connect to database: " + sep.getMessage(),sep);
				throw new ServletException(sep);
		}
	}
}
