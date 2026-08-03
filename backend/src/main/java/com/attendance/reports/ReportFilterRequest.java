package com.attendance.reports;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReportFilterRequest(
        @NotNull(message = "Class is required") Long classId,
        @NotNull(message = "Subject is required") Long subjectId,
        @NotNull(message = "Batch is required") Long batchId,
        @NotNull(message = "From date is required") LocalDate fromDate,
        @NotNull(message = "To date is required") LocalDate toDate,
        Integer minPercent,
        Integer maxPercent,
        ReportPreset preset
) {
    public enum ReportPreset {
        ALL, BELOW_75, BELOW_65, ABOVE_75, ABOVE_85, DEFAULTERS, EXCELLENT
    }
}