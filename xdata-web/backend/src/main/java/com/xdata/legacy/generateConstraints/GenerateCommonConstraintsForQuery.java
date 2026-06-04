package com.xdata.legacy.generateConstraints;
import com.xdata.util.TableMap;

import com.xdata.legacy.generateConstraints.TupleRange;
import com.xdata.legacy.generateConstraints.GenerateJoinPredicateConstraints;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.xdata.legacy.parsing.Column;
import com.xdata.legacy.parsing.ConjunctQueryStructure;
import com.xdata.legacy.parsing.JoinTreeNode;
import com.xdata.legacy.parsing.Node;
import com.xdata.legacy.parsing.RelationHierarchyNode;
import com.xdata.legacy.parsing.Table;
import com.xdata.legacy.testDataGen.PopulateTestData;
import com.xdata.legacy.testDataGen.GenerateCVC1;
import com.xdata.legacy.testDataGen.GenerateDataset_new;
import com.xdata.legacy.testDataGen.QueryBlockDetails;
import com.xdata.legacy.util.Configuration;
import com.xdata.legacy.util.ConstraintObject;
import com.xdata.legacy.util.Utilities;
/**
 * Generates constraints related to null conditions and database constraints
 * @author mahesh
 *
 */
public class GenerateCommonConstraintsForQuery {

