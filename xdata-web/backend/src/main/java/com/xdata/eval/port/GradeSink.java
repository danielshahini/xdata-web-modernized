package com.xdata.eval.port;

import com.xdata.eval.core.GradingOutcome;
import com.xdata.model.Submission;

/**
 * Port for persisting a grading result onto a Submission (marks, verifiedCorrect,
 * markInfo JSON, evaluated flag) and awarding XP. The prod adapter writes through
 * the repositories; tests use a recording fake.
 */
public interface GradeSink {

    void save(Submission submission, GradingOutcome outcome);
}
