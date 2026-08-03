package com.attendance.batch;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BatchMapper {

    @Mapping(target = "subjectId", source = "subject.id")
    BatchDto toDto(Batch batch);
}