package com.attendance.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateStudentRequest(
        @NotNull(message = "Class is required") Long classId,
        @NotBlank(message = "Roll number is required") String rollNo,
        @NotBlank(message = "Student name is required") String name,
        String batchLabel
) {
}