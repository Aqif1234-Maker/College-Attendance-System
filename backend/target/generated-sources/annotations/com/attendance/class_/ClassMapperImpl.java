package com.attendance.class_;

import com.attendance.academicyear.AcademicYear;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-02T12:50:37+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class ClassMapperImpl implements ClassMapper {

    @Override
    public ClassDto toBaseDto(ClassEntity classEntity) {
        if ( classEntity == null ) {
            return null;
        }

        Long academicYearId = null;
        String academicYearLabel = null;
        Long id = null;
        String name = null;

        academicYearId = classEntityAcademicYearId( classEntity );
        academicYearLabel = classEntityAcademicYearLabel( classEntity );
        id = classEntity.getId();
        name = classEntity.getName();

        Long coordinatorId = null;
        String coordinatorName = null;

        ClassDto classDto = new ClassDto( id, name, academicYearId, academicYearLabel, coordinatorId, coordinatorName );

        return classDto;
    }

    private Long classEntityAcademicYearId(ClassEntity classEntity) {
        if ( classEntity == null ) {
            return null;
        }
        AcademicYear academicYear = classEntity.getAcademicYear();
        if ( academicYear == null ) {
            return null;
        }
        Long id = academicYear.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String classEntityAcademicYearLabel(ClassEntity classEntity) {
        if ( classEntity == null ) {
            return null;
        }
        AcademicYear academicYear = classEntity.getAcademicYear();
        if ( academicYear == null ) {
            return null;
        }
        String label = academicYear.getLabel();
        if ( label == null ) {
            return null;
        }
        return label;
    }
}
