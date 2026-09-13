package com.attendance.oe;

import com.attendance.common.ApiResponse;
import com.attendance.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/semesters")
public class SemesterController {

    private final SemesterService semesterService;
    private final CurrentUser currentUser;

    public SemesterController(SemesterService semesterService, CurrentUser currentUser) {
        this.semesterService = semesterService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SemesterDto> create(@Valid @RequestBody CreateSemesterRequest request) {
        return ApiResponse.ok("Semester created", semesterService.create(request, currentUser.getUserId()));
    }

    // Readable by Coordinators/Teachers too — OE setup screens need to know the active semester.
    @GetMapping
    public ApiResponse<List<SemesterDto>> list(@RequestParam(required = false) Long academicYearId) {
        return ApiResponse.ok(semesterService.list(academicYearId));
    }

    @GetMapping("/{id}")
    public ApiResponse<SemesterDto> get(@PathVariable Long id) {
        return ApiResponse.ok(semesterService.get(id));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SemesterDto> close(@PathVariable Long id) {
        return ApiResponse.ok("Semester closed", semesterService.close(id, currentUser.getUserId()));
    }
}