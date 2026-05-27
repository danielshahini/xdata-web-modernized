package com.xdata.service.core;

import com.xdata.service.DatasetGenerationService;
import com.xdata.service.SmtSolverService;
import com.xdata.service.TestExecutionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EvaluationService {
    
    private static final Logger log = LoggerFactory.getLogger(EvaluationService.class);
    private final GradingService gradingService;
    private final DatasetGenerationService datasetGenerationService;
    private final SmtSolverService smtSolverService;
    private final TestExecutionService testExecutionService;
    private final JdbcTemplate jdbcTemplate;

    public void evaluateQuestion(Integer assignmentId, Integer questionId, String courseId) {
        log.info("Starting evaluation for assignment: {}, question: {}, course: {}", assignmentId, questionId, courseId);
        gradingService.evaluateAllSubmissionsForQuestion(questionId);
    }

    @Async
    public void evaluateSubmissionAsync(Integer submissionId) {
        log.info("Starting async evaluation for submission: {}", submissionId);
        try {
            CompletableFuture.runAsync(() -> gradingService.evaluateSubmission(submissionId))
                    .orTimeout(60, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        log.error("Evaluation for submission {} timed out or failed: {}", submissionId, ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            log.error("Error in async evaluation for submission {}: {}", submissionId, e.getMessage());
        }
    }

    @Async
    public CompletableFuture<Void> evaluateQuestionAsync(Integer assignmentId, Integer questionId, String courseId) {
        log.info("Starting async evaluation for question: {}", questionId);
        try {
            CompletableFuture.runAsync(() -> evaluateQuestion(assignmentId, questionId, courseId))
                    .orTimeout(120, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        log.error("Evaluation for question {} timed out or failed: {}", questionId, ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            log.error("Error in async evaluation: {}", e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    public boolean verifyEquivalence(String q1, String q2, Integer schemaId) {
        if (q1 == null || q2 == null) return false;
        if (q1.trim().equalsIgnoreCase(q2.trim())) return true;

        log.info("Verifying equivalence using test data approach for queries.");
        List<String> testData = datasetGenerationService.generateDatasetFromQuery(q1, schemaId);
        if (testData.isEmpty()) {
            log.warn("No test data generated, falling back to partial marking similarity check.");
            try {
                com.xdata.partialmarking.core.MarkInfo mi = (com.xdata.partialmarking.core.MarkInfo) calculatePartialMarksLive(q1, q2, schemaId, null);
                return mi != null && mi.getPercentage() >= 100.0;
            } catch (Exception e) {
                log.error("Partial marking check failed: ", e);
                return false;
            }
        }
        
        return testExecutionService.compareQueries(q1, q2, testData, schemaId);
    }

    public Object calculatePartialMarksLive(String pattern, String student, Integer schemaId, com.xdata.partialmarking.core.PartialMarkParameters params) throws Exception {
        return gradingService.calculatePartialMarksForPlayground(pattern, student, schemaId, params);
    }

    public List<Map<String, Object>> executeStudentQuery(String query, Integer schemaId) {
        log.info("Executing playground query for schema: {}", schemaId);
        return jdbcTemplate.query(query, (rs, rowNum) -> {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnName(i), rs.getObject(i));
            }
            return row;
        });
    }
}
