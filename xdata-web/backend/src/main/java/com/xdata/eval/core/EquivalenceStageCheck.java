package com.xdata.eval.core;

/**
 * One rung of the equivalence chain (internal seam). The engine holds an ordered
 * list of these and the first decisive verdict wins.
 *
 * <p>Invariant: {@link #check} never throws. Any internal failure (no test data,
 * SMT unavailable, no connection) becomes an {@code INCONCLUSIVE} result so the
 * chain can advance to the next rung.
 */
public interface EquivalenceStageCheck {

    EquivalenceResult.Stage stage();

    EquivalenceResult check(QueryPair queries, SchemaRef schema);
}
