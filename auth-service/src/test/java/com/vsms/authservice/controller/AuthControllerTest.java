package com.vsms.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsms.authservice.dto.request.ChangePasswordRequest;
import com.vsms.authservice.dto.request.LoginRequest;
import com.vsms.authservice.dto.response.AuthResponse;
import com.vsms.authservice.security.CustomUserPrincipal;
import com.vsms.authservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("password123");

        AuthResponse response = AuthResponse.of("accessToken", 1, "test@test.com", "CUSTOMER", "John", "Doe");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void logout_Success() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }

    @Test
    void getCurrentUser_Success() throws Exception {
        CustomUserPrincipal principal = new CustomUserPrincipal(1, "test@test.com", "CUSTOMER");

        AuthController controller = new AuthController(authService);
        var response = controller.getCurrentUser(principal);

        assert response.getStatusCode().is2xxSuccessful();
        assert response.getBody() != null;
        assert response.getBody().isSuccess();
    }

    @Test
    void changePassword_Success() throws Exception {
        CustomUserPrincipal principal = new CustomUserPrincipal(1, "test@test.com", "CUSTOMER");
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPass123");
        request.setNewPassword("newPass123");

        doNothing().when(authService).changePassword(any(CustomUserPrincipal.class), any(ChangePasswordRequest.class));

        AuthController controller = new AuthController(authService);
        var response = controller.changePassword(principal, request);

        assert response.getStatusCode().is2xxSuccessful();
        verify(authService, times(1)).changePassword(any(CustomUserPrincipal.class), any(ChangePasswordRequest.class));
    }
}
