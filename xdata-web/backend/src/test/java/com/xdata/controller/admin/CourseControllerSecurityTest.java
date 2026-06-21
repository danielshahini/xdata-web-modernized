package com.xdata.controller.admin;

import com.xdata.exception.GlobalExceptionHandler;
import com.xdata.model.Course;
import com.xdata.repository.CourseRepository;
import com.xdata.repository.UserRepository;
import com.xdata.security.CourseAccessGuard;
import com.xdata.security.JwtAuthenticationFilter;
import com.xdata.security.JwtService;
import com.xdata.security.SecurityConfiguration;
import com.xdata.service.AccessControlService;
import com.xdata.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security/authorization slice test: exercises the real Spring Security filter
 * chain, method security (@PreAuthorize), the {@link CourseAccessGuard} and the
 * {@link GlobalExceptionHandler} — without a database. Locks the K4 authorization
 * behaviour and the single consistent 403 shape.
 */
@WebMvcTest(controllers = CourseController.class)
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class, CourseAccessGuard.class, GlobalExceptionHandler.class})
class CourseControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    // security-chain collaborators (no-op for these tests; auth comes from @WithMockUser)
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private AuthenticationProvider authenticationProvider;

    // controller collaborators
    @MockBean private CourseRepository courseRepository;
    @MockBean private UserRepository userRepository;
    @MockBean private AccessControlService accessControlService;
    @MockBean private AuditService auditService;

    // the @Transactional controller needs a tx manager at invocation time
    @MockBean private PlatformTransactionManager txManager;

    // @EnableJpaAuditing pulls in a JPA metamodel bean that is empty in a web slice
    @MockBean private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMappingContext;

    private static final String COURSE_JSON = "{\"instructorCourseId\":\"C1\",\"name\":\"Test\"}";

    @BeforeEach
    void setUp() {
        when(txManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    }

    @Test
    void createCourse_unauthenticated_is_rejected() throws Exception {
        mockMvc.perform(post("/api/v1/admin/courses")
                        .contentType(MediaType.APPLICATION_JSON).content(COURSE_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void createCourse_as_student_is_forbidden_by_url_role_matcher() throws Exception {
        mockMvc.perform(post("/api/v1/admin/courses")
                        .contentType(MediaType.APPLICATION_JSON).content(COURSE_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void createCourse_as_instructor_is_denied_by_guard_with_consistent_403() throws Exception {
        when(accessControlService.isAdmin()).thenReturn(false); // guard.requireAdmin() denies

        mockMvc.perform(post("/api/v1/admin/courses")
                        .contentType(MediaType.APPLICATION_JSON).content(COURSE_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Zugriff verweigert: Sie haben nicht die erforderlichen Berechtigungen."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCourse_as_admin_succeeds() throws Exception {
        when(accessControlService.isAdmin()).thenReturn(true);
        when(accessControlService.isInstructor()).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/v1/admin/courses")
                        .contentType(MediaType.APPLICATION_JSON).content(COURSE_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instructorCourseId").value("C1"));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void getCourseMembers_denied_for_course_the_instructor_cannot_access() throws Exception {
        Course course = new Course();
        course.setInstructorCourseId("C9");
        when(courseRepository.findById(1)).thenReturn(java.util.Optional.of(course));
        when(accessControlService.canAccessCourse("C9")).thenReturn(false); // guard denies

        mockMvc.perform(get("/api/v1/admin/courses/1/members"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Zugriff verweigert: Sie haben nicht die erforderlichen Berechtigungen."));
    }
}
