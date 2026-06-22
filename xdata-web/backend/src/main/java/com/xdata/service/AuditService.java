package com.xdata.service;

import com.xdata.model.AuditLog;
import com.xdata.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;
    private final AccessControlService accessControlService;

    /**
     * Best-effort audit logging. Runs in its own transaction so it can write even
     * when the caller's transaction is read-only (e.g. the failed-login path), and
     * never propagates an exception — auditing must not break the business request.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String targetUser, String details) {
        try {
            String performedBy = accessControlService.getCurrentUser()
                    .map(u -> u.getLoginId())
                    .orElse("SYSTEM");

            AuditLog entry = AuditLog.builder()
                    .action(action)
                    .performedBy(performedBy)
                    .targetUser(targetUser)
                    .details(details)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.warn("Audit-Log konnte nicht geschrieben werden (action={}): {}", action, e.getMessage());
        }
    }
}
