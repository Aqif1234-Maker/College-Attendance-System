package com.attendance.assignment;

import jakarta.validation.constraints.NotNull;

public record AssignTeacherRequest(
        @NotNull(message = "Teacher is required") Long teacherId,
        @NotNull(message = "Class is required") Long classId,
        @NotNull(message = "Subject is required") Long subjectId,
        @NotNull(message = "Batch is required") Long batchId
) {
}