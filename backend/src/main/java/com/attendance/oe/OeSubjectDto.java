package com.attendance.oe;

public record OeSubjectDto(
        Long id,
        Long subjectId,
        String subjectName,
        Long classId,
        String className,
        Long oeTeachingSlotId,
        OeTeachingSlotMode mode,
        Long teacherId,
        String teacherName
) {
}
