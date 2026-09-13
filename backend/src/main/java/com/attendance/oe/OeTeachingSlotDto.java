package com.attendance.oe;

public record OeTeachingSlotDto(
        Long id,
        Long semesterId,
        String semesterLabel,
        OeTeachingSlotMode mode,
        Long teacherId,
        String teacherName,
        Long assignedById,
        String assignedByName,
        boolean subjectCreated
) {
}