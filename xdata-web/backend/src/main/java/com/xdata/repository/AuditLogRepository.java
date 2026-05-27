package com.xdata.repository;

import com.xdata.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByTimestampDesc();
    List<AuditLog> findByActionContainingIgnoreCase(String action);
    List<AuditLog> findByPerformedByContainingIgnoreCase(String performedBy);
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
