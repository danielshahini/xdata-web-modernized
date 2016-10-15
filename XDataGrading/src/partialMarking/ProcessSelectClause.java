/** @author mathew
 * 
 */
 
package partialMarking;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.derby.impl.sql.compile.ColumnReference;

import generateConstraints.UtilsRelatedToNode;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.DoubleAnd;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import parsing.AggregateFunction;
import parsing.CaseCondition;
import parsing.Conjunct;
import parsing.FromListElement;
import parsing.Node;
import parsing.ProcessResultSetNode;
import parsing.Query;
import parsing.QueryParser;
import parsing.Table;
import parsing.Util;
import parsing.WhereClauseVectorJSQL;
import partialMarking.QueryStructure;

public class ProcessSelectClause {
	private static Logger logger = Logger.getLogger(ProcessSelectClause.class.getName());
	
	
	/** @author mathew on 1st october 2016 
	 * 
	 * 
	 * @param plainSelect
	 * @param debug
	 * @param qParser
	 * @throws Exception
	 * 
	 * processes the  plainSelect clause, calls the respective methods for processing its components/sub-clauses, which 
	 * in turn stores the objects that represent the sub-clauses  in the appropriate data structures in the 
	 * qParser object (3rd argument) . 
	 * 
	 */

	public static void ProcessSelect(PlainSelect plainSelect, boolean debug, QueryStructure qParser) throws Exception {
		logger.info("processing select query"+plainSelect.toString());
		Vector<Node> joinConditions=new Vector<Node>();
		ProcessSelectClause.processFromClause(plainSelect,qParser,joinConditions);
		for(Node n:joinConditions){
			logger.info("joinCondition "+n);
		}
//		display(qParser.fromListElements);

		if(plainSelect.getDistinct()!=null){
			qParser.setIsDistinct(true);
		}
		ProcessSelectClause.processWhereClause(plainSelect,qParser);
		
		if(!joinConditions.isEmpty()){
			if(!qParser.allConds.isEmpty()) {
				Vector<Node> allCondsDups=(Vector<Node>) qParser.allConds.clone();
				Node NewCond = new Node();
				NewCond.setType(Node.getAndNodeType());

				NewCond.setLeft(ProcessResultSetNode.getHierarchyOfJoinNode(joinConditions));
				NewCond.setRight(qParser.allConds.get(0));
				allCondsDups.remove(qParser.allConds.get(0));
				allCondsDups.add(NewCond);
				qParser.allConds.removeAllElements();
				qParser.allConds.addAll(allCondsDups);
			}
			else {
				 qParser.allConds.addAll(joinConditions);
			}
		}

		
		ProcessSelectClause.modifyTreeForCompareSubQ(qParser);		
		
		QueryStructure.flattenAndSeparateAllConds(qParser);
				
		for(Conjunct conjunct:qParser.conjuncts)			
			conjunct.createEqClass();
		
		
		for(QueryStructure qp: qParser.getFromClauseSubqueries()){//For From clause subqueries
			
			QueryStructure.flattenAndSeparateAllConds(qp);
			for(Conjunct conjunct:qp.conjuncts){
				conjunct.createEqClass();
			}
		}
		for(QueryStructure qp: qParser.getWhereClauseSubqueries()){//For Where clause subqueries
			
			QueryStructure.flattenAndSeparateAllConds(qp);
			for(Conjunct conjunct:qp.conjuncts){
				conjunct.createEqClass();
			}
		}
		
		Util.foreignKeyClosure(qParser);
		if(qParser.isDeleteNode){
			return;
		}
		
		ProcessSelectClause.processProjectionList(plainSelect,qParser);
		ProcessSelectClause.processGroupByList(plainSelect,qParser);
		ProcessSelectClause.processHavingClause(plainSelect,qParser);
		ProcessSelectClause.processOrderByList(plainSelect,qParser);

		//System.out.println(qParser.toString());
	}
	
	public static void modifyTreeForCompareSubQ(QueryStructure qParser) {
		try{
			for (Node n: qParser.allConds)  // This is not only for outer block 
				Util.modifyTreeForComapreSubQ(n);
	
			for(QueryStructure qp: qParser.getFromClauseSubqueries()){//For From clause subqueries
				for(Node n: qp.allConds)
					Util.modifyTreeForComapreSubQ(n);
			}
	
			for(QueryStructure qp: qParser.getWhereClauseSubqueries()){//For where clause subqueries
				for(Node n: qp.allConds)
					Util.modifyTreeForComapreSubQ(n);
			}
		}catch(Exception e){
			logger.log(Level.SEVERE,"Error in modifyTreeForCompareSubQ : "+e.getMessage(),e);			
		}
	}
	
	
	/**
	 * This method gets the case statements in where part of the query and adds it to query parser.
	 * Removes the where condition from the whereclause so that the where clause predicates are not generated
	 * for the same. 
	 * 
	 * @param whereClause
	 * @param colExpression
	 * @param qParser
	 * @return
	 * @throws Exception
	 */
	public static boolean caseInWhereClause(Expression whereClause, Expression colExpression, QueryStructure qParser, PlainSelect plainSelect) throws Exception{
		
		Vector<CaseCondition> caseConditionsVector = new Vector<CaseCondition>();
		boolean isCaseExpr = false;
		boolean isCaseExists = false;
		try{
		if(whereClause instanceof CaseExpression){
			parsing.Column nodeColumnValue = null;
			 List<Expression> whenClauses = ((CaseExpression) whereClause).getWhenClauses();
			 for(int i=0;i < whenClauses.size();i++ ){
				
				CaseCondition cC = new CaseCondition();
				Node n = processExpression(((WhenClause)((CaseExpression) whereClause).getWhenClauses().get(i)).getWhenExpression(), qParser.fromListElements,qParser,plainSelect);
				cC.setCaseConditionNode(n);
				cC.setCaseCondition(n.toString());
			    cC.setConstantValue(((WhenClause)((CaseExpression) whereClause).getWhenClauses().get(i)).getThenExpression().toString());
			    if(colExpression!= null && colExpression instanceof Column){
			    	Node n1 = ((processExpression((colExpression), qParser.fromListElements,qParser,plainSelect)));
			    	cC.setColValueForConjunct(UtilsRelatedToNode.getColumn(n1));
			    	nodeColumnValue = UtilsRelatedToNode.getColumn(n1);
			    	cC.setCaseOperator("=");
			    }
			    /*if(cC.getColValueForConjunct() == null){
			    	
			    	nodeColumnValue = UtilsRelatedToNode.getColumn(cC.getCaseConditionNode());
			    	
			    	if(cC.getCaseConditionNode() != null && nodeColumnValue != null){
			    		cC.setColValueForConjunct(nodeColumnValue);
			    	}
			    	cC.setCaseOperator("=");
			    }*/
			    caseConditionsVector.add(cC);
			   // qParser.getCaseConditions().add(cC);
			 }
			 isCaseExpr = true;
			 //Add the else clause if present as the last item
			 if(((CaseExpression) whereClause).getElseExpression() != null){
				CaseCondition cC = new CaseCondition();
				//cC.setCaseConditionNode(n);
				cC.setCaseCondition("else");
			    cC.setConstantValue(((CaseExpression) whereClause).getElseExpression().toString());
			    if(colExpression != null && colExpression instanceof Column){
			    	Node n1 = ((processExpression((colExpression), qParser.fromListElements,qParser,plainSelect)));
			    	cC.setColValueForConjunct(UtilsRelatedToNode.getColumn(n1));
			    }
			   /* if(cC.getColValueForConjunct() == null){
			    	
			    	cC.setColValueForConjunct(nodeColumnValue);
			    }*/
			    caseConditionsVector.add(cC);
			 }
			 //Add Case conditions to queryparser
		   qParser.getCaseConditionMap().put(2,caseConditionsVector);
		   return isCaseExpr;
		}
		else if(whereClause instanceof BinaryExpression){
			Expression binaryLeftExp = ((BinaryExpression)whereClause).getLeftExpression();
			Expression binaryRightExp = ((BinaryExpression)whereClause).getRightExpression();
			if(binaryLeftExp != null){
				isCaseExists= caseInWhereClause(binaryLeftExp,binaryRightExp,qParser,plainSelect);
				//If Case stmnt exists, rearrange Where clause to omit CASE condition
				if(isCaseExists){
					((BinaryExpression) whereClause).setLeftExpression(null);
					((BinaryExpression) whereClause).setRightExpression(null);
				}
			}
			
			if(binaryRightExp != null){
				isCaseExists = caseInWhereClause(binaryRightExp,binaryLeftExp,qParser,plainSelect);
				//If Case stmnt exists, rearrange Where clause to omit CASE condition
				if(isCaseExists){
					((BinaryExpression) whereClause).setLeftExpression(null);
					((BinaryExpression) whereClause).setRightExpression(null);
				}
			}	
		}
		else if( whereClause instanceof Parenthesis){
			Expression caseExpr = ((Parenthesis)whereClause).getExpression();
			
			if(caseExpr instanceof CaseExpression){
				isCaseExists = caseInWhereClause(caseExpr,colExpression,qParser,plainSelect);
				//If Case stmnt exists, rearrange Where clause to omit CASE condition
				if(isCaseExists){
					((Parenthesis) whereClause).setExpression(null);
				}
				return isCaseExists;
			}
		}
		return isCaseExpr;
		}catch(Exception e){
			logger.log(Level.SEVERE,"Error in Processing case condition in where clause: "+e.getMessage(),e);
			throw e;
		}
	}
	//Modified for JSQL Exists - Start

	private static void processWhereClause(PlainSelect plainSelect, QueryStructure qParser) throws Exception{
		// TODO Auto-generated method stub
		Expression whereClauseExpression = plainSelect.getWhere();
		if(whereClauseExpression==null)
			return;
		caseInWhereClause(whereClauseExpression,null,qParser,plainSelect);
		Node whereClause=ProcessSelectClause.processJoinExpression(whereClauseExpression,qParser.fromListElements, qParser,plainSelect);
		//System.out.println(" where clause "+whereClause);
		
		if( whereClause != null) 
			qParser.allConds.add(whereClause);
	}

