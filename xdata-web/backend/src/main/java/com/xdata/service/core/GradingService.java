package com.xdata.service.core;

import com.google.gson.Gson;
import com.xdata.model.Question;
import com.xdata.model.Submission;
import com.xdata.model.XDataUser;
import com.xdata.repository.SubmissionRepository;
import com.xdata.partialmarking.core.CanonicalizeQuery;
import com.xdata.partialmarking.core.MarkInfo;
import com.xdata.partialmarking.core.PartialMarker;
import com.xdata.partialmarking.core.PartialMarkParameters;
import com.xdata.partialmarking.core.QueryStructure;
import com.xdata.partialmarking.core.TableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.xdata.service.LmsIntegrationService;
import com.xdata.service.SmtSolverService;
import com.xdata.service.DatasetGenerationService;
import com.xdata.service.TestExecutionService;
import com.xdata.service.MetadataService;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GradingService {

    private final SubmissionRepository submissionRepository;
    private final LmsIntegrationService lmsIntegrationService;
    private final SmtSolverService smtSolverService;
    private final DatasetGenerationService datasetGenerationService;
    private final TestExecutionService testExecutionService;
    private final SubmissionService submissionService;
    private final SimpMessagingTemplate messagingTemplate;
    private final MetadataService metadataService;
    private final com.xdata.service.SqlSandboxService sqlSandboxService;
    private final com.xdata.repository.UserRepository userRepository;
    private final Gson gson = new Gson();

    public MarkInfo calculatePartialMarksForPlayground(String patternQuery, String studentQuery, Integer schemaId, PartialMarkParameters params) throws Exception {
        log.info("Calculating partial marks for playground with schemaId: {}", schemaId);
        TableMap tableMap = metadataService.getNewTableMap(schemaId);
        
        QueryStructure instructorQS = new QueryStructure(patternQuery, tableMap);
        QueryStructure studentQS = new QueryStructure(studentQuery, tableMap);

        CanonicalizeQuery.canonicalize(instructorQS);
        CanonicalizeQuery.canonicalize(studentQS);

        if (params == null) params = new PartialMarkParameters();
        return PartialMarker.getMarks(instructorQS, studentQS, params);
    }

    @Transactional
    public void evaluateAllSubmissionsForQuestion(Integer questionId) {
        List<Submission> submissions = submissionRepository.findByQuestion_Id(questionId);
        log.info("Evaluating {} submissions for question ID: {}", submissions.size(), questionId);
        for (Submission submission : submissions) {
            evaluateSubmission(submission.getId());
        }
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "leaderboard", allEntries = true)
    public void evaluateSubmission(Integer submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        Question question = submission.getQuestion();
        String instructorQuery = question.getInstructorQuery();
        String studentQuery = submission.getQuery();
        Integer schemaId = question.getAssignment().getDefaultSchemaId();

        log.info("Evaluating submission {} for question {} using XData Partial Marking...", submissionId, question.getName());

        boolean isCorrect = false;
        
        try {
            if (instructorQuery.trim().equalsIgnoreCase(studentQuery.trim())) {
                isCorrect = true;
            } else {
                List<String> testData = datasetGenerationService.generateDatasetFromQuery(instructorQuery, schemaId);
                if (!testData.isEmpty()) {
                    isCorrect = testExecutionService.compareQueries(instructorQuery, studentQuery, testData, schemaId);
                } else {
                    // Fallback to SMT
                    String smtConstraints = datasetGenerationService.generateEquivalenceConstraints(instructorQuery, studentQuery, schemaId);
                    if (smtConstraints != null) {
                        isCorrect = smtSolverService.verifyEquivalence(smtConstraints);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Equivalence check failed: {}", e.getMessage());
        }

        if (!isCorrect) {
            // Last resort: compare on the assigned DB connection
            isCorrect = compareQueriesOnAssignedDb(instructorQuery, studentQuery, question.getAssignment().getConnection());
        }

        float marks = 0.0f;
        MarkInfo markInfo = null;
        try {
            markInfo = calculateXDataPartialMarks(question, studentQuery);
            if (markInfo != null) {
                submission.setMarkInfoJson(gson.toJson(markInfo));
            }
        } catch (Exception e) {
            log.error("Partial marking calculation failed: {}", e.getMessage());
        }

        if (isCorrect) {
            marks = 1.0f;
            submission.setVerifiedCorrect(true);
        } else {
            if (markInfo != null) {
                marks = (float) (markInfo.getMarks() / markInfo.getMaxMarks());
            } else {
                marks = 0.0f;
            }
            submission.setVerifiedCorrect(false);
        }

        submission.setEvaluated(true);
        float penalty = submissionService.calculatePenalty(submission);
        marks = marks * (1.0f - penalty);
        
        submission.setMarks(marks);
        updateUserXP(submission, marks);
        submissionRepository.save(submission);
        notifyUser(submission);
        lmsIntegrationService.syncGradeToLms(submission);
    }

    private void updateUserXP(Submission submission, float currentMarks) {
        try {
            XDataUser user = submission.getUser();
            if (user == null) return;
            
            Integer qId = submission.getQuestion().getId();
            List<Submission> previousSubmissions = submissionRepository.findByUser_LoginIdAndQuestion_IdOrderBySubmissionTimeDesc(user.getLoginId(), qId);
            
            float previousBest = 0.0f;
            for (Submission s : previousSubmissions) {
                if (s.getId().equals(submission.getId())) continue;
                if (s.getEvaluated() != null && s.getEvaluated() && s.getMarks() > previousBest) {
                    previousBest = s.getMarks();
                }
            }
            
            if (currentMarks > previousBest) {
                float questionMaxMarks = submission.getQuestion().getMarks() != null ? submission.getQuestion().getMarks() : 10.0f;
                int xpGain = (int) ((currentMarks - previousBest) * questionMaxMarks * 10);
                if (xpGain > 0) {
                    user.setXp((user.getXp() != null ? user.getXp() : 0) + xpGain);
                    userRepository.save(user);
                    log.info("User {} gained {} XP! Total XP: {}", user.getLoginId(), xpGain, user.getXp());
                }
            }
        } catch (Exception e) {
            log.error("Failed to update user XP: {}", e.getMessage());
        }
    }

    private MarkInfo calculateXDataPartialMarks(Question question, String studentQuery) throws Exception {
        Integer schemaId = question.getAssignment().getDefaultSchemaId();
        TableMap tableMap = metadataService.getNewTableMap(schemaId);
        
        QueryStructure instructorQS = new QueryStructure(question.getInstructorQuery(), tableMap);
        QueryStructure studentQS = new QueryStructure(studentQuery, tableMap);

        CanonicalizeQuery.canonicalize(instructorQS);
        CanonicalizeQuery.canonicalize(studentQS);

        return PartialMarker.getMarks(instructorQS, studentQS, question.getPartialMarkParameters());
    }

    private void notifyUser(Submission submission) {
        try {
            if (submission.getUser() != null) {
                String loginId = submission.getUser().getLoginId();
                messagingTemplate.convertAndSend("/topic/grading/" + loginId, submission);
            }
        } catch (Exception e) {
            log.warn("Could not send WebSocket notification: {}", e.getMessage());
        }
    }

    public boolean compareQueriesOnAssignedDb(String instructorQuery, String studentQuery, com.xdata.model.DbConnection connection) {
        if (connection == null || connection.getUrl() == null) {
            log.warn("Keine Datenbankverbindung für den Vergleich vorhanden.");
            return false;
        }

        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                connection.getUrl(), connection.getUser(), connection.getPassword())) {

            List<List<Object>> instructorResults = executeWithTimeout(conn, instructorQuery, 5);
            List<List<Object>> studentResults = executeWithTimeout(conn, studentQuery, 5);

            if (instructorResults.size() != studentResults.size()) return false;

            instructorResults.sort((a, b) -> Objects.toString(a).compareTo(Objects.toString(b)));
            studentResults.sort((a, b) -> Objects.toString(a).compareTo(Objects.toString(b)));

            return instructorResults.equals(studentResults);
        } catch (Exception e) {
            log.warn("Vergleich auf Ziel-DB fehlgeschlagen: {}", e.getMessage());
            return false;
        }
    }

    private List<List<Object>> executeWithTimeout(java.sql.Connection conn, String query, int timeoutSeconds) throws SQLException {
        try {
            sqlSandboxService.validateQuery(query);
        } catch (RuntimeException e) {
            throw new SQLException(e.getMessage());
        }
        List<List<Object>> results = new ArrayList<>();
        try (java.sql.Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout(timeoutSeconds);
            stmt.setMaxRows(1000);
            try (ResultSet rs = stmt.executeQuery(query)) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (rs.next()) {
                    List<Object> row = new ArrayList<>(columnCount);
                    for (int i = 1; i <= columnCount; i++) {
                        row.add(rs.getObject(i));
                    }
                    results.add(row);
                }
            }
        }
        return results;
    }
}
