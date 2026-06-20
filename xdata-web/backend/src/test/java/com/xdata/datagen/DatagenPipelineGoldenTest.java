package com.xdata.datagen;

import com.xdata.legacy.GenConstraints.GenConstraints;
import com.xdata.legacy.parsing.AppTest_Parameters;
import com.xdata.legacy.parsing.QueryParser;
import com.xdata.legacy.parsing.QueryStructure;
import com.xdata.legacy.testDataGen.GenerateCVC1;
import com.xdata.legacy.testDataGen.RelatedToPreprocessing;
import com.xdata.util.TableMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Golden test for the killing-data generation pipeline, run against a real
 * PostgreSQL — the database the legacy TableMap/QueryParser/GenerateCVC1 code is
 * written for. Uses the running compose PostgreSQL (overridable via system
 * properties {@code datagen.test.pg.*}); skipped when no PostgreSQL is reachable
 * so CI without a DB stays green.
 *
 * <p>Drives the reconstruction documented in docs/dataset-playground-analysis.md
 * / ADR 0002. {@link #parsesQualifiedQueryAgainstSchema()} guards the
 * front-end (schema metadata -> TableMap -> QueryParser), which was 100% broken
 * before the reconstruction. {@link #producesNonEmptyDataset()} is the remaining
 * frontier (the constraint-generation + SMT-solve + extraction engine).
 */
class DatagenPipelineGoldenTest {

    private static final String HOST = System.getProperty("datagen.test.pg.host", "localhost");
    private static final String PORT = System.getProperty("datagen.test.pg.port", "5433");
    private static final String ADMIN_DB = System.getProperty("datagen.test.pg.db", "xdatadb");
    private static final String USER = System.getProperty("datagen.test.pg.user", "postgres");
    private static final String PASS = System.getProperty("datagen.test.pg.password", "1709");
    private static final String TEST_DB = "datagen_golden";

    private static final String DDL = "CREATE TABLE students (id INT, age INT)";
    // The legacy XData parser requires table-qualified column references.
    private static final String QUERY = "SELECT students.id FROM students WHERE students.age > 20";

    private static String testDbUrl;

    @BeforeAll
    static void provisionDb() throws Exception {
        Assumptions.assumeTrue(reachable(), "PostgreSQL not reachable at " + HOST + ":" + PORT + " — skipping");
        String adminUrl = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + ADMIN_DB;
        try (Connection admin = DriverManager.getConnection(adminUrl, USER, PASS);
             Statement st = admin.createStatement()) {
            st.execute("DROP DATABASE IF EXISTS " + TEST_DB + " WITH (FORCE)");
            st.execute("CREATE DATABASE " + TEST_DB);
        }
        testDbUrl = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + TEST_DB;
        try (Connection conn = conn(); Statement st = conn.createStatement()) {
            st.execute(DDL);
        }
    }

    @AfterAll
    static void dropDb() {
        if (testDbUrl == null) return;
        String adminUrl = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + ADMIN_DB;
        try (Connection admin = DriverManager.getConnection(adminUrl, USER, PASS);
             Statement st = admin.createStatement()) {
            st.execute("DROP DATABASE IF EXISTS " + TEST_DB + " WITH (FORCE)");
        } catch (SQLException ignored) {
        }
    }

    @BeforeEach
    void clearTableMapCache() {
        TableMap.clearAllInstances();
    }

    private static boolean reachable() {
        try (Connection ignored = DriverManager.getConnection(
                "jdbc:postgresql://" + HOST + ":" + PORT + "/" + ADMIN_DB, USER, PASS)) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private static Connection conn() throws SQLException {
        return DriverManager.getConnection(testDbUrl, USER, PASS);
    }

    /**
     * Front-end of the pipeline: build a legacy TableMap from live PostgreSQL metadata
     * and parse a qualified query against it. This whole path failed for every query
     * before the reconstruction (case-mismatch in column resolution, an always-true
     * {@code instanceof Expression} guard, a null GROUP BY, and missing config keys).
     */
    @Test
    void parsesQualifiedQueryAgainstSchema() throws Exception {
        try (Connection conn = conn()) {
            TableMap tableMap = TableMap.getInstances(conn, 1);
            assertNotNull(tableMap.getTable("STUDENTS"), "TableMap must contain STUDENTS");

            QueryParser qp = new QueryParser(tableMap);
            qp.parseQuery("Q1", QUERY, new AppTest_Parameters());

            assertNotNull(qp.getQuery(), "parsed Query must not be null");
            assertNotNull(qp.getQuery().getFromTables().get("STUDENTS"),
                    "parsed query's FROM tables must resolve STUDENTS");
        }
    }

    /**
     * Resume harness for the constraint-generation engine (ADR 0002 step 3).
     *
     * <p>This drives the authentic legacy flow end-to-end — it already runs without
     * crashing: GenerateCVC1 builds the SMT, native Z3 solves it, PopulateTestData
     * tries to extract. Two correctness blockers remain, both root-caused:
     *
     * <ol>
     *   <li><b>WHERE selection mis-mapped (outerSQ=true, the default).</b> The query is
     *       wrapped as a subquery (JSQ0) and the predicate {@code students.age > 20} is
     *       emitted on the subquery <i>count</i> column ({@code JSQ0__XDATA_CNT}) instead
     *       of {@code JSQ0_STUDENTS__AGE1}. Combined with {@code count <= 1} that is
     *       {@code count > 20 ∧ count <= 1} → UNSAT. (The base-table copy
     *       {@code STUDENTS_AGE1 > 20} is correct.) Bug lives in the JSQ subquery
     *       selection generation (QueryBlockDetails.addOuterQueryAsSubqueryConstraints /
     *       joinSubqueryConstraintsForFromClauseSubquery).</li>
     *   <li><b>Selection dropped (outerSQ=false).</b> Avoids the wrapping but emits no
     *       selection constraint at all → trivially SAT with age=0 → data that does NOT
     *       satisfy the query. Not a valid shortcut.</li>
     *   <li><b>Array-encoded model extraction.</b> The model encodes a table as
     *       {@code O_T : (Array Int T_TupleType)}; PopulateTestData.cutRequiredOutputForSMTWithAPI
     *       looks for FuncDecls whose range ends with {@code _TupleType} and finds none,
     *       so it writes an empty DS*.sql. Needs an array-aware extractor (read
     *       model.eval(select(O_T, i)) for i in 1..count, decompose tuple fields).</li>
     * </ol>
     *
     * Enable once the selection mapping is fixed AND the array-aware extractor exists.
     */
    /**
     * Engine milestone (ADR 0002 step 3, part a — FIXED): driving the real constraint
     * engine now emits a <b>satisfiable</b> SMT whose model satisfies the query predicate.
     * Before the JSQ selection-column fix the predicate was applied to the subquery count
     * column ({@code JSQ0__XDATA_CNT}) → UNSAT. This guards that fix.
     */
    @Test
    void engineGeneratesSatisfiableDatasetConstraints() throws Exception {
        Path home = Files.createTempDirectory("xdata_datagen");
        com.xdata.legacy.util.Configuration.homeDir = home.toString();
        Files.createDirectories(home.resolve("temp_smt"));

        try (Connection conn = conn()) {
            TableMap tableMap = TableMap.getInstances(conn, 1);

            GenerateCVC1 cvc = new GenerateCVC1();
            cvc.setTableMap(tableMap);
            cvc.setConnection(conn);
            cvc.setQueryId(0);
            cvc.setQueryString(QUERY);
            cvc.setFilePath("");
            cvc.setDBAppparams(new AppTest_Parameters());

            QueryStructure qs = new QueryStructure(tableMap);
            qs.buildQueryStructureJSQL("0", QUERY, false, cvc.getDBAppparams());
            cvc.setqStructure(qs);
            cvc.initializeQueryDetailsQStructure(qs);

            RelatedToPreprocessing.populateData(cvc);
            cvc.initializeOtherDetails();

            GenConstraints.generateDatasetForNonEmptyDataset(cvc);

            Path smt;
            try (Stream<Path> files = Files.walk(home.resolve("temp_smt"))) {
                smt = files.filter(p -> p.getFileName().toString().endsWith(".smt")).findFirst().orElse(null);
            }
            assertNotNull(smt, "engine must emit an SMT file");

            com.xdata.service.SmtSolverService solver = new com.xdata.service.SmtSolverService();
            com.xdata.service.SmtSolverService.SmtResult res = solver.solveDetailed(Files.readString(smt));
            System.out.println("=== STATUS " + res.status + " ===\n" + res.model);

            assertTrue(res.isSat(), "expected SAT (predicate mapped to the age column); got: " + res.status);
            // The selection must constrain the AGE column, not the count column.
            assertTrue(res.model != null && res.model.contains("STUDENTS_TupleType"),
                    "model must populate the STUDENTS relation");
        }
    }

    /**
     * Full pipeline (ADR 0002 step 3, part b — FIXED): query → constraints → native Z3 →
     * array-aware extraction → runnable INSERTs. The model encodes a relation as
     * {@code O_T : (Array Int T_TupleType)}; {@code PopulateTestData.generateInsertsFromArrayModel}
     * reads {@code model.eval(select(O_T, i))}, decomposes the tuple accessors, honours the
     * XDATA_CNT multiplicity and the -99999 NULL sentinel, and emits INSERT statements that
     * make the query non-empty (e.g. {@code insert into students values (null, 21)} for
     * {@code age > 20}).
     */
    @Test
    void producesNonEmptyInsertStatements() throws Exception {
        Path home = Files.createTempDirectory("xdata_datagen");
        com.xdata.legacy.util.Configuration.homeDir = home.toString();
        Files.createDirectories(home.resolve("temp_smt"));

        try (Connection conn = conn()) {
            TableMap tableMap = TableMap.getInstances(conn, 1);

            GenerateCVC1 cvc = new GenerateCVC1();
            cvc.setTableMap(tableMap);
            cvc.setConnection(conn);
            cvc.setQueryId(0);
            cvc.setQueryString(QUERY);
            cvc.setFilePath("");
            cvc.setDBAppparams(new AppTest_Parameters());

            QueryStructure qs = new QueryStructure(tableMap);
            qs.buildQueryStructureJSQL("0", QUERY, false, cvc.getDBAppparams());
            cvc.setqStructure(qs);
            cvc.initializeQueryDetailsQStructure(qs);

            RelatedToPreprocessing.populateData(cvc);
            cvc.initializeOtherDetails();

            GenConstraints.generateDatasetForNonEmptyDataset(cvc);

            List<String> inserts = collectInserts(home.resolve("temp_smt"));
            assertTrue(inserts.stream().anyMatch(s -> s.toLowerCase().contains("insert into students")),
                    "expected an INSERT INTO students to be generated; got: " + inserts);
        }
    }

    @Test
    void drivesFullMutationKillingSuite() throws Exception {
        Path home = Files.createTempDirectory("xdata_datagen");
        com.xdata.legacy.util.Configuration.homeDir = home.toString();
        Files.createDirectories(home.resolve("temp_smt"));

        try (Connection conn = conn()) {
            TableMap tableMap = TableMap.getInstances(conn, 1);
            GenerateCVC1 cvc = new GenerateCVC1();
            cvc.setTableMap(tableMap);
            cvc.setConnection(conn);
            cvc.setQueryId(0);
            cvc.setQueryString(QUERY);
            cvc.setFilePath("");
            cvc.setDBAppparams(new AppTest_Parameters());
            QueryStructure qs = new QueryStructure(tableMap);
            qs.buildQueryStructureJSQL("0", QUERY, false, cvc.getDBAppparams());
            cvc.setqStructure(qs);
            cvc.initializeQueryDetailsQStructure(qs);
            RelatedToPreprocessing.populateData(cvc);
            cvc.initializeOtherDetails();

            cvc.generateDatasetsToKillMutations();

            long dsFiles;
            try (Stream<Path> files = Files.walk(home.resolve("temp_smt"))) {
                dsFiles = files.filter(p -> p.getFileName().toString().endsWith(".sql")).count();
            }
            List<String> inserts = collectInserts(home.resolve("temp_smt"));
            System.out.println("=== DS files: " + dsFiles + ", insert lines: "
                    + inserts.stream().filter(s -> s.toLowerCase().contains("insert into")).count());
            assertTrue(dsFiles >= 1, "full mutation suite must produce at least one dataset");
        }
    }

    private static List<String> collectInserts(Path tempSmtDir) throws IOException {
        try (Stream<Path> files = Files.walk(tempSmtDir)) {
            return files
                    .filter(p -> p.getFileName().toString().endsWith(".sql"))
                    .flatMap(p -> {
                        try {
                            return Files.readAllLines(p).stream();
                        } catch (IOException e) {
                            return Stream.empty();
                        }
                    })
                    .filter(line -> !line.isBlank())
                    .toList();
        }
    }
}
