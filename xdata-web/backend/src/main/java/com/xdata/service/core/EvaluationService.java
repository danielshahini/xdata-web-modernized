package com.xdata.service.core;

import com.xdata.eval.DryRunRequest;
import com.xdata.eval.SubmissionEvaluator;
import com.xdata.model.Submission;
import com.xdata.partialmarking.core.PartialMarkParameters;
import com.xdata.repository.SubmissionRepository;
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

/**
 * Async/timeout entry points and playground helpers around the SubmissionEvaluation
 * module. The full grading orchestration lives in {@link SubmissionEvaluator}; this
 * service only schedules it and adapts the playground calls.
 */
@Service
@RequiredArgsConstructor
public class EvaluationService {

    private static final Logger log = LoggerFactory.getLogger(EvaluationService.class);
    private final SubmissionEvaluator submissionEvaluator;
    private final SubmissionRepository submissionRepository;
    private final JdbcTemplate jdbcTemplate;

    public void evaluateQuestion(Integer assignmentId, Integer questionId, String courseId) {
        log.info("Starting evaluation for assignment: {}, question: {}, course: {}", assignmentId, questionId, courseId);
        List<Submission> submissions = submissionRepository.findByQuestion_Id(questionId);
        log.info("Evaluating {} submissions for question ID: {}", submissions.size(), questionId);
        for (Submission submission : submissions) {
            submissionEvaluator.gradeNow(submission.getId());
        }
    }

    @Async
    public void evaluateSubmissionAsync(Integer submissionId) {
        log.info("Starting async evaluation for submission: {}", submissionId);
        try {
            CompletableFuture.runAsync(() -> submissionEvaluator.gradeNow(submissionId))
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

    public Object calculatePartialMarksLive(String pattern, String student, Integer schemaId, PartialMarkParameters params) {
        return submissionEvaluator.dryRun(new DryRunRequest(pattern, student, schemaId, params)).markInfo();
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
