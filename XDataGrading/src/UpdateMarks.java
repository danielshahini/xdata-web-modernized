

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
@WebServlet("/UpdateMarks")
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

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String userId = ""; float updatedMarks = 0; String status = ""; 
			userId = request.getParameter("userId");
			int assignmentId = Integer.parseInt(request.getParameter("assignmentId"));
			int questionId = Integer.parseInt(request.getParameter("questionId"));
			
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
									
									try(PreparedStatement stmt1 = conn.prepareStatement("update xdata_student_queries set score = ? where rollnum = ? and assignment_id = ? and question_id = ?")){
										stmt1.setFloat(1, updatedMarks);
										stmt1.setString(2, userId);
										stmt1.setInt(3, assignmentId);
										stmt1.setInt(4, questionId);				
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
