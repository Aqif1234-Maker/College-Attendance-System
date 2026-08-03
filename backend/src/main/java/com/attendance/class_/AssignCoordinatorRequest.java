package com.attendance.class_;

import jakarta.validation.constraints.NotNull;

public record AssignCoordinatorRequest(
        @NotNull(message = "User is required") Long userId
) {
}