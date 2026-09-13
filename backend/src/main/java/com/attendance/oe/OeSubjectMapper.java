package com.attendance.oe;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OeSubjectMapper {

    @Mapping(target = "subjectId", source = "subject.id")
    @Mapping(target = "subjectName", source = "subject.name")
    @Mapping(target = "className", source = "subject.classEntity.name")
    @Mapping(target = "classId", source = "subject.classEntity.id")
    @Mapping(target = "oeTeachingSlotId", source = "oeTeachingSlot.id")
    @Mapping(target = "mode", source = "oeTeachingSlot.mode")
    @Mapping(target = "teacherId", source = "oeTeachingSlot.teacher.id")
    @Mapping(target = "teacherName", source = "oeTeachingSlot.teacher.fullName")
    OeSubjectDto toDto(OeSubject oeSubject);
}