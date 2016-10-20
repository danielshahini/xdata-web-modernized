

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.DatabaseConnectionDetails;

import evaluation.TestAssignment;

import database.DatabaseConnection;
import evaluation.QueryStatusData;

/**
 * Servlet implementation class LateSubmission
 */
@WebServlet("/LateSubmission")
public class LateSubmission extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LateSubmission() {
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
		
		String course_id = request.getParameter("courseId");
		int assignment_id= Integer.parseInt((String)request.getParameter("assignment_id"));
		String username = request.getParameter("username");
		String rollnum = request.getParameter("rollnum");
		String email = request.getParameter("email");
		int noOfQuestions = Integer.parseInt((String)request.getParameter("noOfQuestions")); 
		int marksToBeReduced = Integer.parseInt((String)request.getParameter("marksToBeReduced"));
		Connection dbcon = null;
		Connection testConn = null;
		

		try {
			dbcon = (new DatabaseConnection()).dbConnection();
			PreparedStatement stmt;
			ResultSet rs;
		for(int i=1; i< noOfQuestions; i++){
			String correctquery = (String) request.getParameter("newQuery"+i);
			System.out.println("Correct Query " + correctquery);
			
			PreparedStatement updt= dbcon.prepareStatement("update xdata_qinfo set latesubmissionmarks = ? where assignment_id=? and question_id = ? and course_id=?");
			updt.setInt(1,marksToBeReduced);
			updt.setInt(2,assignment_id);
			updt.setInt(3,i);
			updt.setString(4,course_id);
			updt.execute();
			
			stmt = dbcon.prepareStatement("select * from xdata_student_queries where assignment_id=? and question_id = ? and rollnum = ? and course_id=?");
			stmt.setInt(1, assignment_id);
			stmt.setInt(2,i);
			stmt.setString(3, rollnum);
			stmt.setString(4,course_id);
			System.out.println(stmt.toString());
			rs = stmt.executeQuery();
			 
			if(!rs.next()){
				stmt = dbcon.prepareStatement("INSERT INTO xdata_student_queries (dbid, queryid, rollnum, querystring, tajudgement,verifiedcorrect, assignment_id, question_id,course_id,late_submission_flag) VALUES(?,?,?,?,?,?,?,?,?,?)");
				stmt.setString(1, "1");
				stmt.setString(2,"1");
				stmt.setString(3, rollnum);
				stmt.setString(4, correctquery); 
				stmt.setBoolean(5,true);
				stmt.setNull(6, Types.BOOLEAN);
				stmt.setInt(7,assignment_id);
				stmt.setInt(8,i);
				stmt.setString(9,course_id);
				stmt.setBoolean(10,true);
				stmt.execute();
			}else{
				stmt = dbcon.prepareStatement("update xdata_student_queries set dbid=?, queryid=?, querystring= ?, tajudgement = ?,verifiedcorrect= ?,late_submission_flag = ? where assignment_id=? and question_id = ? and course_id=? and rollnum = ?");
				stmt.setString(1, "1");
				stmt.setString(2,"1");
				stmt.setString(3, correctquery); 
				stmt.setBoolean(4,true);
				stmt.setNull(5, Types.BOOLEAN);
				stmt.setBoolean(6,true);
				
				stmt.setInt(7,assignment_id);
				stmt.setInt(8,i);
				stmt.setString(9,course_id);
				stmt.setString(10, rollnum);
				stmt.execute(); 
			}	
		}
		
		//Evaluate all the answers given by this student

		TestAssignment ta = new TestAssignment();
		DatabaseConnectionDetails dbConnDetails = (new util.DatabaseConnection()).getTesterConnection(assignment_id);
		testConn = dbConnDetails.getTesterConn();
		
		String args[] = {String.valueOf(assignment_id), rollnum,course_id, String.valueOf(noOfQuestions), String.valueOf(marksToBeReduced)};
		 
		ta.evaluateLateSubmission(dbcon,testConn,args);
				
		response.sendRedirect("evaluationResultsOfLateSubmission.jsp?assignment_id="+assignment_id+"&&course_id="+course_id+"&&rollnum="+rollnum+"&&user_name="+username+"&&email="+email);
		 
		}catch(Exception e){
			e.printStackTrace();
			throw new ServletException(e); 
		}finally{
			try {
				dbcon.close();
				testConn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new ServletException(e); 
			}
		}
		
			
			
			
			
			
			
			
			
			/*
			 try {
					
				//String questionId = "A"+asID+"Q"+queryID.trim()+"S1";			
				
				stmt = dbcon.prepareStatement("select * from xdata_student_queries where assignment_id=? and question_id = ? and rollnum = ? and course_id=?");
				stmt.setInt(1, asID);
				stmt.setInt(2,Integer.parseInt(questionID));
				stmt.setString(3, studentID);
				stmt.setString(4,courseID);
				System.out.println(stmt.toString());
				rs = stmt.executeQuery();
				
				if(!rs.next()){
				
					stmt = dbcon.prepareStatement("INSERT INTO xdata_student_queries (dbid, queryid, rollnum, querystring, tajudgement,verifiedcorrect, assignment_id, question_id,course_id) VALUES(?,?,?,?,?,?,?,?,?)");
					stmt.setString(1, "dbid");
					stmt.setString(2, query_id);
					stmt.setString(3, studentID);
					stmt.setString(4, correctquery);
					stmt.setBoolean(5,true);
					stmt.setNull(6, Types.BOOLEAN);
					stmt.setInt(7,asID);
					stmt.setInt(8,Integer.parseInt(questionID));
					stmt.setString(9,courseID);
				}
				else{
					stmt = dbcon.prepareStatement("UPDATE xdata_student_queries SET querystring=? WHERE assignment_id=? and question_id = ? AND rollnum=? and course_id =?");
					
					stmt.setString(1, correctquery);
					stmt.setInt(2,asID);
					stmt.setInt(3, Integer.parseInt(questionID));
					stmt.setString(4, studentID);
					stmt.setString(5, courseID);
				} 
				
				stmt.executeUpdate();
			 */
	}

}
