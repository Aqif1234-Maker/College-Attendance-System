package com.attendance.academicyear;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {

    Optional<AcademicYear> findByCurrentTrue();

    boolean existsByLabel(String label);

    @Modifying
    @Query("update AcademicYear y set y.current = false where y.current = true and y.id <> :targetId")
    int clearCurrentExcept(Long targetId);
}
