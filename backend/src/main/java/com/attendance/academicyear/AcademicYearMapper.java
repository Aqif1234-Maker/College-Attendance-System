package com.attendance.academicyear;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AcademicYearMapper {

    AcademicYearDto toDto(AcademicYear academicYear);
}