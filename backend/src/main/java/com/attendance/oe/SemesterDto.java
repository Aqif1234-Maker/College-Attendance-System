package com.attendance.oe;

public record SemesterDto(
        Long id,
        Long academicYearId,
        String academicYearLabel,
        String label,
        SemesterStatus status,
        Long classAId,
        String classAName,
        Long classBId,
        String classBName
) {
}