package partialMarking.queryEdit;

import java.util.List;

import parsing.QueryStructure;

public interface QueryComponent {
	public List<QueryStructure> edit(QueryStructure student, QueryStructure instructor)throws Exception;
	public List<QueryStructure> add(QueryStructure student, QueryStructure instructor)throws Exception;
	public List<QueryStructure> remove(QueryStructure student, QueryStructure instructor)throws Exception;
}