	private static Logger logger=Logger.getLogger(GenerateCommonConstraintsForQuery.class.getName());
	/**
	 * This method generates the null constraints and database constaints
	 * like primary key, foreign key constraints. This method also changes 
	 * the noOfOutputTuples parameter in cvc based on foreign key relations
	 * 
	 * @param cvc
	 * @param unique
	 * @throws Exception
	 */
	public static void generateNullandDBConstraints(GenerateCVC1 cvc, Boolean unique) throws Exception {

		try{
			/** Add null constraints for the query */
			
			// getNullConstraintsForQuery(cvc);
			
			if( cvc.getCVCStr() == null)
				cvc.setCVCStr("");
			String CVCStr = cvc.getCVCStr();

			/**Add constraints related to database */
			CVCStr += AddDataBaseConstraints.addDBConstraints(cvc);
			
			cvc.setCVCStr(CVCStr);
		}catch (TimeoutException e){
			logger.log(Level.SEVERE,e.getMessage(),e);		
			throw e;
		}catch(Exception e){
			logger.log(Level.SEVERE,e.getMessage(),e);		
			throw e;
		}

	}
	/**
	 * 
	 * @param cvc Object used for generating constraints
	 * @param unique Whether all string variables have different values or not
	 * @return If data generation was successful or not 
	 * @throws Exception
	 */
	public static boolean generateDataSetForConstraints(GenerateCVC1 cvc, Boolean unique) throws Exception{
		String CVCStr = "";
		try{
			if( cvc.getCVCStr() == null)
				cvc.setCVCStr("");
			//String CVCStr = cvc.getCVCStr();
			CVCStr = cvc.getCVCStr();
			
			/** Solve the string constraints for the query */
			if(!cvc.getStringConstraints().isEmpty()) {
				cvc.getConstraints().add( ConstraintGenerator.addCommentLine("TEMP VECTOR CONSTRAINTS"));
				
				Vector<String> tempVector = cvc.getStringSolver().solveOrConstraintsForSMT( new Vector<String>(cvc.getStringConstraints()), cvc.getResultsetColumns(), cvc.getTableMap());
				cvc.getConstraints().addAll(tempVector);
				
			}

			cvc.setCVCStr(CVCStr);
			/** Add constraints, if there are branch queries*/
			// if( cvc.getBranchQueries().getBranchQuery() != null)
			// {
			// 	cvc.getConstraints().add( ConstraintGenerator.addCommentLine("BRANCHQUERY CONSTRAINTS"));
			// 	cvc.getConstraints().add( GenerateConstraintsRelatedToBranchQuery.addBranchQueryConstraints(cvc));
			// 	cvc.getConstraints().add( ConstraintGenerator.addCommentLine("END OF BRANCHQUERY CONSTRAINTS"));
			// }

			// if(cvc.getOuterBlock().isConstrainedAggregation())
			// 	cvc.getConstraints().add(addNoExtraTuple(cvc));		

			for(int k=0; k < cvc.getConstraints().size(); k++){
				CVCStr += "\n" + cvc.getConstraints().get(k);
			}
			
			
			
			/** Add not null constraints */
			cvc.getConstraints().add( ConstraintGenerator.addCommentLine(" NOT NULL CONSTRAINTS"));
			//CVCStr += GenerateCVCConstraintForNode.cvcSetNotNull(cvc);
			/* Removing NUll enumerations*/
			
				CVCStr += GenerateCVCConstraintForNode.primaryKeysSetNotNull(cvc);
				
			cvc.setDatatypeColumns( new ArrayList<String>() );
			
			String CVC3_HEADER = GetSolverHeaderAndFooter.generateSolver_Header(cvc, unique);
			
			CVCStr =  ConstraintGenerator.addCommentLine("MUTATION TYPE: " +cvc.getTypeOfMutation()) + CVC3_HEADER + CVCStr;

			CVCStr += GetSolverHeaderAndFooter.generateSolver_Footer(cvc);
			
			cvc.setCVCStr(CVCStr);
			
			/** Add extra tuples to satisfy branch queries constraints*/
			// for(int i = 0; i < cvc.getBranchQueries().getNoOfBranchQueries(); i++){

			// 	HashMap<Table, Integer> noOfTuplesAddedToTablesForBranchQueries[] = cvc.getBranchQueries().getNoOfTuplesAddedToTablesForBranchQueries();

			// 	for(Table tempTab : noOfTuplesAddedToTablesForBranchQueries[i].keySet())

			// 		cvc.putNoOfOutputTuples(tempTab.getTableName(), cvc.getNoOfOutputTuples(tempTab.getTableName()) + noOfTuplesAddedToTablesForBranchQueries[i].get(tempTab));
			// }
			
			Boolean success = false;
				
				/** Call CVC3 Solver with constraints */
				logger.log(Level.INFO,"cvc count =="+cvc.getCount());
				
				// TEMPCODE : Rahul Sharma : for removing duplicate constraints
				// CVCStr = removeDuplicateConstraints(CVCStr);
				
				String ds = "";
				int totalNoOfOutputTuples = 0;
				HashMap<String, Integer> mp = cvc.cloneNoOfOutputTuples();
				for(String table: mp.keySet()){
					totalNoOfOutputTuples += mp.get(table);
				}
				/************** TEMP CODE *******************/
				System.out.println("Dataset: "+cvc.getCount()+"\t"+cvc.getTypeOfMutation());
				System.out.println(cvc.cloneNoOfOutputTuples());
				System.out.println("***************************");
				/*********************************************/
				
				if(cvc.xdatawebFlag==0)
					Utilities.writeFile(Configuration.homeDir + "/temp_smt" + cvc.getFilePath() + "/z3_" + cvc.getCount() + ".smt", CVCStr);
				else
				{
					Utilities.writeFile(Configuration.homeDir + cvc.tempFilePathWeb + "/z3_" + cvc.getCount() + ".smt", CVCStr);
				}
				
				//modifyZ3SMTFile(Configuration.homeDir + "/temp_smt" + cvc.getFilePath() + "/z3_" + cvc.getCount() + ".smt");
				ds = cvc.getQueryId() + "," + cvc.getCount() + "," + cvc.getTypeOfMutation() + ",";
				// if(cvc.getCurrentMutant() != null){
				// 	ds += cvc.getCurrentMutant().getMutationLoc() + ",";
				// }else{
				// 	ds += ",";
				// }
				ds +=  totalNoOfOutputTuples + "," ;

				success= new PopulateTestData().killedMutantsForSMT(cvc, "z3_" + cvc.getCount() 
				+ ".smt", cvc.getQuery(), 
				"DS" + cvc.getCount(), cvc.getQueryString(), cvc.getFilePath(), (HashMap<String, Integer>) cvc.cloneNoOfOutputTuples(), cvc.getTableMap(), 
				cvc.getResultsetColumns(), cvc.getRepeatedRelationCount().keySet(),cvc.getDBAppparams(), ds) ;
				cvc.setOutput( cvc.getOutput() + success);
				cvc.setCount(cvc.getCount() + 1);
				cvc.calcTupleCount();
				
		
			
			/** remove extra tuples for Branch query */		
			// for(int i = 0; i < cvc.getBranchQueries().getNoOfBranchQueries(); i++){

			// 	HashMap<Table, Integer> noOfTuplesAddedToTablesForBranchQueries[] = cvc.getBranchQueries().getNoOfTuplesAddedToTablesForBranchQueries();

			// 	for(Table tempTab : noOfTuplesAddedToTablesForBranchQueries[i].keySet())

			// 		cvc.putNoOfOutputTuples(tempTab.getTableName(), cvc.getNoOfOutputTuples(tempTab.getTableName()) - noOfTuplesAddedToTablesForBranchQueries[i].get(tempTab));
			// }


			/**Upload DB as and when the constraints are generated **/
			ArrayList<String> newList = new ArrayList<String>();
			newList.add("DS"+(cvc.getCount()-1));
			logger.log(Level.INFO,"\n\n***********************************************************************\n");
			if(cvc.getConcatenatedQueryId() != null){
				logger.log(Level.INFO,"DATA SETS FOR QUERY "+cvc.getConcatenatedQueryId()+" ARE GENERATED");
			}else{
				logger.log(Level.INFO,"DATA SETS FOR QUERY ARE GENERATED");
			}
			logger.log(Level.INFO,"\n\n***********************************************************************\n");
//			GenerateDataset_new fp = new GenerateDataset_new( cvc.getFilePath()); // TEMOCODE : Rahul Sharma : Commented out this line, was not doing anything
			return success;
		}catch (TimeoutException e){
			logger.log(Level.SEVERE,"Timeout in generating dataset "+cvc.getCount()+" : "+e.getMessage());		

		}catch(Exception e){
			/************* TEST CODE *************
			 * To save z3_i.smt file even if Dataset generation fails;
			 */
			cvc.setCount(cvc.getCount()+1);
			
			logger.log(Level.SEVERE,e.getMessage());	
			// throw e;
		}
		return false;

	}

	
	/**
	 * TEMPCODE Rahul Sharma : To handle duplicate constraints
	 * @param CVCStr : Constraints
	 * @return : Constraints after removing duplicate ones
	 */
	private static String removeDuplicateConstraints(String CVCStr) {
		// TODO Auto-generated method stub
		String modifiedConstraints = "";
		String lines[] = CVCStr.split("((?<=\\n)|(?=\\n))");
		Set<String> s = new HashSet<String>();
		for(String line : lines) {
			if(line.equals("\n"))
				modifiedConstraints+=line;
			else if(line.startsWith(";"))
				modifiedConstraints+=line;
			else if(!line.startsWith("(declare-datatypes") && !line.startsWith(" (declare-datatypes") && !line.startsWith("(declare-fun") && !line.startsWith(" (declare-fun"))
				modifiedConstraints+=line;
//			else if(line.contentEquals("(assert \n") || line.contentEquals("(assert \r\n") || line.contentEquals("(assert \r"))
//				modifiedConstraints+="\n";
			else {
				if(!s.contains(line)) {
					s.add(line);
					modifiedConstraints+=line;
				}
			}
		}
		//System.out.println(modifiedConstraints);
		return modifiedConstraints;
	}
	
	
	public static boolean generateDataSetForConstraints(GenerateCVC1 cvc) throws Exception{
		try {
			generateNullandDBConstraints(cvc,false);
			
			return generateDataSetForConstraints(cvc, false);
		} catch (TimeoutException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);		
			throw e;
		} catch(Exception e) {
			logger.log(Level.SEVERE,e.getMessage(),e);		
			throw e;
		}
	}


	}
