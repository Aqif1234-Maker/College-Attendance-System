package com.attendance.oe;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OeEnrollmentRepository extends JpaRepository<OeEnrollment, Long> {

    List<OeEnrollment> findByOeSubjectId(Long oeSubjectId);

    Optional<OeEnrollment> findByOeSubjectIdAndStudentId(Long oeSubjectId, Long studentId);

    boolean existsByOeSubjectIdAndStudentId(Long oeSubjectId, Long studentId);

    void deleteByOeSubjectIdAndStudentId(Long oeSubjectId, Long studentId);
}