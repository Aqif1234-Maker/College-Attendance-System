package com.attendance.subject;

import com.attendance.common.ApiResponse;
import com.attendance.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/subjects")
public class SubjectController {

    private final SubjectService subjectService;
    private final CurrentUser currentUser;

    public SubjectController(SubjectService subjectService, CurrentUser currentUser) {
        this.subjectService = subjectService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLASS_COORDINATOR')")
    public ApiResponse<SubjectDto> create(@Valid @RequestBody CreateSubjectRequest request) {
        return ApiResponse.ok("Subject created", subjectService.create(request, currentUser.getUserId()));
    }

    @GetMapping
    public ApiResponse<List<SubjectDto>> listByClass(@RequestParam Long classId) {
        return ApiResponse.ok(subjectService.listByClass(classId));
    }

    @GetMapping("/{id}")
    public ApiResponse<SubjectDto> get(@PathVariable Long id) {
        return ApiResponse.ok(subjectService.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CLASS_COORDINATOR')")
    public ApiResponse<SubjectDto> update(@PathVariable Long id, @Valid @RequestBody CreateSubjectRequest request) {
        return ApiResponse.ok("Subject updated", subjectService.update(id, request, currentUser.getUserId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CLASS_COORDINATOR')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        subjectService.delete(id, currentUser.getUserId());
        return ApiResponse.ok("Subject deleted", null);
    }
}