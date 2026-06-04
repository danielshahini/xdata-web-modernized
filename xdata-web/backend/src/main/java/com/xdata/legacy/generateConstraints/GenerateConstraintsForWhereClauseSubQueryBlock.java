package com.xdata.legacy.generateConstraints;
import com.xdata.util.TableMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import com.xdata.legacy.GenConstraints.GenConstraints;

import java.util.Map.Entry;

import com.xdata.legacy.parsing.AggregateFunction;
import com.xdata.legacy.parsing.Column;
import com.xdata.legacy.parsing.ConjunctQueryStructure;
import com.xdata.legacy.parsing.Node;
import com.xdata.legacy.parsing.QueryStructure;
import com.xdata.legacy.parsing.Table;
import com.xdata.legacy.stringSolver.StringConstraint;
import com.xdata.legacy.testDataGen.GenerateCVC1;
import com.xdata.legacy.testDataGen.QueryBlockDetails;
import com.xdata.legacy.util.Configuration;
import com.xdata.legacy.util.ConstraintObject;
import com.xdata.legacy.generateConstraints.GenerateJoinPredicateConstraints;

/**
 * This class contains methods to generate constraints for the where clause subquery blocks
 * The constraints include 
 *  1>> The constraints for the connective used between outer query block and where clause subquery block
 *  2>> The constraints for the conjuncts involved inside where clause nested subquery block (Whether to generate negative/positive constraints depends on the type of connective)
 *  3>> The constraints for the group by nodes and constrained aggregation inside where clause nested subquery block
 * @author mahesh
 *
 */

public class GenerateConstraintsForWhereClauseSubQueryBlock {

	/**
	 * The method that does the actual genartion of the constraints. It considers all the conditions inside where clause subquery block
	 * This method also adds conditions inside where clause subqueries including group by and aggregation constraints 
	 * @param cvc
	 * @param queryBlock
	 * @param conjunct
	 * @return
	 */
	public static String getConstraintsForWhereClauseSubQueryBlock(	GenerateCVC1 cvc, QueryBlockDetails queryBlock,	ConjunctQueryStructure conjunct) throws Exception{

		String constraintString = "";
		if(conjunct == null) return "";
		cvc.inititalizeSQDataset();
		if(conjunct.getAllSubQueryConds() != null){
			for(int i=0; i < conjunct.getAllSubQueryConds().size(); i++){

				Node subQ = conjunct.getAllSubQueryConds().get(i);
				if(cvc.getCurrentMutant() != null && cvc.getCurrentMutant().getQueryBlock().getLevel() == queryBlock.getLevel()){
					com.xdata.legacy.GenConstraints.GenConstraints.HandleWhereConnectiveMutations(subQ, cvc.getCurrentMutant());

				}
				
				constraintString +=ConstraintGenerator.addCommentLine("CONSTRAINTS FOR WHERE CLAUSE SUBQUERY CONNECTIVE ");
				// constraintString += getConstraintsForWhereSubQueryConnective(cvc, queryBlock, subQ);
				
				constraintString += ConstraintGenerator.addCommentLine("CONSTRAINTS FOR CONDITIONS INSIDE WHERE CLAUSE SUBQUERY CONNECTIVE ");
				
				constraintString += getCVCForCondsInSubQ(cvc, queryBlock, subQ);//conjunq of outer query
			
				
				
				constraintString += ConstraintGenerator.addCommentLine("END OF CONSTRAINTS FOR CONDITIONS INSIDE WHERE CLAUSE SUBQUERY CONNECTIVE ");
			}
		}
		return constraintString;
	}
	



