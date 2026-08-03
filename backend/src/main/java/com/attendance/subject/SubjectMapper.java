package com.attendance.subject;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubjectMapper {

    @Mapping(target = "classId", source = "classEntity.id")
    @Mapping(target = "className", source = "classEntity.name")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByName", source = "createdBy.fullName")
    SubjectDto toDto(Subject subject);
}