package com.attendance.subject;

import com.attendance.class_.ClassEntity;
import com.attendance.user.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-02T12:50:37+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class SubjectMapperImpl implements SubjectMapper {

    @Override
    public SubjectDto toDto(Subject subject) {
        if ( subject == null ) {
            return null;
        }

        Long classId = null;
        String className = null;
        Long createdById = null;
        String createdByName = null;
        Long id = null;
        String name = null;
        SubjectType type = null;

        classId = subjectClassEntityId( subject );
        className = subjectClassEntityName( subject );
        createdById = subjectCreatedById( subject );
        createdByName = subjectCreatedByFullName( subject );
        id = subject.getId();
        name = subject.getName();
        type = subject.getType();

        SubjectDto subjectDto = new SubjectDto( id, classId, className, name, type, createdById, createdByName );

        return subjectDto;
    }

    private Long subjectClassEntityId(Subject subject) {
        if ( subject == null ) {
            return null;
        }
        ClassEntity classEntity = subject.getClassEntity();
        if ( classEntity == null ) {
            return null;
        }
        Long id = classEntity.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String subjectClassEntityName(Subject subject) {
        if ( subject == null ) {
            return null;
        }
        ClassEntity classEntity = subject.getClassEntity();
        if ( classEntity == null ) {
            return null;
        }
        String name = classEntity.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private Long subjectCreatedById(Subject subject) {
        if ( subject == null ) {
            return null;
        }
        User createdBy = subject.getCreatedBy();
        if ( createdBy == null ) {
            return null;
        }
        Long id = createdBy.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String subjectCreatedByFullName(Subject subject) {
        if ( subject == null ) {
            return null;
        }
        User createdBy = subject.getCreatedBy();
        if ( createdBy == null ) {
            return null;
        }
        String fullName = createdBy.getFullName();
        if ( fullName == null ) {
            return null;
        }
        return fullName;
    }
}
