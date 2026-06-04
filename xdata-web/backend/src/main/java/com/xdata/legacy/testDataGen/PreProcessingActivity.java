package com.xdata.legacy.testDataGen;
import com.xdata.util.TableMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.xdata.legacy.killMutations.GenerateDataForOriginalQuery;

import com.xdata.legacy.testDataGen.QueryBlockDetails;
//import com.xdata.legacy.killMutations.outerQueryBlock.SetOperatorMutations;
import com.xdata.legacy.parsing.ConjunctQueryStructure;
import com.xdata.legacy.parsing.ForeignKey;
import com.xdata.legacy.parsing.Node;
import com.xdata.legacy.parsing.QueryParser;
import com.xdata.legacy.parsing.QueryStructure;
import com.xdata.legacy.parsing.RelationHierarchyNode;
import com.xdata.legacy.parsing.Table;
import com.xdata.legacy.util.Configuration;
import com.xdata.legacy.util.TagDatasets;
import com.xdata.legacy.util.Utilities;

/**
 * This class contains functions to do pre processing actions before actual data generation is done
 * @author mahesh
 *
 */
public class PreProcessingActivity {

	private static Logger logger = Logger.getLogger(PreProcessingActivity.class.getName());
	public static GenerateCVC1 convertSetQuerytoSubquery(GenerateCVC1 left, GenerateCVC1 right,String subqueryOpNode) throws Exception{

		List<Node> projectedRight=right.getOuterBlock().getProjectedCols();
		List<Node> projectLeft=left.getOuterBlock().getProjectedCols();

		Iterator<Node> rightIter=projectedRight.iterator();
		Iterator<Node> leftIter=projectLeft.iterator();

		//Setting projected attributes as correlation conditions
		//TODO correlation conditions are treated as joins now - need to change this
		while(leftIter.hasNext() && rightIter.hasNext()) {
			Vector<Node> v=new Vector<Node>();
			v.add(leftIter.next());
			v.add(rightIter.next());
			List<ConjunctQueryStructure> rightConjuncts=right.getOuterBlock().getConjunctsQs();
			if(rightConjuncts ==null || rightConjuncts.isEmpty()) {

				ConjunctQueryStructure c=new ConjunctQueryStructure(new Vector<Node>());
				right.getOuterBlock().getConjunctsQs().add(c);
			}

			rightConjuncts.get(0).getEquivalenceClasses().add(v);
		}


		Node n = new Node();
		n.setType(subqueryOpNode);


		Node subQnode=new Node();
		subQnode.setQueryType(2);

		n.setLhsRhs(subQnode);
		List<ConjunctQueryStructure> leftConjuncts=left.getOuterBlock().getConjunctsQs();
		if(leftConjuncts ==null || leftConjuncts.isEmpty()) {

			ConjunctQueryStructure c=new ConjunctQueryStructure(new Vector<Node>());
			left.getOuterBlock().getConjunctsQs().add(c);
		}

		n.setQueryIndex(leftConjuncts.get(0).allSubQueryConds.size());
		leftConjuncts.get(0).allSubQueryConds.add(n);

		left.getOuterBlock().getWhereClauseSubQueries().add(right.getOuterBlock());
		GenerateCVC1 cvcSetop= left;

		//cvcSetop.getBranchQueries().intitializeDetails(cvcSetop);	
		//RelatedToPreprocessing.populateData(cvcSetop);

		//base relations will be added in initializeOtherDetais()
		cvcSetop.getOuterBlock().setBaseRelations(new ArrayList<String>());

		//TODO:put tableNames with proper query type and query index
		cvcSetop.getBaseRelation().putAll(right.getBaseRelation());
		//cvcSetop.getCurrentIndexCount().putAll(right.getCurrentIndexCount());
		//cvcSetop.getNoOfOutputTuples().putAll(right.getNoOfOutputTuples());
		HashMap<String, Integer[]> hm = right.getTableNames();
		Iterator <String> iterator = hm.keySet().iterator();
		while (iterator.hasNext())
		{  
			String key = iterator.next().toString();  
			Integer[] value = hm.get(key);  
			if(cvcSetop.getTableNames().containsKey(key))
			{
				//FIXME:What if number of repeated relations is in double digits
				Integer i = Integer.parseInt(key.substring(key.length()-1));
				i += 1;
				key = key.substring(0, key.length()-1) + i;
				cvcSetop.getTableNames().put(key, value);
			}
			else
				cvcSetop.getTableNames().put(key,value);
		}

		PreProcessingActivity.setOriginalTablesForSetQuery(cvcSetop, right);


		cvcSetop.initializeOtherDetails();
		// RelatedToPreprocessing.segregateSelectionConditions(cvcSetop);

		//Correcting differences observed
		ArrayList<RelationHierarchyNode> rhnList = new ArrayList<RelationHierarchyNode>();
		rhnList.add(cvcSetop.getOuterBlock().getWhereClauseSubQueries().get(0).getTopLevelRelation());
		cvcSetop.getOuterBlock().getTopLevelRelation().setNotExistsSubQueries(rhnList);
		cvcSetop.getOuterBlock().getWhereClauseSubQueries().get(0).setTopLevelRelation(null);
		//cvcSetop.getOuterBlock().setBaseRelations(left.getOuterBlock().getBaseRelations());
		//temp
		cvcSetop.getOuterBlock().getConjunctsQs().get(0).getAllSubQueryConds().get(0).getLhsRhs().setQueryIndex(0);
		cvcSetop.getOuterBlock().getConjunctsQs().get(0).getAllSubQueryConds().get(0).setQueryIndex(-1);
		//Conjunct c=new Conjunct(new Vector<Node>());
		//ArrayList<Conjunct> conjList = new ArrayList<Conjunct>();
		//cvcSetop.getOuterBlock().getWhereClauseSubQueries().get(0).setConjuncts(conjList);
		//cvcSetop.getOuterBlock().getWhereClauseSubQueries().get(0).getConjuncts().add(c);
		cvcSetop.getColNullValuesMap().putAll(right.getColNullValuesMap());

		//Also works for other set operator queries, only not null required
		cvcSetop.getqStructure().setOperator = "UNION";
		//cvcSetop.getqStructure().setOperator = null;
		return cvcSetop;
	}


