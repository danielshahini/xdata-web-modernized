
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import parsing.Node;
import parsing.QueryStructure;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import partialMarking.PartialMarkParameters;
import partialMarking.TestPartialMarking;
import testDataGen.PopulateTestDataGrading;

import database.DatabaseConnection;
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
		
		QueryStructure bestInstructorQueryData=null;
		QueryStructure bestInstructorQueryData1=null;
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
				
					int i=0;
					testObj.StudentQuery=testObj.processCanonicalize(testObj.StudentQuery,1, studentQuery);
					testObj1.StudentQuery=testObj1.process(testObj1.StudentQuery,1, studentQuery);
				  
					for(String instQuery:instructorQueries){
								i++;
								PartialMarkParameters params = (PartialMarkParameters)session.getAttribute("PartialMarkDemo"+i);
								
								partialMarking.PartialMarker.setConfigurationValues(params);
								
								testObj.InstructorQuery=testObj.processCanonicalize(testObj.InstructorQuery,1, instQuery);		
								
								Float studMarks=partialMarking.PartialMarker.calculateScore(testObj.InstructorQuery.getQueryStructure(), testObj.StudentQuery.getQueryStructure(), 0).Marks;
								Float instMarks=partialMarking.PartialMarker.calculateScore(testObj.InstructorQuery.getQueryStructure(), testObj.InstructorQuery.getQueryStructure(), 0).Marks;
								
								Float newMarks=studMarks*100/instMarks;
			
						if(newMarks> marks){
							marks=newMarks;
							bestInstructorQueryData=testObj.InstructorQuery.getQueryStructure();
							bestInstructorQueryString=instQuery;
						}
					}
					i = 0;
					for(String instQuery:instructorQueries){
						i++;
						PartialMarkParameters params = (PartialMarkParameters)session.getAttribute("PartialMarkDemo"+i);
						partialMarking.PartialMarker.setConfigurationValues(params);
						testObj1.InstructorQuery=testObj1.process(testObj1.InstructorQuery,1, instQuery);
						
						Float studMarks1=partialMarking.PartialMarker.calculateScore(testObj1.InstructorQuery.getQueryStructure(), testObj1.StudentQuery.getQueryStructure(), 0).Marks;
						Float instMarks1=partialMarking.PartialMarker.calculateScore(testObj1.InstructorQuery.getQueryStructure(), testObj1.InstructorQuery.getQueryStructure(), 0).Marks;
						
						Float newMarks1=studMarks1*100/instMarks1;
						
						if(newMarks1> marks1){
							marks1=newMarks1;
							bestInstructorQueryData1=testObj1.InstructorQuery.getQueryStructure();
							bestInstructorQueryString1=instQuery;
						}
					
				}
		}
	catch(Exception e){
		errorMessage=e.getMessage();
		e.printStackTrace();
		throw new ServletException();
		
	}
					QueryStructure instrData = bestInstructorQueryData;
					QueryStructure studentData = testObj.StudentQuery.getQueryStructure();
					
					QueryStructure instrData1 = bestInstructorQueryData1;
					QueryStructure studentData1 = testObj1.StudentQuery.getQueryStructure();
					
					//Upload the details of with canonicalization in first div
					String output="";
					output += "<ul class=\"nav nav-tabs\"><li class=\"active\"><a href=\"#tab1\">Canonicalized Partial Marks: "+marks+"</a></li><li><a href=\"#tab2\">Non-Canonicalized Marks:"+marks1+"</a></li></ul>";
					
					output += "<section id=\"tab1\" class=\"tab-content active\"><div  style='background-color:#FFF'>";
					output +="<br/>";
					String out1 ="";
					
					//Get TAB1 WITHOUT CANONICALIZATION DISPLAY:
					output+= getCanonicalizedDisplay(instrData, studentData, out1);
					
					//Display FromClauseSubQ Structure
					
					String op = "";
					QueryStructure instrDataFromSubQ= new QueryStructure(instrData.getTableMap());
					QueryStructure studentDataFromSubQ =new QueryStructure(studentData.getTableMap());
					
					if( (instrData.getFromClauseSubqueries() != null && !instrData.getFromClauseSubqueries().isEmpty()) 
							|| (studentData.getFromClauseSubqueries() != null && !studentData.getFromClauseSubqueries().isEmpty())){
						op += "<br/><div style ='width:'80%; align:left; padding-left:20px;'><label><h3>From Clause SubQuery: </h3></label>";
						if(instrData.getFromClauseSubqueries() != null && !instrData.getFromClauseSubqueries().isEmpty()){
							instrDataFromSubQ = instrData.getFromClauseSubqueries().get(0);
						}
						if(studentData.getFromClauseSubqueries() != null && !studentData.getFromClauseSubqueries().isEmpty()){
							studentDataFromSubQ = studentData.getFromClauseSubqueries().get(0);
						}
						
						output+= getCanonicalizedDisplay(instrDataFromSubQ, studentDataFromSubQ, op)+"</div>";
					}
					
					//Display WHERE CLAUSE SUBQ structure
					
					String op1 = "";
					QueryStructure instrDataWhereSubQ= new QueryStructure(instrData.getTableMap());
					QueryStructure studentDataWhereSubQ =new QueryStructure(studentData.getTableMap());
					
					if( (instrData.getWhereClauseSubqueries() != null && !instrData.getWhereClauseSubqueries().isEmpty()) 
							|| (studentData.getWhereClauseSubqueries() != null && !studentData.getWhereClauseSubqueries().isEmpty())){
						
						op1 += "<br/><div style ='width:'80%; align:left; padding-left:20px;'><label><h3>Where Clause SubQuery: </h3></label>";
						if(instrData.getWhereClauseSubqueries() != null && !instrData.getWhereClauseSubqueries().isEmpty()){
							instrDataWhereSubQ = instrData.getWhereClauseSubqueries().get(0);
						}
						if(studentData.getWhereClauseSubqueries() != null && !studentData.getWhereClauseSubqueries().isEmpty()){
							studentDataWhereSubQ = studentData.getWhereClauseSubqueries().get(0);
						}
						 
						output+= getCanonicalizedDisplay(instrDataWhereSubQ, studentDataWhereSubQ, op1)+"</div>";
					
					}
					
					
		output += "</section></div><br/>";
	
		//Upload uncanonicalized results in SECOND div
		output += "<section id=\"tab2\" class=\"tab-content hide\"><div style='background-color:#FFF'>";
		output +="<br/>";
		
		
		//Get TAB2 WITHOUT CANONICALIZATION DISPLAY:
		String out2 = "";
		output+= getNonCanonicalizedDisplay(instrData1, studentData1, out2);
		
		//Display FromClauseSubQ Structure
		
		String op2 = "";
		QueryStructure instrDataFromSubQ1= new QueryStructure(instrData1.getTableMap());
		QueryStructure studentDataFromSubQ1 =new QueryStructure(studentData1.getTableMap());
		
		if( (instrData1.getFromClauseSubqueries() != null && !instrData1.getFromClauseSubqueries().isEmpty()) 
				|| (studentData1.getFromClauseSubqueries() != null && !studentData1.getFromClauseSubqueries().isEmpty())){
			op2 += "<br/><div style ='width:'80%; align:left; padding-left:20px;'><label><h3>From Clause SubQuery: </h3></label>";
			if(instrData1.getFromClauseSubqueries() != null && !instrData1.getFromClauseSubqueries().isEmpty()){
				instrDataFromSubQ1 = instrData1.getFromClauseSubqueries().get(0);
			}
			if(studentData1.getFromClauseSubqueries() != null && !studentData1.getFromClauseSubqueries().isEmpty()){
				studentDataFromSubQ1 = studentData1.getFromClauseSubqueries().get(0);
			}
			
			output+= getNonCanonicalizedDisplay(instrDataFromSubQ1, studentDataFromSubQ1, op2)+"</div>";
			
		}
		
		//Display WHERE CLAUSE SUBQ structure
		
		String op3 = "";
		QueryStructure instrDataWhereSubQ1= new QueryStructure(instrData1.getTableMap());
		QueryStructure studentDataWhereSubQ1 =new QueryStructure(studentData1.getTableMap());
		
		if( (instrData1.getWhereClauseSubqueries() != null && !instrData1.getWhereClauseSubqueries().isEmpty()) 
				|| (studentData1.getWhereClauseSubqueries() != null && !studentData1.getWhereClauseSubqueries().isEmpty())){
			
			op3 += "<br/><div style ='width:'80%; align:left; padding-left:20px;'><label><h3>Where Clause SubQuery: </h3></label>";
			if(instrData1.getWhereClauseSubqueries() != null && !instrData1.getWhereClauseSubqueries().isEmpty()){
				instrDataWhereSubQ1 = instrData1.getWhereClauseSubqueries().get(0);
			}
			if(studentData1.getWhereClauseSubqueries() != null && !studentData1.getWhereClauseSubqueries().isEmpty()){
				studentDataWhereSubQ1 = studentData1.getWhereClauseSubqueries().get(0);
			}
			
			output+= getNonCanonicalizedDisplay(instrDataWhereSubQ1, studentDataWhereSubQ1, op3)+"</div>";
			
		}
		
		
		output += "</section></div>";
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
	    
	    /**
	     * This method creates HTML display String as output for Canonicalized tab structure.  
	     * It is written as separate method to aid display of subQueries 
	     * 
	     * @param instrData
	     * @param studentData
	     * @param output
	     * @return
	     */
	    public String getCanonicalizedDisplay(QueryStructure instrData, QueryStructure studentData, String output){
	    	
			output+="<table class='queryTable' width='70%' cellpadding='3' cellspacing='1'><tr>"+
					"<th width='20%'>&nbsp;</th><th width='20%' align='center'>Student</th><th width='20%' align='center'>Instructor</th></tr>";
		
		if( (instrData != null && instrData.getLstRelationInstances().size() > 0)
			|| (studentData != null && studentData.getLstRelationInstances().size() > 0)){
			output += "<tr><td class='emph''>Relations</td>" +
					"<td width=\"20%\">"+listToString(studentData.getLstRelationInstances(),instrData.getLstRelationInstances())+"</td>"+
					"<td width=\"20%\">"+listToString(instrData.getLstRelationInstances(), studentData.getLstRelationInstances())+"</td></tr>";
		
		}
		
		if( (instrData != null && instrData.getLstProjectedCols().size() > 0)
	  			|| (studentData != null && studentData.getLstProjectedCols().size() > 0)){
					output += "<tr><td class='emph''>Projections</td>" +
							"<td width=\"20%\">"+listToString(studentData.getLstProjectedCols(),instrData.getLstProjectedCols())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData.getLstProjectedCols(), studentData.getLstProjectedCols())+"</td></tr>";
	  		
				}
		
		if( (instrData != null && instrData.getIsDistinct())
	  			|| (studentData != null && studentData.getIsDistinct())){
			output += "<tr><td class='emph''>Distinct</td>" ;
			
			int instDistinct = 0;
			int studDistinct = 0;
			if(instrData.getIsDistinct()){
				instDistinct =1;
			}if(studentData.getIsDistinct()){
				studDistinct = 1;
			}
			if(studentData.getIsDistinct() && !instrData.getIsDistinct()){
				
				output += "<td width=\"20%\" align='center' class=\"number\" style=\"color: red;\">"+studDistinct+"</td>";
			}else{
				output += "<td width=\"20%\" align='center' class='number'>"+studDistinct+"</td>";
			}
				
			if((instrData.getIsDistinct() && !studentData.getIsDistinct())){
				output += "<td width=\"20%\" align='center' class=\"number\" style=\"color: red;\">"+instDistinct+"</td></tr>";
				
			}else{
				output += "<td width=\"20%\" align='center' class='number'>"+instDistinct+"</td></tr>";
			}
			}
		
		
		if( (instrData != null && instrData.getLstGroupByNodes().size() > 0)
	  			|| (studentData != null && studentData.getLstGroupByNodes().size() > 0)){
					output += "<tr><td class='emph''>Group By</td>" +
							"<td width=\"20%\">"+listToString(studentData.getLstGroupByNodes(),instrData.getLstGroupByNodes())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData.getLstGroupByNodes(), studentData.getLstGroupByNodes())+"</td></tr>";
	  		
			}
			
			if( (instrData != null && instrData.getLstOrderByNodes().size() > 0)
	  			|| (studentData != null && studentData.getLstOrderByNodes().size() > 0)){
			
				output += "<tr><td class='emph''>Order By</td>" +
							"<td width=\"20%\">"+listToString(studentData.getLstOrderByNodes(),instrData.getLstOrderByNodes())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData.getLstOrderByNodes(), studentData.getLstOrderByNodes())+"</td></tr>";
			}
			
			if( (instrData != null && instrData.getLstHavingConditions().size() > 0)
	  			|| (studentData != null && studentData.getLstHavingConditions().size() > 0)){
				output += "<tr><td class='emph''>Having Clause</td>" +
						  "<td width=\"20%\">"+listToString(studentData.getLstHavingConditions(),instrData.getLstHavingConditions())+"</td>"+
						  "<td width=\"20%\">"+listToString(instrData.getLstHavingConditions(), studentData.getLstHavingConditions())+"</td></tr>";

	}
		if( (instrData != null && instrData.getLstSubQConnectives().size() > 0)
	  			|| (studentData != null && studentData.getLstSubQConnectives().size() > 0)){
	  			
	  			output += "<tr><td class='emph''>SubQuery Connectives</td>" +
							"<td width=\"20%\">"+listToString(studentData.getLstSubQConnectives(),instrData.getLstSubQConnectives())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData.getLstSubQConnectives(), studentData.getLstSubQConnectives())+"</td></tr>";
	  			
	  			}
	
	if( (instrData != null && instrData.getLstSetOpetators().size() > 0)
	  			|| (studentData != null && studentData.getLstSetOpetators().size() > 0)){
	  			
	  			output += "<tr><td class='emph''>Set Operators</td>" +
							"<td width=\"20%\">"+listToString(studentData.getLstSetOpetators(),instrData.getLstSetOpetators())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData.getLstSetOpetators(), studentData.getLstSetOpetators())+"</td></tr>";
	  			
	  			
	  			}
	if( (instrData != null && instrData.getLstSelectionConditions().size() > 0)
	  			|| (studentData != null && studentData.getLstSelectionConditions().size() > 0)){
	  			
	  				output += "<tr><td class='emph''>Selection Conditions</td>" +
							"<td width=\"20%\">"+listToString(studentData.getLstSelectionConditions(),instrData.getLstSelectionConditions())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData.getLstSelectionConditions(), studentData.getLstSelectionConditions())+"</td></tr>";
	  		
	  		
	  			}
	
