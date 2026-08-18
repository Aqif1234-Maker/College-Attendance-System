package com.attendance.subject;

import com.attendance.assignment.TeacherAssignmentRepository;
import com.attendance.attendance.AttendanceRecordRepository;
import com.attendance.attendance.AttendanceSession;
import com.attendance.attendance.AttendanceSessionRepository;
import com.attendance.attendance.AttendanceSessionSlotLockRepository;
import com.attendance.batch.Batch;
import com.attendance.batch.BatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TeachingStructureCleanupService {

    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceSessionSlotLockRepository attendanceSessionSlotLockRepository;
    private final BatchRepository batchRepository;

    public TeachingStructureCleanupService(
            TeacherAssignmentRepository teacherAssignmentRepository,
            AttendanceSessionRepository attendanceSessionRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            AttendanceSessionSlotLockRepository attendanceSessionSlotLockRepository,
            BatchRepository batchRepository) {
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.attendanceSessionRepository = attendanceSessionRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.attendanceSessionSlotLockRepository = attendanceSessionSlotLockRepository;
        this.batchRepository = batchRepository;
    }

    public void cleanupBatch(Batch batch) {
        teacherAssignmentRepository.deleteByBatchId(batch.getId());

        List<AttendanceSession> sessions = attendanceSessionRepository.findByBatchId(batch.getId());
        for (AttendanceSession session : sessions) {
            attendanceRecordRepository.deleteBySessionId(session.getId());
            attendanceSessionSlotLockRepository.deleteBySessionId(session.getId());
        }
        attendanceSessionRepository.deleteAll(sessions);
    }

    public void cleanupSubject(Long subjectId) {
        List<Batch> batches = batchRepository.findBySubjectId(subjectId);
        for (Batch batch : batches) {
            cleanupBatch(batch);
        }

        teacherAssignmentRepository.deleteBySubjectId(subjectId);
        batchRepository.deleteAll(batches);
    }
}
