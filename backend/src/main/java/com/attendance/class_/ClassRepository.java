package com.attendance.class_;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassRepository extends JpaRepository<ClassEntity, Long> {

    boolean existsByAcademicYearIdAndNameIgnoreCase(Long academicYearId, String name);

    List<ClassEntity> findByAcademicYearId(Long academicYearId);
}