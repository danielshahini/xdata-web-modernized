package com.xdata.eval.adapter;

import com.xdata.eval.core.EquivalenceResult.Stage;
import com.xdata.eval.core.EquivalenceResult.Verdict;
import com.xdata.eval.core.QueryPair;
import com.xdata.eval.core.SchemaRef;
import com.xdata.service.DatasetGenerationService;
import com.xdata.service.SmtSolverService;
import com.xdata.service.SqlSandboxService;
import com.xdata.service.TestExecutionService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EquivalenceStageAdaptersTest {

    private final SchemaRef schema = SchemaRef.ofSchema(1);

    // --- TextMatchStage (pure) ---------------------------------------------

    @Test
    void textMatch_equivalent_ignoring_case_and_whitespace() {
        var result = new TextMatchStage().check(
                new QueryPair("  SELECT * FROM t ", "select * from T"), schema);
        assertThat(result.verdict()).isEqualTo(Verdict.EQUIVALENT);
        assertThat(result.decidedBy()).isEqualTo(Stage.TEXT);
    }

    @Test
    void textMatch_abstains_when_text_differs() {
        var result = new TextMatchStage().check(
                new QueryPair("SELECT a FROM t", "SELECT b FROM t"), schema);
        assertThat(result.verdict()).isEqualTo(Verdict.INCONCLUSIVE);
    }

    // --- SmtStage ----------------------------------------------------------

    @Test
    void smt_abstains_when_no_constraints_can_be_built() {
        var datasetGen = mock(DatasetGenerationService.class);
        var solver = mock(SmtSolverService.class);
        when(datasetGen.generateEquivalenceConstraints(anyString(), anyString(), any())).thenReturn(null);

        var result = new SmtStage(datasetGen, solver).check(new QueryPair("a", "b"), schema);

        assertThat(result.verdict()).isEqualTo(Verdict.INCONCLUSIVE);
    }

    @Test
    void smt_proves_equivalent_when_solver_says_so() {
        var datasetGen = mock(DatasetGenerationService.class);
        var solver = mock(SmtSolverService.class);
        when(datasetGen.generateEquivalenceConstraints(anyString(), anyString(), any())).thenReturn("(assert ...)");
        when(solver.verifyEquivalence("(assert ...)")).thenReturn(true);

        var result = new SmtStage(datasetGen, solver).check(new QueryPair("a", "b"), schema);

        assertThat(result.verdict()).isEqualTo(Verdict.EQUIVALENT);
        assertThat(result.decidedBy()).isEqualTo(Stage.SMT);
    }

    @Test
    void smt_abstains_when_solver_cannot_prove_equivalence() {
        var datasetGen = mock(DatasetGenerationService.class);
        var solver = mock(SmtSolverService.class);
        when(datasetGen.generateEquivalenceConstraints(anyString(), anyString(), any())).thenReturn("(assert ...)");
        when(solver.verifyEquivalence(anyString())).thenReturn(false);

        var result = new SmtStage(datasetGen, solver).check(new QueryPair("a", "b"), schema);

        assertThat(result.verdict()).isEqualTo(Verdict.INCONCLUSIVE);
    }

    // --- TestDataExecutionStage --------------------------------------------

    @Test
    void testData_abstains_when_no_dataset_generated() {
        var datasetGen = mock(DatasetGenerationService.class);
        var exec = mock(TestExecutionService.class);
        when(datasetGen.generateDatasetFromQuery(anyString(), any())).thenReturn(List.of());

        var result = new TestDataExecutionStage(datasetGen, exec).check(new QueryPair("a", "b"), schema);

        assertThat(result.verdict()).isEqualTo(Verdict.INCONCLUSIVE);
    }

    @Test
    void testData_is_definitive_when_dataset_exists() {
        var datasetGen = mock(DatasetGenerationService.class);
        var exec = mock(TestExecutionService.class);
        when(datasetGen.generateDatasetFromQuery(anyString(), any())).thenReturn(List.of("INSERT ..."));
        when(exec.compareQueries(anyString(), anyString(), any(), any())).thenReturn(false);

        var result = new TestDataExecutionStage(datasetGen, exec).check(new QueryPair("a", "b"), schema);

        assertThat(result.verdict()).isEqualTo(Verdict.NOT_EQUIVALENT);
        assertThat(result.decidedBy()).isEqualTo(Stage.TEST_DATA);
    }

    // --- AssignedDbStage (guard) -------------------------------------------

    @Test
    void assignedDb_abstains_when_no_connection() {
        var result = new AssignedDbStage(
                mock(SqlSandboxService.class),
                mock(com.xdata.service.DatabaseService.class),
                new com.xdata.db.QueryRunner())
                .check(new QueryPair("a", "b"), SchemaRef.ofSchema(1)); // connection == null

        assertThat(result.verdict()).isEqualTo(Verdict.INCONCLUSIVE);
        assertThat(result.decidedBy()).isEqualTo(Stage.ASSIGNED_DB);
    }
}
