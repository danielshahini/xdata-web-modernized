package partialMarking.queryEdit;

import java.util.ArrayList;
import java.util.List;

import parsing.Node;
import parsing.QueryStructure;
import util.Utilities;

public class Projection implements QueryComponent {

	@Override
	public List<QueryStructure> edit(QueryStructure student, QueryStructure instructor) throws Exception {
		List<QueryStructure> a = new ArrayList <QueryStructure>();
		QueryStructure stu_not_matched = (QueryStructure)Utilities.copy(student);
		QueryStructure stu_matched = (QueryStructure)Utilities.copy(student);	
		QueryStructure ins_not_matched = (QueryStructure)Utilities.copy(instructor);
		for(Node t:instructor.getLstProjectedCols())
		{
			if(student.getLstProjectedCols().contains(t))
			{
				ins_not_matched.getLstProjectedCols().remove(t);
			}
		}
		for(Node t:student.getLstProjectedCols())
		{
			if(instructor.getLstProjectedCols().contains(t))
			{
				stu_not_matched.getLstProjectedCols().remove(t);
			}
			else
			{
				stu_matched.getLstProjectedCols().remove(t);
			}
		}
		for(Node st:stu_not_matched.getLstProjectedCols())
		{
			for(Node t: ins_not_matched.getLstProjectedCols())
			{
				QueryStructure temp = (QueryStructure)Utilities.copy(student);
				temp.getLstProjectedCols().remove(st);
				temp.getLstProjectedCols().add(t);
				a.add(temp);
			}
		}
		return a;
	}

	@Override
	public List<QueryStructure> add(QueryStructure student, QueryStructure instructor) throws Exception {
		List<QueryStructure> a = new ArrayList <QueryStructure>();
		for(Node t:instructor.getLstProjectedCols())
		{
			if(!student.getLstProjectedCols().contains(t))
			{
				QueryStructure temp = (QueryStructure)Utilities.copy(student);
				temp.getLstProjectedCols().add(t);
				a.add(temp);
			}
		}
		return a;
	}

	@Override
	public List<QueryStructure> remove(QueryStructure student, QueryStructure instructor) throws Exception {
		List<QueryStructure> a = new ArrayList <QueryStructure>();
		for(Node t:student.getLstProjectedCols())
		{
			if(!instructor.getLstProjectedCols().contains(t))
			{
				QueryStructure temp = (QueryStructure)Utilities.copy(student);
				temp.getLstProjectedCols().remove(t);
				a.add(temp);
			}	
		}
		
		return a;
	}

}
