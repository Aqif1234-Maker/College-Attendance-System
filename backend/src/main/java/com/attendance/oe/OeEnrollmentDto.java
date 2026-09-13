package com.attendance.oe;

public record OeEnrollmentDto(
        Long id,
        Long oeSubjectId,
        Long studentId,
        String rollNo,
        String studentName,
        Long studentClassId,
        String studentClassName
) {
}