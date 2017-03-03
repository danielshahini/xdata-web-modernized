
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import parsing.JoinClauseInfo;
import parsing.Node;
import parsing.QueryStructure;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import partialMarking.PartialMarkParameters;
import partialMarking.TestPartialMarking;
import testDataGen.PopulateTestDataGrading;
import util.Configuration;
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
		
		int assignId=11;  //Hard code some existing assignment ID here and in TestPartialMarking.java - process and process canonicalize methods
		String err= "";
		int index =1;
				PrintWriter out = response.getWriter();
				try{ 
					graderConn = this.getConnection();
					p.deleteAllTempTablesFromTestUser(graderConn);
					p.createTempTablesForDemoUI(graderConn, assignId, 1);
				
					for(String instQuery:instructorQueries){
						//run the query in the database and check if it is syntactically correct						
						Statement graderStatement = graderConn.createStatement();
						graderStatement.execute(instQuery);
						index ++;
					}
					
				}catch(Exception e){
					
					e.printStackTrace(); 
					caughtException = e;
					PrintWriter writer = response.getWriter();
					response.setStatus(500);
					writer.print("Syntax error in Instructor Query "+index+" : \n\n"+e.getMessage());
					writer.close();
					response.sendError(500,e.getMessage());
					return;
					
					
				}finally{
					try {
						if(graderConn != null && ! graderConn.isClosed())
							graderConn.close();
					} catch (SQLException e1) {
						
						e1.printStackTrace();
					}
				}
				if(caughtException == null){
				try{
					graderConn = this.getConnection();
					p.deleteAllTempTablesFromTestUser(graderConn);
					p.createTempTablesForDemoUI(graderConn, assignId, 1);
					//run the studentQuery in the database and check if it is syntactically correct						
					Statement graderStatement = graderConn.createStatement();
					graderStatement.execute(studentQuery);
				}catch(Exception e){
					e.printStackTrace();
					caughtException = e;
					PrintWriter writer = response.getWriter();
					response.setStatus(500);
				    writer.print("Syntax error in Student Query : \n\n"+e.getMessage());
				    writer.close();
				    response.sendError(500,e.getMessage());
				    return;
					   
				}finally{
					try {
						if(graderConn != null && ! graderConn.isClosed())
							graderConn.close();
					} catch (SQLException e1) {
						
						e1.printStackTrace();
					}
				}
				}
				
			if(caughtException== null ){
				response.setContentType("text/html;charset=UTF-8");
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
					
								if(newMarks>= marks){
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
								
								if(newMarks1>= marks1){
									marks1=newMarks1;
									bestInstructorQueryData1=testObj1.InstructorQuery.getQueryStructure();
									bestInstructorQueryString1=instQuery;
								}
							
						}
				}catch(Exception e){
					errorMessage=e.getMessage();
					e.printStackTrace();
					//throw new ServletException();
					PrintWriter writer = response.getWriter();
					response.setStatus(500);
				    writer.print("Internal Error. Please check log file for details.");
				    writer.close();
				    response.sendError(500,e.getMessage());
				    return;
					
				}
				/***** CODE TO DISPLAY RESULT IN HTML FORM ****/
				try{
							QueryStructure instrData = bestInstructorQueryData;
							QueryStructure studentData = testObj.StudentQuery.getQueryStructure();
							
							QueryStructure instrData1 = bestInstructorQueryData1;
							QueryStructure studentData1 = testObj1.StudentQuery.getQueryStructure();
							
							//Upload the details of with canonicalization in first div
							String output="";
							output += "<ul class=\"nav nav-tabs\"><li class=\"active\"><a href=\"#tab1\">Canonicalized Partial Marks: "+roundToDecimal(marks)+"</a></li><li><a href=\"#tab2\">Non-Canonicalized Marks:"+roundToDecimal(marks1)+"</a></li></ul>";
							
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
				
				if( instrData1 != null && studentData1 != null && 
						instrData1.getFromClauseSubqueries()!=null && studentData1.getFromClauseSubqueries()!=null &&	
						(!instrData1.getFromClauseSubqueries().isEmpty() 
						||  !studentData1.getFromClauseSubqueries().isEmpty())){
					QueryStructure instrDataFromSubQ1= new QueryStructure(instrData1.getTableMap());
					QueryStructure studentDataFromSubQ1 =new QueryStructure(studentData1.getTableMap());

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
				
				if( instrData1 != null && studentData1 != null &&
						instrData1.getWhereClauseSubqueries()!=null && studentData1.getWhereClauseSubqueries()!=null&&
						(!instrData1.getWhereClauseSubqueries().isEmpty()  ||  !studentData1.getWhereClauseSubqueries().isEmpty())){
					QueryStructure instrDataWhereSubQ1= new QueryStructure(instrData1.getTableMap());
					QueryStructure studentDataWhereSubQ1 =new QueryStructure(studentData1.getTableMap());
					
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
				}catch(Exception e){
					errorMessage=e.getMessage();
					e.printStackTrace();
					//throw new ServletException();
					PrintWriter writer = response.getWriter();
					response.setStatus(500);
				    writer.print("Internal Error. Please check log file for details.");
				    writer.close();
				    response.sendError(500,e.getMessage());
				    return;	
				}
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
			if(list1==null||list2==null){
				return "";
			}
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
		 * This function compares the NODES in the list and returns required html element
		 * if they are same (with default color  black)/ different(with color red)
		 * 
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
	
			ArrayList<Node> instrInnerJoin =new ArrayList<Node>();
			ArrayList<Node> studentInnerJoin =new ArrayList<Node>();

					for(Node n : instrData.getLstJoinConditions()){
						if(n.getJoinType().equalsIgnoreCase(JoinClauseInfo.innerJoin)){
							instrInnerJoin.add(n);
						}
					}for(Node n : studentData.getLstJoinConditions()){
						if(n.getJoinType().equalsIgnoreCase(JoinClauseInfo.innerJoin)){
							studentInnerJoin.add(n);
						}
					}
					if((instrInnerJoin != null && instrInnerJoin.size() > 0) || (studentInnerJoin != null && studentInnerJoin.size() > 0) ){
						output += "<tr><td class='emph'>Inner Join Conditions </td>" ;
						output += "<td width=\"20%\">"+listToString(studentInnerJoin,instrInnerJoin)+"</td>";
						output += "<td width=\"20%\">"+listToString(instrInnerJoin,studentInnerJoin)+"</td></tr>";
					}
}

	
if( (instrData != null && instrData.getLstJoinConditions().size() > 0)
	|| (studentData != null && studentData.getLstJoinConditions().size() > 0)){
		
		ArrayList<Node> instrOuterJoin =new ArrayList<Node>();
		ArrayList<Node> studentOuterJoin =new ArrayList<Node>();
		
			for(Node n : instrData.getLstJoinConditions()){
				if(n.getJoinType().equalsIgnoreCase(JoinClauseInfo.leftOuterJoin)
						|| n.getJoinType().equalsIgnoreCase(JoinClauseInfo.rightOuterJoin)
						||n.getJoinType().equalsIgnoreCase(JoinClauseInfo.fullOuterJoin)){
					instrOuterJoin.add(n);
				}
			}for(Node n : studentData.getLstJoinConditions()){
				if(n.getJoinType().equalsIgnoreCase(JoinClauseInfo.leftOuterJoin)
						|| n.getJoinType().equalsIgnoreCase(JoinClauseInfo.rightOuterJoin)
						||n.getJoinType().equalsIgnoreCase(JoinClauseInfo.fullOuterJoin)){
					studentOuterJoin.add(n);
				}
	}
			if((instrOuterJoin != null && instrOuterJoin.size() >0) || (studentOuterJoin != null && studentOuterJoin.size() > 0)){
				output += "<tr><td class='emph'>Outer Join Conditions </td>" ;
				output += "<td width=\"20%\">"+listToString(studentOuterJoin,instrOuterJoin)+"</td>";
				output += "<td width=\"20%\">"+listToString(instrOuterJoin, studentOuterJoin)+"</td></tr>";
			}
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
		
		if((instrData1 != null && instrData1.getLstRelationInstances()!=null && instrData1.getLstRelationInstances().size() > 0) ||
				( studentData1 != null &&  
						studentData1.getLstRelationInstances()!=null && studentData1.getLstRelationInstances().size() > 0)){
			output += "<tr><td class='emph''>Relations</td>" +
					"<td width=\"20%\">"+listToString(studentData1.getLstRelationInstances(),instrData1.getLstRelationInstances())+"</td>"+
					"<td width=\"20%\">"+listToString(instrData1.getLstRelationInstances(), studentData1.getLstRelationInstances())+"</td></tr>";
		
		}
		
		if( (instrData1 != null && instrData1.getLstProjectedCols()!=null&& instrData1.getLstProjectedCols().size() > 0) ||			
				(studentData1 != null &&  
						studentData1.getLstProjectedCols()!=null &&  studentData1.getLstProjectedCols().size() > 0)){
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
		
		
		if( (instrData1 != null && instrData1.getLstGroupByNodes()!=null && instrData1.getLstGroupByNodes().size() > 0) ||
		(studentData1 != null && studentData1.getLstGroupByNodes()!=null && studentData1.getLstGroupByNodes().size() > 0)){
					output += "<tr><td class='emph''>Group By</td>" +
							"<td width=\"20%\">"+listToString(studentData1.getLstGroupByNodes(),instrData1.getLstGroupByNodes())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.getLstGroupByNodes(), studentData1.getLstGroupByNodes())+"</td></tr>";
	  		
			} 
			
			if( (instrData1 != null &&  instrData1.getLstOrderByNodes()!=null && instrData1.getLstOrderByNodes().size() > 0) ||					
					( studentData1 != null && 
							studentData1.getLstOrderByNodes()!=null &&  studentData1.getLstOrderByNodes().size() > 0)){
			
				output += "<tr><td class='emph''>Order By</td>" +
							"<td width=\"20%\">"+listToString(studentData1.getLstOrderByNodes(),instrData1.getLstOrderByNodes())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.getLstOrderByNodes(), studentData1.getLstOrderByNodes())+"</td></tr>";
			}
			
			if( (instrData1 != null &&   instrData1.getLstHavingConditions()!=null && instrData1.getLstHavingConditions().size() > 0) ||( 
					 studentData1 != null && 
						studentData1.getLstHavingConditions()!=null &&  studentData1.getLstHavingConditions().size() > 0)){
				output += "<tr><td class='emph''>Having Clause</td>" +
						  "<td width=\"20%\">"+listToString(studentData1.getLstHavingConditions(),instrData1.getLstHavingConditions())+"</td>"+
						  "<td width=\"20%\">"+listToString(instrData1.getLstHavingConditions(), studentData1.getLstHavingConditions())+"</td></tr>";
	}
		if( (instrData1 != null && instrData1.getLstSubQConnectives()!=null && instrData1.getLstSubQConnectives().size() > 0) ||
 (				studentData1 != null &&  
			studentData1.getLstSubQConnectives()!=null &&  studentData1.getLstSubQConnectives().size() > 0)){
	  			
	  			output += "<tr><td class='emph''>SubQuery Connectives</td>" +
							"<td width=\"20%\">"+listToString(studentData1.getLstSubQConnectives(),instrData1.getLstSubQConnectives())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.getLstSubQConnectives(), studentData1.getLstSubQConnectives())+"</td></tr>";
	  			
	  			}
	
	if( (instrData1 != null &&  instrData1.getLstSetOpetators()!=null && instrData1.getLstSetOpetators().size() > 0) ||			
			(studentData1 != null &&
					studentData1.getLstSetOpetators()!=null &&  studentData1.getLstSetOpetators().size() > 0)){
	  			
	  			output += "<tr><td class='emph''>Set Operators</td>" +
							"<td width=\"20%\">"+listToString(studentData1.getLstSetOpetators(),instrData1.getLstSetOpetators())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.getLstSetOpetators(), studentData1.getLstSetOpetators())+"</td></tr>";
	  			
	  			
	  			}
	if( (instrData1 != null && instrData1.getLstSelectionConditions()!=null && instrData1.getLstSelectionConditions().size() > 0) ||
			(studentData1 != null &&  
					studentData1.getLstSelectionConditions()!=null &&  studentData1.getLstSelectionConditions().size() > 0)){
	  			
	  				output += "<tr><td class='emph''>Selection Conditions</td>" +
							"<td width=\"20%\">"+listToString(studentData1.getLstSelectionConditions(),instrData1.getLstSelectionConditions())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.getLstSelectionConditions(), studentData1.getLstSelectionConditions())+"</td></tr>";
	  		
	  		
	  			}
	
