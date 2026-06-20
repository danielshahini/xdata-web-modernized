package com.xdata.eval.core;

import com.xdata.eval.port.PartialMarkingPort;
import com.xdata.partialmarking.core.MarkInfo;

import java.util.List;

/**
 * Pure decision core of the SubmissionEvaluation module. Sequences the ordered
 * equivalence rungs (first decisive verdict wins), computes partial marking, and
 * scores. No Spring, no JPA, no I/O of its own — every dependency is injected.
 * See CONTEXT.md → "SubmissionEvaluation".
 */
public class EvaluationEngine {

    private final List<EquivalenceStageCheck> chain;
    private final PartialMarkingPort marking;
    private final VerdictScorer scorer;

    public EvaluationEngine(List<EquivalenceStageCheck> chain, PartialMarkingPort marking,
                            VerdictScorer scorer) {
        this.chain = chain;
        this.marking = marking;
        this.scorer = scorer;
    }

    public GradingOutcome evaluate(EvaluationRequest request) {
        EquivalenceResult verdict = decideEquivalence(request.queries(), request.schema());
        MarkInfo markInfo = marking.computeMarks(request.queries(), request.schema(), request.params());
        return scorer.score(verdict, markInfo, request.penalty());
    }

    /** Run the rungs in order; the first decisive verdict wins. */
    private EquivalenceResult decideEquivalence(QueryPair queries, SchemaRef schema) {
        for (EquivalenceStageCheck rung : chain) {
            EquivalenceResult result = rung.check(queries, schema);
            if (result.isDecisive()) {
                return result;
            }
        }
        return EquivalenceResult.inconclusive(EquivalenceResult.Stage.NONE,
                "no rung could decide equivalence");
    }
}
