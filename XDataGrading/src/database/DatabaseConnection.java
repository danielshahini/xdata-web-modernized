package database;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {
	private static Logger logger=Logger.getLogger(DatabaseConnection.class.getName());
	private Connection dbConnection(String hostname, String dbName,
			String username, String passwd, String port) throws SQLException {
		Connection dbcon = null;
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException cnfe) {
			// out.println("<p style=\"font-family:arial;color:red;font-size:20px;background-color:white;\">Could not find the JDBC driver!</p>");
			System.exit(1);
		}
		try {
			dbcon = DriverManager.getConnection(hostname, username, passwd);
			

			if (dbcon != null) {
				//System.out.println("Connected successfully");
			}
		} catch (SQLException ex) {

			System.err.println("SQLException: " + hostname);
			System.err.println("SQLException: " + ex.toString());
			throw ex;
		}
		return dbcon;
	}
	
	public Connection dbConnection() throws SQLException {

		DatabaseProperties prop = new DatabaseProperties();
		String jdbcUrl = "jdbc:postgresql://" + prop.getHostname() + ":"
				+ prop.getPortNumber() + "/" + prop.getDbName();
		return dbConnection(jdbcUrl, prop.getDbName(), prop.getUsername1(),
				prop.getPasswd1(), prop.getPortNumber());

	}
	
	public String getJDBCUrl(DBConnectionInfo dbDetails){
		String url = "";
		String dbType = dbDetails.getDbType(dbDetails.getDbType());
		if(dbType.equals("Oracle")){			
			 url = "jdbc:oracle:thin:@"+dbDetails.getJdbc_Url()+":"+dbDetails.getDbName();
		}else if(dbType.equals("MySql")){			
			 url="jdbc:mysql://"+dbDetails.getJdbc_Url()+"/"+dbDetails.getDbName();
		}
		else if(dbType.equals("PostgreSQL")){
			 url="jdbc:postgresql://"+dbDetails.getJdbc_Url()+"/"+dbDetails.getDbName();
		}
		else if(dbType.equals("MSSQL")){			
			
			 url= "jdbc:sqlserver://" + dbDetails.getJdbc_Url() +
			   ";databaseName=" + dbDetails.getDbName()+";";
		}
		
		else if(dbType.equals("SQLite")){			
			url= "jdbc:sqlite:memory//" + dbDetails.getJdbc_Url() + ";databaseName=" + dbDetails.getDbName()+";";
		}
		 
		else if(dbType.equals("db2")){
		}
		return url;
		
	}
	public static Connection getConnection(DBConnectionInfo dbDetails) throws Exception{
	
		String dbType = dbDetails.getDbType(dbDetails.getDbType());
		
		Connection con = null;
		if(dbType.equals("Oracle")){			
			con = getOracleConnection(dbDetails);
			
		}else if(dbType.equals("MySql")){
			
			con = getMySqlConnection(dbDetails); 
		}
		else if(dbType.equals("PostgreSQL")){
			con=getPostgreSQLConnection(dbDetails);
		}
		else if(dbType.equals("db2")){
			 
		}
		else if(dbType.equals("MSSQL")){
			con = getMicorsoftSQLConnection(dbDetails);
		}
		else if(dbType.equals("SQLite")){
			con = getSQLiteConnection(dbDetails);
		}
			return con;
	}
	private static Connection getOracleConnection(DBConnectionInfo dbDetails) throws Exception {
		    String driver = "oracle.jdbc.driver.OracleDriver";
//		    String url = "jdbc:oracle:thin:@localhost:1521:scorpian";
		    
		    String username = dbDetails.getDbUser();
		    String password = dbDetails.getDbPwd();
		    
		    String url = "jdbc:oracle:thin:"+username+"/"+password+"@"+dbDetails.getJdbc_Url()+":"+dbDetails.getDbName();
		    System.out.println("JDBC URL For Oracle connection : " + url);
		    Class.forName(driver); // load Oracle driver
		    Connection conn = DriverManager.getConnection(url, username, password);
	
					   
		    return conn;
		  }
 
	 private static Connection getMySqlConnection(DBConnectionInfo dbDetails) throws Exception {
		    String driver = "com.mysql.cj.jdbc.Driver";
		    //String url = "jdbc:mysql://localhost/tiger";
		    String url="jdbc:mysql://"+dbDetails.getJdbc_Url()+"/"+dbDetails.getDbName();
		    String username = dbDetails.getDbUser();
		    String password = dbDetails.getDbPwd();
		    Class.forName(driver); // load MySQL driver
		    Connection conn = DriverManager.getConnection(url, username, password);
	
					   
		    return conn;
		  }
		  
		  private static Connection getPostgreSQLConnection(DBConnectionInfo dbDetails) throws Exception{
			  String driver = "org.postgresql.Driver";
			  	// String jdbcUrl = "jdbc:postgresql://" + host + "/" + dbName;
			  String url="jdbc:postgresql://"+dbDetails.getJdbc_Url()+"/"+dbDetails.getDbName();
			  String username = dbDetails.getDbUser();
			  String password = dbDetails.getDbPwd();
			  Class.forName(driver); // load MySQL driver
			  Connection conn = DriverManager.getConnection(url, username, password);

				   
			   return conn;
		  }
		  
		  private static Connection getSQLiteConnection(DBConnectionInfo dbDetails) throws Exception {
			   String driver = "org.sqlite.JDBC";
			    //String url = "jdbc:sqlite://localhost/tiger";
			   String url="jdbc:sqlite://"+dbDetails.getJdbc_Url()+"/"+dbDetails.getDbName();
			   String username = dbDetails.getDbUser();
			   String password = dbDetails.getDbPwd();
			   Class.forName(driver);
			   Connection conn = DriverManager.getConnection(url, username, password);
			   
			   return conn;
		  }
		  
	
		  private static Connection getMicorsoftSQLConnection (
				  DBConnectionInfo dbDetails) throws Exception{
				Connection con = null;
				try{
					String dbUserName = dbDetails.getDbUser();
					String dbPassword = dbDetails.getDbPwd();
					Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

					String dbHostName = "windowsHostName";
					String dbName = dbDetails.getDbName();
					
					////String connectionUrl = "jdbc:sqlserver://localhost:1433;" +
					//		   "databaseName=AdventureWorks;user=MyUserName;password=*****;";
					//		Connection con = DriverManager.getConnection(connectionUrl);

					String connectionUrl = "jdbc:sqlserver://" + dbDetails.getJdbc_Url() +
					   ";databaseName=" + dbName + ";user=" + dbUserName + ";password=" + dbPassword + ";";
					con = DriverManager.getConnection(connectionUrl);
				}catch(Exception e){
					logger.log(Level.SEVERE,e.getMessage(),e);
					e.printStackTrace();
					throw e;
				}
				
				return con;
			}
		  

}
