package com.xdata.db;

import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes one query on a given connection and extracts its rows. The single
 * place row extraction lives (previously duplicated in TestExecutionService and
 * AssignedDbStage). Caller-controlled timeout/maxRows (0 = unlimited) keep each
 * caller's existing behaviour; sandbox validation stays with the caller.
 */
@Component
public class QueryRunner {

    public ResultRows run(Connection conn, String sql) throws SQLException {
        return run(conn, sql, 0, 0);
    }

    public ResultRows run(Connection conn, String sql, int timeoutSeconds, int maxRows) throws SQLException {
        List<List<Object>> rows = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            if (timeoutSeconds > 0) stmt.setQueryTimeout(timeoutSeconds);
            if (maxRows > 0) stmt.setMaxRows(maxRows);
            try (ResultSet rs = stmt.executeQuery(sql)) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (rs.next()) {
                    List<Object> row = new ArrayList<>(columnCount);
                    for (int i = 1; i <= columnCount; i++) {
                        row.add(rs.getObject(i));
                    }
                    rows.add(row);
                }
            }
        }
        return new ResultRows(rows);
    }
}
