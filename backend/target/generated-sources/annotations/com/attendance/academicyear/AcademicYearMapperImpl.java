package com.attendance.academicyear;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-02T12:50:37+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class AcademicYearMapperImpl implements AcademicYearMapper {

    @Override
    public AcademicYearDto toDto(AcademicYear academicYear) {
        if ( academicYear == null ) {
            return null;
        }

        Long id = null;
        String label = null;
        boolean current = false;

        id = academicYear.getId();
        label = academicYear.getLabel();
        current = academicYear.isCurrent();

        AcademicYearDto academicYearDto = new AcademicYearDto( id, label, current );

        return academicYearDto;
    }
}
