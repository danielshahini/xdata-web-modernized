package com.xdata.eval.adapter;

import com.xdata.eval.port.ResultNotifier;
import com.xdata.model.Submission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Prod adapter for {@link ResultNotifier}: pushes the graded submission over STOMP. */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketResultNotifier implements ResultNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void notifyGraded(Submission submission) {
        try {
            if (submission.getUser() != null) {
                // Withhold the live score when the instructor has not released grades.
                com.xdata.model.Assignment a = submission.getQuestion() != null
                        ? submission.getQuestion().getAssignment() : null;
                boolean released = a == null || a.getGradesReleased() == null || a.getGradesReleased();
                if (!released) {
                    log.info("Grades withheld — skipping live notification for submission {}.", submission.getId());
                    return;
                }
                String loginId = submission.getUser().getLoginId();
                messagingTemplate.convertAndSend("/topic/grading/" + loginId, submission);
            }
        } catch (Exception e) {
            log.warn("Could not send WebSocket notification: {}", e.getMessage());
        }
    }
}
