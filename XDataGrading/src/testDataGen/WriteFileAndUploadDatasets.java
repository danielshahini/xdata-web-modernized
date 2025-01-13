package testDataGen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import util.FailedColumnValues;
import util.FailedDataSetValues;
import util.Configuration;
import util.DataSetValue;
import util.DatabaseConnection;
import util.MyConnection;
import util.TableMap;

/**
 * Common methods
 * @author mahesh
 *
 */
public class WriteFileAndUploadDatasets {

	private static Logger logger = Logger.getLogger(WriteFileAndUploadDatasets.class.getName());
	public static void writeFile(String filePath, String content){
		try(java.io.FileWriter fw=new java.io.FileWriter(filePath, false)){
			fw.write(content);
			fw.flush();
		}catch(Exception e){
			logger.log(Level.SEVERE, "Message", e);
			//e.printStackTrace();
		}
	}
	
	/**
	 * Upload the data sets into inout database
	 * @param gd
	 * @param queryId
	 * @param dataSets
	 */
	public static void uploadDataset(GenerateCVC1 gd, int assignmentId,int questionId, int queryId, String course_id,ArrayList<String> dataSets,TableMap tableMap) throws Exception{
		
		String prevDatasets = "SELECT datasetid,tag FROM xdata_datasetvalue WHERE assignment_id = '" + assignmentId + "' and question_id='"+questionId+"' and query_id ='"+queryId+"' and course_id= '"+course_id+"'";
		String existingDataSets = "SELECT * from xdata_datasetvalue where assignment_id = '" + assignmentId + "' and question_id='"+questionId+"' and query_id ='"+queryId+"' and course_id= '"+course_id+"'";
		try(Connection conn = MyConnection.getDatabaseConnection()){
			try(Connection testCon = (new DatabaseConnection().getTesterConnection(assignmentId)).getTesterConn()){

				PopulateTestDataGrading p = new PopulateTestDataGrading();
				p.deleteAllTempTablesFromTestUser(testCon);
				p.createTempTables(testCon, assignmentId, questionId);
				Gson gson = new Gson();
				FailedDataSetValues instrDs = new FailedDataSetValues();
				
				String datasetid="";
				String json1="";
				try(PreparedStatement smt = conn.prepareStatement(prevDatasets)){
					try(ResultSet rs =smt.executeQuery()){
						while(rs.next()){
							datasetid=rs.getString(1);
							int id=Integer.parseInt(datasetid.substring(2));
						}
					}
				}
				for(int i=0;i<dataSets.size();i++){
					boolean dataExists = false;
					String dsPath = Configuration.homeDir+"/temp_smt"+gd.getFilePath()+"/"+dataSets.get(i); 
					Pattern pattern = Pattern.compile("^DS([0-9]+)$");
					Matcher matcher = pattern.matcher(dataSets.get(i));
					int dsId = i;
					
					if (matcher.find()) {
						dsId = Integer.parseInt(matcher.group(1));
					}	
					String cvcPath = Configuration.homeDir+"/temp_smt"+gd.getFilePath()+"/z3_"+dsId+".smt"; 
					File ds=new File(dsPath);		 	
					String line="";
					String tag="";
					
					try(BufferedReader b = new BufferedReader(new FileReader(cvcPath))){
						
						String substr = ";MUTATION TYPE:";
						while ((line = b.readLine()) != null) {
								line = line.trim(); 
							   if(line.startsWith(substr)){
								   tag=line.substring(line.lastIndexOf(substr) + substr.length()).trim();
								   break;
							   }
						}
					} // found the tag that is the mutation type
				
					ArrayList <DataSetValue> dsList = new ArrayList<DataSetValue>();
					
					
					// start inserting DS* inside the table
					try(BufferedReader b = new BufferedReader(new FileReader(dsPath))){
						
						while ((line = b.readLine()) != null) {
								line = line.trim();
								try(PreparedStatement inst=testCon.prepareStatement(line)){
									try{
										if(line.startsWith("--"))
											continue;
										else
										{
											if(!line.startsWith("delete"))
											{
												int flagForExist=0;
												String tableName ="";
												String dataToBeInserted="";
										        String regex = "(?i)INSERT\\s+INTO\\s+([`\"]?)([a-zA-Z_][a-zA-Z0-9_\\$]*)\\1";
										        pattern = Pattern.compile(regex);
										        matcher = pattern.matcher(line);
										        if (matcher.find())
										            tableName = matcher.group(2); // getting the table name from the query
										        
										        regex = "(?i)VALUES\\s*\\(([^)]+)\\)";
										        pattern = Pattern.compile(regex);
										        matcher = pattern.matcher(line);
										        if (matcher.find())
										        {
										        	dataToBeInserted = matcher.group(1);
										        	dataToBeInserted = dataToBeInserted.replace("'", "").replace(",", "|");
										        }
										        
										        //check if the table name is already inserted
										        for(int j=0 ; j<dsList.size(); j++)
										        {
										        	if(dsList.get(j).getTablename().equalsIgnoreCase(tableName))
										        	{
										        		flagForExist=1;
										        		dsList.get(j).addData(dataToBeInserted);
										        		break;
										        	}
										        }
										        
										        //otherwise create the table name and add the data
										        if(flagForExist==0)
										        {
										        	DataSetValue dsValue = new DataSetValue();
										        	dsValue.setTableName(tableName);
										        	dsValue.addData(dataToBeInserted);
										        	
										        	//getting the column name by writing a dummy query
								        	
										        	 DatabaseMetaData metaData = conn.getMetaData();

										             // Get columns for the specified table
										             ResultSet columns = metaData.getColumns(null, null, tableName, "%");
										             while (columns.next()) {
										                 String columnName = columns.getString("COLUMN_NAME");
										                 dsValue.addColumnName(columnName);
										             }
										             										                
										             dsList.add(dsValue);
										            
										          }
												}
											
									            // now we have an array list 
									            inst.executeUpdate();
											
									        
											}
												
									}catch(Exception e){
										//If exception occurs, then this is duplicate column
										logger.log(Level.FINE," Contraint violated ERROR:" + inst+"/n while inserting datasets");
										//e.printStackTrace();
								} finally{
										inst.close();
									}
									}
								
							   
						}
					}
					//end of insertions inside the temp tables
		            
					String json = gson.toJson(dsList);
					datasetid="DS"+ dsId;
					
					//Save this to DB and process this in show data generated
				
					//Once the datasets are loaded to temp tables, run instructor query and save the result
		            //For 'showGeneratedDataSet' UI display - start
					
					//to hold the column names for display 
					ArrayList <FailedColumnValues> failedList = new ArrayList<FailedColumnValues>();
					//to hold the output values for each column
					Map <String,ArrayList<String>> instrColMap = new LinkedHashMap<String,ArrayList<String>>();
					Map <String,Map<String,ArrayList<String>>> instrDataMap = new HashMap<String,Map<String,ArrayList<String>>>();
					String instrQuery = "";
					
					try(PreparedStatement smt = conn.prepareStatement("select sql from xdata_instructor_query where assignment_id=? and question_id=? and query_id=? and course_id=?")){
						smt.setInt(1, assignmentId);
						smt.setInt(2, questionId);
						smt.setInt(3, queryId);
						smt.setString(4,course_id);
						try(ResultSet rset= smt.executeQuery()){
							if(rset.next()){
								instrQuery = rset.getString("sql");
							}
						}
						
					}
					
					//getting the query and saving it in instrQuery
					if(instrQuery != null){
						//TestAnswer testAns = new TestAnswer();
			
						//If there are two columns with same name like count(distinct col_name) and count(col_name)
						// output will have col name as count for both - to use this as key, suffix with index 1,2,.,
						// and use that as key in map to store output values for the column.
						List<String> existingColNames = new ArrayList<String>();  
						int index = 1;
					try(PreparedStatement pp=testCon.prepareStatement(instrQuery)){
						try(ResultSet rr=pp.executeQuery()){
							ResultSetMetaData metadata = rr.getMetaData();
							int no_of_columns=metadata.getColumnCount();
							String result="";
							String columnName = "";
							//ArrayList <String> values = new ArrayList<String>();
							
							//put all col names in a list. Delete the first matching column
							//if col name still exists, then there is duplicate col name - suffix column name with index
							for(int cl=1;cl<=no_of_columns;cl++)
							{
								existingColNames.add(metadata.getColumnName(cl));
							}
							for(int cl=1;cl<=no_of_columns;cl++)
							{
								 ArrayList <String> values = new ArrayList<String>();
								FailedColumnValues failedColumns = new FailedColumnValues();
								
								existingColNames.remove(metadata.getColumnName(cl));
								
								//After removing , if still coName exists it is duplicate column- so suffix with index.
								if(existingColNames.contains(metadata.getColumnName(cl))){
									
									columnName = metadata.getColumnName(cl)+index;
									index ++;
									existingColNames.add(metadata.getColumnName(cl));
									
								}else{
									columnName = metadata.getColumnName(cl);
									
								}
								
								try(ResultSet rr1=pp.executeQuery()){
								 metadata = rr1.getMetaData();
								while(rr1.next())
								{
									int type = metadata.getColumnType(cl);
									values.add(rr1.getString(cl));
									
								}	
								
								failedColumns.setInstrColumnName(metadata.getColumnName(cl));
								failedColumns.setInstrValues(values);
								instrColMap.put(columnName,values);
								failedList.add(failedColumns);
							}
								}
								
						}
					}catch(Exception e){
						logger.log(Level.FINE," WriteFileAndUploadDataSets:" + e.getMessage(),e);
					}
					
					instrDataMap.put(datasetid, instrColMap);
					instrDs.getInstrQueryOutput().put(datasetid, instrColMap);
					instrDs.getDsValueMap().put(datasetid,failedList);
					}
					
					try(PreparedStatement smt = conn.prepareStatement(existingDataSets)){
												
						try(ResultSet rs =smt.executeQuery()){
							while(rs.next()){
								
								String existingDataSetId = rs.getString("datasetid");
								if(datasetid.equalsIgnoreCase(existingDataSetId)){
									dataExists = true;
									break;
								}
							}
						}
					}
					if(dataExists){
						String insertquery="update xdata_datasetvalue set datasetid=?,value=?,tag=? where assignment_id=? and question_id=? and query_id=? and course_id=? and datasetid=?";
						try(PreparedStatement smt = conn.prepareStatement(insertquery)){
								smt.setString(1,datasetid);
								smt.setString(2,json);
								smt.setString(3, tag);
								smt.setInt(4,assignmentId);
								smt.setInt(5,questionId);
								smt.setInt(6,queryId);
								smt.setString(7, course_id);
								smt.setString(8,datasetid);
								
								smt.executeUpdate(); 			 	
						}
					} 
					if(!dataExists){
					//For 'showGeneratedDataSet' UI display - End
					String insertquery="INSERT INTO xdata_datasetvalue VALUES ('"+queryId+"','"+datasetid+"','"+json+"','"+tag+"','"+assignmentId+"','"+questionId+"','"+queryId+"','"+course_id+"')";
										
					try(PreparedStatement smt = conn.prepareStatement(insertquery)){
						smt.executeUpdate(); 			 	
					}
					}
				}//for each dataSet loop ends
				
				json1 = gson.toJson(instrDs);		
				
				String update="update xdata_instructor_query set resultondataset=? where assignment_id=? and question_id=? and query_id=? and course_id=?";
				
				try(PreparedStatement smt = conn.prepareStatement(update)){
					smt.setString(1,json1);
					smt.setInt(2, assignmentId);
					smt.setInt(3, questionId);
					smt.setInt(4, queryId);
					smt.setString(5,course_id);
					smt.executeUpdate(); 			 	
				}
	}//try-with-resources - Connection testCon ends
	}//try-with-resources -Connection conn ends 
}
}
