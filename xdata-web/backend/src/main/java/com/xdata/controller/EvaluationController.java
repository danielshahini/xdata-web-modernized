package com.xdata.controller;

import com.xdata.model.Question;
import com.xdata.model.Submission;
import com.xdata.repository.QuestionRepository;
import com.xdata.repository.SubmissionRepository;
import com.xdata.security.CourseAccessGuard;
import com.xdata.service.core.EvaluationService;
import com.xdata.service.PlagiarismService;
import com.xdata.partialmarking.core.MarkInfo;
import com.xdata.partialmarking.core.PartialMarkParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/evaluation")
@RequiredArgsConstructor
@Slf4j
public class EvaluationController {
    private final EvaluationService evaluationService;
    private final QuestionRepository questionRepository;
    private final SubmissionRepository submissionRepository;
    private final CourseAccessGuard courseAccessGuard;
    private final PlagiarismService plagiarismService;
    private final com.xdata.repository.RegradeRequestRepository regradeRequestRepository;
    private final com.xdata.service.AccessControlService accessControlService;
    private final com.xdata.service.core.SubmissionComparisonService comparisonService;

    @PostMapping("/start/{questionId}")
    public ResponseEntity<String> startEvaluation(@PathVariable Integer questionId) {
        courseAccessGuard.requireInstructorOrAdmin();
        return questionRepository.findById(questionId).map(q -> {
            evaluationService.evaluateQuestionAsync(q.getAssignment().getId(), q.getId(), q.getAssignment().getCourseId());
            return ResponseEntity.ok("Bewertung gestartet");
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/submissions/{questionId}")
    public ResponseEntity<List<Submission>> getSubmissions(@PathVariable Integer questionId) {
        return ResponseEntity.ok(submissionRepository.findByQuestion_Id(questionId));
    }

    @PostMapping("/playground/partial-marking")
    public ResponseEntity<?> simulateGrading(@RequestBody Map<String, Object> body) {
        String instructorQuery = (String) body.get("patternQuery");
        String studentQuery = (String) body.get("studentQuery");
        Integer schemaId = getInteger(body, "schemaId");
        
        Map<String, Object> weightsMap = (Map<String, Object>) body.get("params");
        PartialMarkParameters params = new PartialMarkParameters();
        if (weightsMap != null) {
            params.setPredicate(getWeight(weightsMap, "predicate"));
            params.setProjection(getWeight(weightsMap, "projection"));
            params.setRelation(getWeight(weightsMap, "relation"));
            params.setJoins(getWeight(weightsMap, "joins"));
            params.setGroupBy(getWeight(weightsMap, "groupBy"));
            params.setHavingClause(getWeight(weightsMap, "havingClause"));
            params.setOrderBy(getWeight(weightsMap, "orderBy"));
            params.setAggregates(getWeight(weightsMap, "aggregates"));
            params.setDistinct(getWeight(weightsMap, "distinct"));
            params.setSetOperators(getWeight(weightsMap, "setOperators"));
            params.setWhereSubQueries(getWeight(weightsMap, "whereSubQueries"));
            params.setFromSubQueries(getWeight(weightsMap, "fromSubQueries"));
            params.setOuterQuery(getWeight(weightsMap, "outerQuery"));
            params.setSubQConnective(getWeight(weightsMap, "subQConnective"));
            params.setMaxPartialMarks(getWeight(weightsMap, "maxPartialMarks"));
        }

        try {
            MarkInfo markInfo = (MarkInfo) evaluationService.calculatePartialMarksLive(instructorQuery, studentQuery, schemaId, params);
            return ResponseEntity.ok(markInfo);
        } catch (Exception e) {
            log.error("Simulation error: ", e);
            return ResponseEntity.status(400).body(Map.of("message", "Fehler bei der Simulation: " + e.getMessage()));
        }
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        if (val instanceof String && !((String) val).isEmpty()) {
            try {
                return (int) Double.parseDouble((String) val);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private int getWeight(Map<String, Object> map, String key) {
        Integer val = getInteger(map, key);
        return val != null ? val : 1;
    }

    @GetMapping("/plagiarism/{assignmentId}")
    public ResponseEntity<?> checkPlagiarism(@PathVariable Integer assignmentId, @RequestParam(defaultValue = "0.8") double threshold) {
        courseAccessGuard.requireInstructorOrAdmin();
        return ResponseEntity.ok(plagiarismService.checkAssignment(assignmentId, threshold));
    }
    
    @PostMapping("/submissions/{submissionId}/feedback")
    public ResponseEntity<?> addFeedback(@PathVariable Integer submissionId, @RequestBody Map<String, String> body) {
        courseAccessGuard.requireInstructorOrAdmin();
        return submissionRepository.findById(submissionId).map(s -> {
            s.setInstructorFeedback(body.get("feedback"));
            submissionRepository.save(s);
            return ResponseEntity.ok(s);
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- Regrade requests + manual override (#8) ---

    @GetMapping("/regrade-requests")
    public ResponseEntity<?> listRegradeRequests() {
        courseAccessGuard.requireInstructorOrAdmin();
        boolean admin = accessControlService.isAdmin();
        java.util.List<String> myCourses = admin ? null : accessControlService.getUserCourseIds();
        java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
        for (com.xdata.model.RegradeRequest r : regradeRequestRepository.findByStatusOrderByCreatedAtDesc("OPEN")) {
            com.xdata.model.Submission s = submissionRepository.findById(r.getSubmissionId()).orElse(null);
            com.xdata.model.Question q = s != null ? s.getQuestion() : null;
            com.xdata.model.Assignment a = q != null ? q.getAssignment() : null;
            String courseId = a != null ? a.getCourseId() : null;
            // Scope to the instructor's own courses (admins see all).
            if (!admin && (courseId == null || myCourses == null || !myCourses.contains(courseId))) continue;
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", r.getId());
            m.put("submissionId", r.getSubmissionId());
            m.put("studentLoginId", r.getStudentLoginId());
            m.put("message", r.getMessage());
            m.put("createdAt", r.getCreatedAt());
            if (s != null) {
                m.put("studentName", s.getUser() != null ? s.getUser().getUsername() : r.getStudentLoginId());
                m.put("currentMarks", s.getMarks());
                m.put("query", s.getQuery());
                m.put("markInfoJson", s.getMarkInfoJson());
                m.put("submissionTime", s.getSubmissionTime());
            }
            if (q != null) {
                m.put("questionName", q.getName());
                m.put("questionDescription", q.getDescription());
                m.put("maxMarks", q.getMarks());
            }
            if (a != null) {
                m.put("assignmentName", a.getName());
                m.put("courseId", courseId);
                m.put("courseName", a.getCourse() != null ? a.getCourse().getName() : null);
            }
            out.add(m);
        }
        return ResponseEntity.ok(out);
    }

    /**
     * Instructor view of expected-vs-actual output for a disputed submission, so they can
     * judge a regrade request. Course-access checked; the reference SQL is never returned,
     * only its output (see SubmissionComparisonService).
     */
    @GetMapping("/submissions/{submissionId}/result-comparison")
    public ResponseEntity<?> instructorResultComparison(@PathVariable Integer submissionId) {
        courseAccessGuard.requireInstructorOrAdmin();
        com.xdata.model.Submission s = submissionRepository.findById(submissionId).orElse(null);
        if (s == null) return ResponseEntity.notFound().build();
        com.xdata.model.Question q = s.getQuestion();
        com.xdata.model.Assignment a = q != null ? q.getAssignment() : null;
        if (a == null) return ResponseEntity.notFound().build();
        courseAccessGuard.requireCourseAccess(a.getCourseId());
        return ResponseEntity.ok(comparisonService.compare(
                a.getConnection(), q.getInstructorQuery(), s.getQuery()));
    }

    @PostMapping("/submissions/{submissionId}/override")
    public ResponseEntity<?> overrideMark(@PathVariable Integer submissionId, @RequestBody Map<String, Object> body) {
        courseAccessGuard.requireInstructorOrAdmin();
        Object marksObj = body.get("marks"); // percentage 0..100
        if (marksObj == null) return ResponseEntity.badRequest().body("marks (0-100) ist erforderlich.");
        double pct;
        try { pct = Double.parseDouble(marksObj.toString()); } catch (Exception e) { return ResponseEntity.badRequest().body("Ungültige Note."); }
        if (pct < 0 || pct > 100) return ResponseEntity.badRequest().body("Note muss zwischen 0 und 100 liegen.");
        String reason = body.get("reason") != null ? body.get("reason").toString() : null;

        return submissionRepository.findById(submissionId).map(s -> {
            s.setMarks((float) (pct / 100.0));
            s.setManuallyGraded(true);
            if (reason != null && !reason.isBlank()) {
                String prev = s.getInstructorFeedback();
                s.setInstructorFeedback((prev != null && !prev.isBlank() ? prev + "\n" : "") + "[Manuelle Korrektur] " + reason);
            }
            submissionRepository.save(s);
            // Resolve any open regrade requests for this submission.
            for (com.xdata.model.RegradeRequest r : regradeRequestRepository.findBySubmissionId(submissionId)) {
                if ("OPEN".equals(r.getStatus())) {
                    r.setStatus("RESOLVED");
                    r.setInstructorResponse(reason);
                    regradeRequestRepository.save(r);
                }
            }
            return ResponseEntity.ok(s);
        }).orElse(ResponseEntity.notFound().build());
    }
}
