package com.xdata.eval.port;

import com.xdata.eval.core.QueryPair;
import com.xdata.eval.core.SchemaRef;
import com.xdata.partialmarking.core.MarkInfo;
import com.xdata.partialmarking.core.PartialMarkParameters;

/**
 * Port for structural partial marking. The prod adapter builds QueryStructures and
 * calls PartialMarker; tests inject a fixed MarkInfo without any schema/parsing.
 *
 * <p>Returns null if marking could not be computed (e.g. parse failure); the
 * scorer then falls back to a zero raw score for a non-equivalent answer.
 */
public interface PartialMarkingPort {

    MarkInfo computeMarks(QueryPair queries, SchemaRef schema, PartialMarkParameters params);
}
