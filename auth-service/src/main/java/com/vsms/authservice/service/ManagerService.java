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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// ManagerService handles all business logic for manager operations
// Managers are created by admins with a temp password that should be changed
@Service
@RequiredArgsConstructor
@Transactional
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final AppUserRepository appUserRepository;
    private final NotificationPublisher notificationPublisher;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // Admin creates a manager with a password and sends credentials via email
    // Role is set based on department: INVENTORY gets INVENTORY_MANAGER role
    public ManagerResponse createManager(ManagerCreateRequest request) {
        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        if (managerRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new DuplicateResourceException("Manager", "employeeId", request.getEmployeeId());
        }

        // Set role based on department
        Role role = (request.getDepartment() == Department.INVENTORY)
                ? Role.INVENTORY_MANAGER
                : Role.MANAGER;

        AppUser appUser = AppUser.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();

        Manager manager = Manager.builder()
                .user(appUser)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .employeeId(request.getEmployeeId())
                .department(request.getDepartment())
                .build();

        Manager saved = managerRepository.save(manager);

        // Send email with login credentials
        try {
            notificationPublisher.publishManagerCreated(
                    request.getFirstName() + " " + request.getLastName(),
                    request.getEmail(),
                    request.getEmail(),
                    request.getPassword());
        } catch (Exception e) {
            // Log warning but don't fail manager creation
        }

        return mapToResponse(saved);
    }

    // Get a single manager by ID
    @Transactional(readOnly = true)
    public ManagerResponse getManagerById(Integer id) {
        Manager manager = managerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", id));
        return mapToResponse(manager);
    }

    // Get all managers with optional department filter
    @Transactional(readOnly = true)
    public List<ManagerResponse> getAllManagers(Department department) {
        List<Manager> managers;
        if (department != null) {
            managers = managerRepository.findByDepartment(department);
        } else {
            managers = managerRepository.findAll();
        }
        return managers.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Update manager profile with only the fields that are provided
    // If department changes I also update the role accordingly
    public ManagerResponse updateManager(Integer id, ManagerUpdateRequest request) {
        Manager manager = managerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", id));

        if (request.getPhone() != null) {
            manager.getUser().setPhone(request.getPhone());
        }
        if (request.getFirstName() != null) {
            manager.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            manager.setLastName(request.getLastName());
        }
        if (request.getDepartment() != null) {
            manager.setDepartment(request.getDepartment());
            // Update role when department changes
            Role role = (request.getDepartment() == Department.INVENTORY)
                    ? Role.INVENTORY_MANAGER
                    : Role.MANAGER;
            manager.getUser().setRole(role);
        }

        Manager updated = managerRepository.save(manager);
        return mapToResponse(updated);
    }

    // Soft delete by setting status to INACTIVE
    public void deleteManager(Integer id) {
        Manager manager = managerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", id));
        manager.getUser().setStatus(UserStatus.INACTIVE);
        managerRepository.save(manager);
    }

    // Get manager count with optional department filter
    @Transactional(readOnly = true)
    public long getManagerCount(Department department) {
        if (department != null) {
            return managerRepository.countByDepartment(department);
        }
        return managerRepository.count();
    }

    // Maps Manager entity to response DTO
    private ManagerResponse mapToResponse(Manager manager) {
        return ManagerResponse.builder()
                .id(manager.getId())
                .userId(manager.getUser().getId())
                .email(manager.getUser().getEmail())
                .phone(manager.getUser().getPhone())
                .firstName(manager.getFirstName())
                .lastName(manager.getLastName())
                .employeeId(manager.getEmployeeId())
                .department(manager.getDepartment())
                .status(manager.getUser().getStatus())
                .createdAt(manager.getCreatedAt())
                .build();
    }
}
