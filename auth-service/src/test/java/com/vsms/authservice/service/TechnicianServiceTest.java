package com.vsms.authservice.service;

import com.vsms.authservice.dto.request.TechnicianCreateRequest;
import com.vsms.authservice.dto.response.TechnicianResponse;
import com.vsms.authservice.entity.AppUser;
import com.vsms.authservice.entity.Technician;
import com.vsms.authservice.enums.Role;
import com.vsms.authservice.enums.Specialization;
import com.vsms.authservice.enums.UserStatus;
import com.vsms.authservice.exception.BadRequestException;
import com.vsms.authservice.exception.DuplicateResourceException;
import com.vsms.authservice.exception.ResourceNotFoundException;
import com.vsms.authservice.repository.AppUserRepository;
import com.vsms.authservice.repository.TechnicianRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TechnicianServiceTest {

    @Mock
    private TechnicianRepository technicianRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private NotificationPublisher notificationPublisher;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TechnicianService technicianService;

    private Technician testTechnician;
    private AppUser testUser;

    @BeforeEach
    void setUp() {
        testUser = new AppUser();
        testUser.setId(1);
        testUser.setEmail("tech@test.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setPhone("1234567890");
        testUser.setRole(Role.TECHNICIAN);
        testUser.setStatus(UserStatus.ACTIVE);

        testTechnician = new Technician();
        testTechnician.setId(1);
        testTechnician.setUser(testUser);
        testTechnician.setFirstName("Tech");
        testTechnician.setLastName("User");
        testTechnician.setEmployeeId("TECH-00001");
        testTechnician.setSpecialization(Specialization.ENGINE);
        testTechnician.setExperienceYears(5);
        testTechnician.setOnDuty(true);
        testTechnician.setCurrentWorkload(2);
        testTechnician.setMaxCapacity(5);
        testTechnician.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createTechnician_Success() {
        TechnicianCreateRequest request = new TechnicianCreateRequest();
        request.setEmail("new@test.com");
        request.setPassword("password123");
        request.setPhone("1234567890");
        request.setFirstName("New");
        request.setLastName("Tech");
        request.setSpecialization(Specialization.GENERAL);
        request.setExperienceYears(3);

        when(appUserRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(technicianRepository.save(any(Technician.class))).thenAnswer(inv -> {
            Technician t = inv.getArgument(0);
            t.setId(2);
            t.setCreatedAt(LocalDateTime.now());
            return t;
        });

        TechnicianResponse response = technicianService.createTechnician(request);

        assertNotNull(response);
        assertEquals("New", response.getFirstName());
        assertEquals(UserStatus.PENDING, response.getStatus());
    }

    @Test
    void createTechnician_DuplicateEmail_ThrowsException() {
        TechnicianCreateRequest request = new TechnicianCreateRequest();
        request.setEmail("existing@test.com");

        when(appUserRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> technicianService.createTechnician(request));
    }

    @Test
    void getTechnicianById_Success() {
        when(technicianRepository.findById(1)).thenReturn(Optional.of(testTechnician));

        TechnicianResponse response = technicianService.getTechnicianById(1);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("Tech", response.getFirstName());
    }

    @Test
    void getTechnicianById_Inactive_ThrowsException() {
        testTechnician.getUser().setStatus(UserStatus.INACTIVE);
        when(technicianRepository.findById(1)).thenReturn(Optional.of(testTechnician));

        assertThrows(ResourceNotFoundException.class, () -> technicianService.getTechnicianById(1));
    }

    @Test
    void getAllTechnicians_Success() {
        when(technicianRepository.findAllActive()).thenReturn(List.of(testTechnician));

        List<TechnicianResponse> responses = technicianService.getAllTechnicians();

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void getAvailableTechnicians_Success() {
        when(technicianRepository.findAvailableTechnicians()).thenReturn(List.of(testTechnician));

        List<TechnicianResponse> responses = technicianService.getAvailableTechnicians();

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void getAvailableBySpecialization_Success() {
        when(technicianRepository.findAvailableBySpecialization(Specialization.ENGINE))
                .thenReturn(List.of(testTechnician));

        List<TechnicianResponse> responses = technicianService.getAvailableBySpecialization(Specialization.ENGINE);

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void getPendingTechnicians_Success() {
        testTechnician.getUser().setStatus(UserStatus.PENDING);
        when(technicianRepository.findPendingApproval()).thenReturn(List.of(testTechnician));

        List<TechnicianResponse> responses = technicianService.getPendingTechnicians();

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void approveTechnician_Success() {
        testTechnician.getUser().setStatus(UserStatus.PENDING);
        when(technicianRepository.findById(1)).thenReturn(Optional.of(testTechnician));
        when(technicianRepository.save(any(Technician.class))).thenReturn(testTechnician);
        doNothing().when(notificationPublisher).publishTechnicianApproved(anyString(), anyString(), anyString());

        TechnicianResponse response = technicianService.approveTechnician(1);

        assertNotNull(response);
        assertEquals(UserStatus.ACTIVE, testTechnician.getUser().getStatus());
        assertTrue(testTechnician.getOnDuty());
    }

    @Test
    void approveTechnician_NotPending_ThrowsException() {
        testTechnician.getUser().setStatus(UserStatus.ACTIVE);
        when(technicianRepository.findById(1)).thenReturn(Optional.of(testTechnician));

        assertThrows(BadRequestException.class, () -> technicianService.approveTechnician(1));
    }

    @Test
    void rejectTechnician_Success() {
        testTechnician.getUser().setStatus(UserStatus.PENDING);
        when(technicianRepository.findById(1)).thenReturn(Optional.of(testTechnician));
        when(technicianRepository.save(any(Technician.class))).thenReturn(testTechnician);
        doNothing().when(notificationPublisher).publishTechnicianRejected(anyString(), anyString(), any());

        technicianService.rejectTechnician(1);

        assertEquals(UserStatus.INACTIVE, testTechnician.getUser().getStatus());
    }

    @Test
    void toggleDutyStatus_Success() {
        when(technicianRepository.findById(1)).thenReturn(Optional.of(testTechnician));
        when(technicianRepository.save(any(Technician.class))).thenReturn(testTechnician);

        TechnicianResponse response = technicianService.toggleDutyStatus(1);

        assertNotNull(response);
        assertFalse(testTechnician.getOnDuty()); // Was true, toggled to false
    }

    @Test
    void incrementWorkload_Success() {
        testTechnician.setCurrentWorkload(2);
        when(technicianRepository.findById(1)).thenReturn(Optional.of(testTechnician));
        doNothing().when(technicianRepository).incrementWorkload(1);

        technicianService.incrementWorkload(1);

        verify(technicianRepository, times(1)).incrementWorkload(1);
    }

    @Test
    void incrementWorkload_MaxCapacity_ThrowsException() {
        testTechnician.setCurrentWorkload(5);
        when(technicianRepository.findById(1)).thenReturn(Optional.of(testTechnician));

        assertThrows(BadRequestException.class, () -> technicianService.incrementWorkload(1));
    }

    @Test
    void decrementWorkload_Success() {
        doNothing().when(technicianRepository).decrementWorkload(1);

        technicianService.decrementWorkload(1);

        verify(technicianRepository, times(1)).decrementWorkload(1);
    }

    @Test
    void deleteTechnician_Success() {
        when(technicianRepository.findById(1)).thenReturn(Optional.of(testTechnician));
        when(technicianRepository.save(any(Technician.class))).thenReturn(testTechnician);

        technicianService.deleteTechnician(1);

        assertEquals(UserStatus.INACTIVE, testTechnician.getUser().getStatus());
        assertFalse(testTechnician.getOnDuty());
    }

    @Test
    void getTechnicianCount_ReturnsCount() {
        when(technicianRepository.countActive()).thenReturn(5L);

        long count = technicianService.getTechnicianCount();

        assertEquals(5L, count);
    }

    @Test
    void getPendingCount_ReturnsCount() {
        when(technicianRepository.countPending()).thenReturn(3L);

        long count = technicianService.getPendingCount();

        assertEquals(3L, count);
    }
}
