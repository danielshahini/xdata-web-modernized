package database;
import java.sql.*;
public class DatabseConnection {
	/*public Connection dbConnection(String hostname, String dbName, String username, String passwd, String port) throws Exception{
		Connection dbcon = null;
		try{
			Class.forName("org.postgresql.Driver");
		} 
		catch (ClassNotFoundException cnfe){
    	//out.println("<p style=\"font-family:arial;color:red;font-size:20px;background-color:white;\">Could not find the JDBC driver!</p>");
			System.exit(1);
		}
		String loginUrl = "jdbc:postgresql://" + hostname + ":" + port + "/" + dbName;;
		try {
     // Class.forName("org.postgresql.Driver");
			dbcon = DriverManager.getConnection(loginUrl, username, passwd);
			if(dbcon!=null){
				System.out.println("Connected successfullly");
			} 
		}
		catch (SQLException ex) {
			System.err.println("SQLException: " + loginUrl);
			System.err.println("SQLException: " + ex.toString());
			throw ex;
		}
		return dbcon;
	}*/

}
