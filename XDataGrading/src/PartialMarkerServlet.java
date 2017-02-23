

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import partialMarking.MarkInfo;
import partialMarking.PartialMarkParameters;
import util.MyConnection;

/**
 * Servlet implementation class UploadScore
 */
@WebServlet("/PartialMarkerServlet")
public class PartialMarkerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(PartialMarkerServlet.class.getName());
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PartialMarkerServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String userId = ""; String status = ""; 
			userId = request.getParameter("userId");
			int assignmentId = Integer.parseInt(request.getParameter("assignmentId"));
			int questionId = Integer.parseInt(request.getParameter("questionId"));
			MarkInfo markInfo = new MarkInfo();
			String course_id = (String) request.getSession(false).getAttribute("context_label");	
			 //conn = MyConnection.getExistingDatabaseConnection();
			try(Connection conn = MyConnection.getDatabaseConnection()){
				
				// Initiate partial marking
				try(PreparedStatement stmt = conn.prepareStatement("select * from xdata_instructor_query where assignment_id = ? and question_id = ?")){ 
					stmt.setInt(1,assignmentId);
					stmt.setInt(2,questionId);
					try(ResultSet rs = stmt.executeQuery()){		
						while(rs.next()){	
							int queryId = rs.getInt("query_id");
							try{
								partialMarking.PartialMarker marker = new partialMarking.PartialMarker(assignmentId, questionId, queryId,course_id,userId);
								partialMarking.PartialMarker.Configuration.Predicate = Integer.parseInt(request.getParameter("predicate"));
								partialMarking.PartialMarker.Configuration.Projection = Integer.parseInt(request.getParameter("projection"));
								partialMarking.PartialMarker.Configuration.Relation = Integer.parseInt(request.getParameter("relation"));
								partialMarking.PartialMarker.Configuration.GroupBy = Integer.parseInt(request.getParameter("groupBy"));
								partialMarking.PartialMarker.Configuration.Joins = Integer.parseInt(request.getParameter("joins"));
								partialMarking.PartialMarker.Configuration.HavingClause = Integer.parseInt(request.getParameter("having"));
								partialMarking.PartialMarker.Configuration.SubQConnective = Integer.parseInt(request.getParameter("subQConnective"));
								partialMarking.PartialMarker.Configuration.OuterQuery = Integer.parseInt(request.getParameter("outer"));
								partialMarking.PartialMarker.Configuration.FromSubQueries = Integer.parseInt(request.getParameter("from"));
								partialMarking.PartialMarker.Configuration.WhereSubQueries = Integer.parseInt(request.getParameter("where"));
								partialMarking.PartialMarker.Configuration.Aggregates = Integer.parseInt(request.getParameter("aggregates"));
								partialMarking.PartialMarker.Configuration.SetOperators = Integer.parseInt(request.getParameter("setoperators"));
								partialMarking.PartialMarker.Configuration.Distinct = Integer.parseInt(request.getParameter("distinct"));
								MarkInfo result = marker.getMarksForQueryStructures();
								if(result.Marks > markInfo.Marks)
									markInfo = result;
							}
							catch(Exception ex){
								logger.log(Level.SEVERE,ex.getMessage(),ex);
								status = ex.getMessage();
							}		
						}		
						
						if(status.isEmpty()){
							
							try{
							Gson gson = new Gson();
							String info = gson.toJson(markInfo);
							//try(PreparedStatement stmt1 = conn.prepareStatement("update score set result = ?, markinfo = ? where rollnum = ? and assignment_id = ? and question_id = ?")){
							try(PreparedStatement stmt1 = conn.prepareStatement("update xdata_student_queries set score = ?, markinfo = ? where rollnum = ? and assignment_id = ? and question_id = ?")){
								stmt1.setFloat(1, markInfo.Marks);
								stmt1.setString(2, info);
								stmt1.setString(3, userId);
								stmt1.setInt(4, assignmentId);
								stmt1.setInt(5, questionId);				
								stmt1.executeUpdate();
							}//try block for statement stmt ends
							} catch (SQLException sqlEx){
								logger.log(Level.SEVERE,sqlEx.getMessage(),sqlEx);
								status = sqlEx.getMessage();
							}
						}
						}//try block for resultset rs ends
					}//try block for statement ends
				PrintWriter out = response.getWriter();
				out.write(status);
				
			} catch (Exception e) {
				logger.log(Level.SEVERE,e.getMessage(),e);
			throw new ServletException(e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		
		int queryId = 0;
		int assignmentId = 0;
		int questionId = 0;
		
		if(request.getParameter("queryId") != null && !request.getParameter("queryId").isEmpty()){ queryId = Integer.parseInt(request.getParameter("queryId")); };
		if(request.getParameter("assignmentId") != null && !request.getParameter("assignmentId").isEmpty()){ assignmentId = Integer.parseInt(request.getParameter("assignmentId")); };
		if(request.getParameter("questionId") != null && !request.getParameter("questionId").isEmpty()){ questionId = Integer.parseInt(request.getParameter("questionId")); };
		String requestFrom = request.getParameter("reqFromPage");
		
		PartialMarkParameters markInfo = new PartialMarkParameters();
		try{
			markInfo.QueryId = queryId;
			markInfo.AssignmentId = assignmentId;
			markInfo.QuestionId = questionId;
			markInfo.setPredicate(Integer.parseInt(request.getParameter("predicate")));
			markInfo.setProjection(Integer.parseInt(request.getParameter("projection")));
			markInfo.setRelation(Integer.parseInt(request.getParameter("relation")));
			markInfo.setGroupBy(Integer.parseInt(request.getParameter("groupBy")));
			markInfo.setJoins(Integer.parseInt(request.getParameter("joins")));
			markInfo.setHavingClause(Integer.parseInt(request.getParameter("having")));
			markInfo.setSubQConnective(Integer.parseInt(request.getParameter("subQConnective")));
			markInfo.setOuterQuery(Integer.parseInt(request.getParameter("outer")));
			markInfo.setFromSubQueries(Integer.parseInt(request.getParameter("from")));
			markInfo.setWhereSubQueries(Integer.parseInt(request.getParameter("where")));
			markInfo.setAggregates(Integer.parseInt(request.getParameter("aggregates")));
			markInfo.setSetOperators(Integer.parseInt(request.getParameter("setoperators")));
			markInfo.setDistinct(Integer.parseInt(request.getParameter("distinct")));
			
			Gson gson = new Gson();
			String info = gson.toJson(markInfo);
			if((requestFrom != null && !requestFrom.isEmpty() && requestFrom.trim().equalsIgnoreCase("demo"))
					//|| (queryId == 0 && assignmentId == 0 && questionId ==0)
					){
				session.setAttribute("PartialMarkDemo"+questionId,markInfo);
			}else{
				session.setAttribute(assignmentId+"_"+questionId+"_"+queryId,markInfo);
			}
			
			 
		}//try block ends
		catch (Exception e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw new ServletException(e);
	}
	}
}
