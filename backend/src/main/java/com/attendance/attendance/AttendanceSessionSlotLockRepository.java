package com.attendance.attendance;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceSessionSlotLockRepository extends JpaRepository<AttendanceSessionSlotLock, Long> {
}
