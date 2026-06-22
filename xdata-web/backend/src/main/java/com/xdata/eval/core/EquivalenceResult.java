package com.xdata.eval.core;

/**
 * Outcome of one equivalence rung (or of the whole chain).
 *
 * <p>The verdict is three-state: {@code INCONCLUSIVE} means "this rung could not
 * decide" — it must never collapse to {@code NOT_EQUIVALENT}, because doing so
 * silently zeroes a correct answer when infrastructure (test data, SMT, the
 * assigned DB) is missing. See CONTEXT.md → "Equivalence verdict".
 */
public record EquivalenceResult(Verdict verdict, Stage decidedBy, String detail) {

    public enum Verdict { EQUIVALENT, NOT_EQUIVALENT, INCONCLUSIVE }

    /** Which rung produced the verdict. {@code NONE} = no rung decided. */
    public enum Stage { TEXT, TEST_DATA, SMT, ASSIGNED_DB, NONE }

    public static EquivalenceResult equivalent(Stage stage) {
        return new EquivalenceResult(Verdict.EQUIVALENT, stage, null);
    }

    public static EquivalenceResult notEquivalent(Stage stage) {
        return new EquivalenceResult(Verdict.NOT_EQUIVALENT, stage, null);
    }

    /** This rung abstains; the chain advances to the next rung. */
    public static EquivalenceResult inconclusive(Stage stage, String why) {
        return new EquivalenceResult(Verdict.INCONCLUSIVE, stage, why);
    }

    /** A definite answer (EQUIVALENT or NOT_EQUIVALENT) ends the chain. */
    public boolean isDecisive() {
        return verdict != Verdict.INCONCLUSIVE;
    }
}