	private static void processHavingClause(PlainSelect plainSelect, QueryStructure qParser) throws Exception{
		// TODO Auto-generated method stub
		// get having clause
		Expression hc = plainSelect.getHaving();
		if(hc==null||hc.toString().isEmpty()){
			qParser.setHavingClause(null);
			return;
		}
		Node havingClause=ProcessSelectClause.processJoinExpression(hc,qParser.fromListElements, qParser,plainSelect);
			qParser.setHavingClause(havingClause);
		logger.info(hc+" having clause "+havingClause);
	}

	
	/** @author mathew on 4th october 2016 
	 * 
	 * 
	 * @param plainSelect
	 * @param qParser
	 * @throws Exception
	 * 
	 * processes the group by list of plainSelect, and stores the elements as column nodes in groupByNodes list in 
	 * qParser object (2nd argument) . If the order by element is an alias or an unqualified column, it resolves the 
	 * table name and column name of the element.
	 * 
	 */

	private static void processGroupByList(PlainSelect plainSelect, QueryStructure qParser) throws Exception{
		// TODO Auto-generated method stub
		if (plainSelect.getGroupByColumnReferences() == null||plainSelect.getGroupByColumnReferences().isEmpty()) 
			return;

		List<Expression> gbl = plainSelect.getGroupByColumnReferences();
		for (int i = 0; i < gbl.size(); i++) {
			Column gbc;
			Expression groupExpression=gbl.get(i);

			if (groupExpression instanceof Column){
				gbc = (Column)groupExpression;
			} else {
				continue;
			}
			
			Node groupByColumn=ProcessSelectClause.processJoinExpression(groupExpression,qParser.fromListElements, qParser,plainSelect);
			if(groupByColumn.getTableNameNo()==null||groupByColumn.getTableNameNo().isEmpty()){
				for(Node n:Util.getAllProjectedColumns(qParser.fromListElements, qParser)){
					if(n.getColumn().getColumnName().equalsIgnoreCase(groupByColumn.getColumn().getColumnName())){
						groupByColumn.setTable(n.getTable());
						groupByColumn.setTableNameNo(n.getTableNameNo());
						break;
					}
				}
			}
			if(groupByColumn.getTableNameNo()==null||groupByColumn.getTableNameNo().isEmpty()){
				List<SelectItem> projectedItems=plainSelect.getSelectItems();
				for(int j=0;j<projectedItems.size();j++){
					SelectItem projectedItem=projectedItems.get(j);
					if(projectedItem instanceof net.sf.jsqlparser.statement.select.SelectExpressionItem){
						SelectExpressionItem selExpItem=(net.sf.jsqlparser.statement.select.SelectExpressionItem)projectedItem;
						Expression e=selExpItem.getExpression();
						if(e instanceof net.sf.jsqlparser.expression.Parenthesis){
							net.sf.jsqlparser.expression.Parenthesis p=(net.sf.jsqlparser.expression.Parenthesis) e;
							e=p.getExpression();
						}
						if(selExpItem.getAlias()!=null){
							if(groupByColumn.getColumn().getColumnName().equalsIgnoreCase(selExpItem.getAlias().getName())){
								groupByColumn =ProcessSelectClause.processJoinExpression(e,qParser.fromListElements, qParser,plainSelect);
								logger.info(groupByColumn+" alias name resolved " +selExpItem.getAlias().getName());
								break;
							}
						}
					}
				}

			}
			qParser.groupByNodes.addElement(groupByColumn);
			logger.info(groupExpression.toString()+ " group by column "+groupByColumn);

		}

	}
	
	/** @author mathew on 1st october 2016 
	 * 
	 * 
	 * @param plainSelect
	 * @param qParser
	 * @throws Exception
	 * 
	 * processes the order by list of plainSelect, and stores the elements as column nodes in orderByElements list in 
	 * qParser object (2nd argument) . If the order by element is an alias or an unqualified column, it resolves the 
	 * table name and column name of the element.
	 * 
	 */

	private static void processOrderByList(PlainSelect plainSelect, QueryStructure qParser) throws Exception {
		if (plainSelect.getOrderByElements() == null||plainSelect.getOrderByElements().isEmpty()) 
			return;

		List<OrderByElement> obl = plainSelect.getOrderByElements();
		for (int i = 0; i < obl.size(); i++) {
			Column obc;
			Expression orderExpression=obl.get(i).getExpression();

			if (orderExpression instanceof Column){
				obc = (Column)orderExpression;
			} else {
				continue;
			}

			Node orderByColumn=ProcessSelectClause.processJoinExpression(orderExpression,qParser.fromListElements, qParser,plainSelect);
			if(orderByColumn.getTableNameNo()==null||orderByColumn.getTableNameNo().isEmpty()){
				for(Node n:Util.getAllProjectedColumns(qParser.fromListElements, qParser)){
					if(n.getColumn().getColumnName().equalsIgnoreCase(orderByColumn.getColumn().getColumnName())){
						orderByColumn.setTable(n.getTable());
						orderByColumn.setTableNameNo(n.getTableNameNo());
						break;
					}
				}
			}
			if(orderByColumn.getTableNameNo()==null||orderByColumn.getTableNameNo().isEmpty()){
				List<SelectItem> projectedItems=plainSelect.getSelectItems();
				for(int j=0;j<projectedItems.size();j++){
					SelectItem projectedItem=projectedItems.get(j);
					if(projectedItem instanceof net.sf.jsqlparser.statement.select.SelectExpressionItem){
						SelectExpressionItem selExpItem=(net.sf.jsqlparser.statement.select.SelectExpressionItem)projectedItem;
						Expression e=selExpItem.getExpression();
						if(e instanceof net.sf.jsqlparser.expression.Parenthesis){
							net.sf.jsqlparser.expression.Parenthesis p=(net.sf.jsqlparser.expression.Parenthesis) e;
							e=p.getExpression();
						}
						if(selExpItem.getAlias()!=null){
							if(orderByColumn.getColumn().getColumnName().equalsIgnoreCase(selExpItem.getAlias().getName())){
								orderByColumn =ProcessSelectClause.processJoinExpression(e,qParser.fromListElements, qParser,plainSelect);
								logger.info(orderByColumn+" alias name resolved " +selExpItem.getAlias().getName());
								break;
							}
						}
					}
				}

			}
			qParser.orderByNodes.addElement(orderByColumn);
			logger.info(orderExpression.toString()+ " order by column "+orderByColumn);
		}

	}

	/** @author mathew on 3rd October 2016
	 * 
	 * @param plainSelect
	 * @param qParser
	 * @param joinConditions
	 * @throws Exception
	 * 
	 * Processes the From Clause of the first argument, plainSelect. From list elements in the from clause are extracted and stored 
	 * respecting their  hierarchy/nestedness in the fromListElements list in the qParser object (2nd argument), also the join conditions involved are 
	 * extracted and stored in the 3rd argument, joinConditions
	 * 
	 */
	private static void processFromClause(PlainSelect plainSelect, QueryStructure qParser, Vector<Node> joinConditions) throws Exception{
		// TODO Auto-generated method stub
		FromItem firstFromItem=plainSelect.getFromItem();

		FromListElement leftFLE=null, rightFLE=null;
		
		
		if(firstFromItem instanceof net.sf.jsqlparser.schema.Table){
			net.sf.jsqlparser.schema.Table jsqlTable=(net.sf.jsqlparser.schema.Table)firstFromItem;
			leftFLE = new FromListElement();
			ProcessSelectClause.processFromListTable(jsqlTable, leftFLE, qParser);
			qParser.fromListElements.addElement(leftFLE);
		}
		else if(firstFromItem instanceof SubJoin){
			SubJoin subJoin=(SubJoin) firstFromItem;
			Join join=subJoin.getJoin();
			//System.out.println(" subJoinAlias "+subJoin.getAlias().getName()+" on Expression"+	join.getOnExpression());			
			Vector<FromListElement> tempElements=new Vector<FromListElement>();
			ProcessSelectClause.processFromListSubJoin(subJoin, tempElements, joinConditions, qParser,plainSelect);
			leftFLE=new FromListElement();
			if(subJoin.getAlias()!=null){
				leftFLE.setAliasName(subJoin.getAlias().getName());
			}
			leftFLE.setTabs(tempElements);
			qParser.fromListElements.addElement(leftFLE);
		}
		else if(firstFromItem instanceof SubSelect){
			SubSelect subSelect=(SubSelect) firstFromItem;
			SelectBody selBody=subSelect.getSelectBody();
			QueryStructure subQueryParser=new QueryStructure(qParser.getTableMap());
			leftFLE=new FromListElement();
			leftFLE.setSubQueryParser(subQueryParser);
			if(subSelect.getAlias()!=null){
				leftFLE.setAliasName(subSelect.getAlias().getName());
			}
			qParser.fromListElements.addElement(leftFLE);					
			ProcessSelectClause.processFromListSubSelect(subSelect,subQueryParser,qParser);

		}
		
		if(plainSelect.getJoins()!=null && !plainSelect.getJoins().isEmpty()){
			for(Join join:plainSelect.getJoins()){

				FromItem fromItem=join.getRightItem();
				if(fromItem instanceof net.sf.jsqlparser.schema.Table){
					net.sf.jsqlparser.schema.Table jsqlTable=(net.sf.jsqlparser.schema.Table)fromItem;
					rightFLE=new FromListElement();
					processFromListTable(jsqlTable, rightFLE, qParser);
					qParser.fromListElements.addElement(rightFLE);
				}
				else if(fromItem instanceof SubJoin){
					SubJoin subJoin=(SubJoin) fromItem;
					Vector<FromListElement> tempElements=new Vector<FromListElement>();
					ProcessSelectClause.processFromListSubJoin(subJoin, tempElements, joinConditions, qParser,plainSelect);
					rightFLE=new FromListElement();
					if(subJoin.getAlias()!=null){
						rightFLE.setAliasName(subJoin.getAlias().getName());
					}				
					rightFLE.setTabs(tempElements);
					qParser.fromListElements.addElement(rightFLE);

				}
				else if(fromItem instanceof SubSelect){
					SubSelect subSelect=(SubSelect) fromItem;					
					QueryStructure subQueryParser=new QueryStructure(qParser.getTableMap());
					rightFLE=new FromListElement();
					rightFLE.setSubQueryParser(subQueryParser);
					if(subSelect.getAlias()!=null){
						rightFLE.setAliasName(subSelect.getAlias().getName());
					}
					qParser.fromListElements.addElement(rightFLE);
					
					processFromListSubSelect(subSelect,subQueryParser,qParser);					

				}
				Expression e=join.getOnExpression();
				if(e!=null){
					Node joinCondition=ProcessSelectClause.processJoinExpression(e,qParser.fromListElements, qParser,plainSelect);
					joinConditions.add(joinCondition);
				}
				leftFLE=rightFLE;//reset leftFLE to the previously visited FLE
			}
		}
	}

