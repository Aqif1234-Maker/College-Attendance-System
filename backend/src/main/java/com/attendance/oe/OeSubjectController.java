package com.attendance.oe;

import com.attendance.common.ApiResponse;
import com.attendance.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oe/subjects")
public class OeSubjectController {

    private final OeSubjectService oeSubjectService;
    private final CurrentUser currentUser;

    public OeSubjectController(OeSubjectService oeSubjectService, CurrentUser currentUser) {
        this.oeSubjectService = oeSubjectService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'CLASS_COORDINATOR')")
    public ApiResponse<OeSubjectDto> create(@Valid @RequestBody CreateOeSubjectRequest request) {
        return ApiResponse.ok("OE subject created", oeSubjectService.create(request, currentUser.getUserId()));
    }

    @GetMapping("/by-subject")
    public ApiResponse<OeSubjectDto> getBySubjectId(@RequestParam Long subjectId) {
        return ApiResponse.ok(oeSubjectService.getBySubjectId(subjectId));
    }

    @GetMapping("/by-slot")
    public ApiResponse<OeSubjectDto> getBySlotId(@RequestParam Long slotId) {
        return ApiResponse.ok(oeSubjectService.getBySlotId(slotId));
    }
}