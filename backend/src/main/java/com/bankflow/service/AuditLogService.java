package com.bankflow.service;

import com.bankflow.dto.AuditLogResponse;
import com.bankflow.entity.AuditAction;
import com.bankflow.entity.AuditLog;
import com.bankflow.entity.User;
import com.bankflow.repository.AuditLogRepository;
import com.bankflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public void log(AuditAction action, String description) {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .performedBy(user.getEmail())
                .role(user.getRole())
                .description(description)
                .build();

        auditLogRepository.save(auditLog);
    }

    public Page<AuditLogResponse> getAuditLogs(int page, int size) {

        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return auditLogRepository
                .findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToResponse);
    }

    private AuditLogResponse mapToResponse(AuditLog auditLog) {

        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getAction(),
                auditLog.getPerformedBy(),
                auditLog.getRole(),
                auditLog.getDescription(),
                auditLog.getCreatedAt()
        );
    }
}