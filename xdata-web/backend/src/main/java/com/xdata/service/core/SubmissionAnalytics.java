package com.xdata.service.core;

import com.xdata.model.Assignment;
import com.xdata.model.Question;
import com.xdata.model.Submission;
import com.xdata.model.XDataUser;
import com.xdata.repository.QuestionRepository;
import com.xdata.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The single home for read-side aggregation over submissions: student dashboard,
 * per-question statistics, CSV export and the leaderboard. Extracted out of the
 * controllers (which now delegate) so the aggregation is testable without the HTTP
 * stack, and the "best mark per question" rule lives in one place. See CONTEXT.md
 * → "SubmissionAnalytics".
 */
@Service
@RequiredArgsConstructor
public class SubmissionAnalytics {

    private final SubmissionRepository submissionRepository;
    private final QuestionRepository questionRepository;
    private final AssignmentService assignmentService;

    /** Per-question statistics for one assignment (instructor view). */
    public List<Map<String, Object>> assignmentStats(Integer assignmentId) {
        List<Question> questions = questionRepository.findByAssignment_Id(assignmentId);
        return questions.stream().map(q -> {
            Map<String, Object> qStats = new HashMap<>();
            qStats.put("questionId", q.getId());
            qStats.put("name", q.getName());

            List<Submission> submissions = submissionRepository.findByQuestion_Id(q.getId());
            long totalAttempts = submissions.size();
            long uniqueUsers = submissions.stream().map(s -> s.getUser().getLoginId()).distinct().count();

            long solvedUsers = submissions.stream()
                    .filter(s -> s.getMarks() >= 1.0)
                    .map(s -> s.getUser().getLoginId())
                    .distinct().count();

            double avgMarks = submissions.stream()
                    .mapToDouble(Submission::getMarks)
                    .average().orElse(0.0);

            qStats.put("totalAttempts", totalAttempts);
            qStats.put("uniqueUsers", uniqueUsers);
            qStats.put("solvedUsers", solvedUsers);
            qStats.put("successRate", uniqueUsers > 0 ? (double) solvedUsers / uniqueUsers : 0.0);
            qStats.put("avgMarks", avgMarks);

            return qStats;
        }).collect(Collectors.toList());
    }

    /** A student's dashboard: XP/level plus per-assignment progress for their courses. */
    public Map<String, Object> dashboard(XDataUser user, List<String> courseIds) {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("studentName", user.getUsername());
        dashboard.put("xp", user.getXp() != null ? user.getXp() : 0);
        int level = (int) (Math.floor(Math.sqrt((user.getXp() != null ? user.getXp() : 0) / 100.0)) + 1);
        dashboard.put("level", level);
        dashboard.put("nextLevelXp", (int) (Math.pow(level, 2) * 100));
        dashboard.put("currentLevelXp", (int) (Math.pow(level - 1, 2) * 100));

        List<Assignment> assignments = assignmentService.getAssignmentsByCourses(courseIds).stream()
                .filter(a -> a.getPublishedDate() == null || a.getPublishedDate().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());

        List<Map<String, Object>> assignmentData = assignments.stream().map(a -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", a.getId());
            data.put("assignmentId", a.getId());
            data.put("name", a.getName());
            data.put("deadline", a.getDeadline());

            List<Question> questions = questionRepository.findByAssignment_Id(a.getId());
            data.put("totalQuestions", questions.size());

            double totalMarks = questions.stream().mapToDouble(q -> q.getMarks() != null ? q.getMarks() : 0.0).sum();
            data.put("totalMarks", totalMarks);

            double achievedMarks = 0.0;
            long solvedCount = 0;

            for (Question q : questions) {
                List<Submission> userSubmissions = submissionRepository
                        .findByUser_LoginIdAndQuestion_IdOrderBySubmissionTimeDesc(user.getLoginId(), q.getId());
                double bestMarks = bestMark(userSubmissions);
                achievedMarks += bestMarks * (q.getMarks() != null ? q.getMarks() : 0.0);
                if (bestMarks >= 1.0) {
                    solvedCount++;
                }
            }

            data.put("solvedQuestions", solvedCount);
            data.put("achievedMarks", achievedMarks);
            data.put("percentage", totalMarks > 0 ? (achievedMarks / totalMarks) * 100 : 0);

            return data;
        }).collect(Collectors.toList());

        dashboard.put("assignments", assignmentData);
        return dashboard;
    }

    /** CSV export of all results for one assignment. */
    public String resultsCsv(Integer assignmentId) {
        assignmentService.getAssignmentById(assignmentId).orElseThrow();
        List<Question> questions = questionRepository.findByAssignment_Id(assignmentId);
        List<Submission> allSubmissions = submissionRepository.findByQuestion_Assignment_Id(assignmentId);

        StringBuilder csv = new StringBuilder("StudentId,Username,TotalMarksPercentage,XP,");
        for (Question q : questions) {
            String cleanName = q.getName().replace(",", " ");
            csv.append(cleanName).append(" (Score),");
            csv.append(cleanName).append(" (Attempts),");
            csv.append(cleanName).append(" (Last Submission),");
        }
        csv.append("\n");

        Map<String, List<Submission>> subsByUser = allSubmissions.stream()
                .collect(Collectors.groupingBy(s -> s.getUser().getLoginId()));

        for (Map.Entry<String, List<Submission>> entry : subsByUser.entrySet()) {
            String loginId = entry.getKey();
            XDataUser user = entry.getValue().get(0).getUser();
            String username = user.getUsername();
            Integer xp = user.getXp();

            double totalPossible = questions.stream().mapToDouble(Question::getMarks).sum();
            double achievedPoints = questions.stream().mapToDouble(q -> entry.getValue().stream()
                    .filter(s -> s.getQuestion().getId().equals(q.getId()))
                    .mapToDouble(s -> s.getMarks() * q.getMarks())
                    .max().orElse(0.0)).sum();

            double percentage = totalPossible > 0 ? (achievedPoints / totalPossible) * 100 : 0;

            StringBuilder row = new StringBuilder(String.format("%s,%s,%.2f%%,%d,", loginId, username, percentage, xp != null ? xp : 0));

            for (Question q : questions) {
                List<Submission> qSubs = entry.getValue().stream()
                        .filter(s -> s.getQuestion().getId().equals(q.getId()))
                        .collect(Collectors.toList());

                double best = bestMark(qSubs);
                long attempts = qSubs.size();
                String lastSub = qSubs.stream()
                        .map(s -> s.getSubmissionTime().toString())
                        .max(String::compareTo).orElse("-");

                row.append(String.format("%.2f%%,%d,%s,", best * 100, attempts, lastSub));
            }
            row.append("\n");
            csv.append(row);
        }

        return csv.toString();
    }

    /** The single definition of "best mark per question": the maximum, or 0 if none. */
    private double bestMark(List<Submission> submissions) {
        return submissions.stream().mapToDouble(Submission::getMarks).max().orElse(0.0);
    }
}
