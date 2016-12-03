

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
 * Servlet implementation class CreateUser
 */
@WebServlet("/CreateUser")
public class CreateUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(CreateUser.class.getName()); 
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateUser() {
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

		int internalUserId = 1;
		String prefix="XD";
		String user_id = "";
		String userName=request.getParameter("userName");
		String userPwd = request.getParameter("password");
		String login_user_id = request.getParameter("LoginUserId");
		String email =request.getParameter("email"); 
		String hashedPassword = DigestUtils.md5Hex(userPwd) ;
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){
			//write a method to return max of the user id value and insert nxt value here
			try(PreparedStatement stmt1 = dbcon.prepareStatement("select internal_user_id from xdata_users where internal_user_id like '%XD%'")){
				ResultSet rSet = stmt1.executeQuery();
				
				int max_id = this.getMaxId(rSet);
				user_id = prefix+(max_id+1);
				
				try(PreparedStatement stmt =  dbcon.prepareStatement("insert into xdata_users values(?,?,?,?,?,?,?,?)")){
					stmt.setString(1,user_id);
					stmt.setString(2,userName);
					stmt.setString(3,email);
					stmt.setString(4,""); 
					stmt.setString(5,hashedPassword);
					stmt.setString(6,"");
					stmt.setString(7,"");  
					stmt.setString(8,login_user_id);
					stmt.executeUpdate(); 
					
				}//try block for stmt ends
			}//try block for stmt1 ends
			response.sendRedirect("ViewUsers.jsp");
		}catch (SQLException sep) {
			logger.log(Level.SEVERE,"Could not connect to database: " + sep.getMessage(),sep);
			//sep.printStackTrace();
			throw new ServletException(sep);
		}
	}
	
	public int getMaxId(ResultSet rs) throws SQLException{
		int max_id=0;
		String usr_id = "";
		int curr_id =0;
		int curr_max = 0;
		int count = 0;
		while(rs.next()){
			count++;
			usr_id = rs.getString("internal_user_id");
			//Check user ids created by XDATA system alone
			//DB can have moodle records also
			if(usr_id.contains("XD")){
				curr_id= Integer.valueOf(usr_id.substring(2));
			/*	if(curr_max>curr_id){
					curr_max = curr_id;
					max_id=curr_max;
				}else{
					max_id=curr_id;
					if (count ==1){ 
						curr_max  =curr_id;
					}*/
				if(curr_id > max_id) {
					max_id = curr_id;
	            }
				
			}
		}
		return max_id;
	}

}
