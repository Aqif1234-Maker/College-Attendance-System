package com.attendance.assignment;

import com.attendance.subject.SubjectType;

public record AssignedSubjectDto(
        Long id,
        String name,
        SubjectType type
) {
}