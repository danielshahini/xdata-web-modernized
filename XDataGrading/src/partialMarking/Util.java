/**
 * 
 */
package partialMarking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import parsing.Table;
import util.Graph;
import parsing.Column;
import parsing.ForeignKey;
import parsing.FromListElement;
import parsing.JoinClauseInfo;
import parsing.Node;

/**
 * @author mathew
 *
 */
public class Util {
	private static Logger logger = Logger.getLogger(Util.class.getName()); 
	
	/** @author mathew on 22 June 2016
	 * Returns true if for every node n1 in key, there exists a node n2 in sourceLists
	 * such that n1 and n2 represent the same column 
	 * 
	 */
	public static boolean containsElements(List<Node> sourceList, Vector<Node> keys){
		for(Node key:keys){
			boolean foundKey=false;
			for(Node sourceNode:sourceList){
				if(key.getTableNameNo().equalsIgnoreCase(sourceNode.getTableNameNo())&&
						key.getColumn().getColumnName().equalsIgnoreCase(sourceNode.getColumn().getColumnName())){
					foundKey=true;
					break;
				}
			}
			if(!foundKey)
				return false;
		}
		return true;
	}
	
	public static boolean containsElement(List<Node> sourceList, Node key){
			for(Node sourceNode:sourceList){
				if(key.getTableNameNo().equalsIgnoreCase(sourceNode.getTableNameNo())&&
						key.getColumn().getColumnName().equalsIgnoreCase(sourceNode.getColumn().getColumnName())){
					return true;				
				}
			}
				return false;

	}


	public static Set<Node> toSetOfNodes(List<Node> nodes){
		Set<Node> tempSet=new HashSet<Node>();
		if(nodes!=null){
			for(Node n:nodes)
				tempSet.add(n);
		}
		return tempSet;
	}
	
	/* returns true iff both nodeA and nodeB represent the same columns
	 * 
	 */
	private static boolean isEquivalentColumns(Node nodeA, Node nodeB) {
		// TODO Auto-generated method stub
		if(nodeA.toString().equalsIgnoreCase(nodeB.toString()))
			return true;
		else
			return false;
	}
	
	/*
	 * remove duplicates from a list of input selection/join conditions
	 */
	public static ArrayList<Node> removeDuplicates(ArrayList<Node> selectionConds) {
		// TODO Auto-generated method stub
		boolean removedFlag;
		do{
			removedFlag=false;
			for(int i=0;i<selectionConds.size()-1;i++){
				Node src=selectionConds.get(i);
				boolean found=false;
				for(int j=i+1;j<selectionConds.size();j++){
					Node tar=selectionConds.get(j);
					if(src==tar){
						found=true;
						break;
					}
					String srcLeftStr=src.getLeft().toString();
					String srcRightStr=src.getRight().toString();
					String tarLeftStr=tar.getLeft().toString();
					String tarRightStr=tar.getRight().toString();
					if(srcLeftStr.equalsIgnoreCase(tarLeftStr)&&srcRightStr.equalsIgnoreCase(tarRightStr)){
						found=true;
						break;
					}
				}
				if(found){
					selectionConds.remove(i);
					removedFlag=true;
					break;
				}
			}
		} while(removedFlag);
		return selectionConds;
	}

