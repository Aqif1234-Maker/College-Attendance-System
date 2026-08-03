package com.attendance.class_;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassCoordinatorRepository extends JpaRepository<ClassCoordinator, Long> {

    Optional<ClassCoordinator> findByClassEntityId(Long classId);

    List<ClassCoordinator> findByUserId(Long userId);

    List<ClassCoordinator> findByUserIdAndClassEntityAcademicYearId(Long userId, Long academicYearId);

    boolean existsByClassEntityId(Long classId);
}
