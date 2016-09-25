

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import database.CommonFunctions;
import database.DatabaseConnection;

/**
 * Servlet implementation class MatchResult
 */
@WebServlet("/MatchResult")
public class MatchResult extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MatchResult() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * This method is called from showGeneratedData.jsp for updating the dataset table when check box is clicked in tester mode.
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		
		int assignment_id  = 0;
		int question_id = 0 ;
		int query_id = 0 ;
		String course_id = (String) session.getAttribute("context_label");
		if(request
				.getParameter("assignment_id") != null){
			assignment_id = Integer.parseInt(request
				.getParameter("assignment_id"));
		}
		if(request
				.getParameter("question_id") != null){
			question_id = Integer.parseInt(request
					.getParameter("question_id"));
		}
		if(request.getParameter("query_id") != null ){
			query_id = Integer.parseInt(request.getParameter("query_id"));
		}
		String query = CommonFunctions.decodeURIComponent(request
				.getParameter("query"));
		String loginUsr = (String) session.getAttribute("LOGIN_USER");
		boolean error = false;
		String datasetid = request.getParameter("datasetid");
		String isUpdate = request.getParameter("isUpdate");
		Connection dbcon = null;
		
		try{
			
				 dbcon = (new DatabaseConnection()).dbConnection();
				 String datasets = "update xdata_datasetvalue set isResultMatch=? where assignment_id = ? and question_id = ? and query_id=? and course_id = ? and datasetid=?";
				 PreparedStatement pstmt = dbcon.prepareStatement(datasets);
					
				 if(isUpdate.equalsIgnoreCase("true")){
					 pstmt.setBoolean(1,true);
				 }else{
					 pstmt.setBoolean(1,false);
				 }
				pstmt.setInt(2, assignment_id);
				pstmt.setInt(3, question_id);
				pstmt.setInt(4, query_id);
				pstmt.setString(5, course_id);
				pstmt.setString(6,datasetid);
				pstmt.execute();
				 
				
		}catch(Exception e){
			throw new ServletException("Query results match, but error in updating database. Please check log file.");
		}finally{
			try {
				dbcon.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				throw new ServletException(e);
			}
		}
		
	}

	/**
	 * This method is called from showGeneratedData.jsp to compare the expected result and original istructor query result
	 * in tester mode
	 * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//Get all input from request and sessions
		HttpSession session = request.getSession(false);
		
		int assignment_id  = 0;
		int question_id = 0 ;
		int query_id = 0 ;
		String course_id = (String) session.getAttribute("context_label");
		if(request
				.getParameter("assignment_id") != null){
			assignment_id = Integer.parseInt(request
				.getParameter("assignment_id"));
		}
		if(request
				.getParameter("question_id") != null){
			question_id = Integer.parseInt(request
					.getParameter("question_id"));
		}
		if(request.getParameter("query_id") != null ){
			query_id = Integer.parseInt(request.getParameter("query_id"));
		}
		String query = CommonFunctions.decodeURIComponent(request
				.getParameter("query"));
		String loginUsr = (String) session.getAttribute("LOGIN_USER");
		boolean error = false;
		String datasetid = request.getParameter("datasetid");
		String expectedResult = request.getParameter("expectedResult");
		
		Map<String, Map<String, ArrayList<String>>> instrDataMap = null;
		//try{
		if(session.getAttribute("originalOutput") != null){
			instrDataMap = (Map<String, Map<String, ArrayList<String>>>) session.getAttribute("originalOutput");
		}
		Map<String, ArrayList<String>> instrData = instrDataMap.get(datasetid);
		
		
		//Start processing
		String[] rows = expectedResult.split("\n");
		
		//For every row in expected result
		for(int lm = 0 ; lm < rows.length; lm++){
		
		{
			String row = rows[lm];
			String tq = row.trim();
			//get values
			String[] expValue = tq.split("\\|");
			
		
		Iterator<String> it1 = instrData.keySet().iterator();
		int colSize1 = instrData.keySet().size();
		//Check if colsize matches the columns entered.
		if(expValue.length != colSize1){
			error = true;
			break;
		}
		
		int valSize1 = 0;
	
		
		for(int mn = 0 ; mn < expValue.length ; mn++){
				boolean isMatchFound = false;
			    String expVal = expValue[mn].trim();
				//For each column in original output
				while (it1.hasNext()) {
					int valCnt = 0;
					ArrayList<String> Values = instrData.get(it1.next());
					valSize1 = Values.size();
					//Check if number of rows in output matches with entered output
					if(valSize1 != rows.length){
						error = true;
						break;
					}
					//If rows, columns matches, check the values.
					if(expVal.equalsIgnoreCase(Values.get(lm))){
						isMatchFound = true;
						
					}
					else{
						isMatchFound = false;
						
					}
					break;
				}//While ends
				
				if(!isMatchFound){
					error= true;
					break;
				}
		}//For each value in the row
		
		}
		}//For number of rows in text Area ends
		if(error){
			throw new ServletException("Expected and Original query output mismatch.");
		}
		else if(!error){
			Connection dbcon = null;
			try{
			//Update the dataset Table with dataset as matched.
			dbcon = (new DatabaseConnection()).dbConnection();
			String datasets = "update xdata_datasetvalue set isResultMatch=true where assignment_id = ? and question_id = ? and query_id=? and course_id = ? and datasetid=?";
			PreparedStatement pstmt = dbcon.prepareStatement(datasets);
			pstmt.setInt(1, assignment_id);
			pstmt.setInt(2, question_id);
			pstmt.setInt(3, query_id);
			pstmt.setString(4, course_id);
			pstmt.setString(5,datasetid);
			pstmt.execute();
			 
			}catch(Exception e){
				throw new ServletException("Query results match, but error in updating database." + e);
			}finally{
				try {
					dbcon.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					throw new ServletException(e);
				}
			}
		}
		/*}catch(Exception e){
			e.printStackTrace();
		}*/
	}

}