if( (instrData != null && instrData.getLstJoinTables().size() > 0)
	  			|| (studentData != null && studentData.getLstJoinTables().size() > 0)){
	  			
	  				output += "<tr><td class='emph''>Join Tables</td>" +
							"<td width=\"20%\">"+listToString(studentData.getLstJoinTables(),instrData.getLstJoinTables())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData.getLstJoinTables(), studentData.getLstJoinTables())+"</td></tr>";
	  		
	  		
	  			}
	  			
if( (instrData != null && instrData.getLstJoinConditions().size() > 0)
	  			|| (studentData != null && studentData.getLstJoinConditions().size() > 0)){
	
	output += "<tr><td class='emph'>Join Conditions </td>" +
							"<td width=\"20%\">"+listToString(studentData.getLstJoinConditions(),instrData.getLstJoinConditions())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData.getLstJoinConditions(), studentData.getLstJoinConditions())+"</td></tr>";
	  		
	  		}
output += "</table>";
return output;
	    }
	    
	    
	/**
	 * This method creates HTML display String as output for non-canonicalized tab structure.  
	 * It is written as separate method to aid display of subQueries    
	 * 
	 * @param instrData1
	 * @param studentData1
	 * @param output
	 * @return
	 */
	    
	public String getNonCanonicalizedDisplay(QueryStructure instrData1, QueryStructure studentData1, String output){

		output+="<table class='queryTable1' width='70%' cellpadding='3' cellspacing='1'><tr>"+
				"<th width='20%'>&nbsp;</th><th width='20%' align='center'>Student</th><th width='20%' align='center'>Instructor</th></tr>";
		
		if( (instrData1 != null && instrData1.getLstRelationInstances().size() > 0)
			|| (studentData1 != null && studentData1.getLstRelationInstances().size() > 0)){
			output += "<tr><td class='emph''>Relations</td>" +
					"<td width=\"20%\">"+listToString(studentData1.getLstRelationInstances(),instrData1.getLstRelationInstances())+"</td>"+
					"<td width=\"20%\">"+listToString(instrData1.getLstRelationInstances(), studentData1.getLstRelationInstances())+"</td></tr>";
		
		}
		
		if( (instrData1 != null && instrData1.getLstProjectedCols().size() > 0)
	  			|| (studentData1 != null && studentData1.getLstProjectedCols().size() > 0)){
					output += "<tr><td class='emph''>Projections</td>" +
							"<td width=\"20%\">"+listToString(studentData1.getLstProjectedCols(),instrData1.getLstProjectedCols())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.getLstProjectedCols(), studentData1.getLstProjectedCols())+"</td></tr>";
	  		
				}
		
		if( (instrData1 != null && instrData1.getIsDistinct())
	  			|| (studentData1 != null && studentData1.getIsDistinct())){
			output += "<tr><td class='emph'>Distinct</td>" ;
			
			int instDistinct = 0;
			int studDistinct = 0;
			if(instrData1.getIsDistinct()){
				instDistinct =1;
			}if(studentData1.getIsDistinct()){
				studDistinct = 1;
			}
			if(studentData1.getIsDistinct() && !instrData1.getIsDistinct()){
				
				output += "<td width=\"20%\" align='center' class=\"number\" style=\"color: red;\">"+studDistinct+"</td>";
			}else{
				output += "<td width=\"20%\" align='center' class='number'>"+studDistinct+"</td>";
			}
				
			if((instrData1.getIsDistinct() && !studentData1.getIsDistinct())){
				output += "<td width=\"20%\" align='center' class=\"number\" style=\"color: red;\">"+instDistinct+"</td></tr>";
				
			}else{
				output += "<td width=\"20%\" align='center' class='number'>"+instDistinct+"</td></tr>";
			}
			}
		
		
		if( (instrData1 != null && instrData1.getLstGroupByNodes().size() > 0)
	  			|| (studentData1 != null && studentData1.getLstGroupByNodes().size() > 0)){
					output += "<tr><td class='emph''>Group By</td>" +
							"<td width=\"20%\">"+listToString(studentData1.getLstGroupByNodes(),instrData1.getLstGroupByNodes())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.getLstGroupByNodes(), studentData1.getLstGroupByNodes())+"</td></tr>";
	  		
			} 
			
			if( (instrData1 != null && instrData1.getLstOrderByNodes().size() > 0)
	  			|| (studentData1 != null && studentData1.getLstOrderByNodes().size() > 0)){
			
				output += "<tr><td class='emph''>Order By</td>" +
							"<td width=\"20%\">"+listToString(studentData1.getLstOrderByNodes(),instrData1.getLstOrderByNodes())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.getLstOrderByNodes(), studentData1.getLstOrderByNodes())+"</td></tr>";
			}
			
			if( (instrData1 != null && instrData1.getLstHavingConditions().size() > 0)
	  			|| (studentData1 != null && studentData1.getLstHavingConditions().size() > 0)){
				output += "<tr><td class='emph''>Having Clause</td>" +
						  "<td width=\"20%\">"+listToString(studentData1.getLstHavingConditions(),instrData1.getLstHavingConditions())+"</td>"+
						  "<td width=\"20%\">"+listToString(instrData1.getLstHavingConditions(), studentData1.getLstHavingConditions())+"</td></tr>";
	}
		if( (instrData1 != null && instrData1.getLstSubQConnectives().size() > 0)
	  			|| (studentData1 != null && studentData1.getLstSubQConnectives().size() > 0)){
	  			
	  			output += "<tr><td class='emph''>SubQuery Connectives</td>" +
							"<td width=\"20%\">"+listToString(studentData1.getLstSubQConnectives(),instrData1.getLstSubQConnectives())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.getLstSubQConnectives(), studentData1.getLstSubQConnectives())+"</td></tr>";
	  			
	  			}
	
	if( (instrData1 != null && instrData1.getLstSetOpetators().size() > 0)
	  			|| (studentData1 != null && studentData1.getLstSetOpetators().size() > 0)){
	  			
	  			output += "<tr><td class='emph''>Set Operators</td>" +
							"<td width=\"20%\">"+listToString(studentData1.getLstSetOpetators(),instrData1.getLstSetOpetators())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.getLstSetOpetators(), studentData1.getLstSetOpetators())+"</td></tr>";
	  			
	  			
	  			}
	if( (instrData1 != null && instrData1.getLstSelectionConditions().size() > 0)
	  			|| (studentData1 != null && studentData1.getLstSelectionConditions().size() > 0)){
	  			
	  				output += "<tr><td class='emph''>Selection Conditions</td>" +
							"<td width=\"20%\">"+listToString(studentData1.getLstSelectionConditions(),instrData1.getLstSelectionConditions())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.getLstSelectionConditions(), studentData1.getLstSelectionConditions())+"</td></tr>";
	  		
	  		
	  			}
	
if( (instrData1 != null && instrData1.getLstJoinTables().size() > 0)
	  			|| (studentData1 != null && studentData1.getLstJoinTables().size() > 0)){
	  			
	  				output += "<tr><td class='emph''>Join Tables</td>" +
							"<td width=\"20%\">"+listToString(studentData1.getLstJoinTables(),instrData1.getLstJoinTables())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.getLstJoinTables(), studentData1.getLstJoinTables())+"</td></tr>";
	  		
	  		
	  			}
	  			
if( (instrData1 != null && instrData1.getLstJoinConditions().size() > 0)
	  			|| (studentData1 != null && studentData1.getLstJoinConditions().size() > 0)){
	
	output += "<tr><td class='emph'>Join Conditions</td>" +
							"<td width=\"20%\">"+listToString(studentData1.getLstJoinConditions(),instrData1.getLstJoinConditions())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.getLstJoinConditions(), studentData1.getLstJoinConditions())+"</td></tr>";
	  		
	  		}
	output += "</table>";
	return output;
	}
	    
	    
	    
}