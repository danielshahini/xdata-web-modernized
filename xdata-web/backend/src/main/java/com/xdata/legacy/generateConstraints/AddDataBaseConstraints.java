package com.xdata.legacy.generateConstraints;
import com.xdata.util.TableMap;

import java.util.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.xdata.legacy.parsing.Column;
import com.xdata.legacy.parsing.ForeignKey;
import com.xdata.legacy.parsing.Node;
import com.xdata.legacy.parsing.Table;
import com.xdata.legacy.testDataGen.GenerateCVC1;
import com.xdata.legacy.util.Configuration;
import com.xdata.util.TableMap;

import com.microsoft.z3.*;

/**
 * This class contains the methods for generating constraints related to database such as foreign key constraints and check constraints
 * @author mahesh
 *
 */

public class AddDataBaseConstraints {

	private static Logger logger=Logger.getLogger(AddDataBaseConstraints.class.getName());
    private static boolean usingCnt = false;
    
    
	/**
	 * Generates constraints specific to the database
	 * @param cvc
	 * @return
	 * @throws Exception
	 */
	public static String addDBConstraints(GenerateCVC1 cvc) throws Exception{

		String dbConstraints = "";		


		/** The primary keys have to be distinct across all the tuples needed to satisfy constrained aggregation
		 * If there is no constrained aggregation, then primary key values can be same or distinct
		 * But if there is constrained aggregation then primary key has to be distinct across all tuples 
		 * Otherwise if solver chooses same value then the input tuples may not satisfy constrained aggregation
		 * These constraints must be added before foreign key constraints and this need not be done for the extra tuples added to satisfy constrained aggregation 
		 * These constraints must be added to only that occurrence of the table
		 * FIXME: Killing partial group by case 2 is a special case here
		 * FIXME: We should consider repeated relation occurrences here*/
		String unConstraints = ConstraintGenerator.addCommentLine("UNIQUE CONSTRAINTS FOR PRIMARY KEY TO SATISFY CONSTRAINED AGGREGATION");		

		try{
			
			
			unConstraints +=  ConstraintGenerator.addCommentLine("END OF UNIQUE CONSTRAINTS  FOR PRIMARY KEY TO SATISFY CONSTRAINED AGGREGATION");
			
			/**Generate foreign key constraints */
			dbConstraints +=  ConstraintGenerator.addCommentLine("FOREIGN KEY CONSTRAINTS");
			dbConstraints += generateConstraintsForForeignKeys(cvc);
			dbConstraints +=  ConstraintGenerator.addCommentLine("END OF FOREIGN  KEY CONSTRAINTS");
			
			ConstraintGenerator constraintGenerator = new ConstraintGenerator();
		    dbConstraints += ConstraintGenerator.addCommentLine("DOMAIN CONSTRAINTS");
			Vector<Quantifier> domainConstraints = constraintGenerator.getDomainConstraintsforZ3(cvc);
			dbConstraints += domainConstraints.stream().map(
	        		quantifier -> "(assert "+quantifier.toString()+")"
	        		).collect(Collectors.joining("\n\n"));
			dbConstraints += ConstraintGenerator.addCommentLine("END OF DOMAIN CONSTRAINTS");

			dbConstraints += constraintGenerator.generateCVCForNullCheckInHaving();


			/** Now add primary key constraints */
			dbConstraints +=  ConstraintGenerator.addCommentLine("PRIMARY KEY CONSTRAINTS");
			
			//added by deeksha
			if(Configuration.getProperty("cntFlag").equalsIgnoreCase("true")){
				 usingCnt = true;
			 }else {
				 usingCnt = false;
			 }
			if(usingCnt)
			{
				dbConstraints += generateConstraintsForPrimaryKeysNew(cvc);
			}
			// else
			// {
			// 	dbConstraints += generateConstraintsForPrimaryKeys(cvc);
			// }
				
			//end
			dbConstraints +=  ConstraintGenerator.addCommentLine("END OF PRIMARY KEY CONSTRAINTS");
            
			if(usingCnt)
			{
				dbConstraints +=  ConstraintGenerator.addCommentLine("UPPER BOUND LOWER BOUND CONSTRAINTS");
				dbConstraints += generateBoundConstraints(cvc);
				dbConstraints +=  ConstraintGenerator.addCommentLine("END OF UPPER BOUND LOWER BOUND CONSTRAINTS");
			}
			
		} catch(Exception e) {
			logger.log(Level.SEVERE,"\n Exception in AddDatabaseConstraints.java:Function addDBConstraints :",e);
			throw e;
		}		
		return dbConstraints + unConstraints;		
	}

	
	
//deeksha testcode for pk constraints
	
