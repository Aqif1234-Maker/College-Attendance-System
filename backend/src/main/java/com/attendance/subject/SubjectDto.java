package com.attendance.subject;

public record SubjectDto(
        Long id,
        Long classId,
        String className,
        String name,
        SubjectType type,
        Long createdById,
        String createdByName
) {
}