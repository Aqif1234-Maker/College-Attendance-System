package com.attendance.attendance;

import com.attendance.batch.Batch;
import com.attendance.class_.ClassEntity;
import com.attendance.subject.Subject;
import com.attendance.user.User;
import java.time.LocalDate;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-02T12:50:37+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class AttendanceMapperImpl implements AttendanceMapper {

    @Override
    public AttendanceSessionDto toBaseDto(AttendanceSession session) {
        if ( session == null ) {
            return null;
        }

        Long classId = null;
        String className = null;
        Long subjectId = null;
        String subjectName = null;
        Long batchId = null;
        String batchLabel = null;
        Long lockedById = null;
        String lockedByName = null;
        Long id = null;
        LocalDate sessionDate = null;
        int slot = 0;
        boolean submitted = false;

        classId = sessionClassEntityId( session );
        className = sessionClassEntityName( session );
        subjectId = sessionSubjectId( session );
        subjectName = sessionSubjectName( session );
        batchId = sessionBatchId( session );
        batchLabel = sessionBatchLabel( session );
        lockedById = sessionLockedById( session );
        lockedByName = sessionLockedByFullName( session );
        id = session.getId();
        sessionDate = session.getSessionDate();
        slot = session.getSlot();
        submitted = session.isSubmitted();

        List<AttendanceSessionDto.StudentRecord> students = null;

        AttendanceSessionDto attendanceSessionDto = new AttendanceSessionDto( id, classId, className, subjectId, subjectName, batchId, batchLabel, sessionDate, slot, lockedById, lockedByName, submitted, students );

        return attendanceSessionDto;
    }

    private Long sessionClassEntityId(AttendanceSession attendanceSession) {
        if ( attendanceSession == null ) {
            return null;
        }
        ClassEntity classEntity = attendanceSession.getClassEntity();
        if ( classEntity == null ) {
            return null;
        }
        Long id = classEntity.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String sessionClassEntityName(AttendanceSession attendanceSession) {
        if ( attendanceSession == null ) {
            return null;
        }
        ClassEntity classEntity = attendanceSession.getClassEntity();
        if ( classEntity == null ) {
            return null;
        }
        String name = classEntity.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private Long sessionSubjectId(AttendanceSession attendanceSession) {
        if ( attendanceSession == null ) {
            return null;
        }
        Subject subject = attendanceSession.getSubject();
        if ( subject == null ) {
            return null;
        }
        Long id = subject.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String sessionSubjectName(AttendanceSession attendanceSession) {
        if ( attendanceSession == null ) {
            return null;
        }
        Subject subject = attendanceSession.getSubject();
        if ( subject == null ) {
            return null;
        }
        String name = subject.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private Long sessionBatchId(AttendanceSession attendanceSession) {
        if ( attendanceSession == null ) {
            return null;
        }
        Batch batch = attendanceSession.getBatch();
        if ( batch == null ) {
            return null;
        }
        Long id = batch.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String sessionBatchLabel(AttendanceSession attendanceSession) {
        if ( attendanceSession == null ) {
            return null;
        }
        Batch batch = attendanceSession.getBatch();
        if ( batch == null ) {
            return null;
        }
        String label = batch.getLabel();
        if ( label == null ) {
            return null;
        }
        return label;
    }

    private Long sessionLockedById(AttendanceSession attendanceSession) {
        if ( attendanceSession == null ) {
            return null;
        }
        User lockedBy = attendanceSession.getLockedBy();
        if ( lockedBy == null ) {
            return null;
        }
        Long id = lockedBy.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String sessionLockedByFullName(AttendanceSession attendanceSession) {
        if ( attendanceSession == null ) {
            return null;
        }
        User lockedBy = attendanceSession.getLockedBy();
        if ( lockedBy == null ) {
            return null;
        }
        String fullName = lockedBy.getFullName();
        if ( fullName == null ) {
            return null;
        }
        return fullName;
    }
}
