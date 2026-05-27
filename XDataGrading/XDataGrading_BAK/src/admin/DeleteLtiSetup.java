package admin;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import database.DatabaseConnection;

/**
 * Servlet implementation class DeleteLtiSetup
 */
//@WebServlet("/DeleteLtiSetup")
public class DeleteLtiSetup extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DeleteLtiSetup() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String s = request.getParameter("instanceUrl");
		String courseID = (String) request.getSession().getAttribute("context_label");
		//get connection
		try (Connection dbcon = (new DatabaseConnection()).dbConnection()){
			//Connection dbcon = (new DatabaseConnection()).dbConnection();		
			PreparedStatement stmt;
		
				 // int i=Integer.parseInt(s);
					stmt = dbcon 
							.prepareStatement("delete FROM xdata_lti_credentials where requesting_url= ?");
					stmt.setString(1, s); 
				
					stmt.execute(); 					
				//}  
			dbcon.close(); 
			}catch (Exception err) {
				err.printStackTrace();
				throw new ServletException(err);
			} 							
		PrintWriter out_print=response.getWriter();
		response.sendRedirect("ltisetup.jsp?Delete=true");
	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
