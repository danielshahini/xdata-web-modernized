package partialMarking.queryEdit;

import java.util.ArrayList;
import java.util.List;

import parsing.QueryStructure;
import util.Pair;
public class SingleEdit {
	public static List<Pair<QueryStructure,Float> > single_edit(QueryStructure student, QueryStructure instructor) throws Exception
	{
		List<QueryStructure> edited_query_structure = new ArrayList <QueryStructure>();
		List<QueryStructure> selection_cond_deleted = new Selection().remove(student,instructor);
		List<QueryStructure> selection_cond_added = new Selection().add(student,instructor);
		List<QueryStructure> selection_cond_edited = new Selection().edit(student,instructor);
		List<QueryStructure> projection_cond_deleted = new Projection().remove(student,instructor);
		List<QueryStructure> projection_cond_added = new Projection().add(student,instructor);
		List<QueryStructure> projection_cond_edited = new Projection().edit(student,instructor);
		List<QueryStructure> groupby_cond_deleted = new GroupBy().remove(student,instructor);
		List<QueryStructure> groupby_cond_added = new GroupBy().add(student,instructor);
		List<QueryStructure> groupby_cond_edited = new GroupBy().edit(student,instructor);
		List<QueryStructure> rel_cond_added = new JoinRelation().add(student,instructor);
		List<QueryStructure> rel_cond_deleted = new JoinRelation().remove(student,instructor);
		List<QueryStructure> rel_cond_edited = new JoinRelation().edit(student,instructor);
		List<QueryStructure> join_cond_added = new JoinCondition().add(student,instructor);
		List<QueryStructure> join_cond_deleted = new JoinCondition().remove(student,instructor);
		List<QueryStructure> join_cond_edited = new JoinCondition().edit(student,instructor);
		List<QueryStructure> distinct_added = new Distinct().add(student,instructor);
		List<QueryStructure> distinct_deleted = new Distinct().remove(student,instructor);
		
		for(QueryStructure t:selection_cond_deleted)
		{
			edited_query_structure.add(t);
		}
		for(QueryStructure t:selection_cond_added)
		{
			edited_query_structure.add(t);
		}
		for(QueryStructure t:selection_cond_edited)
		{
			edited_query_structure.add(t);
		}
		for(QueryStructure t:projection_cond_deleted)
		{
			edited_query_structure.add(t);
		}
		for(QueryStructure t:projection_cond_added)
		{
			edited_query_structure.add(t);
		}
		for(QueryStructure t:projection_cond_edited)
		{
			edited_query_structure.add(t);
		}
		for(QueryStructure t:groupby_cond_deleted)
		{
			edited_query_structure.add(t);
		}
		for(QueryStructure t:groupby_cond_added)
		{
			edited_query_structure.add(t);
		}
		for(QueryStructure t:groupby_cond_edited)
		{
			edited_query_structure.add(t);
		}
		for(QueryStructure t:rel_cond_added)
		{
			edited_query_structure.add(t);
		}
		for(QueryStructure t:rel_cond_deleted)
		{
			edited_query_structure.add(t);
		}
		for(QueryStructure t:rel_cond_edited)
		{
			edited_query_structure.add(t);
		}
		for(QueryStructure t:join_cond_added)
		{
			edited_query_structure.add(t);
		}
		for(QueryStructure t:join_cond_deleted)
		{
			edited_query_structure.add(t);
		}
		for(QueryStructure t:join_cond_edited)
		{
			edited_query_structure.add(t);
		}
		for(QueryStructure t:distinct_added)
		{
			edited_query_structure.add(t);
		}
		for(QueryStructure t:distinct_deleted)
		{
			edited_query_structure.add(t);
		}
		return null;
	}
}
