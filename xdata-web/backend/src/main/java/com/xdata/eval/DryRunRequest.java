package com.xdata.eval;

import com.xdata.partialmarking.core.PartialMarkParameters;

/**
 * Playground input: a pattern/answer pair with optional custom weights, evaluated
 * without touching any submission row or side effect.
 */
public record DryRunRequest(String patternQuery, String studentQuery, Integer schemaId,
                            PartialMarkParameters weights) {
}
