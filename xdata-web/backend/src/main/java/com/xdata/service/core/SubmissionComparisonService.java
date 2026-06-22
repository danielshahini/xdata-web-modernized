package com.xdata.service.core;

import com.xdata.model.DbConnection;
import com.xdata.service.DatabaseService;
import com.xdata.service.SqlSandboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runs the instructor's reference query and a student's query read-only against the
 * assignment database and returns both result sets plus a multiset diff. Shared by the
 * student-facing and instructor-facing comparison endpoints. The reference SQL itself is
 * never returned — only its OUTPUT.
 */
@Service
@RequiredArgsConstructor
public class SubmissionComparisonService {

    private final DatabaseService databaseService;
    private final SqlSandboxService sqlSandboxService;

    /**
     * @return {expected, actual, missing, extra, match} on success, or {error} when the
     * comparison cannot be performed (no connection, non-SELECT query, SQL error).
     */
    public Map<String, Object> compare(DbConnection conn, String instructorQuery, String studentQuery) {
        if (conn == null || conn.getUrl() == null) {
            return Map.of("error", "No database is configured for this task.");
        }
        try {
            sqlSandboxService.validateQuery(instructorQuery);
            sqlSandboxService.validateQuery(studentQuery);
        } catch (Exception e) {
            return Map.of("error", "Comparison not possible (SELECT queries only).");
        }
        try (Connection c = databaseService.getConnection(conn)) {
            Map<String, Object> expected = runReadOnly(c, instructorQuery, 100);
            Map<String, Object> actual = runReadOnly(c, studentQuery, 100);
            @SuppressWarnings("unchecked")
            List<List<Object>> expRows = (List<List<Object>>) expected.get("rows");
            @SuppressWarnings("unchecked")
            List<List<Object>> actRows = (List<List<Object>>) actual.get("rows");
            // Multiset diff: rows expected-but-missing, and rows present-but-unexpected.
            List<List<Object>> remaining = new ArrayList<>(actRows);
            List<List<Object>> missing = new ArrayList<>();
            for (List<Object> row : expRows) {
                if (!remaining.remove(row)) missing.add(row);
            }
            Map<String, Object> out = new HashMap<>();
            out.put("expected", expected);
            out.put("actual", actual);
            out.put("missing", missing);   // in expected, not in student's output
            out.put("extra", remaining);   // in student's output, not expected
            out.put("match", missing.isEmpty() && remaining.isEmpty());
            return out;
        } catch (SQLException e) {
            return Map.of("error", "SQL error: " + e.getMessage());
        } catch (Exception e) {
            return Map.of("error", "Comparison failed.");
        }
    }

    /** Read-only execution helper: returns {columns, rows, rowCount, truncated}. */
    public Map<String, Object> runReadOnly(Connection c, String query, int maxRows) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.setQueryTimeout(10);
            st.setMaxRows(maxRows + 1);
            try (ResultSet rs = st.executeQuery(query)) {
                ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();
                List<String> columns = new ArrayList<>();
                for (int i = 1; i <= cols; i++) columns.add(md.getColumnLabel(i));
                List<List<Object>> rows = new ArrayList<>();
                boolean truncated = false;
                while (rs.next()) {
                    if (rows.size() >= maxRows) { truncated = true; break; }
                    List<Object> row = new ArrayList<>(cols);
                    for (int i = 1; i <= cols; i++) {
                        Object v = rs.getObject(i);
                        row.add(v == null ? null : String.valueOf(v));
                    }
                    rows.add(row);
                }
                Map<String, Object> out = new HashMap<>();
                out.put("columns", columns);
                out.put("rows", rows);
                out.put("rowCount", rows.size());
                out.put("truncated", truncated);
                return out;
            }
        }
    }
}
