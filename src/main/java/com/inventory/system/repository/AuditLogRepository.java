package com.inventory.system.repository;

import com.inventory.system.model.AuditLog;
import com.inventory.system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUser(User user);
    List<AuditLog> findByAction(String action);
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}