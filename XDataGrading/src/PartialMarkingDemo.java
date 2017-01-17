

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import parsing.Node;
import partialMarking.QueryData;
import partialMarking.TestPartialMarking;
import testDataGen.PopulateTestDataGrading;

/**
 * Servlet implementation class PartialMarkingDemo
 */
@WebServlet("/PartialMarkingDemo")
public class PartialMarkingDemo extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PartialMarkingDemo() {
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
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		
		HttpSession session = request.getSession(false);
		String loginUsr = "";
		loginUsr = (String) session.getAttribute("LOGIN_USER");
		String instructorQuery = request.getParameter("instructorQuery");
		response.setContentType("text/html;charset=UTF-8");
		String studentQuery = request.getParameter("studentQuery");
		String isProcessCanonicalize = request.getParameter("canonicalize");
		String instructorQueries[]=instructorQuery.split("#@###@#");

		float marks=0.0f;		
		float marks1=0.0f;	
		String errorMessage;
		String rootDir;
		String hiddenString;
		String textareaCount;
		String hiddenSting;
		
		QueryData bestInstructorQueryData=null;
		QueryData bestInstructorQueryData1=null;
		String bestInstructorQueryString="";
		String bestInstructorQueryString1=""; 
		TestPartialMarking testObj=new TestPartialMarking();
		TestPartialMarking testObj1=new TestPartialMarking();
		Connection graderConn=null;
		PopulateTestDataGrading p = new PopulateTestDataGrading();
		Exception caughtException=null;

		int assignId=9;  //Hard code some existing assignment ID here and in TestPartialMarking.java - process and processcanonicalize methods
				
				graderConn = new util.DatabaseConnection().getGraderConnection(assignId);
				
