package com.attendance.attendance;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {

    @Mapping(target = "classId", source = "classEntity.id")
    @Mapping(target = "className", source = "classEntity.name")
    @Mapping(target = "subjectId", source = "subject.id")
    @Mapping(target = "subjectName", source = "subject.name")
    @Mapping(target = "batchId", source = "batch.id")
    @Mapping(target = "batchLabel", source = "batch.label")
    @Mapping(target = "lockedById", source = "lockedBy.id")
    @Mapping(target = "lockedByName", source = "lockedBy.fullName")
    @Mapping(target = "students", ignore = true)
    AttendanceSessionDto toBaseDto(AttendanceSession session);
}