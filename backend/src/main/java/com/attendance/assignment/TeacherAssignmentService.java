package com.attendance.assignment;

import com.attendance.batch.Batch;
import com.attendance.batch.BatchRepository;
import com.attendance.class_.ClassEntity;
import com.attendance.class_.ClassRepository;
import com.attendance.common.ApiException;
import com.attendance.subject.Subject;
import com.attendance.subject.SubjectRepository;
import com.attendance.user.Role;
import com.attendance.user.User;
import com.attendance.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TeacherAssignmentService {

    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final UserRepository userRepository;
    private final ClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final BatchRepository batchRepository;
    private final TeacherAssignmentMapper teacherAssignmentMapper;

    public TeacherAssignmentService(
            TeacherAssignmentRepository teacherAssignmentRepository,
            UserRepository userRepository,
            ClassRepository classRepository,
            SubjectRepository subjectRepository,
            BatchRepository batchRepository,
            TeacherAssignmentMapper teacherAssignmentMapper) {
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.userRepository = userRepository;
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
        this.batchRepository = batchRepository;
        this.teacherAssignmentMapper = teacherAssignmentMapper;
    }

    public TeacherAssignmentDto assign(AssignTeacherRequest request, Long assignerUserId) {
        User teacher = userRepository.findById(request.teacherId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Teacher not found"));

        // A Coordinator is a Teacher with extra responsibilities, so both roles are valid targets.
        if (teacher.getRole() != Role.TEACHER && teacher.getRole() != Role.CLASS_COORDINATOR) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Only users with the TEACHER or CLASS_COORDINATOR role can be assigned to attendance duties");
        }

        if (!teacher.isActive()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This teacher's account is deactivated");
        }

        User assigner = userRepository.findById(assignerUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Assigning user not found"));

        ClassEntity classEntity = classRepository.findById(request.classId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Class not found"));

        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Subject not found"));

        Batch batch = batchRepository.findById(request.batchId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Batch not found"));

        // Referential consistency: batch must belong to subject, subject must belong to class.
        if (!subject.getClassEntity().getId().equals(classEntity.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Subject does not belong to the specified class");
        }
        if (!batch.getSubject().getId().equals(subject.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Batch does not belong to the specified subject");
        }

        TeacherAssignment existingForTeachingSlot = teacherAssignmentRepository
                .findByClassEntityIdAndSubjectIdAndBatchId(classEntity.getId(), subject.getId(), batch.getId())
                .orElse(null);

        if (existingForTeachingSlot != null) {
            if (existingForTeachingSlot.getTeacher().getId().equals(teacher.getId())) {
                return teacherAssignmentMapper.toDto(existingForTeachingSlot);
            }
            teacherAssignmentRepository.delete(existingForTeachingSlot);
            teacherAssignmentRepository.flush();
        }

        TeacherAssignment assignment = new TeacherAssignment();
        assignment.setTeacher(teacher);
        assignment.setClassEntity(classEntity);
        assignment.setSubject(subject);
        assignment.setBatch(batch);
        assignment.setAssignedBy(assigner);

        return teacherAssignmentMapper.toDto(teacherAssignmentRepository.save(assignment));
    }

    @Transactional(readOnly = true)
    public List<TeacherAssignmentDto> listByTeacher(Long teacherId) {
        return teacherAssignmentRepository.findByTeacherId(teacherId).stream()
                .map(teacherAssignmentMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeacherAssignmentDto> listByClass(Long classId) {
        return teacherAssignmentRepository.findByClassEntityId(classId).stream()
                .map(teacherAssignmentMapper::toDto)
                .toList();
    }

    public void remove(Long id) {
        TeacherAssignment assignment = teacherAssignmentRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Assignment not found"));
        teacherAssignmentRepository.delete(assignment);
    }
}