	public static String generateBoundConstraints(GenerateCVC1 cvc) throws Exception
	{
		String boundConstraints = "";
		String positiveConstraints = "";
		try
		{
			//for all tables generate constraint for limit
			ConstraintGenerator constraintGenerator = new ConstraintGenerator();
			Context ctx = ConstraintGenerator.ctx;
			
			
			for(int i=0; i < cvc.getResultsetTables().size(); i++){

				/** Get this data base table */
				Table table = cvc.getResultsetTables().get(i);

				/**Get table name */
				String tableName = table.getTableName();
				
				
				/**Get the number of tuples for this relation  */
				int noOfTuples = cvc.getNoOfOutputTuples(tableName);
				
				Column cntCol = table.getColumn("XDATA_CNT");
				
				ArithExpr[] addExpr = new ArithExpr[noOfTuples];
				
				
				//constraints for value of cnt > 0
				
				
				Vector<BoolExpr> tempCons = new Vector<BoolExpr>();
				for(int tuple=0 ; tuple< noOfTuples ; tuple++)
				{
					Expr tupleNo = (IntExpr) ConstraintGenerator.ctx.mkInt(tuple+1);
					if(Configuration.isEnumInt.equalsIgnoreCase("true")){
						EnumSort currentSort = (EnumSort)ConstraintGenerator.ctxSorts.get(cvc.enumArrayIndex);
						tupleNo = ctx.mkConst(cvc.enumIndexVar+Integer.toString(tuple+1),currentSort);
					}
					tempCons.add(ctx.mkGe((ArithExpr) ConstraintGenerator.smtMap(cntCol, tupleNo) , (ArithExpr) ctx.mkInt(0)));
					tempCons.add(ctx.mkLe((ArithExpr) ConstraintGenerator.smtMap(cntCol, tupleNo) , (ArithExpr) ctx.mkInt(1))); // 1 in case of primary key constraints
					addExpr[tuple] = (ArithExpr) (ConstraintGenerator.smtMap(cntCol, tupleNo));
				}
				positiveConstraints += "\n(assert " + (ctx.mkAnd(tempCons.toArray(new BoolExpr[tempCons.size()]))).toString() + ")\n";
				if(addExpr.length >0)
				{
					Expr additionExpr = ctx.mkAdd(addExpr);
					
					//lower bound
					// temporary comment by Sunanda lb
	//				IntExpr lb = (IntExpr) ConstraintGenerator.ctx.mkInt((noOfTuples+1)/2);
					
					// IntExpr lb = (IntExpr) ConstraintGenerator.ctx.mkInt(noOfTuples); // changed by sunanda
					// tableName = tableName.toUpperCase() + "1";
					// if(cvc.getNoOfTuples().get(tableName) == null)
					// 	lb = (IntExpr) ConstraintGenerator.ctx.mkInt(0);
					// else {
					// 	lb = (IntExpr) ConstraintGenerator.ctx.mkInt(cvc.getNoOfTuples().get(tableName));

					// }
					IntExpr lb = (IntExpr) ConstraintGenerator.ctx.mkInt(0);
					boundConstraints += "\n(assert " + ctx.mkGt((ArithExpr) additionExpr, lb).toString() +" )\n";
					// made ge to gt for testing - not allowing #tuples to be 0
					
					//upper bound
					IntExpr ub = (IntExpr) ConstraintGenerator.ctx.mkInt(noOfTuples); // -1 done by sunanda
					boundConstraints += "\n(assert " + ctx.mkLe((ArithExpr)additionExpr, ub).toString() + " )\n";
				}
			}
			
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE,"\n Exception in AddDatabaseConstraints.java: Function generateBoundConstraints : ",e);
			throw e;
		}
		
