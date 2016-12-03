


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import database.DBConnectionInfo;
import database.DatabaseConnection;

/**
 * Servlet implementation class EditDatabaseConnection
 */
@WebServlet("/EditDatabaseConnection")
public class EditDatabaseConnection extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(EditDatabaseConnection.class.getName());
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EditDatabaseConnection() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String courseId =  request.getParameter("course_id");
		String dbConnectionName = request.getParameter("dbConnectionName");
		String dbName = request.getParameter("dbName");
		String databaseType = request.getParameter("databaseType");
		String jdbcurl = request.getParameter("jdbcurl");
		String dbuserName = request.getParameter("dbuserName");
		String dbPassword = request.getParameter("dbPassword");
		String testUserName = request.getParameter("testUserName");
		String testPassword = request.getParameter("testPassword");
		DBConnectionInfo dbData = new DBConnectionInfo();
		dbData.setConnName(dbConnectionName);
		dbData.setDbName(dbName);
		dbData.setDbPwd(dbPassword);
		dbData.setDbUser(dbuserName);
		dbData.setJdbc_Url(jdbcurl);
		dbData.setDbType(databaseType);

		new DatabaseConnection();
		try(Connection dbcon = DatabaseConnection.getConnection(dbData)){
				//Check DB user name and password
				if(dbcon == null){ 
 					response.sendError(HttpServletResponse.SC_NOT_FOUND);
 				} 
				//Now set username and password for testing the TESTER connection 
				dbData.setDbUser(testUserName);
				dbData.setDbPwd(testPassword);
				
				new DatabaseConnection();
				try(Connection testCon = DatabaseConnection.getConnection(dbData)){
					if(testCon == null){ 
	 					response.sendError(HttpServletResponse.SC_NOT_FOUND);
	 				}  
				}catch (SQLException e) {
					logger.log(Level.SEVERE,"TESTER connection failed:"+e.getMessage(),e);
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
				} catch (Exception e) {
					logger.log(Level.SEVERE,"TESTER Connection Failed:"+e.getMessage(),e);
					response.sendError(HttpServletResponse.SC_NOT_FOUND);	
				}
    		} 
			catch (SQLException e) {
				logger.log(Level.SEVERE,"Grader connection failed"+e.getMessage(),e);
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			} catch (Exception e) {
				logger.log(Level.SEVERE,"Grader connection failed"+e.getMessage(),e);
				response.sendError(HttpServletResponse.SC_NOT_FOUND);	
			}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String courseId =  request.getParameter("course_id");
		int connectionId = Integer.parseInt(request.getParameter("connection_id"));
		String dbConnectionName = request.getParameter("dbConnectionName");
		String dbName = request.getParameter("dbName");
		String databaseType = request.getParameter("databaseType");
		String jdbcurl = request.getParameter("jdbcurl");
	
		String dbuserName = request.getParameter("dbuserName");
		String dbPassword = request.getParameter("dbPassword");
		String testUserName = request.getParameter("testUserName");
		String testPassword = request.getParameter("testPassword");
		
		DBConnectionInfo dbData = new DBConnectionInfo();
		dbData.setConnName(dbConnectionName);
		dbData.setDbName(dbName);
		dbData.setDbPwd(dbPassword);
		dbData.setDbUser(dbuserName);
		dbData.setJdbc_Url(jdbcurl);
		dbData.setDbType(databaseType);
		DatabaseConnection db = new DatabaseConnection();
		
		String jdbcUrlToSave = db.getJDBCUrl(dbData);
		
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){
			try(PreparedStatement stmt = dbcon
					.prepareStatement("update xdata_database_connection set connection_name=?,database_type=?, jdbc_url=?,database_user=?,database_password=?,test_user=?,test_password=?, database_name=?,jdbcdata=? where course_id=? and connection_id=?" )){
				stmt.setString(1, dbConnectionName);
				stmt.setString(2, databaseType);
				stmt.setString(3, jdbcUrlToSave); 
				stmt.setString(4, dbuserName); 
				stmt.setString(5, dbPassword); 
				stmt.setString(6, testUserName); 
				stmt.setString(7, testPassword);
				stmt.setString(8, dbName); 
				stmt.setString(9,jdbcurl);
				stmt.setString(10, courseId);
				stmt.setInt(11, connectionId); 
				
				logger.log(Level.FINE,"STATMENT : "+stmt);
				stmt.executeUpdate();
			}//try block for statement ends

		} catch (SQLException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw new ServletException(e);
		} 
	}

}