	public static Node getNodeForCount(Vector<FromListElement> fle, QueryStructure qParser) {
		
		for(FromListElement f:fle){
			String fromTableName = f.getTableName();
			if (fromTableName != null && f.getTableNameNo()!= null &&!f.getTableNameNo().isEmpty()) {
				Table t=qParser.getTableMap().getTable(fromTableName);
				Column col = t.getColumn(0);
				if (col == null)
					return null;

				Node n = new Node();
				n.setTable(t);
				n.setTableAlias(t.getAliasName());
				n.setColumn(col);
				if(f.getTableNameNo() != null && ! f.getTableNameNo().isEmpty()){
					n.setTableNameNo(f.getTableNameNo());
				}//This may not be correct - FIXME Test and fix
				else{
					n.setTableNameNo(f.getTableName()+"1");
				}
				n.setType(Node.getColRefType());
				return n;
			} else if (f.getTabs()!=null && !f.getTabs().isEmpty()){
				for (int i = 0; i < f.getTabs().size(); i++) {
					Node n = getNodeForCount(f.getTabs().get(i), qParser);
					n.setType(Node.getColRefType());
					if (n != null) {
						return n;
					}
				}
			}
			else if(f.getSubQueryStructure()!=null){
				Node n=getNodeForCount(f.getSubQueryStructure().fromListElements,f.getSubQueryStructure());
				if(n!=null){
					n.setType(Node.getColRefType());
					return n;
				}
			}
		}
		return null;
}
	
	public static Node getNodeForCount(FromListElement f, QueryStructure qParser) {
		
		String fromTableName = f.getTableName();
		if (fromTableName != null && qParser.getQuery().getFromTables().get(fromTableName.toUpperCase()) != null 
				&& f.getTableNameNo()!= null &&!f.getTableNameNo().isEmpty()) {
			
			Table t = qParser.getQuery().getFromTables().get(fromTableName.toUpperCase());
			Column col = t.getColumn(0);
			if (col == null)
				return null;

			Node n = new Node();
			n.setTable(t);
			n.setTableAlias(t.getAliasName());
			n.setColumn(col);
			if(f.getTableNameNo() != null && ! f.getTableNameNo().isEmpty()){
				n.setTableNameNo(f.getTableNameNo());
			}//This may not be correct - FIXME Test and fix
			else{
				n.setTableNameNo(f.getTableName()+"1");
			}
			n.setType(Node.getColRefType());
			return n;
		} else{
		for (int i = 0; i < f.getTabs().size(); i++) {
				Node n = getNodeForCount(f.getTabs().get(i), qParser);
				n.setType(Node.getColRefType());
				if (n != null) {
					return n;
				}
			}
		}
		return null;
}	
	
	/*
	 * Convert the foreignKeyClosure of type Vector<JoinClauseInfo> to a type of
	 * Vector<Node>.
	 */

	public static void foreignKeyInNode(QueryStructure qParser) {
		for (int i = 0; i < qParser.getForeignKeyVector().size(); i++) {
			Node left = new Node();
			left.setColumn(qParser.getForeignKeyVector().get(i).getJoinAttribute1());
			left.setTable(qParser.getForeignKeyVector().get(i).getJoinAttribute1()
					.getTable());
			left.setLeft(null);
			left.setRight(null);
			left.setOperator(null);
			left.setType(Node.getColRefType());

			Node right = new Node();
			right.setColumn(qParser.getForeignKeyVector().get(i).getJoinAttribute2());
			right.setTable(qParser.getForeignKeyVector().get(i).getJoinAttribute2()
					.getTable());
			right.setLeft(null);
			right.setRight(null);
			right.setOperator(null);
			right.setType(Node.getColRefType());

			Node refJoin = new Node();
			refJoin.setColumn(null);
			refJoin.setTable(null);
			refJoin.setLeft(left);
			refJoin.setRight(right);
			refJoin.setType(Node.getBaoNodeType());
			refJoin.setOperator("=");
			refJoin.setStrConst(qParser.getForeignKeyVector().get(i).getConstant());
			qParser.getForeignKeys().add(refJoin);
		}
	}
	
