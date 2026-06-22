package com.xdata.controller.admin;

import com.xdata.exception.GlobalExceptionHandler;
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
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class, CourseAccessGuard.class, GlobalExceptionHandler.class})
class UserControllerSecurityTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private JwtService jwtService;                 // also a controller dep
    @MockBean private UserDetailsService userDetailsService; // also a controller dep
    @MockBean private AuthenticationProvider authenticationProvider;
    @MockBean private JpaMetamodelMappingContext jpaMappingContext;
    @MockBean private PlatformTransactionManager txManager;
    @MockBean private AccessControlService accessControl;
    @MockBean private UserRepository userRepository;
    @MockBean private CourseRepository courseRepository;
    @MockBean private PasswordEncoder passwordEncoder;
    @MockBean private AuditService auditService;

    @BeforeEach
    void setUp() {
        when(txManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void user_admin_area_is_off_limits_to_students() throws Exception {
        // /api/v1/admin/users/** URL matcher excludes STUDENT
        mockMvc.perform(get("/api/v1/admin/users")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void impersonation_is_admin_only() throws Exception {
        // @PreAuthorize("hasRole('ADMIN')") on the method denies instructors
        mockMvc.perform(post("/api/v1/admin/users/someone/impersonate"))
                .andExpect(status().isForbidden());
    }
}
