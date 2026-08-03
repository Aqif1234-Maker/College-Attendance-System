package com.attendance.assignment;

public record TeacherAssignmentDto(
        Long id,
        Long teacherId,
        String teacherName,
        Long classId,
        String className,
        Long subjectId,
        String subjectName,
        Long batchId,
        String batchLabel,
        Long assignedById,
        String assignedByName
) {
}