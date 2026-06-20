package com.xdata.eval;

import com.xdata.eval.adapter.AssignedDbStage;
import com.xdata.eval.adapter.SmtStage;
import com.xdata.eval.adapter.TestDataExecutionStage;
import com.xdata.eval.adapter.TextMatchStage;
import com.xdata.eval.core.GradingOutcome;
import com.xdata.eval.port.GradeSink;
import com.xdata.eval.port.PartialMarkingPort;
import com.xdata.eval.port.ResultNotifier;
import com.xdata.model.Assignment;
import com.xdata.model.Question;
import com.xdata.model.Submission;
import com.xdata.model.XDataUser;
import com.xdata.partialmarking.core.MarkInfo;
import com.xdata.repository.SubmissionRepository;
import com.xdata.service.DatasetGenerationService;
import com.xdata.service.SmtSolverService;
import com.xdata.service.SqlSandboxService;
import com.xdata.service.TestExecutionService;
import com.xdata.service.core.SubmissionService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubmissionEvaluationServiceTest {

    // --- recording fakes for the side-effect ports --------------------------

    static class RecordingGradeSink implements GradeSink {
        Submission savedSubmission;
        GradingOutcome savedOutcome;
        public void save(Submission s, GradingOutcome o) { savedSubmission = s; savedOutcome = o; }
    }

    static class RecordingNotifier implements ResultNotifier {
        int calls;
        public void notifyGraded(Submission s) { calls++; }
    }

    private final RecordingGradeSink sink = new RecordingGradeSink();
    private final RecordingNotifier notifier = new RecordingNotifier();
    private final SubmissionRepository submissionRepository = mock(SubmissionRepository.class);
    private final SubmissionService submissionService = mock(SubmissionService.class);

    private SubmissionEvaluationService newService(PartialMarkingPort marking) {
        // The non-text rungs are wired with mocked services; with a text match they
        // are never reached, and for the dry-run they all abstain (no stubbing needed).
        var datasetGen = mock(DatasetGenerationService.class);
        when(datasetGen.generateDatasetFromQuery(anyString(), any())).thenReturn(List.of());
        when(datasetGen.generateEquivalenceConstraints(anyString(), anyString(), any())).thenReturn(null);
        return new SubmissionEvaluationService(
                submissionRepository, submissionService, marking, sink, notifier,
                new TextMatchStage(),
                new TestDataExecutionStage(datasetGen, mock(TestExecutionService.class)),
                new SmtStage(datasetGen, mock(SmtSolverService.class)),
                new AssignedDbStage(mock(SqlSandboxService.class),
                        mock(com.xdata.service.DatabaseService.class), new com.xdata.db.QueryRunner()));
    }

    private static MarkInfo markInfo(double marks, double maxMarks) {
        MarkInfo mi = new MarkInfo();
        mi.setMarks(marks);
        mi.setMaxMarks(maxMarks);
        return mi;
    }

    private static Submission submission(String instructorQuery, String studentQuery) {
        Assignment assignment = new Assignment();
        assignment.setId(1);
        Question question = new Question();
        question.setId(1);
        question.setInstructorQuery(instructorQuery);
        question.setAssignment(assignment);
        XDataUser user = new XDataUser();
        user.setLoginId("student1");
        Submission submission = new Submission();
        submission.setId(1);
        submission.setQuestion(question);
        submission.setQuery(studentQuery);
        submission.setUser(user);
        return submission;
    }

    // --- behaviour ----------------------------------------------------------

    @Test
    void gradeNow_persists_the_outcome_and_fires_side_effects() {
        when(submissionRepository.findById(1)).thenReturn(
                Optional.of(submission("SELECT * FROM users", "SELECT * FROM users")));
        when(submissionService.calculatePenalty(any())).thenReturn(0.0f);
        var service = newService((q, s, p) -> markInfo(5, 10));

        GradingOutcome outcome = service.gradeNow(1);

        assertThat(outcome.verifiedCorrect()).isTrue();
        assertThat(outcome.finalScore()).isEqualTo(1.0f);
        assertThat(sink.savedOutcome).isSameAs(outcome);   // persisted via the sink
        assertThat(notifier.calls).isEqualTo(1);
    }

    @Test
    void dryRun_evaluates_but_touches_no_side_effect_ports() {
        var service = newService((q, s, p) -> markInfo(4, 10));

        GradingOutcome outcome = service.dryRun(
                new DryRunRequest("SELECT a FROM t", "SELECT b FROM t", 1, null));

        assertThat(outcome).isNotNull();
        assertThat(outcome.finalScore()).isEqualTo(0.4f); // scored by partial marks
        assertThat(sink.savedOutcome).isNull();
        assertThat(notifier.calls).isZero();
    }
}
