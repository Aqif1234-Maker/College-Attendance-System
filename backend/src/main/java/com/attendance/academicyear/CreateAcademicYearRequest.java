package com.attendance.academicyear;

import jakarta.validation.constraints.NotBlank;

public record CreateAcademicYearRequest(
        @NotBlank(message = "Label is required") String label
) {
}