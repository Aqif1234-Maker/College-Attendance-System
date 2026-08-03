package com.attendance.attendance;

import com.attendance.assignment.AssignmentLookupService;
import com.attendance.batch.Batch;
import com.attendance.batch.BatchRepository;
import com.attendance.class_.ClassEntity;
import com.attendance.class_.ClassRepository;
import com.attendance.common.ApiException;
import com.attendance.student.Student;
import com.attendance.student.StudentRepository;
import com.attendance.subject.Subject;
import com.attendance.subject.SubjectRepository;
import com.attendance.subject.SubjectType;
import com.attendance.user.User;
import com.attendance.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendanceService {

    private static final int MAX_THEORY_SLOT = 7;
    private static final int MAX_PRACTICAL_SLOT = 4;

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceSessionSlotLockRepository sessionSlotLockRepository;
    private final AttendanceRecordRepository recordRepository;
    private final ClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final BatchRepository batchRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final AssignmentLookupService assignmentLookupService;
    private final AttendanceMapper attendanceMapper;

    public AttendanceService(
            AttendanceSessionRepository sessionRepository,
            AttendanceSessionSlotLockRepository sessionSlotLockRepository,
            AttendanceRecordRepository recordRepository,
            ClassRepository classRepository,
            SubjectRepository subjectRepository,
            BatchRepository batchRepository,
            StudentRepository studentRepository,
            UserRepository userRepository,
            AssignmentLookupService assignmentLookupService,
            AttendanceMapper attendanceMapper) {
        this.sessionRepository = sessionRepository;
        this.sessionSlotLockRepository = sessionSlotLockRepository;
        this.recordRepository = recordRepository;
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
        this.batchRepository = batchRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.assignmentLookupService = assignmentLookupService;
        this.attendanceMapper = attendanceMapper;
    }

    /**
     * Opens (or idempotently re-opens) an attendance session. A slot lock is scoped to
     * class + date + actual time window, so the same slot can be reused on another day
     * but not double-booked today across different subjects/batches that overlap in time.
     */
    public AttendanceSessionDto openSession(OpenAttendanceRequest request, Long teacherId) {
        ClassEntity classEntity = classRepository.findById(request.classId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Class not found"));

        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Subject not found"));

        Batch batch = batchRepository.findById(request.batchId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Batch not found"));

        // Server-side ABAC re-validation — never trust that the ids in the request belong
        // to this teacher just because the dropdown data came from the same API earlier.
        assignmentLookupService.assertAssigned(teacherId, classEntity.getId(), subject.getId(), batch.getId());

        validateSlot(subject.getType(), request.slot());
        SlotSchedule.SlotWindow requestedWindow = SlotSchedule.forTypeAndSlot(subject.getType(), request.slot());

        Optional<AttendanceSession> existing = sessionRepository
                .findByClassEntityIdAndSubjectIdAndBatchIdAndSessionDateAndSlot(
                        classEntity.getId(), subject.getId(), batch.getId(), request.sessionDate(), request.slot());

        if (existing.isPresent()) {
            AttendanceSession session = existing.get();
            if (!session.getLockedBy().getId().equals(teacherId)) {
                throw new ApiException(HttpStatus.FORBIDDEN,
                        "This session was already started by another teacher and is locked");
            }
            // Idempotent re-open by the same teacher: return the existing session as-is.
            return buildSessionDto(session);
        }

        Optional<AttendanceSession> overlappingSession = sessionRepository
                .findByClassEntityIdAndSessionDate(classEntity.getId(), request.sessionDate()).stream()
                .filter(session -> overlapsRequestedWindow(session, requestedWindow))
                .findFirst();

        if (overlappingSession.isPresent()) {
            AttendanceSession session = overlappingSession.get();
            SlotSchedule.SlotWindow bookedWindow = SlotSchedule.forTypeAndSlot(session.getSubject().getType(), session.getSlot());
            throw new ApiException(HttpStatus.CONFLICT, buildSlotAlreadyBookedMessage(session, bookedWindow));
        }

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Teacher not found"));

        AttendanceSession session = new AttendanceSession();
        session.setClassEntity(classEntity);
        session.setSubject(subject);
        session.setBatch(batch);
        session.setSessionDate(request.sessionDate());
        session.setSlot(request.slot());
        session.setLockedBy(teacher);
        session.setSubmitted(false);

        try {
            session = sessionRepository.saveAndFlush(session);
            createSlotLocks(session, requestedWindow);
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "This slot was just booked for this class by another teacher. Please refresh and try again.");
        }

        return buildSessionDto(session);
    }

    public AttendanceSessionDto submit(MarkAttendanceRequest request, Long teacherId) {
        AttendanceSession session = sessionRepository.findById(request.sessionId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Attendance session not found"));

        if (!session.getLockedBy().getId().equals(teacherId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the teacher who opened this session can submit it");
        }

        // Re-validate assignment even though the session was already opened by this teacher —
        // an Admin may have revoked the assignment in between, per spec §1 "next request must reflect the change".
        assignmentLookupService.assertAssigned(
                teacherId, session.getClassEntity().getId(), session.getSubject().getId(), session.getBatch().getId());

        List<Student> sessionStudents = studentsForSession(session);
        Map<Long, Student> studentsById = sessionStudents.stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        recordRepository.deleteBySessionId(session.getId());

        for (MarkAttendanceRequest.StudentStatus entry : request.statuses()) {
            Student student = studentsById.get(entry.studentId());
            if (student == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Student id " + entry.studentId() + " does not belong to this session's class/batch");
            }

            AttendanceRecord record = new AttendanceRecord();
            record.setSession(session);
            record.setStudent(student);
            record.setStatus(entry.status());
            recordRepository.save(record);
        }

        session.setSubmitted(true);
        session.setSubmittedAt(Instant.now());

        return buildSessionDto(session);
    }

    @Transactional(readOnly = true)
    public AttendanceSessionDto getSession(Long sessionId, Long teacherId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Attendance session not found"));

        assignmentLookupService.assertAssigned(
                teacherId, session.getClassEntity().getId(), session.getSubject().getId(), session.getBatch().getId());

        return buildSessionDto(session);
    }

    @Transactional(readOnly = true)
    public List<AttendanceSessionDto> history(Long teacherId) {
        return sessionRepository.findByLockedByIdOrderBySessionDateDesc(teacherId).stream()
                .map(this::buildSessionDto)
                .toList();
    }

    private void validateSlot(SubjectType type, int slot) {
        int maxSlot = (type == SubjectType.TH) ? MAX_THEORY_SLOT : MAX_PRACTICAL_SLOT;
        if (slot < 1 || slot > maxSlot) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Slot " + slot + " is invalid for a " + type + " subject (valid range: 1-" + maxSlot + ")");
        }
    }

    private boolean overlapsRequestedWindow(AttendanceSession session, SlotSchedule.SlotWindow requestedWindow) {
        SlotSchedule.SlotWindow existingWindow = SlotSchedule.forTypeAndSlot(session.getSubject().getType(), session.getSlot());
        return existingWindow.overlaps(requestedWindow);
    }

    private String buildSlotAlreadyBookedMessage(AttendanceSession session, SlotSchedule.SlotWindow bookedWindow) {
        return "Slot " + bookedWindow.label() + " is already booked for this class on "
                + session.getSessionDate() + " by " + session.getLockedBy().getFullName()
                + " for " + session.getSubject().getName() + " (" + session.getBatch().getLabel() + ").";
    }

    private void createSlotLocks(AttendanceSession session, SlotSchedule.SlotWindow requestedWindow) {
        for (Integer periodUnit : requestedWindow.periodUnits()) {
            AttendanceSessionSlotLock lock = new AttendanceSessionSlotLock();
            lock.setSession(session);
            lock.setClassEntity(session.getClassEntity());
            lock.setSessionDate(session.getSessionDate());
            lock.setPeriodUnit(periodUnit.byteValue());
            sessionSlotLockRepository.save(lock);
        }
        sessionSlotLockRepository.flush();
    }

    /**
     * FIX: students must be scoped to the session's batch, not the whole class.
     * A whole-class (Theory) batch includes every active student in the class.
     * A specific Practical batch (e.g. B1) includes only students whose batchLabel matches.
     */
    private List<Student> studentsForSession(AttendanceSession session) {
        List<Student> classStudents = studentRepository.findByClassEntityId(session.getClassEntity().getId());
        Batch batch = session.getBatch();

        return classStudents.stream()
                .filter(Student::isActive)
                .filter(s -> batch.isWholeClass() || batch.getLabel().equalsIgnoreCase(s.getBatchLabel()))
                .toList();
    }

    private AttendanceSessionDto buildSessionDto(AttendanceSession session) {
        AttendanceSessionDto base = attendanceMapper.toBaseDto(session);

        List<Student> sessionStudents = studentsForSession(session);
        List<AttendanceRecord> records = recordRepository.findBySessionId(session.getId());
        Map<Long, AttendanceStatus> statusByStudentId = records.stream()
                .collect(Collectors.toMap(r -> r.getStudent().getId(), AttendanceRecord::getStatus));

        List<AttendanceSessionDto.StudentRecord> studentRecords = sessionStudents.stream()
                .map(s -> new AttendanceSessionDto.StudentRecord(
                        s.getId(), s.getRollNo(), s.getName(), statusByStudentId.get(s.getId())))
                .toList();

        return new AttendanceSessionDto(
                base.id(), base.classId(), base.className(), base.subjectId(), base.subjectName(),
                base.batchId(), base.batchLabel(), base.sessionDate(), base.slot(),
                base.lockedById(), base.lockedByName(), session.isSubmitted(), studentRecords);
    }
}
