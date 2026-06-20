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

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        return accessControlService.getCurrentUser().map(user -> {
            List<String> courseIds = accessControlService.getUserCourseIds();
            return ResponseEntity.ok(submissionAnalytics.dashboard(user, courseIds));
        }).orElse(ResponseEntity.status(401).build());
    }

    @GetMapping("/assignments/{assignmentId}/questions")
    public ResponseEntity<List<Question>> getQuestions(@PathVariable Integer assignmentId) {
        return assignmentService.getAssignmentById(assignmentId).map(assignment -> {
            courseAccessGuard.requireCourseAccess(assignment.getCourseId());
            if (assignment.getPublishedDate() != null && assignment.getPublishedDate().isAfter(LocalDateTime.now())) {
                return ResponseEntity.status(403).<List<Question>>build();
            }
            return ResponseEntity.ok(questionRepository.findByAssignment_Id(assignmentId));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitSolution(@RequestBody Map<String, Object> body) {
        Object qIdObj = body.get("questionId");
        Integer questionId = (qIdObj instanceof Integer) ? (Integer) qIdObj : Integer.parseInt(qIdObj.toString());
        String query = (String) body.get("query");
        
        return questionRepository.findById(questionId).map(question -> {
            Assignment assignment = question.getAssignment();
            if (assignment == null) {
                return ResponseEntity.status(403).body("Zugriff verweigert für diesen Kurs.");
            }
            courseAccessGuard.requireCourseAccess(assignment.getCourseId());
            
            if (assignment.getPublishedDate() != null && assignment.getPublishedDate().isAfter(LocalDateTime.now())) {
                return ResponseEntity.status(403).body("Diese Aufgabe ist noch nicht veröffentlicht.");
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

    @GetMapping("/submissions")
    public ResponseEntity<List<Submission>> getMySubmissions() {
        String loginId = accessControlService.getCurrentUserLoginId();
        return ResponseEntity.ok(submissionRepository.findByUser_LoginId(loginId));
    }

    @GetMapping("/questions/{questionId}/attempts")
    public ResponseEntity<List<Submission>> getAttempts(@PathVariable Integer questionId) {
        String loginId = accessControlService.getCurrentUserLoginId();
        return ResponseEntity.ok(submissionRepository.findByUser_LoginIdAndQuestion_IdOrderBySubmissionTimeDesc(loginId, questionId));
    }

}
