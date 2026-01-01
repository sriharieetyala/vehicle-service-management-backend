package com.vsms.authservice.service;

import com.vsms.authservice.dto.request.ChangePasswordRequest;
import com.vsms.authservice.dto.request.LoginRequest;
import com.vsms.authservice.dto.response.AuthResponse;
import com.vsms.authservice.entity.*;
import com.vsms.authservice.enums.UserStatus;
import com.vsms.authservice.exception.BadRequestException;
import com.vsms.authservice.exception.UnauthorizedException;
import com.vsms.authservice.repository.*;
import com.vsms.authservice.security.CustomUserPrincipal;
import com.vsms.authservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customerRepository;
    private final TechnicianRepository technicianRepository;
    private final ManagerRepository managerRepository;
    private final AdminRepository adminRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase();
        String password = request.getPassword();

        // Check Customer
        Optional<Customer> customer = customerRepository.findByUserEmail(email);
        if (customer.isPresent()) {
            Customer c = customer.get();
            AppUser user = c.getUser();
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new UnauthorizedException("Account is deactivated");
            }
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                String accessToken = tokenProvider.generateAccessToken(c.getId(), user.getEmail(), "CUSTOMER");
                String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());
                log.info("Customer logged in: {}", email);
                return AuthResponse.of(accessToken, refreshToken, c.getId(), user.getEmail(),
                        "CUSTOMER", c.getFirstName(), c.getLastName());
            }
        }

        // Check Technician
        Optional<Technician> technician = technicianRepository.findByUserEmail(email);
        if (technician.isPresent()) {
            Technician t = technician.get();
            AppUser user = t.getUser();
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new UnauthorizedException("Account is deactivated or not approved");
            }
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                String accessToken = tokenProvider.generateAccessToken(t.getId(), user.getEmail(), "TECHNICIAN");
                String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());
                log.info("Technician logged in: {}", email);
                return AuthResponse.of(accessToken, refreshToken, t.getId(), user.getEmail(),
                        "TECHNICIAN", t.getFirstName(), t.getLastName());
            }
        }

        // Check Manager
        Optional<Manager> manager = managerRepository.findByUserEmail(email);
        if (manager.isPresent()) {
            Manager m = manager.get();
            AppUser user = m.getUser();
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new UnauthorizedException("Account is deactivated");
            }
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                String accessToken = tokenProvider.generateAccessToken(m.getId(), user.getEmail(), "MANAGER");
                String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());
                log.info("Manager logged in: {}", email);
                return AuthResponse.of(accessToken, refreshToken, m.getId(), user.getEmail(),
                        "MANAGER", m.getFirstName(), m.getLastName());
            }
        }

        // Check Admin
        Optional<Admin> admin = adminRepository.findByUserEmail(email);
        if (admin.isPresent()) {
            Admin a = admin.get();
            AppUser user = a.getUser();
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                String accessToken = tokenProvider.generateAccessToken(a.getId(), user.getEmail(), "ADMIN");
                String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());
                log.info("Admin logged in: {}", email);
                return AuthResponse.of(accessToken, refreshToken, a.getId(), user.getEmail(),
                        "ADMIN", a.getFirstName(), a.getLastName());
            }
        }

        throw new UnauthorizedException("Invalid email or password");
    }

    public AuthResponse refresh(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String email = tokenProvider.getEmailFromToken(refreshToken);

        // Find user and generate new access token
        Optional<Customer> customer = customerRepository.findByUserEmail(email);
        if (customer.isPresent()) {
            Customer c = customer.get();
            String accessToken = tokenProvider.generateAccessToken(c.getId(), c.getUser().getEmail(), "CUSTOMER");
            return AuthResponse.of(accessToken, refreshToken, c.getId(), c.getUser().getEmail(),
                    "CUSTOMER", c.getFirstName(), c.getLastName());
        }

        Optional<Technician> technician = technicianRepository.findByUserEmail(email);
        if (technician.isPresent()) {
            Technician t = technician.get();
            String accessToken = tokenProvider.generateAccessToken(t.getId(), t.getUser().getEmail(), "TECHNICIAN");
            return AuthResponse.of(accessToken, refreshToken, t.getId(), t.getUser().getEmail(),
                    "TECHNICIAN", t.getFirstName(), t.getLastName());
        }

        Optional<Manager> manager = managerRepository.findByUserEmail(email);
        if (manager.isPresent()) {
            Manager m = manager.get();
            String accessToken = tokenProvider.generateAccessToken(m.getId(), m.getUser().getEmail(), "MANAGER");
            return AuthResponse.of(accessToken, refreshToken, m.getId(), m.getUser().getEmail(),
                    "MANAGER", m.getFirstName(), m.getLastName());
        }

        Optional<Admin> admin = adminRepository.findByUserEmail(email);
        if (admin.isPresent()) {
            Admin a = admin.get();
            String accessToken = tokenProvider.generateAccessToken(a.getId(), a.getUser().getEmail(), "ADMIN");
            return AuthResponse.of(accessToken, refreshToken, a.getId(), a.getUser().getEmail(),
                    "ADMIN", a.getFirstName(), a.getLastName());
        }

        throw new UnauthorizedException("User not found");
    }

    public void changePassword(CustomUserPrincipal principal, ChangePasswordRequest request) {
        String email = principal.getEmail();
        String role = principal.getRole();
        String currentPassword = request.getCurrentPassword();
        String newPassword = request.getNewPassword();

        switch (role) {
            case "CUSTOMER" -> {
                Customer c = customerRepository.findByUserEmail(email)
                        .orElseThrow(() -> new UnauthorizedException("User not found"));
                if (!passwordEncoder.matches(currentPassword, c.getUser().getPasswordHash())) {
                    throw new BadRequestException("Current password is incorrect");
                }
                c.getUser().setPasswordHash(passwordEncoder.encode(newPassword));
                customerRepository.save(c);
            }
            case "TECHNICIAN" -> {
                Technician t = technicianRepository.findByUserEmail(email)
                        .orElseThrow(() -> new UnauthorizedException("User not found"));
                if (!passwordEncoder.matches(currentPassword, t.getUser().getPasswordHash())) {
                    throw new BadRequestException("Current password is incorrect");
                }
                t.getUser().setPasswordHash(passwordEncoder.encode(newPassword));
                technicianRepository.save(t);
            }
            case "MANAGER" -> {
                Manager m = managerRepository.findByUserEmail(email)
                        .orElseThrow(() -> new UnauthorizedException("User not found"));
                if (!passwordEncoder.matches(currentPassword, m.getUser().getPasswordHash())) {
                    throw new BadRequestException("Current password is incorrect");
                }
                m.getUser().setPasswordHash(passwordEncoder.encode(newPassword));
                managerRepository.save(m);
            }
            case "ADMIN" -> {
                Admin a = adminRepository.findByUserEmail(email)
                        .orElseThrow(() -> new UnauthorizedException("User not found"));
                if (!passwordEncoder.matches(currentPassword, a.getUser().getPasswordHash())) {
                    throw new BadRequestException("Current password is incorrect");
                }
                a.getUser().setPasswordHash(passwordEncoder.encode(newPassword));
                adminRepository.save(a);
            }
        }
        log.info("Password changed for user: {}", email);
    }
}
