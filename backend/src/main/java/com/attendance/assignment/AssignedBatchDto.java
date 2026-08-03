package com.attendance.assignment;

public record AssignedBatchDto(
        Long id,
        String label,
        boolean wholeClass
) {
}