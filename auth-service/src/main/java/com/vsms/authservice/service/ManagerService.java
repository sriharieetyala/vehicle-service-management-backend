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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final AppUserRepository appUserRepository;
    private final NotificationPublisher notificationPublisher;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    /**
     * Admin creates a new manager (auto-generates password)
     */
    public ManagerResponse createManager(ManagerCreateRequest request) {
        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        if (managerRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new DuplicateResourceException("Manager", "employeeId", request.getEmployeeId());
        }

        // Generate temporary password
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        // Set role based on department
        Role role = (request.getDepartment() == Department.INVENTORY)
                ? Role.INVENTORY_MANAGER
                : Role.MANAGER;

        AppUser appUser = AppUser.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(tempPassword))
                .phone(request.getPhone())
                .role(role)
                .status(UserStatus.ACTIVE)
                .mustChangePassword(true) // Must change on first login
                .build();

        Manager manager = Manager.builder()
                .user(appUser)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .employeeId(request.getEmployeeId())
                .department(request.getDepartment())
                .build();

        Manager saved = managerRepository.save(manager);

        // Send email with credentials via notification service
        try {
            notificationPublisher.publishManagerCreated(
                    request.getFirstName() + " " + request.getLastName(),
                    request.getEmail(),
                    request.getEmail(), // username is email
                    tempPassword);
        } catch (Exception e) {
            // Log but don't fail the creation
            System.err.println("Could not send notification: " + e.getMessage());
        }

        return mapToResponse(saved);
    }

    /**
     * Get manager by ID
     */
    @Transactional(readOnly = true)
    public ManagerResponse getManagerById(Integer id) {
        Manager manager = managerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", id));
        return mapToResponse(manager);
    }

    /**
     * Get all managers (optional filter by department)
     */
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
                .collect(Collectors.toList());
    }

    /**
     * Update manager profile
     */
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
            // Update role if department changed
            Role role = (request.getDepartment() == Department.INVENTORY)
                    ? Role.INVENTORY_MANAGER
                    : Role.MANAGER;
            manager.getUser().setRole(role);
        }

        Manager updated = managerRepository.save(manager);
        return mapToResponse(updated);
    }

    /**
     * Deactivate manager
     */
    public void deleteManager(Integer id) {
        Manager manager = managerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", id));
        manager.getUser().setStatus(UserStatus.INACTIVE);
        managerRepository.save(manager);
    }

    /**
     * Get count (optional by department)
     */
    @Transactional(readOnly = true)
    public long getManagerCount(Department department) {
        if (department != null) {
            return managerRepository.countByDepartment(department);
        }
        return managerRepository.count();
    }

    /**
     * Map entity to response DTO
     */
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
