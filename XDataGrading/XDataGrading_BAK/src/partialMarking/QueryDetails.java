

package partialMarking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import parsing.QueryStructure;
import testDataGen.GenerateCVC1;
import testDataGen.preProcessForDataGeneration;
import util.MyConnection;

public class QueryDetails {
	
	// Start Region - Private members
	
	
	private GenerateCVC1 data;
		
	// End Region - Private members
	
	// Start Region - Public members

	public String query;

	// End Region - Public members
	
		public GenerateCVC1 getData(){
		return this.data;
	}
	
	public void setData(GenerateCVC1 data){
		this.data = data;
	}
	
	public QueryDetails(){
	}
	
	public int InitializeInstructorQuery(int assignmentId, int questionId, int queryId) throws Exception {		
		String qry = "select * from xdata_instructor_query where assignment_id = ? and question_id = ? and query_id = ?";		
		//Connection conn = MyConnection.getExistingDatabaseConnection();
		try(Connection conn = MyConnection.getDatabaseConnection()){
			try(PreparedStatement pstmt = conn.prepareStatement(qry)){
				pstmt.setInt(1, assignmentId);
				pstmt.setInt(2, questionId);
				pstmt.setInt(3, queryId);
				
				try(ResultSet rs = pstmt.executeQuery()){
					int marks = 0;		
					
					String sqlQuery = null;
					if(rs.next()){
						sqlQuery = rs.getString("sql");
						marks = rs.getInt("marks");
					}
					this.query = sqlQuery;
					//this.initialize(assignmentId, questionId, sqlQuery);
					this.startProcessing(assignmentId, questionId, sqlQuery);
					return marks;
				}
			}
			}
			
		
	}

	public void InitializeStudentQuery(int aId, int qId, String rollNum, String guestStudentQuery) throws Exception{		
		String qry = "select * from xdata_student_queries where assignment_id = ? and question_id = ? and rollnum = ?";		
		if(guestStudentQuery == null || guestStudentQuery.isEmpty()){
		//Connection conn = MyConnection.getExistingDatabaseConnection();
		try(Connection conn = MyConnection.getDatabaseConnection()){
			try(PreparedStatement pstmt = conn.prepareStatement(qry)){
				pstmt.setInt(1, aId);
				pstmt.setInt(2, qId);
				pstmt.setString(3, rollNum);
				
				try(ResultSet rs = pstmt.executeQuery()){
					String sqlQuery = null;
					if(rs.next()){	
						sqlQuery = rs.getString("querystring");
					}else{
						sqlQuery = guestStudentQuery; 
					}
					this.query = sqlQuery;
					//this.initialize(aId, qId, sqlQuery);
					this.startProcessing(aId, qId, sqlQuery);
		}
		}
		}
	}else{
		String sqlQuery = guestStudentQuery;
		this.query = sqlQuery;
		//this.initialize(aId, qId, sqlQuery);
		this.startProcessing(aId, qId, sqlQuery);
	}
		
	}
	
	QueryStructure qStructure;
	
	public QueryStructure getQueryStructure(){
		return qStructure;
	}
	
	
	public void startProcessing(int assignmentId, int questionId, String query) throws Exception {

		GenerateCVC1 cvc = new GenerateCVC1();
		
		cvc.setAssignmentId(assignmentId);
		cvc.setQuestionId(questionId);
		cvc.setQueryId(1);
		cvc.setCourseId("");

		preProcessForDataGeneration preProcess = new preProcessForDataGeneration();

		preProcess.initializeConnectionDetails(cvc);
				
		qStructure=new QueryStructure(cvc.getTableMap());
				
		cvc.closeConn();
		
		try {
		qStructure.buildQueryStructure("1",query);
		}
		catch(Exception e){
			System.out.println(e);
			System.out.println("*********************EXCEPTION******************************");
			System.out.println(assignmentId+"\t"+questionId+"\t"+query);
			System.out.println("************************************************************");
		}
		
		
	}


}

