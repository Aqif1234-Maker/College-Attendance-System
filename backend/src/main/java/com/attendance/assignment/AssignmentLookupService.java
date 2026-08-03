package com.attendance.assignment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AssignmentLookupService {

    private final TeacherAssignmentRepository teacherAssignmentRepository;

    public AssignmentLookupService(TeacherAssignmentRepository teacherAssignmentRepository) {
        this.teacherAssignmentRepository = teacherAssignmentRepository;
    }

    public List<AssignedClassDto> getAssignedClasses(Long teacherId) {
        return teacherAssignmentRepository.findDistinctAssignedClasses(teacherId).stream()
                .map(c -> new AssignedClassDto(c.getId(), c.getName()))
                .toList();
    }

    public List<AssignedSubjectDto> getAssignedSubjects(Long teacherId, Long classId) {
        return teacherAssignmentRepository.findDistinctAssignedSubjects(teacherId, classId).stream()
                .map(s -> new AssignedSubjectDto(s.getId(), s.getName(), s.getType()))
                .toList();
    }

    public List<AssignedBatchDto> getAssignedBatches(Long teacherId, Long classId, Long subjectId) {
        return teacherAssignmentRepository.findAssignedBatches(teacherId, classId, subjectId).stream()
                .map(b -> new AssignedBatchDto(b.getId(), b.getLabel(), b.isWholeClass()))
                .toList();
    }

    public boolean isAssigned(Long teacherId, Long classId, Long subjectId, Long batchId) {
        return teacherAssignmentRepository
                .existsByTeacherIdAndClassEntityIdAndSubjectIdAndBatchId(teacherId, classId, subjectId, batchId);
    }

    public void assertAssigned(Long teacherId, Long classId, Long subjectId, Long batchId) {
        if (!isAssigned(teacherId, classId, subjectId, batchId)) {
            throw com.attendance.common.ApiException.forbidden(
                    "You are not assigned to this class/subject/batch combination");
        }
    }
}