				try{ 
					p.deleteAllTempTablesFromTestUser(graderConn);
					p.createTempTables(graderConn, assignId, 1);
					//run the studentQuery in the database and check if it is syntactically correct						
					Statement graderStatement = graderConn.createStatement();
					graderStatement.execute(studentQuery);
				}catch(Exception e){
					try {
						if(graderConn != null && ! graderConn.isClosed())
							graderConn.close();
					} catch (SQLException e1) {
						
						e1.printStackTrace();
					}
					e.printStackTrace(); 
					caughtException = e;
					response. sendError(88,e.getMessage());

				}
				try{
					for(String instQuery:instructorQueries){
						//run the query in the database and check if it is syntactically correct						
						Statement graderStatement = graderConn.createStatement();
						graderStatement.execute(instQuery);
					}
				}catch(Exception e){
					try {
						if(graderConn != null && ! graderConn.isClosed())
							graderConn.close();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
					caughtException = e;
					response.sendError(89,e.getMessage());
					
					//throw new ServletException();
				}
		
				if(caughtException== null ){	
		try{
				
					
					testObj.StudentQuery=testObj.processCanonicalize(testObj.StudentQuery,1, studentQuery);
					testObj1.StudentQuery=testObj1.process(testObj1.StudentQuery,1, studentQuery);
				  
					for(String instQuery:instructorQueries){
						
								testObj.InstructorQuery=testObj.processCanonicalize(testObj.InstructorQuery,1, instQuery);		
								
								Float studMarks=testObj.calculateScore(false, testObj.InstructorQuery.OuterQuery, testObj.StudentQuery.OuterQuery, 0).Marks;
								Float instMarks=testObj.calculateScore(false, testObj.InstructorQuery.OuterQuery, testObj.InstructorQuery.OuterQuery, 0).Marks;
								
								Float newMarks=studMarks*100/instMarks;
			
						if(newMarks> marks){
							marks=newMarks;
							bestInstructorQueryData=testObj.InstructorQuery.OuterQuery;
							bestInstructorQueryString=instQuery;
						}
					}
					for(String instQuery:instructorQueries){
						
						testObj1.InstructorQuery=testObj1.process(testObj1.InstructorQuery,1, instQuery);
						
						Float studMarks1=testObj1.calculateScore(false, testObj1.InstructorQuery.OuterQuery, testObj1.StudentQuery.OuterQuery, 0).Marks;
						Float instMarks1=testObj1.calculateScore(false, testObj1.InstructorQuery.OuterQuery, testObj1.InstructorQuery.OuterQuery, 0).Marks;
						
						Float newMarks1=studMarks1*100/instMarks1;
						
						if(newMarks1> marks1){
							marks1=newMarks1;
							bestInstructorQueryData1=testObj1.InstructorQuery.OuterQuery;
							bestInstructorQueryString1=instQuery;
						}
					
				}
		}
	catch(Exception e){
		errorMessage=e.getMessage();
		e.printStackTrace();
		throw new ServletException();
		
	}
					QueryData instrData = bestInstructorQueryData;
					QueryData studentData = testObj.StudentQuery.OuterQuery;
					
					QueryData instrData1 = bestInstructorQueryData1;
					QueryData studentData1 = testObj1.StudentQuery.OuterQuery;
					
					//Upload the details of with canonicalization in first div
					String output="";
					output += "<ul class=\"nav nav-tabs\"><li class=\"active\"><a href=\"#tab1\">Canonicalized Partial Marks: "+marks+"</a></li><li><a href=\"#tab2\">Non-Canonicalized Marks:"+marks1+"</a></li></ul>";
					
					output += "<section id=\"tab1\" class=\"tab-content active\"><div  style='background-color:#FFF'>";
					output +="<br/>";
					output+="<table class='queryTable' width='70%' cellpadding='3' cellspacing='1'><tr>"+
								"<th width='20%'>&nbsp;</th><th width='20%' align='center'>Student</th><th width='20%' align='center'>Instructor</th></tr>";
					
					if( (instrData != null && instrData.getRelations().size() > 0)
		  			|| (studentData != null && studentData.getRelations().size() > 0)){
						output += "<tr><td class='emph''>Relations</td>" +
								"<td width=\"20%\">"+listToString(studentData.getRelations(),instrData.getRelations())+"</td>"+
								"<td width=\"20%\">"+listToString(instrData.getRelations(), studentData.getRelations())+"</td></tr>";
		  		
					}
					
					if( (instrData != null && instrData.getProjectionList().size() > 0)
				  			|| (studentData != null && studentData.getProjectionList().size() > 0)){
								output += "<tr><td class='emph''>Projections</td>" +
										"<td width=\"20%\">"+listToString(studentData.getProjectionList(),instrData.getProjectionList())+"</td>"+
										"<td width=\"20%\">"+listToString(instrData.getProjectionList(), studentData.getProjectionList())+"</td></tr>";
				  		
							}
					
					if( (instrData != null && instrData.hasDistinct)
				  			|| (studentData != null && studentData.hasDistinct)){
						output += "<tr><td class='emph''>Distinct</td>" ;
						
						int instDistinct = 0;
						int studDistinct = 0;
						if(instrData.hasDistinct){
							instDistinct =1;
						}if(studentData.hasDistinct){
							studDistinct = 1;
						}
						if(studentData.hasDistinct && !instrData.hasDistinct){
							
							output += "<td width=\"20%\" align='center' class=\"number\" style=\"color: red;\">"+studDistinct+"</td>";
						}else{
							output += "<td width=\"20%\" align='center' class='number'>"+studDistinct+"</td>";
						}
							

						if((instrData.hasDistinct && !studentData.hasDistinct)){
							output += "<td width=\"20%\" align='center' class=\"number\" style=\"color: red;\">"+instDistinct+"</td></tr>";
							
						}else{
							output += "<td width=\"20%\" align='center' class='number'>"+instDistinct+"</td></tr>";
						}
						}
					
					
					if( (instrData != null && instrData.GroupByNodes.size() > 0)
				  			|| (studentData != null && studentData.GroupByNodes.size() > 0)){
								output += "<tr><td class='emph''>Group By</td>" +
										"<td width=\"20%\">"+listToString(studentData.GroupByNodes,instrData.GroupByNodes)+"</td>"+
										"<td width=\"20%\">"+listToString(instrData.GroupByNodes, studentData.GroupByNodes)+"</td></tr>";
				  		
						}
						
						if( (instrData != null && instrData.orderByNodes.size() > 0)
				  			|| (studentData != null && studentData.orderByNodes.size() > 0)){
						
							output += "<tr><td class='emph''>Order By</td>" +
										"<td width=\"20%\">"+listToString(studentData.orderByNodes,instrData.orderByNodes)+"</td>"+
										"<td width=\"20%\">"+listToString(instrData.orderByNodes, studentData.orderByNodes)+"</td></tr>";
						}
						
						if( (instrData != null && instrData.getHavingClause().size() > 0)
				  			|| (studentData != null && studentData.getHavingClause().size() > 0)){
							output += "<tr><td class='emph''>Having Clause</td>" +
									  "<td width=\"20%\">"+listToString(studentData.getHavingClause(),instrData.getHavingClause())+"</td>"+
									  "<td width=\"20%\">"+listToString(instrData.getHavingClause(), studentData.getHavingClause())+"</td></tr>";
		
				}
					if( (instrData != null && instrData.getSubQConnectives().size() > 0)
				  			|| (studentData != null && studentData.getSubQConnectives().size() > 0)){
				  			
				  			output += "<tr><td class='emph''>SubQuery Connectives</td>" +
										"<td width=\"20%\">"+listToString(studentData.getSubQConnectives(),instrData.getSubQConnectives())+"</td>"+
										"<td width=\"20%\">"+listToString(instrData.getSubQConnectives(), studentData.getSubQConnectives())+"</td></tr>";
				  			
				  			}
				
				if( (instrData != null && instrData.getSetOpetators().size() > 0)
				  			|| (studentData != null && studentData.getSetOpetators().size() > 0)){
				  			
				  			output += "<tr><td class='emph''>Set Operators</td>" +
										"<td width=\"20%\">"+listToString(studentData.getSetOpetators(),instrData.getSetOpetators())+"</td>"+
										"<td width=\"20%\">"+listToString(instrData.getSetOpetators(), studentData.getSetOpetators())+"</td></tr>";
				  			
				  			
				  			}
				if( (instrData != null && instrData.getSelectionConditions().size() > 0)
				  			|| (studentData != null && studentData.getSelectionConditions().size() > 0)){
				  			
				  				output += "<tr><td class='emph''>Selection Conditions</td>" +
										"<td width=\"20%\">"+listToString(studentData.getSelectionConditions(),instrData.getSelectionConditions())+"</td>"+
										"<td width=\"20%\">"+listToString(instrData.getSelectionConditions(), studentData.getSelectionConditions())+"</td></tr>";
				  		
				  		
				  			}
				
		if( (instrData != null && instrData.getJoinTables().size() > 0)
				  			|| (studentData != null && studentData.getJoinTables().size() > 0)){
				  			
				  				output += "<tr><td class='emph''>Join Tables</td>" +
										"<td width=\"20%\">"+listToString(studentData.getJoinTables(),instrData.getJoinTables())+"</td>"+
										"<td width=\"20%\">"+listToString(instrData.getJoinTables(), studentData.getJoinTables())+"</td></tr>";
				  		
				  		
				  			}
				  			
		if( (instrData != null && instrData.getJoinConditions().size() > 0)
				  			|| (studentData != null && studentData.getJoinConditions().size() > 0)){
				
				output += "<tr><td class='emph'>Join Conditions </td>" +
										"<td width=\"20%\">"+listToString(studentData.getJoinConditions(),instrData.getJoinConditions())+"</td>"+
										"<td width=\"20%\">"+listToString(instrData.getJoinConditions(), studentData.getJoinConditions())+"</td></tr>";
				  		
				  		}
		output += "</table></section></div>";
	
		//Upload uncanonicalized results in SECOND div
		output += "<section id=\"tab2\" class=\"tab-content hide\"><div style='background-color:#FFF'>";
		output +="<br/>";
		output+="<table class='queryTable1' width='70%' cellpadding='3' cellspacing='1'><tr>"+
					"<th width='20%'>&nbsp;</th><th width='20%' align='center'>Student</th><th width='20%' align='center'>Instructor</th></tr>";
		
		if( (instrData1 != null && instrData1.getRelations().size() > 0)
			|| (studentData1 != null && studentData1.getRelations().size() > 0)){
			output += "<tr><td class='emph''>Relations</td>" +
					"<td width=\"20%\">"+listToString(studentData1.getRelations(),instrData1.getRelations())+"</td>"+
					"<td width=\"20%\">"+listToString(instrData1.getRelations(), studentData1.getRelations())+"</td></tr>";
		
		}
		
		if( (instrData1 != null && instrData1.getProjectionList().size() > 0)
	  			|| (studentData1 != null && studentData1.getProjectionList().size() > 0)){
					output += "<tr><td class='emph''>Projections</td>" +
							"<td width=\"20%\">"+listToString(studentData1.getProjectionList(),instrData1.getProjectionList())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.getProjectionList(), studentData1.getProjectionList())+"</td></tr>";
	  		
				}
		
		if( (instrData1 != null && instrData1.hasDistinct)
	  			|| (studentData1 != null && studentData1.hasDistinct)){
			output += "<tr><td class='emph'>Distinct</td>" ;
			
			int instDistinct = 0;
			int studDistinct = 0;
			if(instrData1.hasDistinct){
				instDistinct =1;
			}if(studentData1.hasDistinct){
				studDistinct = 1;
			}
			if(studentData1.hasDistinct && !instrData1.hasDistinct){
				
				output += "<td width=\"20%\" align='center' class=\"number\" style=\"color: red;\">"+studDistinct+"</td>";
			}else{
				output += "<td width=\"20%\" align='center' class='number'>"+studDistinct+"</td>";
			}
				

			if((instrData1.hasDistinct && !studentData1.hasDistinct)){
				output += "<td width=\"20%\" align='center' class=\"number\" style=\"color: red;\">"+instDistinct+"</td></tr>";
				
			}else{
				output += "<td width=\"20%\" align='center' class='number'>"+instDistinct+"</td></tr>";
			}
			}
		
		
		if( (instrData1 != null && instrData1.GroupByNodes.size() > 0)
	  			|| (studentData1 != null && studentData1.GroupByNodes.size() > 0)){
					output += "<tr><td class='emph''>Group By</td>" +
							"<td width=\"20%\">"+listToString(studentData1.GroupByNodes,instrData1.GroupByNodes)+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.GroupByNodes, studentData1.GroupByNodes)+"</td></tr>";
	  		
			} 
			
