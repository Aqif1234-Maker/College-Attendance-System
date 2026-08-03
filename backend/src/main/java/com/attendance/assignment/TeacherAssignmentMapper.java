package com.attendance.assignment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TeacherAssignmentMapper {

    @Mapping(target = "teacherId", source = "teacher.id")
    @Mapping(target = "teacherName", source = "teacher.fullName")
    @Mapping(target = "classId", source = "classEntity.id")
    @Mapping(target = "className", source = "classEntity.name")
    @Mapping(target = "subjectId", source = "subject.id")
    @Mapping(target = "subjectName", source = "subject.name")
    @Mapping(target = "batchId", source = "batch.id")
    @Mapping(target = "batchLabel", source = "batch.label")
    @Mapping(target = "assignedById", source = "assignedBy.id")
    @Mapping(target = "assignedByName", source = "assignedBy.fullName")
    TeacherAssignmentDto toDto(TeacherAssignment teacherAssignment);
}