package com.attendance.oe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOeSubjectRequest(
        @NotNull(message = "Teaching slot is required") Long oeTeachingSlotId,
        @NotBlank(message = "Subject name is required") String name
) {
}