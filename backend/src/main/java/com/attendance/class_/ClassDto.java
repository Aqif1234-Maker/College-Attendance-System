package com.attendance.class_;

public record ClassDto(
        Long id,
        String name,
        Long academicYearId,
        String academicYearLabel,
        Long coordinatorId,
        String coordinatorName
) {
}