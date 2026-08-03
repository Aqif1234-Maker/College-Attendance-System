package com.attendance.batch;

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
@RequestMapping("/batches")
public class BatchController {

    private final BatchService batchService;

    public BatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLASS_COORDINATOR')")
    public ApiResponse<BatchDto> create(@Valid @RequestBody CreateBatchRequest request) {
        return ApiResponse.ok("Batch created", batchService.create(request));
    }

    @GetMapping
    public ApiResponse<List<BatchDto>> listBySubject(@RequestParam Long subjectId) {
        return ApiResponse.ok(batchService.listBySubject(subjectId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CLASS_COORDINATOR')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        batchService.delete(id);
        return ApiResponse.ok("Batch deleted", null);
    }
}