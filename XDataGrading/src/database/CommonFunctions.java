package database;

import java.util.*;
import java.util.logging.Logger;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.*;
import java.text.*;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class CommonFunctions {

	private static Logger logger = Logger.getLogger(CommonFunctions.class.getName());
	public String timeDifference(java.util.Date toDate, java.util.Date fromDate) {

		long diff = toDate.getTime() - fromDate.getTime();

		long diffSeconds = diff / 1000 % 60;
		long diffMinutes = diff / (60 * 1000) % 60;
		long diffHours = diff / (60 * 60 * 1000) % 24;
		long diffDays = diff / (24 * 60 * 60 * 1000);

		String timeDiff = diffDays + " days," + diffHours + " hours, "
				+ diffMinutes + " minutes, " + diffSeconds + " seconds.";

		System.out.print(diffDays + " days, ");
		System.out.print(diffHours + " hours, ");
		System.out.print(diffMinutes + " minutes, ");
		System.out.print(diffSeconds + " seconds.");

		return timeDiff;
	}
	
	/**
	 * This method gets the assignment details and instructions block
	 * for instructor mode 
	 * 
	 * @param courseID
	 * @param assignID
	 * @param link
	 * @return
	 * @throws Exception
	 */
	
	public String getAssignmentInstructions(String courseID, int assignID)
			throws Exception {
		return getAssignmentInstructions(courseID, assignID, "");
	}

	
	public String getAssignmentInstructions(String courseID, int assignID,
			String link) throws Exception {

		// get connection
		Timestamp end = null;
		int defaultSchemaId = 0;
		String assignmentName = null;
		String assignmentType="";
		String instructions = "<label><b>Assignment Id:</b></label> <b><label style='color:#353275'> " + assignID
				+ "</label><br/> ";
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){
				
			try(PreparedStatement stmt = dbcon
						.prepareStatement("SELECT * FROM xdata_assignment where assignment_id=? and course_id=?")){
				stmt.setInt(1, assignID);
				stmt.setString(2, courseID);
				try(ResultSet rs = stmt.executeQuery()){
					if (rs.next()) {
						end = rs.getTimestamp("endtime");
						if(rs.getBoolean("learning_mode")){
							assignmentType="Learning Mode";
						}
						else{
							assignmentType="Grading Mode";
						}
						defaultSchemaId = rs.getInt("defaultschemaid");
						assignmentName = rs.getString("assignmentName");
						instructions += "<p></p><label><b>Assignment Name: </b></label> <b><label style='color:#353275'>"
								+ assignmentName + "</b></label>";
						instructions += "<p></p><label>Assignment Mode: </b></label> <b><label style='color:#353275'>"
								+ assignmentType + "</b></label>";
					try(PreparedStatement stmt1 = dbcon
								.prepareStatement("SELECT schema_name from xdata_schemainfo where course_id=? and schema_id=?")){
							stmt1.setInt(2, defaultSchemaId);
							stmt1.setString(1, courseID);
							try(ResultSet rs1 = stmt1.executeQuery()){
								if (rs1.next()) { 
									instructions += "<p></p><label><b>Default Schema: </b></label> <b><label style='color:#353275'>"
											//+ rs1.getString("schema_name")
											+ "<span>&nbsp;</span>";
									instructions += "<a style=\"color:#353275;\" href=\"showSchemaFile.jsp?schema_id="
											+ defaultSchemaId
											+ "\">"+rs1.getString("schema_name")+"</a> </label></b>&nbsp;&nbsp;&nbsp;&nbsp;";
								}
							}
						}
						
						try(PreparedStatement stmt1 = dbcon
								.prepareStatement("SELECT sample_data_name,sampledata_id from xdata_sampledata where course_id=? and schema_id=?")){
							stmt1.setInt(2, defaultSchemaId);
							stmt1.setString(1, courseID);
							try(ResultSet rs1 = stmt1.executeQuery()){
								if (rs1.next()) { 
									instructions += "<p></p><label><b>Default dataset for data generation: </b></label> <b><label style='color:#353275'>"
											+ "<span>&nbsp;</span>";
									instructions += "<a style=\"color:#353275;\" href=\"showSampleData.jsp?schema_id="
											+defaultSchemaId+"&sampledata_id="+rs1.getString("sampledata_id")+"\">"+ rs1.getString("sample_data_name")+"</a></label></b>&nbsp;&nbsp;&nbsp;&nbsp;";
								}
							}
							 
									Gson gson = new Gson();
									Type listType = new TypeToken<String[]>() {}.getType();
									  if(rs.getString("defaultdsetid") != null || 
											  (rs.getString("defaultdsetid")!= null && rs.getString("defaultdsetid").equalsIgnoreCase("null"))){
						                    String[] dsList = new Gson().fromJson(rs.getString("defaultdsetid"), listType);
						                  
											instructions += "<p></p><label><b>Default dataset(s) for evaluation: </b></label> <label style='color:#353275'>"
												//+ rs1.getString("schema_name")
												+ "<span>&nbsp;</span>";
											
											if(dsList != null && dsList.length != 0){
						                    	for(int i=0;i<dsList.length;i++){
						                    		try(ResultSet rs2 = stmt1.executeQuery()){
							                    		while(rs2.next()){
							                    			if(rs2.getString("sampledata_id").equalsIgnoreCase(dsList[i])){
							                    				String sampleDataName = rs2.getString("sample_data_name");
							                    				  instructions += "<b><a style=\"color:#353275;\" href=\"showSampleData.jsp?schema_id="
							                    							+defaultSchemaId+"&sampledata_id="+rs2.getString("sampledata_id")+"\">"+ rs2.getString("sample_data_name")+"</a></b>&nbsp;&nbsp;&nbsp;&nbsp;";
							                    				//break;
							                    			}
							                    		}
							                    		
						                    		}
						                    	}
						                    	instructions += "</label>&nbsp;&nbsp;&nbsp;&nbsp;";
						                    }
											else{
											 instructions += "</label></b>&nbsp;&nbsp;&nbsp;&nbsp;";
										  }
							
							}
						}
						
						
						SimpleDateFormat formatter = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss");
						formatter.setLenient(false);
						String ending = formatter.format(end);
						// String oldTime = "2012-07-11 10:55:21";
						java.util.Date oldDate = formatter.parse(ending);
						// get current date
						Calendar c = Calendar.getInstance();
						String currentDate = formatter.format(c.getTime());
						java.util.Date current = formatter.parse(currentDate);
						// compare times
						if (current.compareTo(oldDate) >= 0) {
		
							CommonFunctions util = new CommonFunctions();
							String dueTime = util.timeDifference(current, oldDate);
							instructions +="<p><label><b> Assignment end date: </b></label> <b><label style='color:#353275'>"+end+ "  </b></label></p>";
							instructions += "<p><label> <b>Assignment is over due by </b></label> <b><label style='color:#353275'>"
									+ dueTime + "</b></label></p>";
						} else {
							instructions += "<p><label> <b>Assignment is due on </b></label> <b><label style='color:#353275'>"+ end
									+ "</label> </b></p>";
						}
		
					}
					}//try block for resultset ends
			}//try block for statement ends
			}//try block for conn ends
		return instructions;
	} 

	/**
	 * This method gets the assignment details for display in student mode
	 * 
	 * @param courseID
	 * @param assignID
	 * @return
	 * @throws Exception
	 */
	public String getStudentAssignmentInstructions(String courseID, int assignID,String user)
			throws Exception {
		// get connection
		Timestamp end = null;
		Timestamp soft = null;
		int defaultSchemaId = 0;
		String assignmentType = "";
		String assignmentName = null;
		String instructions = "";
		String penalty="";
		if(user.equals("guest")){ 
			instructions += "<table border='0'><tr><td width='15%' style='padding: 0px;border:0px solid black;'>";
		}
		
		instructions += "<label><b>Assignment Id: </b></label> <b><label style='color:#353275'>" + assignID
				+ "</b></label>";
		if(user.equals("guest")){ 
			instructions += "</td>"; 
		}
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){
			 
			try(PreparedStatement stmt = dbcon
					.prepareStatement("SELECT * FROM xdata_assignment where assignment_id=? and course_id=?")){
			stmt.setInt(1, assignID);
			stmt.setString(2, courseID);
			try(ResultSet rs = stmt.executeQuery()){
			if (rs.next()) {
				end = rs.getTimestamp("endtime");
				soft = rs.getTimestamp("softtime");
				penalty = rs.getString("penalty");
				if(  rs.getBoolean("learning_mode")){
					assignmentType="Learning Mode";
				}
				else{
					assignmentType="Grading Mode";
				}
				// start=rs.getString("end_date");
				defaultSchemaId = rs.getInt("defaultschemaid"); 
				assignmentName = rs.getString("assignmentName");
				if(!user.equals("guest")){ 
					instructions += "<p></p>";
				}else{ 
					//instructions += "<span>&nbsp;&nbsp;&nbsp;&nbsp;</span>";
				}
				if(user.equals("guest")){ 
					instructions += "<td width='15%' style='padding: 0px;border:0px solid black;'>";
				}
				instructions += "<label><b>Assignment Name: </b></label> <b><label style='color:#353275' >"
						+ assignmentName + "</b></label>";
				if(user.equals("guest")){ 
					instructions += "</td>";
				}
				if(!user.equals("guest")){ 
					instructions += "<p></p>";
				}else{
					//instructions += "<span>&nbsp;&nbsp;&nbsp;&nbsp;</span>";
					instructions += "<td width='15%' style='padding: 0px;border:0px solid black;'>";
				}
				instructions += "<label><b>Assignment Mode: </b></label> <b><label style='color:#353275'>"
						+ assignmentType + "</b></label>";
				if(user.equals("guest")){ 
					instructions += "</td></tr><tr><td width='30%' style='padding: 0px;border:0px solid black;'>";
				}
				try(PreparedStatement stmt1 = dbcon
						.prepareStatement("SELECT schema_name from xdata_schemainfo where course_id=? and schema_id=?")){
				stmt1.setInt(2, defaultSchemaId);
				stmt1.setString(1, courseID);
				try(ResultSet rs1 = stmt1.executeQuery()){
					if (rs1.next()) {
						instructions += "<p></p><label><b>Default Schema:</b></label><span>&nbsp;</span><b><label style='color:#353275'>";
						instructions += "<a style=\"color:#353275;\" href=\"showSchemaFile.jsp?schema_id="
								+ defaultSchemaId + "\">"
								+ rs1.getString("schema_name")
								+ "</a></b></label>";
					}
				} 
				}
				if(!user.equals("guest")){ 
					instructions += "<p></p>";
				}
				else{
					instructions += "</td><td width='30%' style='padding: 0px;border:0px solid black;'>";
				}
				try(PreparedStatement stmt1 = dbcon
						.prepareStatement("SELECT sample_data_name,sampledata_id from xdata_sampledata where course_id=? and schema_id=?")){
					stmt1.setInt(2, defaultSchemaId);
					stmt1.setString(1, courseID);
					try(ResultSet rs1 = stmt1.executeQuery()){
						if (rs1.next()) { 
							instructions += "<label><b>Default dataset for data generation: </b></label> <b><label style='color:#353275;'>"
									+ "<span>&nbsp;</span>";
							instructions += "<a style=\"color:#353275;\" href=\"showSampleData.jsp?schema_id="
									+defaultSchemaId+"&sampledata_id="+rs1.getString("sampledata_id")+"\">"+ rs1.getString("sample_data_name")+"</a></label></b>&nbsp;&nbsp;&nbsp;&nbsp;";
						}
					}
					if(user.equals("guest")){ 
						instructions += "</td></tr></table>";
					}
					if(!user.equals("guest")){ 
							Gson gson = new Gson();
							Type listType = new TypeToken<String[]>() {}.getType();
							  if(rs.getString("defaultdsetid") != null || 
									  (rs.getString("defaultdsetid")!= null && rs.getString("defaultdsetid").equalsIgnoreCase("null"))){
				                    String[] dsList = new Gson().fromJson(rs.getString("defaultdsetid"), listType);
				                  
									instructions += "<p></p><label><b>Default dataset(s) for evaluation: </b></label><label style='color:#353275'>"
										+ "<span>&nbsp;</span>";
									
									if(dsList != null && dsList.length != 0){
				                    	for(int i=0;i<dsList.length;i++){
				                    		try(ResultSet rs2 = stmt1.executeQuery()){
					                    		while(rs2.next()){
					                    			if(rs2.getString("sampledata_id").equalsIgnoreCase(dsList[i])){
					                    				String sampleDataName = rs2.getString("sample_data_name");
					                    				  instructions += "<b><a style=\"color:#353275;\" href=\"showSampleData.jsp?schema_id="
					                    							+defaultSchemaId+"&sampledata_id="+rs2.getString("sampledata_id")+"\">"+ rs2.getString("sample_data_name")+"</a></b>&nbsp;&nbsp;&nbsp;&nbsp;";
					                    				//break;
					                    			}
					                    		}
				                    		}
				                    	}
				                    	instructions += "</label>";
				                    }
									else{
									 instructions += "</label></b>&nbsp;&nbsp;&nbsp;&nbsp;";
									}
					
					}
				}
			}
				if(!user.equals("guest")){ 
					SimpleDateFormat formatter = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					formatter.setLenient(false);
					String ending = formatter.format(end);
					String softdate="";
					if(soft!=null)
						softdate = formatter.format(soft);
					java.util.Date oldDate = formatter.parse(ending);
					// get current date
					Calendar c = Calendar.getInstance();
					String currentDate = formatter.format(c.getTime());
					java.util.Date current = formatter.parse(currentDate);
					// compare times
					if (current.compareTo(oldDate) >= 0) {
	
						CommonFunctions util = new CommonFunctions();
						String dueTime = util.timeDifference(current, oldDate);
								instructions +="<p><label> <b>Assignment end date: </b></label> <b><label style='color:#353275'>"+end+ " </b></label></p>";					
						instructions += "<p><label><b> Assignment is over due by </b></label> <b><label style='color:#353275'>"
								+ dueTime + "</b></label></p>";					
					} else {
						if(soft!=null)
						{
							instructions += "<p><label> <b>Deadline on </b></label> <b><label style='color:#353275'>" + soft
									+ " </b></label></p>";
							instructions += "<p><label> <b>Hard deadline on </b></label> <b><label style='color:#353275'>" + end+ "</b></label>"
									+ " <label><b>with penalty </b></label> <b><label style='color:#353275'>"+penalty+"% </b></label></p>";
						}
						else{
							instructions += "<p><label> <b>Deadline on </b></label> <b><label style='color:#353275'>" + end
									+ " </b></label></p>";
						}
					}
				}
			
			}
			}//try block for resultset ends
			}//try/block for stmnt ends
		}//try block for conn close
		
			return instructions;
	}

	
	public String getTesterAssignmentInstructions(String courseID, int assignID) throws Exception {


		
		// get connection
		Timestamp end = null;
		int defaultSchemaId = 0;
		String assignmentName = null;
		String assignmentType="";
		String instructions = "<label><b>Application Id:</b></label> <b><label style='color:#353275'> " + assignID
				+ "</label><br/> ";
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){
				 
			try(PreparedStatement stmt = dbcon
						.prepareStatement("SELECT * FROM xdata_assignment where assignment_id=? and course_id=?")){
				stmt.setInt(1, assignID);
				stmt.setString(2, courseID);
				try(ResultSet rs = stmt.executeQuery()){
					if (rs.next()) {
						
						// start=rs.getString("end_date");
						defaultSchemaId = rs.getInt("defaultschemaid");
						assignmentName = rs.getString("assignmentName");
						instructions += "<p></p><label><b>Application Name: </b></label> <b><label style='color:#353275'>"
								+ assignmentName + "</b></label>";
						
					try(PreparedStatement stmt1 = dbcon
								.prepareStatement("SELECT schema_name from xdata_schemainfo where course_id=? and schema_id=?")){
							stmt1.setInt(2, defaultSchemaId);
							stmt1.setString(1, courseID);
							try(ResultSet rs1 = stmt1.executeQuery()){
								if (rs1.next()) { 
									instructions += "<p></p><label><b>Default Schema: </b></label> <b><label style='color:#353275'>"
											//+ rs1.getString("schema_name")
											+ "<span>&nbsp;</span>";
									instructions += "<a style=\"color:#353275;\" href=\"showSchemaFile.jsp?schema_id="
											+ defaultSchemaId
											+ "\">"+rs1.getString("schema_name")+"</a> </label></b>&nbsp;&nbsp;&nbsp;&nbsp;";
								}
							}
						}
						
						try(PreparedStatement stmt1 = dbcon
								.prepareStatement("SELECT sample_data_name,sampledata_id from xdata_sampledata where course_id=? and schema_id=?")){
							stmt1.setInt(2, defaultSchemaId);
							stmt1.setString(1, courseID);
							try(ResultSet rs1 = stmt1.executeQuery()){
								if (rs1.next()) { 
									instructions += "<p></p><label><b>Default dataset for data generation: </b></label> <b><label style='color:#353275'>"
											//+ rs1.getString("schema_name")
											+ "<span>&nbsp;</span>";
									instructions += "<a style=\"color:#353275;\" href=\"showSampleData.jsp?schema_id="
											+defaultSchemaId+"&sampledata_id="+rs1.getString("sampledata_id")+"\">"+ rs1.getString("sample_data_name")+"</a></label></b>&nbsp;&nbsp;&nbsp;&nbsp;";
								}
							}
							 
									Gson gson = new Gson();
									Type listType = new TypeToken<String[]>() {}.getType();
									  if(rs.getString("defaultdsetid") != null || 
											  (rs.getString("defaultdsetid")!= null && rs.getString("defaultdsetid").equalsIgnoreCase("null"))){
						                    String[] dsList = new Gson().fromJson(rs.getString("defaultdsetid"), listType);
						                  
											instructions += "<p></p><label><b>Default dataset(s) for evaluation: </b></label> <label style='color:#353275'>"
												//+ rs1.getString("schema_name")
												+ "<span>&nbsp;</span>";
											
											if(dsList != null && dsList.length != 0){
						                    	for(int i=0;i<dsList.length;i++){
						                    		try(ResultSet rs2 = stmt1.executeQuery()){
							                    		while(rs2.next()){
							                    			if(rs2.getString("sampledata_id").equalsIgnoreCase(dsList[i])){
							                    				String sampleDataName = rs2.getString("sample_data_name");
							                    				  instructions += "<b><a style=\"color:#353275;\" href=\"showSampleData.jsp?schema_id="
							                    							+defaultSchemaId+"&sampledata_id="+rs2.getString("sampledata_id")+"\">"+ rs2.getString("sample_data_name")+"</a></b>&nbsp;&nbsp;&nbsp;&nbsp;";
							                    				//break;
							                    			}
							                    		}
							                    		
						                    		}
						                    	}
						                    	instructions += "</label>&nbsp;&nbsp;&nbsp;&nbsp;";
						                    }
											else{
											 instructions += "</label></b>&nbsp;&nbsp;&nbsp;&nbsp;";
										   // instructions += "<a style=\"\" href=\"showSampleData.jsp?schema_id="
											//		+defaultSchemaId+"&sampledata_id="+rs1.getString("sampledata_id")+">"+ rs1.getString("sample_data_name")+"</a> <br/>";
											}
							
							}
						}
					}
					}//try block for resultset ends
			}//try block for statement ends
			}//try block for conn ends
		return instructions;
	} 
	
	/**
	 * This method encodes the query string from HTML and converts the special
	 * characters
	 * 
	 * @param s
	 * @return
	 */
	public static String encodeHTML(String s) {
		StringBuffer out = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c > 127 || c == '"' || c == '<' || c == '>') {
				out.append("&#" + (int) c + ";");
			} else {
				out.append(c);
			}
		}
		return out.toString();
	}

	/**
	 * Decodes the passed UTF-8 String using an algorithm that's compatible with
	 * JavaScript's <code>decodeURIComponent</code> function. Returns
	 * <code>null</code> if the String is <code>null</code>.
	 * 
	 * @param s
	 *            The UTF-8 encoded String to be decoded
	 * @return the decoded String
	 */
	public static String decodeURIComponent(String s) {
		if (s == null) {
			return null;
		}

		String result = null;

		try {
			s = s.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
			s = s.replaceAll("\\+", "%2B");
			result = URLDecoder.decode(s, "UTF-8");
		}
		// This exception should never occur.
		catch (UnsupportedEncodingException e) {
			result = s;

		}

		return result;
	}

	/**
	 * Encodes the passed String as UTF-8 using an algorithm that's compatible
	 * with JavaScript's <code>encodeURIComponent</code> function. Returns
	 * <code>null</code> if the String is <code>null</code>.
	 * 
	 * @param s
	 *            The String to be encoded
	 * @return the encoded String
	 */
	public static String encodeURIComponent(String s) {
		String result = null;

		try {
			result = URLEncoder.encode(s, "UTF-8");
		}

		// This exception should never occur.
		catch (UnsupportedEncodingException e) {
			result = s;
		}

		return result;
	}

}
