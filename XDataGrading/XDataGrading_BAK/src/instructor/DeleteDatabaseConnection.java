package instructor;



import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import database.DatabaseConnection;

/**
 * Servlet implementation class DeleteDatabaseConnection
 */
//@WebServlet("/DeleteDatabaseConnection")
public class DeleteDatabaseConnection extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(DeleteDatabaseConnection.class.getName());   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DeleteDatabaseConnection() {
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
		
		String[] checkedIds = request.getParameterValues("deleteConnection");		
		String courseID = (String) request.getSession().getAttribute("context_label");
		//get connection
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){		
			for(String s: checkedIds){
				  int i=Integer.parseInt(s);
					try(PreparedStatement stmt = dbcon
							.prepareStatement("delete FROM  xdata_database_connection where course_id = ? and connection_id= ?")){
					stmt.setString(1, courseID);
					stmt.setInt(2,i);
					stmt.execute(); 					
					}
				} 
			
			}catch (Exception err) {
				logger.log(Level.SEVERE,err.getMessage(),err);
				throw new ServletException(err); 
			} 	
		PrintWriter out_print=response.getWriter();
		response.sendRedirect("NewDatabaseConnection.jsp?Delete=true");
	}
}
