package com.xdata.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Backs the public, login-free "Try SQL" sandbox (W3Schools-style). Every run
 * spins up a private, in-memory H2 database (PostgreSQL compatibility mode),
 * seeds it with a small fixed teaching dataset, runs the user's read-only
 * SELECT, and throws the database away when the connection closes. Nothing here
 * can touch the real application database — the H2 instance is anonymous
 * (jdbc:h2:mem: with no name) and lives only for the duration of one request.
 */
@Service
@RequiredArgsConstructor
public class PublicSandboxService {

    private final SqlSandboxService sqlSandboxService;

    private static final int MAX_ROWS = 100;
    private static final int TIMEOUT_SECONDS = 5;

    /** DDL + seed data for the sample "university" schema. H2/PostgreSQL compatible. */
    private static final String SEED_SQL = """
        CREATE TABLE students (
            id      INT PRIMARY KEY,
            name    VARCHAR(100) NOT NULL,
            age     INT,
            major   VARCHAR(50),
            gpa     DECIMAL(3,2)
        );
        CREATE TABLE courses (
            course_id INT PRIMARY KEY,
            title     VARCHAR(100) NOT NULL,
            credits   INT
        );
        CREATE TABLE enrollments (
            student_id INT REFERENCES students(id),
            course_id  INT REFERENCES courses(course_id),
            grade      DECIMAL(3,2),
            PRIMARY KEY (student_id, course_id)
        );
        INSERT INTO students (id, name, age, major, gpa) VALUES
            (1, 'Alice',   22, 'Computer Science', 3.8),
            (2, 'Bob',     24, 'Mathematics',      3.2),
            (3, 'Carol',   21, 'Computer Science', 3.9),
            (4, 'Dave',    23, 'Physics',          2.9),
            (5, 'Erin',    20, 'Computer Science', 3.5),
            (6, 'Frank',   25, 'Mathematics',      2.7);
        INSERT INTO courses (course_id, title, credits) VALUES
            (10, 'Databases',   6),
            (20, 'Algorithms',  6),
            (30, 'Analysis',    9),
            (40, 'Statistics',  3);
        INSERT INTO enrollments (student_id, course_id, grade) VALUES
            (1, 10, 1.3), (1, 20, 1.7), (2, 30, 2.3), (3, 10, 1.0),
            (3, 20, 1.3), (4, 30, 2.7), (5, 10, 1.7), (5, 40, 2.0),
            (6, 20, 3.0), (2, 40, 1.7);
        """;

    /** Example queries surfaced in the UI so newcomers have a starting point. */
    public List<Map<String, String>> exampleQueries() {
        List<Map<String, String>> ex = new ArrayList<>();
        ex.add(example("All students", "SELECT * FROM students;"));
        ex.add(example("Filter & sort", "SELECT name, gpa FROM students\nWHERE gpa > 3.0\nORDER BY gpa DESC;"));
        ex.add(example("Join across tables", "SELECT s.name, c.title, e.grade\nFROM enrollments e\nJOIN students s ON s.id = e.student_id\nJOIN courses c ON c.course_id = e.course_id;"));
        ex.add(example("Group & count", "SELECT major, COUNT(*) AS anzahl, ROUND(AVG(gpa), 2) AS schnitt\nFROM students\nGROUP BY major\nORDER BY anzahl DESC;"));
        return ex;
    }

    /** Static metadata describing the sandbox schema, for the schema explorer panel. */
    public Map<String, Object> schemaMetadata() {
        Map<String, Object> students = table("students",
                col("id", "INT", true, null, null),
                col("name", "VARCHAR", false, null, null),
                col("age", "INT", false, null, null),
                col("major", "VARCHAR", false, null, null),
                col("gpa", "DECIMAL", false, null, null));
        Map<String, Object> courses = table("courses",
                col("course_id", "INT", true, null, null),
                col("title", "VARCHAR", false, null, null),
                col("credits", "INT", false, null, null));
        Map<String, Object> enrollments = table("enrollments",
                col("student_id", "INT", true, "students", "id"),
                col("course_id", "INT", true, "courses", "course_id"),
                col("grade", "DECIMAL", false, null, null));
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("schemaName", "University (Demo)");
        meta.put("tables", List.of(students, courses, enrollments));
        return meta;
    }

    /** Validate, run against a throwaway H2 instance, and return columns/rows or an error. */
    public Map<String, Object> run(String query) {
        if (query == null || query.isBlank()) {
            return Map.of("error", "Please enter a SQL query.");
        }
        try {
            sqlSandboxService.validateQuery(query);
        } catch (Exception e) {
            return Map.of("error", "Only read-only SELECT queries are allowed.");
        }

        try (Connection c = DriverManager.getConnection("jdbc:h2:mem:;MODE=PostgreSQL;DB_CLOSE_DELAY=0")) {
            try (Statement seed = c.createStatement()) {
                seed.execute(SEED_SQL);
            }
            try (Statement st = c.createStatement()) {
                st.setQueryTimeout(TIMEOUT_SECONDS);
                st.setMaxRows(MAX_ROWS + 1);
                try (ResultSet rs = st.executeQuery(query)) {
                    ResultSetMetaData md = rs.getMetaData();
                    int cols = md.getColumnCount();
                    List<String> columns = new ArrayList<>();
                    for (int i = 1; i <= cols; i++) columns.add(md.getColumnLabel(i));
                    List<List<Object>> rows = new ArrayList<>();
                    boolean truncated = false;
                    while (rs.next()) {
                        if (rows.size() >= MAX_ROWS) { truncated = true; break; }
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
        } catch (SQLException e) {
            return Map.of("error", "SQL error: " + e.getMessage());
        } catch (Exception e) {
            return Map.of("error", "Execution failed.");
        }
    }

    private static Map<String, String> example(String label, String sql) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("label", label);
        m.put("sql", sql);
        return m;
    }

    private static Map<String, Object> table(String name, Map<String, Object>... columns) {
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("tableName", name);
        t.put("columns", List.of(columns));
        return t;
    }

    private static Map<String, Object> col(String name, String type, boolean pk, String refTable, String refCol) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("columnName", name);
        m.put("dataType", type);
        m.put("primaryKey", pk);
        m.put("referencesTable", refTable);
        m.put("referencesColumn", refCol);
        return m;
    }
}