	/* Getting Foreign Key closure */
	public static void foreignKeyClosure(QueryStructure qParser) {
		Vector<Table> fkClosure = new Vector<Table>();
		LinkedList<Table> fkClosureQueue = new LinkedList<Table>();
		logger.log(Level.INFO,"FOREIGN KEY GRAPH : \n"+qParser.getTableMap().foreignKeyGraph);		
		//for (String tableName : qParser.getQuery().getFromTables().keySet()) {
		for (String tableName : qParser.getLstRelations()) {
			fkClosure.add( qParser.getTableMap().getTables().get(tableName.toUpperCase()));
			fkClosureQueue.addLast(qParser.getTableMap().getTables().get(tableName.toUpperCase()));
			logger.log(Level.INFO,"fkClosureQueue.add tables: \n "+qParser.getTableMap().getTables().get(tableName.toUpperCase()));
		}
		while(!fkClosureQueue.isEmpty())
		{
			Table table = fkClosureQueue.removeFirst();
			logger.log(Level.INFO,"fkClosureQueue Not Empty and contains table \n"+table.getTableName());
			for(Table tempTable : qParser.getTableMap().foreignKeyGraph.getAllVertex())
			{  
				Map<Table,Vector<ForeignKey>> neighbours = qParser.getTableMap().foreignKeyGraph.getNeighbours(tempTable);
				for(Table neighbourTable : neighbours.keySet())
				{
					if(neighbourTable.equals(table) && !fkClosure.contains(tempTable))
					{
						fkClosure.add(tempTable);
						fkClosureQueue.addLast(tempTable);
					}
				}
			}
		}
		Graph<Table, ForeignKey> tempForeignKeyGraph = qParser.getTableMap().foreignKeyGraph.createSubGraph();
		for(Table table : fkClosure)
			tempForeignKeyGraph.add(qParser.getTableMap().foreignKeyGraph, table);
		fkClosure = tempForeignKeyGraph.topSort();

		for(Table table : fkClosure)
			fkClosureQueue.addFirst(table);
		fkClosure.removeAllElements();
		fkClosure.addAll(fkClosureQueue);

		while(!fkClosureQueue.isEmpty())
		{
			Table table = fkClosureQueue.removeFirst();

			if(table.getForeignKeys() != null)
			{
				for (String fKeyName : table.getForeignKeys().keySet())
				{
					ForeignKey fKey = table.getForeignKey(fKeyName);
					qParser.getForeignKeyVectorModified().add(fKey);
					Vector<Column> fKeyColumns = fKey.getFKeyColumns();
					for (Column fKeyColumn : fKeyColumns)
					{
						JoinClauseInfo foreignKey = new JoinClauseInfo(fKeyColumn, fKeyColumn.getReferenceColumn(),JoinClauseInfo.FKType);
						foreignKey.setConstant(fKeyName);
						qParser.getForeignKeyVector().add(foreignKey);
					}
				}
			}
		}
	//Changed by Biplab till here
		qParser.setForeignKeyVectorOriginal((Vector<JoinClauseInfo>) qParser.getForeignKeyVector().clone());

		// Now taking closure of foreign key conditions
		/*
		 * Altered closure algorithm so that the last foreign key in the chain is not added if it is nullable
		 * If the foreign key from this relation to other relations is nullale, 
		 * then this relation must not appear in the closure.
		 */

		//Commented out by Biplab
		/*for (int i = 0; i < this.foreignKeyVector.size(); i++) {
			JoinClauseInfo jci1 = this.foreignKeyVector.get(i);

			for (int j = i + 1; j < this.foreignKeyVector.size(); j++) {
				JoinClauseInfo jci2 = this.foreignKeyVector.get(j);
				if (jci1.getJoinTable2() == jci2.getJoinTable1()
						&& jci1.getJoinAttribute2() == jci2.getJoinAttribute1()) {
					//Check to see if the from column is nullable. If so, do not add the FK.
					//if(jci1.getJoinAttribute1().isNullable()){
					//	continue;
					//}
					JoinClauseInfo foreignKey = new JoinClauseInfo(jci1.getJoinAttribute1(), jci2.getJoinAttribute2(),JoinClauseInfo.FKType);
					if (!this.foreignKeyVector.contains(foreignKey)) {
						this.foreignKeyVector.add(foreignKey);
					}
				}
			}
		}*/
		//Commented out by Biplab till here

		// Convert the closure to type Vector<Node>
		foreignKeyInNode(qParser);
	}
	
	
	public static Vector<Node> getAllProjectedColumns(Vector<FromListElement> visitedFLEs, QueryStructure qParser){
		Vector<Node> projectedColumns=new Vector<Node>();
		for(FromListElement fle:visitedFLEs){
			if(fle!=null && fle.getTableName()!=null){
				Table t=qParser.getTableMap().getTable(fle.getTableName());
				if(t!=null){
					Iterator colItr=t.getColumns().values().iterator();
					while(colItr.hasNext()){
						Column col=(Column)colItr.next();
						Node n = new Node();
						n.setColumn(col);
						n.setTable(col.getTable());
						n.setLeft(null);
						n.setRight(null);
						n.setOperator(null);
						n.setType(Node.getColRefType());
						n.setTableNameNo(fle.getTableNameNo());
						projectedColumns.add(n);
					}
				}
			}
			else if(fle!=null && fle.getTabs()!=null && !fle.getTabs().isEmpty()){
				projectedColumns.addAll(getAllProjectedColumns(fle.getTabs(),qParser));				
			}
			else if(fle!=null && fle.getSubQueryStructure()!=null){
				projectedColumns.addAll(fle.getSubQueryStructure().getProjectedCols());
			}
		}
		return projectedColumns;
	}
	


