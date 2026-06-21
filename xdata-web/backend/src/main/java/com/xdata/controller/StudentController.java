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
    private final com.xdata.repository.RegradeRequestRepository regradeRequestRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        return accessControlService.getCurrentUser().map(user -> {
            List<String> courseIds = accessControlService.getUserCourseIds();
            return ResponseEntity.ok(submissionAnalytics.dashboard(user, courseIds));
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
                    m.put("assignmentId", assignmentId);
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
            return ResponseEntity.badRequest().body("questionId und query sind erforderlich.");
        }
        Integer questionId;
        try {
            questionId = (qIdObj instanceof Integer) ? (Integer) qIdObj : Integer.parseInt(qIdObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Ungültige questionId.");
        }

        return questionRepository.findById(questionId).map(question -> {
            Assignment assignment = question.getAssignment();
            if (assignment == null) {
                return ResponseEntity.status(403).body("Zugriff verweigert für diesen Kurs.");
            }
            courseAccessGuard.requireCourseAccess(assignment.getCourseId());
            
            if (assignment.getPublishedDate() != null && assignment.getPublishedDate().isAfter(LocalDateTime.now())) {
                return ResponseEntity.status(403).body("Diese Aufgabe ist noch nicht veröffentlicht.");
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
                            .body("Maximale Anzahl an Versuchen (" + maxAttempts + ") für diese Frage erreicht.");
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
            return ResponseEntity.badRequest().body("questionId und query sind erforderlich.");
        }
        Integer questionId;
        try {
            questionId = (qIdObj instanceof Integer) ? (Integer) qIdObj : Integer.parseInt(qIdObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Ungültige questionId.");
        }

        return questionRepository.findById(questionId).map(question -> {
            Assignment assignment = question.getAssignment();
            if (assignment == null) return ResponseEntity.status(403).body("Zugriff verweigert.");
            courseAccessGuard.requireCourseAccess(assignment.getCourseId());

            // Only allow read-only SELECTs.
            try {
                sqlSandboxService.validateQuery(query);
            } catch (Exception e) {
                return ResponseEntity.ok(Map.of("error", "Nur lesende SELECT-Abfragen sind erlaubt."));
            }

            com.xdata.model.DbConnection conn = assignment.getConnection();
            if (conn == null || conn.getUrl() == null) {
                return ResponseEntity.ok(Map.of("error", "Für diese Aufgabe ist keine Datenbank konfiguriert."));
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
                return ResponseEntity.ok(Map.of("error", "SQL-Fehler: " + e.getMessage()));
            } catch (Exception e) {
                return ResponseEntity.ok(Map.of("error", "Ausführung fehlgeschlagen: " + e.getMessage()));
            }
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
        return ResponseEntity.ok(java.util.Map.of("message", "Anfechtung eingereicht."));
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
