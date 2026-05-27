package com.xdata.controller;

import com.xdata.model.Assignment;
import com.xdata.model.Question;
import com.xdata.model.Submission;
import com.xdata.repository.QuestionRepository;
import com.xdata.repository.SubmissionRepository;
import com.xdata.repository.UserRepository;
import com.xdata.service.AccessControlService;
import com.xdata.service.core.AssignmentService;
import com.xdata.service.core.EvaluationService;
import com.xdata.service.core.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

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
    private final EvaluationService evaluationService;
    private final SubmissionService submissionService;
    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        return accessControlService.getCurrentUser().map(user -> {
            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("studentName", user.getUsername());
            dashboard.put("xp", user.getXp() != null ? user.getXp() : 0);
            int level = (int) (Math.floor(Math.sqrt((user.getXp() != null ? user.getXp() : 0) / 100.0)) + 1);
            dashboard.put("level", level);
            dashboard.put("nextLevelXp", (int) (Math.pow(level, 2) * 100));
            dashboard.put("currentLevelXp", (int) (Math.pow(level - 1, 2) * 100));
            
            List<String> courseIds = accessControlService.getUserCourseIds();
            List<Assignment> assignments = assignmentService.getAssignmentsByCourses(courseIds).stream()
                    .filter(a -> a.getPublishedDate() == null || a.getPublishedDate().isBefore(LocalDateTime.now()))
                    .collect(Collectors.toList());
            
            List<Map<String, Object>> assignmentData = assignments.stream().map(a -> {
                Map<String, Object> data = new HashMap<>();
                data.put("assignmentId", a.getId());
                data.put("name", a.getName());
                data.put("deadline", a.getDeadline());
                
                List<Question> questions = questionRepository.findByAssignment_Id(a.getId());
                data.put("totalQuestions", questions.size());
                
                double totalMarks = questions.stream().mapToDouble(q -> q.getMarks() != null ? q.getMarks() : 0.0).sum();
                data.put("totalMarks", totalMarks);

                double achievedMarks = 0.0;
                long solvedCount = 0;

                for (Question q : questions) {
                    List<Submission> userSubmissions = submissionRepository.findByUser_LoginIdAndQuestion_IdOrderBySubmissionTimeDesc(user.getLoginId(), q.getId());
                    if (!userSubmissions.isEmpty()) {
                        double bestMarks = userSubmissions.stream().mapToDouble(Submission::getMarks).max().orElse(0.0);
                        achievedMarks += bestMarks * (q.getMarks() != null ? q.getMarks() : 0.0);
                        if (bestMarks >= 1.0) {
                            solvedCount++;
                        }
                    }
                }

                data.put("solvedQuestions", solvedCount);
                data.put("achievedMarks", achievedMarks);
                data.put("percentage", totalMarks > 0 ? (achievedMarks / totalMarks) * 100 : 0);
                
                return data;
            }).collect(Collectors.toList());
            
            dashboard.put("assignments", assignmentData);
            return ResponseEntity.ok(dashboard);
        }).orElse(ResponseEntity.status(401).build());
    }

    @GetMapping("/assignments/{assignmentId}/questions")
    public ResponseEntity<List<Question>> getQuestions(@PathVariable Integer assignmentId) {
        return assignmentService.getAssignmentById(assignmentId).map(assignment -> {
            if (!accessControlService.canAccessCourse(assignment.getCourseId())) {
                return ResponseEntity.status(403).<List<Question>>build();
            }
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
            if (assignment == null || !accessControlService.canAccessCourse(assignment.getCourseId())) {
                return ResponseEntity.status(403).body("Zugriff verweigert für diesen Kurs.");
            }
            
            if (assignment.getPublishedDate() != null && assignment.getPublishedDate().isAfter(LocalDateTime.now())) {
                return ResponseEntity.status(403).body("Diese Aufgabe ist noch nicht veröffentlicht.");
            }
            
            Submission submission = submissionService.createSubmission(
                    accessControlService.getCurrentUserLoginId(),
                    questionId,
                    query
            );
            
            evaluationService.evaluateSubmissionAsync(submission.getId());
            
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

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard(@RequestParam(required = false) String courseId) {
        String effectiveCourseId = courseId;
        if (effectiveCourseId == null) {
            effectiveCourseId = accessControlService.getUserCourseId();
        }
        
        if (effectiveCourseId == null) {
            return ResponseEntity.badRequest().body("Kein Kurs ausgewählt oder zugewiesen.");
        }

        if (!accessControlService.canAccessCourse(effectiveCourseId)) {
            return ResponseEntity.status(403).body("Keine Berechtigung für diesen Kurs.");
        }

        return ResponseEntity.ok(submissionService.getLeaderboard(effectiveCourseId, userRepository));
    }
}
