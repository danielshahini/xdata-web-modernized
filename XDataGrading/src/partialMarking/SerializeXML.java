package partialMarking;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import parsing.JoinClauseInfo;
import parsing.Node;
import partialMarking.QueryData;

public class SerializeXML {
	public static PrintWriter  out;
	public static int idCounter=1;
	public static String spaceTab="    ";
	
	public static void serializeXML(String fileName, QueryData qData) throws IOException, CloneNotSupportedException{
		  out = new PrintWriter(new FileWriter(fileName));
		  printHead();
		  printHasDistinct(qData);
		  printProjectedColumns(qData);
		  printJoinConditions(qData);
		  printSelectionConditions(qData);
		  printGroupByColumns(qData);
		  printHavingConditions(qData);
		  printJoinTables(qData);
		  printRedundantTables(qData);
		  printSubqueryConnectives(qData);
		  printTail();
		  out.flush();
		  out.close();
	}
	
	
	public static void serializeXML(String fileName, QueryStructure qData) throws IOException, CloneNotSupportedException{
		  out = new PrintWriter(new FileWriter(fileName));
		  printHead();
		  out.println(getQueryStructureString(qData, true));
		  printTail();
		  out.flush();
		  out.close();
	}
	
	public static String getQueryStructureString(QueryStructure qData, boolean openFlag) throws IOException, CloneNotSupportedException{
		  String printString="";
		  if(qData.setOperator==null||qData.setOperator.isEmpty()){
			  printString+=getHasDistinctString(qData,openFlag);
			  printString+=getProjectedColumnsString(qData,openFlag);
			  printString+=getJoinConditionsString(qData,openFlag);
			  printString+=getSelectionConditionsString(qData,openFlag);
			  printString+=getSubqueryConditionsString(qData, openFlag);
			  printString+=getGroupByColumnsString(qData,openFlag);
			  printString+=getHavingConditionsString(qData,openFlag);
			  printString+=getJoinTablesString(qData,openFlag);
			  printString+=getRedundantTablesString(qData,openFlag);
			  printString+=getSubqueryConnectivesString(qData,openFlag);
			  printString+=getFromSubqueriesString(qData,openFlag);
			  printString+=getWhereSubqueriesString(qData,openFlag);
		  }
		  else{
			  if(openFlag){
				  printString+="<item text=\""+qData.setOperator +"\" open=\"1\" id=\""+ idCounter++ +"\">\n";
				  printString+=spaceTab+"<item text=\"left SubQuery\" open=\"1\" id=\""+ idCounter++ +"\">\n";
				  printString+=getQueryStructureString(qData.leftQuery,false);
				  printString+=spaceTab+"</item>\n";
				  printString+=spaceTab+"<item text=\"right SubQuery\" open=\"1\" id=\""+ idCounter++ +"\">\n";				
				  printString+=getQueryStructureString(qData.rightQuery,false);
				  printString+=spaceTab+"</item>\n";					 
				  printString+="</item>\n";
			  }
			  else{
				  printString+="<item text=\""+qData.setOperator +"\"  id=\""+ idCounter++ +"\">\n";
				  printString+=spaceTab+"<item text=\"left SubQuery\"  id=\""+ idCounter++ +"\">\n";
				  printString+=getQueryStructureString(qData.leftQuery,false);
				  printString+=spaceTab+"</item>\n";
				  printString+=spaceTab+"<item text=\"right SubQuery\"  id=\""+ idCounter++ +"\">\n";				
				  printString+=getQueryStructureString(qData.rightQuery,false);
				  printString+=spaceTab+"</item>\n";					 
				  printString+="</item>\n";

			  }
		  }
		  return printString;		
	}
	
	public static String getProjectedColumnsString(QueryStructure qData,boolean openFlag){
		if(openFlag){
		 String retString="<item text=\"Projected Columns\" open=\"1\" id=\""+ idCounter++ +"\">\n";
		 for(parsing.Node n:toSetOfNodes(qData.getLstProjectedCols())){
			 retString+=spaceTab+"<item text=\""+ n.toString() +"\" id=\""+ idCounter++ +"\"/>\n";
		 }
		 retString+="</item>\n";
		 return retString;
		}
		else{
			 String retString="<item text=\"Projected Columns\" id=\""+ idCounter++ +"\">\n";
			 for(parsing.Node n:toSetOfNodes(qData.getLstProjectedCols())){
				 retString+=spaceTab+"<item text=\""+ n.toString() +"\" id=\""+ idCounter++ +"\"/>\n";
			 }
			 retString+="</item>\n";
			 return retString;

		}		
	}
	
