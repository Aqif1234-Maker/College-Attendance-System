package com.attendance.oe;

import com.attendance.common.ApiException;
import com.attendance.student.Student;
import com.attendance.student.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OeEnrollmentService {

    private final OeEnrollmentRepository oeEnrollmentRepository;
    private final OeSubjectRepository oeSubjectRepository;
    private final StudentRepository studentRepository;
    private final OeEnrollmentMapper oeEnrollmentMapper;

    public OeEnrollmentService(
            OeEnrollmentRepository oeEnrollmentRepository,
            OeSubjectRepository oeSubjectRepository,
            StudentRepository studentRepository,
            OeEnrollmentMapper oeEnrollmentMapper) {
        this.oeEnrollmentRepository = oeEnrollmentRepository;
        this.oeSubjectRepository = oeSubjectRepository;
        this.studentRepository = studentRepository;
        this.oeEnrollmentMapper = oeEnrollmentMapper;
    }

    /**
     * Enrolls existing students into an OE subject — never creates a new Student row,
     * per the poster's "students never select/are never created here" rule.
     *
     * NOTE: eligibility filtering (which classes' students are valid candidates for a
     * given slot's mode) is currently the frontend's responsibility, populating the
     * checkbox list correctly — this method trusts the student ids it's given, beyond
     * confirming they exist and are active. See OeSubjectService's flagged open question
     * on Combined-mode class ownership; this is the same underlying gap surfacing here.
     */
    public List<OeEnrollmentDto> enroll(EnrollStudentsRequest request, Long teacherId) {
        OeSubject oeSubject = oeSubjectRepository.findById(request.oeSubjectId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "OE subject not found"));

        if (!oeSubject.getOeTeachingSlot().getTeacher().getId().equals(teacherId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not teach this OE subject");
        }

        List<OeEnrollmentDto> results = new ArrayList<>();

        for (Long studentId : request.studentIds()) {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student " + studentId + " not found"));

            if (!student.isActive()) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Student " + student.getName() + " is not active and cannot be enrolled");
            }

            if (oeEnrollmentRepository.existsByOeSubjectIdAndStudentId(oeSubject.getId(), studentId)) {
                continue;
            }

            OeEnrollment enrollment = new OeEnrollment();
            enrollment.setOeSubject(oeSubject);
            enrollment.setStudent(student);

            results.add(oeEnrollmentMapper.toDto(oeEnrollmentRepository.save(enrollment)));
        }

        return results;
    }

    public void unenroll(Long oeSubjectId, Long studentId, Long teacherId) {
        OeSubject oeSubject = oeSubjectRepository.findById(oeSubjectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "OE subject not found"));

        if (!oeSubject.getOeTeachingSlot().getTeacher().getId().equals(teacherId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not teach this OE subject");
        }

        oeEnrollmentRepository.deleteByOeSubjectIdAndStudentId(oeSubjectId, studentId);
    }

    @Transactional(readOnly = true)
    public List<OeEnrollmentDto> listBySubject(Long oeSubjectId) {
        return oeEnrollmentRepository.findByOeSubjectId(oeSubjectId).stream()
                .map(oeEnrollmentMapper::toDto)
                .toList();
    }
}