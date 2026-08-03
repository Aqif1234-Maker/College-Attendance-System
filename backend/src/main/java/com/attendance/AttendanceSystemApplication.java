package com.attendance;

import com.attendance.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AttendanceSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttendanceSystemApplication.class, args);
    }

    @Bean
    public CommandLineRunner bootstrapAdmin(
            UserService userService,
            @Value("${app.bootstrap-admin.enabled}") boolean enabled,
            @Value("${app.bootstrap-admin.username}") String username,
            @Value("${app.bootstrap-admin.password}") String password,
            @Value("${app.bootstrap-admin.full-name}") String fullName) {
        return args -> userService.bootstrapAdminIfNeeded(enabled, username, password, fullName);
    }
}