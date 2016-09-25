

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
 * Servlet implementation class EditLmsCredential
 */
@WebServlet("/EditLmsCredential")
public class EditLmsCredential extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(EditLmsCredential.class.getName()); 
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EditLmsCredential() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String consumerKey= request.getParameter("consumerKey");
		String secretKey= request.getParameter("secretKey");
		String ltiUrl= request.getParameter("ltiUrl");
		int lti_id=Integer.parseInt(request.getParameter("lti_id"));
		
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){
			try(PreparedStatement stmt1 = dbcon
						.prepareStatement("update xdata_lti_credentials set consumer_key=?, secret_key=? where lti_id=?")){
				stmt1.setString(1,consumerKey);
				stmt1.setString(2,secretKey);
				//stmt1.setString(3,ltiUrl);
				stmt1.setInt(3,lti_id);
				stmt1.execute();
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw new ServletException(e);
			//e.printStackTrace();
		}
		response.sendRedirect("ltisetup.jsp");
	
		
	}

}