if( (instrData1 != null && instrData1.getLstJoinTables()!=null && instrData1.getLstJoinTables().size() > 0) || (studentData1 != null && 
studentData1.getLstJoinTables()!=null &&  studentData1.getLstJoinTables().size() > 0)){
	  			
	  				output += "<tr><td class='emph''>Join Tables</td>" +
							"<td width=\"20%\">"+listToString(studentData1.getLstJoinTables(),instrData1.getLstJoinTables())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.getLstJoinTables(), studentData1.getLstJoinTables())+"</td></tr>";
	  		
	  		
	  			}
	  			
/*if( (instrData1 != null && instrData1.getLstJoinConditions().size() > 0)
	  			|| (studentData1 != null && studentData1.getLstJoinConditions().size() > 0)){
	
	output += "<tr><td class='emph'>Join Conditions</td>" +
							"<td width=\"20%\">"+listToString(studentData1.getLstJoinConditions(),instrData1.getLstJoinConditions())+"</td>"+
							"<td width=\"20%\">"+listToString(instrData1.getLstJoinConditions(), studentData1.getLstJoinConditions())+"</td></tr>";
	  		
	  		}*/

	
if( (instrData1 != null && instrData1.getLstJoinConditions()!=null && instrData1.getLstJoinConditions().size() > 0) || 
		(studentData1 != null &&  
studentData1.getLstJoinConditions()!=null &&  studentData1.getLstJoinConditions().size() > 0)){

ArrayList<Node> instrInnerJoin =new ArrayList<Node>();
ArrayList<Node> studentInnerJoin =new ArrayList<Node>();

	for(Node n : instrData1.getLstJoinConditions()){
		if(n.getJoinType().equalsIgnoreCase(JoinClauseInfo.innerJoin)){
			instrInnerJoin.add(n);
		}
	}for(Node n : studentData1.getLstJoinConditions()){
		if(n.getJoinType().equalsIgnoreCase(JoinClauseInfo.innerJoin)){
			studentInnerJoin.add(n);
		}
	}
	if((instrInnerJoin != null && instrInnerJoin.size() > 0) || (studentInnerJoin != null && studentInnerJoin.size() > 0) ){
		output += "<tr><td class='emph'>Inner Join Conditions </td>" ;
		output += "<td width=\"20%\">"+listToString(studentInnerJoin,instrInnerJoin)+"</td>";
		output += "<td width=\"20%\">"+listToString(instrInnerJoin,studentInnerJoin)+"</td></tr>";
	}
}


