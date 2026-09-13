package com.attendance.audit;

import com.attendance.user.User;
import com.attendance.user.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuditLogService(
            AuditLogRepository auditLogRepository,
            UserRepository userRepository,
            AuditLogMapper auditLogMapper) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.auditLogMapper = auditLogMapper;
    }

    /**
     * Records one audit entry. Called by every mutating OE service method — never fails the
     * calling transaction if serialization of before/after has an issue; audit logging must
     * never block the actual business action from succeeding.
     */
    public void record(Long actorId, String action, String entityType, Long entityId, Object before, Object after) {
        User actor = userRepository.findById(actorId).orElse(null);
        if (actor == null) {
            return;
        }

        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setBeforeValue(toJsonSafely(before));
        log.setAfterValue(toJsonSafely(after));

        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLogDto> forEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId).stream()
                .map(auditLogMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogDto> all() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(auditLogMapper::toDto)
                .toList();
    }

    private String toJsonSafely(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{\"error\":\"serialization failed\"}";
        }
    }
}