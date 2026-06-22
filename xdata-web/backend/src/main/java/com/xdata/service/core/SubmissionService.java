package com.xdata.service.core;

import com.xdata.model.Question;
import com.xdata.model.Submission;
import com.xdata.model.XDataUser;
import com.xdata.repository.QuestionRepository;
import com.xdata.repository.SubmissionRepository;
import com.xdata.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final com.xdata.repository.DeadlineExtensionRepository deadlineExtensionRepository;

    public Submission createSubmission(String loginId, Integer questionId, String query) {
        XDataUser user = userRepository.findByLoginIdIgnoreCase(loginId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        Submission submission = Submission.builder()
                .user(user)
                .question(question)
                .assignment(question.getAssignment())
                .query(query)
                .submissionTime(LocalDateTime.now())
                .evaluated(false)
                .marks(0.0f)
                .build();

        return submissionRepository.save(submission);
    }

    public List<Submission> getSubmissionsByUser(String loginId) {
        return submissionRepository.findByUser_LoginId(loginId);
    }

    public float calculatePenalty(Submission submission) {
        Question q = submission.getQuestion();
        if (q == null || q.getAssignment() == null) return 0.0f;
        
        LocalDateTime deadline = q.getAssignment().getDeadline();
        LocalDateTime submissionTime = submission.getSubmissionTime();

        // Honour a per-student deadline extension if one exists for this assignment.
        if (submission.getUser() != null) {
            LocalDateTime effective = deadlineExtensionRepository
                    .findByAssignmentIdAndStudentLoginId(q.getAssignment().getId(), submission.getUser().getLoginId())
                    .map(com.xdata.model.DeadlineExtension::getExtendedDeadline)
                    .orElse(null);
            if (effective != null && (deadline == null || effective.isAfter(deadline))) {
                deadline = effective;
            }
        }

        if (deadline != null && submissionTime != null && submissionTime.isAfter(deadline)) {
            return q.getAssignment().getPenaltyPercentage() != null ? q.getAssignment().getPenaltyPercentage() / 100.0f : 0.0f;
        }
        return 0.0f;
    }

}
