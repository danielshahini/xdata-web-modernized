package com.xdata.controller;

import com.xdata.exception.GlobalExceptionHandler;
import com.xdata.repository.QuestionRepository;
import com.xdata.repository.SubmissionRepository;
import com.xdata.security.CourseAccessGuard;
import com.xdata.security.JwtAuthenticationFilter;
import com.xdata.security.JwtService;
import com.xdata.security.SecurityConfiguration;
import com.xdata.service.AccessControlService;
import com.xdata.service.PlagiarismService;
import com.xdata.service.core.EvaluationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security slice covering the {@code requireInstructorOrAdmin} role gate via the
 * real filter chain + {@link CourseAccessGuard} + {@link GlobalExceptionHandler}.
 */
@WebMvcTest(controllers = EvaluationController.class)
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class, CourseAccessGuard.class, GlobalExceptionHandler.class})
class EvaluationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private AuthenticationProvider authenticationProvider;
    @MockBean private JpaMetamodelMappingContext jpaMappingContext;

    @MockBean private EvaluationService evaluationService;
    @MockBean private QuestionRepository questionRepository;
    @MockBean private SubmissionRepository submissionRepository;
    @MockBean private AccessControlService accessControlService;
    @MockBean private PlagiarismService plagiarismService;
    @MockBean private com.xdata.repository.RegradeRequestRepository regradeRequestRepository;
    @MockBean private com.xdata.service.core.SubmissionComparisonService comparisonService;

    @Test
    void startEvaluation_unauthenticated_is_rejected() throws Exception {
        mockMvc.perform(post("/api/v1/evaluation/start/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void startEvaluation_as_student_is_denied_by_role_guard() throws Exception {
        // passes the URL matcher (students may reach /evaluation/**) but the guard denies
        mockMvc.perform(post("/api/v1/evaluation/start/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Zugriff verweigert: Sie haben nicht die erforderlichen Berechtigungen."));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void startEvaluation_as_instructor_passes_the_guard() throws Exception {
        when(accessControlService.isInstructor()).thenReturn(true);
        when(questionRepository.findById(1)).thenReturn(java.util.Optional.empty());

        // past the guard: the unknown question yields 404, proving authorization succeeded
        mockMvc.perform(post("/api/v1/evaluation/start/1"))
                .andExpect(status().isNotFound());
    }
}