	public static void copyDatabaseTables(Connection srcConn, Connection tarConn, String tableName) throws Exception{
		String selQuery="SELECT * FROM "+tableName;
		PreparedStatement selStmt=srcConn.prepareStatement(selQuery);
		ResultSet tableValues=selStmt.executeQuery();
		while(tableValues.next()){
			String insertHead="INSERT INTO "+tableName+"(";
			String insertTail=" VALUES(";
			ResultSetMetaData metaData=tableValues.getMetaData();
			for(int i=1;i<=metaData.getColumnCount();i++){
				if(i==1){
					insertHead+=metaData.getColumnName(i);
					insertTail+="?";
				}
				else if(metaData.getColumnName(i).contains("evaluationstatus")||metaData.getColumnName(i).contains("learning_mode"))
					continue;

				else{
					insertHead+=","+metaData.getColumnName(i);
					insertTail+=",?";
				}
			}
			String insertQuery= insertHead+")"+insertTail+")";		
//			System.out.println(insertQuery);
			PreparedStatement insStmt=tarConn.prepareStatement(insertQuery);
			int k=0;
			for(int j=1;j<=metaData.getColumnCount();j++){
				if(metaData.getColumnName(j).contains("evaluationstatus")||metaData.getColumnName(j).contains("learning_mode"))
					continue;
				else if((metaData.getColumnType(j)==Types.NUMERIC)||(metaData.getColumnType(j)==Types.INTEGER)){
					insStmt.setInt(++k, tableValues.getInt(j));
				}
				else if(metaData.getColumnType(j)==Types.BOOLEAN){
					insStmt.setBoolean(++k, tableValues.getBoolean(j));
				}
				else if(metaData.getColumnType(j)==Types.DECIMAL||metaData.getColumnType(j)==Types.FLOAT){
					insStmt.setDouble(++k, tableValues.getDouble(j));
				}
				else if(metaData.getColumnType(j)==Types.TIMESTAMP){
					insStmt.setTimestamp(++k, tableValues.getTimestamp(j));
				}
				else if(metaData.getColumnType(j)==Types.TIME){
					insStmt.setTime(++k, tableValues.getTime(j));
				}
				else if(metaData.getColumnType(j)==Types.VARCHAR){
					insStmt.setString(++k, tableValues.getString(j));
				}
				else if(metaData.getColumnType(j)==Types.DATE)
				{
					insStmt.setDate(++k, tableValues.getDate(j));
				}
//				else if(metaData.getColumnLabel(j)=="learning_mode") {
//					if(tableValues.getBoolean(j)==false)
//						insStmt.setBoolean(j, false);
//					else
//						insStmt.setBoolean(j, true);
//				}
				else{
					insStmt.setString(++k, tableValues.getString(j));
				}
			}
			System.out.println(insStmt.toString());
			try{
			insStmt.executeUpdate();
			}
			catch(Exception e){
				System.out.println(insStmt.toString());
				System.out.println(e.getMessage());
			}
		}
	}
	
