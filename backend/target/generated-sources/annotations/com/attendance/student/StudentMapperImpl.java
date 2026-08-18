package com.attendance.student;

import com.attendance.class_.ClassEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-18T10:16:21+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class StudentMapperImpl implements StudentMapper {

    @Override
    public StudentDto toDto(Student student) {
        if ( student == null ) {
            return null;
        }

        Long classId = null;
        Long id = null;
        String rollNo = null;
        String name = null;
        String batchLabel = null;
        boolean active = false;

        classId = studentClassEntityId( student );
        id = student.getId();
        rollNo = student.getRollNo();
        name = student.getName();
        batchLabel = student.getBatchLabel();
        active = student.isActive();

        StudentDto studentDto = new StudentDto( id, classId, rollNo, name, batchLabel, active );

        return studentDto;
    }

    private Long studentClassEntityId(Student student) {
        if ( student == null ) {
            return null;
        }
        ClassEntity classEntity = student.getClassEntity();
        if ( classEntity == null ) {
            return null;
        }
        Long id = classEntity.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
