package com.vsms.authservice.service;

import com.vsms.authservice.dto.request.ManagerCreateRequest;
import com.vsms.authservice.dto.request.ManagerUpdateRequest;
import com.vsms.authservice.dto.response.ManagerResponse;
import com.vsms.authservice.entity.AppUser;
import com.vsms.authservice.entity.Manager;
import com.vsms.authservice.enums.Department;
import com.vsms.authservice.enums.Role;
import com.vsms.authservice.enums.UserStatus;
import com.vsms.authservice.exception.DuplicateResourceException;
import com.vsms.authservice.exception.ResourceNotFoundException;
import com.vsms.authservice.repository.AppUserRepository;
import com.vsms.authservice.repository.ManagerRepository;
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
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private NotificationPublisher notificationPublisher;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ManagerService managerService;

    private Manager testManager;
    private AppUser testUser;

    @BeforeEach
    void setUp() {
        testUser = new AppUser();
        testUser.setId(1);
        testUser.setEmail("manager@test.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setPhone("1234567890");
        testUser.setRole(Role.MANAGER);
        testUser.setStatus(UserStatus.ACTIVE);

        testManager = new Manager();
        testManager.setId(1);
        testManager.setUser(testUser);
        testManager.setFirstName("Manager");
        testManager.setLastName("User");
        testManager.setEmployeeId("MGR-001");
        testManager.setDepartment(Department.SERVICE_BAY);
        testManager.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createManager_Success() {
        ManagerCreateRequest request = new ManagerCreateRequest();
        request.setEmail("new@test.com");
        request.setPassword("password123");
        request.setPhone("1234567890");
        request.setFirstName("New");
        request.setLastName("Manager");
        request.setEmployeeId("MGR-002");
        request.setDepartment(Department.SERVICE_BAY);

        when(appUserRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(managerRepository.existsByEmployeeId("MGR-002")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(managerRepository.save(any(Manager.class))).thenAnswer(inv -> {
            Manager m = inv.getArgument(0);
            m.setId(2);
            m.setCreatedAt(LocalDateTime.now());
            return m;
        });
        doNothing().when(notificationPublisher).publishManagerCreated(anyString(), anyString(), anyString(),
                anyString());

        ManagerResponse response = managerService.createManager(request);

        assertNotNull(response);
        assertEquals("New", response.getFirstName());
    }

    @Test
    void createManager_InventoryDepartment_SetsInventoryManagerRole() {
        ManagerCreateRequest request = new ManagerCreateRequest();
        request.setEmail("inv@test.com");
        request.setPassword("password123");
        request.setPhone("1234567890");
        request.setFirstName("Inventory");
        request.setLastName("Manager");
        request.setEmployeeId("INV-001");
        request.setDepartment(Department.INVENTORY);

        when(appUserRepository.existsByEmail("inv@test.com")).thenReturn(false);
        when(managerRepository.existsByEmployeeId("INV-001")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(managerRepository.save(any(Manager.class))).thenAnswer(inv -> {
            Manager m = inv.getArgument(0);
            m.setId(3);
            m.setCreatedAt(LocalDateTime.now());
            return m;
        });
        doNothing().when(notificationPublisher).publishManagerCreated(anyString(), anyString(), anyString(),
                anyString());

        ManagerResponse response = managerService.createManager(request);

        assertNotNull(response);
    }

    @Test
    void createManager_DuplicateEmail_ThrowsException() {
        ManagerCreateRequest request = new ManagerCreateRequest();
        request.setEmail("existing@test.com");

        when(appUserRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> managerService.createManager(request));
    }

    @Test
    void createManager_DuplicateEmployeeId_ThrowsException() {
        ManagerCreateRequest request = new ManagerCreateRequest();
        request.setEmail("new@test.com");
        request.setEmployeeId("MGR-001");

        when(appUserRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(managerRepository.existsByEmployeeId("MGR-001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> managerService.createManager(request));
    }

    @Test
    void getManagerById_Success() {
        when(managerRepository.findById(1)).thenReturn(Optional.of(testManager));

        ManagerResponse response = managerService.getManagerById(1);

        assertNotNull(response);
        assertEquals(1, response.getId());
    }

    @Test
    void getManagerById_NotFound_ThrowsException() {
        when(managerRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> managerService.getManagerById(999));
    }

    @Test
    void getAllManagers_NoFilter_ReturnsAll() {
        when(managerRepository.findAll()).thenReturn(List.of(testManager));

        List<ManagerResponse> responses = managerService.getAllManagers(null);

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void getAllManagers_WithDepartmentFilter_ReturnsFiltered() {
        when(managerRepository.findByDepartment(Department.SERVICE_BAY)).thenReturn(List.of(testManager));

        List<ManagerResponse> responses = managerService.getAllManagers(Department.SERVICE_BAY);

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void updateManager_Success() {
        ManagerUpdateRequest request = new ManagerUpdateRequest();
        request.setFirstName("UpdatedName");
        request.setPhone("9876543210");

        when(managerRepository.findById(1)).thenReturn(Optional.of(testManager));
        when(managerRepository.save(any(Manager.class))).thenReturn(testManager);

        ManagerResponse response = managerService.updateManager(1, request);

        assertNotNull(response);
        verify(managerRepository, times(1)).save(testManager);
    }

    @Test
    void updateManager_ChangeDepartmentToInventory_UpdatesRole() {
        ManagerUpdateRequest request = new ManagerUpdateRequest();
        request.setDepartment(Department.INVENTORY);

        when(managerRepository.findById(1)).thenReturn(Optional.of(testManager));
        when(managerRepository.save(any(Manager.class))).thenReturn(testManager);

        managerService.updateManager(1, request);

        assertEquals(Role.INVENTORY_MANAGER, testManager.getUser().getRole());
    }

    @Test
    void deleteManager_Success() {
        when(managerRepository.findById(1)).thenReturn(Optional.of(testManager));
        when(managerRepository.save(any(Manager.class))).thenReturn(testManager);

        managerService.deleteManager(1);

        assertEquals(UserStatus.INACTIVE, testManager.getUser().getStatus());
    }

    @Test
    void getManagerCount_NoDepartment_ReturnsTotal() {
        when(managerRepository.count()).thenReturn(5L);

        long count = managerService.getManagerCount(null);

        assertEquals(5L, count);
    }

    @Test
    void getManagerCount_WithDepartment_ReturnsFiltered() {
        when(managerRepository.countByDepartment(Department.SERVICE_BAY)).thenReturn(3L);

        long count = managerService.getManagerCount(Department.SERVICE_BAY);

        assertEquals(3L, count);
    }
}
