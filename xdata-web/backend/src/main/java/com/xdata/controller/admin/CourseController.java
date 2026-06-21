package com.xdata.controller.admin;

import com.xdata.model.Course;
import com.xdata.model.XDataUser;
import com.xdata.repository.CourseRepository;
import com.xdata.repository.UserRepository;
import com.xdata.security.CourseAccessGuard;
import com.xdata.service.AccessControlService;
import com.xdata.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;

@RestController
@Transactional
@Slf4j
@RequestMapping("/api/v1/admin/courses")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
public class CourseController {

    @GetMapping("/{id}/members")
    public ResponseEntity<List<XDataUser>> getCourseMembers(@PathVariable Integer id) {
        log.info("[DEBUG] getCourseMembers called for course id: {}", id);        Course course = courseRepository.findById(id).orElse(null);
        if (course == null) return ResponseEntity.notFound().build();
        
        courseAccessGuard.requireCourseAccess(course.getInstructorCourseId());

        return ResponseEntity.ok(userRepository.findDistinctByCourses_Id(id));
    }

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final AccessControlService accessControlService;
    private final CourseAccessGuard courseAccessGuard;
    private final AuditService auditService;
    private final com.xdata.service.core.SubmissionAnalytics submissionAnalytics;

    private List<XDataUser> courseStudents(Integer courseNumericId) {
        return userRepository.findDistinctByCourses_Id(courseNumericId).stream()
                .filter(u -> "STUDENT".equalsIgnoreCase(u.getRole()))
                .collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/{id}/gradebook")
    public ResponseEntity<?> getGradebook(@PathVariable Integer id) {
        Course course = courseRepository.findById(id).orElse(null);
        if (course == null) return ResponseEntity.notFound().build();
        courseAccessGuard.requireCourseAccess(course.getInstructorCourseId());
        return ResponseEntity.ok(submissionAnalytics.gradebook(course.getInstructorCourseId(), courseStudents(id)));
    }

    @GetMapping("/{id}/gradebook/export")
    public ResponseEntity<byte[]> exportGradebook(@PathVariable Integer id) {
        Course course = courseRepository.findById(id).orElse(null);
        if (course == null) return ResponseEntity.notFound().build();
        courseAccessGuard.requireCourseAccess(course.getInstructorCourseId());
        String csv = submissionAnalytics.gradebookCsv(course.getInstructorCourseId(), courseStudents(id));
        byte[] body = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=notenbuch_" + course.getInstructorCourseId() + ".csv")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv"))
                .body(body);
    }

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        if (accessControlService.isAdmin()) {
            return ResponseEntity.ok(courseRepository.findAll());
        } else if (accessControlService.isInstructor()) {
            List<String> courseIds = accessControlService.getUserCourseIds();
            if (!courseIds.isEmpty()) {
                return ResponseEntity.ok(courseRepository.findAllByInstructorCourseIdIn(courseIds));
            }
        }
        return ResponseEntity.ok(new ArrayList<>());
    }

    // Both admins and instructors may create courses (gated by the class-level @PreAuthorize).
    // An instructor who creates a course is auto-enrolled below so they can manage it.
    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody Course course) {
        if (course.getName() == null || course.getName().isBlank()
                || course.getInstructorCourseId() == null || course.getInstructorCourseId().isBlank()) {
            return ResponseEntity.badRequest().body("Kursname und Kurs-ID sind erforderlich.");
        }
        course.setInstructorCourseId(course.getInstructorCourseId().trim());
        if (courseRepository.findByInstructorCourseId(course.getInstructorCourseId()).isPresent()) {
            return ResponseEntity.badRequest().body("Diese Kurs-ID ist bereits vergeben.");
        }
        Course savedCourse = courseRepository.save(course);
        auditService.log("COURSE_CREATED", savedCourse.getInstructorCourseId(), "Name: " + savedCourse.getName());

        // Wenn ein Instructor einen Kurs erstellt, weise ihn ihm zu
        if (accessControlService.isInstructor()) {
            accessControlService.getCurrentUser().ifPresent(user -> {
                if (user.getCourses() == null) user.setCourses(new java.util.HashSet<>());
                user.getCourses().add(savedCourse);
                userRepository.save(user);
            });
        }
        
        return ResponseEntity.ok(savedCourse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Integer id, @RequestBody Course course) {
        if (!accessControlService.isAdmin()) {
            Course existing = courseRepository.findById(id).orElse(null);
            if (existing == null) {
                return ResponseEntity.status(403).build();
            }
            courseAccessGuard.requireCourseAccess(existing.getInstructorCourseId());
        }
        course.setId(id);
        Course saved = courseRepository.save(course);
        auditService.log("COURSE_UPDATED", saved.getInstructorCourseId(), "Name: " + saved.getName());
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Integer id) {
        if (!accessControlService.isAdmin()) {
            Course existing = courseRepository.findById(id).orElse(null);
            if (existing == null) {
                return ResponseEntity.status(403).build();
            }
            courseAccessGuard.requireCourseAccess(existing.getInstructorCourseId());
        }
        courseRepository.findById(id).ifPresent(c ->
            auditService.log("COURSE_DELETED", c.getInstructorCourseId(), "Name: " + c.getName())
        );
        courseRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
