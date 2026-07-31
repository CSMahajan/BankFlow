package com.bankflow.dto;

import com.bankflow.entity.AuditAction;
import com.bankflow.entity.User;

import java.time.LocalDateTime;

public record AuditLogResponse(

        Long id,

        AuditAction action,

        String performedBy,

        User.Role role,

        String description,

        LocalDateTime createdAt

) {}