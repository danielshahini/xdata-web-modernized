package com.xdata.controller;

import com.xdata.model.Assignment;
import com.xdata.model.Question;
import com.xdata.model.Submission;
import com.xdata.model.XDataUser;
import com.xdata.repository.DbConnectionRepository;
import com.xdata.repository.QuestionRepository;
import com.xdata.repository.SubmissionRepository;
import com.xdata.service.core.AssignmentService;
import com.xdata.service.core.SubmissionAnalytics;
import com.xdata.security.CourseAccessGuard;
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
    private final SubmissionAnalytics submissionAnalytics;
    private final CourseAccessGuard courseAccessGuard;
    private final DbConnectionRepository dbConnectionRepository;
    private final com.xdata.repository.DeadlineExtensionRepository deadlineExtensionRepository;

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
        courseAccessGuard.requireCourseAccess(courseId);


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
            courseAccessGuard.requireCourseAccess(existing.getCourse().getInstructorCourseId());
            existing.setName(assignmentData.getName());
            existing.setDeadline(assignmentData.getDeadline());
            existing.setDefaultSchemaId(assignmentData.getDefaultSchemaId());
            
            if (assignmentData.getConnection() != null && assignmentData.getConnection().getId() != null) {
                dbConnectionRepository.findById(assignmentData.getConnection().getId()).ifPresent(existing::setConnection);
            }
            
            existing.setLateSubmissionAllowed(assignmentData.getLateSubmissionAllowed());
            existing.setPenaltyPercentage(assignmentData.getPenaltyPercentage());
            existing.setPublishedDate(assignmentData.getPublishedDate());
            existing.setMaxAttempts(assignmentData.getMaxAttempts());
            if (assignmentData.getGradesReleased() != null) {
                existing.setGradesReleased(assignmentData.getGradesReleased());
            }

            return ResponseEntity.ok(assignmentService.saveAssignment(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Assignment> getAssignment(@PathVariable Integer id) {
        return assignmentService.getAssignmentById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // --- Per-student deadline extensions (#5b) — instructor/admin only ---

    @GetMapping("/{id}/extensions")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<?> listExtensions(@PathVariable Integer id) {
        return assignmentService.getAssignmentById(id).map(a -> {
            courseAccessGuard.requireCourseAccess(a.getCourse().getInstructorCourseId());
            return ResponseEntity.ok(deadlineExtensionRepository.findByAssignmentId(id));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/extensions")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<?> setExtension(@PathVariable Integer id, @RequestBody java.util.Map<String, String> body) {
        return assignmentService.getAssignmentById(id).map(a -> {
            courseAccessGuard.requireCourseAccess(a.getCourse().getInstructorCourseId());
            String studentLoginId = body.get("studentLoginId");
            String deadlineStr = body.get("extendedDeadline");
            if (studentLoginId == null || studentLoginId.isBlank() || deadlineStr == null || deadlineStr.isBlank()) {
                return ResponseEntity.badRequest().body("studentLoginId und extendedDeadline sind erforderlich.");
            }
            java.time.LocalDateTime when;
            try {
                when = java.time.LocalDateTime.parse(deadlineStr);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Ungültiges Datumsformat.");
            }
            com.xdata.model.DeadlineExtension ext = deadlineExtensionRepository
                    .findByAssignmentIdAndStudentLoginId(id, studentLoginId)
                    .orElseGet(() -> com.xdata.model.DeadlineExtension.builder()
                            .assignmentId(id).studentLoginId(studentLoginId).build());
            ext.setExtendedDeadline(when);
            return ResponseEntity.ok(deadlineExtensionRepository.save(ext));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/extensions/{studentLoginId}")
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<?> deleteExtension(@PathVariable Integer id, @PathVariable String studentLoginId) {
        return assignmentService.getAssignmentById(id).map(a -> {
            courseAccessGuard.requireCourseAccess(a.getCourse().getInstructorCourseId());
            deadlineExtensionRepository.deleteByAssignmentIdAndStudentLoginId(id, studentLoginId);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<List<Question>> getQuestions(@PathVariable Integer id) {
        return ResponseEntity.ok(assignmentService.getQuestionsByAssignment(id));
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<String> exportResults(@PathVariable Integer id) {
        courseAccessGuard.requireInstructorOrAdmin();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=results_" + id + ".csv")
                .body(submissionAnalytics.resultsCsv(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAssignment(@PathVariable Integer id) {
        courseAccessGuard.requireInstructorOrAdmin();
        assignmentService.deleteAssignment(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<Assignment> duplicateAssignment(@PathVariable Integer id) {
        courseAccessGuard.requireInstructorOrAdmin();
        return ResponseEntity.ok(assignmentService.duplicateAssignment(id));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<?> getAssignmentStats(@PathVariable Integer id) {
        courseAccessGuard.requireInstructorOrAdmin();
        return ResponseEntity.ok(submissionAnalytics.assignmentStats(id));
    }
}
