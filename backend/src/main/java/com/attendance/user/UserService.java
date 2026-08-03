package com.attendance.user;

import com.attendance.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDto create(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ApiException(HttpStatus.CONFLICT, "Username '" + request.username() + "' is already taken");
        }

        if (request.role() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Role is required");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setRole(request.role());
        user.setActive(true);

        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserDto> listByRole(Role role) {
        List<User> users = userRepository.findAll();

        if (role == Role.TEACHER) {
            users = users.stream()
                    .filter(u -> u.getRole() == Role.TEACHER || u.getRole() == Role.CLASS_COORDINATOR)
                    .toList();
        } else if (role != null) {
            users = users.stream().filter(u -> u.getRole() == role).toList();
        }

        return users.stream().map(userMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public UserDto get(Long id) {
        return userMapper.toDto(findEntity(id));
    }

    public UserDto setActive(Long id, boolean active) {
        User user = findEntity(id);
        user.setActive(active);
        return userMapper.toDto(user);
    }

    public void resetPassword(Long id, ResetPasswordRequest request) {
        User user = findEntity(id);
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    /**
     * Creates the initial Admin account only if bootstrap is enabled via configuration
     * and no user with that username already exists. Never runs automatically with
     * hardcoded values — every value comes from environment/profile configuration.
     */
    public void bootstrapAdminIfNeeded(boolean enabled, String username, String password, String fullName) {
        if (!enabled) {
            return;
        }
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return;
        }
        if (userRepository.existsByUsername(username)) {
            return;
        }

        User admin = new User();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setFullName(fullName == null || fullName.isBlank() ? "System Administrator" : fullName);
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        userRepository.save(admin);
    }

    private User findEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
