package com.attendance.user;

import com.attendance.common.ApiException;
import com.attendance.common.ApiResponse;
import com.attendance.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final CurrentUser currentUser;

    public UserController(UserService userService, CurrentUser currentUser) {
        this.userService = userService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserDto> create(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.ok("User created", userService.create(request));
    }

    /**
     * Admins can list any role (or all users). When callers ask for TEACHER, return every
     * account that can teach, including CLASS_COORDINATOR accounts, so coordinators do not
     * need duplicate teacher logins.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLASS_COORDINATOR')")
    public ApiResponse<List<UserDto>> list(@RequestParam(required = false) Role role) {
        boolean isCoordinator = currentUser.getUser().getRole() == Role.CLASS_COORDINATOR;

        if (isCoordinator && role != Role.TEACHER) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Coordinators may only list teaching-capable accounts");
        }

        return ApiResponse.ok(userService.listByRole(role));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserDto> get(@PathVariable Long id) {
        return ApiResponse.ok(userService.get(id));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserDto> activate(@PathVariable Long id) {
        return ApiResponse.ok("User activated", userService.setActive(id, true));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserDto> deactivate(@PathVariable Long id) {
        return ApiResponse.ok("User deactivated", userService.setActive(id, false));
    }

    @PatchMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request);
        return ApiResponse.ok("Password reset", null);
    }
}
