package com.xdata.eval;

import com.xdata.eval.core.GradingOutcome;

/**
 * External interface of the SubmissionEvaluation module (see CONTEXT.md).
 *
 * <ul>
 *   <li>{@link #gradeNow} — synchronous full grading of a stored submission:
 *       equivalence verdict → partial marking → final marks, then persist, XP and
 *       notify. Async fire-and-forget is provided by
 *       {@code EvaluationService.evaluateSubmissionAsync}, which delegates here.</li>
 *   <li>{@link #dryRun} — playground evaluation with custom weights and NO side
 *       effects (no persistence, XP or notify).</li>
 * </ul>
 */
public interface SubmissionEvaluator {

    GradingOutcome gradeNow(int submissionId);

    GradingOutcome dryRun(DryRunRequest request);
}
