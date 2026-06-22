package com.xdata.eval.core;

import com.xdata.eval.core.EquivalenceResult.Stage;
import com.xdata.eval.port.PartialMarkingPort;
import com.xdata.partialmarking.core.MarkInfo;
import com.xdata.partialmarking.core.PartialMarkParameters;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EvaluationEngineTest {

    // --- test doubles -------------------------------------------------------

    /** A rung that always returns a fixed result — lets us assemble any chain. */
    private static EquivalenceStageCheck rung(Stage stage, EquivalenceResult result) {
        return new EquivalenceStageCheck() {
            public Stage stage() { return stage; }
            public EquivalenceResult check(QueryPair q, SchemaRef s) { return result; }
        };
    }

    private static PartialMarkingPort marksReturning(MarkInfo info) {
        return (q, s, p) -> info;
    }

    private static MarkInfo markInfo(double marks, double maxMarks) {
        MarkInfo mi = new MarkInfo();
        mi.setMarks(marks);
        mi.setMaxMarks(maxMarks);
        return mi;
    }

    private static EvaluationRequest request() {
        return new EvaluationRequest(
                new QueryPair("SELECT a FROM t", "SELECT a FROM t"),
                SchemaRef.ofSchema(1),
                new PartialMarkParameters(),
                0.0f);
    }

    // --- behaviour ----------------------------------------------------------

    @Test
    void first_rung_equivalent_grades_correct_with_full_marks() {
        EvaluationEngine engine = new EvaluationEngine(
                List.of(rung(Stage.TEXT, EquivalenceResult.equivalent(Stage.TEXT))),
                marksReturning(markInfo(5, 10)),
                new VerdictScorer());

        GradingOutcome outcome = engine.evaluate(request());

        assertThat(outcome.verifiedCorrect()).isTrue();
        assertThat(outcome.finalScore()).isEqualTo(1.0f);
        assertThat(outcome.equivalence().verdict()).isEqualTo(EquivalenceResult.Verdict.EQUIVALENT);
        assertThat(outcome.equivalence().decidedBy()).isEqualTo(Stage.TEXT);
    }

    @Test
    void chain_falls_through_inconclusive_rungs_to_the_first_decisive_one() {
        EvaluationEngine engine = new EvaluationEngine(
                List.of(
                        rung(Stage.TEXT, EquivalenceResult.inconclusive(Stage.TEXT, "differs")),
                        rung(Stage.TEST_DATA, EquivalenceResult.inconclusive(Stage.TEST_DATA, "no data")),
                        rung(Stage.SMT, EquivalenceResult.equivalent(Stage.SMT))),
                marksReturning(markInfo(5, 10)),
                new VerdictScorer());

        GradingOutcome outcome = engine.evaluate(request());

        assertThat(outcome.verifiedCorrect()).isTrue();
        assertThat(outcome.equivalence().decidedBy()).isEqualTo(Stage.SMT);
    }

    @Test
    void all_rungs_inconclusive_yields_INCONCLUSIVE_scored_by_partial_marks_not_zero() {
        EvaluationEngine engine = new EvaluationEngine(
                List.of(
                        rung(Stage.TEXT, EquivalenceResult.inconclusive(Stage.TEXT, "differs")),
                        rung(Stage.SMT, EquivalenceResult.inconclusive(Stage.SMT, "z3 unavailable")),
                        rung(Stage.ASSIGNED_DB, EquivalenceResult.inconclusive(Stage.ASSIGNED_DB, "no connection"))),
                marksReturning(markInfo(6, 10)),
                new VerdictScorer());

        GradingOutcome outcome = engine.evaluate(request());

        assertThat(outcome.verifiedCorrect()).isFalse();
        assertThat(outcome.equivalence().verdict()).isEqualTo(EquivalenceResult.Verdict.INCONCLUSIVE);
        assertThat(outcome.equivalence().decidedBy()).isEqualTo(Stage.NONE);
        assertThat(outcome.finalScore()).isEqualTo(0.6f); // 6/10, not a silent 0
    }

    @Test
    void not_equivalent_stops_the_chain_and_scores_from_partial_marks() {
        EvaluationEngine engine = new EvaluationEngine(
                List.of(
                        rung(Stage.TEST_DATA, EquivalenceResult.notEquivalent(Stage.TEST_DATA)),
                        rung(Stage.ASSIGNED_DB, EquivalenceResult.equivalent(Stage.ASSIGNED_DB))), // must NOT be reached
                marksReturning(markInfo(3, 10)),
                new VerdictScorer());

        GradingOutcome outcome = engine.evaluate(request());

        assertThat(outcome.verifiedCorrect()).isFalse();
        assertThat(outcome.equivalence().decidedBy()).isEqualTo(Stage.TEST_DATA);
        assertThat(outcome.finalScore()).isEqualTo(0.3f);
    }

    @Test
    void penalty_is_applied_to_the_raw_score() {
        EvaluationEngine engine = new EvaluationEngine(
                List.of(rung(Stage.TEXT, EquivalenceResult.equivalent(Stage.TEXT))),
                marksReturning(markInfo(0, 10)),
                new VerdictScorer());

        EvaluationRequest withPenalty = new EvaluationRequest(
                new QueryPair("q", "q"), SchemaRef.ofSchema(1), new PartialMarkParameters(), 0.25f);

        GradingOutcome outcome = engine.evaluate(withPenalty);

        assertThat(outcome.rawScore()).isEqualTo(1.0f);
        assertThat(outcome.finalScore()).isEqualTo(0.75f); // 1.0 * (1 - 0.25)
    }

    @Test
    void not_equivalent_with_no_mark_info_scores_zero() {
        EvaluationEngine engine = new EvaluationEngine(
                List.of(rung(Stage.TEST_DATA, EquivalenceResult.notEquivalent(Stage.TEST_DATA))),
                marksReturning(null),
                new VerdictScorer());

        GradingOutcome outcome = engine.evaluate(request());

        assertThat(outcome.verifiedCorrect()).isFalse();
        assertThat(outcome.finalScore()).isEqualTo(0.0f);
    }
}