		return positiveConstraints + boundConstraints;
	}

	/**
	 * This method generates constraints for the primary keys of the tables used in the query
	 * @param cvc
	 * @return
	 */
	public static String generateConstraintsForPrimaryKeysNew(GenerateCVC1 cvc) throws Exception{
		boolean primaryKeyMethod = false;

		String pkConstraint = "";
		try{
			
			
			ConstraintGenerator constraintGenerator = new ConstraintGenerator();
			Context ctx = ConstraintGenerator.ctx;
			/** For each table in the result tables */
			for(int i=0; i < cvc.getResultsetTables().size(); i++){

				/** Get this data base table */
				Table table = cvc.getResultsetTables().get(i);

				/**Get table name */
				String tableName = table.getTableName();
				
				/**Get the primary keys of this table*/
				ArrayList<Column> primaryKey = new ArrayList<Column>( table.getPrimaryKey() );
				
				ArrayList<Column> primaryKeys = new ArrayList<Column>();
				
				for (Column element : primaryKey) { 
					
		            if (!primaryKeys.contains(element)) { 
		            	primaryKeys.add(element); 
		            	
		            } 
		        } 

				/**If there are no primary keys, then nothing need to be done */
				if( primaryKeys.size() <= 0)
					continue;

				/**If there are no tuples for this query */			
				if( cvc.getNoOfOutputTuples(tableName)== -1 && cvc.getTablesOfOriginalQuery().contains(table))
					 cvc.putNoOfOutputTuples(tableName, 1); 

				else if ( cvc.getNoOfOutputTuples(tableName)==-1)				
					 cvc.putNoOfOutputTuples(tableName, 0); 
				
				/**Get the number of tuples for this relation  */
				int noOfTuples = cvc.getNoOfOutputTuples(tableName);

				/**If there is a single tuple then nothing need to be done */
				if(noOfTuples == 1)
					continue ;
				
			   Vector<BoolExpr> andConstraints = new Vector<BoolExpr>();
			   String pkconstFor2 = "";

				/** for every two tuple either pk is diff or anyone of them is invalid having cnt 0*/
				for(int tupleNo1 = 1 ; tupleNo1 < noOfTuples ; tupleNo1++)
				{
					for(int tupleNo2 = tupleNo1+1 ; tupleNo2 <= noOfTuples ; tupleNo2++)
					{   
						Expr smtTup1 = (IntExpr) ConstraintGenerator.ctx.mkInt(tupleNo1);
						Expr smtTup2 = (IntExpr) ConstraintGenerator.ctx.mkInt(tupleNo2);
						if(Configuration.isEnumInt.equalsIgnoreCase("true")){
							EnumSort currentSort = (EnumSort)ConstraintGenerator.ctxSorts.get(cvc.enumArrayIndex);
							smtTup1 = ctx.mkConst(cvc.enumIndexVar+Integer.toString(tupleNo1),currentSort);
							smtTup2 = ctx.mkConst(cvc.enumIndexVar+Integer.toString(tupleNo2),currentSort);

						}
						
						Vector<BoolExpr> orConstraints = new Vector<BoolExpr>();
						
						//to traverse all col and gen constraint for atleast one pk attribute diff
						Vector<BoolExpr> or1Constraints = new Vector<BoolExpr>();
						for(Column col : primaryKeys)
						{
							or1Constraints.add(ctx.mkDistinct(ConstraintGenerator.smtMap(col, smtTup1), ConstraintGenerator.smtMap(col, smtTup2)));
							
						}
						//add or1 in final or
						orConstraints.add(ctx.mkOr(or1Constraints.toArray(new BoolExpr[or1Constraints.size()])));
						
						//to add condition for anyone of two as invalid
						Vector<BoolExpr> or2Constraints = new Vector<BoolExpr>();
						Column cntCol = table.getColumn("XDATA_CNT");
						or2Constraints.add(ctx.mkEq((ArithExpr) ConstraintGenerator.smtMap(cntCol, smtTup1) , (ArithExpr) ctx.mkInt(0)));
						or2Constraints.add(ctx.mkEq((ArithExpr) ConstraintGenerator.smtMap(cntCol, smtTup2) , (ArithExpr) ctx.mkInt(0)));
						
						//add or2 to final or
						orConstraints.add(ctx.mkOr(or2Constraints.toArray(new BoolExpr[or2Constraints.size()])));
						
						
						andConstraints.add(ctx.mkOr(orConstraints.toArray(new BoolExpr[orConstraints.size()])));
						// System.out.print(Configuration.getProperty("primarykey"));
						// if(primaryKeyMethod == true){
						// 	pkconstFor2 += constraintGenerator.getUserDefinedComparisionOperator(">", primaryKeys.get(0),
						// 	ConstraintGenerator.smtMap(primaryKeys.get(0), smtTup1).toString(), ConstraintGenerator.smtMap(primaryKeys.get(0), smtTup2).toString());
						// }
					}
				}
				if(primaryKeyMethod == true){
					pkconstFor2 += getPrimaryKeyConstUsingNonEquiMethod(primaryKeys, noOfTuples) ;
				}
				BoolExpr exprr = ctx.mkAnd(andConstraints.toArray(new BoolExpr[andConstraints.size()]));
				if(pkconstFor2.equalsIgnoreCase("")){
					pkConstraint += "(assert\n "+ exprr.toString() + "\n)\n";

				}else{
					pkConstraint += "(assert\n "+ pkconstFor2 + "\n)\n";

				}		
						
				}
			
			
		}catch(Exception e){
			logger.log(Level.SEVERE,"\n Exception in AddDatabaseConstraints.java: Function generateConstraintsForPrimaryKeys : ",e);
			throw e;
		}
		return pkConstraint;
	}
