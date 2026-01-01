package com.vsms.authservice.config;

import com.vsms.authservice.entity.Admin;
import com.vsms.authservice.entity.AppUser;
import com.vsms.authservice.enums.Role;
import com.vsms.authservice.enums.UserStatus;
import com.vsms.authservice.repository.AdminRepository;
import com.vsms.authservice.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create default admin if not exists
        if (!appUserRepository.existsByEmail("admin@vsms.com")) {
            AppUser adminUser = AppUser.builder()
                    .email("admin@vsms.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .phone("9999999999")
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();

            Admin admin = Admin.builder()
                    .user(adminUser)
                    .firstName("System")
                    .lastName("Admin")
                    .isSuperAdmin(true)
                    .build();

            adminRepository.save(admin);
            log.info("Default admin created: admin@vsms.com / admin123");
        } else {
            log.info("Admin already exists, skipping seeding");
        }
    }
}
