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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private TechnicianRepository technicianRepository;
    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private AdminRepository adminRepository;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private Customer testCustomer;
    private Technician testTechnician;
    private Manager testManager;
    private Admin testAdmin;

    @BeforeEach
    void setUp() {
        // Create test customer
        AppUser customerUser = new AppUser();
        customerUser.setEmail("customer@test.com");
        customerUser.setPasswordHash("hashedPassword");
        customerUser.setStatus(UserStatus.ACTIVE);

        testCustomer = new Customer();
        testCustomer.setId(1);
        testCustomer.setUser(customerUser);
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");

        // Create test technician
        AppUser techUser = new AppUser();
        techUser.setEmail("tech@test.com");
        techUser.setPasswordHash("hashedPassword");
        techUser.setStatus(UserStatus.ACTIVE);

        testTechnician = new Technician();
        testTechnician.setId(2);
        testTechnician.setUser(techUser);
        testTechnician.setFirstName("Tech");
        testTechnician.setLastName("User");

        // Create test manager
        AppUser managerUser = new AppUser();
        managerUser.setEmail("manager@test.com");
        managerUser.setPasswordHash("hashedPassword");
        managerUser.setStatus(UserStatus.ACTIVE);

        testManager = new Manager();
        testManager.setId(3);
        testManager.setUser(managerUser);
        testManager.setFirstName("Manager");
        testManager.setLastName("User");

        // Create test admin
        AppUser adminUser = new AppUser();
        adminUser.setEmail("admin@test.com");
        adminUser.setPasswordHash("hashedPassword");

        testAdmin = new Admin();
        testAdmin.setId(4);
        testAdmin.setUser(adminUser);
        testAdmin.setFirstName("Admin");
        testAdmin.setLastName("User");
    }

    // ==================== LOGIN TESTS ====================

    @Test
    void login_CustomerSuccess() {
        LoginRequest request = new LoginRequest();
        request.setEmail("customer@test.com");
        request.setPassword("password123");

        when(customerRepository.findByUserEmail("customer@test.com")).thenReturn(Optional.of(testCustomer));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(tokenProvider.generateAccessToken(anyInt(), anyString(), eq("CUSTOMER"))).thenReturn("accessToken");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("CUSTOMER", response.getRole());
        assertEquals(1, response.getUserId());
    }

    @Test
    void login_TechnicianSuccess() {
        LoginRequest request = new LoginRequest();
        request.setEmail("tech@test.com");
        request.setPassword("password123");

        when(customerRepository.findByUserEmail("tech@test.com")).thenReturn(Optional.empty());
        when(technicianRepository.findByUserEmail("tech@test.com")).thenReturn(Optional.of(testTechnician));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(tokenProvider.generateAccessToken(anyInt(), anyString(), eq("TECHNICIAN"))).thenReturn("accessToken");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("TECHNICIAN", response.getRole());
    }

    @Test
    void login_ManagerSuccess() {
        LoginRequest request = new LoginRequest();
        request.setEmail("manager@test.com");
        request.setPassword("password123");

        when(customerRepository.findByUserEmail("manager@test.com")).thenReturn(Optional.empty());
        when(technicianRepository.findByUserEmail("manager@test.com")).thenReturn(Optional.empty());
        when(managerRepository.findByUserEmail("manager@test.com")).thenReturn(Optional.of(testManager));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(tokenProvider.generateAccessToken(anyInt(), anyString(), eq("MANAGER"))).thenReturn("accessToken");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("MANAGER", response.getRole());
    }

    @Test
    void login_AdminSuccess() {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@test.com");
        request.setPassword("password123");

        when(customerRepository.findByUserEmail("admin@test.com")).thenReturn(Optional.empty());
        when(technicianRepository.findByUserEmail("admin@test.com")).thenReturn(Optional.empty());
        when(managerRepository.findByUserEmail("admin@test.com")).thenReturn(Optional.empty());
        when(adminRepository.findByUserEmail("admin@test.com")).thenReturn(Optional.of(testAdmin));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(tokenProvider.generateAccessToken(anyInt(), anyString(), eq("ADMIN"))).thenReturn("accessToken");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void login_InvalidCredentials_ThrowsUnauthorized() {
        LoginRequest request = new LoginRequest();
        request.setEmail("invalid@test.com");
        request.setPassword("wrongpassword");

        when(customerRepository.findByUserEmail("invalid@test.com")).thenReturn(Optional.empty());
        when(technicianRepository.findByUserEmail("invalid@test.com")).thenReturn(Optional.empty());
        when(managerRepository.findByUserEmail("invalid@test.com")).thenReturn(Optional.empty());
        when(adminRepository.findByUserEmail("invalid@test.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void login_DeactivatedAccount_ThrowsUnauthorized() {
        testCustomer.getUser().setStatus(UserStatus.INACTIVE);

        LoginRequest request = new LoginRequest();
        request.setEmail("customer@test.com");
        request.setPassword("password123");

        when(customerRepository.findByUserEmail("customer@test.com")).thenReturn(Optional.of(testCustomer));

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    // ==================== CHANGE PASSWORD TESTS ====================

    @Test
    void changePassword_CustomerSuccess() {
        CustomUserPrincipal principal = new CustomUserPrincipal(1, "customer@test.com", "CUSTOMER");
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");

        when(customerRepository.findByUserEmail("customer@test.com")).thenReturn(Optional.of(testCustomer));
        when(passwordEncoder.matches("oldPassword", "hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newHashedPassword");

        authService.changePassword(principal, request);

        verify(customerRepository, times(1)).save(testCustomer);
    }

    @Test
    void changePassword_WrongCurrentPassword_ThrowsBadRequest() {
        CustomUserPrincipal principal = new CustomUserPrincipal(1, "customer@test.com", "CUSTOMER");
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword");

        when(customerRepository.findByUserEmail("customer@test.com")).thenReturn(Optional.of(testCustomer));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.changePassword(principal, request));
    }

    @Test
    void changePassword_TechnicianSuccess() {
        CustomUserPrincipal principal = new CustomUserPrincipal(2, "tech@test.com", "TECHNICIAN");
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");

        when(technicianRepository.findByUserEmail("tech@test.com")).thenReturn(Optional.of(testTechnician));
        when(passwordEncoder.matches("oldPassword", "hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newHashedPassword");

        authService.changePassword(principal, request);

        verify(technicianRepository, times(1)).save(testTechnician);
    }

    @Test
    void changePassword_ManagerSuccess() {
        CustomUserPrincipal principal = new CustomUserPrincipal(3, "manager@test.com", "MANAGER");
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");

        when(managerRepository.findByUserEmail("manager@test.com")).thenReturn(Optional.of(testManager));
        when(passwordEncoder.matches("oldPassword", "hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newHashedPassword");

        authService.changePassword(principal, request);

        verify(managerRepository, times(1)).save(testManager);
    }

    @Test
    void changePassword_AdminSuccess() {
        CustomUserPrincipal principal = new CustomUserPrincipal(4, "admin@test.com", "ADMIN");
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");

        when(adminRepository.findByUserEmail("admin@test.com")).thenReturn(Optional.of(testAdmin));
        when(passwordEncoder.matches("oldPassword", "hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newHashedPassword");

        authService.changePassword(principal, request);

        verify(adminRepository, times(1)).save(testAdmin);
    }

    // ==================== WRONG PASSWORD TESTS ====================

    @Test
    void login_CustomerWrongPassword_ThrowsUnauthorized() {
        LoginRequest request = new LoginRequest();
        request.setEmail("customer@test.com");
        request.setPassword("wrongPassword");

        when(customerRepository.findByUserEmail("customer@test.com")).thenReturn(Optional.of(testCustomer));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);
        when(technicianRepository.findByUserEmail("customer@test.com")).thenReturn(Optional.empty());
        when(managerRepository.findByUserEmail("customer@test.com")).thenReturn(Optional.empty());
        when(adminRepository.findByUserEmail("customer@test.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void login_TechnicianDeactivated_ThrowsUnauthorized() {
        testTechnician.getUser().setStatus(UserStatus.INACTIVE);

        LoginRequest request = new LoginRequest();
        request.setEmail("tech@test.com");
        request.setPassword("password123");

        when(customerRepository.findByUserEmail("tech@test.com")).thenReturn(Optional.empty());
        when(technicianRepository.findByUserEmail("tech@test.com")).thenReturn(Optional.of(testTechnician));

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void login_ManagerDeactivated_ThrowsUnauthorized() {
        testManager.getUser().setStatus(UserStatus.INACTIVE);

        LoginRequest request = new LoginRequest();
        request.setEmail("manager@test.com");
        request.setPassword("password123");

        when(customerRepository.findByUserEmail("manager@test.com")).thenReturn(Optional.empty());
        when(technicianRepository.findByUserEmail("manager@test.com")).thenReturn(Optional.empty());
        when(managerRepository.findByUserEmail("manager@test.com")).thenReturn(Optional.of(testManager));

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }
}
