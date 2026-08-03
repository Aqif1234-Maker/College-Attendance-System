package com.attendance.academicyear;

public record AcademicYearDto(
        Long id,
        String label,
        boolean current
) {
}