package instructor;


import java.io.IOException;
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
 * Servlet implementation class GetQuestionsForLateSumbission
 */
//@WebServlet("/GetQuestionsForLateSumbission")
public class GetQuestionsForLateSumbission extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetQuestionsForLateSumbission() {
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
		
		String email = request.getParameter("email");
		//String course_id=request.getParameter("course_id");
		String assignment_id=request.getParameter("assignment_id");
		String course_id="";
		//Connection dbcon = null;
		String rollnum = "";
		String user_name = "";
		int cnt =0;
		//Get student details from xdata_users table
				//verify email
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()) {
		//	dbcon = (new DatabaseConnection()).dbConnection();
	
			PreparedStatement stmnt = dbcon.prepareStatement("select user_name,internal_user_id,course_id from xdata_users where email=?");
			//stmnt.setString(1, course_id);
			stmnt.setString(1,email);
			ResultSet rSet = stmnt.executeQuery();
			
				while(rSet.next()){
					cnt ++;
					user_name=rSet.getString("user_name");
					rollnum = rSet.getString("internal_user_id");
					course_id=rSet.getString("course_id");
				}
			if(cnt ==0){
				response.sendRedirect("LateSubmission.jsp?value=error");
			}
			else{

				response.sendRedirect("ViewQuestionListForLateSubmission.jsp?assignment_id="+assignment_id+"&&course_id="+course_id+"&&rollnum="+rollnum+"&&user_name="+user_name+"&&email="+email);
				
			}
		}catch(Exception e){
			response.sendRedirect("LateSubmission.jsp?value=error");
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
		
	
		//Then show the saved questions page with an evaluate button.
		
		//After evaluation, show evaluation status for that student alone.
		
		
				
	}

}
