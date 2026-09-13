package com.attendance.oe;

import com.attendance.audit.AuditLogService;
import com.attendance.batch.Batch;
import com.attendance.batch.BatchRepository;
import com.attendance.class_.ClassEntity;
import com.attendance.common.ApiException;
import com.attendance.subject.Subject;
import com.attendance.subject.SubjectRepository;
import com.attendance.subject.SubjectType;
import com.attendance.user.User;
import com.attendance.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OeSubjectService {

    private static final String OE_BATCH_LABEL = "Enrolled Students (OE)";

    private final OeSubjectRepository oeSubjectRepository;
    private final OeTeachingSlotRepository oeTeachingSlotRepository;
    private final SubjectRepository subjectRepository;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;
    private final SemesterService semesterService;
    private final OeSubjectMapper oeSubjectMapper;
    private final AuditLogService auditLogService;

    public OeSubjectService(
            OeSubjectRepository oeSubjectRepository,
            OeTeachingSlotRepository oeTeachingSlotRepository,
            SubjectRepository subjectRepository,
            BatchRepository batchRepository,
            UserRepository userRepository,
            SemesterService semesterService,
            OeSubjectMapper oeSubjectMapper,
            AuditLogService auditLogService) {
        this.oeSubjectRepository = oeSubjectRepository;
        this.oeTeachingSlotRepository = oeTeachingSlotRepository;
        this.subjectRepository = subjectRepository;
        this.batchRepository = batchRepository;
        this.userRepository = userRepository;
        this.semesterService = semesterService;
        this.oeSubjectMapper = oeSubjectMapper;
        this.auditLogService = auditLogService;
    }

    public OeSubjectDto create(CreateOeSubjectRequest request, Long teacherId) {
        OeTeachingSlot slot = oeTeachingSlotRepository.findById(request.oeTeachingSlotId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Teaching slot not found"));

        if (!slot.getTeacher().getId().equals(teacherId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not hold this teaching slot");
        }

        if (oeSubjectRepository.existsByOeTeachingSlotId(slot.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "A subject has already been created for this teaching slot");
        }

        ClassEntity classEntity = semesterService.resolveClassForMode(
                slot.getSemester().getId(), slot.getMode());

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Teacher not found"));

        if (subjectRepository.existsByClassEntityIdAndNameIgnoreCase(classEntity.getId(), request.name())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "A subject named '" + request.name() + "' already exists for this class");
        }

        // Phase 2, step 1: a real row in the existing subjects table.
        Subject subject = new Subject();
        subject.setClassEntity(classEntity);
        subject.setName(request.name());
        subject.setType(SubjectType.OE);
        subject.setCreatedBy(teacher);
        Subject savedSubject = subjectRepository.save(subject);

        // Phase 2, step 1b: every subject needs at least one batch, since
        // attendance_sessions.batch_id is NOT NULL. OE subjects get a sentinel
        // batch, but the roster itself comes from oe_enrollments.
        Batch wholeClassBatch = new Batch();
        wholeClassBatch.setSubject(savedSubject);
        wholeClassBatch.setLabel(OE_BATCH_LABEL);
        wholeClassBatch.setWholeClass(true);
        batchRepository.save(wholeClassBatch);

        // Phase 2, step 2: link that subject back to the slot that authorized it.
        OeSubject oeSubject = new OeSubject();
        oeSubject.setSubject(savedSubject);
        oeSubject.setOeTeachingSlot(slot);
        OeSubject savedOeSubject = oeSubjectRepository.save(oeSubject);

        OeSubjectDto dto = oeSubjectMapper.toDto(savedOeSubject);
        auditLogService.record(teacherId, "OE_SUBJECT_CREATED", "OeSubject", savedOeSubject.getId(), null, dto);

        return dto;
    }

    @Transactional(readOnly = true)
    public OeSubjectDto getBySubjectId(Long subjectId) {
        OeSubject oeSubject = oeSubjectRepository.findBySubjectId(subjectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "This subject is not an OE subject"));
        return oeSubjectMapper.toDto(oeSubject);
    }

    @Transactional(readOnly = true)
    public OeSubjectDto getBySlotId(Long slotId) {
        OeSubject oeSubject = oeSubjectRepository.findByOeTeachingSlotId(slotId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No subject has been created for this slot yet"));
        return oeSubjectMapper.toDto(oeSubject);
    }
}
