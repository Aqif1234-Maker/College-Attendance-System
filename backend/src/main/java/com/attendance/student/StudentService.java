package com.attendance.student;

import com.attendance.class_.ClassEntity;
import com.attendance.class_.ClassRepository;
import com.attendance.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final ClassRepository classRepository;
    private final StudentMapper studentMapper;

    public StudentService(StudentRepository studentRepository, ClassRepository classRepository, StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.classRepository = classRepository;
        this.studentMapper = studentMapper;
    }

    public StudentDto create(CreateStudentRequest request) {
        ClassEntity classEntity = classRepository.findById(request.classId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Class not found"));

        if (studentRepository.existsByClassEntityIdAndRollNoIgnoreCase(classEntity.getId(), request.rollNo())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Roll number '" + request.rollNo() + "' already exists in this class");
        }

        Student student = new Student();
        student.setClassEntity(classEntity);
        student.setRollNo(request.rollNo());
        student.setName(request.name());
        student.setBatchLabel(request.batchLabel());
        student.setActive(true);

        return studentMapper.toDto(studentRepository.save(student));
    }

    @Transactional(readOnly = true)
    public List<StudentDto> listByClass(Long classId, String batchLabel) {
        List<Student> students = (batchLabel == null || batchLabel.isBlank())
                ? studentRepository.findByClassEntityId(classId)
                : studentRepository.findByClassEntityIdAndBatchLabel(classId, batchLabel);

        return students.stream().map(studentMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public StudentDto get(Long id) {
        return studentMapper.toDto(findEntity(id));
    }

    public StudentDto update(Long id, CreateStudentRequest request) {
        Student student = findEntity(id);

        if (!student.getRollNo().equalsIgnoreCase(request.rollNo())
                && studentRepository.existsByClassEntityIdAndRollNoIgnoreCase(student.getClassEntity().getId(), request.rollNo())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Roll number '" + request.rollNo() + "' already exists in this class");
        }

        student.setRollNo(request.rollNo());
        student.setName(request.name());
        student.setBatchLabel(request.batchLabel());

        return studentMapper.toDto(student);
    }

    public void setActive(Long id, boolean active) {
        Student student = findEntity(id);
        student.setActive(active);
    }

    private Student findEntity(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"));
    }
}