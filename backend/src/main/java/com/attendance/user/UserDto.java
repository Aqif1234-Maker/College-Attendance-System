package com.attendance.user;

public record UserDto(
        Long id,
        String username,
        String fullName,
        Role role,
        boolean active
) {
}