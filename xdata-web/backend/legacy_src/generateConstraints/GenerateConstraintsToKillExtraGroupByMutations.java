package generateConstraints;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import parsing.Column;
import parsing.ConjunctQueryStructure;
import parsing.Node;
import parsing.Table;
import testDataGen.GenerateCVC1;
import testDataGen.QueryBlockDetails;
import util.Configuration;
import util.ConstraintObject;

/**
 * This class generates constraints to kill the extra group by mutations
 * @author mahesh
 *
 */
public class GenerateConstraintsToKillExtraGroupByMutations {


	
	//public static ArrayList<Column> getExtraColumns(GenerateCVC1 cvc, QueryBlockDetails queryBlock, Map<String, String> tableOccurrence) throws Exception{
public static ArrayList<Column> getExtraColumns(GenerateCVC1 cvc, QueryBlockDetails queryBlock, Map<String, String> tableOccurrence) throws Exception{
		/** get the list of tables which contain group by nodes of this query block */
		/** Along with the base table names, get their occurrences */
		/** Store base table names */
		ArrayList<Table> tempFromTables = new ArrayList<Table>();

		/**Stores relations occurrences*/
		//tempFromTables = getListOfRelations( cvc,queryBlock.getGroupByNodes(), tableOccurrence );
 tempFromTables = getListOfRelations(cvc,queryBlock, queryBlock.getGroupByNodes(), tableOccurrence );

		/** get all the extra columns of these tables*/
		ArrayList<Column> extraColumn = new ArrayList<Column>();
		//extraColumn = getListOfExtraColumns(cvc,tempFromTables, queryBlock.getGroupByNodes());
extraColumn = getListOfExtraColumns(tempFromTables, queryBlock.getGroupByNodes());
		return extraColumn;
	}

	/**
	 * This function is used to get the list of extra columns apart from the group by attributes
	 * @param tempFromTables
	 * @param groupbyNodes
	 * @return
	 */
	//public static ArrayList<Column> getListOfExtraColumns(GenerateCVC1 cvc,ArrayList<Table> tempFromTables, ArrayList<Node> groupbyNodes) throws Exception {
public static ArrayList<Column> getListOfExtraColumns(	ArrayList<Table> tempFromTables, ArrayList<Node> groupbyNodes) throws Exception {

		/** Store the list of columns*/
		ArrayList<Column> extraColumn = new ArrayList<Column>();

		/** For each table */
		int size;
		for(Table table: tempFromTables){
			if(Configuration.getProperty("cntFlag").equalsIgnoreCase("true"))
				size = table.getNoOfColumn() -1 ;
			else
				size = table.getNoOfColumn();
			for(int j=0;j<size;j++){/**For each column of this table*/

				/**Get this column */
				Column col = table.getColumn(j);

				/**Indicates if this is a group by node */
				boolean flag=true;

				/** check if this column is a group by node */
				for(Node each: groupbyNodes){
					if(each.getColumn().getColumnName().equalsIgnoreCase(col.getColumnName())){
						flag=false;
						break;
					}
				}
			if(flag)/** If this not a group by node column */
					extraColumn.add(col);
			//}
		}
	
		
		}	
		return extraColumn;
	}

	/**
	 * This function returns the list of relations of group by nodes
	 * @param groupbyNodes
	 * @param tableOccurrence 
	 * @return
	 */
	public static ArrayList<Table> getListOfRelations(GenerateCVC1 cvc, QueryBlockDetails qbt, ArrayList<Node> groupbyNodes, Map<String, String> tableOccurrence) throws Exception{
		ConstraintGenerator ConstGen = new ConstraintGenerator();
		ArrayList<Table> tempFromTables = new ArrayList<Table>();
		//String tableName = "";
		/**Get for each group by node */
		
		for(int i=0; i<qbt.getBaseRelations().size(); i++) {
			Table temp = cvc.getTableMap().getTable(ConstGen.removeAllDigit(qbt.getBaseRelations().get(i)));
			if(temp != null && !tempFromTables.contains(temp))		{		
				/** Add this table */
				tempFromTables.add(temp);
				}
			if( temp != null && !tableOccurrence.containsValue(temp.getTableName()))
				/**Add the occurrence of this relation*/
				tableOccurrence.put(temp.getTableName(),qbt.getBaseRelations().get(i));
		}
		for(Node tempgroupByNodeNew : groupbyNodes){
			/**If this table is not already in the list */
			if(!tempFromTables.contains(tempgroupByNodeNew.getColumn().getTable()))		{		
				/** Add this table */
				tempFromTables.add(tempgroupByNodeNew.getColumn().getTable());
				}
			
			/**If this table occurrence is not already in the list */
			if( !tableOccurrence.containsValue(tempgroupByNodeNew.getType()))
				/**Add the occurrence of this relation*/
				tableOccurrence.put(tempgroupByNodeNew.getColumn().getTable().getTableName(), tempgroupByNodeNew.getTableNameNo());
		}

		return tempFromTables;
	}


}
