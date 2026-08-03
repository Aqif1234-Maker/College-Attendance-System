package com.attendance.class_;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateClassRequest(
        @NotNull(message = "Academic year is required") Long academicYearId,
        @NotBlank(message = "Class name is required") String name
) {
}