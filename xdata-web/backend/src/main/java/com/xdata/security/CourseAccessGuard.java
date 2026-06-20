package com.xdata.security;

import com.xdata.service.AccessControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * The single seam for enforcing course/role authorization in controllers. Each
 * method throws {@link AccessDeniedException} on denial, which the
 * {@code GlobalExceptionHandler} renders as one consistent 403 JSON body — instead
 * of the scattered {@code if (!check) return ResponseEntity.status(403)...} idioms
 * with their divergent error shapes. See CONTEXT.md → "CourseAccess".
 */
@Component
@RequiredArgsConstructor
public class CourseAccessGuard {

    private final AccessControlService accessControl;

    /** Allow only ADMIN. */
    public void requireAdmin() {
        if (!accessControl.isAdmin()) {
            throw denied();
        }
    }

    /** Allow INSTRUCTOR or ADMIN. */
    public void requireInstructorOrAdmin() {
        if (!accessControl.isInstructor() && !accessControl.isAdmin()) {
            throw denied();
        }
    }

    /** Allow callers who may access the given course (ADMIN, or a member of it). */
    public void requireCourseAccess(String courseId) {
        if (!accessControl.canAccessCourse(courseId)) {
            throw denied();
        }
    }

    private AccessDeniedException denied() {
        return new AccessDeniedException("Keine Berechtigung für diese Aktion.");
    }
}
