package com.attendance.attendance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MarkAttendanceRequest(
        @NotNull(message = "Session is required") Long sessionId,
        @NotEmpty(message = "At least one student status is required")
        @Valid
        List<StudentStatus> statuses
) {
    public record StudentStatus(
            @NotNull(message = "Student is required") Long studentId,
            @NotNull(message = "Status is required") AttendanceStatus status
    ) {
    }
}