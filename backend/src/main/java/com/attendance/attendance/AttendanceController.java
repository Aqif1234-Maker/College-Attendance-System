package com.attendance.attendance;

import com.attendance.common.ApiResponse;
import com.attendance.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/attendance")
@PreAuthorize("hasAnyRole('TEACHER', 'CLASS_COORDINATOR')")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final CurrentUser currentUser;

    public AttendanceController(AttendanceService attendanceService, CurrentUser currentUser) {
        this.attendanceService = attendanceService;
        this.currentUser = currentUser;
    }

    @PostMapping("/sessions")
    public ApiResponse<AttendanceSessionDto> openSession(@Valid @RequestBody OpenAttendanceRequest request) {
        return ApiResponse.ok("Attendance session ready", attendanceService.openSession(request, currentUser.getUserId()));
    }

    @GetMapping("/sessions/{id}")
    public ApiResponse<AttendanceSessionDto> getSession(@PathVariable Long id) {
        return ApiResponse.ok(attendanceService.getSession(id, currentUser.getUserId()));
    }

    @PostMapping("/submit")
    public ApiResponse<AttendanceSessionDto> submit(@Valid @RequestBody MarkAttendanceRequest request) {
        return ApiResponse.ok("Attendance submitted", attendanceService.submit(request, currentUser.getUserId()));
    }

    @GetMapping("/history")
    public ApiResponse<List<AttendanceSessionDto>> history() {
        return ApiResponse.ok(attendanceService.history(currentUser.getUserId()));
    }
}