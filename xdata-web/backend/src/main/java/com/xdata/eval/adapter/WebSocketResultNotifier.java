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
                String loginId = submission.getUser().getLoginId();
                messagingTemplate.convertAndSend("/topic/grading/" + loginId, submission);
            }
        } catch (Exception e) {
            log.warn("Could not send WebSocket notification: {}", e.getMessage());
        }
    }
}
