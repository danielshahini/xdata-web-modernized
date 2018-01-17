package partialMarking.queryEdit;

import java.util.ArrayList;
import java.util.List;

import parsing.Node;
import parsing.QueryStructure;
import util.Utilities;

public class JoinRelation implements QueryComponent {

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
		// TODO Auto-generated method stub
		return null;
	}

}