		/**
	 * Given a Where clause SubQuery node, generates constraints for conditions inside that subQuery
	 * @param cvc
	 * @param queryBlock
	 * @param subQ
	 * @return
	 */
	/**FIXME: What about from clause sub queries inside where clause nested sub query	 (If we consider . For now not doing for more than one nesting level)
	 * FIXME: Write good documentation for this method
	 */
	public static String getCVCForCondsInSubQ(GenerateCVC1 cvc, QueryBlockDetails queryBlock, Node subQ) throws Exception{

		String constraintString="";
		//System.out.println("mid ...");
		/**get the index of this where clause subquery */
		int index = UtilsRelatedToNode.getQueryIndexOfSubQNode(subQ);

		/** Used to store conditions of this subquery block*/
		Vector<Node> condsInSubQ = new Vector<Node>();

		/** Get the query block of this subquery node*/
		QueryBlockDetails subQuery = null;
		if(queryBlock.getWhereClauseSubQueries() != null && ! queryBlock.getWhereClauseSubQueries().isEmpty()&& index>-1&&queryBlock.getWhereClauseSubQueries().size()>index){
			subQuery= queryBlock.getWhereClauseSubQueries().get(index);
			
		}

		if(subQuery != null){
			/**Get the conditions of the subquery*/
			/**FIXME: What should be done if inside is ORing of conditions*/
			for(ConjunctQueryStructure con: subQuery.getConjunctsQs()){
				condsInSubQ.addAll(con.getStringSelectionConds());
				condsInSubQ.addAll(con.getSelectionConds());
				/** add equi joins*/
				for(Vector<Node> ecn: con.getEquivalenceClasses()){
					Node n1 = ecn.get(0);
					for(int l = 1; l<ecn.size(); l++){
						Node jn = new Node();
						jn.setLeft(n1);
						jn.setRight(ecn.get(l));
						jn.setOperator("=");
						//jn.setQueryIndex();
						condsInSubQ.add(jn);
					}
				}
				//condsInSubQ.addAll(con.getJoinConds());
				condsInSubQ.addAll(con.getAllConds());
			}
			
			return generateConstraintsForConditionsInWhereSubquery(cvc, subQ, condsInSubQ, subQuery);
		}
		return "";

	}


	/**
	 * used to get constraints for given set of where clause sub query conditions
	 * @param cvc
	 * @param subQ
	 * @param condsInSubQ
	 * @param subQuery
	 * @return
	 * @throws Exception
	 */
	public static String generateConstraintsForConditionsInWhereSubquery(GenerateCVC1 cvc, Node subQ, 	Vector<Node> condsInSubQ, QueryBlockDetails subQuery)
			throws Exception {

		String constraintString = "";
		ConstraintGenerator constrGen = new ConstraintGenerator();
		/**Depending on the type of connective generate the constraints */
		if(subQ.getType().equals(Node.getBroNodeSubQType())){	

			constraintString = getConstraintsForConditionsInSubquery(cvc, condsInSubQ, subQuery);

		}
		else if(Configuration.getProperty("tempJoins").equalsIgnoreCase("true")) {//conjunct doesnt exist we still map SQ
			boolean isExist=true;
			if(subQ.getType().equals(Node.getNotExistsNodeType())) isExist=false;

			/** if subQuery contains a JOIN then all the selection and correlations conditions from the subquery are enforced on the tempJoin table **/
			if(Configuration.getProperty("cntFlag").equalsIgnoreCase("false")) {
					return GenerateJoinPredicateConstraints.getConstraintsForJoinsWithoutCount(cvc, subQuery, null, null, "=", isExist, "where");
				}
			else{
				return GenerateJoinPredicateConstraints.getConstraintsForJoinsInSameQueryBlockWithCount(cvc, subQuery, null, null, "=", isExist, "where");
			}
			
		}
		else if(subQ.getType().equals(Node.getNotExistsNodeType())){//tempjon=false
			for (ConjunctQueryStructure conjuct: subQuery.getConjunctsQs()){
				//&& subQuery.getConjunctsQs().get(0).getJoinCondsForEquivalenceClasses() != null && subQuery.getConjunctsQs().get(0).getJoinCondsForEquivalenceClasses().size() != 0
				constraintString += GenerateConstraintsForConjunct.generateNegativeConstraintsConjunct(cvc, subQuery, conjuct);
			}
			return constraintString;
		}
		else {//tempjoin false

			for(int i=0;i<condsInSubQ.size();i++){

				Node n = condsInSubQ.get(i);
				if(n.getLeft().getType().equals(Node.getColRefType()) && n.getRight().getType().equals(Node.getColRefType()) && n.getOperator().equals("="))
					constraintString += GenerateJoinPredicateConstraints.getConstraintsForEquiJoins(cvc, subQuery,n.getLeft(), n.getRight(), "where");	/** If it is equi join condition */

				else if(n.getLeft().getType().equalsIgnoreCase(Node.getColRefType()) && n.getRight().getType().equalsIgnoreCase(Node.getColRefType())    
						&& !n.getOperator().equalsIgnoreCase("")) /**if non equi join constraint*/

					constraintString += GenerateJoinPredicateConstraints.getConstraintsForNonEquiJoins(cvc, subQuery, n.getLeft(), n.getRight(), n.getOperator(), "where");

				else{
					String tableNameNo = n.getLeft().getTableNameNo();
					int offset = cvc.getRepeatedRelNextTuplePos().get(tableNameNo)[1];

					/**The total number of tuples across all groups of this subquery*/
					int num = cvc.getNoOfTuples().get(tableNameNo) * subQuery.getNoOfGroups();
					for(int j=0;j<num;j++){
						// changed made by sunanda for count
						String constraints = getConstraintsForSelectionConditionsUsingCount(n, j+offset);

						if( UtilsRelatedToNode.isStringSelection(n,0)){/** If it is a string selection condition*/

							String subQueryConstraints = constrGen.genPositiveCondsForPred(subQuery, n, j+offset);
							String result = cvc.getStringSolver().solveConstraints(subQueryConstraints,cvc.getResultsetColumns(), cvc.getTableMap()).get(0);
							//							StringConstraint s;
							if(Configuration.getProperty("cntFlag").equalsIgnoreCase("true")) {
								if(result.contains("assert")) {
									result = result.split("assert")[1].trim();
									result = result.substring(0,result.lastIndexOf(")")).trim();
								}
								
								constraintString += "(assert (and "+ result + " " + constraints + "))";	
							}
							else {
								constraintString += result;
							}
						}
						else{
							String res = ConstraintGenerator.genPositiveCondsForPred(subQuery, n, offset+j);
							if(Configuration.getProperty("cntFlag").equalsIgnoreCase("true"))	
								res = "(and "+res +" " + constraints + " )"; // changes for condition with count
							
							constraintString += constrGen.getAssertConstraint(res);
						}
					}
				}
			}
		}

		return constraintString;
	}
	