	public static void printProjectedColumns(QueryData qData){
		 out.println("<item text=\"Projected Columns\" open=\"1\" id=\""+ idCounter++ +"\">");
		 for(parsing.Node n:toSetOfNodes(qData.getProjectionList())){
			 out.println(spaceTab+"<item text=\""+ n.toString() +"\" id=\""+ idCounter++ +"\"/>");
		 }
		 out.println("</item>");
	}
	
	public static String getFromSubqueriesString(QueryStructure qData, boolean openFlag) throws IOException, CloneNotSupportedException{
		if(openFlag){
		 String retString="<item text=\"From Subqueries\" open=\"1\" id=\""+ idCounter++ +"\">\n";
		 int i=1;
		 for(QueryStructure subquery:qData.getFromClauseSubqueries()){
			 retString+=spaceTab+"<item text=\"fromSubquery"+i+"\" id=\""+ idCounter++ +"\">\n";
			 retString+=getQueryStructureString(subquery,false);
			 retString+=spaceTab+"</item>\n";
			 i++;
		 }
		 retString+="</item>\n";
		 return retString;

		}
		else{
			 String retString="<item text=\"From Subqueries\" id=\""+ idCounter++ +"\">\n";
			 int i=1;
			 for(QueryStructure subquery:qData.getFromClauseSubqueries()){
				 retString+=spaceTab+"<item text=\"fromSubquery"+i+"\" id=\""+ idCounter++ +"\">\n";
				 retString+=getQueryStructureString(subquery,false);
				 retString+=spaceTab+"</item>\n";
				 i++;
			 }
			 retString+="</item>\n";
			 return retString;
			
		}
	}
	
	public static String getWhereSubqueriesString(QueryStructure qData, boolean openFlag) throws IOException, CloneNotSupportedException{
		if(openFlag){
		 String retString="<item text=\"Where Subqueries\" open=\"1\" id=\""+ idCounter++ +"\">\n";
		 int i=1;
		 for(QueryStructure subquery:qData.getWhereClauseSubqueries()){
			 retString+=spaceTab+"<item text=\"whereSubquery"+i+"\" id=\""+ idCounter++ +"\">\n";
			 retString+=getQueryStructureString(subquery,false);
			 retString+=spaceTab+"</item>\n";
			 i++;
		 }
		 retString+="</item>\n";
		 return retString;
		}
		else{
			String retString="<item text=\"Where Subqueries\"  id=\""+ idCounter++ +"\">\n";
			 int i=1;
			 for(QueryStructure subquery:qData.getWhereClauseSubqueries()){
				 retString+=spaceTab+"<item text=\"whereSubquery"+i+"\" id=\""+ idCounter++ +"\">\n";
				 retString+=getQueryStructureString(subquery,false);
				 retString+=spaceTab+"</item>\n";
				 i++;
			 }
			 retString+="</item>\n";
			 return retString;
		}
	}
	
	public static String getHasDistinctString(QueryStructure qData, boolean openFlag){
		if(openFlag){
			String retString="<item text=\"Distinct Present\" open=\"1\" id=\""+ idCounter++ +"\">\n";
			if(qData.getIsDistinct()){
				retString+=spaceTab+"<item text=\"True\" id=\""+ idCounter++ +"\"/>\n";
			}
			else{
				retString+=spaceTab+"<item text=\"False\" id=\""+ idCounter++ +"\"/>\n";
			}
			retString+="</item>\n";
			return retString;
		}
		else{
			String retString="<item text=\"Distinct Present\" id=\""+ idCounter++ +"\">\n";
			if(qData.getIsDistinct()){
				retString+=spaceTab+"<item text=\"True\" id=\""+ idCounter++ +"\"/>\n";
			}
			else{
				retString+=spaceTab+"<item text=\"False\" id=\""+ idCounter++ +"\"/>\n";
			}
			retString+="</item>\n";
			return retString;
		}
	}
	
