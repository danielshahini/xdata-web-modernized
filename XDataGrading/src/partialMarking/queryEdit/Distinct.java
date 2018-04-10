package partialMarking.queryEdit;

import java.util.ArrayList;
import java.util.List;

import parsing.QueryStructure;
import util.Utilities;

public class Distinct implements QueryComponent {

	@Override
	public List<QueryStructure> edit(QueryStructure student, QueryStructure instructor) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<QueryStructure> add(QueryStructure student, QueryStructure instructor) throws Exception {
		List<QueryStructure> a = new ArrayList <QueryStructure>();
		if(instructor.getIsDistinct()==true && student.getIsDistinct()==false)
		{
			QueryStructure temp = (QueryStructure)Utilities.copy(student);
			temp.setIsDistinct(true);
			a.add(temp);
		}
		return a;
	}

	@Override
	public List<QueryStructure> remove(QueryStructure student, QueryStructure instructor) throws Exception {
		List<QueryStructure> a = new ArrayList <QueryStructure>();
		if(instructor.getIsDistinct()==false && student.getIsDistinct()==true)
		{
			QueryStructure temp = (QueryStructure)Utilities.copy(student);
			temp.setIsDistinct(false);
			a.add(temp);
		}
		return a;
	}

}
