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
		  printHead(out);
		  printHasDistinct(qData);
		  printProjectedColumns(qData);
		  printJoinConditions(qData);
		  printSelectionConditions(qData);
		  printGroupByColumns(qData);
		  printHavingConditions(qData);
		  printJoinTables(qData);
		  printRedundantTables(qData);
		  printSubqueryConnectives(qData);
		  printTail(out);
		  out.flush();
		  out.close();
	}
	
	
	public static void serializeXML(String fileName, QueryStructure qStruct) throws IOException, CloneNotSupportedException{
		qStruct.reAdjustJoins(); //needed for removing any join conditions from selection conditions
		  out = new PrintWriter(new FileWriter(fileName));
		  printHead(out);
		  out.println(getQueryStructureString(qStruct, true));
		  printTail(out);
		  out.flush();
		  out.close();
	}

	static String retStringSlave="",retStringMaster="";

	public static void serializeXML(String slaveFileName, String masterFileName, QueryStructure slaveQstruct, QueryStructure masterQstruct) throws IOException,CloneNotSupportedException{
		slaveQstruct.reAdjustJoins();
		masterQstruct.reAdjustJoins();
		PrintWriter slaveOut=new PrintWriter(new FileWriter(slaveFileName));
		PrintWriter masterOut=new PrintWriter(new FileWriter(masterFileName));
		getQueryStructureString(slaveQstruct,masterQstruct);
		
		printHead(slaveOut);
		slaveOut.println(retStringSlave);
		printTail(slaveOut);
		slaveOut.flush();
		slaveOut.close();
		
		printHead(masterOut);
		masterOut.println(retStringMaster);
		printTail(masterOut);
		masterOut.flush();
		masterOut.close();

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
	
	public static boolean isEqualForColoring(Node a, Node b){
		if(a.toString().equalsIgnoreCase(b.toString()))
			return true;
		else
			return false;
	}
	
	public static boolean getQueryStructureString(QueryStructure slaveQstruct, QueryStructure masterQstruct) throws CloneNotSupportedException{
		boolean perfectMatch=true;
		
		adjoinDistinctPart(slaveQstruct,masterQstruct);			
		adjoinProjectionPart(slaveQstruct,masterQstruct);
		adjoinJoinsPart(slaveQstruct,masterQstruct);
		adjoinSelectionPart(slaveQstruct,masterQstruct);
		adjoinGroupByPart(slaveQstruct,masterQstruct);
		adjoinHavingPart(slaveQstruct,masterQstruct);
		adjoinRelationInstancesPart(slaveQstruct,masterQstruct);
		adjoinRedundantRelationsPart(slaveQstruct,masterQstruct);
		adjoinSubqueryConnectivesPart(slaveQstruct,masterQstruct);
//		System.out.println(" slave "+retStringSlave);
//		System.out.println(" master "+retStringMaster);
//		
		return perfectMatch;
	}
	


	private static boolean adjoinSubqueryConnectivesPart(QueryStructure slaveQstruct, QueryStructure masterQstruct) {
		// TODO Auto-generated method stub
		boolean isSlaveContained=true;
		String tempStringSlave="";
		
		for(String slaveConnective:slaveQstruct.getLstSubQConnectives()){
			boolean found=false;
			for(String masterConnective:masterQstruct.getLstSubQConnectives()){
				if(slaveConnective.equalsIgnoreCase(masterConnective)){
					found=true;
					break;
				}				
			}
			if(found){
				tempStringSlave+=spaceTab+"<item text=\""+ slaveConnective +"\" style=\"color:#ee0000;\" id=\""+ idCounter++ +"\"/>\n";
			}
			else{
				tempStringSlave+=spaceTab+"<item text=\""+ slaveConnective +"\" style=\"color:#ff0000;\" id=\""+ idCounter++ +"\"/>\n";
				isSlaveContained=false;
			}
		}
		if(isSlaveContained){
			tempStringSlave="<item text=\"Subquery Connectives \" open=\"1\" style=\"color:#ee0000 id=\""+ idCounter++ +"\">\n"+tempStringSlave+"</item>\n";
		}
		else{
			tempStringSlave="<item text=\"Subquery Connectives\" open=\"1\" style=\"color:#ff0000 id=\""+ idCounter++ +"\">\n"+tempStringSlave+"</item>\n";			
		}
		retStringSlave+=tempStringSlave;
		
		boolean isMasterContained=true;		
		String tempStringMaster="";
		for(String masterConnective:masterQstruct.getLstSubQConnectives()){
			boolean found=false;
			for(String slaveConnective:slaveQstruct.getLstSubQConnectives()){
				if(slaveConnective.equalsIgnoreCase(masterConnective)){
					found=true;
					break;
				}				
			}
			if(found){
				tempStringMaster+=spaceTab+"<item text=\""+ masterConnective.toString() +"\" style=\"color:#ee0000;\" id=\""+ idCounter++ +"\"/>\n";
			}
			else{
				isMasterContained=false;
				tempStringMaster+=spaceTab+"<item text=\""+ masterConnective.toString() +"\" id=\""+ idCounter++ +"\"/>\n";
			}
		}			
		if(isMasterContained){
			tempStringMaster="<item text=\"Subquery Connectives\" open=\"1\" style=\"color:#ee0000 id=\""+ idCounter++ +"\">\n"+tempStringMaster+"</item>\n";
		}
		else{
			tempStringMaster="<item text=\"Subquery Connectives\" open=\"1\" id=\""+ idCounter++ +"\">\n"+tempStringMaster+"</item>\n";
		}
		retStringMaster+=tempStringMaster;
		
		if(isSlaveContained&&isMasterContained)
			return true;
		else
			return false;
	}


	private static boolean adjoinRedundantRelationsPart(QueryStructure slaveQstruct, QueryStructure masterQstruct) {
		// TODO Auto-generated method stub
		boolean isSlaveContained=true;
		String tempStringSlave="";
		
		for(String slaveRelation:slaveQstruct.getLstRedundantRelations()){
			boolean found=false;
			for(String masterRelation:masterQstruct.getLstRedundantRelations()){
				if(slaveRelation.equalsIgnoreCase(masterRelation)){
					found=true;
					break;
				}				
			}
			if(found){
				tempStringSlave+=spaceTab+"<item text=\""+ slaveRelation +"\" style=\"color:#ee0000;\" id=\""+ idCounter++ +"\"/>\n";
			}
			else{
				tempStringSlave+=spaceTab+"<item text=\""+ slaveRelation +"\" style=\"color:#ff0000;\" id=\""+ idCounter++ +"\"/>\n";
				isSlaveContained=false;
			}
		}
		if(isSlaveContained){
			tempStringSlave="<item text=\"Redundant Tables \" open=\"1\" style=\"color:#ee0000 id=\""+ idCounter++ +"\">\n"+tempStringSlave+"</item>\n";
		}
		else{
			tempStringSlave="<item text=\"Redundant Tables\" open=\"1\" style=\"color:#ff0000 id=\""+ idCounter++ +"\">\n"+tempStringSlave+"</item>\n";			
		}
		retStringSlave+=tempStringSlave;
		
		boolean isMasterContained=true;	
		String tempStringMaster="";
		
		for(String masterRelation:masterQstruct.getLstRedundantRelations()){
			boolean found=false;
			for(String slaveRelation:slaveQstruct.getLstRedundantRelations()){
				if(slaveRelation.equalsIgnoreCase(masterRelation)){
					found=true;
					break;
				}				
			}
			if(found){
				tempStringMaster+=spaceTab+"<item text=\""+ masterRelation.toString() +"\" style=\"color:#ee0000;\" id=\""+ idCounter++ +"\"/>\n";
			}
			else{
				isMasterContained=false;
				tempStringMaster+=spaceTab+"<item text=\""+ masterRelation.toString() +"\" id=\""+ idCounter++ +"\"/>\n";
			}
		}			
		if(isMasterContained){
			tempStringMaster="<item text=\"Redundant Tables\" open=\"1\" style=\"color:#ee0000 id=\""+ idCounter++ +"\">\n"+tempStringMaster+"</item>\n";
		}
		else{
			tempStringMaster="<item text=\"Redundant Tables\" open=\"1\" id=\""+ idCounter++ +"\">\n"+tempStringMaster+"</item>\n";
		}
		retStringMaster+=tempStringMaster;
		
		if(isSlaveContained&&isMasterContained)
			return true;
		else
			return false;
	}


	private static boolean adjoinRelationInstancesPart(QueryStructure slaveQstruct, QueryStructure masterQstruct) {
		// TODO Auto-generated method stub
		boolean isSlaveContained=true;
		String tempStringSlave="";
		
		for(String slaveRelation:slaveQstruct.getLstRelationInstances()){
			boolean found=false;
			for(String masterRelation:masterQstruct.getLstRelationInstances()){
				if(slaveRelation.equalsIgnoreCase(masterRelation)){
					found=true;
					break;
				}				
			}
			if(found){
				tempStringSlave+=spaceTab+"<item text=\""+ slaveRelation +"\" style=\"color:#ee0000;\" id=\""+ idCounter++ +"\"/>\n";
			}
			else{
				tempStringSlave+=spaceTab+"<item text=\""+ slaveRelation +"\" style=\"color:#ff0000;\" id=\""+ idCounter++ +"\"/>\n";
				isSlaveContained=false;
			}
		}
		if(isSlaveContained){
			tempStringSlave="<item text=\"Table Instances \" open=\"1\" style=\"color:#ee0000 id=\""+ idCounter++ +"\">\n"+tempStringSlave+"</item>\n";
		}
		else{
			tempStringSlave="<item text=\"Table Instances\" open=\"1\" style=\"color:#ff0000 id=\""+ idCounter++ +"\">\n"+tempStringSlave+"</item>\n";			
		}
		retStringSlave+=tempStringSlave;
		
		boolean isMasterContained=true;
		String tempStringMaster="";
		
		for(String masterRelation:masterQstruct.getLstRelationInstances()){
			boolean found=false;
			for(String slaveRelation:slaveQstruct.getLstRelationInstances()){
				if(slaveRelation.equalsIgnoreCase(masterRelation)){
					found=true;
					break;
				}				
			}
			if(found){
				tempStringMaster+=spaceTab+"<item text=\""+ masterRelation.toString() +"\" style=\"color:#ee0000;\" id=\""+ idCounter++ +"\"/>\n";
			}
			else{
				isMasterContained=false;
				tempStringMaster+=spaceTab+"<item text=\""+ masterRelation.toString() +"\" id=\""+ idCounter++ +"\"/>\n";
			}
		}			
		if(isMasterContained){
			tempStringMaster="<item text=\"Table Instances\" open=\"1\" style=\"color:#ee0000 id=\""+ idCounter++ +"\">\n"+tempStringMaster+"</item>\n";
		}
		else{
			tempStringMaster="<item text=\"Table Instances\" open=\"1\" id=\""+ idCounter++ +"\">\n"+tempStringMaster+"</item>\n";
		}
		retStringMaster+=tempStringMaster;
		
		if(isSlaveContained&&isMasterContained)
			return true;
		else
			return false;
	}


	private static boolean adjoinHavingPart(QueryStructure slaveQstruct, QueryStructure masterQstruct) throws CloneNotSupportedException{
		// TODO Auto-generated method stub
		boolean isSlaveContained=true;
		String tempStringSlave="";
		
		for(Node slaveHavingNode:Util.toSetOfNodes(slaveQstruct.getLstHavingConditions())){
			boolean found=false;
			for(Node masterHavingNode:Util.toSetOfNodes(masterQstruct.getLstHavingConditions())){
				if(isEqualForColoring(slaveHavingNode,masterHavingNode)){
					found=true;
					break;
				}				
			}
			if(found){
				tempStringSlave+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(slaveHavingNode).toString() +"\" style=\"color:#ee0000;\" id=\""+ idCounter++ +"\"/>\n";
			}
			else{
				tempStringSlave+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(slaveHavingNode).toString() +"\" style=\"color:#ff0000;\" id=\""+ idCounter++ +"\"/>\n";
				isSlaveContained=false;
			}
		}
		if(isSlaveContained){
			tempStringSlave="<item text=\"Having Conditions\" open=\"1\" style=\"color:#ee0000 id=\""+ idCounter++ +"\">\n"+tempStringSlave+"</item>\n";
		}
		else{
			tempStringSlave+="<item text=\"Having Conditions\" open=\"1\" style=\"color:#ff0000 id=\""+ idCounter++ +"\">\n"+tempStringSlave+"</item>\n";			
		}
		retStringSlave+=tempStringSlave;
		
		boolean isMasterContained=true;		
		String tempStringMaster="";
		
		for(Node masterHavingNode:Util.toSetOfNodes(masterQstruct.getLstHavingConditions())){
			boolean found=false;
			for(Node slaveHavingNode:Util.toSetOfNodes(slaveQstruct.getLstHavingConditions())){
				if(isEqualForColoring(slaveHavingNode,masterHavingNode)){
					found=true;
					break;
				}				
			}
			if(found){
				tempStringMaster+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(masterHavingNode).toString() +"\" style=\"color:#ee0000;\" id=\""+ idCounter++ +"\"/>\n";
			}
			else{
				isMasterContained=false;
				tempStringMaster+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(masterHavingNode).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
			}
		}			
		if(isMasterContained){
			tempStringMaster="<item text=\"Having Conditions\" open=\"1\" style=\"color:#ee0000 id=\""+ idCounter++ +"\">\n"+tempStringMaster+"</item>\n";
		}
		else{
			tempStringMaster="<item text=\"Having Conditions\" open=\"1\" id=\""+ idCounter++ +"\">\n"+tempStringMaster+"</item>\n";
		}
		
		retStringMaster+=tempStringMaster;
		
		if(isSlaveContained&&isMasterContained)
			return true;
		else
			return false;
	}


	private static boolean adjoinGroupByPart(QueryStructure slaveQstruct, QueryStructure masterQstruct) {
		// TODO Auto-generated method stub
		boolean isSlaveContained=true;
		String tempStringSlave="";
		
		for(Node slaveGroupByNode:Util.toSetOfNodes(slaveQstruct.getLstGroupByNodes())){
			boolean found=false;
			for(Node masterGroupByNode:Util.toSetOfNodes(masterQstruct.getLstGroupByNodes())){
				if(isEqualForColoring(slaveGroupByNode,masterGroupByNode)){
					found=true;
					break;
				}				
			}
			if(found){
				tempStringSlave+=spaceTab+"<item text=\""+ slaveGroupByNode.toString() +"\" style=\"color:#ee0000;\" id=\""+ idCounter++ +"\"/>\n";
			}
			else{
				tempStringSlave+=spaceTab+"<item text=\""+ slaveGroupByNode.toString() +"\" style=\"color:#ff0000;\" id=\""+ idCounter++ +"\"/>\n";
				isSlaveContained=false;
			}
		}
		if(isSlaveContained){
			tempStringSlave="<item text=\"GroupBy Columns\" open=\"1\" style=\"color:#ee0000 id=\""+ idCounter++ +"\">\n"+tempStringSlave+"</item>\n";
		}
		else{
			tempStringSlave="<item text=\"GroupBy Columns\" open=\"1\" style=\"color:#ff0000 id=\""+ idCounter++ +"\">\n"+tempStringSlave+"</item>\n";			
		}
		retStringSlave+=tempStringSlave;
		
		boolean isMasterContained=true;		
		String tempStringMaster="";
		
		for(Node masterGroupByNode:Util.toSetOfNodes(masterQstruct.getLstGroupByNodes())){
			boolean found=false;
			for(Node slaveGroupByNode:Util.toSetOfNodes(slaveQstruct.getLstGroupByNodes())){
				if(isEqualForColoring(slaveGroupByNode,masterGroupByNode)){
					found=true;
					break;
				}				
			}
			if(found){
				tempStringMaster+=spaceTab+"<item text=\""+ masterGroupByNode.toString() +"\" style=\"color:#ee0000;\" id=\""+ idCounter++ +"\"/>\n";
			}
			else{
				isMasterContained=false;
				tempStringMaster+=spaceTab+"<item text=\""+ masterGroupByNode.toString() +"\" id=\""+ idCounter++ +"\"/>\n";
			}
		}			
		if(isMasterContained){
			tempStringMaster="<item text=\"GroupBy Columns\" open=\"1\" style=\"color:#ee0000 id=\""+ idCounter++ +"\">\n"+tempStringMaster+"</item>\n";
		}
		else{
			tempStringMaster="<item text=\"GroupBy Columns\" open=\"1\" id=\""+ idCounter++ +"\">\n"+tempStringMaster+"</item>\n";
		}
		retStringMaster+=tempStringMaster;
		
		if(isSlaveContained&&isMasterContained)
			return true;
		else
			return false;
	}


	public static void adjoinDistinctPart(QueryStructure slaveQstruct, QueryStructure masterQstruct){
		if(slaveQstruct.getIsDistinct() && masterQstruct.getIsDistinct()){
			retStringSlave+="<item text=\"Distinct Present\" style=\"color:#ee0000;\"  open=\"1\" id=\""+ idCounter++ +"\">\n";
			retStringSlave+=spaceTab+"<item text=\"True\" style=\"color:#ee0000;\"  id=\""+ idCounter++ +"\"/>\n";
			retStringSlave+="</item>\n";
			
			retStringMaster+="<item text=\"Distinct Present\" style=\"color:#ee0000;\"  open=\"1\" id=\""+ idCounter++ +"\">\n";
			retStringMaster+=spaceTab+"<item text=\"True\" style=\"color:#ee0000;\"  id=\""+ idCounter++ +"\"/>\n";
			retStringMaster+="</item>\n";
		}
		else if(!slaveQstruct.getIsDistinct() && !masterQstruct.getIsDistinct()){
			retStringSlave+="<item text=\"Distinct Present\" style=\"color:#ee0000;\"  open=\"1\" id=\""+ idCounter++ +"\">\n";
			retStringSlave+=spaceTab+"<item text=\"False\" style=\"color:#ee0000;\"  id=\""+ idCounter++ +"\"/>\n";
			retStringSlave+="</item>\n";
			
			retStringMaster+="<item text=\"Distinct Present\" style=\"color:#ee0000;\"  open=\"1\" id=\""+ idCounter++ +"\">\n";
			retStringMaster+=spaceTab+"<item text=\"True\" style=\"color:#ee0000;\"  id=\""+ idCounter++ +"\"/>\n";
			retStringMaster+="</item>\n";
		}
		else if(slaveQstruct.getIsDistinct() && !masterQstruct.getIsDistinct()){ 
			retStringSlave+="<item text=\"Distinct Present\" style=\"color:#ff0000;\"  open=\"1\" id=\""+ idCounter++ +"\">\n";
			retStringSlave+=spaceTab+"<item text=\"True\" style=\"color:#ff0000;\"  id=\""+ idCounter++ +"\"/>\n";
			retStringSlave+="</item>\n";
			
			retStringMaster+="<item text=\"Distinct Present\" style=\"color:#ee0000;\"  open=\"1\" id=\""+ idCounter++ +"\">\n";
			retStringMaster+=spaceTab+"<item text=\"False\" style=\"color:#ee0000;\"  id=\""+ idCounter++ +"\"/>\n";
			retStringMaster+="</item>\n";
		}
		else{
			retStringSlave+="<item text=\"Distinct Present\" style=\"color:#ff0000;\"  open=\"1\" id=\""+ idCounter++ +"\">\n";
			retStringSlave+=spaceTab+"<item text=\"False\" style=\"color:#ff0000;\"  id=\""+ idCounter++ +"\"/>\n";
			retStringSlave+="</item>\n";
			
			retStringMaster+="<item text=\"Distinct Present\"   open=\"1\" id=\""+ idCounter++ +"\">\n";
			retStringMaster+=spaceTab+"<item text=\"False\"  id=\""+ idCounter++ +"\"/>\n";
			retStringMaster+="</item>\n";

		}
		
	}
	

	
	private static boolean  adjoinJoinsPart(QueryStructure slaveQstruct, QueryStructure masterQstruct) throws CloneNotSupportedException{

		String tempStringSlaveOuter="";
		boolean isSlaveContainedOuter=true;
		for(Node slaveOuterJoinNode:filterAndGetOuterJoins(slaveQstruct.getLstJoinConditions())){
			boolean found=false;
			for(Node masterOuterJoinNode:filterAndGetOuterJoins(masterQstruct.getLstJoinConditions())){
				if(isEqualForColoring(slaveOuterJoinNode,masterOuterJoinNode)){
					found=true;
					break;
				}							
			}
			if(found){
				tempStringSlaveOuter+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(slaveOuterJoinNode).toString() +"\" style=\"color:#ee0000;\" id=\""+ idCounter++ +"\"/>\n";				
			}
			else{
				tempStringSlaveOuter+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(slaveOuterJoinNode).toString() +"\" style=\"color:#ff0000;\" id=\""+ idCounter++ +"\"/>\n";				
				isSlaveContainedOuter=false;
			}
		}
		if(isSlaveContainedOuter){
			tempStringSlaveOuter="<item text=\"Outer\" open=\"1\" style=\"color:#ee0000 id=\""+ idCounter++ +"\">\n"+tempStringSlaveOuter+"</item>\n";
		}
		else{
			tempStringSlaveOuter="<item text=\"Outer\" open=\"1\" style=\"color:#ff0000 id=\""+ idCounter++ +"\">\n"+tempStringSlaveOuter+"</item>\n";
		}
		
		
		String tempStringMasterOuter="";
		
		boolean isMasterContainedOuter=true;
		
		for(Node masterOuterJoinNode:filterAndGetOuterJoins(masterQstruct.getLstJoinConditions())){
			boolean found=false;
			for(Node slaveOuterJoinNode:filterAndGetOuterJoins(slaveQstruct.getLstJoinConditions())){
				if(isEqualForColoring(slaveOuterJoinNode,masterOuterJoinNode)){
					found=true;
					break;
				}							
			}
			if(found){
				tempStringMasterOuter+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(masterOuterJoinNode).toString() +"\" style=\"color:#ee0000;\" id=\""+ idCounter++ +"\"/>\n";				
			}
			else{
				tempStringMasterOuter+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(masterOuterJoinNode).toString() +"\" id=\""+ idCounter++ +"\"/>\n";				
				isSlaveContainedOuter=false;
			}
		}
		if(isMasterContainedOuter){
			tempStringMasterOuter="<item text=\"Outer\" open=\"1\" style=\"color:#ee0000 id=\""+ idCounter++ +"\">\n"+tempStringMasterOuter+"</item>\n";
		}
		else{
			tempStringMasterOuter="<item text=\"Outer\" open=\"1\"  id=\""+ idCounter++ +"\">\n"+tempStringMasterOuter+"</item>\n";
		}
		
		
		String tempStringSlaveInner="";
		boolean isSlaveContainedInner=true;
		for(Node slaveInnerJoinNode:filterAndGetInnerJoins(slaveQstruct.getLstJoinConditions())){
			boolean found=false;
			for(Node masterInnerJoinNode:filterAndGetInnerJoins(masterQstruct.getLstJoinConditions())){
				if(isEqualForColoring(slaveInnerJoinNode,masterInnerJoinNode)){
					found=true;
					break;
				}							
			}
			if(found){
				tempStringSlaveInner+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(slaveInnerJoinNode).toString() +"\" style=\"color:#ee0000;\" id=\""+ idCounter++ +"\"/>\n";				
			}
			else{
				tempStringSlaveInner+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(slaveInnerJoinNode).toString() +"\" style=\"color:#ff0000;\" id=\""+ idCounter++ +"\"/>\n";				
				isSlaveContainedInner=false;
			}
		}
		if(isSlaveContainedInner){
			tempStringSlaveInner="<item text=\"Inner\" open=\"1\" style=\"color:#ee0000 id=\""+ idCounter++ +"\">\n"+tempStringSlaveInner+"</item>\n";
		}
		else{
			tempStringSlaveInner="<item text=\"Inner\" open=\"1\" style=\"color:#ff0000 id=\""+ idCounter++ +"\">\n"+tempStringSlaveInner+"</item>\n";
		}
		
		
		boolean isMasterContainedInner=true;
		
		String tempStringMasterInner="";
		for(Node masterInnerJoinNode:filterAndGetInnerJoins(masterQstruct.getLstJoinConditions())){
			boolean found=false;
			for(Node slaveInnerJoinNode:filterAndGetInnerJoins(slaveQstruct.getLstJoinConditions())){
				if(isEqualForColoring(slaveInnerJoinNode,masterInnerJoinNode)){
					found=true;
					break;
				}							
			}
			if(found){
				tempStringMasterInner+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(masterInnerJoinNode).toString() +"\" style=\"color:#ee0000;\" id=\""+ idCounter++ +"\"/>\n";				
			}
			else{
				tempStringMasterInner+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(masterInnerJoinNode).toString() +"\" id=\""+ idCounter++ +"\"/>\n";				
				isMasterContainedInner=false;
			}
		}
		if(isMasterContainedInner){
			tempStringMasterInner="<item text=\"Inner\" open=\"1\" style=\"color:#ee0000 id=\""+ idCounter++ +"\">\n"+tempStringMasterInner+"</item>\n";
		}
		else{
			tempStringMasterInner="<item text=\"Inner\" open=\"1\"  id=\""+ idCounter++ +"\">\n"+tempStringMasterInner+"</item>\n";
		}		
		
		if(isSlaveContainedOuter&&isSlaveContainedInner){
			retStringSlave+="<item text=\"Join Conditions\" open=\"1\" style=\"color:#ee0000 id=\""+ idCounter++ +"\">\n"+tempStringSlaveOuter+tempStringSlaveInner+"</item>\n";
		}
		else{
			retStringSlave+="<item text=\"Join Conditions\" open=\"1\" style=\"color:#ff0000 id=\""+ idCounter++ +"\">\n"+tempStringSlaveOuter+tempStringSlaveInner+"</item>\n";
		}
		
		if(isMasterContainedOuter&&isMasterContainedInner){
			retStringMaster+="<item text=\"Join Conditions\" open=\"1\" style=\"color:#ee0000 id=\""+ idCounter++ +"\">\n"+tempStringMasterOuter+tempStringMasterInner+"</item>\n";
		}
		else{
			retStringMaster+="<item text=\"Join Conditions\" open=\"1\" id=\""+ idCounter++ +"\">\n"+tempStringMasterOuter+tempStringMasterInner+"</item>\n";
		}
		
		if(isMasterContainedInner&&isMasterContainedOuter&&isSlaveContainedOuter&&isSlaveContainedInner){
			return true;
		}
		else{
			return false;
		}
	}
	
	private static boolean adjoinSelectionPart(QueryStructure slaveQstruct, QueryStructure masterQstruct) throws CloneNotSupportedException{
		// TODO Auto-generated method stub
		
		String tempStringSlave="";
		boolean isSlaveContained=true;
		for(Node slaveSelectionNode:Util.toSetOfNodes(slaveQstruct.getLstSelectionConditions())){
			boolean found=false;
			for(Node masterSelectionNode:Util.toSetOfNodes(masterQstruct.getLstSelectionConditions())){
				if(isEqualForColoring(slaveSelectionNode,masterSelectionNode)){
					found=true;
					break;
				}				
			}
			if(found){
				tempStringSlave+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(slaveSelectionNode).toString() +"\" style=\"color:#ee0000;\" id=\""+ idCounter++ +"\"/>\n";
			}
			else{
				tempStringSlave+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(slaveSelectionNode).toString() +"\" style=\"color:#ff0000;\" id=\""+ idCounter++ +"\"/>\n";
				isSlaveContained=false;
			}
		}
		if(isSlaveContained){
			tempStringSlave="<item text=\"Selection Conditions\" open=\"1\" style=\"color:#ee0000 id=\""+ idCounter++ +"\">\n"+tempStringSlave+"</item>\n";
		}
		else{
			tempStringSlave="<item text=\"Selection Conditions\" open=\"1\" style=\"color:#ff0000 id=\""+ idCounter++ +"\">\n"+tempStringSlave+"</item>\n";			
		}
		retStringSlave+=tempStringSlave;
		
		String tempStringMaster="";
		boolean isMasterContained=true;		
		for(Node masterSelectionNode:Util.toSetOfNodes(masterQstruct.getLstSelectionConditions())){
			boolean found=false;
			for(Node slaveSelectionNode:Util.toSetOfNodes(slaveQstruct.getLstSelectionConditions())){
				if(isEqualForColoring(slaveSelectionNode,masterSelectionNode)){
					found=true;
					break;
				}				
			}
			if(found){
				tempStringMaster+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(masterSelectionNode).toString() +"\" style=\"color:#ee0000;\" id=\""+ idCounter++ +"\"/>\n";
			}
			else{
				isMasterContained=false;
				tempStringMaster+=spaceTab+"<item text=\""+ cloneNodeForXMLserialization(masterSelectionNode).toString() +"\" id=\""+ idCounter++ +"\"/>\n";
			}
		}			
		if(isMasterContained){
			tempStringMaster="<item text=\"Selection Conditions\" open=\"1\" style=\"color:#ee0000 id=\""+ idCounter++ +"\">\n"+tempStringMaster+"</item>\n";
		}
		else{
			tempStringMaster="<item text=\"Selection Conditions\" open=\"1\" id=\""+ idCounter++ +"\">\n"+tempStringMaster+"</item>\n";
		}
		
		if(isSlaveContained&&isMasterContained)
			return true;
		else
			return false;
		
	}

	
	private static boolean  adjoinProjectionPart(QueryStructure slaveQstruct, QueryStructure masterQstruct) {

		boolean isSlaveContained=true;
		String tempStringSlave="";
		for(Node slaveProjNode:Util.toSetOfNodes(slaveQstruct.getLstProjectedCols())){
			boolean found=false;
			for(Node masterProjNode:Util.toSetOfNodes(masterQstruct.getLstProjectedCols())){
				if(isEqualForColoring(slaveProjNode,masterProjNode)){
					found=true;
					break;
				}				
			}
			if(found){
				tempStringSlave+=spaceTab+"<item text=\""+ slaveProjNode.toString() +"\" style=\"color:#ee0000;\" id=\""+ idCounter++ +"\"/>\n";
			}
			else{
				tempStringSlave+=spaceTab+"<item text=\""+ slaveProjNode.toString() +"\" style=\"color:#ff0000;\" id=\""+ idCounter++ +"\"/>\n";
				isSlaveContained=false;
			}
		}
		if(isSlaveContained){
			tempStringSlave="<item text=\"Projected Columns\" open=\"1\" style=\"color:#ee0000 id=\""+ idCounter++ +"\">\n"+tempStringSlave+"</item>\n";
		}
		else{
			tempStringSlave="<item text=\"Projected Columns\" open=\"1\" style=\"color:#ff0000 id=\""+ idCounter++ +"\">\n"+tempStringSlave+"</item>\n";			
		}
		retStringSlave+=tempStringSlave;
		
		String tempStringMaster="";
		boolean isMasterContained=true;		
		for(Node masterProjNode:Util.toSetOfNodes(masterQstruct.getLstProjectedCols())){
			boolean found=false;
			for(Node slaveProjNode:Util.toSetOfNodes(slaveQstruct.getLstProjectedCols())){
				if(isEqualForColoring(slaveProjNode,masterProjNode)){
					found=true;
					break;
				}				
			}
			if(found){
				tempStringMaster+=spaceTab+"<item text=\""+ masterProjNode.toString() +"\" style=\"color:#ee0000;\" id=\""+ idCounter++ +"\"/>\n";
			}
			else{
				isMasterContained=false;
				tempStringMaster+=spaceTab+"<item text=\""+ masterProjNode.toString() +"\" id=\""+ idCounter++ +"\"/>\n";
			}
		}			
		if(isMasterContained){
			tempStringMaster="<item text=\"Projected Columns\" open=\"1\" style=\"color:#ee0000 id=\""+ idCounter++ +"\">\n"+tempStringMaster+"</item>\n";
		}
		else{
			tempStringMaster="<item text=\"Projected Columns\" open=\"1\" id=\""+ idCounter++ +"\">\n"+tempStringMaster+"</item>\n";
		}
		
		retStringMaster+=tempStringMaster;
		
		if(isSlaveContained&&isMasterContained)
			return true;
		else
			return false;
	}


	public static boolean containmentForColoring(Vector<Node> Slave, Vector<Node> Master){
		for(Node a:Slave){
			boolean found=false;
			for(Node b:Master){
				if(isEqualForColoring(a,b)){
					found=true;
					break;
				}
			}
			if(!found)
				return false;
		}
		return true;
			
	}
	
	public static String getProjectedColumnsString(QueryStructure qStruct,boolean openFlag){
		String retString="";
		if(openFlag){
			if(qStruct.getLstProjectedCols().size()>0){
				retString+="<item text=\"Projected Columns\" open=\"1\" id=\""+ idCounter++ +"\">\n";
				for(parsing.Node n:Util.toSetOfNodes(qStruct.getLstProjectedCols())){
					retString+=spaceTab+"<item text=\""+ n.toString() +"\"  id=\""+ idCounter++ +"\"/>\n";
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
	
	public static String getHasDistinctString(QueryStructure qStruct, boolean openFlag){
		if(openFlag){
			String retString="<item text=\"Distinct Present\"  open=\"1\" id=\""+ idCounter++ +"\">\n";
			if(qStruct.getIsDistinct()){
				retString+=spaceTab+"<item text=\"True\"  id=\""+ idCounter++ +"\"/>\n";
			}
			else{
				retString+=spaceTab+"<item text=\"False\" id=\""+ idCounter++ +"\"/>\n";
			}
			retString+="</item>\n";
			return retString;
		}
		else{
			String retString="<item text=\"Distinct Present\"  id=\""+ idCounter++ +"\">\n";
			if(qStruct.getIsDistinct()){
				retString+=spaceTab+"<item text=\"True\"  id=\""+ idCounter++ +"\"/>\n";
			}
			else{
				retString+=spaceTab+"<item text=\"False\"  id=\""+ idCounter++ +"\"/>\n";
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
		parsing.Node n=(Node)m.clone();
		
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
	
	public static ArrayList<Node> filterAndGetOuterJoins(ArrayList<Node> lstJoinConditions){
		ArrayList<Node> outerJoins=new ArrayList<Node>();
		for(parsing.Node n:Util.toSetOfNodes(lstJoinConditions)){
			if(n.getJoinType()!=null&&(n.getJoinType().equals(JoinClauseInfo.leftOuterJoin)
					||n.getJoinType().equals(JoinClauseInfo.rightOuterJoin)
					||n.getJoinType().equals(JoinClauseInfo.fullOuterJoin)))
				outerJoins.add(n);
		}
		return outerJoins;
	}
	
	public static ArrayList<Node> filterAndGetInnerJoins(ArrayList<Node> lstJoinConditions){
		ArrayList<Node> innerJoins=new ArrayList<Node>();
		for(parsing.Node n:Util.toSetOfNodes(lstJoinConditions)){
			if(n.getJoinType()!=null&&!n.getJoinType().equals(JoinClauseInfo.leftOuterJoin)
					&&!n.getJoinType().equals(JoinClauseInfo.rightOuterJoin)
					&&!n.getJoinType().equals(JoinClauseInfo.fullOuterJoin))
				innerJoins.add(n);

		}
		return innerJoins;
	}
	
	public static String getJoinConditionsString(QueryStructure qStruct, boolean openFlag) throws CloneNotSupportedException{
		 String retString="";
		if(openFlag){
			if(qStruct.getLstJoinConditions().size()>0){
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
			if(qStruct.getLstJoinConditions().size()>0){
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
	
	public static void printHead(PrintWriter out){
		out.println("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");
		out.println("<tree id=\"0\">");
	
	}
	
	public static void printTail(PrintWriter out){
		out.println("</tree>");
	}

}