if( (instrData1 != null && instrData1.getLstJoinConditions()!=null && instrData1.getLstJoinConditions().size() > 0) ||
  (	studentData1 != null &&  
		  studentData1.getLstJoinConditions()!=null &&  studentData1.getLstJoinConditions().size() > 0)){

ArrayList<Node> instrOuterJoin =new ArrayList<Node>();
ArrayList<Node> studentOuterJoin =new ArrayList<Node>();

for(Node n : instrData1.getLstJoinConditions()){
if(n.getJoinType().equalsIgnoreCase(JoinClauseInfo.leftOuterJoin)
		|| n.getJoinType().equalsIgnoreCase(JoinClauseInfo.rightOuterJoin)
		||n.getJoinType().equalsIgnoreCase(JoinClauseInfo.fullOuterJoin)){
	instrOuterJoin.add(n);
}
}for(Node n : studentData1.getLstJoinConditions()){
if(n.getJoinType().equalsIgnoreCase(JoinClauseInfo.leftOuterJoin)
		|| n.getJoinType().equalsIgnoreCase(JoinClauseInfo.rightOuterJoin)
		||n.getJoinType().equalsIgnoreCase(JoinClauseInfo.fullOuterJoin)){
	studentOuterJoin.add(n);
}
}
if(instrOuterJoin != null && studentOuterJoin != null && ( instrOuterJoin.size() >0 ||  studentOuterJoin.size() > 0)){
output += "<tr><td class='emph'>Outer Join Conditions </td>" ;
output += "<td width=\"20%\">"+listToString(studentOuterJoin,instrOuterJoin)+"</td>";
output += "<td width=\"20%\">"+listToString(instrOuterJoin, studentOuterJoin)+"</td></tr>";
}
}

	output += "</table>";
	return output;
	}
	    
	
/**
 * Get standalone DB Connection for demo
 * 	
 * @return
 * @throws Exception
 */
public Connection getConnection() throws Exception{
	
	Class.forName("org.postgresql.Driver");
	
	String loginUrl = "jdbc:postgresql://" + Configuration.getProperty("databaseIP") + ":" + Configuration.getProperty("databasePort") + "/" + Configuration.getProperty("databaseName");
	Connection conn=DriverManager.getConnection(loginUrl, Configuration.getProperty("existingDatabaseUser"), Configuration.getProperty("existingDatabaseUserPasswd"));;
	return conn;

	
}
	    
}