package com.attendance.audit;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    @Mapping(target = "actorId", source = "actor.id")
    @Mapping(target = "actorName", source = "actor.fullName")
    AuditLogDto toDto(AuditLog auditLog);
}