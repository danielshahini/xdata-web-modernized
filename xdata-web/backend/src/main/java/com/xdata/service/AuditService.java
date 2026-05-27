package com.xdata.service;

import com.xdata.model.AuditLog;
import com.xdata.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;
    private final AccessControlService accessControlService;

    public void log(String action, String targetUser, String details) {
        String performedBy = accessControlService.getCurrentUser()
                .map(u -> u.getLoginId())
                .orElse("SYSTEM");

        AuditLog log = AuditLog.builder()
                .action(action)
                .performedBy(performedBy)
                .targetUser(targetUser)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
        
        auditLogRepository.save(log);
    }
}
