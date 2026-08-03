package com.attendance.auth;

public record LoginResponse(
        String token,
        Long userId,
        String username,
        String fullName,
        String role
) {
}