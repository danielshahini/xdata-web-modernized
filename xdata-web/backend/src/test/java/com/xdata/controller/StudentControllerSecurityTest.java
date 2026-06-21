package com.xdata.controller;

import com.xdata.exception.GlobalExceptionHandler;
import com.xdata.repository.QuestionRepository;
import com.xdata.repository.SubmissionRepository;
import com.xdata.security.CourseAccessGuard;
import com.xdata.security.JwtAuthenticationFilter;
import com.xdata.security.JwtService;
import com.xdata.security.SecurityConfiguration;
import com.xdata.service.AccessControlService;
import com.xdata.service.core.AssignmentService;
import com.xdata.service.core.EvaluationService;
import com.xdata.service.core.SubmissionAnalytics;
import com.xdata.service.core.SubmissionService;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StudentController.class)
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class, CourseAccessGuard.class, GlobalExceptionHandler.class})
class StudentControllerSecurityTest {

    private static final String DENIED = "Zugriff verweigert: Sie haben nicht die erforderlichen Berechtigungen.";

    @Autowired private MockMvc mockMvc;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private AuthenticationProvider authenticationProvider;
    @MockBean private JpaMetamodelMappingContext jpaMappingContext;
    @MockBean private PlatformTransactionManager txManager;
    @MockBean private AccessControlService accessControl;
    @MockBean private AssignmentService assignmentService;
    @MockBean private QuestionRepository questionRepository;
    @MockBean private SubmissionRepository submissionRepository;
    @MockBean private SubmissionAnalytics submissionAnalytics;
    @MockBean private EvaluationService evaluationService;
    @MockBean private SubmissionService submissionService;
    @MockBean private com.xdata.service.DatabaseService databaseService;
    @MockBean private com.xdata.service.SqlSandboxService sqlSandboxService;
    @MockBean private com.xdata.repository.RegradeRequestRepository regradeRequestRepository;
    @MockBean private com.xdata.repository.UserRepository userRepository;

    @BeforeEach
    void setUp() {
        when(txManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    }

    @Test
    void dashboard_requires_authentication() throws Exception {
        // An anonymous request (no/expired JWT) now resolves to 401 via the
        // AuthenticationEntryPoint, so the SPA can tell "log in again" apart from
        // a genuine "logged in but forbidden" 403.
        mockMvc.perform(get("/api/v1/student/dashboard")).andExpect(status().isUnauthorized());
    }
}