	public static void setOriginalTablesForSetQuery(GenerateCVC1 left,GenerateCVC1 right){
		left.setTablesOfOriginalQuery( new Vector<Table>() );
		left.getTablesOfOriginalQuery().addAll(left.getQuery().getFromTables().values());
		left.getTablesOfOriginalQuery().addAll(right.getQuery().getFromTables().values());
		Iterator<Table> iter1 = left.getTablesOfOriginalQuery().iterator();
		while(iter1.hasNext())
		{
			Table t = (Table)iter1.next();
			if(t.hasForeignKey())
			{
				Map<String, ForeignKey> fks = t.getForeignKeys();
				Iterator<ForeignKey> iter2 = fks.values().iterator();
				while(iter2.hasNext())
				{
					ForeignKey fk = (ForeignKey)iter2.next();
					if(!left.getTablesOfOriginalQuery().contains(fk.getReferenceTable()))
					{
						left.getTablesOfOriginalQuery().add(fk.getReferenceTable());
						iter1 = left.getTablesOfOriginalQuery().iterator();
					}
				}
			}
		}

		right.setTablesOfOriginalQuery(left.getTablesOfOriginalQuery());

	}	

	//Application Testing
	public static Vector<Node> preProcessingActivityForSchema(GenerateCVC1 cvc) throws Exception{


		/** check if there are branch queries and upload the details */
		//TODO: This is for application testing, a flag should be set
		//for calling this function
		// RelatedToPreprocessing.uploadBranchQueriesDetails(cvc);
		/** To store input query string */
		String queryString = "";
		boolean isSetOp = false;
		BufferedReader input = null;
		StringBuffer queryStr = new StringBuffer();
		try {
			input =  new BufferedReader(new FileReader(Configuration.homeDir+"/temp_smt" + cvc.getFilePath() + "/queries.txt"));
			/**Read the input query */
			while (( queryString = input.readLine()) != null){
				queryStr.append(queryString+"\n");
			}
			if(queryStr != null){
				/**Create a new query parser
			cvc.setqParser( new QueryParser(cvc.getTableMap()));
			cvc.getqParser().parseQuery("q1", queryStr.toString());
				 */
				//Trying with new query structure
				QueryStructure qStructure=new QueryStructure(cvc.getTableMap());
				qStructure.buildQueryStructure("q1", queryStr.toString(),cvc.getDBAppparams());
				cvc.setqStructure(qStructure);


				cvc.getDBAppparams().setSchemaProjectedColumns(cvc.getqStructure().getProjectedCols());				
				return cvc.getDBAppparams().getSchemaProjectedColumns();

				//end


			}
		}catch(Exception e){
			logger.log(Level.SEVERE,""+e.getStackTrace(),e);
			//e.printStackTrace();
			throw e;
		} 
		finally {
			if(cvc != null &&  cvc.getConnection() != null && ! cvc.getConnection().isClosed()){
				cvc.closeConn();
			}
			if(input != null)
				input.close();
		}
		return cvc.getDBAppparams().getSchemaProjectedColumns();
	}
	//end

