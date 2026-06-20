package com.xdata.eval.adapter;

import com.xdata.eval.core.EquivalenceResult;
import com.xdata.eval.core.EquivalenceResult.Stage;
import com.xdata.eval.core.EquivalenceStageCheck;
import com.xdata.eval.core.QueryPair;
import com.xdata.eval.core.SchemaRef;
import com.xdata.service.DatasetGenerationService;
import com.xdata.service.SmtSolverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Proves equivalence via the SMT solver. Only ever proves EQUIVALENT: if the
 * solver cannot show it (no constraints, SAT, unknown, or unavailable) the rung
 * abstains (INCONCLUSIVE) so the assigned-DB rung gets the last word — matching
 * the legacy fallthrough where a non-unsat SMT result dropped through to the DB
 * compare.
 */
@Component
@RequiredArgsConstructor
public class SmtStage implements EquivalenceStageCheck {

    private final DatasetGenerationService datasetGenerationService;
    private final SmtSolverService smtSolverService;

    @Override
    public Stage stage() {
        return Stage.SMT;
    }

    @Override
    public EquivalenceResult check(QueryPair queries, SchemaRef schema) {
        try {
            String constraints = datasetGenerationService.generateEquivalenceConstraints(
                    queries.instructorQuery(), queries.studentQuery(), schema.schemaId());
            if (constraints == null) {
                return EquivalenceResult.inconclusive(Stage.SMT, "no equivalence constraints");
            }
            return smtSolverService.verifyEquivalence(constraints)
                    ? EquivalenceResult.equivalent(Stage.SMT)
                    : EquivalenceResult.inconclusive(Stage.SMT, "SMT did not prove equivalence");
        } catch (Exception e) {
            return EquivalenceResult.inconclusive(Stage.SMT, e.getMessage());
        }
    }
}
