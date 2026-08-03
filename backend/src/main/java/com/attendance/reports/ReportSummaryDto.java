package com.attendance.reports;

public record ReportSummaryDto(
        int totalStudents,
        double averageAttendance,
        int studentsBelowThreshold,
        double highestAttendance,
        double lowestAttendance
) {
}