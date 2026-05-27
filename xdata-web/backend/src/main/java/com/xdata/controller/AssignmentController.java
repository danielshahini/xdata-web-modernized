package com.xdata.controller;

import com.xdata.model.Assignment;
import com.xdata.model.Course;
import com.xdata.model.DbConnection;
import com.xdata.model.Question;
import com.xdata.model.Submission;
import com.xdata.repository.CourseRepository;
import com.xdata.repository.DbConnectionRepository;
import com.xdata.repository.SubmissionRepository;
import com.xdata.repository.QuestionRepository;
import com.xdata.repository.UserRepository;
import com.xdata.service.AccessControlService;
import com.xdata.service.core.AssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Comparator;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final CourseRepository courseRepository;
    private final AccessControlService accessControlService;
    private final SubmissionRepository submissionRepository;
    private final DbConnectionRepository dbConnectionRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Assignment>> getAllAssignments() {
        if (accessControlService.isAdmin()) {
            return ResponseEntity.ok(assignmentService.getAllAssignments());
        }
        List<String> courseIds = accessControlService.getUserCourseIds();
        return ResponseEntity.ok(assignmentService.getAssignmentsByCourses(courseIds));
    }

    @PostMapping
    public ResponseEntity<?> createAssignment(@RequestBody Assignment assignment, @RequestParam String courseId) {
        log.info("Request to create assignment: {} for course: {}", assignment, courseId);
        if (!accessControlService.canAccessCourse(courseId)) {
            return ResponseEntity.status(403).build();
        }

        if (assignment.getConnection() == null || assignment.getConnection().getId() == null) {
            return ResponseEntity.badRequest().body("Eine Datenbankverbindung ist für neue Aufgaben zwingend erforderlich.");
        }

        Optional<DbConnection> dbConnOpt = dbConnectionRepository.findById(assignment.getConnection().getId());
        if (!dbConnOpt.isPresent()) {
            return ResponseEntity.badRequest().body("Die angegebene Datenbankverbindung wurde nicht gefunden.");
        }
        DbConnection dbConn = dbConnOpt.get();

        if (!accessControlService.canAccessCourse(dbConn.getCourse() != null ? dbConn.getCourse().getInstructorCourseId() : null)) {
            return ResponseEntity.status(403).body("Keine Berechtigung für diese Datenbankverbindung.");
        }

        Optional<Course> courseOpt = courseRepository.findByInstructorCourseId(courseId);
        if (!courseOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        assignment.setCourse(courseOpt.get());
        assignment.setConnection(dbConn);
        try {
            assignmentService.validateConnection(dbConn);
            return ResponseEntity.ok(assignmentService.saveAssignment(assignment));
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Verbindungsfehler zur Ziel-Datenbank: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAssignment(@PathVariable Integer id, @RequestBody Assignment assignmentData) {
        return assignmentService.getAssignmentById(id).map(existing -> {
            if (!accessControlService.canAccessCourse(existing.getCourseId())) {
                return ResponseEntity.status(403).build();
            }

            if (assignmentData.getConnection() != null && assignmentData.getConnection().getId() != null) {
                Optional<DbConnection> dbConnOpt = dbConnectionRepository.findById(assignmentData.getConnection().getId());
                if (dbConnOpt.isPresent()) {
                    DbConnection dbConn = dbConnOpt.get();
                    if (!accessControlService.canAccessCourse(dbConn.getCourse() != null ? dbConn.getCourse().getInstructorCourseId() : null)) {
                        return ResponseEntity.status(403).body("Keine Berechtigung für diese Datenbankverbindung.");
                    }
                    try {
                        assignmentService.validateConnection(dbConn);
                        existing.setConnection(dbConn);
                    } catch (Exception e) {
                        return ResponseEntity.status(400).body("Verbindungsfehler zur Ziel-Datenbank: " + e.getMessage());
                    }
                }
            }

            existing.setName(assignmentData.getName());
            existing.setDefaultSchemaId(assignmentData.getDefaultSchemaId());
            existing.setDeadline(assignmentData.getDeadline());
            existing.setPenaltyPercentage(assignmentData.getPenaltyPercentage());
            existing.setLateSubmissionAllowed(assignmentData.getLateSubmissionAllowed());
            existing.setPublishedDate(assignmentData.getPublishedDate());

            return ResponseEntity.ok(assignmentService.saveAssignment(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Assignment> getAssignment(@PathVariable Integer id) {
        return assignmentService.getAssignmentById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<List<Question>> getQuestions(@PathVariable Integer id) {
        return ResponseEntity.ok(assignmentService.getQuestionsByAssignment(id));
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<String> exportResults(@PathVariable Integer id) {
        if (!accessControlService.isInstructor() && !accessControlService.isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        
        Assignment assignment = assignmentService.getAssignmentById(id).orElseThrow();
        List<Question> questions = questionRepository.findByAssignment_Id(id);
        List<Submission> allSubmissions = submissionRepository.findByQuestion_Assignment_Id(id);
        
        StringBuilder csv = new StringBuilder("StudentId,Username,TotalMarksPercentage,");
        csv.append(questions.stream().map(Question::getName).collect(Collectors.joining(","))).append("\n");
        
        Map<String, List<Submission>> subsByUser = allSubmissions.stream()
                .collect(Collectors.groupingBy(s -> s.getUser().getLoginId()));
        
        for (Map.Entry<String, List<Submission>> entry : subsByUser.entrySet()) {
            String loginId = entry.getKey();
            String username = entry.getValue().get(0).getUser().getUsername();
            
            double totalAchieved = 0;
            double totalPossible = questions.stream().mapToDouble(Question::getMarks).sum();
            
            StringBuilder row = new StringBuilder(String.format("%s,%s,", loginId, username));
            List<String> qMarks = questions.stream().map(q -> {
                double best = entry.getValue().stream()
                        .filter(s -> s.getQuestion().getId().equals(q.getId()))
                        .mapToDouble(Submission::getMarks)
                        .max().orElse(0.0);
                return String.format("%.2f", best * 100);
            }).collect(Collectors.toList());
            
            double achievedPoints = questions.stream().mapToDouble(q -> {
                 return entry.getValue().stream()
                        .filter(s -> s.getQuestion().getId().equals(q.getId()))
                        .mapToDouble(s -> s.getMarks() * q.getMarks())
                        .max().orElse(0.0);
            }).sum();
            
            double percentage = totalPossible > 0 ? (achievedPoints / totalPossible) * 100 : 0;
            row.append(String.format("%.2f%%,", percentage));
            row.append(String.join(",", qMarks)).append("\n");
            csv.append(row);
        }
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=results_" + id + ".csv")
                .body(csv.toString());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAssignment(@PathVariable Integer id) {
        if (!accessControlService.isInstructor() && !accessControlService.isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        assignmentService.deleteAssignment(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<Assignment> duplicateAssignment(@PathVariable Integer id) {
        if (!accessControlService.isInstructor() && !accessControlService.isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(assignmentService.duplicateAssignment(id));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<?> getAssignmentStats(@PathVariable Integer id) {
        if (!accessControlService.isInstructor() && !accessControlService.isAdmin()) {
            return ResponseEntity.status(403).build();
        }

        List<Question> questions = questionRepository.findByAssignment_Id(id);
        List<Map<String, Object>> stats = questions.stream().map(q -> {
            Map<String, Object> qStats = new HashMap<>();
            qStats.put("questionId", q.getId());
            qStats.put("name", q.getName());

            List<Submission> submissions = submissionRepository.findByQuestion_Id(q.getId());
            long totalAttempts = submissions.size();
            long uniqueUsers = submissions.stream().map(s -> s.getUser().getLoginId()).distinct().count();
            
            long solvedUsers = submissions.stream()
                    .filter(s -> s.getMarks() >= 1.0)
                    .map(s -> s.getUser().getLoginId())
                    .distinct().count();
            
            double avgMarks = submissions.stream()
                    .mapToDouble(Submission::getMarks)
                    .average().orElse(0.0);

            qStats.put("totalAttempts", totalAttempts);
            qStats.put("uniqueUsers", uniqueUsers);
            qStats.put("solvedUsers", solvedUsers);
            qStats.put("successRate", uniqueUsers > 0 ? (double) solvedUsers / uniqueUsers : 0.0);
            qStats.put("avgMarks", avgMarks);
            
            return qStats;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(stats);
    }
}
