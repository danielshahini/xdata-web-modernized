package com.xdata.service.core;

import com.xdata.model.Assignment;
import com.xdata.model.Question;
import com.xdata.model.Submission;
import com.xdata.model.XDataUser;
import com.xdata.repository.QuestionRepository;
import com.xdata.repository.SubmissionRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubmissionAnalyticsTest {

    private final SubmissionRepository submissionRepository = mock(SubmissionRepository.class);
    private final QuestionRepository questionRepository = mock(QuestionRepository.class);
    private final AssignmentService assignmentService = mock(AssignmentService.class);
    private final SubmissionAnalytics analytics = new SubmissionAnalytics(
            submissionRepository, questionRepository, assignmentService);

    private static XDataUser user(String loginId) {
        XDataUser u = new XDataUser();
        u.setLoginId(loginId);
        u.setUsername(loginId);
        return u;
    }

    private static Question question(int id, String name, double marks) {
        Question q = new Question();
        q.setId(id);
        q.setName(name);
        q.setMarks((float) marks);
        return q;
    }

    private static Submission submission(XDataUser u, Question q, float marks) {
        Submission s = new Submission();
        s.setUser(u);
        s.setQuestion(q);
        s.setMarks(marks);
        s.setSubmissionTime(LocalDateTime.parse("2026-06-10T10:00:00"));
        return s;
    }

    @Test
    void assignmentStats_aggregates_attempts_unique_solved_and_success_rate() {
        Question q = question(1, "Q1", 10);
        when(questionRepository.findByAssignment_Id(7)).thenReturn(List.of(q));

        XDataUser alice = user("alice");
        XDataUser bob = user("bob");
        // alice: two attempts, best solved (1.0); bob: one attempt, not solved (0.4)
        when(submissionRepository.findByQuestion_Id(1)).thenReturn(List.of(
                submission(alice, q, 0.5f),
                submission(alice, q, 1.0f),
                submission(bob, q, 0.4f)));

        List<Map<String, Object>> stats = analytics.assignmentStats(7);

        assertThat(stats).hasSize(1);
        Map<String, Object> s = stats.get(0);
        assertThat(s.get("questionId")).isEqualTo(1);
        assertThat(s.get("totalAttempts")).isEqualTo(3L);
        assertThat(s.get("uniqueUsers")).isEqualTo(2L);
        assertThat(s.get("solvedUsers")).isEqualTo(1L);  // only alice reached >= 1.0
        assertThat((double) s.get("successRate")).isEqualTo(0.5);
    }

    @Test
    void dashboard_computes_percentage_from_best_marks() {
        XDataUser alice = user("alice");
        alice.setXp(100);
        Assignment a = new Assignment();
        a.setId(5);
        a.setName("A1");
        a.setPublishedDate(null); // visible
        when(assignmentService.getAssignmentsByCourses(any())).thenReturn(List.of(a));

        Question q = question(1, "Q1", 10);
        when(questionRepository.findByAssignment_Id(5)).thenReturn(List.of(q));
        when(submissionRepository.findByUser_LoginIdAndQuestion_IdOrderBySubmissionTimeDesc("alice", 1))
                .thenReturn(List.of(submission(alice, q, 0.5f)));

        Map<String, Object> dash = analytics.dashboard(alice, List.of("C1"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> assignments = (List<Map<String, Object>>) dash.get("assignments");
        assertThat(assignments).hasSize(1);
        assertThat((double) assignments.get(0).get("percentage")).isEqualTo(50.0); // 0.5*10 / 10 * 100
        assertThat(assignments.get(0).get("solvedQuestions")).isEqualTo(0L); // 0.5 < 1.0
    }
}