			if( (instrData1 != null && instrData1.orderByNodes.size() > 0)
	  			|| (studentData1 != null && studentData1.orderByNodes.size() > 0)){
			
				output += "<tr><td class='emph''>Order By</td>" +
							"<td width=\"20%\">"+listToString(studentData1.orderByNodes,instrData1.orderByNodes)+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.orderByNodes, studentData1.orderByNodes)+"</td></tr>";
			}
			
			if( (instrData1 != null && instrData1.getHavingClause().size() > 0)
	  			|| (studentData1 != null && studentData1.getHavingClause().size() > 0)){
				output += "<tr><td class='emph''>Having Clause</td>" +
						  "<td width=\"20%\">"+listToString(studentData1.getHavingClause(),instrData1.getHavingClause())+"</td>"+
						  "<td width=\"20%\">"+listToString(instrData1.getHavingClause(), studentData1.getHavingClause())+"</td></tr>";

	}
		if( (instrData1 != null && instrData1.getSubQConnectives().size() > 0)
	  			|| (studentData1 != null && studentData1.getSubQConnectives().size() > 0)){
	  			
	  			output += "<tr><td class='emph''>SubQuery Connectives</td>" +
							"<td width=\"20%\">"+listToString(studentData1.getSubQConnectives(),instrData1.getSubQConnectives())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.getSubQConnectives(), studentData1.getSubQConnectives())+"</td></tr>";
	  			
	  			}
	
	if( (instrData1 != null && instrData1.getSetOpetators().size() > 0)
	  			|| (studentData1 != null && studentData1.getSetOpetators().size() > 0)){
	  			
	  			output += "<tr><td class='emph''>Set Operators</td>" +
							"<td width=\"20%\">"+listToString(studentData1.getSetOpetators(),instrData1.getSetOpetators())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.getSetOpetators(), studentData1.getSetOpetators())+"</td></tr>";
	  			
	  			
	  			}
	if( (instrData1 != null && instrData1.getSelectionConditions().size() > 0)
	  			|| (studentData1 != null && studentData1.getSelectionConditions().size() > 0)){
	  			
	  				output += "<tr><td class='emph''>Selection Conditions</td>" +
							"<td width=\"20%\">"+listToString(studentData1.getSelectionConditions(),instrData1.getSelectionConditions())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.getSelectionConditions(), studentData1.getSelectionConditions())+"</td></tr>";
	  		
	  		
	  			}
	
