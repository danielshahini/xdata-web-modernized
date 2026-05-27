package instructor;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import database.DatabaseConnection;

/**
 * Servlet implementation class EvaluateAssignment
 */
//@WebServlet("/EvaluateAssignment")
public class EvaluateAssignment extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(EvaluateQuestion.class.getName());
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EvaluateAssignment() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session=request.getSession();		
		String courseID = (String) request.getSession().getAttribute(
				"context_label");
		String assignment_id = request.getParameter("assignment_id");
		String question_id = "";// (String)request.getParameter("question_id");
		
		String questionSel="select * from xdata_qinfo where assignment_id = ? and course_id= ? ";
		String updateAssignmentStatus = "update xdata_assignment set evaluationstatus = true where assignment_id = ? and course_id= ?";
		String evalQuestion = "";
		EvaluateQuestion evaluateQuestion = new EvaluateQuestion();
		
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){
			
			try(PreparedStatement selStmt=dbcon.prepareStatement(questionSel)){
				
				selStmt.setInt(1, Integer.parseInt(assignment_id));
				selStmt.setString(2,courseID);
				
				try(ResultSet r=selStmt.executeQuery()){
					while(r.next()){
						question_id = String.valueOf(r.getInt("question_id"));
						evaluateQuestion.evaluateQuestion(assignment_id, question_id, courseID);
					}
				}
			}
			try(PreparedStatement updateStmt=dbcon.prepareStatement(updateAssignmentStatus)){
				updateStmt.setInt(1, Integer.parseInt(assignment_id));
				updateStmt.setString(2,courseID);
				updateStmt.executeQuery();
				}
			
		} catch (SQLException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw new ServletException(e);
		}
		//String forwardUrl = "assignmentScores.jsp?AssignmentID="+request.getParameter("AssignmentID")+"&&ResultUploadStatus=fail";
		//out.write("<script>window.location.href='"+forwardUrl+"'</script>");
		//response.sendRedirect("asgnmentList.jsp?assignmentId="+assignment_id+"&&showQuestions=true");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
