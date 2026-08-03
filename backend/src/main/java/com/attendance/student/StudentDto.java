package com.attendance.student;

public record StudentDto(
        Long id,
        Long classId,
        String rollNo,
        String name,
        String batchLabel,
        boolean active
) {
}