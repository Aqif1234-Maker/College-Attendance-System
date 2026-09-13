package com.attendance.oe;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OeEnrollmentMapper {

    @Mapping(target = "oeSubjectId", source = "oeSubject.id")
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "rollNo", source = "student.rollNo")
    @Mapping(target = "studentName", source = "student.name")
    @Mapping(target = "studentClassId", source = "student.classEntity.id")
    @Mapping(target = "studentClassName", source = "student.classEntity.name")
    OeEnrollmentDto toDto(OeEnrollment enrollment);
}