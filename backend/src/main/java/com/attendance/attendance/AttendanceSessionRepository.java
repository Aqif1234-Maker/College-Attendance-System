package com.attendance.attendance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    List<AttendanceSession> findByBatchId(Long batchId);

    Optional<AttendanceSession> findByClassEntityIdAndSubjectIdAndBatchIdAndSessionDateAndSlot(
            Long classId, Long subjectId, Long batchId, LocalDate sessionDate, int slot);

    List<AttendanceSession> findByLockedByIdOrderBySessionDateDesc(Long teacherId);

    List<AttendanceSession> findByClassEntityIdAndSessionDate(Long classId, LocalDate sessionDate);

    List<AttendanceSession> findByClassEntityIdAndSubjectIdAndBatchIdAndSessionDateBetween(
            Long classId, Long subjectId, Long batchId, LocalDate from, LocalDate to);
}
