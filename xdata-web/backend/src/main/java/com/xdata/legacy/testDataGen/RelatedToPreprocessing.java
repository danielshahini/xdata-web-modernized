package com.xdata.legacy.testDataGen;
import com.xdata.util.TableMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.xdata.legacy.parsing.Column;
import com.xdata.legacy.parsing.ConjunctQueryStructure;
import com.xdata.legacy.parsing.ForeignKey;
import com.xdata.legacy.parsing.Node;
import com.xdata.legacy.parsing.Query;
import com.xdata.legacy.parsing.Table;
import com.xdata.legacy.testDataGen.PopulateTestData;
import com.xdata.legacy.util.Configuration;
import com.xdata.legacy.util.Utilities;

/**
 * Contains methods needed to preprocessing actions
 * @author mahesh
 *
 */
public class RelatedToPreprocessing {

	private static Logger logger = Logger.getLogger(RelatedToPreprocessing.class.getName());
	/**
	 * 

	/**
	 * Generates data values from the input data base
	 * These values are used while generating the data for the query
	 * @param cvc	 
	 */
	public static void populateData(GenerateCVC1 cvc) throws Exception{

		/**if there are branch queries*/

		
			populateDataWithoutBranchQueries(cvc);
	}

	/**
	 * Generates data values for the set of branch queries
	 * @param cvc
	 * @throws Exception
	 */
	
	/**
	 * generates data values for this query
	 * @param cvc
	 * @throws Exception
	 */
	public static void populateDataWithoutBranchQueries(GenerateCVC1 cvc) throws Exception{


		Query query = cvc.getQuery();
		Connection assignmentConn  = cvc.getConnection();
		
				/**Gets the name of tables required for the query and the name columns of that query. 
				 * Also checks for any foreign key reference and adds the referenced table to the list of tables being considered.*/
		
				Column column;
				Table table;
				Collection<Table> tables  = new Vector<Table>();
				tables.addAll(query.getFromTables().values());
				//Also add the foreign key tables
				Iterator iter = tables.iterator();
				while(iter.hasNext()){
					Table t = (Table)iter.next();
					if(t.hasForeignKey()){
						Map<String, ForeignKey> fks = t.getForeignKeys();
						Iterator iter2 = fks.values().iterator();
						while(iter2.hasNext()){
							ForeignKey fk = (ForeignKey)iter2.next();
							if(!tables.contains(fk.getReferenceTable())){
								tables.add(fk.getReferenceTable());
								iter = tables.iterator();
							}
						}
					}
				}
		
				/** Then for each column it gets a max of 50 distinct values from the tables existing in the database */
		
				Iterator t = tables.iterator();
				cvc.getResultsetColumns().add(new Column("dummy","dummy"));
				while(t.hasNext()){
					table = (Table)t.next();
					if(!cvc.getResultsetTables().contains(table)){
						cvc.getResultsetTables().add(table);
					}
					
					
					Collection columns = table.getColumns().values();
					Iterator c = columns.iterator();
					while(c.hasNext()){
						
						column = (Column)c.next();
						// test code deeksha - start
						if(column.getColumnName().toUpperCase().equals("XDATA_CNT")) {
							cvc.getResultsetColumns().add(column); 
							continue;
						}
						// test code deeksha - end
						column.intializeColumnValuesVector();
						String qs = "select distinct " + column.getColumnName() + " from " + table.getTableName() + " limit 50";				
						PreparedStatement ps = assignmentConn.prepareStatement(qs);
						ResultSet rs = ps.executeQuery();
						while(rs != null && rs.next()){
							String temp = rs.getString(column.getColumnName().toUpperCase());
							if(temp != null)
								column.addColumnValues(temp);
						}
						cvc.getResultsetColumns().add(column);
						ps.close();
						rs.close();				
					}
				}
			  
			
	}
	

	/**
	 * Segregate selection conditions in the given query block
	 * * parismita -  added nesting
	 * @param cvc
	 * @param queryBlock
	 */
	public static void segregateSelectionConditionsForQueryBlock(GenerateCVC1 cvc, QueryBlockDetails queryBlock) {

		/** Segregate selection conditions of each conjunct of this query block */
		for(ConjunctQueryStructure conjunct : queryBlock.getConjunctsQs()){
			conjunct.seggregateSelectionConds();
		}
		/** Segregate selection conditions of each from clause nested sub query block */
		for(QueryBlockDetails qB: queryBlock.getFromClauseSubQueries())
			segregateSelectionConditionsForQueryBlock(cvc, qB);

		/** Segregate selection conditions of each where clause nested sub query block */
		for(QueryBlockDetails qB: queryBlock.getWhereClauseSubQueries())
			segregateSelectionConditionsForQueryBlock(cvc, qB);

	}

