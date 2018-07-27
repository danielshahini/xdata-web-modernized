package instructor;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
 * Servlet implementation class TestUploadedFile
 */
//@WebServlet("/TestUploadedFile")
public class TestUploadedFile extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(TestUploadedFile.class.getName());
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TestUploadedFile() {
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
	 * This servlet method checks the request for requesting page and based on that the uploaded schema or datafile is 
	 * is tested or tested and uploaded.
	 *   
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	HttpSession session = request.getSession(false);
		String courseId = (String) request.getSession().getAttribute(
				"context_label");
		String schema_id =  request.getParameter("schema_id");
		String sampledata_id = request.getParameter("sample_data_id");
		//String schema_id = s.substring(0,s.indexOf("-"));
		//String sampledata_id =s.substring(s.indexOf("-")+1,s.lastIndexOf("-"));
		String requestingPage = request.getParameter("requestingPage");
		int i = 0;
		String dbUser="";
		String dbPassword="";
		String jdbc_url="";
		String connection_name="";
		String script_name=""; 
		String dbName="";
		String ddltext="";
		String ddl = "";
		int connection_id =0;
		
		if(!request.getParameter("dbConnection").equals("select")){
			connection_id = Integer.parseInt(request.getParameter("dbConnection"));
		} 
		//Get Existing DB connection to get connection data for testing the scripts
		 try(Connection dbcon = (new DatabaseConnection()).dbConnection()){				
				//Get assignment id
			 try(PreparedStatement stmt =  dbcon.prepareStatement("SELECT connection_name,jdbc_url,database_user," +
			 		"database_password,connection_name from xdata_database_connection where connection_id=?")){
				stmt.setInt(1,connection_id);
				try(ResultSet rs = stmt.executeQuery()){ 
					while(rs.next()){
						connection_name = rs.getString("connection_name");
						jdbc_url = rs.getString("jdbc_url");
						dbUser = rs.getString("database_user");
						dbPassword = rs.getString("database_password");	
					}
				}
			 }
		 
		/*************************************************/
		if((requestingPage.trim()).equals("sampleDataUpload")){
			if(schema_id != null){
				i = Integer.parseInt(schema_id.trim());
				try(PreparedStatement stmt1 =  dbcon.prepareStatement("SELECT ddltext from " +
						"xdata_schemainfo where course_id=? and schema_id=?")){
				
						
					//split and get sample data from sample data file
					//Get sample_data Id from UI along with schema _ id
					stmt1.setString(1,courseId);
					stmt1.setInt(2,i);
					
					try(ResultSet rs1 = stmt1.executeQuery()){ 
						while(rs1.next()){
							//script_name = rs1.getString("sample_data");
							ddltext = rs1.getString("ddltext");				
						}	
				
						//schema file already exists and no file for new upload - then just test the file
						if(script_name != null && sampledata_id != null && session.getAttribute("FileContents") == null){
					    
							try(PreparedStatement stmnt =  dbcon.prepareStatement("SELECT sample_data from " +
									"xdata_sampledata where course_id=? and schema_id=? and sampledata_id = ?")){

								stmnt.setString(1,courseId);
								stmnt.setInt(2,i);
								stmnt.setInt(3, Integer.getInteger(sampledata_id));
								try(ResultSet rSet = stmnt.executeQuery()){
										if(rSet.next()){
											script_name = rSet.getString("sample_data");
										}
									
							    		boolean status = this.testQueries(ddltext+script_name, jdbc_url, dbUser, dbPassword,dbName);
										if(status){
											response.sendRedirect("sampleDataUpload.jsp?test=success");
											return;
										} 
										else{
											response.sendRedirect("sampleDataUpload.jsp?error=corrupted");
											return;
										} 
								}//sample data test stmt try
							}//rSet ends
						}
						/*else if(script_name != null && session.getAttribute("FileContents") != null){
							//There exists schema file and another new file upload req for same schema comes
							//throw error.
							//logger.log(Level.WARNING,"Sample data file already exists for this schema_id");
							response.sendRedirect("sampleDataUpload.jsp?test=success");
							//response.sendRedirect("sampleDataUpload.jsp?exists");
							return;
						}*/
						else{
							//Test and upload the new file.
							String file = (String)session.getAttribute("FileContents");
							// Test the file by running it on database creating temp tables and inserting data
							boolean status = this.testQueries(ddltext+file, jdbc_url, dbUser, dbPassword,dbName);
							String schemaFileName= (String)session.getAttribute("DataFileName");
							
							if(status && session.getAttribute("FileContents") == null){
								dbcon.close();
								response.sendRedirect("sampleDataUpload.jsp?test=success");
								return;
							}  
							else if( !status){
								dbcon.close();
								response.sendRedirect("sampleDataUpload.jsp?error=corrupted");					
								return;
							}else{ 	int sampleDataId = 1;
								try(PreparedStatement statemnt = dbcon.prepareStatement("select MAX(sampledata_id) as id from xdata_Sampledata")){
								
									try(ResultSet rset = statemnt.executeQuery()){
										if(rset.next()){
											sampleDataId= rset.getInt("id")+1;
										}
									}
								}
								try(PreparedStatement stmt =  dbcon.prepareStatement("insert into " +
										"xdata_sampledata(sample_data_name,sample_data,course_id,schema_id,sampledata_id) " +
										"values (?,?,?,?,?)")){
									stmt.setString(1,schemaFileName);
									stmt.setString(2, (String)session.getAttribute("FileContents")); 
									stmt.setString(3, courseId);
									stmt.setInt(4, i); 
									stmt.setInt(5,sampleDataId);
						
									stmt.executeUpdate(); 
								}
								
				 			/*try(PreparedStatement stmnt = dbcon.prepareStatement("update xdata_schemainfo set " +
				 					"sample_data_name = ?, sample_data = ? where course_id =? and schema_id=?")){
									stmnt.setString(1, schemaFileName);
									stmnt.setString(2, file);				 
									stmnt.setString(3, courseId);
									stmnt.setInt(4,Integer.parseInt(schema_id.trim()));
									stmnt.executeUpdate();
								   
									dbcon.close();
				 				}*/
								response.sendRedirect("sampleDataUpload.jsp?upload=success");	
								return;
							}
						
							/**********/
						} 
					   }//rs1 ends
					}//stmt1 ends
				
			} 
		}//If requesting page is sample data upload	ends
				 
		else if(requestingPage.trim().equals("schemaUpload")){ 
			if(schema_id != null && !schema_id.trim().equals("")){
				 i = Integer.parseInt(schema_id.trim());
					
					try(PreparedStatement stmt1 =  dbcon.prepareStatement("SELECT ddltext from xdata_schemainfo where course_id=? and schema_id=?")){
						stmt1.setString(1,courseId);
						stmt1.setInt(2,i);  
						try(ResultSet rs1 = stmt1.executeQuery()){ 
							while(rs1.next()){
								script_name = rs1.getString("ddltext");
							}
							//String script = script_name.replace("\\","'");
							boolean status = this.testQueries(script_name, jdbc_url, dbUser, dbPassword,dbName);
						 
							if(status){ 
									dbcon.close();
									response.sendRedirect("schemaUpload.jsp?test=success");
							}
							else{
								dbcon.close(); 
								response.sendRedirect("schemaUpload.jsp?error=corrupted");
							}
						}//rs1 ends
					}//stmt1 ends
			 }
			 else{//If schema id is null - then this is new file
				 //Test and upload functionality
					//Get and test the file
				 //GET FILE CONTENTS HERE SOMEHOW _ USE SESSION
						String file = (String)session.getAttribute("FileContents");
						boolean status = this.testQueries(file, jdbc_url, dbUser, dbPassword,dbName);
						 String schemaFile = (String)session.getAttribute("schemaFileName");
						 //If testing is success and the file contents in session are empty,
						 // then nothing to upload
						if(status && session.getAttribute("FileContents") == null){
							dbcon.close();
							response.sendRedirect("schemaUpload.jsp?error=nodata");
							return;
						} 
						else if( !status){
							dbcon.close();
							response.sendRedirect("schemaUpload.jsp?error=corrupted");					
							return;
						}else{
							int newSchemaId = 1;  
						//Get schema id
						try(PreparedStatement statement =  dbcon.prepareStatement("SELECT MAX(schema_id) as schemaId from xdata_schemainfo")){
							try(ResultSet rset = statement.executeQuery()){
								 
					 			if(rset.next()){
									newSchemaId = rset.getInt("schemaId") + 1;
								} 
					 			PreparedStatement stmnt;
					 			stmnt = dbcon.prepareStatement("Insert into xdata_schemainfo values(?, ?, ?, ? )");
								stmnt.setString(1, courseId);
								stmnt.setInt(2, newSchemaId);
								stmnt.setString(3, schemaFile); 
								stmnt.setString(4, file); 
								
								
								stmnt.executeUpdate();
							  
								dbcon.close();
								response.sendRedirect("schemaUpload.jsp?upload=success");	
								return;
								}//resultset ends
						}//Prepared Statment ends
					}
			 }// If schema-id is null - then new request processing ends here
		}// If requesting page is "Schemaupload" ends 
				/*************************************************/
		}catch(Exception e){
			logger.log(Level.SEVERE,e.getMessage(),e);
				if(requestingPage.equals("schemaUpload")){
					response.sendRedirect("schemaUpload.jsp?error=corrupted");
				}
				else{
					response.sendRedirect("sampleDataUpload.jsp?error=corrupted"); 
				}
		}
		 session.removeAttribute("FileContents");
	}
	  
	/**
	 * This method creates temporary tables and inserts data into those tables for testing schema and data files
	 * It does not allow alter,delete or drop commands and those commands are skipped while running.
	 * If no error occurs, it returns true.
	 *
	 * @param script
	 * @param jdbc_url
	 * @param dbUser
	 * @param dbPassword
	 * @param dbName
	 * @return
	 */
	protected boolean testQueries(String script,String jdbc_url,String dbUser,String dbPassword,String dbName){
		
		String tempFile = "/tmp/testScripts";
		Connection givenConnection = null;
		
		try{
			FileOutputStream fos = new FileOutputStream(tempFile);
			fos.write(script.getBytes());
			fos.close(); 
			
			ArrayList<String> listOfQueries  = this.parseQueries(script);
			 
			String[] inst = listOfQueries.toArray(new String[listOfQueries.size()]);
			try{ 
				Class.forName("org.postgresql.Driver");
			} 
			catch (ClassNotFoundException cnfe){
				logger.log(Level.SEVERE,cnfe.getMessage(),cnfe);
	    	//out.println("<p style=\"font-family:arial;color:red;font-size:20px;background-color:white;\">Could not find the JDBC driver!</p>");
				System.exit(1);
			}
			try {
				givenConnection = DriverManager.getConnection(jdbc_url, dbUser, dbPassword);
				if(givenConnection!=null){
				}
			}
			catch (SQLException ex) {
				
				logger.log(Level.FINE,"SQLException: " + jdbc_url +" --"+ dbUser+"--"+dbPassword);
				logger.log(Level.SEVERE,"SQLException: " + ex.toString(),ex);
				throw ex;
			}
			PreparedStatement stmt;
			
			for (int j = 0; j < inst.length; j++) {
				// we ensure that there is no spaces before or after the request string  
				// in order to not execute empty statements  
				if (!inst[j].trim().equals("")) {
					
					String temp = inst[j].trim().replaceAll("(?i)^\\s*create\\s+table\\s+", "create temporary table ");
					if(temp.contains("drop table") || temp.contains("alter database") || temp.contains("delete from")
							|| temp.contains("alter user") || temp.contains("alter password") || temp.contains("alter login")
							|| temp.contains("alter ")){
						 
						continue;
					} 
					//String q = temp.replace("''","'" );
					stmt = givenConnection.prepareStatement(temp);
					stmt.executeUpdate();						
				}
			}
		}catch(Exception e){
			logger.log(Level.SEVERE,e.getMessage(),e);
			return false;
		}
		finally{
			try {
				givenConnection.close();
			} catch (SQLException e) {
				logger.log(Level.SEVERE,e.getMessage(),e);
				return false;
			}
		}
	
		return true;
	}
	
	/**
	 * This method parses the string content taken as input and reads it line by line and creates queries
	 * and returns the parsed query
	 * 
	 * @param queriesFromFile
	 * @return
	 * @throws ServletException
	 */
	protected ArrayList<String> parseQueries(String queriesFromFile) throws ServletException{
		
		   String queryLine =  new String();  
	        StringBuffer sBuffer =  new StringBuffer();  
	        ArrayList<String> listOfQueries = new ArrayList<String>();  
	        String path =  "/tmp/testScripts";
	          
	        try    
	        {    
	           FileReader fr =     new FileReader(new File(path));    
	        	
	            BufferedReader br = new BufferedReader(fr);    
	        
	            //read the SQL file line by line  
	            while((queryLine = br.readLine()) != null)    
	            {    
	                // ignore comments beginning with #  
	                int indexOfCommentSign = queryLine.indexOf('#');  
	                if(indexOfCommentSign != -1)  
	                {  
	                    if(queryLine.startsWith("#"))  
	                    {  
	                        queryLine = new String("");  
	                    }  
	                    else   
	                        queryLine = new String(queryLine.substring(0, indexOfCommentSign-1));  
	                }  
	                // ignore comments beginning with --  
	                indexOfCommentSign = queryLine.indexOf("--");  
	                if(indexOfCommentSign != -1)  
	                {  
	                    if(queryLine.startsWith("--"))  
	                    {  
	                        queryLine = new String("");  
	                    }  
	                    else   
	                        queryLine = new String(queryLine.substring(0, indexOfCommentSign-1));  
	                }  
	                // ignore comments surrounded by /* */  
	                indexOfCommentSign = queryLine.indexOf("/*");  
	                if(indexOfCommentSign != -1)  
	                {  
	                    if(queryLine.startsWith("#"))  
	                    {  
	                        queryLine = new String("");  
	                    }  
	                    else  
	                        queryLine = new String(queryLine.substring(0, indexOfCommentSign-1));  
	                      
	                    sBuffer.append(queryLine + " ");   
	                    // ignore all characters within the comment  
	                    do  
	                    {  
	                        queryLine = br.readLine();  
	                    }  
	                    while(queryLine != null && !queryLine.contains("*/"));  
	                    indexOfCommentSign = queryLine.indexOf("*/");  
	                    if(indexOfCommentSign != -1)  
	                    {  
	                        if(queryLine.endsWith("*/"))  
	                        {  
	                            queryLine = new String("");  
	                        }  
	                        else  
	                            queryLine = new String(queryLine.substring(indexOfCommentSign+2, queryLine.length()-1));  
	                    }  
	                }  
	                  
	                //  the + " " is necessary, because otherwise the content before and after a line break are concatenated  
	                // like e.g. a.xyz FROM becomes a.xyzFROM otherwise and can not be executed   
	                if(queryLine != null)  
	                    sBuffer.append(queryLine + " ");    
	            }    
	            br.close();  
	            fr.close();
	            // here is our splitter ! We use ";" as a delimiter for each request   
	            String[] splittedQueries = sBuffer.toString().split(";");  
	              
	            // filter out empty statements  
	            for(int i = 0; i<splittedQueries.length; i++)    
	            {  
	                if(!splittedQueries[i].trim().equals("") && !splittedQueries[i].trim().equals("\t"))    
	                {  
	                    listOfQueries.add(new String(splittedQueries[i]));  
	                }  
	            }  
	        }    
	        catch(Exception e)    
	        {        
	        	logger.log(Level.SEVERE,e.getMessage(),e);
	            throw new ServletException(e);
	           // throw e;
	        }  
	        return listOfQueries;  
	    

	}

}
