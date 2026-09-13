package com.attendance.audit;

import com.attendance.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<List<AuditLogDto>> all() {
        return ApiResponse.ok(auditLogService.all());
    }

    @GetMapping("/entity")
    public ApiResponse<List<AuditLogDto>> forEntity(
            @RequestParam String entityType, @RequestParam Long entityId) {
        return ApiResponse.ok(auditLogService.forEntity(entityType, entityId));
    }
}