//testcode ends

	public static String getPrimaryKeyConstUsingNonEquiMethod(ArrayList<Column> primaryKeys, int noOfTuples) {
		// TODO Auto-generated method stub
		String constr = "";
		ConstraintGenerator constgen = new ConstraintGenerator();
		for(Column pcol: primaryKeys){
			String tempConstr = "";
			int t1=1;
			while(t1<noOfTuples){
				int t2=t1+1;
				Expr smtTup1 = (IntExpr) ConstraintGenerator.ctx.mkInt(t1);
				Expr smtTup2 = (IntExpr) ConstraintGenerator.ctx.mkInt(t2);
				tempConstr += "\t\t"+constgen.getUserDefinedComparisionOperator("<", pcol,
					ConstraintGenerator.smtMap(pcol, smtTup1).toString(), ConstraintGenerator.smtMap(pcol, smtTup2).toString()) + "\n";
				t1++;			
			}
			constr += "\n\t(and \n\t" + tempConstr + ")\n";
		}
		constr = "\n(or "+ constr + ")\n\n";
		
		return constr;
	}


	/**
	 * Generates constraints to satisfy foreign key relationships
	 * @param cvc
	 * @return
	 */
	public static String generateConstraintsForForeignKeys(GenerateCVC1 cvc) throws Exception{
		String fkConstraint = "";/** To store constraints for foreign keys*/
		try{
			/** Get the list of foreign keys*/
			ArrayList<ForeignKey> foreignKeys = cvc.getForeignKeysModified();
			TableMap tableMap = cvc.getTableMap();
			int size = tableMap.foreignKeyGraph.topSort().size();

			HashMap<String,Vector<ForeignKey>> fmap = new HashMap<String,Vector<ForeignKey>>();
			for(int i=0; i<foreignKeys.size(); i++) {
				ForeignKey foreignKey = foreignKeys.get(i);
				String fkTableName = foreignKey.getFKTablename();
				if(fmap.containsKey(fkTableName))
					fmap.get(fkTableName).add(foreignKey);
				else {
					fmap.put(fkTableName, new Vector<ForeignKey>());
					fmap.get(fkTableName).add(foreignKey);
				}
			}
			for (int fg=(size-1); fg >= 0 ;fg--){
				String tableName = tableMap.foreignKeyGraph.topSort().get(fg).toString();
				
				if(fmap.containsKey(tableName)) {
					
					/** Get this foreign key */
					Vector<ForeignKey> fkeys = fmap.get(tableName);
					for(int i=0; i<fkeys.size(); i++) {

						/** Get foreign key table details */
						ForeignKey foreignKey = fkeys.get(i);
						String fkTableName = foreignKey.getFKTablename();
						
						/**Get the number of tuples of foreign key table*/
						Integer[] fkCount = {0};/**one variable is sufficient, but primitives are immutable*/

						/**If FK Table do not contain any tuple, at least one tuple should be there */
						if( cvc.getNoOfOutputTuples(fkTableName) == -1 || cvc.getNoOfOutputTuples(fkTableName) == 0) {

							fkCount[0] = 1;

							/** Update the number of tuples data structure */
							cvc.putNoOfOutputTuples(fkTableName,1);				
						}
						else/**Get the number of tuples of FK table */
							fkCount[0] = cvc.getNoOfOutputTuples(fkTableName);

						/**check if the foreign key table is present across any query block
						 * Also we need to check if all the attributes of the foreign key are involved in joins in that query block
						 * If yes then we should not add the extra tuples in the primary key table  because the join conditions ensure that the foreign key relationship is satisfied 
						 * If no, we should add the extra tuples in the primary key table 
						 * These extra tuples are added for that occurrence of the relation in the query 
						 * In either case we will decrement the foreign key table count as for that many tuples we ensured the primary key relationship*/


						fkConstraint += generateForeignKeyConstraints(cvc, foreignKey, fkCount);
					}
				}	
				
			}
			
		}catch(Exception e){
			System.out.println(e.getClass().getName());
			logger.log(Level.SEVERE,"\n Exception in AddDatabaseConstraints.java: Function generateConstraintsForForeignKeys : ",e);
			throw e;
		}
		return fkConstraint;
	}

	/**
	 * gets the foreign key constraint for the given foreign key with given foreign key count
	 * @param cvc
	 * @param foreignKey
	 * @param fkCount
	 * @return
	 */
	public static String generateForeignKeyConstraints(GenerateCVC1 cvc, ForeignKey foreignKey, Integer[] fkCount) throws Exception{

		try{
			/**If there are no tuples in the foreign key table*/
			if( fkCount[0] <= 0)
				return "";	

			/**stores the constraint*/
			String fkConstraint = "";
			/** Get foreign key table details */
			String fkTableName = foreignKey.getFKTablename();		

			/**get the list of equi join conditions on this foreign key table*/
			Vector< Vector< Node > > equiJoins = cvc.getEquiJoins().get(fkTableName);

			/**stores whether there are any equi joins conditions between foreign key and primary key columns*/
			HashMap<String, Boolean> presentinJoin = new HashMap<String, Boolean>();

			String violate = "";
			/**If there are tuples left out in the foreign key table
			 * Get constraints for these extra tuples */
			if( fkCount[0] > 0){

				/**get the repeated relations for this foreign key table*/
				int repeatedCount = -1;
				if( cvc.getRepeatedRelationCount().get(fkTableName) != null)
					repeatedCount = cvc.getRepeatedRelationCount().get(fkTableName);


				/**check for each occurrence of this foreign key table, if*/
				/**joins between foreign key and primary key table are not true then add foreign key constraints for that relation occurrence*/
				for(int i = 1; i <= repeatedCount; i++){

					String fkTableNameNo = fkTableName + i;

					/**means this foreign key relation occurrence does not have join conditions*/
					if(!presentinJoin.containsKey(fkTableNameNo)) {

						/**get the total count for this relation occurrence*/
						int count = getTotalNumberOfTuples(cvc, fkTableNameNo);

						/**decrement count*/
						fkCount[0] -= count;

						/**get the foreign key constraint*/
						fkConstraint += getFkConstraint(cvc, foreignKey, fkTableNameNo, count, 0);

						/**get the primary key tuple offset */
						int pkOffset = cvc.getNoOfOutputTuples(foreignKey.getReferenceTable().getTableName()) - fkCount[0];

						//violate += getNegativeCondsForExtraTuples(cvc, foreignKey, fkTableNameNo, count, 0, pkOffset);			
					}
				}

				/**once done for all relation occurrences in original query, then 
				 * get constraints for the extra tuples (Added due to foreign key relationship)*/
				/**get the number of tuples for which foreign keys are already added*/
				int fOffset = cvc.getNoOfOutputTuples(foreignKey.getFKTablename()) - fkCount[0];

				/**get the foreign key constraint*/
				fkConstraint += getFkConstraint(cvc, foreignKey, null, fkCount[0], fOffset);
			}

			return fkConstraint + "\n"+ violate;
		}catch(Exception e){
			logger.log(Level.SEVERE,e.getMessage(),e);		
			throw new Exception("Internal Error", e);
		}
	}

	/**
	 * Gets the total number of tuples for this foreign key table occurrence
	 * @param cvc
	 * @param fkTableNameNo
	 * @return
	 */
	public static int getTotalNumberOfTuples(GenerateCVC1 cvc,	String fkTableNameNo) {

		if(fkTableNameNo == null)
			return -1;

		/**get the query block type and query index of in which this foreign key table is present*/
		int queryType = cvc.getTableNames().get(fkTableNameNo)[0];
		int queryIndex = cvc.getTableNames().get(fkTableNameNo)[1];


		/**get the total number of tuples of this relation occurrence in this query block*/
		int totalCount = -1;

		if( queryType == 0) /** means the foreign key table is present in outer block of query*/
			totalCount = cvc.getNoOfTuples().get(fkTableNameNo) * cvc.getOuterBlock().getNoOfGroups();

		else if( queryType == 1)/** the foreign key table is present in from clause nested sub query block*/
			totalCount = cvc.getNoOfTuples().get(fkTableNameNo) * cvc.getOuterBlock().getFromClauseSubQueries().get( queryIndex).getNoOfGroups();

		else if( queryType == 2)/** the foreign key table is present in where clause nested sub query block*/
			totalCount = cvc.getNoOfTuples().get(fkTableNameNo) * cvc.getOuterBlock().getWhereClauseSubQueries().get( queryIndex).getNoOfGroups();

		return totalCount;
	}


	public static String getFkConstraint(GenerateCVC1 cvc, ForeignKey foreignKey, String fkTableNameNo, int fkCount, int fOffset) {

		/**used to store foreign key occurrence*/
		String fkConstraint = "";

		if(fkCount <= 0)
			return fkConstraint;

		/** Get foreign key table details */
		String ftableName = foreignKey.getFKTablename();					

		/** Get primary key table details*/
		String pkTableName = foreignKey.getReferenceTable().getTableName();		

		/** Get details about the number of extra tuples to be added for primary key table */	
		int pkCount = 0;

		/**To indicate the tuple starting position in the primary key table*/
		int offset = 0;

		/** update the extra tuples to be added for the primary key table*/
		/**if there are no tuple*/
		pkCount = fkCount;
		if( cvc.getNoOfOutputTuples(pkTableName) == -1 || cvc.getNoOfOutputTuples(pkTableName) == 0){
			offset = 1;
		}

		else{
			offset = cvc.getNoOfOutputTuples(pkTableName) + 1;
		}

		/**updates the number of tuples for primary key and foreign key table*/
		updateTheNumberOfTuples(cvc, pkTableName, fkCount, pkCount);


		/**get the tuple offsets for both primary key table and foreign key table based on the relation occurrences*/
		int fkOffset;

		/**get repeated offset for foreign key table*/
		if(fkTableNameNo != null)
			fkOffset = cvc.getRepeatedRelNextTuplePos().get(fkTableNameNo)[1];
		else
			fkOffset = fOffset + 1;
		
		//deeksha testcode for new foreign key constraints

		String fkConstraintNew = "";
		
			Vector<String> fkConstraintsVectorNew = getSMTforForeignKeyZ3New(cvc, foreignKey, fkCount, fkOffset, offset);
	        fkConstraintNew = fkConstraintsVectorNew.stream().map(
	        		assertion -> "(assert "+assertion.toString()+")"
	        		).collect(Collectors.joining("\n\n"));
		
		
		//testcode ends
		
		return fkConstraintNew;
	}	


	//deeksha testcode for new foreign key constraints

	public static Vector<String> getSMTforForeignKeyZ3New(GenerateCVC1 cvc, ForeignKey foreignKey, int fkCount, int fkOffset, int pkOffset) {

		/** Get foreign key column details */					
		Vector<Column> fCol = (Vector<Column>) foreignKey.getFKeyColumns().clone();
        
		HashMap<String, FuncDecl> ctxFunDeclHashMap = ConstraintGenerator.getCtxFuncDecl(); // added by sunanda for ISNULL

		/** Get primary key column details*/
		Vector<Column> pCol = (Vector<Column>) foreignKey.getReferenceKeyColumns().clone();
        
		ConstraintGenerator constraintGenerator = new ConstraintGenerator();
				
		//Vector<Column> temp = new Vector<Column>();
		Vector<String> fkConstraints = new Vector<String>();
		Vector<BoolExpr> orConstraints = new Vector<BoolExpr>();
		if(Configuration.getProperty("cntFlag").equalsIgnoreCase("true")){
			 usingCnt = true;
		 }else {
			 usingCnt = false;
		 }
		Context ctx = ConstraintGenerator.ctx;

			if (fCol.size()>0) {
				
				//to get tablename1 and tablename2 and no of tuples in them
				Column fSingleCol = fCol.get(0);
				Column pSingleCol = pCol.get(0);
				String tableName1 = fSingleCol.getTable().getTableName();
				String tableName2 = pSingleCol.getTable().getTableName();
				int table1Tuples = cvc.getNoOfOutputTuples(tableName1);
				int table2Tuples = cvc.getNoOfOutputTuples(tableName2);
			
				
				for(int i=1 ; i<= table1Tuples ; i++) //this loops on tuples in child table
				{
					Expr findex = (IntExpr) ctx.mkInt(i);

					
					if(Configuration.isEnumInt.equalsIgnoreCase("true")){
						EnumSort currentSort = (EnumSort)ConstraintGenerator.ctxSorts.get(cvc.enumArrayIndex);
						findex = ctx.mkConst(cvc.enumIndexVar+Integer.toString(i),currentSort);
					}

					orConstraints.clear();
					
					for(int z=1 ; z<=table2Tuples; z++)   //this loops on tuples in parent table
					{
						Expr pindex =  (IntExpr) ctx.mkInt(z);
						//we are matching each tuple in child table to all the tuples in parent table
						if(Configuration.isEnumInt.equalsIgnoreCase("true")){
							EnumSort currentSort = (EnumSort)ConstraintGenerator.ctxSorts.get(cvc.enumArrayIndex);
							pindex = ctx.mkConst(cvc.enumIndexVar+Integer.toString(z),currentSort);
						}
						
						//testcode for multiattribute
						Vector<BoolExpr> andConstraints = new Vector<BoolExpr>();
						Vector<Column> temp = new Vector<Column>();
						for(int k = 0 ; k < fCol.size(); k++)  //this loops for multiattribute foreign key
						{
							fSingleCol = fCol.get(k);
							pSingleCol = pCol.get(k);
                            
							if (!temp.contains(fSingleCol)) {
								temp.add(fSingleCol);
							} else {
								continue;
							}
                            
							
							tableName1 = fSingleCol.getTable().getTableName();
							tableName2 = pSingleCol.getTable().getTableName();
							
							String temp1 = "ISNULL_" + fSingleCol.getCvcDatatype();
							if(fSingleCol.getCvcDatatype().equalsIgnoreCase("int") || fSingleCol.getCvcDatatype().equalsIgnoreCase("real"))
								temp1 = "ISNULL_" + fSingleCol.getColumnName();
							
							FuncDecl funIsNull = ctxFunDeclHashMap.get(temp1);
							
							Expr funExpr = funIsNull.apply(ConstraintGenerator.smtMap(fSingleCol, findex));
							
							Expr ex = ConstraintGenerator.smtMap(fSingleCol, findex);
							Expr exx = ConstraintGenerator.smtMap(pSingleCol, pindex);
							Expr eqExpr = ctx.mkEq(ConstraintGenerator.smtMap(fSingleCol, findex), ConstraintGenerator.smtMap(pSingleCol, pindex));
							andConstraints.add(ctx.mkOr((BoolExpr)funExpr, eqExpr));
						}
						
						if(usingCnt)
						{	
							Table table = pSingleCol.getTable();
							Column cntCol = table.getColumn("XDATA_CNT");
							andConstraints.add(ctx.mkGt((ArithExpr) ConstraintGenerator.smtMap(cntCol, pindex) , (ArithExpr) ctx.mkInt(0)));
							orConstraints.add(ctx.mkAnd(andConstraints.toArray(new BoolExpr[andConstraints.size()])));
						} 
						else {
		                    orConstraints.add(ctx.mkAnd(andConstraints.toArray(new BoolExpr[andConstraints.size()])));
						}
					}
					String temp1 = "";
					if(usingCnt)
					{
							Table table = fSingleCol.getTable();
							String col_datatype = fSingleCol.getCvcDatatype();
							Column cntCol = table.getColumn("XDATA_CNT");
							orConstraints.add(ctx.mkEq((ArithExpr) ConstraintGenerator.smtMap(cntCol, findex) , (ArithExpr) ctx.mkInt(0)));
							
					}
					BoolExpr orConst = ctx.mkOr(orConstraints.toArray(new BoolExpr[orConstraints.size()]));
					// String constraints = "(or \n"+temp1+"\n\t"+orConst.toString()+")\n";
					if(usingCnt)
						fkConstraints.add(orConst.toString());
					else
						fkConstraints.add(orConst.toString());					
				}	
				}	
	    return fkConstraints;
	}	
	
	/**
	 * Updates the number of tuples of the tables
	 * @param cvc
	 * @param ptableName
	 * @param fkCount
	 * @param pkCount
	 */
	public static void updateTheNumberOfTuples(GenerateCVC1 cvc, String ptableName, int fkCount, int pkCount) {

		/**update the number of tuples for the whole primary key table relation*/
		if( cvc.getNoOfOutputTuples(ptableName) == -1 || cvc.getNoOfOutputTuples(ptableName) == 0) 
			cvc.putNoOfOutputTuples(ptableName, pkCount);

		else{
		 	cvc.putNoOfOutputTuples(ptableName, Math.min(pkCount + cvc.getNoOfOutputTuples(ptableName), 5) );
			//cvc.putNoOfOutputTuples(ptableName, pkCount  );
		}
	}

}
