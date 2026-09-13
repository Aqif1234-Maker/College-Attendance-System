package com.attendance.audit;

import java.time.Instant;

public record AuditLogDto(
        Long id,
        Long actorId,
        String actorName,
        String action,
        String entityType,
        Long entityId,
        String beforeValue,
        String afterValue,
        Instant createdAt
) {
}