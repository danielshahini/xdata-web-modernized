package com.xdata.service.core;

import com.xdata.dto.AssignmentAnalyticsDTO;
import com.xdata.dto.AssignmentSummaryDTO;
import com.xdata.model.Assignment;
import com.xdata.model.Question;
import com.xdata.model.Submission;
import com.xdata.model.XDataUser;
import com.xdata.repository.AssignmentRepository;
import com.xdata.repository.QuestionRepository;
import com.xdata.repository.SubmissionRepository;
import com.xdata.repository.UserRepository;
import com.xdata.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.xdata.service.DatabaseService;
import com.xdata.service.SqlValidationService;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final QuestionRepository questionRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final SqlValidationService sqlValidationService;
    private final com.xdata.service.SqlSandboxService sqlSandboxService;
    private final DatabaseService databaseService;

    @Value("${spring.datasource.url}")
    private String systemDbUrl;

    public void validateConnection(com.xdata.model.DbConnection connection) throws Exception {
        if (connection == null || connection.getUrl() == null) {
            throw new IllegalArgumentException("Keine gültige Datenbankverbindung angegeben.");
        }

        if (connection.getUrl().equalsIgnoreCase(systemDbUrl)) {
            log.warn("Sicherheitswarnung: Aufgabe versucht die System-Datenbank zu nutzen!");
            throw new Exception("Die System-Datenbank darf aus Sicherheitsgründen nicht für Aufgaben verwendet werden.");
        }

        // Teste die Verbindung aktiv
        try (Connection conn = databaseService.getConnection(connection)) {
            if (!conn.isValid(5)) {
                throw new Exception("Die Datenbankverbindung konnte nicht validiert werden (Timeout).");
            }
        } catch (Exception e) {
            throw new Exception("Fehler beim Verbindungsaufbau: " + e.getMessage());
        }
    }

    @Transactional
    public Assignment createAssignment(Assignment assignment, String courseId) throws Exception {
        com.xdata.model.Course course = courseRepository.findByInstructorCourseId(courseId)
                .orElseThrow(() -> new Exception("Kurs nicht gefunden: " + courseId));
        
        assignment.setCourse(course);
        validateConnection(assignment.getConnection());
        
        return assignmentRepository.save(assignment);
    }

    public List<Assignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    public List<Assignment> getAssignmentsByCourse(String courseId) {
        return assignmentRepository.findByCourse_InstructorCourseId(courseId);
    }

    public List<Assignment> getAssignmentsByCourses(List<String> courseIds) {
        return assignmentRepository.findByCourse_InstructorCourseIdIn(courseIds);
    }

    public Optional<Assignment> getAssignmentById(Integer id) {
        return assignmentRepository.findById(id);
    }

    @Transactional
    public Assignment saveAssignment(Assignment assignment) {
        return assignmentRepository.save(assignment);
    }

    @Transactional
    public void deleteAssignment(Integer id) {
        assignmentRepository.deleteById(id);
    }

    public void validateQuestionQuery(Question question) throws Exception {
        Assignment assignment = question.getAssignment();
        if (assignment == null || assignment.getConnection() == null) {
            throw new Exception("Assignment oder Datenbankverbindung fehlt.");
        }

        String query = question.getInstructorQuery();
        sqlSandboxService.validateQuery(query);

        com.xdata.model.DbConnection connection = assignment.getConnection();

        try (Connection conn = databaseService.getConnection(connection)) {
            try (java.sql.PreparedStatement pstmt = conn.prepareStatement(query)) {
                // Erfolgreich
            }
        } catch (Exception e) {
            throw new Exception("SQL-Validierung fehlgeschlagen: " + e.getMessage());
        }
    }

    public List<Question> getQuestionsByAssignment(Integer assignmentId) {
        return questionRepository.findByAssignment_Id(assignmentId);
    }
    
    public Optional<Question> getQuestionById(Integer id) {
        return questionRepository.findById(id);
    }
    
    @Transactional
    public Question saveQuestion(Question question) {
        return questionRepository.save(question);
    }
    
    @Transactional
    public void deleteQuestion(Integer id) {
        questionRepository.deleteById(id);
    }

    @Transactional
    public Assignment duplicateAssignment(Integer id) {
        Assignment original = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        Assignment copy = new Assignment();
        copy.setName(original.getName() + " (Kopie)");
        copy.setCourse(original.getCourse());
        copy.setConnection(original.getConnection());
        copy.setDefaultSchemaId(original.getDefaultSchemaId());
        copy.setSeedSql(original.getSeedSql());
        copy.setDeadline(original.getDeadline());
        copy.setSoftDeadline(original.getSoftDeadline());
        copy.setPenaltyPercentage(original.getPenaltyPercentage());
        copy.setLateSubmissionAllowed(original.getLateSubmissionAllowed());
        copy.setPublishedDate(original.getPublishedDate());
        
        Assignment savedCopy = assignmentRepository.save(copy);
        
        List<Question> questions = questionRepository.findByAssignment_Id(id);
        for (Question q : questions) {
            Question qCopy = new Question();
            qCopy.setAssignment(savedCopy);
            qCopy.setName(q.getName());
            qCopy.setInstructorQuery(q.getInstructorQuery());
            qCopy.setMarks(q.getMarks());
            qCopy.setTags(q.getTags());
            qCopy.setHints(q.getHints());
            qCopy.setDifficulty(q.getDifficulty());
            qCopy.setDescription(q.getDescription());
            qCopy.setPartialMarkParameters(q.getPartialMarkParameters());
            questionRepository.save(qCopy);
        }
        
        return savedCopy;
    }

    public AssignmentSummaryDTO getAssignmentSummary(Integer assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        List<Question> questions = questionRepository.findByAssignment_Id(assignmentId);
        List<Submission> allSubmissions = submissionRepository.findByQuestion_Assignment_Id(assignmentId);
        
        List<XDataUser> students = userRepository.findDistinctByCourses_InstructorCourseId(assignment.getCourseId());
        
        List<AssignmentSummaryDTO.StudentResultDTO> results = students.stream().map(student -> {
            List<AssignmentSummaryDTO.QuestionMarkDTO> qMarks = questions.stream().map(q -> {
                Optional<Submission> bestSub = allSubmissions.stream()
                        .filter(s -> s.getUser().getLoginId().equals(student.getLoginId()))
                        .filter(s -> s.getQuestion().getId().equals(q.getId()))
                        .max((s1, s2) -> Float.compare(s1.getMarks(), s2.getMarks()));
                
                return new AssignmentSummaryDTO.QuestionMarkDTO(
                    q.getId(),
                    q.getName(),
                    bestSub.map(Submission::getMarks).orElse(0.0f)
                );
            }).collect(Collectors.toList());
            
            float total = (float) qMarks.stream().mapToDouble(AssignmentSummaryDTO.QuestionMarkDTO::getMarks).sum();
            
            return new AssignmentSummaryDTO.StudentResultDTO(
                student.getLoginId(),
                student.getUsername(),
                total,
                qMarks
            );
        }).collect(Collectors.toList());
        
        return new AssignmentSummaryDTO(assignmentId, assignment.getName(), results);
    }

    public AssignmentAnalyticsDTO getAssignmentAnalytics(Integer assignmentId) {
        List<Question> questions = questionRepository.findByAssignment_Id(assignmentId);
        List<Submission> allSubmissions = submissionRepository.findByQuestion_Assignment_Id(assignmentId);
        
        List<AssignmentAnalyticsDTO.QuestionAnalyticsDTO> qAnalytics = questions.stream().map(q -> {
            List<Submission> qSubs = allSubmissions.stream()
                    .filter(s -> s.getQuestion().getId().equals(q.getId()))
                    .collect(Collectors.toList());
            
            long uniqueStudents = qSubs.stream().map(s -> s.getUser().getLoginId()).distinct().count();
            double avgMarks = qSubs.stream().mapToDouble(Submission::getMarks).average().orElse(0.0);
            long perfect = qSubs.stream().filter(s -> s.getMarks() >= 1.0f).count();
            
            return new AssignmentAnalyticsDTO.QuestionAnalyticsDTO(
                q.getId(),
                q.getName(),
                qSubs.size(),
                (int) uniqueStudents,
                (float) avgMarks,
                (int) perfect,
                new ArrayList<>()
            );
        }).collect(Collectors.toList());
        
        return new AssignmentAnalyticsDTO(assignmentId, qAnalytics);
    }
}
