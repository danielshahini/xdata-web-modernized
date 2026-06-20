package com.xdata.controller.admin;

import com.xdata.model.AuditLog;
import com.xdata.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {
    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<List<AuditLog>> getLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        if (action != null) return ResponseEntity.ok(auditLogRepository.findByActionContainingIgnoreCase(action));
        if (performedBy != null) return ResponseEntity.ok(auditLogRepository.findByPerformedByContainingIgnoreCase(performedBy));
        if (start != null && end != null) return ResponseEntity.ok(auditLogRepository.findByTimestampBetween(start, end));
        
        return ResponseEntity.ok(auditLogRepository.findAllByOrderByTimestampDesc());
    }
}