	public static void printHasDistinct(QueryData qData){
		 out.println("<item text=\"Distinct Present\" open=\"1\" id=\""+ idCounter++ +"\">");
		if(qData.hasDistinct){
			 out.println(spaceTab+"<item text=\"True\" id=\""+ idCounter++ +"\"/>");
		 }
		else{
			out.println(spaceTab+"<item text=\"False\" id=\""+ idCounter++ +"\"/>");
		}
		 out.println("</item>");
	}
	
	public static Set<Node> toSetOfNodes(List<Node> nodes){
		Set<Node> tempSet=new HashSet<Node>();
		if(nodes!=null){
			for(Node n:nodes)
				tempSet.add(n);
		}
		return tempSet;
	}
	
	public static parsing.Node cloneNodeForXMLserialization(parsing.Node m) throws CloneNotSupportedException{
		parsing.Node n=m.clone();
		if(n!=null&&n.getOperator()!=null){
			if(n.getOperator().equals("<")){
				n.setOperator("&lt;");
			}
			else if(n.getOperator().equals("<=")){
				n.setOperator("&lt;=");
			}
		}
		return n;
		
	}
	
	public static String getJoinConditionsString(QueryStructure qData, boolean openFlag) throws CloneNotSupportedException{
		if(openFlag){
		 String retString="<item text=\"Join Conditions\" open=\"1\" id=\""+ idCounter++ +"\">\n";
		 retString+="<item text=\"Outer\" open=\"1\" id=\""+ idCounter++ +"\">\n";
		 for(parsing.Node n:toSetOfNodes(qData.getLstJoinConditions())){
			 if(n.getJoinType()!=null&&(n.getJoinType().equals(JoinClauseInfo.leftOuterJoin)
					 ||n.getJoinType().equals(JoinClauseInfo.rightOuterJoin)
					 ||n.getJoinType().equals(JoinClauseInfo.fullOuterJoin)))
				 retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
		 }
		 retString+="</item>\n";
		 retString+="<item text=\"Inner\" open=\"1\" id=\""+ idCounter++ +"\">\n";
		 for(parsing.Node n:toSetOfNodes(qData.getLstJoinConditions())){
			 if(n.getJoinType()!=null&& !n.getJoinType().equals(JoinClauseInfo.leftOuterJoin)
					 && !n.getJoinType().equals(JoinClauseInfo.rightOuterJoin)
					 && !n.getJoinType().equals(JoinClauseInfo.fullOuterJoin))

				 retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
		 }
		 retString+="</item>\n";
		 retString+="</item>\n";
		 return retString;
		}
		else{
			String retString="<item text=\"Join Conditions\"  id=\""+ idCounter++ +"\">\n";
			 retString+="<item text=\"Outer\"  id=\""+ idCounter++ +"\">\n";
			 for(parsing.Node n:toSetOfNodes(qData.getLstJoinConditions())){
				 if(n.getJoinType()!=null&&(n.getJoinType().equals(JoinClauseInfo.leftOuterJoin)
						 ||n.getJoinType().equals(JoinClauseInfo.rightOuterJoin)
						 ||n.getJoinType().equals(JoinClauseInfo.fullOuterJoin)))
					 retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
			 }
			 retString+="</item>\n";
			 retString+="<item text=\"Inner\"  id=\""+ idCounter++ +"\">\n";
			 for(parsing.Node n:toSetOfNodes(qData.getLstJoinConditions())){
				 if(n.getJoinType()!=null&& !n.getJoinType().equals(JoinClauseInfo.leftOuterJoin)
						 && !n.getJoinType().equals(JoinClauseInfo.rightOuterJoin)
						 && !n.getJoinType().equals(JoinClauseInfo.fullOuterJoin))

					 retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
			 }
			 retString+="</item>\n";
			 retString+="</item>\n";
			 return retString;
		}
	}
	
