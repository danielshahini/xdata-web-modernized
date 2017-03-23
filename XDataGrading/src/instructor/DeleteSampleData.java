package instructor;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import database.DatabaseConnection;

/**
 * Servlet implementation class SampleDataDelete
 */
//@WebServlet("/SampleDataDelete")
public class DeleteSampleData extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(DeleteSampleData.class.getName());
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DeleteSampleData() {
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
		
		String[] checkedIds = request.getParameterValues("deleteSampleData");		
		String courseID = (String) request.getSession().getAttribute("context_label");
		//get connection
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){		
			
			for(String s: checkedIds){
				  int i=Integer.parseInt(s);
					/*try(PreparedStatement stmt = dbcon
							.prepareStatement("update xdata_schemainfo set sample_data = null, sample_data_name=null where course_id = ? and schema_id = ?")){
						stmt.setString(1, courseID);
						stmt.setInt(2, i);
						stmt.executeUpdate(); 
						logger.log(Level.FINE,"Deleted : Schema Id : "+s);
					}//try with resources for Preparedstmt ends*/
				  try(PreparedStatement stmt = dbcon
							.prepareStatement("delete from xdata_sampledata where course_id = ? and schema_id = ?")){
						stmt.setString(1, courseID);
						stmt.setInt(2, i);
						stmt.executeUpdate(); 
				  }
				  
				} 
		}//try block to close connection obj
		catch (Exception err) { 
			logger.log(Level.SEVERE,err.getMessage(),err);
			throw new ServletException(err); 
		} 	
		
		PrintWriter out_print=response.getWriter();
		response.sendRedirect("sampleDataUpload.jsp?Delete=true");
	}

}