	public static void preProcessingActivity(GenerateCVC1 cvc) throws Exception{


		/** check if there are branch queries and upload the details */
		//TODO: This is for application testing, a flag should be set
		//for calling this function
		// if(Configuration.calledFromApplicationTester) {
		// 	RelatedToPreprocessing.uploadBranchQueriesDetails(cvc);
		// }
		/** To store input query string */
		String queryString = "";
		boolean isSetOp = false;
		BufferedReader input = null;
		StringBuffer queryStr = new StringBuffer();
		try {
			input =  new BufferedReader(new FileReader(Configuration.homeDir+"/temp_smt" + cvc.getFilePath() + "/queries.txt"));
			/**Read the input query */
			while (( queryString = input.readLine()) != null){
				queryStr.append(queryString+"\n");
			}
			//System.out.println("..................."+queryStr+".......................input");
			
			if(queryStr != null){
				/**Create a new query parser
			cvc.setqParser( new QueryParser(cvc.getTableMap()));
			cvc.getqParser().parseQuery("q1", queryStr.toString());
				 */
				//Trying with new query structure
				QueryStructure qStructure=new QueryStructure(cvc.getTableMap());
				qStructure.buildQueryStructure("q1", queryStr.toString(),cvc.getDBAppparams());
				qStructure.buildSQLevel(); //parismita
				cvc.setqStructure(qStructure);

				//Application Testing --- not need to execute further statements for schema parsing

				if(cvc.getDBAppparams().isSchemasetFlag()== true){
					cvc.getDBAppparams().setSchemaProjectedColumns(cvc.getqStructure().getProjectedCols());				
					return;
				}
				//end

				if(cvc.getqStructure().setOperator!=null && cvc.getqStructure().setOperator.length()>0){
					qStructure.isSetOp= true;
					isSetOp = true;
				}
				cvc.initializeQueryDetailsQStructure(cvc.getqStructure() );
				
				ArrayList<ForeignKey> foreignK = new ArrayList<ForeignKey>();

				for(ForeignKey key : cvc.getForeignKeysModified()) {
					if(!foreignK.contains(key)) {
						foreignK.add(key);
					}
				}
				cvc.setForeignKeysModified(foreignK);
				logger.log(Level.INFO," Query Parser output = "+ cvc.getqStructure());
				/**Delete data sets in the path*/
				//RelatedToPreprocessing.deleteDatasets(cvc.getFilePath());

				logger.log(Level.INFO,"File path = "+cvc.getFilePath());
				/**Check if the input query contains set operators */
				logger.log(Level.INFO,"cvc.getqParser().setOperator = " + cvc.getqStructure().setOperator);
				if(cvc.getqStructure().setOperator!=null && cvc.getqStructure().setOperator.length()>0){
					qStructure.isSetOp= true;
					isSetOp = true;
					cvc.initializeQueryDetailsQStructureForSet(cvc.getqStructure().leftQuery ,"left");
					cvc.initializeQueryDetailsQStructureForSet(cvc.getqStructure().rightQuery ,"right");
				}

				

				// cvc.getBranchQueries().intitializeDetails(cvc);

				/**Populate the values from the data base 
				 * Needed so that the generated values looks realistic */	
				//RelatedToPreprocessing.populateData(cvc); //commented by rambabu for regression test to eliminate duplicate resultset columns error

				/**Initialize cvc3 headers etc>,*/				
				cvc.initializeOtherDetails();
				cvc.setConstraintSolver(Configuration.getProperty("smtsolver"));					

				if(Configuration.getProperty("smtsolver").equalsIgnoreCase("cvc3")){
					cvc.setSolverSpecificCommentCharacter("%");
				}else{
					cvc.setSolverSpecificCommentCharacter(";");
				}

				/** Segregate selection conditions in each query block */
				
				cvc.generateDatasetsToKillMutations();
					

				
				cvc.closeConn();
			}
		}catch(Exception e){
			logger.log(Level.SEVERE,""+e.getStackTrace(),e);
			e.printStackTrace();
//			throw e;
		} 
		finally {
			if(cvc != null &&  cvc.getConnection() != null && ! cvc.getConnection().isClosed()){
				cvc.closeConn();
			}
			if(input != null)
				input.close();
		}
	}

		
}
