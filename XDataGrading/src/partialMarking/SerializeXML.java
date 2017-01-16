package partialMarking;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
		  qData.reAdjustJoins(); //needed for removing any join conditions from selection conditions
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
	
	public static String getProjectedColumnsString(QueryStructure qStruct,boolean openFlag){
		String retString="";
		if(openFlag){
			if(qStruct.getLstProjectedCols().size()>0){
				retString+="<item text=\"Projected Columns\" open=\"1\" id=\""+ idCounter++ +"\">\n";
				for(parsing.Node n:Util.toSetOfNodes(qStruct.getLstProjectedCols())){
					retString+=spaceTab+"<item text=\""+ n.toString() +"\" id=\""+ idCounter++ +"\"/>\n";
				}
				retString+="</item>\n";
			}
		 return retString;
		}
		else{
			if(qStruct.getLstProjectedCols().size()>0){
				retString+="<item text=\"Projected Columns\" id=\""+ idCounter++ +"\">\n";
				for(parsing.Node n:Util.toSetOfNodes(qStruct.getLstProjectedCols())){
					retString+=spaceTab+"<item text=\""+ n.toString() +"\" id=\""+ idCounter++ +"\"/>\n";
				}
				retString+="</item>\n";
			}
			 return retString;

		}		
	}
	
	public static void printProjectedColumns(QueryData qData){
		 out.println("<item text=\"Projected Columns\" open=\"1\" id=\""+ idCounter++ +"\">");
		 for(parsing.Node n:Util.toSetOfNodes(qData.getProjectionList())){
			 out.println(spaceTab+"<item text=\""+ n.toString() +"\" id=\""+ idCounter++ +"\"/>");
		 }
		 out.println("</item>");
	}
	
	public static String getFromSubqueriesString(QueryStructure qStruct, boolean openFlag) throws IOException, CloneNotSupportedException{
		String retString="";
		if(openFlag){
			if(qStruct.getFromClauseSubqueries().size()>0){
				retString+="<item text=\"From Subqueries\" open=\"1\" id=\""+ idCounter++ +"\">\n";
				int i=1;
				for(QueryStructure subquery:qStruct.getFromClauseSubqueries()){
					retString+=spaceTab+"<item text=\"fromSubquery"+i+"\" id=\""+ idCounter++ +"\">\n";
					retString+=getQueryStructureString(subquery,false);
					retString+=spaceTab+"</item>\n";
					i++;
				}
				retString+="</item>\n";
			}
			return retString;

		}
		else{
			if(qStruct.getFromClauseSubqueries().size()>0){
				retString+="<item text=\"From Subqueries\" id=\""+ idCounter++ +"\">\n";
				int i=1;
				for(QueryStructure subquery:qStruct.getFromClauseSubqueries()){
					retString+=spaceTab+"<item text=\"fromSubquery"+i+"\" id=\""+ idCounter++ +"\">\n";
					retString+=getQueryStructureString(subquery,false);
					retString+=spaceTab+"</item>\n";
					i++;
				}
				retString+="</item>\n";
			}
			return retString;

		}
	}
	
	public static String getWhereSubqueriesString(QueryStructure qStruct, boolean openFlag) throws IOException, CloneNotSupportedException{
		String retString="";
		if(openFlag){
			if(qStruct.getWhereClauseSubqueries().size()>0){
				retString+="<item text=\"Where Subqueries\" open=\"1\" id=\""+ idCounter++ +"\">\n";
				int i=1;
				for(QueryStructure subquery:qStruct.getWhereClauseSubqueries()){
					retString+=spaceTab+"<item text=\"whereSubquery"+i+"\" id=\""+ idCounter++ +"\">\n";
					retString+=getQueryStructureString(subquery,false);
					retString+=spaceTab+"</item>\n";
					i++;
				}
				retString+="</item>\n";
			}
		 return retString;
		}
		else{
			if(qStruct.getWhereClauseSubqueries().size()>0){
				retString+="<item text=\"Where Subqueries\"  id=\""+ idCounter++ +"\">\n";
				int i=1;
				for(QueryStructure subquery:qStruct.getWhereClauseSubqueries()){
					retString+=spaceTab+"<item text=\"whereSubquery"+i+"\" id=\""+ idCounter++ +"\">\n";
					retString+=getQueryStructureString(subquery,false);
					retString+=spaceTab+"</item>\n";
					i++;
				}
				retString+="</item>\n";
			}
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
	

	
	public static parsing.Node cloneNodeForXMLserialization(parsing.Node m) throws CloneNotSupportedException{
		parsing.Node n=m.clone();
		
		if(n!=null&&n.getOperator()!=null){
			if(n.getOperator().equals("<")){
				n.setOperator("&lt;");
			}
			else if(n.getOperator().equals("<=")){
				n.setOperator("&lt;=");
			}
			else if(n.getOperator().equals("&&")){
				n.setOperator("&amp;&amp;");
			}
		}
		return n;
		
	}
	
	public static String getJoinConditionsString(QueryStructure qStruct, boolean openFlag) throws CloneNotSupportedException{
		 String retString="";
		if(openFlag){
			if(qStruct.getLstSelectionConditions().size()>0){
				retString+="<item text=\"Join Conditions\" open=\"1\" id=\""+ idCounter++ +"\">\n";
				if(hasOuterJoinCondition(qStruct.getLstJoinConditions())){
					retString+="<item text=\"Outer\" open=\"1\" id=\""+ idCounter++ +"\">\n";
					for(parsing.Node n:Util.toSetOfNodes(qStruct.getLstJoinConditions())){
						if(n.getJoinType()!=null&&(n.getJoinType().equals(JoinClauseInfo.leftOuterJoin)
								||n.getJoinType().equals(JoinClauseInfo.rightOuterJoin)
								||n.getJoinType().equals(JoinClauseInfo.fullOuterJoin)))
							retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
					}
					retString+="</item>\n";
				}
				if(hasInnerJoinCondition(qStruct.getLstJoinConditions())){
					retString+="<item text=\"Inner\" open=\"1\" id=\""+ idCounter++ +"\">\n";
					for(parsing.Node n:Util.toSetOfNodes(qStruct.getLstJoinConditions())){
						if(n.getJoinType()!=null&& !n.getJoinType().equals(JoinClauseInfo.leftOuterJoin)
								&& !n.getJoinType().equals(JoinClauseInfo.rightOuterJoin)
								&& !n.getJoinType().equals(JoinClauseInfo.fullOuterJoin))

							retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
					}
					retString+="</item>\n";
				}
				retString+="</item>\n";
			}
		 return retString;
		}
		else{
			if(qStruct.getLstSelectionConditions().size()>0){
				retString+="<item text=\"Join Conditions\"  id=\""+ idCounter++ +"\">\n";
				if(hasOuterJoinCondition(qStruct.getLstJoinConditions())){
					retString+="<item text=\"Outer\"  id=\""+ idCounter++ +"\">\n";
					for(parsing.Node n:Util.toSetOfNodes(qStruct.getLstJoinConditions())){
						if(n.getJoinType()!=null&&(n.getJoinType().equals(JoinClauseInfo.leftOuterJoin)
								||n.getJoinType().equals(JoinClauseInfo.rightOuterJoin)
								||n.getJoinType().equals(JoinClauseInfo.fullOuterJoin)))
							retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
					}
					retString+="</item>\n";
				}
				if(hasInnerJoinCondition(qStruct.getLstJoinConditions())){
					retString+="<item text=\"Inner\"  id=\""+ idCounter++ +"\">\n";
					for(parsing.Node n:Util.toSetOfNodes(qStruct.getLstJoinConditions())){
						if(n.getJoinType()!=null&& !n.getJoinType().equals(JoinClauseInfo.leftOuterJoin)
								&& !n.getJoinType().equals(JoinClauseInfo.rightOuterJoin)
								&& !n.getJoinType().equals(JoinClauseInfo.fullOuterJoin))
							retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
					}
					retString+="</item>\n";
				}
				retString+="</item>\n";
			}
			 return retString;
		}
	}
	
	public static boolean hasOuterJoinCondition(ArrayList<Node> joinConditions){
		for(parsing.Node n:joinConditions){
			if(n.getJoinType()!=null&&(n.getJoinType().equals(JoinClauseInfo.leftOuterJoin)
					||n.getJoinType().equals(JoinClauseInfo.rightOuterJoin)
					||n.getJoinType().equals(JoinClauseInfo.fullOuterJoin)))
				return true;
		}
		return false;
	}
	
	public static boolean hasInnerJoinCondition(ArrayList<Node> joinConditions){
		for(parsing.Node n:joinConditions){
			if(n.getJoinType()!=null&& !n.getJoinType().equals(JoinClauseInfo.leftOuterJoin)
					&&!n.getJoinType().equals(JoinClauseInfo.rightOuterJoin)
					&&!n.getJoinType().equals(JoinClauseInfo.fullOuterJoin))
				return true;
		}
		return false;
	}
	
	public static void printJoinConditions(QueryData qData) throws CloneNotSupportedException{
		 out.println("<item text=\"Join Conditions\" open=\"1\" id=\""+ idCounter++ +"\">");
		 out.println("<item text=\"Outer\" open=\"1\" id=\""+ idCounter++ +"\">");
		 for(parsing.Node n:Util.toSetOfNodes(qData.getJoinConditions())){
			 if(n.getJoinType().equals(JoinClauseInfo.leftOuterJoin)
					 ||n.getJoinType().equals(JoinClauseInfo.rightOuterJoin)
					 ||n.getJoinType().equals(JoinClauseInfo.fullOuterJoin))
			 out.println(spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>");
		 }
		 out.println("</item>");
		 out.println("<item text=\"Inner\" open=\"1\" id=\""+ idCounter++ +"\">");
		 for(parsing.Node n:Util.toSetOfNodes(qData.getJoinConditions())){
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
	
	public static String getSubqueryConditionsString(QueryStructure qStruct, boolean openFlag) throws  CloneNotSupportedException{
		String retString="";
		if(openFlag){
			if(qStruct.getAllSubQueryConds().size()>0){
				retString+="<item text=\"Subquery Conditions\" open=\"1\" id=\""+ idCounter++ +"\">\n";
				for(parsing.Node n:Util.toSetOfNodes(qStruct.getAllSubQueryConds())){
					if(n!=null && n.getType()!=null){
						if(n.getType().equals(Node.getInNodeType())||n.getType().equals(Node.getNotInNodeType())){
							if(n.getLeft()!=null&&n.getRight()!=null){
								if(n.getRight().getSubQueryStructure()!=null){
									String projCol="."+n.getRight().getSubQueryStructure().projectedCols.get(0).getColumn().getColumnName();
									int index=getSubQueryIndex(qStruct.getWhereClauseSubqueries(),n.getRight().getSubQueryStructure());
									retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n.getLeft()).toString()+ " "+n.getType()+" "+ "subquery"+index+projCol +"\" id=\""+ idCounter++ +"\"/>\n";
								}
								else if(n.getLeft().getSubQueryStructure()!=null){
									String projCol="."+n.getLeft().getSubQueryStructure().projectedCols.get(0).getColumn().getColumnName();
									int index=getSubQueryIndex(qStruct.getWhereClauseSubqueries(),n.getLeft().getSubQueryStructure());
									retString+=spaceTab+"<item text=\""+"subquery"+index+projCol+ " "+n.getType()+" "+ cloneNodeForXMLserialization(n.getRight()).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
								}
							}
						}
						else if(n.getType().equals(Node.getBroNodeSubQType())){
							if(n.getLeft()!=null&&n.getRight()!=null){
								if(n.getRight().getSubQueryStructure()!=null){
									String projCol="."+n.getRight().getSubQueryStructure().projectedCols.get(0).getColumn().getColumnName();
									int index=getSubQueryIndex(qStruct.getWhereClauseSubqueries(),n.getRight().getSubQueryStructure());
									retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n.getLeft()).toString()+ " "+n.getOperator()+" "+n.getRight().getType()+ " subquery"+index +projCol+"\" id=\""+ idCounter++ +"\"/>\n";
								}
								else if(n.getLeft().getSubQueryStructure()!=null){
									String projCol="."+n.getLeft().getSubQueryStructure().projectedCols.get(0).getColumn().getColumnName();
									int index=getSubQueryIndex(qStruct.getWhereClauseSubqueries(),n.getLeft().getSubQueryStructure());
									retString+=spaceTab+"<item text=\""+"subquery"+index+projCol+ " "+n.getLeft().getType()+" "+n.getOperator()+" "+ cloneNodeForXMLserialization(n.getRight()).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
								}						 
							}					 
						}
						else if(n.getType().equalsIgnoreCase(Node.getExistsNodeType())||n.getType().equalsIgnoreCase(Node.getNotExistsNodeType())){
							if(n.getSubQueryStructure()!=null){
								int index=getSubQueryIndex(qStruct.getWhereClauseSubqueries(),n.getSubQueryStructure());
								retString+=spaceTab+"<item text=\""+ n.getType()+ " "+ "subquery"+index +"\" id=\""+ idCounter++ +"\"/>\n";

							}
						}

					}
				}
				retString+="</item>\n";
			}
		 return retString;
		}
		else{
			if(qStruct.getAllSubQueryConds().size()>0){
				retString+="<item text=\"Subquery Conditions\"  id=\""+ idCounter++ +"\">\n";
				for(parsing.Node n:Util.toSetOfNodes(qStruct.getAllSubQueryConds())){
					if(n!=null && n.getType()!=null){
						if(n.getType().equals(Node.getInNodeType())||n.getType().equals(Node.getNotInNodeType())){
							if(n.getLeft()!=null&&n.getRight()!=null){
								if(n.getRight().getSubQueryStructure()!=null){
									String projCol="."+n.getRight().getSubQueryStructure().projectedCols.get(0).getColumn().getColumnName();
									int index=getSubQueryIndex(qStruct.getWhereClauseSubqueries(),n.getRight().getSubQueryStructure());
									retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n.getLeft()).toString()+ " "+n.getType()+" "+ "subquery"+index+projCol +"\" id=\""+ idCounter++ +"\"/>\n";
								}
								else if(n.getLeft().getSubQueryStructure()!=null){
									String projCol="."+n.getLeft().getSubQueryStructure().projectedCols.get(0).getColumn().getColumnName();
									int index=getSubQueryIndex(qStruct.getWhereClauseSubqueries(),n.getLeft().getSubQueryStructure());
									retString+=spaceTab+"<item text=\""+"subquery"+index+projCol+ " "+n.getType()+" "+ cloneNodeForXMLserialization(n.getRight()).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
								}
							}
						}
						else if(n.getType().equals(Node.getBroNodeSubQType())){
							if(n.getLeft()!=null&&n.getRight()!=null){
								if(n.getRight().getSubQueryStructure()!=null){
									int index=getSubQueryIndex(qStruct.getWhereClauseSubqueries(),n.getRight().getSubQueryStructure());
									retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n.getLeft()).toString()+ " "+n.getOperator()+" "+n.getRight().getType()+ " subquery"+index +"\" id=\""+ idCounter++ +"\"/>\n";
								}
								else if(n.getLeft().getSubQueryStructure()!=null){
									int index=getSubQueryIndex(qStruct.getWhereClauseSubqueries(),n.getLeft().getSubQueryStructure());
									retString+=spaceTab+"<item text=\""+"subquery"+index+ " "+n.getLeft().getType()+" "+n.getOperator()+" "+ cloneNodeForXMLserialization(n.getRight()).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
								}						 
							}					 
						}
						else if(n.getType().equalsIgnoreCase(Node.getExistsNodeType())||n.getType().equalsIgnoreCase(Node.getNotExistsNodeType())){
							if(n.getSubQueryStructure()!=null){
								int index=getSubQueryIndex(qStruct.getWhereClauseSubqueries(),n.getSubQueryStructure());
								retString+=spaceTab+"<item text=\""+ n.getType()+ " "+ "subquery"+index +"\" id=\""+ idCounter++ +"\"/>\n";
							}
						}

					}
				}
				retString+="</item>\n";
			}
			 return retString;
		}
	}
	
	public static String getSelectionConditionsString(QueryStructure qStruct, boolean openFlag) throws  CloneNotSupportedException{
		 String retString="";
		if(openFlag){
			if(qStruct.getLstSelectionConditions().size()>0){
				retString+="<item text=\"Selection Conditions\" open=\"1\" id=\""+ idCounter++ +"\">\n";
				for(parsing.Node n:Util.toSetOfNodes(qStruct.getLstSelectionConditions())){
					retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
				}
				retString+="</item>\n";
			}
			return retString;
		}
		else{
			if(qStruct.getLstSelectionConditions().size()>0){
				retString+="<item text=\"Selection Conditions\"  id=\""+ idCounter++ +"\">\n";
				for(parsing.Node n:Util.toSetOfNodes(qStruct.getLstSelectionConditions())){
					retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
				}
				retString+="</item>\n";
			}
			return retString;
		}
	}
	
	public static void printSelectionConditions(QueryData qData) throws  CloneNotSupportedException{
		 out.println("<item text=\"Selection Conditions\" open=\"1\" id=\""+ idCounter++ +"\">");
		 for(parsing.Node n:Util.toSetOfNodes(qData.getSelectionConditions())){
			 out.println(spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>");
		 }
		 out.println("</item>");
	}
	
	public static String getSubqueryConnectivesString(QueryStructure qStruct, boolean openFlag){
		String retString="";
		if(openFlag){
			if(qStruct.getLstSubQConnectives().size()>0){
				retString+="<item text=\"Subquery Connectives\" open=\"1\" id=\""+ idCounter++ +"\">\n";
				for(String str:qStruct.getLstSubQConnectives()){
					retString+=spaceTab+"<item text=\""+ str +"\" id=\""+ idCounter++ +"\"/>\n";
				}
				retString+="</item>\n";
			}
			return retString;
		}
		else{
			if(qStruct.getLstSubQConnectives().size()>0){
				retString+="<item text=\"Subquery Connectives\" id=\""+ idCounter++ +"\">\n";
				for(String str:qStruct.getLstSubQConnectives()){
					retString+=spaceTab+"<item text=\""+ str +"\" id=\""+ idCounter++ +"\"/>\n";
				}
				retString+="</item>\n";
			}
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
	
	public static String getHavingConditionsString(QueryStructure qStruct, boolean openFlag) throws CloneNotSupportedException{
		String retString="";
		if(openFlag){
			if(qStruct.getLstHavingConditions().size()>0){
				retString+="<item text=\"Having Conditions\" open=\"1\" id=\""+ idCounter++ +"\">\n";
				for(parsing.Node n:Util.toSetOfNodes(qStruct.getLstHavingConditions())){
					retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
				}
				retString+="</item>\n";
			}
			return retString;
		}
		else{
			if(qStruct.getLstHavingConditions().size()>0){
				retString+="<item text=\"Having Conditions\" id=\""+ idCounter++ +"\">\n";
				for(parsing.Node n:Util.toSetOfNodes(qStruct.getLstHavingConditions())){
					retString+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
				}
				retString+="</item>\n";
			}
			 return retString;
		}
	}
	
	public static void printHavingConditions(QueryData qData) throws CloneNotSupportedException{
		 out.println("<item text=\"Having Conditions\" open=\"1\" id=\""+ idCounter++ +"\">");
		 for(parsing.Node n:Util.toSetOfNodes(qData.getHavingClause())){
			 out.println(spaceTab+"<item text=\""+ cloneNodeForXMLserialization(n).toString() +"\" id=\""+ idCounter++ +"\"/>");
		 }
		 out.println("</item>");
	}
	
	public static String getGroupByColumnsString(QueryStructure qStruct, boolean openFlag) {
		String retString="";
		if(openFlag){
			if(qStruct.getLstGroupByNodes().size()>0){
				retString+="<item text=\"GroupBy Columns\" open=\"1\" id=\""+ idCounter++ +"\">\n";
				for(parsing.Node n:Util.toSetOfNodes(qStruct.getLstGroupByNodes())){
					retString+=spaceTab+"<item text=\""+ n.toString() +"\" id=\""+ idCounter++ +"\"/>\n";
				}
				retString+="</item>\n";
			}
			return retString;
		}
		else{
			if(qStruct.getLstGroupByNodes().size()>0){
				retString+="<item text=\"GroupBy Columns\"  id=\""+ idCounter++ +"\">\n";
				for(parsing.Node n:Util.toSetOfNodes(qStruct.getLstGroupByNodes())){
					retString+=spaceTab+"<item text=\""+ n.toString() +"\" id=\""+ idCounter++ +"\"/>\n";
				}
				retString+="</item>\n";
			}
			return retString;
		}
	}

	
	public static void printGroupByColumns(QueryData qData) {
		 out.println("<item text=\"GroupBy Columns\" open=\"1\" id=\""+ idCounter++ +"\">");
		 for(parsing.Node n:Util.toSetOfNodes(qData.GroupByNodes)){
			 out.println(spaceTab+"<item text=\""+ n.toString() +"\" id=\""+ idCounter++ +"\"/>");
		 }
		 out.println("</item>");
	}
	
	public static String getJoinTablesString(QueryStructure qStruct, boolean openFlag){
		String retString="";
		if(openFlag){
			if(qStruct.getLstRelationInstances().size()>0){
				retString+="<item text=\"Table Instances\" open=\"1\" id=\""+ idCounter++ +"\">\n";
				for(String str:qStruct.getLstRelationInstances()){
					retString+=spaceTab+"<item text=\""+ str +"\" id=\""+ idCounter++ +"\"/>\n";
				}
				retString+="</item>\n";
			}
			return retString;
		}
		else{
			if(qStruct.getLstRelationInstances().size()>0){

				retString+="<item text=\"Table Instances\" id=\""+ idCounter++ +"\">\n";
				for(String str:qStruct.getLstRelationInstances()){
					retString+=spaceTab+"<item text=\""+ str +"\" id=\""+ idCounter++ +"\"/>\n";
				}
				retString+="</item>\n";
			}
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
	
	public static String getRedundantTablesString(QueryStructure qStruct, boolean openFlag){
		String retString="";
		if(openFlag){
			if(qStruct.lstRedundantRelations.size()>0){
				retString+="<item text=\"Redundant Tables\" open=\"1\" id=\""+ idCounter++ +"\">\n";
				if(qStruct.lstRedundantRelations!=null){
					for(String str:qStruct.lstRedundantRelations){
						retString+=(spaceTab+"<item text=\""+ str +"\" id=\""+ idCounter++ +"\"/>\n");
					}
				}
				retString+=("</item>\n");
			}
			return retString;
		}
		else{
			if(qStruct.lstRedundantRelations.size()>0){
				retString+="<item text=\"Redundant Tables\" id=\""+ idCounter++ +"\">\n";
				if(qStruct.lstRedundantRelations!=null){
					for(String str:qStruct.lstRedundantRelations){
						retString+=(spaceTab+"<item text=\""+ str +"\" id=\""+ idCounter++ +"\"/>\n");
					}
				}
				retString+=("</item>\n");
			}
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
