package com.vsms.authservice.controller;

import com.vsms.authservice.dto.request.ChangePasswordRequest;
import com.vsms.authservice.dto.request.LoginRequest;
import com.vsms.authservice.dto.request.RefreshRequest;
import com.vsms.authservice.dto.response.ApiResponse;
import com.vsms.authservice.dto.response.AuthResponse;
import com.vsms.authservice.security.CustomUserPrincipal;
import com.vsms.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 1. Login - Returns access + refresh tokens directly
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // 2. Refresh token - Get new access token
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    // 3. Get current user info
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Object>> getCurrentUser(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of(
                "userId", principal.getId(),
                "email", principal.getEmail(),
                "role", principal.getRole())));
    }

    // 4. Logout (client should discard token)
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {

        // Client should remove the token from storage
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    // 5. Change password
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }
}
