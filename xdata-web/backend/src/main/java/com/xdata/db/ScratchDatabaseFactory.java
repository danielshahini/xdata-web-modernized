package com.xdata.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Creates {@link ScratchDatabase} instances: ephemeral Derby in-memory databases
 * with a schema (and optional test data) applied. The single owner of the
 * Derby-memory create/setup/drop lifecycle previously duplicated in
 * TestExecutionService and MetadataService.
 */
@Component
@Slf4j
public class ScratchDatabaseFactory {

    private static final AtomicLong SEQ = new AtomicLong();

    static {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException e) {
            log.warn("Derby embedded driver not on classpath: {}", e.getMessage());
        }
    }

    public ScratchDatabase create(String ddl) {
        return create(ddl, List.of());
    }

    public ScratchDatabase create(String ddl, List<String> testData) {
        String dbUrl = "jdbc:derby:memory:scratch_" + System.nanoTime() + "_"
                + SEQ.incrementAndGet() + ";create=true";
        try {
            Connection conn = DriverManager.getConnection(dbUrl);
            if (ddl != null) {
                executeStatements(conn, ddl);
            }
            if (testData != null && !testData.isEmpty()) {
                executeStatements(conn, String.join(";", testData));
            }
            return new DerbyScratchDatabase(conn, dbUrl);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create scratch database: " + e.getMessage(), e);
        }
    }

    private void executeStatements(Connection conn, String sql) throws SQLException {
        if (sql == null || sql.trim().isEmpty()) return;
        try (Statement stmt = conn.createStatement()) {
            for (String s : sql.split(";")) {
                if (!s.trim().isEmpty()) {
                    try {
                        stmt.execute(s.trim());
                    } catch (SQLException e) {
                        log.warn("Failed to execute statement: {} - {}", s.trim(), e.getMessage());
                    }
                }
            }
        }
    }

    /** Derby-memory implementation; {@link #close} drops the database. */
    private static final class DerbyScratchDatabase implements ScratchDatabase {
        private final Connection connection;
        private final String dbUrl;

        DerbyScratchDatabase(Connection connection, String dbUrl) {
            this.connection = connection;
            this.dbUrl = dbUrl;
        }

        @Override
        public Connection connection() {
            return connection;
        }

        @Override
        public void close() {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
            try {
                DriverManager.getConnection(dbUrl + ";drop=true");
            } catch (SQLException ignored) {
                // Derby signals a successful drop with an exception; ignore.
            }
        }
    }
}