	 public static boolean isCorrelated(Node selectionCondition, GenerateCVC1 cvc) {
			if(cvc.getqStructure().getWhereClauseSubqueries().size()==0) return false;

	    	if(selectionCondition.getRight().getColumn()!=null) {
	    		String operator = selectionCondition.getOperator();
	    		ArrayList<String> tablesInSelectionConditions = new ArrayList<String>();
	    		String table1 = selectionCondition.getLeft().getTableNameNo();
	    		String table2 = selectionCondition.getRight().getTableNameNo();
	    		tablesInSelectionConditions.add(table1);
	    		tablesInSelectionConditions.add(table2);

	    		ArrayList<String> innerTables = cvc.getqStructure().getWhereClauseSubqueries().get(0).getLstRelationInstances();
	    		ArrayList<String> outerTables = new ArrayList<String>();
	    		Iterator<Entry<String, String>> it = cvc.getBaseRelation().entrySet().iterator();
	    		while(it.hasNext()) {
	    			Map.Entry<String, String> temp = (Map.Entry<String, String>) it.next();
	    			outerTables.add(temp.getValue());
	    		}
	    		if(innerTables.contains(tablesInSelectionConditions.get(0)) && outerTables.contains(tablesInSelectionConditions.get(1))) {
	    			return true;
	    		}
	    		else if(innerTables.contains(tablesInSelectionConditions.get(1)) && outerTables.contains(tablesInSelectionConditions.get(0))){
	    			return true;
	    		}
	    	}
	    	return false;
	    }
	 
