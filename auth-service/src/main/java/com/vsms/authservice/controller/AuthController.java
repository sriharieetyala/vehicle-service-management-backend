package com.vsms.authservice.controller;

import com.vsms.authservice.dto.request.ChangePasswordRequest;
import com.vsms.authservice.dto.request.LoginRequest;
import com.vsms.authservice.dto.response.ApiResponse;
import com.vsms.authservice.dto.response.AuthResponse;
import com.vsms.authservice.security.CustomUserPrincipal;
import com.vsms.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// AuthController handles all authentication related endpoints
// I created this to manage login, logout and password operations
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Login endpoint where users authenticate and get their JWT token
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // Returns the current logged in user's info from the JWT token
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Object>> getCurrentUser(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of(
                "userId", principal.getId(),
                "email", principal.getEmail(),
                "role", principal.getRole())));
    }

    // Logout is stateless so client just removes the token from storage
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    // Users can change their password after logging in
    // I require the current password for security reasons
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }
}
