package com.xdata.eval.adapter;

import com.xdata.db.QueryRunner;
import com.xdata.db.ResultRows;
import com.xdata.eval.core.EquivalenceResult;
import com.xdata.eval.core.EquivalenceResult.Stage;
import com.xdata.eval.core.EquivalenceStageCheck;
import com.xdata.eval.core.QueryPair;
import com.xdata.eval.core.SchemaRef;
import com.xdata.model.DbConnection;
import com.xdata.service.DatabaseService;
import com.xdata.service.SqlSandboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Connection;

/**
 * Last-resort rung: run both queries against the assignment's real database and
 * compare result sets. Abstains (INCONCLUSIVE) when no connection is configured or
 * the connection/query fails; definitive otherwise.
 *
 * <p>Connection acquisition goes through {@link DatabaseService} (driver loading);
 * execution, extraction and the multiset comparison live in the {@code com.xdata.db}
 * module.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AssignedDbStage implements EquivalenceStageCheck {

    private static final int TIMEOUT_SECONDS = 5;
    private static final int MAX_ROWS = 1000;

    private final SqlSandboxService sqlSandboxService;
    private final DatabaseService databaseService;
    private final QueryRunner queryRunner;

    @Override
    public Stage stage() {
        return Stage.ASSIGNED_DB;
    }

    @Override
    public EquivalenceResult check(QueryPair queries, SchemaRef schema) {
        DbConnection connection = schema.assignedConnection();
        if (connection == null || connection.getUrl() == null) {
            return EquivalenceResult.inconclusive(Stage.ASSIGNED_DB, "no assigned connection");
        }

        try (Connection conn = databaseService.getConnection(connection)) {
            sqlSandboxService.validateQuery(queries.instructorQuery());
            sqlSandboxService.validateQuery(queries.studentQuery());

            ResultRows instructor = queryRunner.run(conn, queries.instructorQuery(), TIMEOUT_SECONDS, MAX_ROWS);
            ResultRows student = queryRunner.run(conn, queries.studentQuery(), TIMEOUT_SECONDS, MAX_ROWS);

            return instructor.matches(student)
                    ? EquivalenceResult.equivalent(Stage.ASSIGNED_DB)
                    : EquivalenceResult.notEquivalent(Stage.ASSIGNED_DB);
        } catch (Exception e) {
            log.warn("Compare on assigned DB failed: {}", e.getMessage());
            return EquivalenceResult.inconclusive(Stage.ASSIGNED_DB, e.getMessage());
        }
    }
}