	 /**
	  * 
	  * 
	  * @param Node type
	  * @param Int index
	  * @return String constraints
	  * @author sunanda
	  */ 
	public static String getConstraintsForSelectionConditionsUsingCount(Node n, int index) {
		//test code by Sunanda for count
		String validConstraints;
		Vector<ConstraintObject> constrObjList = new Vector<ConstraintObject>();
		ConstraintGenerator constrGen = new ConstraintGenerator();
		if(Configuration.getProperty("cntFlag").equalsIgnoreCase("true")) {
			if(n.getLeft().getType().equalsIgnoreCase(Node.getColRefType())){	
				Column col = n.getLeft().getColumn();	
				Table table = col.getTable();
				String type = col.getCvcDatatype();
				String tableName = col.getTableName();
				validConstraints= constrGen.getConstraintsForValidCountUsingTable(table, tableName, index , 0).toString();
				ConstraintObject constrnObjValid = new ConstraintObject();
				constrnObjValid.setLeftConstraint(validConstraints);
				// constrnObjValid.setRightConstraint("0");
				// constrnObjValid.setOperator(">");
				constrObjList.add(constrnObjValid);
			}
			if(n.getRight().getType().equalsIgnoreCase(Node.getColRefType())){	
				Column col = n.getRight().getColumn();
				
				
				Table table = col.getTable();
				String type = col.getCvcDatatype();
				String tableName = col.getTableName();
				validConstraints= constrGen.getConstraintsForValidCountUsingTable(table, tableName, index , 0).toString();
				ConstraintObject constrnObjValid = new ConstraintObject();
				constrnObjValid.setLeftConstraint(validConstraints);
				// constrnObjValid.setRightConstraint("0");
				// constrnObjValid.setOperator(">");
				constrObjList.add(constrnObjValid);
			}
		}
		String constraints = "";
		for(int i=0; i<constrObjList.size(); i++) {
			constraints = "( "+constrObjList.get(0).getOperator() + " "+ constrObjList.get(0).getLeftConstraint() +" "+ constrObjList.get(0).getRightConstraint() + " )\n\n" ;

		}
		return constraints;

	}

/**
 * 
 * 
 * @param cvc
 * @param condsInSubQ
 * @param subQuery
 * @return
 * @throws Exception
 */
	public static String getConstraintsForConditionsInSubquery(GenerateCVC1 cvc, Vector<Node> condsInSubQ,	QueryBlockDetails subQuery)
			throws Exception {
		String constraintString = "";
		ConstraintGenerator constrGen = new ConstraintGenerator();
		for(int i=0;i<condsInSubQ.size();i++){
			Node subQcond = condsInSubQ.get(i);

			Node left = subQcond.getLeft();
			Node right = subQcond.getRight();
			if(left.getType().equalsIgnoreCase(Node.getColRefType()) && right.getType().equalsIgnoreCase(Node.getColRefType())    
					&& subQcond.getOperator().equalsIgnoreCase("="))				/** If it is equi join condition */
				constraintString += GenerateJoinPredicateConstraints.getConstraintsForEquiJoins(cvc, subQuery, left, right, "");

			else if(left.getType().equalsIgnoreCase(Node.getColRefType()) && right.getType().equalsIgnoreCase(Node.getColRefType())    
					&& !subQcond.getOperator().equalsIgnoreCase("")) /**if non equi join constraint*/

				constraintString += GenerateJoinPredicateConstraints.getConstraintsForNonEquiJoins(cvc, subQuery, left, right, subQcond.getOperator(), "");

			else {
				String tableNameNo = left.getTableNameNo();
				int offset = cvc.getRepeatedRelNextTuplePos().get(tableNameNo)[1];
				// test code by sunanda for count
					for(int j=0; j< cvc.getNoOfTuples().get(tableNameNo) * subQuery.getNoOfGroups(); j++)	{	
					
					String constraints = getConstraintsForSelectionConditionsUsingCount(subQcond, j+offset);
	
					if( UtilsRelatedToNode.isStringSelection(subQcond,0)){/** If it is a string selection condition*/
	
						String subQueryConstraints = constrGen.genPositiveCondsForPred(subQuery, subQcond, j+offset);
						String result = cvc.getStringSolver().solveConstraints(subQueryConstraints,cvc.getResultsetColumns(), cvc.getTableMap()).get(0);
						//							StringConstraint s;
						
						if(Configuration.getProperty("cntFlag").equalsIgnoreCase("true")) {
							if(result.contains("assert")) {
								result = result.split("assert")[1].trim();
								result = result.substring(0,result.lastIndexOf(")")).trim();
								constraintString += "(assert (and "+ result + " " + constraints + "))";		

							}
						}
						else 
							constraintString += result;
					}
					else{
						String res = ConstraintGenerator.genPositiveCondsForPred(subQuery, subQcond, offset+j);
						if(Configuration.getProperty("cntFlag").equalsIgnoreCase("true"))
							res = " (and "+res+ " "+ constraints + ")";	 // changes for condition with count
						constraintString += constrGen.getAssertConstraint(res);
					}
				}
				
				// test code by sunanda ends
				
			}
		}
		return constraintString;
	}

	
		
	
}
