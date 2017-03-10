package partialMarking;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.gson.Gson;
import parsing.AggregateFunction;
import parsing.Node;
import util.MyConnection;
import parsing.QueryStructure;
public class PartialMarker {
	private static Logger logger = Logger.getLogger(PartialMarker.class.getName());
	// The unique identifier of the assignment
	int assignmentId;
	
	// The unique identifier for the question within the assignment
	int questionId;
	
	// The unique identifier for the query within the question
	int queryId;
	
	//Unique identifier for the course
	String course_id;
	
	// The id of the student
	String studentId;
	
	// Maximum marks
	static int maxMarks=100;
	
	String guestStudentQuery;
	
	// Details corresponding to the instructor query
	public QueryDetails InstructorQuery;
	
	// Details corresponding to the student query 
	public QueryDetails StudentQuery;
	
	// Configuration values required for the scoring function
	public static PartialMarkerConfig Configuration;
	
	// Returns the assignment id
	public int getAssignmentId(){
		return this.assignmentId;
	}
	
	// Returns the question id
	public int getQuestionId(){
		return this.questionId;
	}
	
	// Returns the question id
	public int getQueryId(){
		return this.queryId;
	}
	
	// Returns the student id
	public String getStudentId(){
		return this.studentId;
	}
	
	// Sets the assignment id
	public void setAssignmentId(int aId){
		this.assignmentId = aId;
	}
	
	// Sets the question id
	public void setQuestionId(int aId){
		this.questionId = aId;
	}
	
	// Sets the question id
	public void setQueryId(int qId){
		this.queryId = qId;
	}
	
	// Sets the student id
	public void setStudentQuery(String query){
		this.studentId = query;
	}
	
	// Returns an instance of the partial marker
	public PartialMarker(int assignmentId, int quesId, int queryId, String course_id, String rollNum){
		this.assignmentId = assignmentId;
		this.questionId = quesId;
		this.queryId = queryId;
		this.course_id = course_id;
		this.studentId = rollNum;
		this.InstructorQuery = new QueryDetails();
		this.StudentQuery = new QueryDetails();
		this.guestStudentQuery = null;
		//the following two members changed to by mathew (oct 20 2016)
		PartialMarker.Configuration = new PartialMarkerConfig();
		PartialMarker.maxMarks = 100;
		try{
			PartialMarker.Configuration.setConfigurationValues(assignmentId, quesId, queryId);
		} catch (Exception ex){
			logger.log(Level.SEVERE,ex.getMessage(),ex);
			//ex.printStackTrace();
		}
	}
	
	public PartialMarker(int assignmentId, int quesId, int queryId, String course_id, String rollNum,String studQuery){
		this.assignmentId = assignmentId;
		this.questionId = quesId;
		this.queryId = queryId;
		this.course_id = course_id;
		this.studentId = rollNum;
		this.InstructorQuery = new QueryDetails();
		this.StudentQuery = new QueryDetails();
		this.guestStudentQuery = studQuery;
		//the following two members changed to by mathew (oct 20 2016)
		PartialMarker.maxMarks = 100;  
		PartialMarker.Configuration = new PartialMarkerConfig();
		try{
			PartialMarker.Configuration.setConfigurationValues(assignmentId, quesId, queryId);
		} catch (Exception ex){
			logger.log(Level.SEVERE,ex.getMessage(),ex);
			//ex.printStackTrace();
		}
	}
	
	
	private void initialize(){
		try{		
			PartialMarker.maxMarks = this.InstructorQuery.InitializeInstructorQuery(this.assignmentId, this.questionId, this.queryId);
			this.StudentQuery.InitializeStudentQuery(this.assignmentId, this.questionId, this.studentId,this.guestStudentQuery);
		}
		catch(Exception ex){
			logger.log(Level.SEVERE,ex.getMessage(), ex);
			//ex.printStackTrace();
		}
	}
	
