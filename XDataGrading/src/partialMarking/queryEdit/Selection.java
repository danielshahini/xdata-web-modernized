package partialMarking.queryEdit;

import java.util.ArrayList;
import java.util.List;

import parsing.Node;
import parsing.QueryStructure;
import util.Utilities;

public class Selection implements QueryComponent {

	@Override
	public List<QueryStructure> edit(QueryStructure student, QueryStructure instructor) throws Exception {
		List<QueryStructure> a = new ArrayList <QueryStructure>();
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
				a.add(temp);
			}
		}
		return a;
	}

	@Override
	public List<QueryStructure> add(QueryStructure student, QueryStructure instructor) throws Exception {
		List<QueryStructure> a = new ArrayList <QueryStructure>();
		for(Node t:instructor.getLstSelectionConditions())
		{
			if(!student.getLstSelectionConditions().contains(t))
			{
				QueryStructure temp = (QueryStructure)Utilities.copy(student);
				temp.getLstSelectionConditions().add(t);
				a.add(temp);
			}
		}
		return a;
	}

	@Override
	public List<QueryStructure> remove(QueryStructure student, QueryStructure instructor) throws Exception {
		List<QueryStructure> a = new ArrayList <QueryStructure>();
		for(Node t:student.getLstSelectionConditions())
		{
			if(!instructor.getLstSelectionConditions().contains(t))
			{
				QueryStructure temp = (QueryStructure)Utilities.copy(student);
				temp.getLstSelectionConditions().remove(t);
				a.add(temp);
			}	
		}
		
		return a;
	}

}