	public static void copyDatabaseTables(Connection srcConn, Connection tarConn) throws Exception{
		String srcQuery="SELECT table_name from information_schema.tables where table_schema='testing1'";
		PreparedStatement srcStmt= srcConn.prepareStatement(srcQuery);
		PreparedStatement tarStmt= tarConn.prepareStatement(srcQuery);
		ResultSet srcRes=srcStmt.executeQuery();
		ResultSet tarRes=tarStmt.executeQuery();
        
		List<String> srcTables=new ArrayList<String>();
		while(srcRes.next()){
			srcTables.add(srcRes.getString(1));
		}
		List<String> tarTables=new ArrayList<String>();
		while(tarRes.next()){
			tarTables.add(tarRes.getString(1));
		}
		for(String tableName:srcTables){
			if(!tarTables.contains(tableName))
				continue;
			String selQuery="SELECT * FROM "+tableName;
			PreparedStatement selStmt=srcConn.prepareStatement(selQuery);
			ResultSet tableValues=selStmt.executeQuery();
			while(tableValues.next()){
				String insertHead="INSERT INTO "+tableName+"(";
				String insertTail=" VALUES(";
				ResultSetMetaData metaData=tableValues.getMetaData();
				for(int i=1;i<metaData.getColumnCount();i++){
					if(i==1){
						insertHead+=metaData.getColumnName(i);
						insertTail+="?";
					}
					else{
						insertHead+=","+metaData.getColumnName(i);
						insertTail+=",?";
					}
				}
				String insertQuery= insertHead+")"+insertTail+")";				
				PreparedStatement insStmt=tarConn.prepareStatement(insertQuery);
				for(int j=1;j<metaData.getColumnCount();j++){
					if(metaData.getColumnType(j)==Types.NUMERIC||metaData.getColumnType(j)==Types.INTEGER){
						insStmt.setInt(j, tableValues.getInt(j));
					}
					else if(metaData.getColumnType(j)==Types.BOOLEAN){
						insStmt.setBoolean(j, tableValues.getBoolean(j));
					}
					else if(metaData.getColumnType(j)==Types.DECIMAL||metaData.getColumnType(j)==Types.FLOAT){
						insStmt.setDouble(j, tableValues.getDouble(j));
					}
					else if(metaData.getColumnType(j)==Types.TIMESTAMP){
						insStmt.setTimestamp(j, tableValues.getTimestamp(j));
					}
					else if(metaData.getColumnType(j)==Types.TIME){
						insStmt.setTime(j, tableValues.getTime(j));
					}
					else if(metaData.getColumnType(j)==Types.VARCHAR){
						insStmt.setString(j, tableValues.getString(j));
					}
					else if(metaData.getColumnType(j)==Types.DATE)
					{
						insStmt.setDate(j, tableValues.getDate(j));
					}
//					else if(metaData.getColumnLabel(j)=="learning_mode") {
//						if(tableValues.getBoolean(j)==false)
//							insStmt.setBoolean(j, false);
//						else
//							insStmt.setBoolean(j, true);
//					}
					else{
						insStmt.setString(j, tableValues.getString(j));
					}
				}
				System.out.println(insStmt.toString());
				try{
				insStmt.executeUpdate();
				}
				catch(Exception e){
					System.out.println(e.getMessage());
				}
			}
		}
	}


	/**
	 * 
	 */
	public Util() {
		// TODO Auto-generated constructor stub
	}

}
