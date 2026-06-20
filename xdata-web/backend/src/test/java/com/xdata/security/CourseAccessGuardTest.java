package com.xdata.security;

import com.xdata.service.AccessControlService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CourseAccessGuardTest {

    private final AccessControlService accessControl = mock(AccessControlService.class);
    private final CourseAccessGuard guard = new CourseAccessGuard(accessControl);

    @Test
    void requireAdmin_passes_for_admin() {
        when(accessControl.isAdmin()).thenReturn(true);
        assertThatCode(guard::requireAdmin).doesNotThrowAnyException();
    }

    @Test
    void requireAdmin_denies_non_admin() {
        when(accessControl.isAdmin()).thenReturn(false);
        assertThatThrownBy(guard::requireAdmin).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void requireInstructorOrAdmin_passes_for_instructor() {
        when(accessControl.isInstructor()).thenReturn(true);
        assertThatCode(guard::requireInstructorOrAdmin).doesNotThrowAnyException();
    }

    @Test
    void requireInstructorOrAdmin_passes_for_admin() {
        when(accessControl.isInstructor()).thenReturn(false);
        when(accessControl.isAdmin()).thenReturn(true);
        assertThatCode(guard::requireInstructorOrAdmin).doesNotThrowAnyException();
    }

    @Test
    void requireInstructorOrAdmin_denies_student() {
        when(accessControl.isInstructor()).thenReturn(false);
        when(accessControl.isAdmin()).thenReturn(false);
        assertThatThrownBy(guard::requireInstructorOrAdmin).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void requireCourseAccess_passes_when_allowed() {
        when(accessControl.canAccessCourse("C1")).thenReturn(true);
        assertThatCode(() -> guard.requireCourseAccess("C1")).doesNotThrowAnyException();
    }

    @Test
    void requireCourseAccess_denies_when_not_allowed() {
        when(accessControl.canAccessCourse("C1")).thenReturn(false);
        assertThatThrownBy(() -> guard.requireCourseAccess("C1")).isInstanceOf(AccessDeniedException.class);
    }
}
