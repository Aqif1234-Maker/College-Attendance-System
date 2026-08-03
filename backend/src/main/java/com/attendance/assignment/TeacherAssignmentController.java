package com.attendance.assignment;

import com.attendance.common.ApiResponse;
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
@RequestMapping("/assignments")
public class TeacherAssignmentController {

    private final TeacherAssignmentService teacherAssignmentService;
    private final AssignmentLookupService assignmentLookupService;
    private final com.attendance.security.CurrentUser currentUser;

    public TeacherAssignmentController(
            TeacherAssignmentService teacherAssignmentService,
            AssignmentLookupService assignmentLookupService,
            com.attendance.security.CurrentUser currentUser) {
        this.teacherAssignmentService = teacherAssignmentService;
        this.assignmentLookupService = assignmentLookupService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLASS_COORDINATOR')")
    public ApiResponse<TeacherAssignmentDto> assign(@Valid @RequestBody AssignTeacherRequest request) {
        return ApiResponse.ok("Teacher assigned", teacherAssignmentService.assign(request, currentUser.getUserId()));
    }

    @GetMapping
    @PreAuthorize("hasRole('CLASS_COORDINATOR')")
    public ApiResponse<List<TeacherAssignmentDto>> listByTeacher(@RequestParam Long teacherId) {
        return ApiResponse.ok(teacherAssignmentService.listByTeacher(teacherId));
    }

    @GetMapping("/by-class")
    @PreAuthorize("hasRole('CLASS_COORDINATOR')")
    public ApiResponse<List<TeacherAssignmentDto>> listByClass(@RequestParam Long classId) {
        return ApiResponse.ok(teacherAssignmentService.listByClass(classId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CLASS_COORDINATOR')")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        teacherAssignmentService.remove(id);
        return ApiResponse.ok("Assignment removed", null);
    }

    // Read-only cascading-dropdown endpoints for the logged-in teacher (or coordinator
    // acting as a teacher on their own self-assignment). Used by the frontend's shared
    // DependentFilter component for both Attendance and Reports.
    @GetMapping("/my-classes")
    public ApiResponse<List<AssignedClassDto>> myClasses() {
        return ApiResponse.ok(assignmentLookupService.getAssignedClasses(currentUser.getUserId()));
    }

    @GetMapping("/my-subjects")
    public ApiResponse<List<AssignedSubjectDto>> mySubjects(@RequestParam Long classId) {
        return ApiResponse.ok(assignmentLookupService.getAssignedSubjects(currentUser.getUserId(), classId));
    }

    @GetMapping("/my-batches")
    public ApiResponse<List<AssignedBatchDto>> myBatches(@RequestParam Long classId, @RequestParam Long subjectId) {
        return ApiResponse.ok(assignmentLookupService.getAssignedBatches(currentUser.getUserId(), classId, subjectId));
    }
}
