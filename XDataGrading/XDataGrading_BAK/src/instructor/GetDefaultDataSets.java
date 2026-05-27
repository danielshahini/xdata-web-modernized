package instructor;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import database.DatabaseConnection;


/**
 * Servlet implementation class GetDefaultDataSets
 */
//@WebServlet("/GetDefaultDataSets")
public class GetDefaultDataSets extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetDefaultDataSets() {
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
	
		String courseId=request.getParameter("course_id");
		//String schema_id=request.getParameter("schemaId");
		int schemaId = 0;
		if(request.getParameter("schemaId") != null && ! request.getParameter("schemaId").equalsIgnoreCase("select")){
			schemaId = Integer.parseInt(request.getParameter("schemaId"));
		}else{
			throw new ServletException("Please select the schema");
		}
		
		//Connection dbcon  = null;
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String output = "";
		try (Connection dbcon = (new DatabaseConnection()).dbConnection()){
			//dbcon = (new DatabaseConnection()).dbConnection();
	
			PreparedStatement stmnt = dbcon.prepareStatement("select sampledata_id,sample_data_name from xdata_sampledata where course_id=? and schema_id=?");
			stmnt.setString(1, courseId);
			stmnt.setInt(2,schemaId);
			ResultSet rSet = stmnt.executeQuery();
			output = "";//<div id=\"defaultDSList\">";
			if(rSet.next()){
				output += "<label>Please select default datasets for evaluation.</label>";
			}
				rSet = stmnt.executeQuery();
			while(rSet.next()){
						
				 output += "<div id='dSet'><input type=\"checkbox\" name=\"defaultDSId\" value=\""+rSet.getString("sampledata_id")+"\"/><label id=\"dsname\">"+rSet.getString("sample_data_name")+"</label>";
				 output +="</div><br/>";

			}
			//output +="</div>";
			out.println(output);
		} catch (Exception err) {
			err.printStackTrace();
			throw new ServletException(err);	
		}
//		finally{
//			try {
//				dbcon.close();
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				throw new ServletException(e);	
//			}
//		}

	}

}
