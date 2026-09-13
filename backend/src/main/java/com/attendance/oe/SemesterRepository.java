package com.attendance.oe;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SemesterRepository extends JpaRepository<Semester, Long> {

    boolean existsByAcademicYearIdAndLabelIgnoreCase(Long academicYearId, String label);

    List<Semester> findByAcademicYearId(Long academicYearId);

    List<Semester> findByStatus(SemesterStatus status);
}