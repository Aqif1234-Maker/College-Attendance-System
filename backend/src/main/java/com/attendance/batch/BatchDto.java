package com.attendance.batch;

public record BatchDto(
        Long id,
        Long subjectId,
        String label,
        boolean wholeClass
) {
}