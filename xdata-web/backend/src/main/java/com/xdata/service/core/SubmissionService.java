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
        
        if (deadline != null && submissionTime != null && submissionTime.isAfter(deadline)) {
            return q.getAssignment().getPenaltyPercentage() != null ? q.getAssignment().getPenaltyPercentage() / 100.0f : 0.0f;
        }
        return 0.0f;
    }

    public List<Map<String, Object>> getLeaderboard(String courseId, UserRepository userRepository) {
        List<XDataUser> students = userRepository.findDistinctByCourses_InstructorCourseId(courseId);
        return students.stream().map(student -> {
            List<Submission> studentSubs = submissionRepository.findByUser_LoginId(student.getLoginId());
            double totalMarks = studentSubs.stream()
                    .filter(s -> s.getQuestion() != null)
                    .collect(Collectors.groupingBy(s -> s.getQuestion().getId(),
                            Collectors.maxBy(Comparator.comparing(Submission::getMarks))))
                    .values().stream()
                    .mapToDouble(opt -> opt.map(Submission::getMarks).orElse(0.0f))
                    .sum();
            
            Map<String, Object> entry = new HashMap<>();
            entry.put("username", student.getUsername());
            String loginId = student.getLoginId();
            String anonymousId = loginId.length() > 3 ? loginId.substring(0, 2) + "***" + loginId.substring(loginId.length() - 1) : "***";
            entry.put("loginId", anonymousId);
            entry.put("xp", student.getXp() != null ? student.getXp() : 0);
            entry.put("totalMarks", totalMarks);
            return entry;
        }).sorted((a, b) -> Double.compare((Double) b.get("totalMarks"), (Double) a.get("totalMarks")))
        .collect(Collectors.toList());
    }
}
