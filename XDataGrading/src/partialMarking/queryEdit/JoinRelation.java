package partialMarking.queryEdit;

import java.util.ArrayList;
import java.util.List;

import parsing.Node;
import parsing.QueryStructure;
import util.Utilities;

public class JoinRelation implements QueryComponent {
	private boolean isDependent(QueryStructure student,String s)
	{
		// Check dependency in ProjectionCol
		ArrayList<Node> projectionNodes = student.getLstProjectedCols();
		for(Node t: projectionNodes)
		{
			if(s.equals(t.getTableNameNo()))
			{
				return true;
			}
		}
		// Check dependency in groupBy clauses
		ArrayList<Node> groupByNodes = student.getLstGroupByNodes();
		for(Node t: groupByNodes)
		{
			if(s.equals(t.getTableNameNo()))
			{
				return true;
			}
		}
		// Check dependency in orderBy clauses
		ArrayList<Node> orderByNodes = student.getLstOrderByNodes();
		for(Node t: orderByNodes)
		{
			if(s.equals(t.getTableNameNo()))
			{
				return true;
			}
		}
		// Check dependency in selection clauses
		ArrayList<Node> selectionNodes = student.getLstSelectionConditions();
		for(Node t: selectionNodes)
		{
			if(s.equals(t.getLeft().getTableNameNo()) || s.equals(t.getRight().getTableNameNo()))
			{
				return true;
			}
		}
//		// Check dependency in having clauses
				ArrayList<Node> havingNodes = student.getLstHavingConditions();
				for(Node t: havingNodes)
				{
					if(s.equals(t.getLeft().getTableNameNo()) || s.equals(t.getRight().getTableNameNo()))
					{
						return true;
					}
				}
		return false;
	}
	@Override
	public List<QueryStructure> edit(QueryStructure student, QueryStructure instructor) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<QueryStructure> add(QueryStructure student, QueryStructure instructor) throws Exception {
		
		List<QueryStructure> a = new ArrayList <QueryStructure>();
		for(String t:instructor.getLstRelationInstances())
		{
			if(!student.getLstRelationInstances().contains(t))
			{
				QueryStructure temp = (QueryStructure)Utilities.copy(student);
				//temp.getLstRelations().add(t);
				temp.getLstRelationInstances().add(t);
				a.add(temp);
			}
		}
		return a;
	}
	
	@Override
	public List<QueryStructure> remove(QueryStructure student, QueryStructure instructor) throws Exception {
		List<QueryStructure> a = new ArrayList <QueryStructure>();
		for(String t:student.getLstRelationInstances())
		{
			if(!instructor.getLstRelationInstances().contains(t) && !isDependent(student,t))
			{
				QueryStructure temp = (QueryStructure)Utilities.copy(student);
				temp.getLstRelationInstances().remove(t);
				a.add(temp);
			}	
		}
		return a;
	}

}
