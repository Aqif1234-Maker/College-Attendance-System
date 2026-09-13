package com.attendance.oe;

import com.attendance.audit.AuditLogService;
import com.attendance.common.ApiException;
import com.attendance.user.Role;
import com.attendance.user.User;
import com.attendance.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OeTeachingSlotService {

    private final OeTeachingSlotRepository oeTeachingSlotRepository;
    private final OeSubjectRepository oeSubjectRepository;
    private final SemesterRepository semesterRepository;
    private final UserRepository userRepository;
    private final OeTeachingSlotMapper oeTeachingSlotMapper;
    private final AuditLogService auditLogService;

    public OeTeachingSlotService(
            OeTeachingSlotRepository oeTeachingSlotRepository,
            OeSubjectRepository oeSubjectRepository,
            SemesterRepository semesterRepository,
            UserRepository userRepository,
            OeTeachingSlotMapper oeTeachingSlotMapper,
            AuditLogService auditLogService) {
        this.oeTeachingSlotRepository = oeTeachingSlotRepository;
        this.oeSubjectRepository = oeSubjectRepository;
        this.semesterRepository = semesterRepository;
        this.userRepository = userRepository;
        this.oeTeachingSlotMapper = oeTeachingSlotMapper;
        this.auditLogService = auditLogService;
    }

    /**
     * Locks in a teaching mode for a semester. Whichever coordinator's request reaches the
     * database first wins — enforced by the DB unique constraint (semester_id, mode), not by
     * application-level timing, so this is safe even if two coordinators submit at the exact
     * same instant. The losing coordinator gets the "Cannot Assign" message from the poster.
     */
    public OeTeachingSlotDto assign(AssignOeSlotRequest request, Long assignerUserId) {
        Semester semester = semesterRepository.findById(request.semesterId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Semester not found"));

        if (semester.getStatus() == SemesterStatus.CLOSED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This semester is closed; OE setup cannot be changed");
        }

        User teacher = userRepository.findById(request.teacherId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Teacher not found"));

        if (teacher.getRole() != Role.TEACHER && teacher.getRole() != Role.CLASS_COORDINATOR) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Only users with the TEACHER or CLASS_COORDINATOR role can be assigned an OE slot");
        }

        User assigner = userRepository.findById(assignerUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Assigning user not found"));

        // Clean pre-check for a specific error message. The DB constraint below is the
        // actual race-safe guarantee — this check can still lose a race to a concurrent
        // request, which is exactly what the catch block below is for.
        Optional<OeTeachingSlot> existing = oeTeachingSlotRepository
                .findBySemesterIdAndMode(semester.getId(), request.mode());

        if (existing.isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, cannotAssignMessage(existing.get()));
        }

        OeTeachingSlot slot = new OeTeachingSlot();
        slot.setSemester(semester);
        slot.setMode(request.mode());
        slot.setTeacher(teacher);
        slot.setAssignedBy(assigner);

        OeTeachingSlot saved;
        try {
            saved = oeTeachingSlotRepository.saveAndFlush(slot);
        } catch (DataIntegrityViolationException ex) {
            // Another coordinator's request won the race between our pre-check and this insert.
            OeTeachingSlot winner = oeTeachingSlotRepository
                    .findBySemesterIdAndMode(semester.getId(), request.mode())
                    .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT,
                            "This teaching mode was just assigned by another coordinator."));
            throw new ApiException(HttpStatus.CONFLICT, cannotAssignMessage(winner));
        }

        OeTeachingSlotDto dto = oeTeachingSlotMapper.toDto(saved, false);
        auditLogService.record(assignerUserId, "OE_SLOT_ASSIGNED", "OeTeachingSlot", saved.getId(), null, dto);

        return dto;
    }

    /**
     * Only the coordinator who originally made the assignment may reassign or remove it —
     * per the poster: "Only the assigning coordinator can reassign/change it."
     */
    public OeTeachingSlotDto reassign(Long slotId, AssignOeSlotRequest request, Long requesterUserId) {
        OeTeachingSlot slot = findEntity(slotId);

        if (!slot.getAssignedBy().getId().equals(requesterUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Only the coordinator who made this assignment can change it");
        }

        if (oeSubjectRepository.existsByOeTeachingSlotId(slotId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "This slot already has a subject created against it and can no longer be reassigned");
        }

        User newTeacher = userRepository.findById(request.teacherId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Teacher not found"));

        OeTeachingSlotDto before = oeTeachingSlotMapper.toDto(slot, false);

        slot.setTeacher(newTeacher);

        OeTeachingSlotDto after = oeTeachingSlotMapper.toDto(slot, false);
        auditLogService.record(requesterUserId, "OE_SLOT_REASSIGNED", "OeTeachingSlot", slot.getId(), before, after);

        return after;
    }

    @Transactional(readOnly = true)
    public List<OeTeachingSlotDto> listBySemester(Long semesterId) {
        return oeTeachingSlotRepository.findBySemesterId(semesterId).stream()
                .map(slot -> oeTeachingSlotMapper.toDto(slot, oeSubjectRepository.existsByOeTeachingSlotId(slot.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OeTeachingSlotDto> listForTeacher(Long teacherId) {
        return oeTeachingSlotRepository.findByTeacherId(teacherId).stream()
                .map(slot -> oeTeachingSlotMapper.toDto(slot, oeSubjectRepository.existsByOeTeachingSlotId(slot.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public OeTeachingSlotDto get(Long id) {
        OeTeachingSlot slot = findEntity(id);
        return oeTeachingSlotMapper.toDto(slot, oeSubjectRepository.existsByOeTeachingSlotId(id));
    }

    private String cannotAssignMessage(OeTeachingSlot existing) {
        return "Cannot Assign — this teaching mode is already assigned to " + existing.getTeacher().getFullName()
                + " by " + existing.getAssignedBy().getFullName()
                + ". Only the assigning coordinator can reassign or modify this.";
    }

    private OeTeachingSlot findEntity(Long id) {
        return oeTeachingSlotRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "OE teaching slot not found"));
    }
}