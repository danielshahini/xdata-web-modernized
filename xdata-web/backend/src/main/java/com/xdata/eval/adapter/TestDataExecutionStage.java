package com.xdata.eval.adapter;

import com.xdata.eval.core.EquivalenceResult;
import com.xdata.eval.core.EquivalenceResult.Stage;
import com.xdata.eval.core.EquivalenceStageCheck;
import com.xdata.eval.core.QueryPair;
import com.xdata.eval.core.SchemaRef;
import com.xdata.service.DatasetGenerationService;
import com.xdata.service.TestExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Generates a killing dataset for the instructor query, then executes both queries
 * on it and compares results. Definitive when data exists; abstains
 * (INCONCLUSIVE) when no dataset could be generated.
 */
@Component
@RequiredArgsConstructor
public class TestDataExecutionStage implements EquivalenceStageCheck {

    private final DatasetGenerationService datasetGenerationService;
    private final TestExecutionService testExecutionService;

    @Override
    public Stage stage() {
        return Stage.TEST_DATA;
    }

    @Override
    public EquivalenceResult check(QueryPair queries, SchemaRef schema) {
        try {
            List<String> testData = datasetGenerationService
                    .generateDatasetFromQuery(queries.instructorQuery(), schema.schemaId());
            if (testData == null || testData.isEmpty()) {
                return EquivalenceResult.inconclusive(Stage.TEST_DATA, "no test data generated");
            }
            boolean equal = testExecutionService.compareQueries(
                    queries.instructorQuery(), queries.studentQuery(), testData, schema.schemaId());
            return equal ? EquivalenceResult.equivalent(Stage.TEST_DATA)
                    : EquivalenceResult.notEquivalent(Stage.TEST_DATA);
        } catch (Exception e) {
            return EquivalenceResult.inconclusive(Stage.TEST_DATA, e.getMessage());
        }
    }
}
