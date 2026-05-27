package com.xdata.controller;

import com.xdata.model.Assignment;
import com.xdata.model.Question;
import com.xdata.model.Submission;
import com.xdata.model.XDataUser;
import com.xdata.repository.DbConnectionRepository;
import com.xdata.repository.QuestionRepository;
import com.xdata.repository.SubmissionRepository;
import com.xdata.service.core.AssignmentService;
import com.xdata.service.AccessControlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final AccessControlService accessControlService;
    private final QuestionRepository questionRepository;
    private final SubmissionRepository submissionRepository;
    private final DbConnectionRepository dbConnectionRepository;

    @GetMapping
    public ResponseEntity<List<Assignment>> getAssignments(@RequestParam(required = false) String courseId) {
        if (courseId != null) {
            return ResponseEntity.ok(assignmentService.getAssignmentsByCourse(courseId));
        }
        return ResponseEntity.ok(assignmentService.getAllAssignments());
    }

    @PostMapping
    public ResponseEntity<?> createAssignment(@RequestBody Assignment assignment, @RequestParam String courseId) {
        log.info("Request to create assignment: {} for course: {}", assignment.getName(), courseId);
        if (!accessControlService.canAccessCourse(courseId)) {
            log.warn("Permission denied for course: {}", courseId);
            return ResponseEntity.status(403).body("Keine Berechtigung für diesen Kurs.");
        }
        
        if (assignment.getConnection() == null || assignment.getConnection().getId() == null) {
            return ResponseEntity.badRequest().body("Eine Datenbankverbindung ist zwingend erforderlich.");
        }

        // Resolve connection from DB
        return dbConnectionRepository.findById(assignment.getConnection().getId()).map(conn -> {
            assignment.setConnection(conn);
            try {
                return ResponseEntity.ok(assignmentService.createAssignment(assignment, courseId));
            } catch (Exception e) {
                log.error("Error creating assignment: {}", e.getMessage());
                return ResponseEntity.status(400).body(e.getMessage());
            }
        }).orElse(ResponseEntity.badRequest().body("Die gewählte Datenbankverbindung wurde nicht gefunden."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAssignment(@PathVariable Integer id, @RequestBody Assignment assignmentData) {
        log.info("Request to update assignment ID: {}", id);
        return assignmentService.getAssignmentById(id).map(existing -> {
            if (!accessControlService.canAccessCourse(existing.getCourse().getInstructorCourseId())) {
                return ResponseEntity.status(403).build();
            }
            existing.setName(assignmentData.getName());
            existing.setDeadline(assignmentData.getDeadline());
            existing.setDefaultSchemaId(assignmentData.getDefaultSchemaId());
            
            if (assignmentData.getConnection() != null && assignmentData.getConnection().getId() != null) {
                dbConnectionRepository.findById(assignmentData.getConnection().getId()).ifPresent(existing::setConnection);
            }
            
            existing.setLateSubmissionAllowed(assignmentData.getLateSubmissionAllowed());
            existing.setPenaltyPercentage(assignmentData.getPenaltyPercentage());
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
        
        StringBuilder csv = new StringBuilder("StudentId,Username,TotalMarksPercentage,XP,");
        for (Question q : questions) {
            String cleanName = q.getName().replace(",", " ");
            csv.append(cleanName).append(" (Score),");
            csv.append(cleanName).append(" (Attempts),");
            csv.append(cleanName).append(" (Last Submission),");
        }
        csv.append("\n");
        
        Map<String, List<Submission>> subsByUser = allSubmissions.stream()
                .collect(Collectors.groupingBy(s -> s.getUser().getLoginId()));
        
        for (Map.Entry<String, List<Submission>> entry : subsByUser.entrySet()) {
            String loginId = entry.getKey();
            XDataUser user = entry.getValue().get(0).getUser();
            String username = user.getUsername();
            Integer xp = user.getXp();
            
            double totalPossible = questions.stream().mapToDouble(Question::getMarks).sum();
            double achievedPoints = questions.stream().mapToDouble(q -> {
                 return entry.getValue().stream()
                        .filter(s -> s.getQuestion().getId().equals(q.getId()))
                        .mapToDouble(s -> s.getMarks() * q.getMarks())
                        .max().orElse(0.0);
            }).sum();
            
            double percentage = totalPossible > 0 ? (achievedPoints / totalPossible) * 100 : 0;
            
            StringBuilder row = new StringBuilder(String.format("%s,%s,%.2f%%,%d,", loginId, username, percentage, xp != null ? xp : 0));
            
            for (Question q : questions) {
                List<Submission> qSubs = entry.getValue().stream()
                        .filter(s -> s.getQuestion().getId().equals(q.getId()))
                        .collect(Collectors.toList());
                
                double best = qSubs.stream().mapToDouble(Submission::getMarks).max().orElse(0.0);
                long attempts = qSubs.size();
                String lastSub = qSubs.stream()
                        .map(s -> s.getSubmissionTime().toString())
                        .max(String::compareTo).orElse("-");
                
                row.append(String.format("%.2f%%,%d,%s,", best * 100, attempts, lastSub));
            }
            row.append("\n");
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
