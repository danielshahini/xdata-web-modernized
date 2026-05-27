package com.xdata.service;

import com.xdata.model.Submission;
import com.xdata.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlagiarismService {
    private final SubmissionRepository submissionRepository;

    public static class PlagiarismResult {
        public String student1;
        public String student2;
        public double similarity;
        public String query1;
        public String query2;
    }

    public List<PlagiarismResult> checkAssignment(Integer assignmentId, double threshold) {
        List<Submission> submissions = submissionRepository.findByQuestion_Assignment_Id(assignmentId);
        List<PlagiarismResult> results = new ArrayList<>();

        for (int i = 0; i < submissions.size(); i++) {
            for (int j = i + 1; j < submissions.size(); j++) {
                Submission s1 = submissions.get(i);
                Submission s2 = submissions.get(j);
                
                if (s1.getUser().getLoginId().equals(s2.getUser().getLoginId())) continue;

                double similarity = calculateSimilarity(s1.getQuery(), s2.getQuery());
                if (similarity >= threshold) {
                    PlagiarismResult res = new PlagiarismResult();
                    res.student1 = s1.getUser().getLoginId();
                    res.student2 = s2.getUser().getLoginId();
                    res.similarity = similarity;
                    res.query1 = s1.getQuery();
                    res.query2 = s2.getQuery();
                    results.add(res);
                }
            }
        }
        return results;
    }

    private double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0;
        String n1 = s1.replaceAll("\s+", "").toLowerCase();
        String n2 = s2.replaceAll("\s+", "").toLowerCase();
        
        if (n1.equals(n2)) return 1.0;
        
        int distance = levenshteinDistance(n1, n2);
        return 1.0 - ((double) distance / Math.max(n1.length(), n2.length()));
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[s1.length()][s2.length()];
    }
}
