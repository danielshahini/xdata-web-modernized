package testDataGen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;



import util.Configuration;
import util.DatabaseConnection;
import util.MyConnection;
import util.TableMap;
import util.Utilities;

public class preProcessForDataGeneration {

	private static Logger logger=Logger.getLogger(preProcessForDataGeneration.class.getName());
	
	public void generateDatasetForQuery(int assignmentId, int questionId,int queryId, String course_id) throws Exception {
		this.generateDatasetForQuery(assignmentId,questionId,queryId,course_id,1);
	}

	
	public void generateDatasetForQuery(int assignmentId, int  questionId, int queryId,String course_id, int dummy) throws Exception{
		GenerateCVC1 cvc = new GenerateCVC1();
		String concatenatedQueryId = "A"+assignmentId+"Q"+questionId+"S"+queryId;
		String filePath = "4/"+course_id+"/"+concatenatedQueryId;
		
		cvc.setAssignmentId(assignmentId);
		cvc.setQuestionId(questionId);
		cvc.setQueryId(queryId);
		//cvc.setQueryString(query);
		cvc.setConcatenatedQueryId(concatenatedQueryId);
		cvc.setOrderindependent(true);
		
		
		cvc.setFne(false); 
		cvc.setIpdb(false);
		cvc.setFilePath(filePath);
		cvc = initializeConnectionDetails(cvc);
				
		try(Connection dbcon = MyConnection.getDatabaseConnection()){
	    	if(dbcon!=null){
	    	    	  logger.log(Level.INFO,"Connected successfullly");	    	  
	    	 }
	    	
	    	String sel="Select sql from xdata_instructor_query where assignment_id=? and question_id=? and query_id=?";
	    	
			try(PreparedStatement stmt=dbcon.prepareStatement(sel)){
				stmt.setInt(1,assignmentId); 
				stmt.setInt(2, questionId); 
				stmt.setInt(3, queryId);
				try(ResultSet rs=stmt.executeQuery()){ 
					rs.next();
					String sql=rs.getString("sql");
					System.out.println(sql); // added by ram
					cvc.setQueryString(sql);
					
				}
			}
			DataGenController.generateDatasetForQuery(cvc);
			
			/** Check the data sets generated for this query */
			ArrayList<String> dataSets = RelatedToPreprocessing.getListOfDataset(cvc);
			
			WriteFileAndUploadDatasets.uploadDataset(cvc, assignmentId,questionId,queryId, course_id,dataSets,cvc.getTableMap());	
			
			logger.log(Level.INFO,"\n***********************************************************************\n\n");
			logger.log(Level.INFO,"DATASET FOR QUERY "+queryId+" ARE UPLOADED");
			logger.log(Level.INFO,"\n***********************************************************************\n\n");
			
			
	    }catch(Exception e){
	    	logger.log(Level.SEVERE,e.getMessage(),e);
	    	throw e;
	    }finally{
			if(cvc!= null && cvc.getConnection() != null){
				 cvc.closeConn();
				}
			}
	
	}
	
