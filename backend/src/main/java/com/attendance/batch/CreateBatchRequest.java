package com.attendance.batch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateBatchRequest(
        @NotNull(message = "Subject is required") Long subjectId,
        @NotBlank(message = "Batch label is required") String label
) {
}