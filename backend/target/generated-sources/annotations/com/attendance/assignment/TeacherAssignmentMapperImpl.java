package com.attendance.assignment;

import com.attendance.batch.Batch;
import com.attendance.class_.ClassEntity;
import com.attendance.subject.Subject;
import com.attendance.user.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-02T12:50:37+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class TeacherAssignmentMapperImpl implements TeacherAssignmentMapper {

    @Override
    public TeacherAssignmentDto toDto(TeacherAssignment teacherAssignment) {
        if ( teacherAssignment == null ) {
            return null;
        }

        Long teacherId = null;
        String teacherName = null;
        Long classId = null;
        String className = null;
        Long subjectId = null;
        String subjectName = null;
        Long batchId = null;
        String batchLabel = null;
        Long assignedById = null;
        String assignedByName = null;
        Long id = null;

        teacherId = teacherAssignmentTeacherId( teacherAssignment );
        teacherName = teacherAssignmentTeacherFullName( teacherAssignment );
        classId = teacherAssignmentClassEntityId( teacherAssignment );
        className = teacherAssignmentClassEntityName( teacherAssignment );
        subjectId = teacherAssignmentSubjectId( teacherAssignment );
        subjectName = teacherAssignmentSubjectName( teacherAssignment );
        batchId = teacherAssignmentBatchId( teacherAssignment );
        batchLabel = teacherAssignmentBatchLabel( teacherAssignment );
        assignedById = teacherAssignmentAssignedById( teacherAssignment );
        assignedByName = teacherAssignmentAssignedByFullName( teacherAssignment );
        id = teacherAssignment.getId();

        TeacherAssignmentDto teacherAssignmentDto = new TeacherAssignmentDto( id, teacherId, teacherName, classId, className, subjectId, subjectName, batchId, batchLabel, assignedById, assignedByName );

        return teacherAssignmentDto;
    }

    private Long teacherAssignmentTeacherId(TeacherAssignment teacherAssignment) {
        if ( teacherAssignment == null ) {
            return null;
        }
        User teacher = teacherAssignment.getTeacher();
        if ( teacher == null ) {
            return null;
        }
        Long id = teacher.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String teacherAssignmentTeacherFullName(TeacherAssignment teacherAssignment) {
        if ( teacherAssignment == null ) {
            return null;
        }
        User teacher = teacherAssignment.getTeacher();
        if ( teacher == null ) {
            return null;
        }
        String fullName = teacher.getFullName();
        if ( fullName == null ) {
            return null;
        }
        return fullName;
    }

    private Long teacherAssignmentClassEntityId(TeacherAssignment teacherAssignment) {
        if ( teacherAssignment == null ) {
            return null;
        }
        ClassEntity classEntity = teacherAssignment.getClassEntity();
        if ( classEntity == null ) {
            return null;
        }
        Long id = classEntity.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String teacherAssignmentClassEntityName(TeacherAssignment teacherAssignment) {
        if ( teacherAssignment == null ) {
            return null;
        }
        ClassEntity classEntity = teacherAssignment.getClassEntity();
        if ( classEntity == null ) {
            return null;
        }
        String name = classEntity.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private Long teacherAssignmentSubjectId(TeacherAssignment teacherAssignment) {
        if ( teacherAssignment == null ) {
            return null;
        }
        Subject subject = teacherAssignment.getSubject();
        if ( subject == null ) {
            return null;
        }
        Long id = subject.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String teacherAssignmentSubjectName(TeacherAssignment teacherAssignment) {
        if ( teacherAssignment == null ) {
            return null;
        }
        Subject subject = teacherAssignment.getSubject();
        if ( subject == null ) {
            return null;
        }
        String name = subject.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private Long teacherAssignmentBatchId(TeacherAssignment teacherAssignment) {
        if ( teacherAssignment == null ) {
            return null;
        }
        Batch batch = teacherAssignment.getBatch();
        if ( batch == null ) {
            return null;
        }
        Long id = batch.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String teacherAssignmentBatchLabel(TeacherAssignment teacherAssignment) {
        if ( teacherAssignment == null ) {
            return null;
        }
        Batch batch = teacherAssignment.getBatch();
        if ( batch == null ) {
            return null;
        }
        String label = batch.getLabel();
        if ( label == null ) {
            return null;
        }
        return label;
    }

    private Long teacherAssignmentAssignedById(TeacherAssignment teacherAssignment) {
        if ( teacherAssignment == null ) {
            return null;
        }
        User assignedBy = teacherAssignment.getAssignedBy();
        if ( assignedBy == null ) {
            return null;
        }
        Long id = assignedBy.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String teacherAssignmentAssignedByFullName(TeacherAssignment teacherAssignment) {
        if ( teacherAssignment == null ) {
            return null;
        }
        User assignedBy = teacherAssignment.getAssignedBy();
        if ( assignedBy == null ) {
            return null;
        }
        String fullName = assignedBy.getFullName();
        if ( fullName == null ) {
            return null;
        }
        return fullName;
    }
}
