package com.attendance.assignment;

import com.attendance.batch.Batch;
import com.attendance.batch.BatchRepository;
import com.attendance.common.ApiException;
import com.attendance.oe.OeSubject;
import com.attendance.oe.OeSubjectRepository;
import com.attendance.oe.OeTeachingSlot;
import com.attendance.oe.OeTeachingSlotMode;
import com.attendance.oe.OeTeachingSlotRepository;
import com.attendance.subject.Subject;
import com.attendance.subject.SubjectRepository;
import com.attendance.subject.SubjectType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AssignmentLookupService {

    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final OeTeachingSlotRepository oeTeachingSlotRepository;
    private final OeSubjectRepository oeSubjectRepository;
    private final SubjectRepository subjectRepository;
    private final BatchRepository batchRepository;

    public AssignmentLookupService(
            TeacherAssignmentRepository teacherAssignmentRepository,
            OeTeachingSlotRepository oeTeachingSlotRepository,
            OeSubjectRepository oeSubjectRepository,
            SubjectRepository subjectRepository,
            BatchRepository batchRepository) {
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.oeTeachingSlotRepository = oeTeachingSlotRepository;
        this.oeSubjectRepository = oeSubjectRepository;
        this.subjectRepository = subjectRepository;
        this.batchRepository = batchRepository;
    }

    public List<AssignedClassDto> getAssignedClasses(Long teacherId) {
        // Keyed by class id to de-duplicate a class the teacher reaches via both
        // a normal TeacherAssignment and an OE slot.
        Map<Long, AssignedClassDto> byId = new LinkedHashMap<>();

        teacherAssignmentRepository.findDistinctAssignedClasses(teacherId)
                .forEach(c -> byId.put(c.getId(), new AssignedClassDto(c.getId(), c.getName(), c.getName())));

        for (OeSubject oeSubject : getOeSubjectLinksForTeacher(teacherId)) {
            Subject subject = oeSubject.getSubject();
            var classEntity = subject.getClassEntity();
            String displayName = classDisplayNameForOeSubject(oeSubject);
            byId.putIfAbsent(classEntity.getId(),
                    new AssignedClassDto(classEntity.getId(), classEntity.getName(), displayName));
        }

        return List.copyOf(byId.values());
    }

    public List<AssignedSubjectDto> getAssignedSubjects(Long teacherId, Long classId) {
        Map<Long, AssignedSubjectDto> byId = new LinkedHashMap<>();

        teacherAssignmentRepository.findDistinctAssignedSubjects(teacherId, classId)
                .forEach(s -> byId.put(s.getId(), new AssignedSubjectDto(s.getId(), s.getName(), s.getType())));

        for (Subject oeSubject : getOeSubjectsForTeacher(teacherId)) {
            if (oeSubject.getClassEntity().getId().equals(classId)) {
                byId.putIfAbsent(oeSubject.getId(),
                        new AssignedSubjectDto(oeSubject.getId(), oeSubject.getName(), oeSubject.getType()));
            }
        }

        return List.copyOf(byId.values());
    }

    public List<AssignedBatchDto> getAssignedBatches(Long teacherId, Long classId, Long subjectId) {
        // If this subject is one of the teacher's OE subjects, its batches (the
        // sentinel "Entire Class" row) are always visible to them — OE has no
        // separate batch-level assignment concept the way Practical subjects do.
        boolean isMyOeSubject = getOeSubjectsForTeacher(teacherId).stream()
                .anyMatch(s -> s.getId().equals(subjectId) && s.getClassEntity().getId().equals(classId));

        if (isMyOeSubject) {
            List<Batch> batches = batchRepository.findBySubjectId(subjectId);
            return batches.stream()
                    .map(b -> new AssignedBatchDto(b.getId(), b.getLabel(), b.isWholeClass()))
                    .toList();
        }

        return teacherAssignmentRepository.findAssignedBatches(teacherId, classId, subjectId).stream()
                .map(b -> new AssignedBatchDto(b.getId(), b.getLabel(), b.isWholeClass()))
                .toList();
    }

    /**
     * The single server-side authorization check every attendance and report write/read
     * must call before touching data. Never trust that a valid-looking class/subject/batch
     * combination in a request body actually belongs to the caller — always re-verify here.
     * OE subjects are authorized via oe_teaching_slots ownership, not teacher_assignments,
     * since a Coordinator explicitly locks OE access outside the normal assignment flow.
     */
    public boolean isAssigned(Long teacherId, Long classId, Long subjectId, Long batchId) {
        Subject subject = subjectRepository.findById(subjectId).orElse(null);

        if (subject != null && subject.getType() == SubjectType.OE) {
            return getOeSubjectsForTeacher(teacherId).stream()
                    .anyMatch(s -> s.getId().equals(subjectId) && s.getClassEntity().getId().equals(classId));
        }

        return teacherAssignmentRepository
                .existsByTeacherIdAndClassEntityIdAndSubjectIdAndBatchId(teacherId, classId, subjectId, batchId);
    }

    public void assertAssigned(Long teacherId, Long classId, Long subjectId, Long batchId) {
        if (!isAssigned(teacherId, classId, subjectId, batchId)) {
            throw ApiException.forbidden("You are not assigned to this class/subject/batch combination");
        }
    }

    private List<Subject> getOeSubjectsForTeacher(Long teacherId) {
        return getOeSubjectLinksForTeacher(teacherId).stream()
                .map(oeSubject -> subjectRepository.findById(oeSubject.getSubject().getId()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private List<OeSubject> getOeSubjectLinksForTeacher(Long teacherId) {
        List<OeTeachingSlot> slots = oeTeachingSlotRepository.findByTeacherId(teacherId);
        return slots.stream()
                .map(slot -> oeSubjectRepository.findByOeTeachingSlotId(slot.getId()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private String classDisplayNameForOeSubject(OeSubject oeSubject) {
        OeTeachingSlot slot = oeSubject.getOeTeachingSlot();
        if (slot.getMode() == OeTeachingSlotMode.COMBINED) {
            return slot.getSemester().getClassA().getName()
                    + " + "
                    + slot.getSemester().getClassB().getName()
                    + " (Combined OE)";
        }
        return oeSubject.getSubject().getClassEntity().getName();
    }
}