	/** @author mathew on 1st october 2016 
	 * 
	 * 
	 * @param plainSelect
	 * @param qParser
	 * @throws Exception
	 * 
	 * processes the projection list of plainSelect, for each element of the project list checks for the various possibilities - 
	 * column expression , or an all column object. In case of the former, check if there is a  case expression is present,
	 * in which case the parser's case condition map is updated with the respective objects from the case condition
	 * 
	 */
	private static void processProjectionList(PlainSelect plainSelect, QueryStructure qParser) throws Exception{
		// TODO Auto-generated method stub
		Vector<CaseCondition> caseConditionsVector = new Vector<CaseCondition>();
		
		List<SelectItem> projectedItems=plainSelect.getSelectItems();
		for(int i=0;i<projectedItems.size();i++){
			SelectItem projectedItem=projectedItems.get(i);
			if(projectedItem instanceof net.sf.jsqlparser.statement.select.AllColumns){
				for(Node n:Util.getAllProjectedColumns(qParser.fromListElements, qParser)){
						logger.info(" all column, select all columns... "+n);
						qParser.projectedCols.add(n);
				}
			}
			else if(projectedItem instanceof net.sf.jsqlparser.statement.select.SelectExpressionItem){
				SelectExpressionItem selExpItem=(net.sf.jsqlparser.statement.select.SelectExpressionItem)projectedItem;
				Expression e=selExpItem.getExpression();
				if(selExpItem.getAlias()!=null){
					logger.info(" alias present " +selExpItem.getAlias().getName());
				}
					
				if(e instanceof net.sf.jsqlparser.expression.Parenthesis){
					net.sf.jsqlparser.expression.Parenthesis p=(net.sf.jsqlparser.expression.Parenthesis) e;
					Expression exp=p.getExpression();
					if(exp instanceof net.sf.jsqlparser.expression.CaseExpression)
						e=exp;
				}
				if(e instanceof net.sf.jsqlparser.expression.CaseExpression){
					
					 List<Expression> whenClauses = ((CaseExpression) e).getWhenClauses();
					 for(int j=0;j < whenClauses.size();j++ ){
						
						CaseCondition cC = new CaseCondition();
						Node n = processExpression(((WhenClause)((CaseExpression) e).getWhenClauses().get(j)).getWhenExpression(), qParser.fromListElements, qParser,plainSelect);
						cC.setCaseConditionNode(n);
						cC.setCaseCondition(n.toString());
					    cC.setConstantValue(((WhenClause)((CaseExpression) e).getWhenClauses().get(j)).getThenExpression().toString());
					    caseConditionsVector.add(cC);
					   // qParser.getCaseConditions().add(cC);
					 }
					 //Add the else clause if present as the last item
					 if(((CaseExpression) e).getElseExpression() != null){
						CaseCondition cC = new CaseCondition();
						//cC.setCaseConditionNode(n);
						cC.setCaseCondition("else");
					    cC.setConstantValue(((CaseExpression) e).getElseExpression().toString());
					    caseConditionsVector.add(cC);
					 }
					 //Add Case conditions to queryparser
				   qParser.getCaseConditionMap().put(1,caseConditionsVector);
				}
				else{
					Node projectedColumn=ProcessSelectClause.processJoinExpression(e,qParser.fromListElements, qParser,plainSelect);
					if(projectedColumn.getAgg()!=null&&selExpItem.getAlias()!=null){
						projectedColumn.getAgg().setAggAliasName(selExpItem.getAlias().getName());
					}
					else if(selExpItem.getAlias()!=null){
						projectedColumn.setAliasName(selExpItem.getAlias().getName());
					}
					qParser.projectedCols.add(projectedColumn);
					logger.info("Select Expression"+projectedItem.toString()+ " "+projectedColumn);

					if(qParser.setOperator==null||qParser.setOperator.isEmpty()){
						if(projectedColumn.getTableNameNo()==null||projectedColumn.getTableNameNo().isEmpty()){
							logger.info(" Column name could not be resolved, query parsing failed, exception thrown, query: "+plainSelect.toString());
							throw new Exception(" Column name could not be resolved, query parsing failed, exception thrown");
						}
					}
					

				}
			}
		}
	}

	public static void display(Vector<FromListElement> visitedFromListElements) {
		for(FromListElement fle:visitedFromListElements){
			if(fle!=null && (fle.getTableName()!=null||fle.getTableNameNo()!=null))
				System.out.println(fle.toString());
			else if(fle!=null && fle.getSubQueryParser()!=null){
				System.out.println(fle.toString());
				display(fle.getSubQueryParser().getFromListElements());
			}
			else if(fle!=null && fle.getTabs()!=null && !fle.getTabs().isEmpty()){
				System.out.println(fle.toString());
				display(fle.getTabs());				
			}
	
		}
	}
	
	public static void processFromListTable(net.sf.jsqlparser.schema.Table jsqlTable, FromListElement frmListElement, QueryStructure qParser){
		String tableName = jsqlTable.getFullyQualifiedName().toUpperCase();// getWholeTableName();
		String aliasName = "";
		if (jsqlTable.getAlias() == null) {
			aliasName = tableName;
		} else {
			aliasName = jsqlTable.getAlias().getName().toUpperCase();// getAlias();
		}
		if (qParser.getQuery().getRepeatedRelationCount().get(tableName) != null) {
			qParser.getQuery().putRepeatedRelationCount(tableName, qParser.getQuery()
					.getRepeatedRelationCount().get(tableName) + 1);
		} else {
			qParser.getQuery().putRepeatedRelationCount(tableName, 1);
		}
		String tableNameNo = tableName
				+ qParser.getQuery().getRepeatedRelationCount().get(tableName);
		
		frmListElement.setAliasName(aliasName);
		frmListElement.setTableName(tableName);
		frmListElement.setTableNameNo(tableNameNo);
		frmListElement.setTabs(null);	
		logger.info("Table added"+frmListElement);
	}

	public static void processFromListSubJoin(SubJoin subJoin, Vector<FromListElement> visitedFromListElements, Vector<Node> joinConditions,
			QueryStructure qParser, PlainSelect plainSelect) throws Exception{
		logger.info("processing subjoin"+ subJoin.toString());

		FromItem leftFromItem=subJoin.getLeft();
		FromListElement leftFLE=null, rightFLE=null;

		//JSQL parser restricts the left from item of a sub join  to be a Table
		if(leftFromItem instanceof net.sf.jsqlparser.schema.Table){
			net.sf.jsqlparser.schema.Table jsqlTable=(net.sf.jsqlparser.schema.Table)leftFromItem;
			leftFLE=new FromListElement();
			ProcessSelectClause.processFromListTable(jsqlTable, leftFLE, qParser);
			//logger.info(leftFLE.toString());
			visitedFromListElements.add(leftFLE);
		}
		else if(leftFromItem instanceof SubJoin){
			SubJoin leftSubJoin=(SubJoin) leftFromItem;
			Vector<FromListElement> tempElements=new Vector<FromListElement>();
			ProcessSelectClause.processFromListSubJoin(leftSubJoin, tempElements, joinConditions, qParser,plainSelect);
			leftFLE=new FromListElement();
			if(leftSubJoin.getAlias()!=null){
				leftFLE.setAliasName(leftSubJoin.getAlias().getName());
			}
			leftFLE.setTabs(tempElements);
			visitedFromListElements.add(leftFLE);
		}
		else if(leftFromItem instanceof SubSelect){
			SubSelect subSelect=(SubSelect) leftFromItem;					
			QueryStructure subQueryParser=new QueryStructure(qParser.getTableMap());
			leftFLE=new FromListElement();
			leftFLE.setSubQueryParser(subQueryParser);
			if(subSelect.getAlias()!=null){
				leftFLE.setAliasName(subSelect.getAlias().getName());
			}
			qParser.fromListElements.addElement(leftFLE);
			
			ProcessSelectClause.processFromListSubSelect(subSelect,subQueryParser,qParser);					

		}
		FromItem rightFromItem=subJoin.getJoin().getRightItem();
		if(rightFromItem instanceof net.sf.jsqlparser.schema.Table){
			net.sf.jsqlparser.schema.Table jsqlTable=(net.sf.jsqlparser.schema.Table)rightFromItem;
			rightFLE=new FromListElement();
			ProcessSelectClause.processFromListTable(jsqlTable, rightFLE, qParser);
			//logger.info(rightFLE.toString());
			visitedFromListElements.add(rightFLE);
		}
		else if(rightFromItem instanceof SubJoin){
			SubJoin rightSubJoin=(SubJoin) rightFromItem;
			Vector<FromListElement> tempElements=new Vector<FromListElement>();
			ProcessSelectClause.processFromListSubJoin(rightSubJoin, tempElements, joinConditions, qParser,plainSelect);
			rightFLE=new FromListElement();
			if(rightSubJoin.getAlias()!=null){
				rightFLE.setAliasName(rightSubJoin.getAlias().getName());
			}
			rightFLE.setTabs(tempElements);
			visitedFromListElements.add(rightFLE);
		}
		else if(rightFromItem instanceof SubSelect){
			SubSelect subSelect=(SubSelect) rightFromItem;					
			QueryStructure subQueryParser=new QueryStructure(qParser.getTableMap());
			rightFLE=new FromListElement();
			rightFLE.setSubQueryParser(subQueryParser);
			if(subSelect.getAlias()!=null){
				rightFLE.setAliasName(subSelect.getAlias().getName());
			}
			qParser.fromListElements.addElement(rightFLE);
			
			ProcessSelectClause.processFromListSubSelect(subSelect,subQueryParser,qParser);					

		}
		Join join=subJoin.getJoin();
		Expression e=join.getOnExpression();
		if(e!=null){
			Node joinCondition=processJoinExpression(e,visitedFromListElements, qParser,plainSelect);
			joinConditions.add(joinCondition);
		}
	}

	
	public static Node processJoinExpression(Expression e, Vector<FromListElement> visitedElements, QueryStructure qParser, PlainSelect plainSelect) throws Exception{
		Node n=processExpression(e, visitedElements , qParser, plainSelect);
		return n;
	}
	
