package com.attendance.student;

import com.attendance.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLASS_COORDINATOR')")
    public ApiResponse<StudentDto> create(@Valid @RequestBody CreateStudentRequest request) {
        return ApiResponse.ok("Student added", studentService.create(request));
    }

    @GetMapping
    public ApiResponse<List<StudentDto>> listByClass(
            @RequestParam Long classId, @RequestParam(required = false) String batchLabel) {
        return ApiResponse.ok(studentService.listByClass(classId, batchLabel));
    }

    @GetMapping("/{id}")
    public ApiResponse<StudentDto> get(@PathVariable Long id) {
        return ApiResponse.ok(studentService.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CLASS_COORDINATOR')")
    public ApiResponse<StudentDto> update(@PathVariable Long id, @Valid @RequestBody CreateStudentRequest request) {
        return ApiResponse.ok("Student updated", studentService.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('CLASS_COORDINATOR')")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        studentService.setActive(id, false);
        return ApiResponse.ok("Student deactivated", null);
    }
}