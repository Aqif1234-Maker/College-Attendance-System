package com.attendance.oe;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OeTeachingSlotRepository extends JpaRepository<OeTeachingSlot, Long> {

    List<OeTeachingSlot> findBySemesterId(Long semesterId);

    Optional<OeTeachingSlot> findBySemesterIdAndMode(Long semesterId, OeTeachingSlotMode mode);

    List<OeTeachingSlot> findByTeacherId(Long teacherId);

    boolean existsBySemesterIdAndMode(Long semesterId, OeTeachingSlotMode mode);
}