	public static Node processExpression(Object clause, Vector<FromListElement> fle,
			QueryStructure qParser, PlainSelect plainSelect) throws Exception {
		try{
			if (clause == null) {
				return null;
			} else if (clause instanceof Parenthesis){
				 boolean isNot = ((Parenthesis) clause).isNot();
				Node n= processExpression(((Parenthesis)clause).getExpression(), fle,  qParser,plainSelect);
				if(clause instanceof Parenthesis && isNot){
					Node left = n.getLeft();
					Node right = n.getRight();
					if(left != null && left.getNodeType() != null && 
							left.getNodeType().equals(Node.getBroNodeType())
							&& left.getOperator() != null && left.getOperator().equalsIgnoreCase("=")){
						left.setOperator("/=");
					}
					if(right != null && right.getNodeType() != null && 
							right.getNodeType().equals(Node.getBroNodeType())
							&& right.getOperator() != null && right.getOperator().equalsIgnoreCase("=")){
						right.setOperator("/=");
					}
				
					if(left != null && left.getNodeType() != null && 
							left.getNodeType().equals(Node.getBroNodeType())
							&& left.getOperator() != null && left.getOperator().equalsIgnoreCase("=")){
						left.setOperator("/=");
					}
					if(right != null && right.getNodeType() != null && 
							right.getNodeType().equals(Node.getBroNodeType())
							&& right.getOperator() != null && right.getOperator().equalsIgnoreCase("=")){
						right.setOperator("/=");
					}
				}
				return n;
			}
			else if (clause instanceof Function) {
				Function an = (Function)clause;
				String funcName = an.getName();
			
				
				//All these are string manipulation functions and not aggregate function
				if(! (funcName.equalsIgnoreCase("Lower") || funcName.equalsIgnoreCase("substring") || funcName.equalsIgnoreCase("upper")
						||funcName.equalsIgnoreCase("trim") || funcName.equalsIgnoreCase("postion") || funcName.equalsIgnoreCase("octet_length")
						|| funcName.equalsIgnoreCase("bit_length") || funcName.equalsIgnoreCase("char_length") || funcName.equalsIgnoreCase("overlay"))){
					
					AggregateFunction af = new AggregateFunction();
					if (an.getParameters()!=null){
						ExpressionList anList = an.getParameters();
						List<Expression> expList = anList.getExpressions();//FIXME not only 1 expression but all expressions
		 				Node n = processExpression(expList.get(0), fle,  qParser,plainSelect);
						af.setAggExp(n);
						
					} else {
						af.setAggExp(null);
					}
					
					af.setFunc(funcName.toUpperCase());
					af.setAggAliasName(funcName.toUpperCase());
					af.setDistinct(an.isDistinct());
					//af.setAggAliasName(exposedName);
		
					Node agg = new Node();
					agg.setAgg(af);
					agg.setType(Node.getAggrNodeType());
					//Shree added this to set Table, TableNameNo for Aggregate function node - @ node level
					if(af.getAggExp() != null){
						agg.setTable(af.getAggExp().getTable());
						agg.setTableNameNo(af.getAggExp().getTableNameNo());
						agg.setTableAlias(af.getAggExp().getTableAlias());
						agg.setColumn(af.getAggExp().getColumn());
						
					}//Added by Shree for count(*) 
					else if(af.getFunc().toUpperCase().contains("COUNT") && an.isAllColumns()){				
						if(af.getAggExp() == null){
									//Node n1 = Util.getNodeForCount(fle, qParser);
								Node n1 = Util.getNodeForCount(fle, qParser);
									af.setAggExp(n1);
									af.setFunc(funcName.toUpperCase());
									af.setDistinct(an.isDistinct());
									
									agg.setTable(af.getAggExp().getTable());
									agg.setTableNameNo(af.getAggExp().getTableNameNo());
									agg.setTableAlias(af.getAggExp().getTableAlias());
									agg.setColumn(af.getAggExp().getColumn());
									
									agg.setLeft(null);
									agg.setRight(null);
								}
							}
					//Storing sub query details
//					agg.setQueryType(queryType);
//					if(queryType == 1) agg.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//					if(queryType == 2) agg.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);
//					//Adding this to the list of aliased names
//					if(exposedName !=null){
//						Vector<Node> present = new Vector<Node>();
//						if( qParser.getAliasedToOriginal().get(exposedName) != null)
//							present = qParser.getAliasedToOriginal().get(exposedName);
//						present.add(agg);
//						qParser.getAliasedToOriginal().put(exposedName, present);
//					}
		
					return agg;
				}
				else {
					//String function manipulation
					Node n=new Node();
					if (an.getParameters()!=null){
						ExpressionList anList = an.getParameters();
						List<Expression> expList = anList.getExpressions();//FIXME not only 1 expression but all expressions
		 				n = processExpression(expList.get(0),fle, qParser,plainSelect);		
					}
					return n;
				}
			} else if (clause instanceof DoubleValue) {
				Node n = new Node();
				n.setType(Node.getValType());
				String s=((((DoubleValue)clause).getValue()))+"";
				//String str=(BigIntegerDecimal)((((NumericConstantNode) clause).getValue()).getDouble()).toString();
				s=util.Utilities.covertDecimalToFraction(s);
				n.setStrConst(s);
				n.setLeft(null);
				n.setRight(null);
				return n;

			}else if (clause instanceof LongValue){
				Node n = new Node();
				n.setType(Node.getValType());
				String s=((((LongValue)clause).getValue()))+"";
				s=util.Utilities.covertDecimalToFraction(s);
				n.setStrConst(s);
				n.setLeft(null);
				n.setRight(null);
				return n;
			}
			else if (clause instanceof StringValue) {
				Node n = new Node();
				n.setType(Node.getValType());
				n.setStrConst(((StringValue) clause).getValue());
				n.setLeft(null); 
				n.setRight(null); 
				return n; 
			} else if (clause instanceof Column) {
				Column columnReference = (Column) clause;
				String colName= columnReference.getColumnName();
				String tableName  = columnReference.getTable().getFullyQualifiedName();

				Node n = new Node();
				n.setTableNameNo(tableName);
				n.setColumn(new parsing.Column(colName, tableName));
				//List <FromListElement> frmElementList = fle.getTabs();
//				 if(qParser.getQuery().getQueryString().toLowerCase().contains(("as "+tableName.toLowerCase()))){
//					}
//				 
//				if(qParser.getQuery().getQueryString().toLowerCase().contains(("as "+colName.toLowerCase()))){
//					
//					Vector <Node> value = qParser.getAliasedToOriginal().get(colName);
//					  
//					if(value != null && value.size() > 0){
//						n = value.get(0);//FIXME: vector of nodes not a single node
//					}
//					return n;
	//  
//				}
//				
//				else{
//					n = Util.getColumnFromOccurenceInJC(colName,tableName, fle, qParser);
//					if (n == null) {//then probably the query is correlated				
//						n = Util.getColumnFromOccurenceInJC(colName,tableName, qParser.getQueryAliases(),qParser);
//					}	
//				} 
//				if(n == null) {
//					logger.log(Level.WARNING,"WhereClauseVectorJSQL : Util.getColumnFromOccurenceInJC is not able to find matching Column - Node n = null");
//					return null;
//				}
				 
				n.setType(Node.getColRefType());
				if (tableName != null) {
					n.setTableAlias(tableName);
				} else {
					n.setTableAlias("");
				} 

				if(n.getColumn() != null){
					//n.getColumn().setAliasName(exposedName);
					n.setTable(n.getColumn().getTable());
					
				}
				//if(n.getTableNameNo() == null || n.getTableNameNo().isEmpty()){
					//n.setTableNameNo(tableNameNumber);
				//} 
				n.setLeft(null);
				n.setRight(null); 


				//Storing sub query details
				if(qParser.getSubQueryNames().containsKey(tableName)){//If this node is inside a sub query
					n.setQueryType(1);
					n.setQueryIndex(qParser.getSubQueryNames().get(tableName));
				}
				else if(qParser.getTableNames().containsKey(tableName)){
					n.setQueryType(qParser.getTableNames().get(tableName)[0]);
					n.setQueryIndex(qParser.getTableNames().get(tableName)[1]);
				}
				
				if(n.getTableNameNo()==null||n.getTableNameNo().isEmpty()){
					for(Node m:Util.getAllProjectedColumns(qParser.fromListElements, qParser)){
						if(m.getColumn().getColumnName().equalsIgnoreCase(n.getColumn().getColumnName())){
							n.setTable(m.getTable());
							n.setTableNameNo(m.getTableNameNo());
							break;
						}
					}
				}

				
//				Node tempn=n;
				n=transformToAbsoluteTableNames(n,fle,false, qParser);				

				if(n.getTableNameNo()==null||n.getTableNameNo().isEmpty()){
					List<SelectItem> projectedItems=plainSelect.getSelectItems();
					for(int j=0;j<projectedItems.size();j++){
						SelectItem projectedItem=projectedItems.get(j);
						if(projectedItem instanceof net.sf.jsqlparser.statement.select.SelectExpressionItem){
							SelectExpressionItem selExpItem=(net.sf.jsqlparser.statement.select.SelectExpressionItem)projectedItem;
							Expression e=selExpItem.getExpression();
							if(e instanceof net.sf.jsqlparser.expression.Parenthesis){
								net.sf.jsqlparser.expression.Parenthesis p=(net.sf.jsqlparser.expression.Parenthesis) e;
								e=p.getExpression();
							}
							if(selExpItem.getAlias()!=null){
								if(n.getColumn().getColumnName().equalsIgnoreCase(selExpItem.getAlias().getName())){
									n =partialMarking.ProcessSelectClause.processJoinExpression(e,qParser.fromListElements, qParser,plainSelect);
									break;
								}
							}
						}
					}

				}
				


				
				return n;

			} else if (clause instanceof AndExpression) {
				BinaryExpression andNode = ((BinaryExpression) clause);
				if (andNode.getLeftExpression() != null
						&& andNode.getRightExpression() != null) {
					
					/*if(andNode.getLeftExpression() instanceof ExtractExpression) {
						//type new_name = (type) ;
						return null;
					}else if(andNode.getRightExpression() instanceof ExtractExpression){
						return null;
					}*/
					Node n = new Node();
					Node left = new Node();
					Node right = new Node();
					n.setType(Node.getAndNodeType());
					n.setOperator("AND");
					left = processExpression(andNode.getLeftExpression(), fle, qParser,plainSelect);
					right = processExpression(andNode.getRightExpression(), fle, qParser,plainSelect);
					
					
					n.setLeft(left);
					n.setRight(right);

					return n;
				}

			} else if (clause instanceof OrExpression) {
				BinaryExpression orNode = ((BinaryExpression) clause);
				if (orNode.getLeftExpression() != null
						&& orNode.getRightExpression() != null) {
					Node n = new Node();
					n.setType(Node.getOrNodeType());
					n.setOperator("OR");
					n.setLeft(processExpression(orNode.getLeftExpression(),  fle, qParser,plainSelect));
					n.setRight(processExpression(orNode.getRightExpression(), fle, qParser,plainSelect));

					//Storing sub query details
//					n.setQueryType(queryType);
//					if(queryType == 1) n.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//					if(queryType == 2) n.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);

					return n;
				}
			} 

			//Added by Bikash ---------------------------------------------------------------------------------
			else if(clause instanceof LikeExpression){
				BinaryExpression likeNode=((BinaryExpression)clause);
				if (likeNode.getLeftExpression() !=null && likeNode.getRightExpression()!=null )
				{
					//if(likeNode.getReceiver() instanceof ColumnReference && (likeNode.getLeftOperand() instanceof CharConstantNode || likeNode.getLeftOperand() instanceof ParameterNode))
					{
						Node n=new Node();
						if(! likeNode.isNot()){
							n.setType(Node.getLikeNodeType());
							n.setOperator("~");
						}
						else{
							n.setType(Node.getLikeNodeType());
							n.setOperator("!~");
						}
						n.setLeft(processExpression(likeNode.getLeftExpression(),fle,qParser,plainSelect));
						n.setRight(processExpression(likeNode.getRightExpression(),  fle,qParser,plainSelect));

						//Storing sub query details
//						n.setQueryType(queryType);
//						if(queryType == 1) n.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//						if(queryType == 2) n.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);
						return n;
					}
				}
			}

			else if(clause instanceof JdbcParameter){
				Node n = new Node();
				n.setType(Node.getValType());		
				n.setStrConst("$"+qParser.paramCount);
				qParser.paramCount++;
				n.setLeft(null);
				n.setRight(null);

				//Storing sub query details
//				n.setQueryType(queryType);
//				if(queryType == 1) n.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//				if(queryType == 2) n.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);

				return n;
			}

			//**********************************************************************************/
			else if (clause instanceof Addition){
				BinaryExpression baoNode = ((BinaryExpression)clause);
				Node n = new Node();
				n.setType(Node.getBaoNodeType());
				n.setOperator("+");
				n.setLeft(processExpression(baoNode.getLeftExpression(), fle, qParser,plainSelect));
				n.setRight(processExpression(baoNode.getRightExpression(),fle, qParser,plainSelect));
				
				n=WhereClauseVectorJSQL.getTableDetailsForArithmeticExpressions(n);
				//Storing sub query details
//				n.setQueryType(queryType);
//				if(queryType == 1) n.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//				if(queryType == 2) n.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);
				return n;
			}
			else if (clause instanceof Subtraction){
				BinaryExpression baoNode = ((BinaryExpression)clause);
				Node n = new Node();
				n.setType(Node.getBaoNodeType());
				n.setOperator("-");
				n.setLeft(processExpression(baoNode.getLeftExpression(), fle,qParser,plainSelect));
				n.setRight(processExpression(baoNode.getRightExpression(), fle, qParser,plainSelect));
				n=WhereClauseVectorJSQL.getTableDetailsForArithmeticExpressions(n);
				//Storing sub query details
//				n.setQueryType(queryType);
//				if(queryType == 1) n.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//				if(queryType == 2) n.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);
				return n;
			}
			else if (clause instanceof Multiplication){
				BinaryExpression baoNode = ((BinaryExpression)clause);
				Node n = new Node();
				n.setType(Node.getBaoNodeType());
				n.setOperator("*");
				n.setLeft(processExpression(baoNode.getLeftExpression(), fle, qParser,plainSelect));
				n.setRight(processExpression(baoNode.getRightExpression(),fle, qParser,plainSelect));
				n=WhereClauseVectorJSQL.getTableDetailsForArithmeticExpressions(n);
				//Storing sub query details
//				n.setQueryType(queryType);
//				if(queryType == 1) n.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//				if(queryType == 2) n.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);
				return n;
			}
			else if (clause instanceof Division){
				BinaryExpression baoNode = ((BinaryExpression)clause);
				Node n = new Node();
				n.setType(Node.getBaoNodeType());
				n.setOperator("/");
				n.setLeft(processExpression(baoNode.getLeftExpression(),fle, qParser,plainSelect));
				n.setRight(processExpression(baoNode.getRightExpression(),fle, qParser,plainSelect));
				n=WhereClauseVectorJSQL.getTableDetailsForArithmeticExpressions(n);
				//Storing sub query details
//				n.setQueryType(queryType);
//				if(queryType == 1) n.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//				if(queryType == 2) n.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);
				return n;
			}

			else if (clause instanceof NotEqualsTo) {
				NotEqualsTo broNode = (NotEqualsTo)clause;
				/*if(broNode.getLeftExpression() instanceof ExtractExpression) {
					//type new_name = (type) ;
					Node n = new Node();
					return n;
				}else if(broNode.getRightExpression() instanceof ExtractExpression){
					Node n = new Node();
					return n;
				}*/
				
				//BinaryRelationalOperatorNode broNode = ((BinaryRelationalOperatorNode) clause);			
				Node n = new Node();
				n.setType(Node.getBroNodeType());
				n.setOperator(QueryStructure.cvcRelationalOperators[2]);
				n.setLeft(processExpression(broNode.getLeftExpression(), fle, qParser,plainSelect));
				n.setRight(processExpression(broNode.getRightExpression(), fle, qParser,plainSelect));

				//Storing sub query details
//				n.setQueryType(queryType);
//				if(queryType == 1) n.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//				if(queryType == 2) n.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);

				return n;
				} 
			else if (clause instanceof DoubleAnd) {
				DoubleAnd broNode = (DoubleAnd)clause;	
				
				Node n = new Node();
				n.setType(Node.getBroNodeType());
				n.setOperator(QueryStructure.cvcRelationalOperators[7]);
				n.setLeft(processExpression(broNode.getLeftExpression(), fle, qParser,plainSelect));
				n.setRight(processExpression(broNode.getRightExpression(), fle,qParser,plainSelect));

				//Storing sub query details
//				n.setQueryType(queryType);
//				if(queryType == 1) n.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//				if(queryType == 2) n.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);

				return n;
				} 
			else if (clause instanceof IsNullExpression) {
				IsNullExpression isNullNode = (IsNullExpression) clause;
				Node n = new Node();
				n.setType(Node.getIsNullNodeType());
				n.setLeft(processExpression(isNullNode.getLeftExpression(),fle,qParser,plainSelect));
				if(((IsNullExpression) clause).isNot()){
					n.setOperator("!=");
				}else{
					n.setOperator("=");
				}
				n.setRight(null);
				//Storing sub query details
//				n.setQueryType(queryType);
//				if(queryType == 1) n.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//				if(queryType == 2) n.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);
				return n;
			} else if (clause instanceof InExpression){ 
				//handles NOT and NOT IN both
				InExpression sqn = (InExpression)clause;
				SubSelect subS=null;
				Node inNode=new Node();
				inNode.setType(Node.getInNodeType());
								
				
				Node notNode = new Node();				   
				
				
				Node rhs = new Node();
			
				
				if (sqn. getLeftItemsList() instanceof SubSelect){
					subS=(SubSelect)sqn.getLeftItemsList();
				}
				else if(sqn. getRightItemsList() instanceof SubSelect){ 
					subS=(SubSelect)sqn.getRightItemsList();
				}
				QueryStructure subQueryParser=new QueryStructure(qParser.getTableMap());
				rhs.setSubQueryParser(subQueryParser);	
				processWhereSubSelect(subS,subQueryParser,qParser);
				
								 
				Node lhs = processExpression(sqn. getLeftExpression(), fle, qParser,plainSelect);
				
				inNode.setLeft(lhs);
				inNode.setRight(rhs);

				if(!sqn.isNot()){					
					return inNode;
				}else{
					notNode.setType(Node.getNotNodeType());
					notNode.setRight(null);
					notNode.setLeft(inNode);
					return notNode;
					
				}
				
			} else if (clause instanceof ExistsExpression){
				
				ExistsExpression sqn = (ExistsExpression)clause;
				SubSelect subS = (SubSelect)sqn.getRightExpression();

				
				QueryStructure subQueryParser=new QueryStructure(qParser.getTableMap());
				Node existsNode=new Node();
				existsNode.setSubQueryParser(subQueryParser);
				existsNode.setType(Node.getExistsNodeType());
				existsNode.setSubQueryConds(null);
				processWhereSubSelect(subS,subQueryParser,qParser);
								
				
				Node notNode = new Node();				   
				
				if(!((ExistsExpression) clause).isNot()){					
					return existsNode;
				}else{
					notNode.setType(Node.getNotNodeType());
					notNode.setRight(null);
					notNode.setLeft(existsNode);
					return notNode;
					
				}
				
			}
			else if (clause instanceof SubSelect) {
				SubSelect sqn = (SubSelect) clause;
				
				QueryStructure subQueryParser=new QueryStructure(qParser.getTableMap());
				Node node=new Node();
				node.setSubQueryParser(subQueryParser);
				node.setType(Node.getBroNodeSubQType());
				processWhereSubSelect(sqn,subQueryParser,qParser);
								

				
				//PlainSelect ps = sqn.getSelectBody();
				List<SelectItem> rcList = ((PlainSelect)sqn.getSelectBody()).getSelectItems();	
				
				SelectExpressionItem rc = (SelectExpressionItem)rcList.get(0);

				if(rc.getExpression() instanceof Function){ 
					return node;
				}
				else if(rc.getExpression() instanceof ColumnReference || 
						(((Parenthesis)rc.getExpression()).getExpression()) instanceof Column){
					//the result of subquery must be a single tuple
					logger.log(Level.WARNING,"the result of subquery must be a single tuple");
			    }
			}
			else if(clause instanceof Between){
				
				//FIXME: Mahesh If aggregate in where (due to aliased) then add to list of having clause of the subquery
				
				Between bn=(Between)clause;
				Node n=new Node();
				n.setType(Node.getAndNodeType());
				
				Node l=new Node();
				l.setLeft(processExpression(bn.getLeftExpression(),fle,qParser,plainSelect));
				l.setOperator(">=");
				l.setRight(processExpression(bn.getBetweenExpressionStart(),fle,qParser,plainSelect));
				l.setType(Node.getBroNodeType());
				n.setLeft(l);

				Node r=new Node();
				r.setLeft(processExpression(bn.getLeftExpression(), fle,qParser,plainSelect));
				r.setOperator("<=");
				r.setRight(processExpression(bn.getBetweenExpressionEnd(),fle,qParser,plainSelect));
				r.setType(Node.getBroNodeType());
				n.setRight(r);

				//Storing sub query details
//				n.setQueryType(queryType);
//				if(queryType == 1) n.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//				if(queryType == 2) n.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);

				return n;
				//throw new Exception("getWhereClauseVector needs more programming \n"+clause.getClass()+"\n"+clause.toString());
			} else if (clause instanceof EqualsTo){

				BinaryExpression bne = (BinaryExpression)clause;
				Node n = new Node();
				/*if(bne.getLeftExpression() instanceof ExtractExpression) {
					return n;
				}else if(bne.getRightExpression() instanceof ExtractExpression){
					return n;
				}*/
				n.setType(Node.getBroNodeType());
				n.setOperator("=");
				Node ndl = processExpression(bne.getLeftExpression(), fle, qParser,plainSelect);
				if(ndl != null){
					n.setLeft(ndl);
				}
				Node ndr = processExpression(bne.getRightExpression(), fle, qParser,plainSelect);
				if(ndr!= null){
					n.setRight(ndr);
				}
				
				if((ndl == null && ndr ==null)){
					return null;
				}
				
				return n;
			} else if (clause instanceof GreaterThan){
				GreaterThan broNode = (GreaterThan)clause;
				/*if(broNode.getLeftExpression() instanceof ExtractExpression) {
					//type new_name = (type) ;
					Node n = new Node();
					return n;
				}else if(broNode.getRightExpression() instanceof ExtractExpression){
					Node n = new Node();
					return n;
				}*/
				//BinaryRelationalOperatorNode broNode = ((BinaryRelationalOperatorNode) clause);			
				Node n = new Node();
				n.setType(Node.getBroNodeType());
				n.setOperator(QueryStructure.cvcRelationalOperators[3]);
				n.setLeft(processExpression(broNode.getLeftExpression(), fle, qParser,plainSelect));
				n.setRight(processExpression(broNode.getRightExpression(),fle, qParser,plainSelect));

				//Storing sub query details
//				n.setQueryType(queryType);
//				if(queryType == 1) n.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//				if(queryType == 2) n.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);
				
				if(n.getLeft() != null && n.getLeft().getSubQueryConds() != null && n.getLeft().getSubQueryConds().size() > 0 && n.getSubQueryConds()!=null){
					n.setSubQueryConds(n.getLeft().getSubQueryConds());
					n.getLeft().getSubQueryConds().clear();
				}
				else  { 
					if(n.getRight() != null &&n.getRight().getSubQueryConds() != null && n.getRight().getSubQueryConds().size() >0 && n.getSubQueryConds()!=null){
					n.setSubQueryConds(n.getRight().getSubQueryConds());
					n.getRight().getSubQueryConds().clear();
					}
				}
				
				if(((GreaterThan) clause).getRightExpression() instanceof AllComparisonExpression ||
						((GreaterThan) clause).getLeftExpression() instanceof AllComparisonExpression){

					Node sqNode = new Node();
					/* the expression: Node.getAllAnyNodeType() from the statement below removed, and 
					 * Node.getAllNodeType() in the following statement added by mathew on 27 June 2016
					 */
					sqNode.setType(Node.getAllNodeType());
					if(n.getLeft().getSubQueryConds() != null && n.getLeft().getSubQueryConds().size() > 0){
						sqNode.setSubQueryConds(n.getLeft().getSubQueryConds());
						n.getLeft().getSubQueryConds().clear();
				}
					else{
						if(n.getRight()!=null&&n.getRight().getSubQueryConds()!=null&&n.getRight().getSubQueryConds().size()>0 && sqNode.getSubQueryConds()!=null)
						{						sqNode.setSubQueryConds(n.getRight().getSubQueryConds());
						n.getRight().getSubQueryConds().clear();
						}
				} 
					sqNode.setLhsRhs(n);
					return sqNode;
				} 

				if(((GreaterThan) clause).getRightExpression() instanceof AnyComparisonExpression ||
						((GreaterThan) clause).getLeftExpression() instanceof AnyComparisonExpression){
					Node sqNode = new Node();
					/* the expression: Node.getAllAnyNodeType() from the statement below removed, and 
					 * Node.getAnyNodeType() in the following statement added by mathew on 27 June 2016
					 */
					sqNode.setType(Node.getAnyNodeType());
					if(n.getLeft().getSubQueryConds() != null && n.getLeft().getSubQueryConds().size() > 0){
						sqNode.setSubQueryConds(n.getLeft().getSubQueryConds());
						n.getLeft().getSubQueryConds().clear();
					} 
					else{ 
						if(n.getRight()!=null&&n.getRight().getSubQueryConds()!=null&&n.getRight().getSubQueryConds().size()>0 && sqNode.getSubQueryConds()!=null)
						{						sqNode.setSubQueryConds(n.getRight().getSubQueryConds());
						sqNode.setSubQueryConds(n.getRight().getSubQueryConds());
						n.getRight().getSubQueryConds().clear();
						}
					} 
					sqNode.setLhsRhs(n);
					return sqNode; 
				}  
				
				return n;
			}
			else if (clause instanceof GreaterThanEquals){
				GreaterThanEquals broNode = (GreaterThanEquals)clause;
				/*if(broNode.getLeftExpression() instanceof ExtractExpression) {
					//type new_name = (type) ;
					Node n = new Node();
					return n;
				}else if(broNode.getRightExpression() instanceof ExtractExpression){
					Node n = new Node();
					return n;
				}*/
				//BinaryRelationalOperatorNode broNode = ((BinaryRelationalOperatorNode) clause);			
				Node n = new Node();
				n.setType(Node.getBroNodeType());
				n.setOperator(QueryStructure.cvcRelationalOperators[4]);
				n.setLeft(processExpression(broNode.getLeftExpression(), fle, qParser,plainSelect));
				n.setRight(processExpression(broNode.getRightExpression(), fle, qParser,plainSelect));
	 
				//Storing sub query details
//				n.setQueryType(queryType);
//				if(queryType == 1) n.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//				if(queryType == 2) n.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);
				
				//Code added for ALL / ANY subqueries - Start
				//FIXME ANy condition needs to be tested - IS this correct???
				if(n.getLeft() != null && n.getLeft().getSubQueryConds() != null && n.getLeft().getSubQueryConds().size() > 0){
					n.setSubQueryConds(n.getLeft().getSubQueryConds());
					n.getLeft().getSubQueryConds().clear();
				}
				else{ 
					if(n.getRight() != null &&n.getRight().getSubQueryConds() != null && n.getRight().getSubQueryConds().size() >0 ){
					n.setSubQueryConds(n.getRight().getSubQueryConds());
					n.getRight().getSubQueryConds().clear();
					}
				}
				
				if(((GreaterThanEquals) clause).getRightExpression() instanceof AllComparisonExpression ||
						((GreaterThanEquals) clause).getLeftExpression() instanceof AllComparisonExpression){
					Node sqNode = new Node();
					/* the expression: Node.getAllAnyNodeType() from the statement below removed, and 
					 * Node.getAllNodeType() in the following statement added by mathew on 27 June 2016
					 */
					sqNode.setType(Node.getAllNodeType());
					if(n.getLeft().getSubQueryConds() != null && n.getLeft().getSubQueryConds().size() > 0){
						sqNode.setSubQueryConds(n.getLeft().getSubQueryConds());
						n.getLeft().getSubQueryConds().clear();
				}
					else{ 
						if(n.getRight() != null &&n.getRight().getSubQueryConds() != null && n.getRight().getSubQueryConds().size() >0 ){
						sqNode.setSubQueryConds(n.getRight().getSubQueryConds());
						n.getRight().getSubQueryConds().clear();
						}
				} 
					sqNode.setLhsRhs(n);
					return sqNode;
				}
				
				if(((GreaterThanEquals) clause).getRightExpression() instanceof AnyComparisonExpression ||
						((GreaterThanEquals) clause).getLeftExpression() instanceof AnyComparisonExpression){
					Node sqNode = new Node();
					/* the expression: Node.getAllAnyNodeType() from the statement below removed, and 
					 * Node.getAnyNodeType() in the following statement added by mathew on 27 June 2016
					 */
					sqNode.setType(Node.getAnyNodeType());
					if(n.getLeft().getSubQueryConds() != null && n.getLeft().getSubQueryConds().size() > 0){
						sqNode.setSubQueryConds(n.getLeft().getSubQueryConds());
						n.getLeft().getSubQueryConds().clear();
				} 
					else{ 
						if(n.getRight() != null &&n.getRight().getSubQueryConds() != null && n.getRight().getSubQueryConds().size() >0 ){
						sqNode.setSubQueryConds(n.getRight().getSubQueryConds());
						n.getRight().getSubQueryConds().clear();
						}
				} 
					sqNode.setLhsRhs(n);
					return sqNode; 
				}  

				return n;
			}
			else if (clause instanceof MinorThan){
				BinaryExpression bne = (BinaryExpression)clause;
				/*if(bne.getLeftExpression() instanceof ExtractExpression) {
					//type new_name = (type) ;
					Node n = new Node();
					return n;
				}else if(bne.getRightExpression() instanceof ExtractExpression){
					Node n = new Node();
					return n;
				}*/
				Node n = new Node();
				n.setType(Node.getBroNodeType());
				n.setOperator(QueryStructure.cvcRelationalOperators[5]);
				n.setLeft(processExpression(bne.getLeftExpression(), fle,qParser,plainSelect));
				n.setRight(processExpression(bne.getRightExpression(),fle,qParser,plainSelect));

				//Storing sub query details
//				n.setQueryType(queryType);
//				if(queryType == 1) n.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//				if(queryType == 2) n.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);

				if(n.getLeft() != null && n.getLeft().getSubQueryConds() != null && n.getLeft().getSubQueryConds().size() > 0){
					n.setSubQueryConds(n.getLeft().getSubQueryConds());
					n.getLeft().getSubQueryConds().clear();
				}
				else{ 
					if(n.getRight() != null &&n.getRight().getSubQueryConds() != null && n.getRight().getSubQueryConds().size() >0 ){
					n.setSubQueryConds(n.getRight().getSubQueryConds());
					n.getRight().getSubQueryConds().clear();
					}
				}
				
				if(((MinorThan) clause).getRightExpression() instanceof AllComparisonExpression ||
						((MinorThan) clause).getLeftExpression() instanceof AllComparisonExpression){
					Node sqNode = new Node();
					/* the expression: Node.getAllAnyNodeType() from the statement below removed, and 
					 * Node.getAllNodeType() in the following statement added by mathew on 27 June 2016
					 */
					sqNode.setType(Node.getAllNodeType());
					if(n.getLeft().getSubQueryConds() != null && n.getLeft().getSubQueryConds().size() > 0){
						sqNode.setSubQueryConds(n.getLeft().getSubQueryConds());
						n.getLeft().getSubQueryConds().clear();
				} 
					else{ 
						sqNode.setSubQueryConds(n.getRight().getSubQueryConds());
						n.getRight().getSubQueryConds().clear();
				} 
					sqNode.setLhsRhs(n);
					return sqNode;
				} 

				if(((MinorThan) clause).getRightExpression() instanceof AnyComparisonExpression ||
						((MinorThan) clause).getLeftExpression() instanceof AnyComparisonExpression){
					Node sqNode = new Node();
					/* the expression: Node.getAllAnyNodeType() from the statement below removed, and 
					 * Node.getAnyNodeType() in the following statement added by mathew on 27 June 2016
					 */
					sqNode.setType(Node.getAnyNodeType());
					if(n.getLeft().getSubQueryConds() != null && n.getLeft().getSubQueryConds().size() > 0){
						sqNode.setSubQueryConds(n.getLeft().getSubQueryConds());
						n.getLeft().getSubQueryConds().clear();
				} 
					else{ 
						sqNode.setSubQueryConds(n.getRight().getSubQueryConds());
						n.getRight().getSubQueryConds().clear();
				} 
					sqNode.setLhsRhs(n);
					return sqNode; 
				}  
				
				return n;
			} else if (clause instanceof MinorThanEquals){
				BinaryExpression bne = (BinaryExpression)clause;
				Node n = new Node();
				/*if(bne.getLeftExpression() instanceof ExtractExpression) {
					//type new_name = (type) ;
					return n;
				}else if(bne.getRightExpression() instanceof ExtractExpression){
					return n;
				}*/
				n.setType(Node.getBroNodeType());
				n.setOperator(QueryStructure.cvcRelationalOperators[6]);
				n.setLeft(processExpression(bne.getLeftExpression(), fle,qParser,plainSelect));
				n.setRight(processExpression(bne.getRightExpression(),fle, qParser,plainSelect));

				//Storing sub query details
//				n.setQueryType(queryType);
//				if(queryType == 1) n.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//				if(queryType == 2) n.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);

				if(n.getLeft() != null && n.getLeft().getSubQueryConds() != null && n.getLeft().getSubQueryConds().size() > 0){
					n.setSubQueryConds(n.getLeft().getSubQueryConds());
					n.getLeft().getSubQueryConds().clear();
				}
				else{ 
					if(n.getRight() != null &&n.getRight().getSubQueryConds() != null && n.getRight().getSubQueryConds().size() >0 ){
					n.setSubQueryConds(n.getRight().getSubQueryConds());
					n.getRight().getSubQueryConds().clear();
					}
				}
				if(((MinorThanEquals) clause).getRightExpression() instanceof AllComparisonExpression ||
						((MinorThanEquals) clause).getLeftExpression() instanceof AllComparisonExpression){
					Node sqNode = new Node();
					/* the expression: Node.getAllAnyNodeType() from the statement below removed, and 
					 * Node.getAllNodeType() in the following statement added by mathew on 27 June 2016
					 */
					sqNode.setType(Node.getAllNodeType());
					if(n.getLeft().getSubQueryConds() != null && n.getLeft().getSubQueryConds().size() > 0){
						sqNode.setSubQueryConds(n.getLeft().getSubQueryConds());
						n.getLeft().getSubQueryConds().clear();
				} 
					else{ 
						sqNode.setSubQueryConds(n.getRight().getSubQueryConds());
						n.getRight().getSubQueryConds().clear();
				} 
					sqNode.setLhsRhs(n);
					return sqNode;
				} 
				
				if(((MinorThanEquals) clause).getRightExpression() instanceof AnyComparisonExpression ||
						((MinorThanEquals) clause).getLeftExpression() instanceof AnyComparisonExpression){
					Node sqNode = new Node();
					/* the expression: Node.getAllAnyNodeType() from the statement below removed, and 
					 * Node.getAnyNodeType() in the following statement added by mathew on 27 June 2016
					 */
					sqNode.setType(Node.getAnyNodeType());
					if(n.getLeft().getSubQueryConds() != null && n.getLeft().getSubQueryConds().size() > 0){
						sqNode.setSubQueryConds(n.getLeft().getSubQueryConds());
						n.getLeft().getSubQueryConds().clear();
				} 
					else{ 
						sqNode.setSubQueryConds(n.getRight().getSubQueryConds());
						n.getRight().getSubQueryConds().clear();
				} 
					sqNode.setLhsRhs(n);
					return sqNode; 
				}  
				
				return n;
			} else if(clause instanceof CaseExpression){
				CaseExpression expr =  (CaseExpression)clause;
				Node n = new Node();
				List<Expression> whenExprList = expr.getWhenClauses();
				Vector <Node> caseConditionNode = new Vector<Node>();
				//If it is a case expression, then create a vector of nodes that holds case condition and else cond
				//Add that to cvc or qparser and return a node that is of type casecondition.

					if(expr.getElseExpression() != null){
						n = processExpression(expr.getElseExpression(), fle, qParser,plainSelect);
					}
					else if(expr.getWhenClauses() != null){
						for(int i = 0; i < expr.getWhenClauses().size();i++){
							Expression ex = expr.getWhenClauses().get(i);
							n = processExpression(ex, fle,qParser,plainSelect);
						}
		
					return n;
					}else{
						return null;
					}
			}
			else if (clause instanceof AllComparisonExpression){
				
				AllComparisonExpression ace = (AllComparisonExpression)clause;
				SubSelect ss = ace.getSubSelect();
				
				QueryStructure subQueryParser=new QueryStructure(qParser.getTableMap());
				Node allNode=new Node();
				allNode.setSubQueryParser(subQueryParser);
				allNode.setType(Node.getAllNodeType());
				processWhereSubSelect(ss,subQueryParser,qParser);
				
				return allNode;				

			}
			else if (clause instanceof AnyComparisonExpression){
				AnyComparisonExpression ace = (AnyComparisonExpression)clause;
				SubSelect ss = ace.getSubSelect();
				
				QueryStructure subQueryParser=new QueryStructure(qParser.getTableMap());
				Node anyNode=new Node();
				anyNode.setSubQueryParser(subQueryParser);
				anyNode.setType(Node.getAnyNodeType());
				processWhereSubSelect(ss,subQueryParser,qParser);
				
				return anyNode;
			}
			else if(clause instanceof ExtractExpression){
				//To extract approximate no: of days in month is considered as 30.
				/*Assuming the ExtractExpression clause holds name and Column alone*/
					ExtractExpression exp = (ExtractExpression)clause;
					Node n=new Node(); // Main node
					String name = exp.getName();
					Node table = processExpression(exp.getExpression(),fle, qParser,plainSelect);
					if(name.equalsIgnoreCase("year")){
						//Formula : 1970+ (col Name/(30*12)) - create 5 nodes
						n.setOperator("+"); // Main node
						n.setType(Node.getBaoNodeType());
//						n.setQueryType(queryType);
//						if(queryType == 1) n.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//						if(queryType == 2) n.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);
						Node n1 = new Node(); //Left of main node - level 1
						n1.setType(Node.getValType());
						String s="1970";
						s=util.Utilities.covertDecimalToFraction(s);
						n1.setStrConst(s);
						Node i = new Node();
						i.setType(Node.getExtractFuncType());
						n1.setLeft(i);
						//n1.setLeft(null);
						n1.setRight(null);
						n1.setTable(table.getTable());
						n1.setTableNameNo(table.getTableNameNo());
						n1.setColumn(table.getColumn());
						n.setLeft(n1);
						
						//Call method to get Node :
						
						//n.setRight(getYearCalc(exp,exposedName,fle,isWhereClause,queryType,qParser));
						
						
					}else if(name.equalsIgnoreCase("month")){
						//Formula 1 : (col Name/30) MOD 12 But as CVC does not support MOD rewrite the formula 
						// Formula 2: a mod b = a - (a/b) *b => (col Name/30)-((col Name/30)/12) * 12) create 12 nodes
						n.setOperator("-"); // Main node
						n.setType(Node.getBaoNodeType());
//						n.setQueryType(queryType);
//						if(queryType == 1) n.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//						if(queryType == 2) n.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);
						
							//LEFT OF MAIN NODE - considered as level 0
							Node nl1 = new Node(); 
								nl1.setOperator("/");
								nl1.setType(Node.getBaoNodeType());
//								nl1.setQueryType(queryType);
//								if(queryType == 1) nl1.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//								if(queryType == 2)nl1.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);
								//Left of node at level 1
								nl1.setLeft(processExpression(exp.getExpression(),fle,qParser,plainSelect));
								
									Node nl1r1 = new Node();
									nl1r1.setType(Node.getValType());
									String s="30";
									s=util.Utilities.covertDecimalToFraction(s);
									nl1r1.setStrConst(s);
									Node i = new Node();
									i.setType(Node.getExtractFuncType());
									nl1r1.setLeft(i);
									//nl1r1.setLeft(null);
									nl1r1.setRight(null);
							nl1.setRight(nl1r1);
						n.setTable(table.getTable());
						n.setTableNameNo(table.getTableNameNo());
						n.setColumn(table.getColumn());
						n.setLeft(nl1);
						
						//RIGHT OF MAIN NODE  - considered as level 0
						Node nr1 = new Node();
							nr1.setOperator("*");
							nr1.setType(Node.getBaoNodeType());
//	9						nr1.setQueryType(queryType);
//							if(queryType == 1) nr1.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//							if(queryType == 2) nr1.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);
							//Left Node at level 1
								Node nr1l1 = new Node();
									nr1l1.setOperator("/");
									nr1l1.setType(Node.getBaoNodeType());
//									nr1l1.setQueryType(queryType);
//									if(queryType == 1) nr1l1.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//									if(queryType == 2) nr1l1.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);
											//Left Node at level 2
											Node nr1l2 = new Node();
												nr1l2.setOperator("/");
												nr1l2.setType(Node.getBaoNodeType());
//												nr1l2.setQueryType(queryType);
//												if(queryType == 1) nr1l2.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//												if(queryType == 2) nr1l2.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);
												//Left Node at level 3
												nr1l2.setLeft(processExpression(exp.getExpression(),fle,qParser,plainSelect));
												
												//Right Node at level 3
												Node nr1r3 = new Node();
													nr1r3.setType(Node.getValType());
													String st="30";
													st=util.Utilities.covertDecimalToFraction(st);
													nr1r3.setStrConst(st);
													Node i1 = new Node();
													i1.setType(Node.getExtractFuncType());
													nr1r3.setLeft(i1);
													nr1r3.setRight(null);
												nr1l2.setRight(nr1r3);
												
											
									nr1l1.setLeft(nr1l2);	
										//Right Node at level 2
											Node nr1r2 = new Node();
											nr1r2.setType(Node.getValType());
											String s1="12";
											s1=util.Utilities.covertDecimalToFraction(s1);
											nr1r2.setStrConst(s1);
											Node i2 = new Node();
											i2.setType(Node.getExtractFuncType());
											nr1r2.setLeft(i2);
											//nr1r2.setLeft(null);
											nr1r2.setRight(null);
									nr1l1.setRight(nr1r2);
							nr1.setLeft(nr1l1);
							
							//Right Node at level 1
								Node nr1r1 = new Node();
								nr1r1.setType(Node.getValType());
								String s2="12";
								s2=util.Utilities.covertDecimalToFraction(s2);
								nr1r1.setStrConst(s2);
								Node i4 = new Node();
								i4.setType(Node.getExtractFuncType());
								nr1r1.setLeft(i4);
								//nr1r1.setLeft(null);
								nr1r1.setRight(null);
							nr1.setRight(nr1r1);
						
					 n.setRight(nr1);
						
					}else if(name.equalsIgnoreCase("day")){
						//Formula 1: approximate Date : (column value -((col Value/(30*12))*365) MOD 30)
						//Formula 2 : eliminating MOD :[ (column value -((col Value/(30*12))*365)) - ([(column value -((col Value/(30*12))*365))/30] * 30) ]
						n.setOperator("-"); // Main node
						n.setType(Node.getBaoNodeType());
//						n.setQueryType(queryType);
						n.setTable(table.getTable());
						n.setTableNameNo(table.getTableNameNo());
						n.setColumn(table.getColumn());
//						if(queryType == 1) n.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//						if(queryType == 2) n.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);
						//Left of main node
//						n.setLeft(getDayCalc(exp, exposedName, fle, isWhereClause, queryType, qParser));
							//Right of main node
							Node nr1 = new Node();
							nr1.setOperator("*");
							nr1.setType(Node.getBaoNodeType());
//							nr1.setQueryType(queryType);
//							if(queryType == 1) nr1.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//							if(queryType == 2) nr1.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);
							
								Node nr1l1 = new Node();
								nr1l1.setOperator("/");
								nr1l1.setType(Node.getBaoNodeType());
//								nr1l1.setQueryType(queryType);
//								if(queryType == 1) nr1l1.setQueryIndex(qParser.getFromClauseSubqueries().size()-1);
//								if(queryType == 2) nr1l1.setQueryIndex(qParser.getWhereClauseSubqueries().size()-1);
//								nr1l1.setLeft(getDayCalc(exp, exposedName, fle, isWhereClause, queryType, qParser));
										Node nr1r2 = new Node();
										nr1r2.setType(Node.getValType());
										String st="30";
										st=util.Utilities.covertDecimalToFraction(st);
										nr1r2.setStrConst(st);
										Node i = new Node();
										i.setType(Node.getExtractFuncType());
										nr1r2.setLeft(i);
										//nr1r2.setLeft(null);
										nr1r2.setRight(null);
								nr1l1.setRight(nr1r2);
							nr1.setLeft(nr1l1);
							
								Node nr1r1 = new Node();
								nr1r1.setType(Node.getValType());
								String s2="30";
								s2=util.Utilities.covertDecimalToFraction(s2);
								nr1r1.setStrConst(s2);
								Node i1 = new Node();
								i1.setType(Node.getExtractFuncType());
								nr1r1.setLeft(i1);
								//nr1r1.setLeft(null);
								nr1r1.setRight(null);
							nr1.setRight(nr1r1);
						n.setRight(nr1);
					}
				return n;
			}
			 
			else {
				logger.log(Level.SEVERE,"getWhereClauseVector needs more programming ");
				throw new Exception("getWhereClauseVector needs more programming ");
			}
		}catch(Exception e){
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw e;
		}
			return null;
	}
	
