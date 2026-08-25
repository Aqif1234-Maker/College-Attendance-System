package com.attendance.attendance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceSessionSlotLockRepository extends JpaRepository<AttendanceSessionSlotLock, Long> {

    void deleteBySessionId(Long sessionId);

    List<AttendanceSessionSlotLock> findByClassEntityIdAndSessionDateAndPeriodUnitIn(
            Long classId, LocalDate sessionDate, List<Byte> periodUnits);
}
