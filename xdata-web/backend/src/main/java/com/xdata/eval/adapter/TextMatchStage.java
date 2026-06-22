package com.xdata.eval.adapter;

import com.xdata.eval.core.EquivalenceResult;
import com.xdata.eval.core.EquivalenceResult.Stage;
import com.xdata.eval.core.EquivalenceStageCheck;
import com.xdata.eval.core.QueryPair;
import com.xdata.eval.core.SchemaRef;
import org.springframework.stereotype.Component;

/**
 * Cheapest rung: literal text equality (trimmed, case-insensitive). Pure, no I/O.
 * Only ever proves EQUIVALENT — a textual difference is not proof of
 * non-equivalence, so it abstains (INCONCLUSIVE) and lets later rungs decide.
 */
@Component
public class TextMatchStage implements EquivalenceStageCheck {

    @Override
    public Stage stage() {
        return Stage.TEXT;
    }

    @Override
    public EquivalenceResult check(QueryPair queries, SchemaRef schema) {
        String instructor = queries.instructorQuery();
        String student = queries.studentQuery();
        if (instructor != null && student != null
                && instructor.trim().equalsIgnoreCase(student.trim())) {
            return EquivalenceResult.equivalent(Stage.TEXT);
        }
        return EquivalenceResult.inconclusive(Stage.TEXT, "queries differ textually");
    }
}