	public static void printJoinConditions(QueryData qData) throws CloneNotSupportedException{
		 out.println("<item text=\"Join Conditions\" open=\"1\" id=\""+ idCounter++ +"\">");
		 out.println("<item text=\"Outer\" open=\"1\" id=\""+ idCounter++ +"\">");
		 for(parsing.Node n:toSetOfNodes(qData.getJoinConditions())){
			 if(n.getJoinType().equals(JoinClauseInfo.leftOuterJoin)
					 ||n.getJoinType().equals(JoinClauseInfo.rightOuterJoin)
					 ||n.getJoinType().equals(JoinClauseInfo.fullOuterJoin))
			 out.println(spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>");
		 }
		 out.println("</item>");
		 out.println("<item text=\"Inner\" open=\"1\" id=\""+ idCounter++ +"\">");
		 for(parsing.Node n:toSetOfNodes(qData.getJoinConditions())){
			 if(!n.getJoinType().equals(JoinClauseInfo.leftOuterJoin)
					 && !n.getJoinType().equals(JoinClauseInfo.rightOuterJoin)
					 && !n.getJoinType().equals(JoinClauseInfo.fullOuterJoin))

			 out.println(spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>");
		 }
		 out.println("</item>");
		 out.println("</item>");
	}

	public static int getSubQueryIndex(Vector<QueryStructure> qStructVector, QueryStructure subQueryStruct){
		int i=1;
		for(QueryStructure qStructure:qStructVector){
			if(qStructure.equals(subQueryStruct)){
				return i;
			}
			i++;
		}
		return -1;
	}
	
	public static String getSubqueryConditionsString(QueryStructure qData, boolean openFlag) throws  CloneNotSupportedException{
		if(openFlag){
		 String retString="<item text=\"Subquery Conditions\" open=\"1\" id=\""+ idCounter++ +"\">\n";
		 for(parsing.Node n:toSetOfNodes(qData.getAllSubQueryConds())){
			 if(n!=null && n.getType()!=null){
				 if(n.getType().equals(Node.getInNodeType())||n.getType().equals(Node.getNotInNodeType())){
					 if(n.getLeft()!=null&&n.getRight()!=null){
					 if(n.getRight().getSubQueryParser()!=null){
						 String projCol="."+n.getRight().getSubQueryParser().projectedCols.get(0).getColumn().getColumnName();
						 int index=getSubQueryIndex(qData.getWhereClauseSubqueries(),n.getRight().getSubQueryParser());
						 retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n.getLeft()).toString()+ " "+n.getType()+" "+ "subquery"+index+projCol +"\" id=\""+ idCounter++ +"\"/>\n";
					 }
					 else if(n.getLeft().getSubQueryParser()!=null){
						 String projCol="."+n.getLeft().getSubQueryParser().projectedCols.get(0).getColumn().getColumnName();
						 int index=getSubQueryIndex(qData.getWhereClauseSubqueries(),n.getLeft().getSubQueryParser());
						 retString+=spaceTab+"<item text=\""+"subquery"+index+projCol+ " "+n.getType()+" "+ cloneNodeForXMLserialization(n.getRight()).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
					 }
					 }
				 }
				 else if(n.getType().equals(Node.getBroNodeSubQType())){
					 if(n.getLeft()!=null&&n.getRight()!=null){
						 if(n.getRight().getSubQueryParser()!=null){
							 int index=getSubQueryIndex(qData.getWhereClauseSubqueries(),n.getRight().getSubQueryParser());
							 retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n.getLeft()).toString()+ " "+n.getOperator()+" "+n.getRight().getType()+ " subquery"+index +"\" id=\""+ idCounter++ +"\"/>\n";
						 }
						 else if(n.getLeft().getSubQueryParser()!=null){
							 int index=getSubQueryIndex(qData.getWhereClauseSubqueries(),n.getLeft().getSubQueryParser());
							 retString+=spaceTab+"<item text=\""+"subquery"+index+ " "+n.getLeft().getType()+" "+n.getOperator()+" "+ cloneNodeForXMLserialization(n.getRight()).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
						 }						 
					 }					 
				 }
				 else if(n.getType().equalsIgnoreCase(Node.getExistsNodeType())||n.getType().equalsIgnoreCase(Node.getNotExistsNodeType())){
					 if(n.getSubQueryParser()!=null){
						 int index=getSubQueryIndex(qData.getWhereClauseSubqueries(),n.getSubQueryParser());
						 retString+=spaceTab+"<item text=\""+ n.getType()+ " "+ "subquery"+index +"\" id=\""+ idCounter++ +"\"/>\n";

					 }
				 }
				 
			 }
		 }
		 retString+="</item>\n";
		 return retString;
		}
		else{
			String retString="<item text=\"Subquery Conditions\"  id=\""+ idCounter++ +"\">\n";
			 for(parsing.Node n:toSetOfNodes(qData.getAllSubQueryConds())){
				 retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
			 }
			 retString+="</item>\n";
			 return retString;
		}
	}
	
