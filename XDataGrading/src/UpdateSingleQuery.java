
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import partialMarking.PartialMarkParameters;

import com.google.gson.Gson;

import testDataGen.PopulateTestDataGrading;
import database.DatabaseConnection;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;

/**
 * Servlet implementation class UpdateSingleQuery
 */
@WebServlet("/UpdateSingleQuery")
public class UpdateSingleQuery extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(UpdateSingleQuery.class.getName());
	/**
	 * @see HttpServlet#HttpServlet() 
	 */
	public UpdateSingleQuery() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * This method will be called from servlet to check if all input queries are syntactically correct.
	 * If not, it will return the first incorrect query id to show error message in UI
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		//Test submittted queries
		String optionalSchemaId = request.getParameter("optionalschemaid");
		//Get selected default data sets
		String[] defaultDSId = request.getParameterValues("defaultDSId");
		Gson gson = new Gson();
		String json = gson.toJson(defaultDSId);
		String questionId = request.getParameter("question_id");
		int qId = Integer.parseInt(questionId);
		int asID = Integer.parseInt(request.getParameter("assignment_id"));
		int optId = Integer.parseInt(optionalSchemaId);
		try{
		//Get queries as request parameters
		String queryToSave = "";
		//queryToSave=get first query
		PopulateTestDataGrading p = new PopulateTestDataGrading();
		Connection graderConn = new util.DatabaseConnection().getGraderConnection(asID);
		p.deleteAllTempTablesFromTestUser(graderConn);
		p.createTempTables(graderConn, asID, qId);
		
	
		//Before updating the query run it in the database and check if it is syntactically correct
		Statement graderStatement = graderConn.createStatement();
		graderStatement.execute(queryToSave);
		}catch(Exception e){
			logger.log(Level.SEVERE,"Exception in UpdateSingleQuery: Syntax error in the query.  \n" + e.getMessage(),e);
			throw new ServletException(e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		String loginUsr = "";
		loginUsr = (String) session.getAttribute("LOGIN_USER");
		
		String[] editedQueries = request.getParameterValues("query"); 
		String[] newQueries = request.getParameterValues("newQuery");		
		String[] existingQueryMarks = request
				.getParameterValues("existingMarks");
		String[] newQueryMarks = request.getParameterValues("newMarks");
		String matchAll = request.getParameter("matchAll");
		boolean matchAllQueries = false;
		if (matchAll != null && matchAll.equals("matchAll")) {
			matchAllQueries = true;
		}

		String desc = request.getParameter("quesTxt");
		String optionalSchemaId = request.getParameter("optionalschemaid");
		//Get selected default data sets
		String[] defaultDSId = request.getParameterValues("defaultDSId");
		Gson gson = new Gson();
		String json = gson.toJson(defaultDSId);
		String partialParamJson = "";
		String questionId = request.getParameter("question_id");
		int maxMarks = Integer.parseInt(request.getParameter("maxMarks"));
		int qId = Integer.parseInt(questionId);
		int asID = Integer.parseInt(request.getParameter("assignment_id"));
		int optId = Integer.parseInt(optionalSchemaId);
		Hashtable<Integer, String> existingQueries = (Hashtable<Integer, String>) session
				.getAttribute("existingSQL");
		String[] queryIdsEdited = request.getParameterValues("queryIdsEdited");
		int newQueryId = 1;
		String courseID = (String) request.getSession().getAttribute(
				"context_label");
		String queryDesc = desc;/*.trim().replaceAll("\r\n+", " ").trim()
				.replaceAll("\n+", " ").trim().replaceAll(" +", " ");*/

	
				
				
		logger.log(Level.FINE,"Desc: " + queryDesc);
		logger.log(Level.FINE,"optional schema id" + optId);
		Connection graderConn=null;
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){
			try(PreparedStatement stmt = dbcon
					.prepareStatement("SELECT * from xdata_qinfo where course_id=? and assignment_id=? and question_id=?")){
			stmt.setString(1, courseID);
			stmt.setInt(2, asID);
			stmt.setInt(3, qId);
			ResultSet rs = stmt.executeQuery();
			
			try(PreparedStatement stmt1 = dbcon
					.prepareStatement("SELECT MAX(query_id) from xdata_instructor_query where course_id=? and assignment_id=? and question_id=?")){
			stmt1.setString(1, courseID);
			stmt1.setInt(2, asID);
			stmt1.setInt(3, qId);
			try(ResultSet rs1 = stmt1.executeQuery()){
				if (rs1.next()) {
					newQueryId = (rs1.getInt(1)) + 1;
				}
			}
			PopulateTestDataGrading p = new PopulateTestDataGrading();
			graderConn = new util.DatabaseConnection().getGraderConnection(asID);
			p.deleteAllTempTablesFromTestUser(graderConn);
			p.createTempTables(graderConn, asID, qId);
			if (rs.next()) {
				/** query already present */

				try(PreparedStatement stmt2 = dbcon
						.prepareStatement("UPDATE xdata_qinfo SET querytext=?, " +
								"optionalschemaid=?, " +
								"matchallqueries=?, totalmarks=?,default_sampledataid=?  " +
								"WHERE course_id=? and assignment_id=? and question_id=?")){

				stmt2.setString(1, queryDesc);
				stmt2.setInt(2, optId);
				stmt2.setBoolean(3, matchAllQueries);
				stmt2.setInt(4, maxMarks);
				stmt2.setString(5,json);
				stmt2.setString(6, courseID);
				stmt2.setInt(7, asID);
				stmt2.setInt(8, qId);
				
				stmt2.executeUpdate();
				}//stmt2.close
				/** Insert the SQL queries into QUERY table **/
				// Update Edited queries
				if (editedQueries != null && editedQueries.length > 0) {
					for (int i = 0; i < editedQueries.length; i++) {
						partialParamJson="";
						String partialParamKey = asID+"_"+qId+"_"+queryIdsEdited[i];
						PartialMarkParameters partialMarkParam = new PartialMarkParameters();
						if(session.getAttribute(partialParamKey) != null){
							partialMarkParam = (PartialMarkParameters)session.getAttribute(partialParamKey);	
						}
						//System.out.println("partialMarkParam.getValue == "+partialMarkParam.getValue());
						//If there is no value set in the session, initialize to its default value
						if(partialMarkParam.getValue() == 0){
							partialMarkParam = new PartialMarkParameters();
						} 
						partialParamJson = gson.toJson(partialMarkParam);
						String queryToSave = editedQueries[i];/*.trim()
								.replaceAll("\r\n+", " ").trim()
								.replaceAll("\n+", " ").trim()
								.replaceAll(" +", " ").trim().replace(";", "")
								.trim();*/
						try{
								//Before updating the query run it in the database and check if it is syntactically correct						
								Statement graderStatement = graderConn.createStatement();
								graderStatement.execute(queryToSave);
						}catch(Exception e){
							logger.log(Level.SEVERE,"Exception in UpdateSingleQuery: Syntax error in the query. \n" + e.getMessage(),e);
							if(graderConn != null && ! graderConn.isClosed())
								graderConn.close();
							throw new ServletException(e);
						}
						// If it is existing query, then update
						try(PreparedStatement stmt3 = dbcon
								.prepareStatement("UPDATE xdata_instructor_query SET sql=?, marks = ?,partialmarkinfo=?  where course_id=? and assignment_id=? and question_id=? and query_id=?")){
								stmt3.setString(1, queryToSave);
								stmt3.setInt(2, Integer.parseInt(existingQueryMarks[i]));
								stmt3.setString(3,partialParamJson);
								stmt3.setString(4, courseID);
								stmt3.setInt(5, asID);
								stmt3.setInt(6, qId);
								stmt3.setInt(7, Integer.parseInt(queryIdsEdited[i]));
								stmt3.executeUpdate();
								if(session.getAttribute(partialParamKey) != null){
									session.removeAttribute(partialParamKey);
								}
						}//stmt3.end
						/**
						 * For existing queries,find whether they are edited.If
						 * so, delete the data generated - Start
						 **/
						if(queryIdsEdited[i] != null){
						//Remove all indentaions for comparing
							
						String existingSql = "";
						if(existingQueries.contains(queryIdsEdited[i])){
							existingSql= (existingQueries.get(Integer
						
								.parseInt(queryIdsEdited[i]))).trim()
								.replaceAll("\r\n+", " ").trim()
								.replaceAll("\n+", " ").trim()
								.replaceAll(" +", " ").trim().replace(";", "")
								.trim();
						}
						queryToSave = queryToSave.trim().replaceAll("\r\n+", " ").trim()
								.replaceAll("\n+", " ").trim()
								.replaceAll(" +", " ").trim().replace(";", "")
								.trim();
						
						logger.log(Level.FINE,"ExistingSQL = " + existingSql);
						logger.log(Level.FINE,"Edited SQL = " + queryToSave);
						
						if (existingSql != null && !existingSql.isEmpty() && !existingSql.equalsIgnoreCase(queryToSave)) {

							// If queries are not equal, then delete the data
							// generated for old query
							// Take that out from session list
							ArrayList dataSetGeneratedList = (ArrayList) session
									.getAttribute("DataGenerationCompleted");
							String keyId = loginUsr + "&" + asID + "&"
									+ questionId + "&" + queryIdsEdited[i];
							if (dataSetGeneratedList != null
									&& dataSetGeneratedList.size() > 0) {
								dataSetGeneratedList.remove(keyId);
								session.setAttribute("DataGenerationCompleted",
										dataSetGeneratedList);
							}
							try(PreparedStatement stmt4 = dbcon
									.prepareStatement("delete from xdata_datasetvalue WHERE assignment_id=? and question_id=? and query_id=? and course_id = ?")){

								stmt4.setInt(1, asID);
								stmt4.setInt(2, qId);
								stmt4.setInt(3, Integer.parseInt(queryIdsEdited[i]));
								stmt4.setString(4,courseID);
								stmt4.executeUpdate();
							}
							if(((String)session.getAttribute("LOGIN_USER")).equalsIgnoreCase("tester")){
								try(PreparedStatement stmnt1 = dbcon.prepareStatement("update xdata_qinfo set equivalence_failed_datasets = ?, equivalencestatus = ? where assignment_id=? and question_id=? and course_id = ?" )){
									stmnt1.setString(1,null);
									stmnt1.setBoolean(2,false);
									stmnt1.setInt(3, asID);
									stmnt1.setInt(4, qId);
									stmnt1.setString(5, courseID);
									stmnt1.executeUpdate();
								}
							}
							existingQueries.remove(Integer
									.parseInt(queryIdsEdited[i]));
						} else {
							if(existingQueries.contains(queryIdsEdited[i])){
								existingQueries.remove(Integer
									.parseInt(queryIdsEdited[i]));
							}
						}
						}
						/**
						 * For existing queries, on edit, delete the data
						 * generated - End
						 **/
					}
					session.setAttribute("existingQueries", existingQueries);
				}
				// Add new queries
				if (newQueries != null && newQueries.length > 0) {
				
					for (int i = 0; i < newQueries.length; i++) {
						//System.out.println(" NEW Queries when submited : " + newQueries[i]);
						
						if (newQueries[i] != null) {
							//Test query id here
							partialParamJson="";
							String partialParamKey = asID+"_"+qId+"_"+newQueryId;
							PartialMarkParameters partialMarkParam = new PartialMarkParameters();
							if(session.getAttribute(partialParamKey) != null){
								partialMarkParam = (PartialMarkParameters)session.getAttribute(partialParamKey);								

							}
							//System.out.println("partialMarkParam.getValue == "+partialMarkParam.getValue());
							if(partialMarkParam.getValue() == 0){
								partialMarkParam = new PartialMarkParameters();
							}
							partialParamJson = gson.toJson(partialMarkParam);
							String queryToSave = newQueries[i];
							/*.trim()
									.replaceAll("\r\n+", " ").trim() 
									.replaceAll("\n+", " ").trim()
									.replaceAll(" +", " ").trim()
									.replace(";", "");
									*/
 
							try{
								//Before updating the query run it in the database and check if it is syntactically correct
								Statement graderStatement = graderConn.createStatement();
								graderStatement.execute(queryToSave);
								}catch(Exception e){
									logger.log(Level.SEVERE,"Exception in UpdateSingleQuery: Syntax error in the query.  \n" + e.getMessage(),e);
									if(graderConn != null && ! graderConn.isClosed())
										graderConn.close();
									throw new ServletException(e);
								}
							 
							// New query added, so insert the new query
							try(PreparedStatement stmt6 = dbcon
									.prepareStatement("insert into xdata_instructor_query Values (?,?,?,?,?,?,?)")){
								stmt6.setInt(1, asID);
								stmt6.setInt(2, qId);
								stmt6.setString(3, queryToSave);
								//stmt6.setString(4, "");
								stmt6.setString(4, courseID);
								stmt6.setInt(5, newQueryId);
								stmt6.setInt(6, Integer.parseInt(newQueryMarks[i]));
								stmt6.setString(7,partialParamJson);
								newQueryId++;
								stmt6.executeUpdate();
								if(session.getAttribute(partialParamKey) != null){
									session.removeAttribute(partialParamKey);
								}
								
							}//stmt6 ends
						}
					}
				}

			} else {

				try(PreparedStatement stmt7 = dbcon
						.prepareStatement("INSERT INTO xdata_qinfo VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)")){
					stmt7.setString(1, courseID);
					stmt7.setInt(2, asID);
					stmt7.setInt(3, qId);
					stmt7.setString(4, queryDesc);
					stmt7.setString(5, "");
					stmt7.setInt(6, maxMarks);
					stmt7.setBoolean(7, false);
					stmt7.setBoolean(8, false);
					stmt7.setBoolean(9, matchAllQueries);
					stmt7.setInt(10, 1);
					stmt7.setInt(11, optId);
					stmt7.setBoolean(12, true);
					stmt7.setString(13,json);
					stmt7.executeUpdate();
				}//stmt7 ends
				// New query added, so insert the new query
				for (int i = 0; i < newQueries.length; i++) {

					String queryToSave = newQueries[i];
					/*.trim()
							.replaceAll("\r\n+", " ").trim()
							.replaceAll("\n+", " ").trim()
							.replaceAll(" +", " ").trim().replace(";", "");
							*/
					PartialMarkParameters partialMarkParam = new PartialMarkParameters();
					String partialParamKey = asID+"_"+qId+"_"+newQueryId;
					if(session.getAttribute(partialParamKey) != null){
						partialMarkParam = (PartialMarkParameters)session.getAttribute(partialParamKey);								

					}
					//System.out.println("partialMarkParam.getValue == "+partialMarkParam.getValue());
					if(partialMarkParam.getValue() == 0){
						partialMarkParam = new PartialMarkParameters();
					}
					partialParamJson = gson.toJson(partialMarkParam);
					try{
						//Before updating the query run it in the database and check if it is syntactically correct
						Statement graderStatement = graderConn.createStatement();
						graderStatement.execute(queryToSave);
						}catch(Exception e){
							logger.log(Level.SEVERE,"Exception in UpdateSingleQuery: Syntax error in the query. \n" + e.getMessage(),e);
							if(graderConn != null && ! graderConn.isClosed())
								graderConn.close();
							throw new ServletException(e);
						}
					
					try(PreparedStatement stmt3 = dbcon
							.prepareStatement("insert into xdata_instructor_query Values (?,?,?,?,?,?,?)")){
							stmt3.setInt(1, asID);
							stmt3.setInt(2, qId);
							stmt3.setString(3, queryToSave);
							//stmt3.setString(4, "");
							stmt3.setString(4, courseID);
							stmt3.setInt(5, newQueryId);
							stmt3.setInt(6, Integer.parseInt(newQueryMarks[i]));
							stmt3.setString(7, partialParamJson);
							newQueryId++;
							logger.log(Level.FINE,"Query Table SQL = " + stmt3);
							stmt3.executeUpdate();
					}
				}
			}
			}//stmt1.close
			}//stmt.close
			String remoteLink = "asgnmentList.jsp?assignmentId=" + asID+"&showQuestions=true";
			if(((String)session.getAttribute("LOGIN_USER")).equalsIgnoreCase("tester")){
				remoteLink = "ListOfTesterQuestions.jsp?AssignmentID="+asID; 
			}
			response.sendRedirect(remoteLink);
			
		} catch (SQLException sep) {
			logger.log(Level.SEVERE,sep.getMessage(),sep);
			throw new ServletException(sep);

		}catch(Exception e){
			logger.log(Level.SEVERE,"Exception in UpdateSingleQuery:" + e.getMessage(),e);
			throw new ServletException(e);
		}finally{
			try {
				if(graderConn != null && ! graderConn.isClosed()){
					graderConn.close();
				} 
				}catch (SQLException e) {
					logger.log(Level.SEVERE,e.getMessage(),e);
				
			}
		}
		

	}

}

