package com.attendance.subject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSubjectRequest(
        @NotNull(message = "Class is required") Long classId,
        @NotBlank(message = "Subject name is required") String name,
        @NotNull(message = "Subject type is required") SubjectType type
) {
}