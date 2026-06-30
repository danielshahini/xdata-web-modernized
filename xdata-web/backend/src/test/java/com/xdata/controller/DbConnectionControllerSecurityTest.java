package com.xdata.controller;

import com.xdata.exception.GlobalExceptionHandler;
import com.xdata.repository.CourseRepository;
import com.xdata.repository.DbConnectionRepository;
import com.xdata.security.CourseAccessGuard;
import com.xdata.security.JwtAuthenticationFilter;
import com.xdata.security.JwtService;
import com.xdata.security.SecurityConfiguration;
import com.xdata.service.AccessControlService;
import com.xdata.service.AuditService;
import com.xdata.service.DatabaseService;
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

@WebMvcTest(controllers = DbConnectionController.class)
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class, CourseAccessGuard.class, GlobalExceptionHandler.class})
class DbConnectionControllerSecurityTest {

    private static final String DENIED = "Access denied: you do not have the required permissions.";

    @Autowired private MockMvc mockMvc;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private AuthenticationProvider authenticationProvider;
    @MockBean private JpaMetamodelMappingContext jpaMappingContext;
    @MockBean private PlatformTransactionManager txManager;
    @MockBean private AccessControlService accessControl;
    @MockBean private DbConnectionRepository dbConnectionRepository;
    @MockBean private CourseRepository courseRepository;
    @MockBean private AuditService auditService;
    @MockBean private DatabaseService databaseService;

    @BeforeEach
    void setUp() {
        when(txManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void connections_area_is_off_limits_to_students() throws Exception {
        // /api/v1/instructor/** URL matcher + class @PreAuthorize both exclude STUDENT
        mockMvc.perform(get("/api/v1/instructor/connections")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void instructor_sees_their_connections() throws Exception {
        when(accessControl.isAdmin()).thenReturn(false);
        when(accessControl.getUserCourseIds()).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/instructor/connections")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void test_all_connections_is_admin_only() throws Exception {
        when(accessControl.isAdmin()).thenReturn(false); // guard.requireAdmin() denies
        mockMvc.perform(get("/api/v1/instructor/connections/test"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(DENIED));
    }
}
