import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;

import database.DatabaseConnection;

/**
 * Servlet implementation class ForgotPassword
 */
@WebServlet("/ForgotPassword")
public class ForgotPassword extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ForgotPassword.class.getName());
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ForgotPassword() {
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
		String mailId = request.getParameter("mailId");
		//String newPwd = request.getParameter("newPwd");
		String status = "";
		String userLoginName = "";
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){
			try(PreparedStatement stmt = dbcon
					.prepareStatement("select * from xdata_users where login_user_id = ?")){
			stmt.setString(1, userId);
			//stmt.setString(2, mailId);
			
			try(ResultSet rs = stmt.executeQuery()){
			if (rs.next()) {
				status = "success";
				userLoginName = rs.getString("login_user_id");
 
				String newPwdGenerated = RandomStringUtils
						.randomAlphanumeric(6).toUpperCase();
			String hashedPassword = DigestUtils.md5Hex(newPwdGenerated);
				//String hashedPassword = DigestUtils.md5Hex(newPwd); 
 
				try(PreparedStatement stmt1 = dbcon
						.prepareStatement("update xdata_users set password=? where login_user_id = ?")){
				stmt1.setString(1, hashedPassword);
				stmt1.setString(2, userId);
				//stmt1.setString(3, mailId);
				stmt1.executeUpdate();
				}
				RequestDispatcher rd = request
						.getRequestDispatcher("SendEmail?newPasswrd="
								+ newPwdGenerated + "&userName=" + userLoginName
								+ "&mail=" + mailId);
				rd.forward(request, response);
		//	response.sendRedirect("index.jsp?pwdStatus="+status);
			} else {
				status = "error";
				response.sendRedirect("forgotPwd.jsp?status=" + status);
				
				
			}
			}//try block for resultset ends 
			}//try block for statement ends
		} catch (SQLException sep) {
				logger.log(Level.SEVERE,sep.getMessage(),sep);
				throw new ServletException(sep);
		}

	}

}
