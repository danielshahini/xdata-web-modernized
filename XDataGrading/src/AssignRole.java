

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;

import database.DatabaseConnection;

/**
 * Servlet implementation class AssignRole
 */
@WebServlet("/AssignRole")
public class AssignRole extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(AssignRole.class.getName());   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AssignRole() {
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
		
		String user_id="";
		String userLoginId = request.getParameter("loginId");
		String role=request.getParameter("role");
		String course_id=request.getParameter("CourseId");
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){
			
			try(PreparedStatement stmt1 = dbcon.prepareStatement("select * from xdata_users where login_user_id=?")){
			stmt1.setString(1,userLoginId);
			try(ResultSet rs = stmt1.executeQuery()){
				if(rs.next()){
					user_id = rs.getString("internal_user_id");
				}
				else{
					throw new ServletException("The specified user name does not exist in the records.");
				}
				//check if the same user with same role already exists
				try(PreparedStatement stmt2 = dbcon.prepareStatement("select * from xdata_roles where internal_user_id=? and course_id= ? and role =?")){
					stmt2.setString(1,user_id);
					stmt2.setString(2,course_id); 
					stmt2.setString(3,role);
					try(ResultSet resultset = stmt2.executeQuery()){
						if(resultset.next()){
							//Existing record, so update
							try(PreparedStatement stmt =  dbcon.prepareStatement("update xdata_roles set role = ? where internal_user_id=? and course_id = ?")){
								stmt.setString(1,role); 
								stmt.setString(2,user_id);
								stmt.setString(3,course_id); 
								stmt.executeUpdate(); 
							}
						}
						else{
						
							//Its new record, insert
						try(PreparedStatement stmt =  dbcon.prepareStatement("insert into xdata_roles values(?,?,?,?)")){
							stmt.setString(1,user_id);
							stmt.setString(2,userLoginId);
							stmt.setString(3,course_id);
							stmt.setString(4,role); 
							stmt.executeUpdate();  
						}
						}
						}//try for resultset ends
					}//try for statement stmt2 ends
				}//try for resultset ends
			}//try for statement ends
			response.sendRedirect("ViewUsers.jsp");
			
		}catch (SQLException sep) {
			logger.log(Level.SEVERE,"Could not connect to database: " + sep.getMessage(),sep);
			//sep.printStackTrace();
			throw new ServletException(sep);
		}
	
	}

}