	public static void processFromListSubSelect(SubSelect subSelect, QueryStructure subQueryParser,QueryStructure parentQueryParser) throws Exception {
		// TODO Auto-generated method stub
		logger.info(" Processing subselect, selbody:"+subSelect.getSelectBody().toString());
		if(subSelect.getAlias()!=null)
		logger.info(" subselect alias "+subSelect.getAlias().getName());

		parentQueryParser.getFromClauseSubqueries().add(subQueryParser);
		subQueryParser.parentQueryParser=parentQueryParser;
		subQueryParser.setQuery(new Query("q2",subSelect.getSelectBody().toString()));
		subQueryParser.getQuery().setRepeatedRelationCount(parentQueryParser.getQuery().getRepeatedRelationCount());
		subQueryParser.parseQueryJSQL("q2", subSelect.getSelectBody().toString(), true);
		
	}
	
	
	public static void processWhereSubSelect(SubSelect subSelect, QueryStructure subQueryParser,QueryStructure parentQueryParser) throws Exception {
		// TODO Auto-generated method stub
		
		logger.info(" Processing subselect, selbody:"+subSelect.getSelectBody().toString());

		parentQueryParser.getWhereClauseSubqueries().add(subQueryParser);
		subQueryParser.parentQueryParser=parentQueryParser;
		subQueryParser.setQuery(new Query("q2",subSelect.getSelectBody().toString()));
		subQueryParser.getQuery().setRepeatedRelationCount(parentQueryParser.getQuery().getRepeatedRelationCount());
		subQueryParser.parseQueryJSQL("q2", subSelect.getSelectBody().toString(), true);
		
	}
	
