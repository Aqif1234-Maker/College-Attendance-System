package com.attendance.oe;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SemesterMapper {

    @Mapping(target = "academicYearId", source = "academicYear.id")
    @Mapping(target = "academicYearLabel", source = "academicYear.label")
    @Mapping(target = "classAId", source = "classA.id")
    @Mapping(target = "classAName", source = "classA.name")
    @Mapping(target = "classBId", source = "classB.id")
    @Mapping(target = "classBName", source = "classB.name")
    SemesterDto toDto(Semester semester);
}