package com.attendance.attendance;

import java.time.LocalDate;
import java.util.List;

public record AttendanceSessionDto(
        Long id,
        Long classId,
        String className,
        Long subjectId,
        String subjectName,
        Long batchId,
        String batchLabel,
        LocalDate sessionDate,
        int slot,
        Long lockedById,
        String lockedByName,
        boolean submitted,
        List<StudentRecord> students
) {
    public record StudentRecord(
            Long studentId,
            String rollNo,
            String studentName,
            AttendanceStatus status
    ) {
    }
}