	private static Node transformToAbsoluteTableNames(Node n, Vector<FromListElement> fleList, boolean aliasNameFound, QueryStructure qParser) throws Exception {
		// TODO Auto-generated method stub
		for(FromListElement fle:fleList){
			if(fle!=null&&fle.getTableName()!=null){
				if(fle.getTableName().equalsIgnoreCase(n.getTableNameNo())){
					n.setTableNameNo(fle.getTableNameNo());
					Table table=qParser.getTableMap().getTable(fle.getTableName());
					n.setTable(table);
					logger.info("table Name Found "+n);
					return n;
				}
				else if(fle.getAliasName().equalsIgnoreCase(n.getTableNameNo())){
					n.setTableNameNo(fle.getTableNameNo());
					Table table=qParser.getTableMap().getTable(fle.getTableName());
					if(table!=null)
						n.setTable(table);		
					logger.info("alias Name Found "+n);
					return n;
				}
				else if(aliasNameFound){
					logger.info("alias Name Found but not n");
					Table table=qParser.getTableMap().getTable(fle.getTableName());
					parsing.Column c;
					if((c=table.getColumn(n.getColumn().getColumnName().toUpperCase()))!=null){
						n.setTableNameNo(fle.getTableNameNo());
						n.setTable(table);
						return n;
					}
				}
			}
			if(aliasNameFound){
				logger.info("alias name found"+n);
				if(fle.getTabs()!=null&&!fle.getTabs().isEmpty()){
					Node k= transformToAbsoluteTableNames(n,fle.getTabs(),false,qParser);
					if(!n.getTableNameNo().equalsIgnoreCase(k.getTableNameNo()))
						return k;
				}
				if(fle.getSubQueryParser()!=null){
					Node k= transformToAbsoluteTableNames(n,fle.getSubQueryParser().getFromListElements(),true,fle.getSubQueryParser());
					if(k!=null&&!n.getTableNameNo().equalsIgnoreCase(k.getTableNameNo()))
						return k;					
					
				}
				logger.info(" Alias name found, but column name cannot be resolved");
			}
			if(fle!=null&&fle.getTableName()==null && fle.getAliasName()!=null){
				logger.info(" alias name is not null, but table name is null");
				if(fle.getAliasName().equalsIgnoreCase(n.getTableNameNo())){
					if(fle.getSubQueryParser()!=null){
						Node k= transformToAbsoluteTableNames(n,fle.getSubQueryParser().getFromListElements(),true,fle.getSubQueryParser());
						
						if(k!=null&&!n.getTableNameNo().equalsIgnoreCase(k.getTableNameNo()))
							return k;		
					}
					else {
						Node k= transformToAbsoluteTableNames(n,fle.getTabs(),true, qParser);
						if(k!=null&& !n.getTableNameNo().equalsIgnoreCase(k.getTableNameNo()))
							return k;		
					}
				}						
			}
			if(fle!=null && fle.getTabs()!=null && !fle.getTabs().isEmpty()){
				logger.info(" tabs is not null");
				Node k= transformToAbsoluteTableNames(n,fle.getTabs(),false,qParser);
				if(!n.getTableNameNo().equalsIgnoreCase(k.getTableNameNo()))
					return k;
			}
			if(fle!=null && fle.getSubQueryParser()!=null){
				logger.info(" subQueryParser: checking projected cols");
				
				for(Node m:fle.getSubQueryParser().getProjectedCols()){
					if(m.getAgg()!=null && m.getAgg().getAggAliasName()!=null){
						if(n.getColumn().getColumnName().equalsIgnoreCase(m.getAgg().getAggAliasName())){
							logger.info(" agg alias Name "+m.getAgg().getAggAliasName()+" node "+m);
							return m;
						}
					}					
					if(m.getColumn()!=null&&m.getColumn().getColumnName().equalsIgnoreCase(n.getColumn().getColumnName())){	
						logger.info(" column Name found in subQueryParser "+m);
						return m;
					}
					if(m.getAliasName()!=null&&m.getAliasName().equalsIgnoreCase(n.getColumn().getColumnName())){
						logger.info(" column Name found as alias in subQueryParser "+m);
						return m;
					}
				}	
				
				Node k=transformToAbsoluteTableNames(n,fle.getSubQueryParser().getFromListElements(),false, fle.getSubQueryParser());
				if(!n.getTableNameNo().equalsIgnoreCase(k.getTableNameNo()))
					return k;


			}
		}
		
		return n;
	}
}
