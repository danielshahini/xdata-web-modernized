package com.xdata.eval;

import com.xdata.eval.adapter.AssignedDbStage;
import com.xdata.eval.adapter.SmtStage;
import com.xdata.eval.adapter.TestDataExecutionStage;
import com.xdata.eval.adapter.TextMatchStage;
import com.xdata.eval.core.EvaluationEngine;
import com.xdata.eval.core.EvaluationRequest;
import com.xdata.eval.core.GradingOutcome;
import com.xdata.eval.core.QueryPair;
import com.xdata.eval.core.SchemaRef;
import com.xdata.eval.core.VerdictScorer;
import com.xdata.eval.port.GradeSink;
import com.xdata.eval.port.PartialMarkingPort;
import com.xdata.eval.port.ResultNotifier;
import com.xdata.model.Assignment;
import com.xdata.model.Question;
import com.xdata.model.Submission;
import com.xdata.repository.SubmissionRepository;
import com.xdata.service.core.SubmissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Thin Spring shell of the SubmissionEvaluation deep module (see CONTEXT.md).
 * Holds the transaction/cache boundary, loads the Submission, maps it to a pure
 * {@link EvaluationRequest}, runs the {@link EvaluationEngine}, then fires the
 * side-effect ports. The equivalence chain order lives here, explicitly.
 */
@Service
@Slf4j
public class SubmissionEvaluationService implements SubmissionEvaluator {

    private final SubmissionRepository submissionRepository;
    private final SubmissionService submissionService;
    private final GradeSink gradeSink;
    private final ResultNotifier notifier;
    private final EvaluationEngine engine;

    public SubmissionEvaluationService(SubmissionRepository submissionRepository,
                                       SubmissionService submissionService,
                                       PartialMarkingPort partialMarking,
                                       GradeSink gradeSink,
                                       ResultNotifier notifier,
                                       TextMatchStage textMatch,
                                       TestDataExecutionStage testData,
                                       SmtStage smt,
                                       AssignedDbStage assignedDb) {
        this.submissionRepository = submissionRepository;
        this.submissionService = submissionService;
        this.gradeSink = gradeSink;
        this.notifier = notifier;
        // Order is behaviour: cheap/pure first, the live assigned-DB compare last.
        this.engine = new EvaluationEngine(
                List.of(textMatch, testData, smt, assignedDb),
                partialMarking, new VerdictScorer());
    }

    @Override
    @Transactional
    public GradingOutcome gradeNow(int submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        GradingOutcome outcome = engine.evaluate(toRequest(submission));

        gradeSink.save(submission, outcome);
        notifier.notifyGraded(submission);
        log.info("Submission {} graded: {} (via {}), marks={}",
                submissionId, outcome.equivalence().verdict(),
                outcome.equivalence().decidedBy(), outcome.finalScore());
        return outcome;
    }

    @Override
    public GradingOutcome dryRun(DryRunRequest request) {
        EvaluationRequest req = new EvaluationRequest(
                new QueryPair(request.patternQuery(), request.studentQuery()),
                SchemaRef.ofSchema(request.schemaId()),
                request.weights(),
                0.0f);
        return engine.evaluate(req);
    }

    private EvaluationRequest toRequest(Submission submission) {
        Question question = submission.getQuestion();
        Assignment assignment = question.getAssignment();
        Integer schemaId = assignment != null ? assignment.getDefaultSchemaId() : null;
        var connection = assignment != null ? assignment.getConnection() : null;
        float penalty = submissionService.calculatePenalty(submission);
        return new EvaluationRequest(
                new QueryPair(question.getInstructorQuery(), submission.getQuery()),
                new SchemaRef(schemaId, connection),
                question.getPartialMarkParameters(),
                penalty);
    }
}