	public static String getSelectionConditionsString(QueryStructure qData, boolean openFlag) throws  CloneNotSupportedException{
		if(openFlag){
		 String retString="<item text=\"Selection Conditions\" open=\"1\" id=\""+ idCounter++ +"\">\n";
		 for(parsing.Node n:toSetOfNodes(qData.getLstSelectionConditions())){
			 retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
		 }
		 retString+="</item>\n";
		 return retString;
		}
		else{
			String retString="<item text=\"Selection Conditions\"  id=\""+ idCounter++ +"\">\n";
			 for(parsing.Node n:toSetOfNodes(qData.getLstSelectionConditions())){
				 retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
			 }
			 retString+="</item>\n";
			 return retString;
		}
	}
	
	public static void printSelectionConditions(QueryData qData) throws  CloneNotSupportedException{
		 out.println("<item text=\"Selection Conditions\" open=\"1\" id=\""+ idCounter++ +"\">");
		 for(parsing.Node n:toSetOfNodes(qData.getSelectionConditions())){
			 out.println(spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>");
		 }
		 out.println("</item>");
	}
	
	public static String getSubqueryConnectivesString(QueryStructure qData, boolean openFlag){
		if(openFlag){
		String retString="<item text=\"Subquery Connectives\" open=\"1\" id=\""+ idCounter++ +"\">\n";
		 for(String str:qData.getLstSubQConnectives()){
			 retString+=spaceTab+"<item text=\""+ str +"\" id=\""+ idCounter++ +"\"/>\n";
		 }
		 retString+="</item>\n";
		 return retString;
		}
		else{
			String retString="<item text=\"Subquery Connectives\" id=\""+ idCounter++ +"\">\n";
			 for(String str:qData.getLstSubQConnectives()){
				 retString+=spaceTab+"<item text=\""+ str +"\" id=\""+ idCounter++ +"\"/>\n";
			 }
			 retString+="</item>\n";
			 return retString;
		}
	}
	
	public static void printSubqueryConnectives(QueryData qData){
		
		 out.println("<item text=\"Subquery Connectives\" open=\"1\" id=\""+ idCounter++ +"\">");
		 for(String str:qData.getSubQConnectives()){
			 out.println(spaceTab+"<item text=\""+ str +"\" id=\""+ idCounter++ +"\"/>");
		 }
		 out.println("</item>");
	}
	
	public static String getHavingConditionsString(QueryStructure qData, boolean openFlag) throws CloneNotSupportedException{
		if(openFlag){
		String retString="<item text=\"Having Conditions\" open=\"1\" id=\""+ idCounter++ +"\">\n";
		 for(parsing.Node n:toSetOfNodes(qData.getLstHavingConditions())){
			 retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
		 }
		 retString+="</item>\n";
		 return retString;
		}
		else{
			String retString="<item text=\"Having Conditions\" id=\""+ idCounter++ +"\">\n";
			 for(parsing.Node n:toSetOfNodes(qData.getLstHavingConditions())){
				 retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
			 }
			 retString+="</item>\n";
			 return retString;
		}
	}
	
	public static void printHavingConditions(QueryData qData) throws CloneNotSupportedException{
		 out.println("<item text=\"Having Conditions\" open=\"1\" id=\""+ idCounter++ +"\">");
		 for(parsing.Node n:toSetOfNodes(qData.getHavingClause())){
			 out.println(spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>");
		 }
		 out.println("</item>");
	}
	
	public static String getGroupByColumnsString(QueryStructure qData, boolean openFlag) {
		if(openFlag){
		String retString="<item text=\"GroupBy Columns\" open=\"1\" id=\""+ idCounter++ +"\">\n";
		 for(parsing.Node n:toSetOfNodes(qData.getLstGroupByNodes())){
			 retString+=spaceTab+"<item text=\""+ n.toString() +"\" id=\""+ idCounter++ +"\"/>\n";
		 }
		 retString+="</item>\n";
		 return retString;
		}
		else{
			String retString="<item text=\"GroupBy Columns\"  id=\""+ idCounter++ +"\">\n";
			 for(parsing.Node n:toSetOfNodes(qData.getLstGroupByNodes())){
				 retString+=spaceTab+"<item text=\""+ n.toString() +"\" id=\""+ idCounter++ +"\"/>\n";
			 }
			 retString+="</item>\n";
			 return retString;
		}
	}

	
	public static void printGroupByColumns(QueryData qData) {
		 out.println("<item text=\"GroupBy Columns\" open=\"1\" id=\""+ idCounter++ +"\">");
		 for(parsing.Node n:toSetOfNodes(qData.GroupByNodes)){
			 out.println(spaceTab+"<item text=\""+ n.toString() +"\" id=\""+ idCounter++ +"\"/>");
		 }
		 out.println("</item>");
	}
	
	public static String getJoinTablesString(QueryStructure qData, boolean openFlag){
		if(openFlag){
		 String retString="<item text=\"Tables\" open=\"1\" id=\""+ idCounter++ +"\">\n";
		 for(String str:qData.getLstRelations()){
			 retString+=spaceTab+"<item text=\""+ str +"\" id=\""+ idCounter++ +"\"/>\n";
		 }
		 retString+="</item>\n";
		 return retString;
		}
		else{
			String retString="<item text=\"Tables\" id=\""+ idCounter++ +"\">\n";
			 for(String str:qData.getLstRelations()){
				 retString+=spaceTab+"<item text=\""+ str +"\" id=\""+ idCounter++ +"\"/>\n";
			 }
			 retString+="</item>\n";
			 return retString;
		}
	}

	
	public static void printJoinTables(QueryData qData){
		 out.println("<item text=\"Tables\" open=\"1\" id=\""+ idCounter++ +"\">");
		 for(String str:qData.getRelations()){
			 out.println(spaceTab+"<item text=\""+ str +"\" id=\""+ idCounter++ +"\"/>");
		 }
		 out.println("</item>");
	}
	
	public static String getRedundantTablesString(QueryStructure qData, boolean openFlag){
		if(openFlag){
		String retString="<item text=\"Redundant Tables\" open=\"1\" id=\""+ idCounter++ +"\">\n";
		 if(qData.lstRedundantRelations!=null){
			 for(String str:qData.lstRedundantRelations){
				 retString+=(spaceTab+"<item text=\""+ str +"\" id=\""+ idCounter++ +"\"/>\n");
			 }
		 }
		 retString+=("</item>\n");
		 return retString;
		}
		else{
			String retString="<item text=\"Redundant Tables\" id=\""+ idCounter++ +"\">\n";
			 if(qData.lstRedundantRelations!=null){
				 for(String str:qData.lstRedundantRelations){
					 retString+=(spaceTab+"<item text=\""+ str +"\" id=\""+ idCounter++ +"\"/>\n");
				 }
			 }
			 retString+=("</item>\n");
			 return retString;
		}
	}
	
	public static void printRedundantTables(QueryData qData){
		 out.println("<item text=\"Redundant Tables\" open=\"1\" id=\""+ idCounter++ +"\">");
		 if(qData.RedundantRelations!=null){
			 for(String str:qData.RedundantRelations){
				 out.println(spaceTab+"<item text=\""+ str +"\" id=\""+ idCounter++ +"\"/>");
			 }
		 }
		 out.println("</item>");
	}
	
	public static void printHead(){
		out.println("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");
		out.println("<tree id=\"0\">");
	
	}
	
	public static void printTail(){
		out.println("</tree>");
	}

}
