/**
 * 
 */
package partialMarking.queryEdit;

import java.util.ArrayList;
import java.util.List;

import parsing.Node;
import parsing.QueryStructure;
import util.Utilities;

/**
 * @author udbhas
 *
 */
public class OrderBy implements QueryComponent {

	/* (non-Javadoc)
	 * @see partialMarking.queryEdit.QueryComponent#edit(parsing.QueryStructure, parsing.QueryStructure)
	 */
	@Override
	public List<QueryStructure> edit(QueryStructure student, QueryStructure instructor) throws Exception {
		List<QueryStructure> a = new ArrayList <QueryStructure>();
		QueryStructure stu_not_matched = (QueryStructure)Utilities.copy(student);
		QueryStructure stu_matched = (QueryStructure)Utilities.copy(student);	
		QueryStructure ins_not_matched = (QueryStructure)Utilities.copy(instructor);
		for(Node t:instructor.getLstOrderByNodes())
		{
			if(student.getLstOrderByNodes().contains(t))
			{
				ins_not_matched.getLstOrderByNodes().remove(t);
			}
		}
		for(Node t:student.getLstOrderByNodes())
		{
			if(instructor.getLstOrderByNodes().contains(t))
			{
				stu_not_matched.getLstOrderByNodes().remove(t);
			}
			else
			{
				stu_matched.getLstOrderByNodes().remove(t);
			}
		}
		int c=0;
		int size = student.getLstOrderByNodes().size();
		for(Node st:student.getLstOrderByNodes())
		{
			if(stu_not_matched.getLstOrderByNodes().contains(st))
			{
				for(Node t: ins_not_matched.getLstOrderByNodes())
				{
					QueryStructure temp = (QueryStructure)Utilities.copy(student);
					if(c==size-1)
						temp.getLstOrderByNodes().add(t);
					else
						temp.getLstOrderByNodes().add(c,t);
					temp.getLstOrderByNodes().remove(st);
					a.add(temp);
				}
			}
			c++;
		}
		return a;
	}

	/* (non-Javadoc)
	 * @see partialMarking.queryEdit.QueryComponent#add(parsing.QueryStructure, parsing.QueryStructure)
	 */
	@Override
	public List<QueryStructure> add(QueryStructure student, QueryStructure instructor) throws Exception {
		List<QueryStructure> a = new ArrayList <QueryStructure>();
		for(Node t:instructor.getLstOrderByNodes())
		{
			if(!student.getLstOrderByNodes().contains(t))
			{
				int size=student.getLstOrderByNodes().size();
				int i=0;
				while(i<size)
				{
					QueryStructure temp = (QueryStructure)Utilities.copy(student);
					temp.getLstOrderByNodes().add(i,t);
					a.add(temp);
					i++;
				}
				QueryStructure temp = (QueryStructure)Utilities.copy(student);
				temp.getLstOrderByNodes().add(t);
				a.add(temp);
			}
		}
		return a;
	}

	/* (non-Javadoc)
	 * @see partialMarking.queryEdit.QueryComponent#remove(parsing.QueryStructure, parsing.QueryStructure)
	 */
	@Override
	public List<QueryStructure> remove(QueryStructure student, QueryStructure instructor) throws Exception {
		List<QueryStructure> a = new ArrayList <QueryStructure>();
		for(Node t:student.getLstOrderByNodes())
		{
			if(!instructor.getLstOrderByNodes().contains(t))
			{
				QueryStructure temp = (QueryStructure)Utilities.copy(student);
				temp.getLstOrderByNodes().remove(t);
				a.add(temp);
			}	
		}
		return a;
	}
	public List<QueryStructure> move(QueryStructure student, QueryStructure instructor) throws Exception {
		List<QueryStructure> a = new ArrayList <QueryStructure>();
		int c=0;
		for(Node t:student.getLstOrderByNodes())
		{
			QueryStructure temp = (QueryStructure)Utilities.copy(student);
			temp.getLstOrderByNodes().remove(t);
			int size=temp.getLstOrderByNodes().size();
			int i=0;
			while(i<size)
			{
				if(c!=i)
				{
					QueryStructure temp1 = (QueryStructure)Utilities.copy(temp);
					temp1.getLstOrderByNodes().add(i,t);
					a.add(temp1);
				}
				i++;
			}
			if(c!=i)
			{
				QueryStructure temp1 = (QueryStructure)Utilities.copy(temp);
				temp1.getLstOrderByNodes().add(t);
				a.add(temp1);
			}
			c++;
		}
		return a;
	}
	
}
