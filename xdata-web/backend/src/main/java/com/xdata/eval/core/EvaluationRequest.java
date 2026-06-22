package com.xdata.eval.core;

import com.xdata.partialmarking.core.PartialMarkParameters;

/**
 * Pure input to the decision core — mapped from a Submission by the shell, so the
 * core never touches JPA.
 *
 * @param queries instructor + student query
 * @param schema  schema id and (optional) assigned connection
 * @param params  partial-marking weights (may be null → defaults)
 * @param penalty pre-computed penalty fraction in [0,1)
 */
public record EvaluationRequest(QueryPair queries, SchemaRef schema,
                                PartialMarkParameters params, float penalty) {
}
