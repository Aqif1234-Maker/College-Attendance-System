package com.attendance.student;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(target = "classId", source = "classEntity.id")
    StudentDto toDto(Student student);
}