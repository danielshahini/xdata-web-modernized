

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
 * Servlet implementation class UpdateDatabaseConnection
 */
@WebServlet("/UpdateDatabaseConnection")
public class UpdateDatabaseConnection extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(UpdateDatabaseConnection.class.getName());
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdateDatabaseConnection() {
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
		Connection dbcon = null; 
		try {
				new DatabaseConnection();
				dbcon = DatabaseConnection.getConnection(dbData);
				if(dbcon == null){ 
 					response.sendError(HttpServletResponse.SC_NOT_FOUND);
 				} 
				
				dbData.setDbUser(testUserName);
				dbData.setDbPwd(testPassword);
				new DatabaseConnection();
				dbcon = DatabaseConnection.getConnection(dbData);
				
 				if(dbcon == null){ 
 					response.sendError(HttpServletResponse.SC_NOT_FOUND);
 				} 
			 
    		} 
			catch (SQLException e) {
				logger.log(Level.SEVERE,e.getMessage(),e);
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			} catch (Exception e) {
				logger.log(Level.SEVERE,e.getMessage(),e);
				response.sendError(HttpServletResponse.SC_NOT_FOUND);				
			}finally{
				try {
					dbcon.close();
				} catch (SQLException e) {
					logger.log(Level.SEVERE,e.getMessage(),e);
				}
			}
			 
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 
		String courseId =  request.getParameter("course_id");
		String dbConnectionName = request.getParameter("dbConnectionName");
		String dbName = request.getParameter("dbName");
		String databaseType = request.getParameter("databaseType");
		String jdbcurl = request.getParameter("jdbcurl");
		String jdbcData =  request.getParameter("jdbcurl");
		String schemaid = request.getParameter("schemaid");	
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
		Connection dbcon = null;
  
		try {
			dbcon = (new DatabaseConnection()).dbConnection();
			try(PreparedStatement stmt  = dbcon
					.prepareStatement("INSERT INTO xdata_database_connection VALUES (?,DEFAULT,?,?,?,?,?,?,?,?,?)")){
		 
				stmt.setString(1, courseId);
				stmt.setString(2, dbConnectionName);
				stmt.setString(3, databaseType); 
				stmt.setString(4, jdbcUrlToSave);
				stmt.setString(5, dbuserName); 
				stmt.setString(6, dbPassword);
				stmt.setString(7, testUserName);
				stmt.setString(8, testPassword);
				stmt.setString(9, dbName);
				stmt.setString(10,jdbcData);
				stmt.executeUpdate();
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw new ServletException(e);
		}
	}
}
