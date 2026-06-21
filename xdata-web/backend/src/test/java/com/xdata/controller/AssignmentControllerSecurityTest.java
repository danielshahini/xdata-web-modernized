package com.xdata.controller;

import com.xdata.exception.GlobalExceptionHandler;
import com.xdata.repository.DbConnectionRepository;
import com.xdata.security.CourseAccessGuard;
import com.xdata.security.JwtAuthenticationFilter;
import com.xdata.security.JwtService;
import com.xdata.security.SecurityConfiguration;
import com.xdata.service.AccessControlService;
import com.xdata.service.core.AssignmentService;
import com.xdata.service.core.SubmissionAnalytics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AssignmentController.class)
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class, CourseAccessGuard.class, GlobalExceptionHandler.class})
class AssignmentControllerSecurityTest {

    private static final String DENIED = "Zugriff verweigert: Sie haben nicht die erforderlichen Berechtigungen.";

    @Autowired private MockMvc mockMvc;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private AuthenticationProvider authenticationProvider;
    @MockBean private JpaMetamodelMappingContext jpaMappingContext;
    @MockBean private PlatformTransactionManager txManager;
    @MockBean private AccessControlService accessControl;
    @MockBean private AssignmentService assignmentService;
    @MockBean private SubmissionAnalytics submissionAnalytics;
    @MockBean private DbConnectionRepository dbConnectionRepository;

    @BeforeEach
    void setUp() {
        when(txManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    }

    @Test
    void deleteAssignment_unauthenticated_is_rejected() throws Exception {
        mockMvc.perform(delete("/api/v1/assignments/1")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void deleteAssignment_as_student_denied_by_role_guard() throws Exception {
        mockMvc.perform(delete("/api/v1/assignments/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(DENIED));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void createAssignment_denied_for_course_the_instructor_cannot_access() throws Exception {
        when(accessControl.canAccessCourse("C1")).thenReturn(false);
        mockMvc.perform(post("/api/v1/assignments?courseId=C1")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(DENIED));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void listAssignments_is_allowed_for_students() throws Exception {
        when(assignmentService.getAllAssignments()).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/assignments")).andExpect(status().isOk());
    }
}
