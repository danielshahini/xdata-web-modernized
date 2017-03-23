package admin;


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
 * Servlet implementation class NewLmsCredential
 */
//@WebServlet("/NewLmsCredential")
public class NewLmsCredential extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(NewLmsCredential.class.getName());   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NewLmsCredential() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String consumerKey= request.getParameter("consumerKey");
		String secretKey= request.getParameter("secretKey");
		String ltiUrl= request.getParameter("ltiUrl");
		int lti_id=0;
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){
			try(PreparedStatement stmt = dbcon
					.prepareStatement("select MAX(lti_id) as id from xdata_lti_credentials")){
				ResultSet rs = stmt.executeQuery();
				if(rs.next()){
					lti_id =rs.getInt("id")+1;
				}else{
					lti_id=1;
				}				
			}
			
			try(PreparedStatement stmt1 = dbcon
						.prepareStatement("insert into xdata_lti_credentials values(?,?,?,?)")){
				stmt1.setString(1,consumerKey);
				stmt1.setString(2,secretKey);
				stmt1.setString(3,ltiUrl);
				stmt1.setInt(4, lti_id);
				stmt1.execute();
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			//e.printStackTrace();
		}
				
		response.sendRedirect("ltisetup.jsp");
	}

}
