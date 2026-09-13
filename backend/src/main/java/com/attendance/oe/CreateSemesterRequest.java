package com.attendance.oe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSemesterRequest(
        @NotNull(message = "Academic year is required") Long academicYearId,
        @NotBlank(message = "Label is required") String label,
        @NotNull(message = "Division A class is required") Long classAId,
        @NotNull(message = "Division B class is required") Long classBId
) {
}