package com.attendance.class_;

import com.attendance.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/classes")
public class ClassController {

    private final ClassService classService;
    private final com.attendance.security.CurrentUser currentUser;

    public ClassController(ClassService classService, com.attendance.security.CurrentUser currentUser) {
        this.classService = classService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ClassDto> create(@Valid @RequestBody CreateClassRequest request) {
        return ApiResponse.ok("Class created", classService.create(request));
    }

    // Readable by any authenticated role — Coordinators/Teachers need class names
    // rendered in the dependent filter and dashboards.
    @GetMapping
    public ApiResponse<List<ClassDto>> list(@RequestParam(required = false) Long academicYearId) {
        return ApiResponse.ok(classService.list(academicYearId));
    }

    @GetMapping("/my-coordinated")
    @PreAuthorize("hasRole('CLASS_COORDINATOR')")
    public ApiResponse<List<ClassDto>> myCoordinatedClasses(
            @RequestParam(required = false) Long academicYearId) {
        return ApiResponse.ok(classService.listCoordinatedBy(currentUser.getUserId(), academicYearId));
    }

    @GetMapping("/{id}")
    public ApiResponse<ClassDto> get(@PathVariable Long id) {
        return ApiResponse.ok(classService.get(id));
    }

    @PutMapping("/{id}/coordinator")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ClassDto> assignCoordinator(
            @PathVariable Long id, @Valid @RequestBody AssignCoordinatorRequest request) {
        return ApiResponse.ok("Coordinator assigned", classService.assignCoordinator(id, request));
    }
}
