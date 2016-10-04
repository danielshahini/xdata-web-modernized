

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import database.DatabaseConnection;
import evaluation.FailedDataSetValues;

import evaluation.TestAnswer;
import evaluation.TestAssignment;

/**
 * Servlet implementation class checkQueryEquivalence
 */
@WebServlet("/CheckQueryEquivalence")
public class CheckQueryEquivalence extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CheckQueryEquivalence() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		String asId = request.getParameter("assignment_id");
		String questionId = request.getParameter("question_id");
		String course_id= (String)session.getAttribute("context_label");
		//Code here.....   
		TestAssignment ta = new TestAssignment(); 
		try{ 
		String status = ta.checkQueryEquivalence( Integer.parseInt(asId),Integer.parseInt(questionId),course_id);
		if(!(status.equalsIgnoreCase("failed"))){
			response.sendRedirect("ListOfTesterQuestions.jsp?AssignmentID="+ asId);
		}else{  
			throw new ServletException("Query equivalence failed.");
		}
		}catch(Exception e){
			throw new ServletException(e);
		}		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		String asId = request.getParameter("assignment_id");
		String questionId = request.getParameter("question_id");
		String courseID = (String)session.getAttribute("context_label");
		int assignID = Integer.parseInt(asId);
		int qID = Integer.parseInt(questionId);
		
		boolean isDataSetExists = false;
		try{
		Connection dbcon = (new DatabaseConnection()).dbConnection();
		int queryWithoutDS = 0;
		PreparedStatement stmt1 = dbcon 
				.prepareStatement("SELECT * FROM xdata_instructor_query  where assignment_id=? and course_id=? and question_id=? order by query_id");
		stmt1.setInt(1, assignID);
		stmt1.setString(2, courseID);
		stmt1.setInt(3,qID);
		 
		ResultSet rs1 = stmt1.executeQuery();
		while (rs1.next()) {
			int query_id = rs1.getInt("query_id");
			//For each query check if data set exists. If no datasets are there for a query then return error.
			String datasets="Select datasetid from xdata_datasetvalue where assignment_id=? and question_id=? and query_id=? and course_id = ?";
			PreparedStatement pstmt1=dbcon.prepareStatement(datasets);
			
			pstmt1.setInt(1, assignID);
			pstmt1.setInt(2,qID);
			pstmt1.setInt(3,query_id);
			pstmt1.setString(4,courseID);
			ResultSet rset=pstmt1.executeQuery();
			
			if(rset.next()){
				isDataSetExists = true;
			}else{
				isDataSetExists = false;
				queryWithoutDS = query_id;
				break;
			}
		}//End while sql query resultset
		if(!isDataSetExists){
			throw new ServletException("Please generate Data Set for query "+queryWithoutDS);
		}
		}catch(SQLException e){
			throw new ServletException(e);
		}
		response.sendRedirect("ListOfTesterQuestions.jsp?AssignmentID="+ asId);
	}

}