	// Returns the marks corresponding to the query of the student in comparison to the instructor query
	public MarkInfo getMarksForQueryStructures() throws Exception{
		
		this.initialize();
			
		// Canonicalizing the queries
		CanonicalizeQuery.Canonicalize(this.InstructorQuery.getQueryStructure());
		CanonicalizeQuery.Canonicalize(this.StudentQuery.getQueryStructure());
		
		//Check for distinct
		//boolean evaluateDistinct = EvaluateDistinct.evaluate(this.InstructorQuery,this.StudentQuery,this.assignmentId, this.questionId, this.queryId, this.course_id);		
		
		float maxMainQueryScore = PartialMarker.calculateScore(this.InstructorQuery.getQueryStructure(), this.InstructorQuery.getQueryStructure(), 0).Marks;
		
		MarkInfo result = calculateScore(this.InstructorQuery.getQueryStructure(), this.StudentQuery.getQueryStructure(), 0);
		float studentQueryScore=result.Marks;
	
		result.Configuration = Configuration;
		
		float mainQueryScore = result.Marks;
				
		// Setting the negative scores to zero
		if(mainQueryScore < 0) 
			mainQueryScore = 0;
		
		if(maxMainQueryScore<=0.0001f){
			result.Marks=0.0f;
		}
		else
			result.Marks = mainQueryScore/maxMainQueryScore * PartialMarker.maxMarks ;
		System.out.println("Computed Marks="+result.Marks+ " student score="+studentQueryScore +" mainqueryScore="+maxMainQueryScore);
		return result;
	}
	
		
	public static void main(String args[]) throws Exception{		
		//Connection conn = MyConnection.getExistingDatabaseConnection();
		try(Connection conn = MyConnection.getDatabaseConnection()){
		
			String instructorQuery=" SELECT distinct time_slot.day FROM teaches, section, time_slot where teaches.course_id=section.course_id AND teaches.semester=section.semester AND teaches.year=section.year AND teaches.sec_id=section.sec_id AND section.time_slot_id= time_slot.time_slot_id AND section.semester='Fall' AND section.year='2009' and teaches.id='22222'";
			String studentAnswer="select time_slot.time_slot_id FROM teaches, section, time_slot where teaches.course_id=section.course_id AND teaches.semester=section.semester AND teaches.year=section.year AND teaches.sec_id=section.sec_id AND section.time_slot_id= time_slot.time_slot_id AND section.semester='Fall' AND section.year='2009' and teaches.id='22222'";
		String courseId = "CS632";
		int assignmentId =11;
		int questionId = 1;
		String rollnum = "09005027";
		int queryId = 1;		
		int maxMarks = 100;
		String oldQueryId = "A" + assignmentId + "Q" + questionId + "S" + queryId;
		String desc = "DUMMY";
		
		insertIntoQinfo(courseId, assignmentId, questionId, queryId, instructorQuery, desc, maxMarks, maxMarks);
		
		//WriteFileAndUploadDatasets.updateQueryInfo(new GenerateDataset_new(""), assignmentId, questionId, queryId, instructorQuery, "Find, for each course, the number of distinct students who have taken the course");
		
		try(PreparedStatement stmt = conn.prepareStatement("select * from xdata_student_queries where assignment_id = ? and question_id = ? and rollnum = ?")){
		stmt.setInt(1, assignmentId);
		stmt.setInt(2, questionId);
		stmt.setString(3, rollnum);
		try(ResultSet rs = stmt.executeQuery()){
		
		if(!rs.next()){		
			String insertquery = "INSERT INTO xdata_student_queries (dbid, queryid, rollnum, querystring, assignment_id, question_id,course_id) VALUES (?,?,?,?,?,?,?)";			
			try(PreparedStatement stmt1 = conn.prepareStatement(insertquery)){
			stmt1.setString(1, "d1");
			stmt1.setString(2, oldQueryId);
			stmt1.setString(3, rollnum);
			stmt1.setString(4, studentAnswer);
			stmt1.setInt(5, assignmentId);
			stmt1.setInt(6, questionId);
			stmt1.setString(7,courseId);
			stmt1.executeUpdate();
			}
		} else {
			try(PreparedStatement stmt1 = conn.prepareStatement("update xdata_student_queries set querystring = ? where assignment_id = ? and question_id = ? and rollnum = ? and course_id=?")){
				stmt1.setString(1, studentAnswer);
				stmt1.setInt(2, assignmentId);
				stmt1.setInt(3, questionId);
				stmt1.setString(4, rollnum);			
				stmt1.setString(5,courseId);
				stmt1.executeUpdate();
			}
		}
		}	
				
		PartialMarker part = new PartialMarker(assignmentId, questionId, queryId, courseId, rollnum);
		//MarkInfo result = part.getMarks(); commented and line following added by mathew on 19 oct 2016
		MarkInfo result = part.getMarksForQueryStructures();
		Gson gson = new Gson();
		String json = gson.toJson(result);
		String updateScoreQuery = "update xdata_student_queries set score = ?,markinfo=?,max_marks=? where assignment_id=? and question_id=? and rollnum=?";
		try(PreparedStatement ps = conn.prepareStatement(updateScoreQuery)){
		ps.setFloat(1, result.Marks);
		ps.setString(2, json);
		ps.setInt(3, maxMarks);
		ps.setFloat(4, assignmentId);
		ps.setInt(5,questionId);
		ps.setString(6, rollnum);
		ps.executeUpdate();
		}
		
		}
		}
	}
	
	private static void insertIntoQinfo(String courseId, int asId, int qId, int queryId, String query, String desc, int maxMarks, int marks) throws Exception{		
		//Connection conn = MyConnection.getExistingDatabaseConnection();
		try(Connection conn = MyConnection.getDatabaseConnection()){
		
		try(PreparedStatement stmt = conn.prepareStatement("select * from xdata_qinfo where course_id = ? and assignment_id = ? and question_id = ?")){
			stmt.setString(1, courseId);
			stmt.setInt(2, asId);
			stmt.setInt(3, qId);		
			
			ResultSet rs = stmt.executeQuery();
			if(!rs.next()){		
				try(PreparedStatement stmt1 = conn.prepareStatement("INSERT INTO xdata_qinfo VALUES (?,?,?,?,?,?,?,?,?,?,?,?)")){
					stmt1.setString(1, courseId);
					stmt1.setInt(2, asId);
					stmt1.setInt(3, qId);		
					stmt1.setString(4, desc); 
					stmt1.setString(5, "");
					stmt1.setInt(6, maxMarks); 
					stmt1.setBoolean(7, false);
					stmt1.setBoolean(8, false); 
					stmt1.setBoolean(9, true);
					stmt1.setInt(10, queryId);
					stmt1.setInt(11, 15);
					stmt1.setBoolean(12, true);
					stmt1.executeUpdate();
				}
			} else {
				try(PreparedStatement stmt1 = conn.prepareStatement("update xdata_qinfo set querytext = ?, totalmarks = ? where course_id = ? and assignment_id = ? and question_id = ?")){
					stmt1.setString(1, desc); 
					stmt1.setInt(2, maxMarks);
					stmt1.setString(3, courseId);
					stmt1.setInt(4, asId);
					stmt1.setInt(5, qId);
					stmt1.executeUpdate();
				}
			}
		
			try(PreparedStatement stmt1 = conn.prepareStatement("select * from xdata_instructor_query where course_id = ? and assignment_id = ? and question_id = ? and query_id = ?")){
				stmt1.setString(1, courseId);
				stmt1.setInt(2, asId);
				stmt1.setInt(3, qId);
				stmt1.setInt(4, queryId);
				
				rs = stmt1.executeQuery();
				if(!rs.next()){
					try(PreparedStatement stmt2 = conn.prepareStatement("insert into xdata_instructor_query Values (?,?,?,?,?,?)")){
						stmt2.setInt(1, asId); 
						stmt2.setInt(2, qId);
						stmt2.setString(3, query); 
						//stmt2.setString(4,""); 
						stmt2.setString(4, courseId); 
						stmt2.setInt(5, queryId);
						stmt2.setInt(6, marks);
						
						stmt2.executeUpdate();
					}
				} else {
					try(PreparedStatement stmt2 = conn.prepareStatement("update xdata_instructor_query set sql = ?, marks = ? where course_id = ? and assignment_id = ? and question_id = ? and query_id = ?")){
						stmt2.setString(1, query); 
						stmt2.setInt(2, marks);
						stmt2.setString(3, courseId);
						stmt2.setInt(4, asId);
						stmt2.setInt(5, qId);
						stmt2.setInt(6, queryId);
						stmt2.executeUpdate();
					}
				}	
			}
		}
	}
	}
	