if( (instrData1 != null && instrData1.getJoinTables().size() > 0)
	  			|| (studentData1 != null && studentData1.getJoinTables().size() > 0)){
	  			
	  				output += "<tr><td class='emph''>Join Tables</td>" +
							"<td width=\"20%\">"+listToString(studentData1.getJoinTables(),instrData1.getJoinTables())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.getJoinTables(), studentData1.getJoinTables())+"</td></tr>";
	  		
	  		
	  			}
	  			
if( (instrData1 != null && instrData1.getJoinConditions().size() > 0)
	  			|| (studentData1 != null && studentData1.getJoinConditions().size() > 0)){
	
	output += "<tr><td class='emph'>Join Conditions</td>" +
							"<td width=\"20%\">"+listToString(studentData1.getJoinConditions(),instrData1.getJoinConditions())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.getJoinConditions(), studentData1.getJoinConditions())+"</td></tr>";
	  		
	  		}
	output += "</table></section></div>";
	response.getWriter().write(output);	
				}
					
	}
		/**
		 * This function compares the items in the list and returns required html element
		 * if they are same (with default color  black)/ different(with color red)
		 * 
		 * @param list1
		 * @param list2
		 * @return
		 */
		public String listToString(List<String> list1, List<String> list2){
			String ret = "<ul>";
			for(String s:list1){
				if(list2.contains(s)){
					ret += "<li>" + s + "</li>";
				}else{
					ret += "<li style='color:red;'>" + s + "</li>";
				}		 
			}
			ret += "</ul>";
			return ret;
		}
		
		/**
		 * Same as previous function, but compares array list of Nodes
		 * @param list1
		 * @param list2
		 * @return
		 */
		public String listToString(ArrayList <Node> list1, ArrayList<Node> list2){
			String ret = "<ul>";
			for(Node s:list1){
				if(list2.toString().contains(s.toString())){
					ret += "<li>" + s + "</li>";
				}else{
					ret += "<li style='color:red;'>" + s + "</li>";
				}		 
			}
			ret += "</ul>";
			return ret;
		}

		/**
		 * This method is used to round of marks
		 * @param marks
		 * @return
		 */
	    public float roundToDecimal(float marks){
		return BigDecimal.valueOf(marks).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
	}
	    
	    

}
