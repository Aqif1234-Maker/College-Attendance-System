package com.attendance.oe;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OeSubjectRepository extends JpaRepository<OeSubject, Long> {

    boolean existsByOeTeachingSlotId(Long oeTeachingSlotId);

    Optional<OeSubject> findByOeTeachingSlotId(Long oeTeachingSlotId);

    Optional<OeSubject> findBySubjectId(Long subjectId);
}