package instructor;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import evaluation.TestAssignment;
import database.*;
/**
 * Servlet implementation class EvaluateQuestion
 */
//@WebServlet("/EvaluateQuestion")
public class EvaluateQuestion extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(EvaluateQuestion.class.getName());
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public EvaluateQuestion() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session=request.getSession();		
		String courseID = (String) request.getSession().getAttribute(
				"context_label");
		String assignment_id = request.getParameter("assignment_id");
		String question_id= request.getParameter("question_id");
		//String query_id = (String)request.getParameter("query_id");
		//int query_id = 1;
		this.evaluateQuestion(assignment_id, question_id, courseID);
	}

	/**
	 * This method checks if all queries are evaluated. Querying server for checking the status of completion.
	 * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		HttpSession session=request.getSession();		
		String courseID = (String) request.getSession().getAttribute(
				"context_label");
		String assignment_id = request.getParameter("assignment_id");
		String question_id= request.getParameter("question_id");
		boolean evaluationCompleted = false;
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){
			String update="select tajudgement from xdata_student_queries where assignment_id = ? and question_id=? and course_id= ? ";
			try(PreparedStatement updatestmt=dbcon.prepareStatement(update)){
				updatestmt.setInt(1, Integer.parseInt(assignment_id));
				updatestmt.setInt(2,Integer.parseInt(question_id));
				updatestmt.setString(3,courseID);
				int query_id=1;
				ResultSet rs =  updatestmt.executeQuery();
				while(rs.next()){
					if(rs.getBoolean("tajudgement")){
						//continue
						evaluationCompleted = true;
					}else{
						evaluationCompleted = false;
						break;
					}
				}
			}//try block for statement ends
		} catch (SQLException e1) {
			logger.log(Level.SEVERE,e1.getMessage(),e1);
			//throw new ServletException(e1);
		}
		if(!evaluationCompleted){
			response.sendError(00,"INCOMPLETE");
		}else{
			response.setStatus(200); 
		}
	}

	public void evaluateQuestion(String assignment_id, String question_id,String courseID) throws ServletException{


		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){
			String update="update xdata_student_queries set tajudgement = false where assignment_id = ? and question_id=? and course_id= ? ";
			try(PreparedStatement updatestmt=dbcon.prepareStatement(update)){
				updatestmt.setInt(1, Integer.parseInt(assignment_id));
				updatestmt.setInt(2,Integer.parseInt(question_id));
				updatestmt.setString(3,courseID);
				int query_id=1;
				updatestmt.execute();
			}//try block for statement ends
		} catch (SQLException e1) {
			logger.log(Level.SEVERE,e1.getMessage(),e1);
			//throw new ServletException(e1);
		}

		Connection dbcon = null;
		try{
			String s = null; 

			String args[] = {assignment_id.trim(), question_id.trim(),courseID};
			TestAssignment.entry(args);


			dbcon = (new DatabaseConnection()).dbConnection();

			PreparedStatement updatestmt=dbcon.prepareStatement("update xdata_instructor_query set evaluationstatus = true where assignment_id=? and question_id=? and course_id=?");
			updatestmt.setInt(1, Integer.parseInt(assignment_id));
			updatestmt.setInt(2,Integer.parseInt(question_id));
			updatestmt.setString(3,courseID);
			updatestmt.execute();	
			
//			updatestmt=dbcon.prepareStatement("update xdata_assignment set evaluationstatus = true where assignment_id=? and course_id=?");
//			updatestmt.setInt(1, Integer.parseInt(assignment_id));
//			updatestmt.setString(2,courseID);
//			updatestmt.execute();

		}catch(Exception e){
			logger.log(Level.SEVERE,e.getMessage(),e);
			//throw new ServletException(e);
		}finally{
			try {
				if(dbcon != null){
					dbcon.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.log(Level.SEVERE,e.getMessage(),e);
			}
		}
	}

}
