package com.xdata.controller.admin;

import com.xdata.exception.GlobalExceptionHandler;
import com.xdata.model.Assignment;
import com.xdata.model.Course;
import com.xdata.model.Question;
import com.xdata.security.CourseAccessGuard;
import com.xdata.security.JwtAuthenticationFilter;
import com.xdata.security.JwtService;
import com.xdata.security.SecurityConfiguration;
import com.xdata.service.AccessControlService;
import com.xdata.service.core.AssignmentService;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = QuestionController.class)
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class, CourseAccessGuard.class, GlobalExceptionHandler.class})
class QuestionControllerSecurityTest {

    private static final String DENIED = "Access denied: you do not have the required permissions.";

    @Autowired private MockMvc mockMvc;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private AuthenticationProvider authenticationProvider;
    @MockBean private JpaMetamodelMappingContext jpaMappingContext;
    @MockBean private PlatformTransactionManager txManager;
    @MockBean private AccessControlService accessControl;
    @MockBean private AssignmentService assignmentService;

    @BeforeEach
    void setUp() {
        when(txManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    }

    private static Question questionInCourse(String courseId) {
        Course course = new Course();
        course.setInstructorCourseId(courseId);
        Assignment assignment = new Assignment();
        assignment.setCourse(course);
        Question question = new Question();
        question.setAssignment(assignment);
        return question;
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void createQuestion_forbidden_for_students_by_class_security() throws Exception {
        mockMvc.perform(post("/api/v1/admin/questions")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getQuestion_denied_when_student_cannot_access_its_course() throws Exception {
        when(assignmentService.getQuestionById(1)).thenReturn(Optional.of(questionInCourse("C1")));
        when(accessControl.canAccessCourse("C1")).thenReturn(false);

        mockMvc.perform(get("/api/v1/admin/questions/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(DENIED));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void getQuestion_allowed_when_instructor_can_access_its_course() throws Exception {
        when(assignmentService.getQuestionById(1)).thenReturn(Optional.of(questionInCourse("C1")));
        when(accessControl.canAccessCourse("C1")).thenReturn(true);

        mockMvc.perform(get("/api/v1/admin/questions/1")).andExpect(status().isOk());
    }
}
