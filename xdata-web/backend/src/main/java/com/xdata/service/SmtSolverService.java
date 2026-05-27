package com.xdata.service;

import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.BoolExpr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Slf4j
public class SmtSolverService {

    private static boolean z3Available = false;

    static {
        try {
            // Versuche Z3 native Bibliothek zu laden
            System.loadLibrary("z3");
            z3Available = true;
            log.info("Native Z3 library loaded successfully.");
        } catch (UnsatisfiedLinkError e) {
            log.warn("Could not load native Z3 library from java.library.path. SMT functions might be limited.");
        }
    }

    public boolean isAvailable() {
        if (z3Available) return true;
        // Check if CLI is available
        try {
            String z3Path = "z3";
            if (new java.io.File("/usr/local/Cellar/z3/4.15.4/bin/z3").exists()) {
                z3Path = "/usr/local/Cellar/z3/4.15.4/bin/z3";
            } else if (new java.io.File("/usr/local/bin/z3").exists()) {
                z3Path = "/usr/local/bin/z3";
            } else if (new java.io.File("/opt/homebrew/bin/z3").exists()) {
                z3Path = "/opt/homebrew/bin/z3";
            }
            Process p = new ProcessBuilder(z3Path, "--version").start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean verifyEquivalence(String smtConstraints) {
        if (smtConstraints == null || smtConstraints.trim().isEmpty()) {
            log.warn("SMT constraints are empty!");
            return false;
        }
        
        log.info("Verifying equivalence using SMT constraints (length: {})...", smtConstraints.length());
        String result = solve(smtConstraints);
        log.info("Z3 Result in verifyEquivalence: {}", result);
        
        // Wenn die Negation der Äquivalenz UNSATISFIABLE ist, sind die Queries äquivalent
        boolean isEquivalent = result != null && (result.contains("unsat") || result.contains("UNSATISFIABLE"));
        log.info("Is equivalent: {}", isEquivalent);
        return isEquivalent;
    }

    public static class SmtResult {
        public final boolean success;
        public final String status;
        public final String model;
        
        public SmtResult(boolean success, String status, String model) {
            this.success = success;
            this.status = status;
            this.model = model;
        }
        
        public boolean isSat() {
            return status != null && (status.contains("sat") || status.contains("SATISFIABLE"));
        }
    }

    public SmtResult solveDetailed(String smtLib2String) {
        if (z3Available) {
            log.info("Solving SMT string using JNI...");
            try (Context ctx = new Context()) {
                Solver solver = ctx.mkSolver();
                BoolExpr[] assertions = ctx.parseSMTLIB2String(smtLib2String, null, null, null, null);
                solver.add(assertions);
                Status status = solver.check();
                log.info("SMT JNI Solver Status: {}", status);
                String model = null;
                if (status == Status.SATISFIABLE) {
                    model = solver.getModel().toString();
                }
                return new SmtResult(true, status.toString(), model);
            } catch (Exception e) {
                log.error("Failed to solve SMT using JNI: ", e);
                return new SmtResult(false, "ERROR", null);
            }
        } else {
            log.info("Falling back to Z3 CLI solver...");
            String cliResult = solveUsingCli(smtLib2String);
            if (cliResult == null || cliResult.equals("ERROR")) {
                return new SmtResult(false, "ERROR", null);
            }
            String status = cliResult.contains("unsat") ? "unsat" : (cliResult.contains("sat") ? "sat" : "unknown");
            return new SmtResult(true, status, cliResult);
        }
    }

    public String solve(String smtLib2String) {
        SmtResult res = solveDetailed(smtLib2String);
        return res.model != null ? res.model : res.status;
    }

    private String solveUsingCli(String smtLib2String) {
        try {
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("query", ".smt2");
            // Add check-sat and get-model if not present
            String fullSmt = smtLib2String;
            if (!fullSmt.contains("(check-sat)")) {
                fullSmt += "\n(check-sat)\n(get-model)\n";
            }
            java.nio.file.Files.writeString(tempFile, fullSmt);
            
            String z3Path = "z3";
            // Prüfe bekannte Pfade auf MacOS
            if (new java.io.File("/usr/local/Cellar/z3/4.15.4/bin/z3").exists()) {
                z3Path = "/usr/local/Cellar/z3/4.15.4/bin/z3";
            } else if (new java.io.File("/usr/local/bin/z3").exists()) {
                z3Path = "/usr/local/bin/z3";
            } else if (new java.io.File("/opt/homebrew/bin/z3").exists()) {
                z3Path = "/opt/homebrew/bin/z3";
            }
            
            ProcessBuilder pb = new ProcessBuilder(z3Path, "-smt2", tempFile.toString());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            String result = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            
            log.info("Z3 CLI exit code: {}, Result: {}", exitCode, result);
            java.nio.file.Files.deleteIfExists(tempFile);
            return result;
        } catch (Exception e) {
            log.error("Failed to solve SMT using CLI: ", e);
            return "ERROR";
        }
    }
}
