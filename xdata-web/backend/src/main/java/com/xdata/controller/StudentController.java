package com.xdata.controller;

import com.xdata.model.Assignment;
import com.xdata.model.Question;
import com.xdata.model.Submission;
import com.xdata.repository.QuestionRepository;
import com.xdata.repository.SubmissionRepository;
import com.xdata.security.CourseAccessGuard;
import com.xdata.service.AccessControlService;
import com.xdata.service.core.AssignmentService;
import com.xdata.service.core.EvaluationService;
import com.xdata.service.core.SubmissionAnalytics;
import com.xdata.service.core.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
@Transactional
public class StudentController {

    private final AssignmentService assignmentService;
    private final QuestionRepository questionRepository;
    private final SubmissionRepository submissionRepository;
    private final AccessControlService accessControlService;
    private final CourseAccessGuard courseAccessGuard;
    private final SubmissionAnalytics submissionAnalytics;
    private final EvaluationService evaluationService;
    private final SubmissionService submissionService;
    private final com.xdata.service.DatabaseService databaseService;
    private final com.xdata.service.SqlSandboxService sqlSandboxService;
    private final com.xdata.service.core.SubmissionComparisonService comparisonService;
    private final com.xdata.repository.RegradeRequestRepository regradeRequestRepository;
    private final com.xdata.repository.UserRepository userRepository;
    private final com.xdata.service.core.SchemaService schemaService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        return accessControlService.getCurrentUser().map(user -> {
            List<String> courseIds = accessControlService.getUserCourseIds();
            return ResponseEntity.ok(submissionAnalytics.dashboard(user, courseIds));
        }).orElse(ResponseEntity.status(401).build());
    }

    /** Privacy-aware cohort leaderboard for the student's own courses. */
    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard() {
        return accessControlService.getCurrentUser().map(user -> {
            List<String> courseIds = accessControlService.getUserCourseIds();
            if (courseIds.isEmpty()) {
                return ResponseEntity.ok(Map.of("totalStudents", 0, "entries", List.of()));
            }
            List<com.xdata.model.XDataUser> students = userRepository
                    .findDistinctByCourses_InstructorCourseIdIn(courseIds).stream()
                    .filter(u -> "STUDENT".equalsIgnoreCase(u.getRole()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(submissionAnalytics.leaderboard(user, courseIds, students));
        }).orElse(ResponseEntity.status(401).build());
    }

    @GetMapping("/assignments/{assignmentId}/questions")
    public ResponseEntity<List<Map<String, Object>>> getQuestions(@PathVariable Integer assignmentId) {
        return assignmentService.getAssignmentById(assignmentId).map(assignment -> {
            courseAccessGuard.requireCourseAccess(assignment.getCourseId());
            if (assignment.getPublishedDate() != null && assignment.getPublishedDate().isAfter(LocalDateTime.now())) {
                return ResponseEntity.status(403).<List<Map<String, Object>>>build();
            }
            // SECURITY: never expose the reference solution (instructorQuery) to
            // students — return a sanitized view with only display fields.
            List<Map<String, Object>> safe = questionRepository.findByAssignment_Id(assignmentId).stream()
                .map(q -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", q.getId());
                    m.put("name", q.getName());
                    m.put("marks", q.getMarks());
                    m.put("tags", q.getTags());
                    m.put("difficulty", q.getDifficulty());
                    m.put("description", q.getDescription());
                    m.put("hints", q.getHints() == null ? List.of()
                            : java.util.Arrays.stream(q.getHints().split("\\r?\\n"))
                                .map(String::trim).filter(h -> !h.isEmpty()).collect(Collectors.toList()));
                    m.put("assignmentId", assignmentId);
                    // Needed by the student schema explorer to load the table/PK/FK metadata.
                    m.put("defaultSchemaId", assignment.getDefaultSchemaId());
                    return m;
                })
                .collect(Collectors.toList());
            return ResponseEntity.ok(safe);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitSolution(@RequestBody Map<String, Object> body) {
        Object qIdObj = body != null ? body.get("questionId") : null;
        String query = body != null ? (String) body.get("query") : null;
        if (qIdObj == null || query == null || query.isBlank()) {
            return ResponseEntity.badRequest().body("questionId and query are required.");
        }
        Integer questionId;
        try {
            questionId = (qIdObj instanceof Integer) ? (Integer) qIdObj : Integer.parseInt(qIdObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid questionId.");
        }

        return questionRepository.findById(questionId).map(question -> {
            Assignment assignment = question.getAssignment();
            if (assignment == null) {
                return ResponseEntity.status(403).body("Access denied for this course.");
            }
            courseAccessGuard.requireCourseAccess(assignment.getCourseId());

            if (assignment.getPublishedDate() != null && assignment.getPublishedDate().isAfter(LocalDateTime.now())) {
                return ResponseEntity.status(403).body("This assignment is not yet published.");
            }

            // Enforce the per-question attempt limit, if configured on the assignment.
            Integer maxAttempts = assignment.getMaxAttempts();
            if (maxAttempts != null && maxAttempts > 0) {
                int used = submissionRepository
                        .findByUser_LoginIdAndQuestion_IdOrderBySubmissionTimeDesc(
                                accessControlService.getCurrentUserLoginId(), questionId)
                        .size();
                if (used >= maxAttempts) {
                    return ResponseEntity.status(403)
                            .body("Maximum number of attempts (" + maxAttempts + ") for this question reached.");
                }
            }

            Submission submission = submissionService.createSubmission(
                    accessControlService.getCurrentUserLoginId(),
                    questionId,
                    query
            );

            // Trigger async grading only AFTER this transaction commits — otherwise the
            // async thread (separate transaction) cannot see the just-saved submission
            // and fails with "Submission not found".
            final Integer submissionId = submission.getId();
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        evaluationService.evaluateSubmissionAsync(submissionId);
                    }
                });
            } else {
                evaluationService.evaluateSubmissionAsync(submissionId);
            }

            return ResponseEntity.ok(submission);
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Run a student's query against the assignment database WITHOUT grading, so they
     * can see the result set before submitting. Read-only (sandbox-validated), row-limited.
     */
    @PostMapping("/run")
    public ResponseEntity<?> runQuery(@RequestBody Map<String, Object> body) {
        Object qIdObj = body != null ? body.get("questionId") : null;
        String query = body != null ? (String) body.get("query") : null;
        if (qIdObj == null || query == null || query.isBlank()) {
            return ResponseEntity.badRequest().body("questionId and query are required.");
        }
        Integer questionId;
        try {
            questionId = (qIdObj instanceof Integer) ? (Integer) qIdObj : Integer.parseInt(qIdObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid questionId.");
        }

        return questionRepository.findById(questionId).map(question -> {
            Assignment assignment = question.getAssignment();
            if (assignment == null) return ResponseEntity.status(403).body("Access denied.");
            courseAccessGuard.requireCourseAccess(assignment.getCourseId());

            // Only allow read-only SELECTs.
            try {
                sqlSandboxService.validateQuery(query);
            } catch (Exception e) {
                return ResponseEntity.ok(Map.of("error", "Only read-only SELECT queries are allowed."));
            }

            com.xdata.model.DbConnection conn = assignment.getConnection();
            if (conn == null || conn.getUrl() == null) {
                return ResponseEntity.ok(Map.of("error", "No database is configured for this assignment."));
            }

            final int MAX_ROWS = 100;
            try (java.sql.Connection c = databaseService.getConnection(conn);
                 java.sql.Statement st = c.createStatement()) {
                st.setQueryTimeout(10);
                st.setMaxRows(MAX_ROWS + 1);
                try (java.sql.ResultSet rs = st.executeQuery(query)) {
                    java.sql.ResultSetMetaData md = rs.getMetaData();
                    int cols = md.getColumnCount();
                    List<String> columns = new java.util.ArrayList<>();
                    for (int i = 1; i <= cols; i++) columns.add(md.getColumnLabel(i));
                    List<List<Object>> rows = new java.util.ArrayList<>();
                    boolean truncated = false;
                    while (rs.next()) {
                        if (rows.size() >= MAX_ROWS) { truncated = true; break; }
                        List<Object> row = new java.util.ArrayList<>(cols);
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
                    return ResponseEntity.ok(out);
                }
            } catch (java.sql.SQLException e) {
                return ResponseEntity.ok(Map.of("error", "SQL error: " + e.getMessage()));
            } catch (Exception e) {
                return ResponseEntity.ok(Map.of("error", "Execution failed: " + e.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Expected-output vs. the student's own output for a graded submission. Runs the
     * reference query and the student's stored query read-only against the assignment DB
     * and returns both result sets plus a multiset diff. The reference SQL itself is never
     * sent — only its OUTPUT — and only once grades are released.
     */
    @GetMapping("/submissions/{submissionId}/result-comparison")
    public ResponseEntity<?> resultComparison(@PathVariable Integer submissionId) {
        String loginId = accessControlService.getCurrentUserLoginId();
        Submission s = submissionRepository.findById(submissionId).orElse(null);
        if (s == null) return ResponseEntity.notFound().build();
        if (s.getUser() == null || !s.getUser().getLoginId().equalsIgnoreCase(loginId)) {
            return ResponseEntity.status(403).build();
        }
        Question question = s.getQuestion();
        Assignment assignment = question != null ? question.getAssignment() : null;
        if (assignment == null) return ResponseEntity.notFound().build();
        if (Boolean.FALSE.equals(assignment.getGradesReleased())) {
            return ResponseEntity.status(403).body(Map.of("error", "Result comparison is only available after grade release."));
        }
        return ResponseEntity.ok(comparisonService.compare(
                assignment.getConnection(), question.getInstructorQuery(), s.getQuery()));
    }

    /**
     * A small sample of real rows per table of the assignment's schema, so students can see
     * the data they are querying. Read-only; table names come strictly from the schema
     * metadata (an allow-list), never from user input.
     */
    @GetMapping("/assignments/{assignmentId}/sample-data")
    public ResponseEntity<?> sampleData(@PathVariable Integer assignmentId) {
        return assignmentService.getAssignmentById(assignmentId).map(assignment -> {
            courseAccessGuard.requireCourseAccess(assignment.getCourseId());
            Integer schemaId = assignment.getDefaultSchemaId();
            if (schemaId == null) {
                return ResponseEntity.ok(Map.of("tables", List.of()));
            }
            com.xdata.model.DbConnection conn = assignment.getConnection();
            if (conn == null || conn.getUrl() == null) {
                return ResponseEntity.ok(Map.of("error", "No database is configured for this assignment."));
            }
            com.xdata.dto.SchemaMetadataDTO meta = schemaService.getSchemaMetadata(schemaId);
            List<Map<String, Object>> tables = new java.util.ArrayList<>();
            try (java.sql.Connection c = databaseService.getConnection(conn)) {
                for (com.xdata.dto.SchemaMetadataDTO.TableMetadataDTO t : meta.getTables()) {
                    String table = t.getTableName();
                    // Allow-list guard: only plain identifiers from the parsed DDL.
                    if (table == null || !table.matches("[A-Za-z_][A-Za-z0-9_]*")) continue;
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("tableName", table);
                    try (java.sql.Statement st = c.createStatement()) {
                        st.setQueryTimeout(8);
                        st.setMaxRows(5);
                        try (java.sql.ResultSet rs = st.executeQuery("SELECT * FROM " + table + " LIMIT 5")) {
                            java.sql.ResultSetMetaData md = rs.getMetaData();
                            int cols = md.getColumnCount();
                            List<String> columns = new java.util.ArrayList<>();
                            for (int i = 1; i <= cols; i++) columns.add(md.getColumnLabel(i));
                            List<List<Object>> rows = new java.util.ArrayList<>();
                            while (rs.next()) {
                                List<Object> row = new java.util.ArrayList<>(cols);
                                for (int i = 1; i <= cols; i++) {
                                    Object v = rs.getObject(i);
                                    row.add(v == null ? null : String.valueOf(v));
                                }
                                rows.add(row);
                            }
                            entry.put("columns", columns);
                            entry.put("rows", rows);
                        }
                    } catch (Exception e) {
                        entry.put("columns", List.of());
                        entry.put("rows", List.of());
                        entry.put("error", true);
                    }
                    tables.add(entry);
                }
            } catch (Exception e) {
                return ResponseEntity.ok(Map.of("error", "Sample data not available."));
            }
            Map<String, Object> out = new HashMap<>();
            out.put("schemaName", meta.getSchemaName());
            out.put("tables", tables);
            return ResponseEntity.ok(out);
        }).orElse(ResponseEntity.notFound().build());
    }


    /** Student raises an objection / regrade request on one of their graded submissions. */
    @PostMapping("/submissions/{submissionId}/regrade-request")
    public ResponseEntity<?> requestRegrade(@PathVariable Integer submissionId, @RequestBody Map<String, String> body) {
        String loginId = accessControlService.getCurrentUserLoginId();
        Submission s = submissionRepository.findById(submissionId).orElse(null);
        if (s == null) return ResponseEntity.notFound().build();
        if (s.getUser() == null || !s.getUser().getLoginId().equalsIgnoreCase(loginId)) {
            return ResponseEntity.status(403).build();
        }
        com.xdata.model.RegradeRequest r = com.xdata.model.RegradeRequest.builder()
                .submissionId(submissionId)
                .studentLoginId(loginId)
                .message(body != null ? body.get("message") : null)
                .status("OPEN")
                .build();
        regradeRequestRepository.save(r);
        return ResponseEntity.ok(java.util.Map.of("message", "Objection submitted."));
    }

    @GetMapping("/submissions")
    public ResponseEntity<List<Map<String, Object>>> getMySubmissions() {
        String loginId = accessControlService.getCurrentUserLoginId();
        return ResponseEntity.ok(toStudentView(submissionRepository.findByUser_LoginId(loginId)));
    }

    @GetMapping("/questions/{questionId}/attempts")
    public ResponseEntity<List<Map<String, Object>>> getAttempts(@PathVariable Integer questionId) {
        String loginId = accessControlService.getCurrentUserLoginId();
        return ResponseEntity.ok(toStudentView(
                submissionRepository.findByUser_LoginIdAndQuestion_IdOrderBySubmissionTimeDesc(loginId, questionId)));
    }

    /**
     * Student-safe submission view: never exposes the reference solution
     * (question.instructorQuery), and withholds marks/feedback for assignments
     * whose grades the instructor has not released yet.
     */
    private List<Map<String, Object>> toStudentView(List<Submission> subs) {
        List<Map<String, Object>> out = new java.util.ArrayList<>();
        for (Submission s : subs) {
            Assignment a = s.getQuestion() != null ? s.getQuestion().getAssignment() : null;
            boolean released = a == null || a.getGradesReleased() == null || a.getGradesReleased();
            Map<String, Object> m = new HashMap<>();
            m.put("submissionId", s.getId());
            m.put("questionId", s.getQuestion() != null ? s.getQuestion().getId() : null);
            m.put("query", s.getQuery());
            m.put("submissionTime", s.getSubmissionTime());
            m.put("gradesReleased", released);
            m.put("marks", released ? s.getMarks() : null);
            m.put("markInfoJson", released ? s.getMarkInfoJson() : null);
            m.put("instructorFeedback", released ? s.getInstructorFeedback() : null);
            out.add(m);
        }
        return out;
    }

}
