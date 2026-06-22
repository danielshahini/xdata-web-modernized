package com.xdata.eval.adapter;

import com.google.gson.Gson;
import com.xdata.eval.core.GradingOutcome;
import com.xdata.eval.port.GradeSink;
import com.xdata.model.Submission;
import com.xdata.model.XDataUser;
import com.xdata.repository.SubmissionRepository;
import com.xdata.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Prod adapter for {@link GradeSink}: writes the outcome onto the Submission and
 * awards XP. The XP "improvement over previous best" rule is relocated verbatim
 * from {@code GradingService.updateUserXP}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JpaGradeSink implements GradeSink {

    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final Gson gson = new Gson();

    @Override
    public void save(Submission submission, GradingOutcome outcome) {
        if (outcome.markInfo() != null) {
            submission.setMarkInfoJson(gson.toJson(outcome.markInfo()));
        }
        submission.setVerifiedCorrect(outcome.verifiedCorrect());
        submission.setMarks(outcome.finalScore());
        submission.setEvaluated(true);
        updateUserXP(submission, outcome.finalScore());
        submissionRepository.save(submission);
    }

    private void updateUserXP(Submission submission, float currentMarks) {
        try {
            XDataUser user = submission.getUser();
            if (user == null) return;

            Integer qId = submission.getQuestion().getId();
            List<Submission> previousSubmissions = submissionRepository
                    .findByUser_LoginIdAndQuestion_IdOrderBySubmissionTimeDesc(user.getLoginId(), qId);

            float previousBest = 0.0f;
            for (Submission s : previousSubmissions) {
                if (s.getId().equals(submission.getId())) continue;
                if (s.getEvaluated() != null && s.getEvaluated() && s.getMarks() > previousBest) {
                    previousBest = s.getMarks();
                }
            }

            if (currentMarks > previousBest) {
                float questionMaxMarks = submission.getQuestion().getMarks() != null
                        ? submission.getQuestion().getMarks() : 10.0f;
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
}