	public static void deletePreviousDatasets(GenerateDataset_new g, String query) throws IOException,InterruptedException {

		//Runtime r = Runtime.getRuntime();
		logger.log(Level.INFO,Configuration.homeDir+"/temp_smt"+g.getFilePath()+"/");
		File f=new File(Configuration.homeDir+"/temp_smt"+g.getFilePath()+"/");
		
		if(f.exists()){		
			File f2[]=f.listFiles();
			if(f2 != null)
			for(int i=0;i<f2.length;i++){
				if(f2[i].isDirectory() && f2[i].getName().startsWith("DS")){
					
					Utilities.deletePath(Configuration.homeDir+"/temp_smt"+g.getFilePath()+"/"+f2[i].getName());
				}
			}
		}
		

		File dir= new File(Configuration.homeDir+"/temp_smt"+g.getFilePath());
		if(dir.exists()){
			for(File file: dir.listFiles()) {
				file.delete();
			}
		}
		else{
			dir.mkdirs();
		}
		
		BufferedWriter ord = new BufferedWriter(new FileWriter(Configuration.homeDir+"/temp_smt"+g.getFilePath()+"/queries.txt"));
		BufferedWriter ord1 = new BufferedWriter(new FileWriter(Configuration.homeDir+"/temp_smt"+g.getFilePath()+"/queries_mutant.txt"));
		ord.write(query);
		ord1.write(query);
		ord.close();
		ord1.close();
	}

	public static void deletePreviousDatasets(GenerateCVC1 g, String query) throws IOException,InterruptedException {

		//Runtime r = Runtime.getRuntime();
		logger.log(Level.INFO,Configuration.homeDir+"/temp_smt"+g.getFilePath()+"/");
		File f=new File(Configuration.homeDir+"/temp_smt"+g.getFilePath()+"/");
		
		if(f.exists()){		
			File f2[]=f.listFiles();
			if(f2 != null)
			for(int i=0;i<f2.length;i++){
				if(f2[i].isDirectory() && f2[i].getName().startsWith("DS")){
					
					Utilities.deletePath(Configuration.homeDir+"/temp_smt"+g.getFilePath()+"/"+f2[i].getName());
				}
			}
		}
		

		File dir= new File(Configuration.homeDir+"/temp_smt"+g.getFilePath());
		if(dir.exists()){
			for(File file: dir.listFiles()) {
				file.delete();
			}
		}
		else{
			dir.mkdirs();
		}
		
		BufferedWriter ord = new BufferedWriter(new FileWriter(Configuration.homeDir+"/temp_smt"+g.getFilePath()+"/queries.txt"));
		BufferedWriter ord1 = new BufferedWriter(new FileWriter(Configuration.homeDir+"/temp_smt"+g.getFilePath()+"/queries_mutant.txt"));
		ord.write(query);
		ord1.write(query);
		ord.close();
		ord1.close();
	}
	
	/** 
	 * Get the list of datasets generated
	 * @param gd
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<String> getListOfDataset(GenerateDataset_new gd) throws Exception{

		ArrayList<String> fileListVector = new ArrayList<String>();		
		ArrayList<String> datasets = new ArrayList<String>();

		String fileList[]=new File(Configuration.homeDir+"/temp_smt" + gd.getFilePath()).list();

		for(int k=0;k<fileList.length;k++){
			fileListVector.add(fileList[k]);
		}
		Collections.sort(fileListVector);	        
		for(int i=0;i<fileList.length;i++)
		{
			File f1=new File(Configuration.homeDir+"/temp_smt" + gd.getFilePath() +"/"+fileListVector.get(i));	          
			if(f1.isDirectory() && fileListVector.get(i).substring(0,2).equals("DS"))
			{
				datasets.add(fileListVector.get(i));
			}
		}

		return datasets;
	}

	/** 
	 * Get the list of datasets generated
	 * @param gd
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<String> getListOfDataset(GenerateCVC1 gd) throws Exception{

		ArrayList<String> fileListVector = new ArrayList<String>();		
		ArrayList<String> datasets = new ArrayList<String>();

		String fileList[]=new File(Configuration.homeDir+"/temp_smt" + gd.getFilePath()).list();

		for(int k=0;k<fileList.length;k++){
			fileListVector.add(fileList[k]);
		}
		Collections.sort(fileListVector);	        
		for(int i=0;i<fileList.length;i++)
		{
			File f1=new File(Configuration.homeDir+"/temp_smt" + gd.getFilePath() +"/"+fileListVector.get(i));	          
			// if(f1.isDirectory() && fileListVector.get(i).substring(0,2).equals("DS"))
			if(!f1.isDirectory() && fileListVector.get(i).substring(0,2).equals("DS"))
			{
				datasets.add(fileListVector.get(i));
			}
		}

		return datasets;
	}
	

	/**
	 * Sorts the foreign keys using the topological sort. 
	 * This is required to ensure that the extra tuples are added in the right order without violating foreign key constraints
	 * @param cvc
	 * @throws Exception
	 */
	public static void sortForeignKeys(GenerateCVC1 cvc ) throws Exception{

		Vector<Table> sortedTable = cvc.getTableMap().getAllTablesInTopSorted();
		int len = sortedTable.size();

		ArrayList<Node> foreignKeyTemp=(ArrayList<Node>)cvc.getForeignKeys().clone();
		cvc.getForeignKeys().removeAll(foreignKeyTemp);

		for(int i=0;i<len;i++){
			for(Node n:foreignKeyTemp){

				String ftable = n.getLeft().getTable().getTableName();
				String ptable = n.getRight().getTable().getTableName();
				if(sortedTable.get(i).getTableName().equals(ptable))

					for(int j=i+1;j<len;j++)						
						if(sortedTable.get(j).getTableName().equals(ftable))							
							cvc.getForeignKeys().add(n);				
			}
		}
	}



}
