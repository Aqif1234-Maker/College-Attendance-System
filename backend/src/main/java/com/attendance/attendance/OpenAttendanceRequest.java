package com.attendance.attendance;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record OpenAttendanceRequest(
        @NotNull(message = "Class is required") Long classId,
        @NotNull(message = "Subject is required") Long subjectId,
        @NotNull(message = "Batch is required") Long batchId,
        @NotNull(message = "Date is required") LocalDate sessionDate,
        @NotNull(message = "Slot is required")
        @Min(value = 1, message = "Slot must be at least 1")
        @Max(value = 7, message = "Slot must be at most 7")
        Integer slot
) {
}