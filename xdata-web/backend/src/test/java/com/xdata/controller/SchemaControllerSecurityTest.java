package com.xdata.controller;

import com.xdata.exception.GlobalExceptionHandler;
import com.xdata.repository.CourseRepository;
import com.xdata.security.CourseAccessGuard;
import com.xdata.security.JwtAuthenticationFilter;
import com.xdata.security.JwtService;
import com.xdata.security.SecurityConfiguration;
import com.xdata.service.AccessControlService;
import com.xdata.service.core.SchemaService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SchemaController.class)
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class, CourseAccessGuard.class, GlobalExceptionHandler.class})
class SchemaControllerSecurityTest {

    private static final String DENIED = "Zugriff verweigert: Sie haben nicht die erforderlichen Berechtigungen.";

    @Autowired private MockMvc mockMvc;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private AuthenticationProvider authenticationProvider;
    @MockBean private JpaMetamodelMappingContext jpaMappingContext;
    @MockBean private PlatformTransactionManager txManager;
    @MockBean private AccessControlService accessControl;
    @MockBean private SchemaService schemaService;
    @MockBean private CourseRepository courseRepository;

    @BeforeEach
    void setUp() {
        when(txManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void schemasByCourse_denied_when_student_cannot_access_course() throws Exception {
        when(accessControl.canAccessCourse("C1")).thenReturn(false);
        mockMvc.perform(get("/api/v1/schemas/course/C1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(DENIED));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void schemasByCourse_allowed_when_student_can_access_course() throws Exception {
        when(accessControl.canAccessCourse("C1")).thenReturn(true);
        when(schemaService.getSchemasByCourse("C1")).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/schemas/course/C1")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void deleteSchema_forbidden_for_students_by_method_security() throws Exception {
        // @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')") on the method denies students
        mockMvc.perform(delete("/api/v1/schemas/1")).andExpect(status().isForbidden());
    }
}
