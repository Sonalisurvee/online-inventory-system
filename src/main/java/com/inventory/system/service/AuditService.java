package com.inventory.system.service;

import com.inventory.system.model.AuditLog;
import com.inventory.system.model.User;
import com.inventory.system.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserService userService;

    public void log(String action, String tableName, Long recordId, String oldValue, String newValue) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setTableName(tableName);
        log.setRecordId(recordId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setTimestamp(LocalDateTime.now());

        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String username = auth.getName();
            User user = userService.getUserByUsername(username).orElse(null);
            log.setUser(user);
        }

        // Get IP address
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String ip = request.getRemoteAddr();
            log.setIpAddress(ip);
        } catch (Exception e) {
            log.setIpAddress("unknown");
        }

        auditLogRepository.save(log);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    public List<AuditLog> getLogsByUser(User user) {
        return auditLogRepository.findByUser(user);
    }

    public List<AuditLog> getLogsByAction(String action) {
        return auditLogRepository.findByAction(action);
    }
}