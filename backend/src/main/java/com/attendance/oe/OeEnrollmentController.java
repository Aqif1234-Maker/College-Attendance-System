package com.attendance.oe;

import com.attendance.common.ApiResponse;
import com.attendance.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/oe/enrollments")
@PreAuthorize("hasAnyRole('TEACHER', 'CLASS_COORDINATOR')")
public class OeEnrollmentController {

    private final OeEnrollmentService oeEnrollmentService;
    private final CurrentUser currentUser;

    public OeEnrollmentController(OeEnrollmentService oeEnrollmentService, CurrentUser currentUser) {
        this.oeEnrollmentService = oeEnrollmentService;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ApiResponse<List<OeEnrollmentDto>> enroll(@Valid @RequestBody EnrollStudentsRequest request) {
        return ApiResponse.ok("Students enrolled", oeEnrollmentService.enroll(request, currentUser.getUserId()));
    }

    @DeleteMapping("/{oeSubjectId}/{studentId}")
    public ApiResponse<Void> unenroll(@PathVariable Long oeSubjectId, @PathVariable Long studentId) {
        oeEnrollmentService.unenroll(oeSubjectId, studentId, currentUser.getUserId());
        return ApiResponse.ok("Student unenrolled", null);
    }

    @GetMapping
    public ApiResponse<List<OeEnrollmentDto>> listBySubject(@RequestParam Long oeSubjectId) {
        return ApiResponse.ok(oeEnrollmentService.listBySubject(oeSubjectId));
    }
}