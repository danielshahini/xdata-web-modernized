package partialMarking.queryEdit;

import java.util.ArrayList;
import java.util.List;

import parsing.Node;
import parsing.QueryStructure;
import util.Utilities;

public class GroupBy implements QueryComponent {

	@Override
	public List<QueryStructure> edit(QueryStructure student, QueryStructure instructor) throws Exception{
		List<QueryStructure> a = new ArrayList <QueryStructure>();
		QueryStructure stu_not_matched = (QueryStructure)Utilities.copy(student);
		QueryStructure stu_matched = (QueryStructure)Utilities.copy(student);	
		QueryStructure ins_not_matched = (QueryStructure)Utilities.copy(instructor);
		for(Node t:instructor.getLstGroupByNodes())
		{
			if(student.getLstGroupByNodes().contains(t))
			{
				ins_not_matched.getLstGroupByNodes().remove(t);
			}
		}
		for(Node t:student.getLstGroupByNodes())
		{
			if(instructor.getLstGroupByNodes().contains(t))
			{
				stu_not_matched.getLstGroupByNodes().remove(t);
			}
			else
			{
				stu_matched.getLstGroupByNodes().remove(t);
			}
		}
		for(Node st:stu_not_matched.getLstGroupByNodes())
		{
			for(Node t: ins_not_matched.getLstGroupByNodes())
			{
				QueryStructure temp = (QueryStructure)Utilities.copy(student);
				temp.getLstGroupByNodes().remove(st);
				//temp.getLstProjectedCols().remove(st);
				temp.getLstGroupByNodes().add(t);
				a.add(temp);
			}
		}
		return a;
	}

	@Override
	public List<QueryStructure> add(QueryStructure student, QueryStructure instructor) throws Exception {
		List<QueryStructure> a = new ArrayList <QueryStructure>();
		for(Node t:instructor.getLstGroupByNodes())
		{
			if(!student.getLstGroupByNodes().contains(t))
			{
				QueryStructure temp = (QueryStructure)Utilities.copy(student);
				temp.getLstGroupByNodes().add(t);
				a.add(temp);
			}
		}
		return a;
	}

	@Override
	public List<QueryStructure> remove(QueryStructure student, QueryStructure instructor) throws Exception{
		
		List<QueryStructure> a = new ArrayList <QueryStructure>();
		for(Node t:student.getLstGroupByNodes())
		{
			if(!instructor.getLstGroupByNodes().contains(t))
			{
				QueryStructure temp = (QueryStructure)Utilities.copy(student);
				temp.getLstGroupByNodes().remove(t);
				//temp.getLstProjectedCols().remove(t);
				a.add(temp);
			}	
		}
		
		return a;
	}

}
