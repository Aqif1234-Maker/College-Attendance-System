package com.attendance.oe;

import com.attendance.academicyear.AcademicYear;
import com.attendance.academicyear.AcademicYearRepository;
import com.attendance.audit.AuditLogService;
import com.attendance.class_.ClassEntity;
import com.attendance.class_.ClassRepository;
import com.attendance.common.ApiException;
import com.attendance.user.User;
import com.attendance.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class SemesterService {

    private final SemesterRepository semesterRepository;
    private final AcademicYearRepository academicYearRepository;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;
    private final SemesterMapper semesterMapper;
    private final AuditLogService auditLogService;

    public SemesterService(
            SemesterRepository semesterRepository,
            AcademicYearRepository academicYearRepository,
            ClassRepository classRepository,
            UserRepository userRepository,
            SemesterMapper semesterMapper,
            AuditLogService auditLogService) {
        this.semesterRepository = semesterRepository;
        this.academicYearRepository = academicYearRepository;
        this.classRepository = classRepository;
        this.userRepository = userRepository;
        this.semesterMapper = semesterMapper;
        this.auditLogService = auditLogService;
    }

    public SemesterDto create(CreateSemesterRequest request, Long actorId) {
        AcademicYear year = academicYearRepository.findById(request.academicYearId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Academic year not found"));

        if (semesterRepository.existsByAcademicYearIdAndLabelIgnoreCase(year.getId(), request.label())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Semester '" + request.label() + "' already exists for this academic year");
        }

        ClassEntity classA = classRepository.findById(request.classAId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Division A class not found"));
        ClassEntity classB = classRepository.findById(request.classBId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Division B class not found"));

        if (classA.getId().equals(classB.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Division A and Division B must be different classes");
        }

        Semester semester = new Semester();
        semester.setAcademicYear(year);
        semester.setLabel(request.label());
        semester.setStatus(SemesterStatus.ACTIVE);
        semester.setClassA(classA);
        semester.setClassB(classB);

        Semester saved = semesterRepository.save(semester);
        SemesterDto dto = semesterMapper.toDto(saved);
        auditLogService.record(actorId, "SEMESTER_CREATED", "Semester", saved.getId(), null, dto);

        return dto;
    }

    @Transactional(readOnly = true)
    public List<SemesterDto> list(Long academicYearId) {
        List<Semester> semesters = (academicYearId == null)
                ? semesterRepository.findAll()
                : semesterRepository.findByAcademicYearId(academicYearId);
        return semesters.stream().map(semesterMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public SemesterDto get(Long id) {
        return semesterMapper.toDto(findEntity(id));
    }

    public SemesterDto close(Long id, Long actorId) {
        Semester semester = findEntity(id);

        if (semester.getStatus() == SemesterStatus.CLOSED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This semester is already closed");
        }

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Actor not found"));

        SemesterDto before = semesterMapper.toDto(semester);

        semester.setStatus(SemesterStatus.CLOSED);
        semester.setClosedAt(Instant.now());
        semester.setClosedBy(actor);

        SemesterDto after = semesterMapper.toDto(semester);
        auditLogService.record(actorId, "SEMESTER_CLOSED", "Semester", semester.getId(), before, after);

        return after;
    }

    /**
     * Resolves which class a given OE mode belongs to for this semester —
     * the single source of truth OeSubjectService uses instead of trusting
     * a client-supplied classId.
     */
    @Transactional(readOnly = true)
    public ClassEntity resolveClassForMode(Long semesterId, OeTeachingSlotMode mode) {
        Semester semester = findEntity(semesterId);
        return switch (mode) {
            case SEPARATE_A, COMBINED -> semester.getClassA();
            case SEPARATE_B -> semester.getClassB();
        };
    }

    private Semester findEntity(Long id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Semester not found"));
    }
}