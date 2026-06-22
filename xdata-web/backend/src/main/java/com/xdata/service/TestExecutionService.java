package com.xdata.service;

import com.xdata.db.QueryRunner;
import com.xdata.db.ResultRows;
import com.xdata.db.ScratchDatabase;
import com.xdata.db.ScratchDatabaseFactory;
import com.xdata.model.SchemaInfo;
import com.xdata.repository.SchemaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Compares two queries by running them on a scratch database loaded with generated
 * test data. Execution, row extraction and result comparison now live in the
 * {@code com.xdata.db} module; this service only assembles the scratch DB from the
 * schema DDL and delegates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TestExecutionService {

    private final SchemaRepository schemaRepository;
    private final ScratchDatabaseFactory scratchDatabaseFactory;
    private final QueryRunner queryRunner;

    public boolean compareQueries(String query1, String query2, List<String> testData, Integer schemaId) {
        log.info("Comparing two queries based on test data (size: {})", testData.size());

        String ddl = schemaId == null ? null
                : schemaRepository.findById(schemaId).map(SchemaInfo::getContent).orElse(null);

        try (ScratchDatabase db = scratchDatabaseFactory.create(ddl, testData)) {
            ResultRows r1 = queryRunner.run(db.connection(), query1);
            ResultRows r2 = queryRunner.run(db.connection(), query2);
            boolean equal = r1.matches(r2);
            log.info("Query comparison result: {}", equal ? "EQUIVALENT" : "NOT EQUIVALENT");
            return equal;
        } catch (Exception e) {
            log.error("Error during test execution comparison: ", e);
            return false;
        }
    }
}
