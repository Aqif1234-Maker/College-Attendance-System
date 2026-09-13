package com.attendance.oe;

import jakarta.validation.constraints.NotNull;

public record AssignOeSlotRequest(
        @NotNull(message = "Semester is required") Long semesterId,
        @NotNull(message = "Mode is required") OeTeachingSlotMode mode,
        @NotNull(message = "Teacher is required") Long teacherId
) {
}