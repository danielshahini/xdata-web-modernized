package com.xdata.controller;

import com.xdata.model.Question;
import com.xdata.model.Submission;
import com.xdata.repository.QuestionRepository;
import com.xdata.repository.SubmissionRepository;
import com.xdata.security.CourseAccessGuard;
import com.xdata.service.core.EvaluationService;
import com.xdata.service.PlagiarismService;
import com.xdata.partialmarking.core.MarkInfo;
import com.xdata.partialmarking.core.PartialMarkParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/evaluation")
@RequiredArgsConstructor
@Slf4j
public class EvaluationController {
    private final EvaluationService evaluationService;
    private final QuestionRepository questionRepository;
    private final SubmissionRepository submissionRepository;
    private final CourseAccessGuard courseAccessGuard;
    private final PlagiarismService plagiarismService;

    @PostMapping("/start/{questionId}")
    public ResponseEntity<String> startEvaluation(@PathVariable Integer questionId) {
        courseAccessGuard.requireInstructorOrAdmin();
        return questionRepository.findById(questionId).map(q -> {
            evaluationService.evaluateQuestionAsync(q.getAssignment().getId(), q.getId(), q.getAssignment().getCourseId());
            return ResponseEntity.ok("Bewertung gestartet");
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/submissions/{questionId}")
    public ResponseEntity<List<Submission>> getSubmissions(@PathVariable Integer questionId) {
        return ResponseEntity.ok(submissionRepository.findByQuestion_Id(questionId));
    }

    @PostMapping("/playground/partial-marking")
    public ResponseEntity<?> simulateGrading(@RequestBody Map<String, Object> body) {
        String instructorQuery = (String) body.get("patternQuery");
        String studentQuery = (String) body.get("studentQuery");
        Integer schemaId = getInteger(body, "schemaId");
        
        Map<String, Object> weightsMap = (Map<String, Object>) body.get("params");
        PartialMarkParameters params = new PartialMarkParameters();
        if (weightsMap != null) {
            params.setPredicate(getWeight(weightsMap, "predicate"));
            params.setProjection(getWeight(weightsMap, "projection"));
            params.setRelation(getWeight(weightsMap, "relation"));
            params.setJoins(getWeight(weightsMap, "joins"));
            params.setGroupBy(getWeight(weightsMap, "groupBy"));
            params.setHavingClause(getWeight(weightsMap, "havingClause"));
            params.setOrderBy(getWeight(weightsMap, "orderBy"));
            params.setAggregates(getWeight(weightsMap, "aggregates"));
            params.setDistinct(getWeight(weightsMap, "distinct"));
            params.setSetOperators(getWeight(weightsMap, "setOperators"));
            params.setWhereSubQueries(getWeight(weightsMap, "whereSubQueries"));
            params.setFromSubQueries(getWeight(weightsMap, "fromSubQueries"));
            params.setOuterQuery(getWeight(weightsMap, "outerQuery"));
            params.setSubQConnective(getWeight(weightsMap, "subQConnective"));
            params.setMaxPartialMarks(getWeight(weightsMap, "maxPartialMarks"));
        }

        try {
            MarkInfo markInfo = (MarkInfo) evaluationService.calculatePartialMarksLive(instructorQuery, studentQuery, schemaId, params);
            return ResponseEntity.ok(markInfo);
        } catch (Exception e) {
            log.error("Simulation error: ", e);
            return ResponseEntity.status(400).body(Map.of("message", "Fehler bei der Simulation: " + e.getMessage()));
        }
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        if (val instanceof String && !((String) val).isEmpty()) {
            try {
                return (int) Double.parseDouble((String) val);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private int getWeight(Map<String, Object> map, String key) {
        Integer val = getInteger(map, key);
        return val != null ? val : 1;
    }

    @GetMapping("/plagiarism/{assignmentId}")
    public ResponseEntity<?> checkPlagiarism(@PathVariable Integer assignmentId, @RequestParam(defaultValue = "0.8") double threshold) {
        courseAccessGuard.requireInstructorOrAdmin();
        return ResponseEntity.ok(plagiarismService.checkAssignment(assignmentId, threshold));
    }
    
    @PostMapping("/submissions/{submissionId}/feedback")
    public ResponseEntity<?> addFeedback(@PathVariable Integer submissionId, @RequestBody Map<String, String> body) {
        courseAccessGuard.requireInstructorOrAdmin();
        return submissionRepository.findById(submissionId).map(s -> {
            s.setInstructorFeedback(body.get("feedback"));
            submissionRepository.save(s);
            return ResponseEntity.ok(s);
        }).orElse(ResponseEntity.notFound().build());
    }
}
