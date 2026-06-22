package com.xdata.eval.core;

import com.xdata.partialmarking.core.MarkInfo;

/**
 * Pure combiner: equivalence verdict + partial marking + penalty → final marks.
 * No I/O. See CONTEXT.md → "VerdictScorer".
 */
public class VerdictScorer {

    public GradingOutcome score(EquivalenceResult equivalence, MarkInfo markInfo, float penalty) {
        boolean correct = equivalence.verdict() == EquivalenceResult.Verdict.EQUIVALENT;

        float rawScore;
        if (correct) {
            rawScore = 1.0f;
        } else if (markInfo != null && markInfo.getMaxMarks() > 0) {
            rawScore = (float) (markInfo.getMarks() / markInfo.getMaxMarks());
        } else {
            rawScore = 0.0f;
        }

        float finalScore = clamp(rawScore * (1.0f - penalty));
        return new GradingOutcome(equivalence, markInfo, rawScore, penalty, finalScore, correct);
    }

    private static float clamp(float v) {
        if (v < 0.0f) return 0.0f;
        if (v > 1.0f) return 1.0f;
        return v;
    }
}
