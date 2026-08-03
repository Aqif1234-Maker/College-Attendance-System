package com.attendance.reports;

public record ReportRowDto(
        Long studentId,
        String rollNo,
        String studentName,
        String subjectName,
        int classesConducted,
        int classesAttended,
        double percentage,
        String status
) {
}