package com.xdata.legacy.partialMarking.queryEdit;

import java.util.List;
import com.xdata.legacy.util.Pair;
import com.xdata.legacy.parsing.QueryStructure;

public interface QueryComponent {
	public List<Pair<QueryStructure,Float>> edit(QueryStructure student, QueryStructure instructor)throws Exception;
	public List<Pair<QueryStructure,Float>> add(QueryStructure student, QueryStructure instructor)throws Exception;
	public List<Pair<QueryStructure,Float>> remove(QueryStructure student, QueryStructure instructor)throws Exception;
}
