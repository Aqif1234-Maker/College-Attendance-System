package com.attendance.academicyear;

import com.attendance.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/academic-years")
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    public AcademicYearController(AcademicYearService academicYearService) {
        this.academicYearService = academicYearService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AcademicYearDto> create(@Valid @RequestBody CreateAcademicYearRequest request) {
        return ApiResponse.ok("Academic year created", academicYearService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<AcademicYearDto>> list() {
        return ApiResponse.ok(academicYearService.list());
    }

    // Any authenticated role may read the current year — Coordinators/Teachers need it
    // for the auto-selected Academic Year step in the dependent filter (spec §3).
    @GetMapping("/current")
    public ApiResponse<AcademicYearDto> getCurrent() {
        return ApiResponse.ok(academicYearService.getCurrent());
    }

    @PatchMapping("/{id}/set-current")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AcademicYearDto> setCurrent(@PathVariable Long id) {
        return ApiResponse.ok("Current academic year updated", academicYearService.setCurrent(id));
    }
}