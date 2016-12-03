

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import database.DatabaseConnection;

/**
 * Servlet implementation class DeleteQuestionWithQueries
 */
@WebServlet("/DeleteQuestionWithQueries")
public class DeleteQuestionWithQueries extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(DeleteQuestionWithQueries.class.getName());
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DeleteQuestionWithQueries() {
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
		
		HttpSession session = request.getSession(false);
		int assign_id = Integer.parseInt(request.getParameter("assignment_id"));
		int question_id = Integer.parseInt(request.getParameter("question_id"));
		String courseID = (String) request.getSession().getAttribute(
				"context_label");
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){
					
				  try(PreparedStatement stmt = dbcon.prepareStatement("select * from xdata_instructor_query where assignment_id=? and course_id=? and question_id=?")){
						stmt.setInt(1, assign_id);
						stmt.setString(2, courseID);
						stmt.setInt(3,question_id);
						try(ResultSet rs = stmt.executeQuery()){
						while(rs.next()){
							try(PreparedStatement stmt1 = dbcon.prepareStatement("delete from xdata_datasetvalue where assignment_id = ? and question_id = ? and query_id=? and course_id= ?")){
								stmt1.setInt(1,assign_id);
								stmt1.setInt(2,question_id);
								stmt1.setInt(3,rs.getInt("query_id"));
								stmt1.setString(4,courseID);
								stmt1.executeUpdate();
							}//close try block for stmt1
						} 
						}//close result set try block
				  }//close statement stmt try block
						
				try(PreparedStatement stmt1 = dbcon 
						.prepareStatement("delete from xdata_instructor_query where assignment_id=? and course_id=? and question_id=?")){
					stmt1.setInt(1, assign_id);
					stmt1.setString(2, courseID);
					stmt1.setInt(3,question_id);
					stmt1.executeUpdate(); 
				}
				try(PreparedStatement stmt1 = dbcon.prepareStatement("delete from xdata_qinfo where assignment_id = ? and course_id=? and question_id = ?")){
					stmt1.setInt(1, assign_id);
					stmt1.setString(2, courseID);
					stmt1.setInt(3,question_id);
					stmt1.executeUpdate();
				}
		
			String remoteLink = "";
			if(((String)session.getAttribute("LOGIN_USER")).equalsIgnoreCase("tester")){
				remoteLink ="ListOfTesterQuestions.jsp?AssignmentID=" + assign_id;
			}else{
				remoteLink ="asgnmentList.jsp?assignmentId=" + assign_id+"&&showQuestions=true";
			}
			response.sendRedirect(remoteLink);
			}
			catch(Exception err){ 
				logger.log(Level.SEVERE,err.getMessage(),err);
				throw new ServletException(err);
			}
	
		}

}
