package com.attendance.attendance;

import com.attendance.assignment.AssignmentLookupService;
import com.attendance.batch.Batch;
import com.attendance.batch.BatchRepository;
import com.attendance.class_.ClassEntity;
import com.attendance.class_.ClassRepository;
import com.attendance.common.ApiException;
import com.attendance.oe.OeEnrollment;
import com.attendance.oe.OeEnrollmentRepository;
import com.attendance.oe.OeSubject;
import com.attendance.oe.OeSubjectRepository;
import com.attendance.oe.OeTeachingSlotMode;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendanceService {

    private static final int MAX_THEORY_SLOT = 7;
    private static final int MAX_PRACTICAL_SLOT = 4;
    private static final long WHOLE_CLASS_SCOPE_KEY = 0L;

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
    private final OeSubjectRepository oeSubjectRepository;
    private final OeEnrollmentRepository oeEnrollmentRepository;

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
            AttendanceMapper attendanceMapper,
            OeSubjectRepository oeSubjectRepository,
            OeEnrollmentRepository oeEnrollmentRepository) {
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
        this.oeSubjectRepository = oeSubjectRepository;
        this.oeEnrollmentRepository = oeEnrollmentRepository;
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

        List<ClassEntity> lockClasses = classesToLockForSession(classEntity, subject);

        Optional<AttendanceSession> overlappingSession = findConflictingSession(
                lockClasses, batch, request.sessionDate(), requestedWindow);

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
            createSlotLocks(session, requestedWindow, lockClasses);
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
        int maxSlot = (type == SubjectType.PR) ? MAX_PRACTICAL_SLOT : MAX_THEORY_SLOT;
        if (slot < 1 || slot > maxSlot) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Slot " + slot + " is invalid for a " + type + " subject (valid range: 1-" + maxSlot + ")");
        }
    }

    private Optional<AttendanceSession> findConflictingSession(
            List<ClassEntity> lockClasses,
            Batch requestedBatch,
            java.time.LocalDate sessionDate,
            SlotSchedule.SlotWindow requestedWindow) {
        List<Byte> requestedPeriodUnits = requestedWindow.periodUnits().stream()
                .map(Integer::byteValue)
                .toList();

        long requestedScopeKey = scopeKeyForBatch(requestedBatch);

        return lockClasses.stream()
                .flatMap(lockClass -> sessionSlotLockRepository
                        .findByClassEntityIdAndSessionDateAndPeriodUnitIn(
                                lockClass.getId(), sessionDate, requestedPeriodUnits)
                        .stream())
                .filter(lock -> conflictsWithRequestedScope(lock, requestedBatch, requestedScopeKey))
                .sorted(Comparator.comparing(AttendanceSessionSlotLock::getPeriodUnit))
                .map(AttendanceSessionSlotLock::getSession)
                .findFirst();
    }

    private boolean conflictsWithRequestedScope(
            AttendanceSessionSlotLock existingLock, Batch requestedBatch, long requestedScopeKey) {
        // A whole-class session blocks everyone in the class for that period.
        if (existingLock.getScopeKey() == WHOLE_CLASS_SCOPE_KEY || requestedBatch.isWholeClass()) {
            return true;
        }
        // Practicals may run in parallel, but only if they are for different batches.
        return existingLock.getScopeKey() == requestedScopeKey;
    }

    private String buildSlotAlreadyBookedMessage(AttendanceSession session, SlotSchedule.SlotWindow bookedWindow) {
        return "Slot " + bookedWindow.label() + " is already booked on "
                + session.getSessionDate() + " by " + session.getLockedBy().getFullName()
                + " for " + session.getSubject().getName() + " (" + session.getBatch().getLabel()
                + "). Please choose another slot.";
    }

    private void createSlotLocks(
            AttendanceSession session, SlotSchedule.SlotWindow requestedWindow, List<ClassEntity> lockClasses) {
        long scopeKey = scopeKeyForBatch(session.getBatch());
        for (ClassEntity lockClass : lockClasses) {
            for (Integer periodUnit : requestedWindow.periodUnits()) {
                AttendanceSessionSlotLock lock = new AttendanceSessionSlotLock();
                lock.setSession(session);
                lock.setClassEntity(lockClass);
                lock.setSessionDate(session.getSessionDate());
                lock.setPeriodUnit(periodUnit.byteValue());
                lock.setScopeKey(scopeKey);
                sessionSlotLockRepository.save(lock);
            }
        }
        sessionSlotLockRepository.flush();
    }

    private long scopeKeyForBatch(Batch batch) {
        return batch.isWholeClass() ? WHOLE_CLASS_SCOPE_KEY : batch.getId();
    }

    private List<ClassEntity> classesToLockForSession(ClassEntity classEntity, Subject subject) {
        if (subject.getType() != SubjectType.OE) {
            return List.of(classEntity);
        }

        OeSubject oeSubject = oeSubjectRepository.findBySubjectId(subject.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "OE subject metadata missing for subject " + subject.getId()));

        if (oeSubject.getOeTeachingSlot().getMode() != OeTeachingSlotMode.COMBINED) {
            return List.of(classEntity);
        }

        return List.of(
                oeSubject.getOeTeachingSlot().getSemester().getClassA(),
                oeSubject.getOeTeachingSlot().getSemester().getClassB());
    }

    private String classNameForSessionDisplay(AttendanceSession session, String fallbackClassName) {
        Subject subject = session.getSubject();
        if (subject.getType() != SubjectType.OE) {
            return fallbackClassName;
        }

        OeSubject oeSubject = oeSubjectRepository.findBySubjectId(subject.getId()).orElse(null);
        if (oeSubject == null || oeSubject.getOeTeachingSlot().getMode() != OeTeachingSlotMode.COMBINED) {
            return fallbackClassName;
        }

        return oeSubject.getOeTeachingSlot().getSemester().getClassA().getName()
                + " + "
                + oeSubject.getOeTeachingSlot().getSemester().getClassB().getName()
                + " (Combined OE)";
    }

    private List<Student> studentsForSession(AttendanceSession session) {
        Subject subject = session.getSubject();

        if (subject.getType() == SubjectType.OE) {
            OeSubject oeSubject = oeSubjectRepository.findBySubjectId(subject.getId())
                    .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "OE subject metadata missing for subject " + subject.getId()));

            return oeEnrollmentRepository.findByOeSubjectId(oeSubject.getId()).stream()
                    .map(OeEnrollment::getStudent)
                    .filter(Student::isActive)
                    .toList();
        }

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
                base.id(), base.classId(), classNameForSessionDisplay(session, base.className()),
                base.subjectId(), base.subjectName(),
                base.batchId(), base.batchLabel(), base.sessionDate(), base.slot(),
                base.lockedById(), base.lockedByName(), session.isSubmitted(), studentRecords);
    }
}
