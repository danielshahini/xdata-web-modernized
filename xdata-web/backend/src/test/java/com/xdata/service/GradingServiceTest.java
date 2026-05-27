package com.xdata.service;

import com.xdata.model.Question;
import com.xdata.model.Assignment;
import com.xdata.model.Submission;
import com.xdata.model.XDataUser;
import com.xdata.repository.SubmissionRepository;
import com.xdata.service.core.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GradingServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private SmtSolverService smtSolverService;
    @Mock
    private DatasetGenerationService datasetGenerationService;
    @Mock
    private SubmissionService submissionService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private LmsIntegrationService lmsIntegrationService;
    @Mock
    private MetadataService metadataService;
    @Mock
    private TestExecutionService testExecutionService;
    @Mock
    private SqlSandboxService sqlSandboxService;

    @InjectMocks
    private GradingService gradingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testEvaluateSubmission_Correct() {
        // Arrange
        Integer submissionId = 1;
        Assignment assignment = new Assignment();
        assignment.setId(1);
        
        Question question = new Question();
        question.setId(1);
        question.setName("Test Question");
        question.setInstructorQuery("SELECT * FROM users");
        question.setAssignment(assignment);
        
        XDataUser student = new XDataUser();
        student.setLoginId("student1");

        Submission submission = new Submission();
        submission.setId(submissionId);
        submission.setQuestion(question);
        submission.setQuery("SELECT * FROM users");
        submission.setUser(student);
        
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(datasetGenerationService.generateEquivalenceConstraints(anyString(), anyString(), any())).thenReturn("unsat");
        when(smtSolverService.verifyEquivalence(anyString())).thenReturn(true);
        when(submissionService.calculatePenalty(any())).thenReturn(0.0f);

        // Act
        gradingService.evaluateSubmission(submissionId);

        // Assert
        assertTrue(submission.getVerifiedCorrect());
        assertEquals(1.0f, submission.getMarks());
        verify(submissionRepository).save(submission);
    }
}
