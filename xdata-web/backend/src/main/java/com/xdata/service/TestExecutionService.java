package com.xdata.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestExecutionService {

    private final MetadataService metadataService;
    private final com.xdata.repository.SchemaRepository schemaRepository;

    public boolean compareQueries(String query1, String query2, List<String> testData, Integer schemaId) {
        log.info("Comparing two queries based on test data (size: {})", testData.size());
        
        String dbUrl = "jdbc:derby:memory:execDB_" + System.currentTimeMillis() + ";create=true";
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            // 1. Setup Schema
            if (schemaId != null) {
                schemaRepository.findById(schemaId).ifPresent(schema -> {
                    try {
                        executeStatements(conn, schema.getContent());
                    } catch (SQLException e) {
                        log.warn("Error setting up schema DDL: {}", e.getMessage());
                    }
                });
            }

            // 2. Insert Test Data
            executeStatements(conn, String.join(";", testData));

            // 3. Execute and compare
            List<List<Object>> res1 = executeQuery(conn, query1);
            List<List<Object>> res2 = executeQuery(conn, query2);

            boolean equal = compareResults(res1, res2);
            log.info("Query comparison result: {}", equal ? "EQUIVALENT" : "NOT EQUIVALENT");
            return equal;

        } catch (SQLException e) {
            log.error("Error during test execution comparison: ", e);
            return false;
        } finally {
            try {
                DriverManager.getConnection(dbUrl + ";drop=true");
            } catch (SQLException ignored) {}
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

    private List<List<Object>> executeQuery(Connection conn, String query) throws SQLException {
        List<List<Object>> result = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            int colCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                List<Object> row = new ArrayList<>();
                for (int i = 1; i <= colCount; i++) {
                    row.add(rs.getObject(i));
                }
                result.add(row);
            }
        }
        return result;
    }

    private boolean compareResults(List<List<Object>> res1, List<List<Object>> res2) {
        if (res1.size() != res2.size()) return false;
        // Da SQL Multiset-Semantik hat (sofern kein ORDER BY), sortieren wir die Ergebnisse für den Vergleich, 
        // oder wir prüfen auf Multiset-Gleichheit.
        // Für diesen Zweck reicht ein einfacher Listen-Vergleich (wenn wir annehmen, dass Ordnung wichtig ist oder wir sie normalisieren).
        // Besser: Multiset-Vergleich
        List<List<Object>> copy2 = new ArrayList<>(res2);
        for (List<Object> row1 : res1) {
            boolean found = false;
            for (int i = 0; i < copy2.size(); i++) {
                if (Objects.equals(row1, copy2.get(i))) {
                    copy2.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return copy2.isEmpty();
    }
}