	// Calculates a score based on the relations involved in the join
	// Number of inner and outer joins are also compared
	public static float getJoinScore(QueryStructure masterData, QueryStructure slaveData){
		float score = compareSelection(masterData.getLstJoinConditions(), slaveData.getLstJoinConditions());
		
		score = masterData.getNumberOfOuterJoins() == slaveData.getNumberOfOuterJoins() ? score + 1 : score - 0.5f;
		score = masterData.getNumberOfInnerJoins() == slaveData.getNumberOfInnerJoins() ? score + 1 : score - 0.5f;
		
		return score;
	}
	
	public static float compare(ArrayList<String> master, ArrayList<String> slave) {
		float score = 0;
		for(String n1 : slave){
			Boolean found = false;
			for(String n2 : master){				
				if(n1.equals(n2)){
					found = true;
					break;
				}
			}
			
			if(found){
				score++;
			}
			else{
				score=score-0.5f;
			}
		}
		
		return score;
	}

/** @author bharath, recoded by mathew
 * 
 * checks if two nodes that represents selection clauses are syntactically identical or not, 
 * returns true iff if they are identical 
 * 	
 * @param n1
 * @param n2

 * @return boolean
 */
	
public static Boolean checkSelectionEquality(Node n1, Node n2){
		
	if(!n1.getOperator().equals(n2.getOperator()))
		return false;		
	
	//if left node of n1 is a column reference
		if(n1.getLeft().getNodeType().equals(Node.getColRefType())){
			if(!n2.getLeft().getNodeType().equals(Node.getColRefType()))
				return  false;
			if(!n1.getLeft().getTable().getTableName().equals(n2.getLeft().getTable().getTableName()))
				return false;
			
			if(!n1.getLeft().getTableNameNo().equals(n2.getLeft().getTableNameNo()))
				return false;
			if(!n1.getLeft().getColumn().getColumnName().equals(n2.getLeft().getColumn().getColumnName()))
				return false;
		}
		//if left node of n1 is a constant value
		if(n1.getLeft().getNodeType().equals(Node.getValType())){
			if(!n2.getLeft().getNodeType().equals(Node.getValType()))
				return  false;
			if(!n1.getLeft().getStrConst().equals(n2.getLeft().getStrConst()))
				return false;
		}
	
	if(n1.getRight().getNodeType().equals(Node.getColRefType())){
		
		if(!n2.getRight().getNodeType().equals(Node.getColRefType()))
			return  false;
		
		if(!n1.getRight().getTable().getTableName().equals(n2.getRight().getTable().getTableName()))
			return false;
		
		if(!n1.getRight().getTableNameNo().equals(n2.getRight().getTableNameNo()))
			return false;
		
		if(!n1.getRight().getColumn().getColumnName().equals(n2.getRight().getColumn().getColumnName()))
			return false;
	}
	
	if(n1.getRight().getNodeType().equals(Node.getValType())){
		if(!n2.getRight().getNodeType().equals(Node.getValType()))
			return  false;
		
		if(!n1.getRight().getStrConst().equals(n2.getRight().getStrConst()))
			return false;
	}
	
	return true;
		
	}
	
public static Boolean checkProjectionEquality(Node n1, Node n2){
	if(n1.getNodeType().equals(Node.getAggrNodeType())){
		
		if(!n2.getNodeType().equals(Node.getAggrNodeType()))
			return false;
		
		AggregateFunction agg1 = n1.getAgg();
		AggregateFunction agg2 = n2.getAgg();
		
		if(!agg1.getFunc().equals(agg2.getFunc()))
			return false;
		
		if(!agg1.getAggExp().getColumn().getTable().getTableName().equals(agg2.getAggExp().getColumn().getTable().getTableName()))
			return false;
		
		if(!agg1.getAggExp().getTableNameNo().equals(agg2.getAggExp().getTableNameNo()))
			return false;
		
		if(!agg1.getAggExp().getColumn().getColumnName().equals(agg2.getAggExp().getColumn().getColumnName()))
			return false;
	}
	
	if(n1.getNodeType().equals(Node.getColRefType())){
		
		if(!n1.getTable().getTableName().equals(n2.getTable().getTableName()))
			return false;
		
		if(!n1.getTableNameNo().equals(n2.getTableNameNo()))
			return false;
		
		if(!n1.getColumn().getColumnName().equals(n2.getColumn().getColumnName()))
			return false;
	}
	
	return true;
}
	
/* recoded by mathew on 12 May 2016, 
 * 
 * checks the syntactic equivalence of two nodes that represents atomic having clause expressions
 */
public static Boolean checkHavingClauseEquality(Node n1, Node n2){
	//check for the equivalence of operator
	if(!n1.getOperator().equals(n2.getOperator()))
		return false;
	
	//check for equivalence of right nodes
	
	//if right node of n1 is a column reference
	if(n1.getRight().getNodeType().equals(Node.getColRefType())){
		if(!n2.getRight().getNodeType().equals(Node.getColRefType()))
			return  false;
		
		if(!n1.getRight().getTableNameNo().equals(n2.getRight().getTableNameNo()))
			return false;
		if(!n1.getRight().getTable().getTableName().equals(n2.getRight().getTable().getTableName()))
			return false;
		if(!n1.getRight().getColumn().getColumnName().equals(n2.getRight().getColumn().getColumnName()))
			return false;
	}
	//if right node of n1 is a constant value
	if(n1.getRight().getNodeType().equals(Node.getValType())){
		if(!n2.getRight().getNodeType().equals(Node.getValType()))
			return  false;
		if(!n1.getRight().getStrConst().equals(n2.getRight().getStrConst()))
			return false;
	}
	//if right node of n1 is an aggregate expression
	if(n1.getRight().getNodeType().equals(Node.getAggrNodeType())){
		if(!n2.getRight().getNodeType().equals(Node.getAggrNodeType()))
			return false;
		AggregateFunction agg1 = n1.getRight().getAgg();
		AggregateFunction agg2 = n2.getRight().getAgg();
		if(!agg1.getFunc().equals(agg2.getFunc()))
			return false;
		if(!agg1.getAggExp().getColumn().getTable().getTableName().equals(agg2.getAggExp().getColumn().getTable().getTableName()))
			return false;
		
		if(!agg1.getAggExp().getTableNameNo().equals(agg2.getAggExp().getTableNameNo()))
			return false;
		if(!agg1.getAggExp().getColumn().getColumnName().equals(agg2.getAggExp().getColumn().getColumnName()))
			return false;
	}
	//check for equivalence of left nodes
	
	//if left node of n1 is a column reference
	if(n1.getLeft().getNodeType().equals(Node.getColRefType())){
		if(!n2.getLeft().getNodeType().equals(Node.getColRefType()))
			return  false;
		if(!n1.getLeft().getTable().getTableName().equals(n2.getLeft().getTable().getTableName()))
			return false;
		
		if(!n1.getLeft().getTableNameNo().equals(n2.getLeft().getTableNameNo()))
			return false;
		if(!n1.getLeft().getColumn().getColumnName().equals(n2.getLeft().getColumn().getColumnName()))
			return false;
	}
	//if left node of n1 is a constant value
	if(n1.getLeft().getNodeType().equals(Node.getValType())){
		if(!n2.getLeft().getNodeType().equals(Node.getValType()))
			return  false;
		if(!n1.getLeft().getStrConst().equals(n2.getLeft().getStrConst()))
			return false;
	}
	//if left node of n1 is a aggregate expression
	if(n1.getLeft().getNodeType().equals(Node.getAggrNodeType())){
		if(!n2.getLeft().getNodeType().equals(Node.getAggrNodeType()))
			return false;
		AggregateFunction agg1 = n1.getLeft().getAgg();
		AggregateFunction agg2 = n2.getLeft().getAgg();
		if(!agg1.getFunc().equals(agg2.getFunc()))
			return false;
		if(!agg1.getAggExp().getColumn().getTable().getTableName().equals(agg2.getAggExp().getColumn().getTable().getTableName()))
			return false;
		
		if(!agg1.getAggExp().getTableNameNo().equals(agg2.getAggExp().getTableNameNo()))
			return false;
		if(!agg1.getAggExp().getColumn().getColumnName().equals(agg2.getAggExp().getColumn().getColumnName()))
			return false;
	}
		
		return true;
	}
/**
 * This method checks whether the aggregateFunction SUM,COUNT,etc., matches with student query
 * 
 * @param master
 * @param slave
 * @return
 */
public static Boolean checkAggregateName(AggregateFunction master, AggregateFunction slave){
	if(master.getFunc() != null && slave.getFunc() == null){
		return false;
	}
	if(master.getFunc() != null && slave.getFunc() == null){
		return false;
	}
	if(!(master.getFunc().equalsIgnoreCase(slave.getFunc()))){
		return false;
	}
	return true;
}
//Added by bikash for vldb2016 demo. Need to test this further
public static float compareOrderBy(List<Node> instructorOrderBy, List<Node> studentOrderBy){
			
	int[][] distanceMetric=new int[instructorOrderBy.size()+1][studentOrderBy.size()+1];
	//distanceMetric[0][0]=0;
	
	//initialization of the first row and first column of the matrix required,
	// the following two for loops accomplishes this
	//added by mathew on 16 Sep 16
	for(int i=0;i<=instructorOrderBy.size();i++)
		distanceMetric[i][0]=i;
	
	for(int j=0;j<=studentOrderBy.size();j++)
		distanceMetric[0][j]=j;
	
	for(int i=0;i<instructorOrderBy.size();i++)
		for(int j=0;j<studentOrderBy.size();j++){
			Node ins=instructorOrderBy.get(i);
			Node s=studentOrderBy.get(j);
			if(ins.getColumn().getColumnName().equalsIgnoreCase(s.getColumn().getColumnName())&&
					ins.getColumn().getTableName().equalsIgnoreCase(s.getColumn().getTableName())){
				distanceMetric[i+1][j+1]=distanceMetric[i][j];
			} else{
				int replace = distanceMetric[i][j] + 1;
				int insert = distanceMetric[i][j + 1] + 1;
				int delete = distanceMetric[i + 1][j] + 1;
 
				int min = replace > insert ? insert : replace;
				min = delete > min ? min : delete;
				distanceMetric[i + 1][j + 1] = min;
			}
		}
	
	int distance=distanceMetric[instructorOrderBy.size()][studentOrderBy.size()];
	
	return (instructorOrderBy.size()+studentOrderBy.size()-1.5f*distance)/2;
}
public static float compareHavingClause(ArrayList<Node> master, ArrayList<Node> slave){
	float score = 0;
	for(Node n1 : slave){
		Boolean found = false;
		for(Node n2 : master){				
			if(checkHavingClauseEquality(n1, n2)){
				found = true;
				break;
			}
		}
		
		if(found){
			score++;
		}
		else{
			score=score-0.5f;
		}
	}
	
	return score;
	
	/*if(master.getNodeType().equals(Node.getBroNodeType()) && slave.getNodeType().equals(Node.getBroNodeType())){
		this.uniqueHavingClause++;
		if(checkHavingClauseEquality(master,slave)){
			score ++;
		}else{ 
			score--;
		}
	}
	else if(master.getNodeType().equals(Node.getAndNodeType()) && slave.getNodeType().equals(Node.getAndNodeType())){
		compareHavingClause(master.getLeft(),slave.getLeft());
		compareHavingClause(master.getRight(),slave.getRight());
		
	}
	return score;*/
}
	
public static float compareSelection(List<Node> master, List<Node> slave){
	float score = 0;
	for(Node n1 : slave){
		Boolean found = false;
		for(Node n2 : master){				
			if(checkSelectionEquality(n1, n2)){
				found = true;
				break;
			}
		}
		
		if(found){
			score++;
		}
		else{
			score=score-0.5f;
		}
	}
	
	return score;
}
public static float compareProjection(ArrayList<Node> master, ArrayList<Node> slave){
	float score = 0;
	for(Node n1 : slave){
		Boolean found = false;
		for(Node n2 : master){				
			if(checkProjectionEquality(n1, n2)){
				found = true;
				break;
			}
		}
		if(found){
			score++;
		}
		else{
			score=score-0.5f;
		}
	}		
	return score;
}
	
public static float compareAggregates(ArrayList<AggregateFunction> master, ArrayList<AggregateFunction> slave){
	float score = 0;
	for(AggregateFunction n1 : slave){
		Boolean found = false;
		for(AggregateFunction n2 : master){				
			//Aggregate Name should match and the column also should match
			if(checkAggregateName(n1,n2) && checkProjectionEquality(n1.getAggExp(), n2.getAggExp())){
				found = true;
				break;
			}
		}
		if(found){
			score++;
		}
		else{
			score=score-0.5f;
		}
	}		
	return score;
}
	
	
		private static Node checkTableOccurence(Node n,ArrayList<Node> nodeList) {
			Node newNode = null;
			String tableNameNumber = null; 
			String num = null;
			int numValue=0;
			String newTableName = null;
			if(n != null && nodeList.size() >0){
			//if n is binary node - like bro node or and node, get left and right and call the same method
			if(n.getType().equalsIgnoreCase(Node.getBroNodeType()) || n.getType().equalsIgnoreCase(Node.getAndNodeType())){
				//newNode = this.checkTableOccurence(n, nodeList);
				if(n.getLeft() != null){
					newNode = checkTableOccurence(n.getLeft(),nodeList);
					n.setLeft(newNode);
				}
				if(n.getRight() != null){
					newNode = checkTableOccurence(n.getRight(), nodeList);
					n.setRight(newNode);
				}
				 return n;
			}
				
			//if n is aggr node - chk node itself or get agg function and get the table name number
			if(n.getType().equalsIgnoreCase(Node.getAggrNodeType())){
				
				if(n.getTableNameNo() != null){
					tableNameNumber = n.getTableNameNo();
					num = tableNameNumber.substring(tableNameNumber.length()-1,tableNameNumber.length());
					if(isInteger(num)){
						numValue = (Integer.parseInt(num)+1);
						if(numValue == 1){
						newTableName = n.getTable().toString()+numValue;
						for(Node nn : nodeList){
							if(nn.toString().contains(newTableName)){
								//do nothing
							}
							else{
								n.setTableNameNo(n.getTable().toString());							
							}
						}
						}
					}
				}
				
				newNode = checkTableOccurence(n.getAgg().getAggExp(), nodeList);
				n.getAgg().setAggExp(newNode);
				return n;
				
			}
			//if n is column reference node, get the table name number
			//manipulate it to next number and search the list of nodes for the new table number
			
			if(n.getType().equalsIgnoreCase(Node.getColRefType())){
				tableNameNumber = n.getTableNameNo();
				num = tableNameNumber.substring(tableNameNumber.length()-1,tableNameNumber.length());
				if(isInteger(num)){
				numValue = (Integer.parseInt(num)+1);
				if(numValue == 1){
				newTableName = n.getTable().toString()+numValue;
				for(Node nn : nodeList){
					if(nn.toString().contains(newTableName)){
						return n;
					}
					else{
						try {
							newNode = n.clone();
						} catch (CloneNotSupportedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						newNode.setTableNameNo(n.getTable().toString());
						return newNode;
					}
				}
				}else{
					return n;
				}
			}else{
				return n;
			}
			}
			if(n.getType().equalsIgnoreCase(Node.getValType())){
				return n;
				
			}
			}
			   //if result is null - ie., not tablename exists with new number,  change the input node and set to tablename.column name and return the new node
			//if result matches any, then return the node as it is. No changes reqd.
			return newNode;
					
		}
		
		/* The following method converts returns the parameter as 
		 * it is if its value is greater than or equal to zero, and
		 * otherwise returns zero if the 
		 * input parameter has a negative  value
		 * 
		 */
		public static float normalizeNegativeValuesToZero(float d){
			if(d>=0)
				return d;
			else
				return 0;
		}
		
		
		/** @author mathew
		 * 
		 * 
		 * @param instructorData
		 * @param studentData
		 * @param level
		 * @return
		 * 
		 *  Compares query structure corresponding to the instructor and student when one of it is a set operator query, 
		 *  considers 3 alternate cases. case i) when both instructor and student structures are set operator queries,
		 *  case ii) when only instructor query is a set operator query, case iii) when only student query is a set operator 
		 *  query
		 *  
		 */
		
		public static MarkInfo  calculateScoreForSetOperatorQueries( QueryStructure instructorData, QueryStructure studentData,
				int level) {
			MarkInfo marks = new MarkInfo();
			ArrayList<QueryInfo> currentInfo = null;
			ArrayList<QueryInfo> maxInfo;
			float result=0;
			// case when instructor query is a set operator query
			if(instructorData.setOperator!=null&&!instructorData.setOperator.isEmpty()){
				// case when student query is also a set operator query
				if(studentData.setOperator!=null&&!studentData.setOperator.isEmpty()){
					float score = 0;
					currentInfo = new ArrayList<QueryInfo>();
					
					//case 1, compare left operand with left operand of the set operator and right operand with right
					
					MarkInfo e = calculateScore(instructorData.leftQuery,studentData.leftQuery, level);
					currentInfo.addAll(e.SubqueryData);
					score += e.Marks;
					
					e=calculateScore(instructorData.rightQuery,studentData.rightQuery, level);
					currentInfo.addAll(e.SubqueryData);
					score += e.Marks/2;
					
					result = score;
					maxInfo=currentInfo;
					
					//case 2, compare left operand of the instructor query with right operand of the student query  
					//and right operand of the instructor query  with left operand of the student query
					
					currentInfo = new ArrayList<QueryInfo>();
					score = 0;
					
					e = calculateScore(instructorData.leftQuery,studentData.rightQuery, level);
					currentInfo.addAll(e.SubqueryData);
					score += e.Marks;
					
					e=calculateScore(instructorData.rightQuery,studentData.leftQuery, level);
					currentInfo.addAll(e.SubqueryData);
					score += e.Marks/2;
					
					if(score > result){
						result = score;
						maxInfo=currentInfo;
					}
					
					if(!instructorData.setOperator.equalsIgnoreCase(studentData.setOperator))
						result-=result/3;
					
					marks.Marks=result;
					marks.SubqueryData=maxInfo;
				}
				else // student query is not a set operator query
				{
					
					//case 1, compare left operand of the instructor Query with the student query
					
					MarkInfo e = calculateScore(instructorData.leftQuery,studentData, level);
										
					result =  e.Marks;;
					maxInfo=e.SubqueryData;
					
					//case 2, compare right operand of the instructor Query with the student query
					
					e = calculateScore(instructorData.rightQuery,studentData, level);					
					
					if(e.Marks > result){
						result = e.Marks;
						maxInfo=e.SubqueryData;
					}
					
					marks.Marks=result/3;
					marks.SubqueryData=maxInfo;
				}
			}
			// instructor query is not a set operator query, where as student query is a set operator query
			else if(studentData.setOperator!=null&& !studentData.setOperator.isEmpty())
			{
				//case 1, compare  the instructor Query with left operand of the student query
				
				MarkInfo e = calculateScore(instructorData,studentData.leftQuery, level);
									
				result =  e.Marks;;
				maxInfo=e.SubqueryData;
				
				//case 2, compare right operand of the instructor Query with the student query
				
				e = calculateScore(instructorData,studentData.rightQuery, level);					
				
				if(e.Marks > result){
					result = e.Marks;
					maxInfo=e.SubqueryData;
				}
				
				marks.Marks=result/3;
				marks.SubqueryData=maxInfo;
			}
									
			
			return marks;
		}
		
		public static void initializeConfiguration(){
			Configuration = new PartialMarkerConfig();
			Configuration.Relation=1;
			Configuration.Predicate=1;
			Configuration.Projection=1;
			Configuration.Joins=1;
			Configuration.OuterQuery=2;
			Configuration.GroupBy=1;
			Configuration.HavingClause=1;
			Configuration.SubQConnective=1;
			Configuration.SetOperators=1;
			Configuration.Distinct=1;
			Configuration.Aggregates=1;
			Configuration.WhereSubQueries=1;
			Configuration.FromSubQueries=1;
			Configuration.OrderBy=1;
		}
		
		public static void setConfigurationValues(PartialMarkParameters params){
			Configuration = new PartialMarkerConfig();
			if(params != null){
				Configuration.Relation=params.getRelation();
				Configuration.Predicate=params.getPredicate();
				Configuration.Projection=params.getProjection();
				Configuration.Joins=params.getJoins();
				Configuration.WhereSubQueries=params.getWhereSubQueries();
				Configuration.FromSubQueries=params.getFromSubQueries();
				Configuration.OuterQuery=params.getOuterQuery();
				Configuration.GroupBy=params.getGroupBy();
				Configuration.HavingClause=params.getHavingClause();
				Configuration.SubQConnective=params.getSubQConnective();
				Configuration.Aggregates=params.getAggregates();
				Configuration.SetOperators=params.getSetOperators();
				Configuration.Distinct=params.getDistinct();
			}
			//Configuration.OrderBy=params.getOrderBy();
		}
		
		/* @author mathew
		 *   Compares query structure corresponding to the instructor and student. Depending on whether
		 *   the student/instructor query is a set operator query or not, calls the respective for 
		 *   matching set operator/plain select queries
		 *   
		 */
		public static MarkInfo calculateScore( QueryStructure instructorData, QueryStructure studentData,
				int level) {
			if((instructorData.setOperator!=null&&!instructorData.setOperator.isEmpty())||(studentData.setOperator!=null&&!studentData.setOperator.isEmpty()))
				return calculateScoreForSetOperatorQueries(instructorData, studentData, level);
			else
				return calculateScoreForPlainSelect(instructorData, studentData, level);
		}
	
	// Compares query structure corresponding to the instructor and student
	
	public static MarkInfo calculateScoreForPlainSelect( QueryStructure instructorData, QueryStructure studentData,
			int level) {
		// TODO Auto-generated method stub
		
		if(Configuration==null)
			PartialMarker.initializeConfiguration();

		
		int distinctWeightage = 0;
		MarkInfo marks = new MarkInfo();
		MarkInfo whereSubQuery = compareListOfQueries(instructorData.getWhereClauseSubqueries(), studentData.getWhereClauseSubqueries(), level + 1);
		
		MarkInfo fromSubQuery = compareListOfQueries(instructorData.getFromClauseSubqueries(), studentData.getFromClauseSubqueries(), level + 1);
		
		distinctWeightage = Configuration.Distinct;
		
		
		float totalWeightage = Configuration.Predicate + Configuration.Relation + Configuration.Projection + Configuration.Joins + Configuration.GroupBy + Configuration.HavingClause + Configuration.SubQConnective + Configuration.Aggregates + Configuration.SetOperators + distinctWeightage +Configuration.OrderBy;
		
		float predWeightage = (Configuration.Predicate * 100)/totalWeightage;
		float relationWeightage = (Configuration.Relation * 100)/totalWeightage;
		float projWeightage = (Configuration.Projection* 100)/totalWeightage;
		float joinWeightage = (Configuration.Joins * 100)/totalWeightage;
		float groupByWeightage = (Configuration.GroupBy * 100)/totalWeightage;
		float havingClauseWeightage = (Configuration.HavingClause * 100)/totalWeightage;
		float subQConnectiveWeightage = (Configuration.SubQConnective * 100)/totalWeightage;
		float aggregateWeightage = (Configuration.Aggregates * 100) / totalWeightage;
		float setOperatorWeightage = (Configuration.SetOperators * 100) / totalWeightage;
		float distinctOpWeightage = (distinctWeightage * 100) / totalWeightage;
		float orderWeightage=0;
		if(level==0)
			orderWeightage = (Configuration.OrderBy*100)/totalWeightage;
		
		
		float uniquePredicates = instructorData.getLstSelectionConditions().size();
		float uniqueRelations = instructorData.getLstRelationInstances().size();
		float uniqueProj = instructorData.getLstProjectedCols().size();
		float instructorJoin = getJoinScore(instructorData, instructorData);
		float uniqueGroupBy = instructorData.getLstGroupByNodes().size();
		float uniqueHavingClause = instructorData.getLstHavingConditions().size();
		float uniqueSubQConnective = instructorData.getLstSubQConnectives().size();
		float uniqueAggregates = instructorData.getLstAggregateList().size(); 
		float uniqueSetOperators = instructorData.getLstSetOpetators().size();
		float uniqueDistinct = 1;
		float orderByColumns = instructorData.getOrderByNodes().size();
		
		float perPredicate = uniquePredicates == 0 ? 0 : predWeightage/uniquePredicates;
		
		float perRelation = uniqueRelations == 0 ? 0 : relationWeightage/uniqueRelations;
		
		float perProjection = uniqueProj == 0 ? 0 : projWeightage/uniqueProj;
		
		float perJoin = instructorJoin == 0 ? 0 : joinWeightage/instructorJoin;
		
		float perGroupBy = uniqueGroupBy == 0 ? 0 : groupByWeightage/uniqueGroupBy;
		
		float perHavingClause = uniqueHavingClause == 0 ? 0 : havingClauseWeightage/uniqueHavingClause;
		
		float perSubQConnective = uniqueSubQConnective == 0 ? 0 : subQConnectiveWeightage/uniqueSubQConnective;
		
		float perAggregate = uniqueAggregates == 0 ? 0 : aggregateWeightage/ uniqueAggregates;
		
		float perSetOperator = uniqueSetOperators == 0 ? 0 : setOperatorWeightage / uniqueSetOperators;
		
		float perDistinctOperator = uniqueDistinct == 0? 0 : distinctOpWeightage / uniqueDistinct;
		
		float perOrderBy = orderByColumns == 0 ? 0 : orderWeightage/orderByColumns;
		
		float predicateScore = compareSelection(instructorData.getLstSelectionConditions(), studentData.getLstSelectionConditions());
		
		float predicateScoreTotal=(perPredicate==0&&predicateScore!=0)?-predWeightage/2:
			perPredicate*normalizeNegativeValuesToZero(predicateScore);
		
		float projectionScore = compareProjection(instructorData.getLstProjectedCols(), studentData.getLstProjectedCols());		
		projectionScore = instructorData.getIsDistinct() == studentData.getIsDistinct() ? projectionScore : projectionScore/2;
		float projectionScoreTotal=(perProjection==0 && projectionScore!=0)?-projWeightage/2:
			perProjection*normalizeNegativeValuesToZero(projectionScore);				
		
		float relationScore = compare(instructorData.getLstRelationInstances(), studentData.getLstRelationInstances());
		float relationScoreTotal=(perRelation==0 && relationScore!=0)?-relationWeightage/2:
			perRelation*normalizeNegativeValuesToZero(relationScore);
				
		float joinScore = getJoinScore(instructorData, studentData);
		float joinScoreTotal=(perJoin==0 && joinScore!=0)?-joinWeightage/2:
			perJoin*normalizeNegativeValuesToZero(joinScore);	
		
		float groupByScore = compareProjection(instructorData.getLstGroupByNodes(), studentData.getLstGroupByNodes());
		float groupByScoreTotal=(perGroupBy==0 && groupByScore!=0)?-groupByWeightage/2:
			perGroupBy*normalizeNegativeValuesToZero(groupByScore);
				
		float havingClauseScore = compareHavingClause(instructorData.getLstHavingConditions(), studentData.getLstHavingConditions());
		float havingClauseScoreTotal=(perHavingClause==0 && havingClauseScore!=0)?-havingClauseWeightage/2:
			perHavingClause*normalizeNegativeValuesToZero(havingClauseScore);
		
		float subQConnectiveScore = compare(instructorData.getLstSubQConnectives(),studentData.getLstSubQConnectives());
		float subQConnectiveScoreTotal=(perSubQConnective==0 && subQConnectiveScore!=0)?-subQConnectiveWeightage/2:
			perSubQConnective*normalizeNegativeValuesToZero(subQConnectiveScore);
				
		float aggregateScore = compareAggregates(instructorData.getLstAggregateList(), studentData.getLstAggregateList());
		float aggregateScoreTotal=(perAggregate==0 && aggregateScore!=0)?-aggregateWeightage/2:
			perAggregate*normalizeNegativeValuesToZero(aggregateScore);
		float setOperatorScore = compare(instructorData.getLstSetOpetators(),studentData.getLstSetOpetators());
		float setOperatorScoreTotal=(perSetOperator==0 && setOperatorScore!=0)?-setOperatorWeightage/2:
			perSetOperator*normalizeNegativeValuesToZero(setOperatorScore);
		
		float distinctOperatorScore = 0;
		
				if(instructorData.getIsDistinct() && studentData.getIsDistinct()){
					distinctOperatorScore++;
				}
				else if(!instructorData.getIsDistinct() && !studentData.getIsDistinct()){
					distinctOperatorScore++;
				}
				else{
					//Even if any one query doesnot has Distinct - there is a mismatch
					distinctOperatorScore=distinctOperatorScore-0.5f;
				}
		float distinctOperatorScoreTotal=(perDistinctOperator==0 && distinctOperatorScore!=0)?-distinctWeightage/2:
			perDistinctOperator*distinctOperatorScore;
		
		float orderByScore = compareOrderBy(instructorData.getLstOrderByNodes(),studentData.getLstOrderByNodes()); ///compute order by score
		
		float orderByOperatorScoreTotal=(perOrderBy==0 && orderByScore!=0)? -orderWeightage/2:perOrderBy*orderByScore;
		if(orderByOperatorScoreTotal<0)
			orderByOperatorScoreTotal=0;
		
		float student =  normalizeNegativeValuesToZero(predicateScoreTotal + relationScoreTotal + projectionScoreTotal 
				+ joinScoreTotal + groupByScoreTotal + havingClauseScoreTotal + subQConnectiveScoreTotal + 
			aggregateScoreTotal + setOperatorScoreTotal + distinctOperatorScoreTotal + orderByOperatorScoreTotal);
		
		float instructor = perPredicate * uniquePredicates + perRelation * uniqueRelations + perProjection * uniqueProj + perJoin * instructorJoin + 
				perGroupBy * uniqueGroupBy + perHavingClause * uniqueHavingClause + perSubQConnective * uniqueSubQConnective + perAggregate * uniqueAggregates + perSetOperator * uniqueSetOperators + perDistinctOperator * uniqueDistinct +perOrderBy;
		
		if(level==0){
			logger.info("                  |     instructor    |    student         ");
			logger.info("distinct   score  |     "+perDistinctOperator * uniqueDistinct+"    |    "+distinctOperatorScoreTotal);
			logger.info("projection score  |     "+perProjection * uniqueProj+"    |    "+projectionScoreTotal);
			logger.info("selection score   |     "+perPredicate*uniquePredicates+"    |    "+predicateScoreTotal);
			logger.info("relation score    |     "+perRelation * uniqueRelations+"    |    "+relationScoreTotal);
			logger.info("join score        |     "+perJoin * instructorJoin+"    |    "+joinScoreTotal);
			logger.info("group by score    |     "+perGroupBy * uniqueGroupBy+"    |    "+groupByScoreTotal);
			logger.info("having score      |     "+perHavingClause * uniqueHavingClause+"    |    "+havingClauseScoreTotal);
			logger.info("order by score    |     "+perOrderBy+"    |    "+orderByOperatorScoreTotal);
			logger.info("subq. conn. score |     "+perSubQConnective * uniqueSubQConnective+"    |    "+subQConnectiveScoreTotal);
			logger.info("aggregate score   |     "+perAggregate * uniqueAggregates+"    |    "+aggregateScoreTotal);
			logger.info("set oper. score   |     "+perSetOperator * uniqueSetOperators+"    |    "+setOperatorScoreTotal);
			logger.info("total score       |     "+instructor+"    |    "+student);
		}
		float score = student/instructor * maxMarks;

		marks.Marks = score;
		if(fromSubQuery!=null&&whereSubQuery!=null)
			marks.Marks = Configuration.OuterQuery * score + Configuration.FromSubQueries * fromSubQuery.Marks + Configuration.WhereSubQueries * whereSubQuery.Marks ;				
		logger.info("partial mark="+marks.Marks);
		return marks;
	}
		
	
	// Compares all permutations of the queries and allocates the maximum mark.
	public static MarkInfo compareListOfQueries( Vector<QueryStructure> master, Vector<QueryStructure> slave, int level){
		int result = 0;
		
		MarkInfo marks = new MarkInfo();
		ArrayList<QueryInfo> currentInfo = null;
		ArrayList<QueryInfo> maxInfo = new ArrayList<QueryInfo>();
				
		int masterCount = master.size();		
		int slaveCount = slave.size();
		
		ArrayList<ArrayList<Integer>> combinations = new ArrayList<ArrayList<Integer>>();
		if(masterCount < slaveCount){						
			generateCombinations(combinations, masterCount, slaveCount, new ArrayList<Integer>(), 0);
			
			result = 0;
			for(ArrayList<Integer> combination : combinations){
				int score = 0;
				currentInfo = new ArrayList<QueryInfo>();
				for(int i = 0; i < combination.size(); i++){					
					MarkInfo e = calculateScore(master.get(i), slave.get(combination.get(i)), level);
					currentInfo.addAll(e.SubqueryData);
					score += e.Marks;
				}
				
				if(score > result){
					result = score;
					maxInfo = currentInfo;
				}
			}
		} else {						
			generateCombinations(combinations, slaveCount, masterCount, new ArrayList<Integer>(), 0);
			
			result = 0;
			for(ArrayList<Integer> combination : combinations){
				int score = 0;
				currentInfo = new ArrayList<QueryInfo>();
				for(int i = 0; i < combination.size(); i++){
					MarkInfo e = calculateScore(master.get(combination.get(i)), slave.get(i), level);
					currentInfo.addAll(e.SubqueryData);
					score += e.Marks;
				}
				
				if(score > result){
					result = score;
					maxInfo = currentInfo;
				}
			}
		}
		
		logger.log(Level.INFO,combinations.toString());
		logger.log(Level.INFO,"size ="+combinations.size());
		
		marks.SubqueryData = maxInfo;
		marks.Marks = result/(Math.abs(masterCount - slaveCount) + 1); 
		return marks;
	}
	
	
	public static boolean isInteger( String input )
	{
	   try 
	   {
	      Integer.parseInt( input );
	      return true;
	   }
	   catch( Exception e)
	   {
	      return false;
	   }
	}
	
	// Generates all the combinations
	public static void generateCombinations(ArrayList<ArrayList<Integer>> combinations, int limit,  int total, ArrayList<Integer> temp, int index){
		if(temp.size() == limit){
			combinations.add(new ArrayList<Integer>(temp));
			temp = new ArrayList<Integer>();
			return;
		}
		
		for(int j = index; j < total; j++){
			temp.add(j);
			generateCombinations(combinations, limit, total, temp, index + 1);
			temp.remove(index);
		}
	}
	
	private void cleanup(){
		try {
			this.InstructorQuery.getData().closeConn();
			this.StudentQuery.getData().closeConn();
		}
		catch(Exception ex){
			logger.log(Level.SEVERE,ex.getMessage(), ex);
			//ex.printStackTrace();
		}
	}
}