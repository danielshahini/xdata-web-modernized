package instructor;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.MyConnection;

/**
 * Servlet implementation class UploadScore
 */
//@WebServlet("/UpdateMarks")
public class UpdateMarks extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(UpdateMarks.class.getName());
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdateMarks() {
        super();
        // TODO Auto-generated constructor stub
    }
    private float calculateScale(Connection conn, int assignmentId, int questionId, String userId,float marks)
    {
		float ans=0;
		boolean isScaled=true;
		float scaling_factor=0;
		float max_mark=0;
		String select_scale=" select * from xdata_qinfo where assignment_id=? and question_id=? ";
		try(PreparedStatement ps = conn.prepareStatement(select_scale)){
			//ps.setString(1, course_id);
			ps.setInt(1, assignmentId);
			ps.setInt(2,questionId);
			try(ResultSet rs = ps.executeQuery()){
			
				if(rs.next()){
					if(rs.getObject("scale") != null && ! rs.wasNull())
					{
						scaling_factor=rs.getFloat("scale");
					}
					else
					{
						isScaled=false;
					}
					
				}
			}
			
		}catch (SQLException e) {
			 logger.log(Level.SEVERE,"Error in DatabaseHelper.java : InsertIntoScores : \n" + e.getMessage(),e);
		}
		
		String select_max_marks="select max(marks) as mx_mark from xdata_instructor_query where assignment_id=? and question_id=? ";
		try(PreparedStatement ps = conn.prepareStatement(select_max_marks)){
			//ps.setString(1, course_id);
			ps.setInt(1, assignmentId);
			ps.setInt(2,questionId);
			try(ResultSet rs = ps.executeQuery()){
			
				if(rs.next()){
					if(rs.getObject("mx_mark") != null && ! rs.wasNull())
					{
						max_mark=rs.getFloat("mx_mark");
					}
					else
					{
						isScaled=false;
					}
					
				}
			}
			
		}catch (SQLException e) {
			 logger.log(Level.SEVERE,"Error in DatabaseHelper.java : InsertIntoScores : \n" + e.getMessage(),e);
		}
		
		if(isScaled==false)
			return marks;
		ans=marks * (scaling_factor/max_mark);
			return ans;
		
	}
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String userId = ""; float updatedMarks = 0; String status = ""; 
			userId = request.getParameter("userId");
			int assignmentId = Integer.parseInt(request.getParameter("assignmentId"));
			int questionId = Integer.parseInt(request.getParameter("questionId"));
			String comment = request.getParameter("feedbackTxt");
			try{
			 updatedMarks = Float.parseFloat(request.getParameter("marks"));
			}
			catch(NumberFormatException ex){
				status = ex.getMessage();
			}
			
			if(status.isEmpty()){
				//Connection conn = MyConnection.getExistingDatabaseConnection();
				try(Connection conn = MyConnection.getDatabaseConnection()){
					try(PreparedStatement stmt = conn.prepareStatement("select totalmarks from xdata_qinfo where assignment_id = ? and question_id = ?")){
							stmt.setInt(1, assignmentId);
							stmt.setInt(2, questionId); 
							try(ResultSet rs = stmt.executeQuery()){
								float maxMarks = 0;
								if(rs.next()){
									maxMarks = rs.getFloat("totalmarks");
								}
								
								if(updatedMarks > maxMarks){
									status = "Maximum exceeded";
								}
								
								if(status.isEmpty()){
									float scaled_mark=calculateScale(conn,assignmentId,questionId,userId,updatedMarks);
									try(PreparedStatement stmt1 = conn.prepareStatement("update xdata_student_queries set score = ?, feedback =?, manual_score=?,scaled_score=? where rollnum = ? and assignment_id = ? and question_id = ?")){
										stmt1.setFloat(1, updatedMarks);
										stmt1.setString(2, comment);
										stmt1.setFloat(3, updatedMarks);
										stmt1.setFloat(4, scaled_mark);
										stmt1.setString(5, userId);
										stmt1.setInt(6, assignmentId);
										stmt1.setInt(7, questionId);				
										stmt1.executeUpdate();
									} catch (SQLException sqlEx){
										logger.log(Level.SEVERE,sqlEx.getMessage(),sqlEx);
										status = sqlEx.getMessage();
									}
								}
							PrintWriter out = response.getWriter();
							out.write(status);
		
						}//rs ends
			}//Stmt ends
				
			} catch (Exception e) {
				logger.log(Level.SEVERE,e.getMessage(),e);
			}
	}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
	}
}
