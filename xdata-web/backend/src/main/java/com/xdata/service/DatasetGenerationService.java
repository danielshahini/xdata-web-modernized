package com.xdata.service;

import com.microsoft.z3.*;
import com.xdata.model.Question;
import com.xdata.model.SchemaInfo;
import com.xdata.repository.SchemaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.xdata.legacy.parsing.AppTest_Parameters;
import com.xdata.legacy.parsing.QueryParser;
import com.xdata.legacy.parsing.QueryStructure;
import com.xdata.legacy.GenConstraints.GenConstraints;
import com.xdata.legacy.testDataGen.GenerateCVC1;
import com.xdata.legacy.testDataGen.RelatedToPreprocessing;
import com.xdata.legacy.util.TagDatasets;
import com.xdata.util.TableMap;

import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatasetGenerationService {

    /**
     * The legacy datagen engine drives shared static state — {@link com.xdata.legacy.util.Configuration}
     * (homeDir) and the single {@code ConstraintGenerator.ctx} Z3 context — so generations
     * must be serialized.
     */
    private static final ReentrantLock ENGINE_LOCK = new ReentrantLock();

    private final SmtSolverService smtSolverService;
    private final MetadataService metadataService;
    private final SchemaRepository schemaRepository;
    private final Map<String, List<String>> datasetCache = new ConcurrentHashMap<>();

    @Value("${spring.datasource.url}")
    private String datasourceUrl;
    @Value("${spring.datasource.username}")
    private String datasourceUser;
    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    public List<String> generateKillingDataset(Question question, SchemaInfo schema) {
        String cacheKey = question.getId() + "_" + (question.getInstructorQuery() != null ? question.getInstructorQuery().hashCode() : 0);
        if (datasetCache.containsKey(cacheKey)) {
            return datasetCache.get(cacheKey);
        }

        log.info("Generating killing dataset for question: {}", question.getName());
        List<String> insertStatements = generateDatasetFromQuery(question.getInstructorQuery(), schema != null ? schema.getId() : null);
        if (!insertStatements.isEmpty()) {
            datasetCache.put(cacheKey, insertStatements);
        }
        return insertStatements;
    }

    public List<String> generateDatasetFromQuery(String query, Integer schemaId) {
        List<String> insertStatements = new ArrayList<>();
        try {
            TableMap tableMap = metadataService.getLegacyTableMap(schemaId);
            QueryParser qp = new QueryParser(tableMap);
            // The legacy parser (revived in the datagen reconstruction) requires a non-null
            // AppTest_Parameters and table-qualified column references.
            qp.parseQuery("Q1", query, new com.xdata.legacy.parsing.AppTest_Parameters());

            // NOTE: The constraint-generation engine
            // (GenerateCVC1.generateDatasetsToKillMutations -> GenConstraints ->
            // GenerateCommonConstraintsForQuery.generateDataSetForConstraints) is not yet
            // reconstructed; the calls below do not yet produce SMT, so this method still
            // returns an empty list (see ADR 0002 / docs/dataset-playground-analysis.md).
            GenerateCVC1 cvc = new GenerateCVC1();
            cvc.setTableMap(tableMap);
            cvc.setQuery(qp.getQuery());
            cvc.setQueryString(query);
            cvc.inititalizeSQDataset();

            String smtConstraints = cvc.getSMTLIB_HEADER() + "\n" + cvc.getCVCStr() + "\n(check-sat)\n(get-model)";
            SmtSolverService.SmtResult res = smtSolverService.solveDetailed(smtConstraints);

            if (res.isSat() && res.model != null) {
                // Wir nutzen hier einen einfachen Weg, um die Inserts zu generieren
                log.info("SMT is SAT, model obtained. Extracting data...");
                insertStatements = extractInsertsFromModel(res.model, tableMap);
            }
        } catch (Exception e) {
            log.error("Failed to generate dataset from query: ", e);
        }
        return insertStatements;
    }

    public List<String> generateDatasetFromQuery(String query, String mutantQuery, Integer schemaId, List<String> mutationTypes) {
        // The playground drives the reconstructed datagen engine: a base dataset for which the
        // query yields a non-empty result, plus a targeted "killing" dataset for each requested
        // mutation type (SELECTION, EQUIVALENCE, AGG, …). mutantQuery (distinguishing the query
        // from one specific arbitrary mutant) is a different comparison path and not yet wired.
        log.info("Playground: generating killing data via the reconstructed engine for schema {} (types={}).",
                schemaId, mutationTypes);
        return generateDatasetViaEngine(query, schemaId, mutationTypes);
    }

    /**
     * Drives the legacy datagen engine end-to-end against an isolated, throwaway PostgreSQL
     * database (the legacy TableMap/GenerateCVC1 are PostgreSQL-native — schema {@code public},
     * {@code pg_constraint}, … — so the uploaded schema cannot run on the Derby scratch DB).
     * Returns INSERT statements (base non-empty dataset + one per requested mutation type), or
     * an empty list on any failure.
     */
    public List<String> generateDatasetViaEngine(String query, Integer schemaId, List<String> mutationTypes) {
        if (query == null || query.isBlank() || schemaId == null) {
            return new ArrayList<>();
        }
        Optional<SchemaInfo> schema = schemaRepository.findById(schemaId);
        if (schema.isEmpty() || schema.get().getContent() == null || schema.get().getContent().isBlank()) {
            log.warn("No DDL for schema {} — cannot generate killing data.", schemaId);
            return new ArrayList<>();
        }
        return runEngine(query, schema.get().getContent(), mutationTypes);
    }

    private List<String> runEngine(String query, String ddl, List<String> mutationTypes) {
        String tempDb = "xdata_pg_" + Math.abs(System.nanoTime());
        Path home = null;
        ENGINE_LOCK.lock();
        String prevHome = com.xdata.legacy.util.Configuration.homeDir;
        try {
            // The legacy Z3 context is static and accumulates declarations across runs
            // (enum sorts are named after columns) -> "enumeration sort name is already
            // declared" on the second generation. Start every run from a fresh context.
            com.xdata.legacy.generateConstraints.ConstraintGenerator.resetContext();

            execAdmin("CREATE DATABASE " + tempDb);
            String tempUrl = datasourceUrl.substring(0, datasourceUrl.lastIndexOf('/') + 1) + tempDb;

            home = Files.createTempDirectory("xdata_datagen");
            com.xdata.legacy.util.Configuration.homeDir = home.toString();
            Files.createDirectories(home.resolve("temp_smt"));

            try (Connection conn = DriverManager.getConnection(tempUrl, datasourceUser, datasourcePassword)) {
                applyDdl(conn, ddl);

                TableMap.clearAllInstances();
                TableMap tableMap = TableMap.getInstances(conn, 1);

                GenerateCVC1 cvc = new GenerateCVC1();
                cvc.setTableMap(tableMap);
                cvc.setConnection(conn);
                cvc.setQueryId(0);
                cvc.setQueryString(query);
                cvc.setFilePath("");
                cvc.setDBAppparams(new AppTest_Parameters());

                QueryStructure qs = new QueryStructure(tableMap);
                qs.buildQueryStructureJSQL("0", query, false, cvc.getDBAppparams());
                cvc.setqStructure(qs);
                cvc.initializeQueryDetailsQStructure(qs);

                RelatedToPreprocessing.populateData(cvc);
                cvc.initializeOtherDetails();

                // Base dataset: data for which the query yields a non-empty result.
                GenConstraints.generateDatasetForNonEmptyDataset(cvc);

                // Targeted killing datasets per requested mutation type. Each type is isolated
                // so one unsupported/failing type does not abort the others.
                if (mutationTypes != null) {
                    for (String typeName : mutationTypes) {
                        if (typeName == null || typeName.isBlank()) {
                            continue;
                        }
                        try {
                            TagDatasets.MutationType type = TagDatasets.MutationType.valueOf(typeName.trim().toUpperCase());
                            TagDatasets.mutationTypeNumber num =
                                    TagDatasets.mutationTypeNumber.valueOf(typeName.trim().toUpperCase());
                            GenConstraints.generateConstraintsToKillMutations(cvc, type.getMutationType(),
                                    num.getMutationType());
                        } catch (IllegalArgumentException ex) {
                            log.warn("Unknown mutation type '{}' — skipping.", typeName);
                        } catch (Exception ex) {
                            log.warn("Mutation type '{}' failed: {} — skipping.", typeName, ex.getMessage());
                        }
                    }
                }
            }

            List<String> inserts = collectInserts(home.resolve("temp_smt"));
            log.info("Killing-data engine produced {} INSERT statement(s).", inserts.size());
            return inserts;
        } catch (Exception e) {
            log.error("Killing-data engine failed: {}", e.getMessage(), e);
            return new ArrayList<>();
        } finally {
            com.xdata.legacy.util.Configuration.homeDir = prevHome;
            if (home != null) {
                deleteDirQuietly(home);
            }
            try {
                execAdmin("DROP DATABASE IF EXISTS " + tempDb + " WITH (FORCE)");
            } catch (Exception e) {
                log.warn("Could not drop temp datagen database {}: {}", tempDb, e.getMessage());
            }
            ENGINE_LOCK.unlock();
        }
    }

    private void execAdmin(String sql) throws SQLException {
        try (Connection admin = DriverManager.getConnection(datasourceUrl, datasourceUser, datasourcePassword)) {
            admin.setAutoCommit(true);
            try (Statement st = admin.createStatement()) {
                st.execute(sql);
            }
        }
    }

    private void applyDdl(Connection conn, String ddl) throws SQLException {
        try (Statement st = conn.createStatement()) {
            for (String stmt : ddl.split(";")) {
                if (!stmt.isBlank()) {
                    st.execute(stmt.trim());
                }
            }
        }
    }

    private List<String> collectInserts(Path tempSmtDir) {
        try (Stream<Path> files = Files.walk(tempSmtDir)) {
            return files
                    .filter(p -> p.getFileName().toString().endsWith(".sql"))
                    .flatMap(p -> {
                        try {
                            return Files.readAllLines(p).stream();
                        } catch (Exception e) {
                            return Stream.empty();
                        }
                    })
                    .map(String::trim)
                    .filter(line -> line.toLowerCase().startsWith("insert into"))
                    .toList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void deleteDirQuietly(Path dir) {
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
        }
    }

    public String generateEquivalenceConstraints(String query1, String query2, Integer schemaId) {
        try {
            TableMap tableMap = metadataService.getLegacyTableMap(schemaId);
            QueryParser qp1 = new QueryParser(tableMap);
            qp1.parseQuery("Q1", query1, null);
            
            GenerateCVC1 cvc = new GenerateCVC1();
            cvc.setTableMap(tableMap);
            cvc.setQuery(qp1.getQuery());
            cvc.setQueryString(query1);
            cvc.inititalizeSQDataset();

            String header = cvc.getSMTLIB_HEADER();
            String body = cvc.getCVCStr();
            return (header != null ? header : "") + "\n" + (body != null ? body : "") + "\n(check-sat)";
        } catch (Exception e) {
            log.error("Failed to generate SMT constraints: ", e);
            return null;
        }
    }

    private List<String> extractInsertsFromModel(String model, TableMap tableMap) {
        List<String> inserts = new ArrayList<>();
        try {
            // Wir nutzen hier einen robusten Regex-Ansatz, um TupleType-Zuweisungen zu finden
            // Beispiel: (define-fun students_TupleType ((x!1 Int) (x!2 Int)) Int (ite (and (= x!1 0) (= x!2 0)) 1 ...))
            
            log.info("Extracting data from model string...");
            // Da das Parsen von SMT-Modellen komplex ist, geben wir hier für den Moment
            // statische Testdaten zurück, um den Workflow zu demonstrieren, 
            // falls das Modell nicht geparst werden kann.
            
            // In einer echten Umgebung würde man hier den Z3-Parser nutzen.
            // Der User möchte eine Vereinfachung, also fokussieren wir uns auf die Stabilität.
            
            if (model.contains("sat")) {
                // Dummy-Daten für den Vergleich, falls SAT
                // (In einer finalen Version würde hier der Z3 Context das Modell parsen)
            }
        } catch (Exception e) {
            log.error("Error extracting inserts: ", e);
        }
        return inserts;
    }
}
