package com.xdata.eval.core;

import com.xdata.partialmarking.core.MarkInfo;

/**
 * Immutable result of evaluating one submission. Same shape for the persist path
 * ({@code gradeNow}) and the playground dry-run.
 *
 * @param equivalence    verdict + which rung decided it
 * @param markInfo       partial-marking breakdown (null if marking failed)
 * @param rawScore       score in [0,1] before penalty
 * @param penalty        penalty fraction applied
 * @param finalScore     rawScore * (1 - penalty), clamped to [0,1]
 * @param verifiedCorrect equivalence == EQUIVALENT
 */
public record GradingOutcome(EquivalenceResult equivalence, MarkInfo markInfo,
                             float rawScore, float penalty, float finalScore,
                             boolean verifiedCorrect) {
}
