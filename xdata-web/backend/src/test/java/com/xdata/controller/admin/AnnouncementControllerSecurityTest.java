package com.xdata.controller.admin;

import com.xdata.exception.GlobalExceptionHandler;
import com.xdata.repository.CourseRepository;
import com.xdata.repository.UserRepository;
import com.xdata.security.CourseAccessGuard;
import com.xdata.security.JwtAuthenticationFilter;
import com.xdata.security.JwtService;
import com.xdata.security.SecurityConfiguration;
import com.xdata.service.AccessControlService;
import com.xdata.service.AnnouncementService;
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

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnnouncementController.class)
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class, CourseAccessGuard.class, GlobalExceptionHandler.class})
class AnnouncementControllerSecurityTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private AuthenticationProvider authenticationProvider;
    @MockBean private JpaMetamodelMappingContext jpaMappingContext;
    @MockBean private AccessControlService accessControl;
    @MockBean private AnnouncementService announcementService;
    @MockBean private CourseRepository courseRepository;
    @MockBean private UserRepository userRepository;

    @Test
    void announcements_require_authentication() throws Exception {
        mockMvc.perform(get("/api/v1/announcements")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void students_may_read_announcements() throws Exception {
        when(accessControl.getUserCourseIds()).thenReturn(List.of());
        when(announcementService.getAnnouncementsForUser(List.of())).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/announcements")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void students_may_not_create_announcements() throws Exception {
        mockMvc.perform(post("/api/v1/announcements?courseId=C1")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }
}