	public static void deletePreviousDatasets(GenerateDataset_new g, String query) throws IOException,InterruptedException {

		//Runtime r = Runtime.getRuntime();
		//logger.log(Level.INFO,Configuration.homeDir+"/temp_cvc"+g.getFilePath()+"/");
		//File f=new File(Configuration.homeDir+"/temp_cvc"+g.getFilePath()+"/");
		
		logger.log(Level.INFO,Configuration.homeDir+"/temp_smt"+g.getFilePath()+"/"); // added by ram
		File f=new File(Configuration.homeDir+"/temp_smt"+g.getFilePath()+"/"); /// added by ram
		
		if(f.exists()){		
			File f2[]=f.listFiles();
			if(f2 != null)
			for(int i=0;i<f2.length;i++){
				if(f2[i].isDirectory() && f2[i].getName().startsWith("DS")){
					
					//Utilities.deletePath(Configuration.homeDir+"/temp_cvc"+g.getFilePath()+"/"+f2[i].getName());
					Utilities.deletePath(Configuration.homeDir+"/temp_smt"+g.getFilePath()+"/"+f2[i].getName()); // added by ram
				}
			}
		}
		

		//File dir= new File(Configuration.homeDir+"/temp_cvc"+g.getFilePath());
		File dir= new File(Configuration.homeDir+"/temp_smt"+g.getFilePath());
		if(dir.exists()){
			for(File file: dir.listFiles()) {
				file.delete();
			}
		}
		else{
			dir.mkdirs();
		}
		
		//BufferedWriter ord = new BufferedWriter(new FileWriter(Configuration.homeDir+"/temp_cvc"+g.getFilePath()+"/queries.txt"));
		//BufferedWriter ord1 = new BufferedWriter(new FileWriter(Configuration.homeDir+"/temp_cvc"+g.getFilePath()+"/queries_mutant.txt"));
		BufferedWriter ord = new BufferedWriter(new FileWriter(Configuration.homeDir+"/temp_smt"+g.getFilePath()+"/queries.txt"));    // added by ram
		BufferedWriter ord1 = new BufferedWriter(new FileWriter(Configuration.homeDir+"/temp_smt"+g.getFilePath()+"/queries_mutant.txt")); // added by ram
		ord.write(query);
		ord1.write(query);
		ord.close();
		ord1.close();
	}
	
	
	
		
public GenerateCVC1 initializeConnectionDetails(GenerateCVC1 cvc) throws Exception {
		
	Connection testConn = (new DatabaseConnection().getTesterConnection(cvc.getAssignId())).getTesterConn();
	
	try(Connection conn = MyConnection.getDatabaseConnection()){
		int connId = 0, schemaId = 0, optionalSchemaId=0;
			
		int assignmentId  = cvc.getAssignId();
		int questionId = cvc.getQuestionId();
		int queryId = cvc.getQueryId();
		String course_id = cvc.getCourseId();
		int q_id = questionId;
		
		try(PreparedStatement stmt = conn.prepareStatement("select connection_id, defaultschemaid from xdata_assignment where assignment_id = ?")){
			stmt.setInt(1, assignmentId); 
			 
			try(ResultSet result = stmt.executeQuery()){			
					//Get optional Schema Id for this question
					try(PreparedStatement statement = conn.prepareStatement("select optionalschemaid from xdata_qinfo where assignment_id = ? and question_id= ? ")){
						statement.setInt(1, assignmentId); 
						statement.setInt(2,q_id); 
						
						try(ResultSet resultSet = statement.executeQuery()){
							if(resultSet.next()){
								optionalSchemaId = resultSet.getInt("optionalschemaid");
							}
						}
					}			
					if(result.next()){
						connId = result.getInt("connection_id");
						//If optional schema id exists and it is not same as default schema id, then set it as schemaId 
						if(optionalSchemaId != 0 && optionalSchemaId != result.getInt("defaultschemaid")){	
							schemaId = optionalSchemaId;
						} else{
							schemaId = result.getInt("defaultschemaid");
						}
					} 
			}
		}
			if(connId != 0 && schemaId != 0){
				 
			
						
				
				PopulateTestDataGrading p = new PopulateTestDataGrading();
				p.deleteAllTempTablesFromTestUser(testConn);
				byte[] dataBytes = null;
				String tempFile = "";
				FileOutputStream fos = null;
				ArrayList<String> listOfQueries = null;
				ArrayList<String> listOfDDLQueries = new ArrayList<String>();
				String[] inst = null;
				
				if(testConn != null){
				
					try(PreparedStatement stmt1 = conn.prepareStatement("select ddltext from xdata_schemainfo where schema_id = ?")){
					stmt1.setInt(1, schemaId);			
					try(ResultSet result = stmt1.executeQuery()){
					
					    
					// Process the result			
					if(result.next()){
						String fileContent= result.getString("ddltext");
						cvc.setSchemaFile(fileContent);
						// CCJSqlParserManager fleParser = new CCJSqlParserManager();
							//Statement parsedStmt = fleParser.parse(new StringReader(fileContent));
						dataBytes = fileContent.getBytes();
						tempFile = "/tmp/dummy";
						
						 fos = new FileOutputStream(tempFile);
						fos.write(dataBytes);
						fos.close();
						listOfQueries = Utilities.createQueries(tempFile);
						inst = listOfQueries.toArray(new String[listOfQueries.size()]);
						listOfDDLQueries.addAll(listOfQueries);
						p.deleteAllTablesFromTestUser(testConn); // added by ram : delete tables if already exists.
						
						// to test the time taken for creating tables, added by ram
						long startTime = System.currentTimeMillis();
						
						for (int i = 0; i < inst.length; i++) {
							// we ensure that there is no spaces before or after the request string  
							// in order to not execute empty statements  
							if (!inst[i].trim().equals("") && ! inst[i].trim().contains("drop table")) {
								//Changed for MSSQL testing
								//String temp = inst[i].replaceAll("(?i)^[ ]*create[ ]+table[ ]+", "create table ##");
								//stmt = assignmentConn.prepareStatement(temp);
								//add by ram
								DatabaseMetaData dbmd=testConn.getMetaData();      
								String dbType = dbmd.getDatabaseProductName(); 
								String temp = "";
								if (dbType.equalsIgnoreCase("MySql"))
								{
									temp = inst[i].trim();
								}
								else if(dbType.equalsIgnoreCase("PostgreSQL")) {
									temp = inst[i].trim().replaceAll("(?i)^\\s*create\\s+table\\s+", "create temporary table ");	
								}
								System.out.println(temp); // added by ram
								PreparedStatement stmt2 = testConn.prepareStatement(temp);
									stmt2.executeUpdate();	
								stmt2.close();
							
								    
							}
						}
						
						// to test the time taken for creating tables, added by ram
						long stopTime = System.currentTimeMillis();
						long elapsedTime = stopTime - startTime;
				        System.out.println("Total time taken for creating tables/temp tables is: ");
				        System.out.print(elapsedTime/1000F);
					}
					}
					}
					try(PreparedStatement stmt2 = conn.prepareStatement("select sample_data from xdata_sampledata where schema_id = ?")){
						stmt2.setInt(1, schemaId);			
						try(ResultSet result = stmt2.executeQuery()){
							if(result.next()){
								String sdContent= result.getString("sample_data");
								cvc.setDataFileName(sdContent);
							//	String sdReplace=sdContent.replace("\\\\","'");
								//fc = sdContent.replace("\t", "    ");
								dataBytes = sdContent.getBytes(); 
								fos = new FileOutputStream(tempFile);
								fos.write(dataBytes);
								fos.close();
								
								listOfQueries = Utilities.createQueries(tempFile);
								inst = listOfQueries.toArray(new String[listOfQueries.size()]);
								
								// to test the time taken for creating tables, added by ram
								long startTime = System.currentTimeMillis();
								 
								for (int i = 0; i < inst.length; i++) {
									// we ensure that there is no spaces before or after the request string  
									// in order to not execute empty statements  
									if (!inst[i].trim().equals("") && !inst[i].contains("drop table") && !inst[i].contains("delete from")) {
									//Changed for MSSQL TESTING
										//String temp = inst[i].replaceAll("(?i)^[ ]*insert[ ]+into[ ]+", "insert into [xdata].[dbo].##");
										//stmt = assignmentConn.prepareStatement(temp+";");
										System.out.println(inst[i]);// add by RAM
										PreparedStatement stmt3 = testConn.prepareStatement(inst[i]);
											stmt3.executeUpdate();							
											stmt3.close();
									}
								}
								
								// to test the time taken for creating tables, added by ram
								long stopTime = System.currentTimeMillis();
								long elapsedTime = stopTime - startTime;
						        System.out.println("Total time taken for uploading sample data is: ");
						        System.out.print(elapsedTime/1000F);
							} 
					}
					}
				} 
				
				cvc.setTableMap(TableMap.getInstances(testConn,schemaId));	
			
		}
		}
	catch(Exception ex){
		//this.closeConn();
		logger.log(Level.SEVERE,ex.getMessage(), ex);
		//ex.printStackTrace();
		throw ex;
	}
	cvc.setConnection(testConn);
	return cvc;
	}
	
}
