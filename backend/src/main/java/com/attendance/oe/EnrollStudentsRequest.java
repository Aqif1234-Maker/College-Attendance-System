package com.attendance.oe;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record EnrollStudentsRequest(
        @NotNull(message = "OE subject is required") Long oeSubjectId,
        @NotEmpty(message = "At least one student must be selected") List<Long> studentIds
) {
}