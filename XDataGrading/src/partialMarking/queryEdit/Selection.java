package partialMarking.queryEdit;

import java.util.ArrayList;
import java.util.List;

import parsing.Node;
import parsing.QueryStructure;
import util.Utilities;
import util.Pair;
public class Selection implements QueryComponent {

	public static float NodeDiff(Node n1,Node n2)
	{
		float total_score=0;
		if(n1.getOperator().equals(n2.getOperator()))
			total_score++;		

		//if left node of n1 is a column reference
		boolean tag=true;
		if(n1.getLeft().getNodeType().equals(Node.getColRefType())){
			if(!n2.getLeft().getNodeType().equals(Node.getColRefType()))
				tag=  false;
			if(!n1.getLeft().getTable().getTableName().equals(n2.getLeft().getTable().getTableName()))
				tag=  false;

			if(!n1.getLeft().getTableNameNo().equals(n2.getLeft().getTableNameNo()))
				tag=  false;
			if(!n1.getLeft().getColumn().getColumnName().equals(n2.getLeft().getColumn().getColumnName()))
				tag=  false;
		}
		//if left node of n1 is a constant value
		if(n1.getLeft().getNodeType().equals(Node.getValType())){
			if(!n2.getLeft().getNodeType().equals(Node.getValType()))
				tag=   false;
			if(!n1.getLeft().getStrConst().equals(n2.getLeft().getStrConst()))
				tag=  false;
		}
		if(tag)
			total_score++;
		tag=true;
		if(n1.getRight().getNodeType().equals(Node.getColRefType())){

			if(!n2.getRight().getNodeType().equals(Node.getColRefType()))
				tag=   false;

			if(!n1.getRight().getTable().getTableName().equals(n2.getRight().getTable().getTableName()))
				tag=  false;

			if(!n1.getRight().getTableNameNo().equals(n2.getRight().getTableNameNo()))
				tag=  false;

			if(!n1.getRight().getColumn().getColumnName().equals(n2.getRight().getColumn().getColumnName()))
				tag=  false;
		}

		if(n1.getRight().getNodeType().equals(Node.getValType())){
			if(!n2.getRight().getNodeType().equals(Node.getValType()))
				tag=   false;

			if(!n1.getRight().getStrConst().equals(n2.getRight().getStrConst()))
				tag=  false;
		}

		if(tag)
			total_score++;

			
		return (float) total_score;
	}
	public List<Pair<QueryStructure,Float>> edit(QueryStructure student, QueryStructure instructor) throws Exception {
		List<Pair<QueryStructure,Float>> a = new ArrayList <Pair<QueryStructure,Float>>();
		QueryStructure stu_not_matched = (QueryStructure)Utilities.copy(student);
		QueryStructure stu_matched = (QueryStructure)Utilities.copy(student);	
		QueryStructure ins_not_matched = (QueryStructure)Utilities.copy(instructor);
		for(Node t:instructor.getLstSelectionConditions())
		{
			if(student.getLstSelectionConditions().contains(t))
			{
				ins_not_matched.getLstSelectionConditions().remove(t);
			}
		}
		for(Node t:student.getLstSelectionConditions())
		{
			if(instructor.getLstSelectionConditions().contains(t))
			{
				stu_not_matched.getLstSelectionConditions().remove(t);
			}
			else
			{
				stu_matched.getLstSelectionConditions().remove(t);
			}
		}
		for(Node st:stu_not_matched.getLstSelectionConditions())
		{
			for(Node t: ins_not_matched.getLstSelectionConditions())
			{
				QueryStructure temp = (QueryStructure)Utilities.copy(student);
				temp.getLstSelectionConditions().remove(st);
				temp.getLstSelectionConditions().add(t);
				Pair<QueryStructure,Float> tempCost= new Pair<QueryStructure,Float> ();
				tempCost.setFirst(temp);
				tempCost.setSecond(3-NodeDiff(st,t));
				a.add(tempCost);
				
			}
		}
		return a;
	}

	@Override
	public List<Pair<QueryStructure,Float>> add(QueryStructure student, QueryStructure instructor) throws Exception {
		List<Pair<QueryStructure,Float>> a = new ArrayList <Pair<QueryStructure,Float>>();
		for(Node t:instructor.getLstSelectionConditions())
		{
			if(!student.getLstSelectionConditions().contains(t))
			{
				QueryStructure temp = (QueryStructure)Utilities.copy(student);
				temp.getLstSelectionConditions().add(t);
				Pair<QueryStructure,Float> tempCost= new Pair<QueryStructure,Float> ();
				tempCost.setFirst(temp);
				tempCost.setSecond((float) 3.0);
				a.add(tempCost);
			}
		}
		return a;
	}

	@Override
	public List<Pair<QueryStructure,Float>> remove(QueryStructure student, QueryStructure instructor) throws Exception {
		List<Pair<QueryStructure,Float>> a = new ArrayList <Pair<QueryStructure,Float>>();
		for(Node t:student.getLstSelectionConditions())
		{
			if(!instructor.getLstSelectionConditions().contains(t))
			{
				QueryStructure temp = (QueryStructure)Utilities.copy(student);
				temp.getLstSelectionConditions().remove(t);
				Pair<QueryStructure,Float> tempCost= new Pair<QueryStructure,Float> ();
				tempCost.setFirst(temp);
				tempCost.setSecond((float) 3.0);
				a.add(tempCost);
			}	
		}
		
		return a;
	}

}
