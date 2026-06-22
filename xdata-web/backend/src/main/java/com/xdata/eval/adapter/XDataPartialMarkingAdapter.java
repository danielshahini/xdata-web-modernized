package com.xdata.eval.adapter;

import com.xdata.eval.core.QueryPair;
import com.xdata.eval.core.SchemaRef;
import com.xdata.eval.port.PartialMarkingPort;
import com.xdata.partialmarking.core.CanonicalizeQuery;
import com.xdata.partialmarking.core.MarkInfo;
import com.xdata.partialmarking.core.PartialMarkParameters;
import com.xdata.partialmarking.core.PartialMarker;
import com.xdata.partialmarking.core.QueryStructure;
import com.xdata.partialmarking.core.TableMap;
import com.xdata.service.MetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Prod adapter for {@link PartialMarkingPort}: builds canonicalized QueryStructures
 * and runs the XData PartialMarker. The single place this construction lives —
 * shared by real grading and the playground dry-run (kills the former duplication
 * between {@code calculateXDataPartialMarks} and {@code calculatePartialMarksForPlayground}).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class XDataPartialMarkingAdapter implements PartialMarkingPort {

    private final MetadataService metadataService;

    @Override
    public MarkInfo computeMarks(QueryPair queries, SchemaRef schema, PartialMarkParameters params) {
        try {
            TableMap tableMap = metadataService.getNewTableMap(schema.schemaId());
            QueryStructure instructorQS = new QueryStructure(queries.instructorQuery(), tableMap);
            QueryStructure studentQS = new QueryStructure(queries.studentQuery(), tableMap);
            CanonicalizeQuery.canonicalize(instructorQS);
            CanonicalizeQuery.canonicalize(studentQS);
            return PartialMarker.getMarks(instructorQS, studentQS,
                    params != null ? params : new PartialMarkParameters());
        } catch (Exception e) {
            log.error("Partial marking calculation failed: {}", e.getMessage());
            return null;
        }
    }
}
