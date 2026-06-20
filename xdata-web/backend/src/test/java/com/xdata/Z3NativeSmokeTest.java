package com.xdata;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Confirms the z3-turnkey native bindings load and solve (the gate for Path A / killing-data gen). */
class Z3NativeSmokeTest {

    @Test
    void z3_native_context_loads_and_solves() {
        try (Context ctx = new Context()) {
            Solver solver = ctx.mkSolver();
            BoolExpr x = ctx.mkBoolConst("x");
            solver.add(x);
            assertThat(solver.check()).isEqualTo(Status.SATISFIABLE);
        }
    }
}
