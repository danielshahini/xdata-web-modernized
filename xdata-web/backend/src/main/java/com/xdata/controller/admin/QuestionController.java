package com.xdata.controller.admin;

import com.xdata.model.Question;
import com.xdata.service.core.AssignmentService;
import com.xdata.security.CourseAccessGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/v1/admin/questions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
@Slf4j
@Transactional
public class QuestionController {

    private final AssignmentService assignmentService;
    private final CourseAccessGuard courseAccessGuard;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<Question> getQuestionById(@PathVariable Integer id) {
        return assignmentService.getQuestionById(id)
                .map(question -> {
                    String courseId = question.getAssignment().getCourse() != null ? 
                                     question.getAssignment().getCourse().getInstructorCourseId() : null;
                    courseAccessGuard.requireCourseAccess(courseId);
                    return ResponseEntity.ok(question);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createQuestion(@RequestBody Question question, @RequestParam(required = false) Integer assignmentId) {
        log.info("Request to create question. Body: {}", question);
        if (question == null) {
            return ResponseEntity.badRequest().body("Frage-Daten fehlen.");
        }
        log.info("Question detail: name={}, marks={}, query={}, assignmentId={}", 
            question.getName(), question.getMarks(), question.getInstructorQuery(), question.getAssignmentId());
        
        Integer actualAssignmentId = assignmentId != null ? assignmentId : question.getAssignmentId();
        log.info("Resolved assignmentId: {}", actualAssignmentId);
        
        if (actualAssignmentId == null) {
            return ResponseEntity.badRequest().body("assignmentId ist erforderlich.");
        }

        return assignmentService.getAssignmentById(actualAssignmentId).map(assignment -> {
            String courseId = assignment.getCourse() != null ? assignment.getCourse().getInstructorCourseId() : null;
            courseAccessGuard.requireCourseAccess(courseId);
            question.setAssignment(assignment);
            try {
                assignmentService.validateQuestionQuery(question);
                Question saved = assignmentService.saveQuestion(question);
                log.info("Successfully saved question with ID: {}", saved.getId());
                return ResponseEntity.ok(saved);
            } catch (Exception e) {
                log.error("Fehler bei SQL-Validierung: {}", e.getMessage());
                return ResponseEntity.status(400).body("Fehler in der Musterlösung: " + e.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuestion(@PathVariable Integer id, @RequestBody Question questionData) {
        log.info("Request to update question ID {}. Body: {}", id, questionData);
        return assignmentService.getQuestionById(id)
                .map(existing -> {
                    String courseId = existing.getAssignment().getCourse() != null ? 
                                     existing.getAssignment().getCourse().getInstructorCourseId() : null;
                    courseAccessGuard.requireCourseAccess(courseId);
                    
                    existing.setName(questionData.getName());
                    existing.setInstructorQuery(questionData.getInstructorQuery());
                    existing.setMarks(questionData.getMarks());
                    existing.setTags(questionData.getTags());
                    existing.setPartialMarkParameters(questionData.getPartialMarkParameters());

                    try {
                        assignmentService.validateQuestionQuery(existing);
                        Question saved = assignmentService.saveQuestion(existing);
                        log.info("Successfully updated question with ID: {}", saved.getId());
                        return ResponseEntity.ok(saved);
                    } catch (Exception e) {
                        log.error("Fehler bei SQL-Update-Validierung: {}", e.getMessage());
                        return ResponseEntity.status(400).body("Fehler in der Musterlösung: " + e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Integer id) {
        return assignmentService.getQuestionById(id)
                .map(question -> {
                    String courseId = question.getAssignment().getCourse() != null ? 
                                     question.getAssignment().getCourse().getInstructorCourseId() : null;
                    courseAccessGuard.requireCourseAccess(courseId);
                    assignmentService.deleteQuestion(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
