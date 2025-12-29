package com.vsms.authservice.service;

import com.vsms.authservice.dto.request.ManagerCreateRequest;
import com.vsms.authservice.dto.request.ManagerUpdateRequest;
import com.vsms.authservice.dto.response.ManagerResponse;
import com.vsms.authservice.entity.AppUser;
import com.vsms.authservice.entity.Manager;
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

        AppUser appUser = AppUser.builder()
                .email(request.getEmail())
                .passwordHash(tempPassword) // TODO: Hash and send via email
                .phone(request.getPhone())
                .role(Role.MANAGER)
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

        // TODO: Send email with credentials via notification service

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
     * Get all managers
     */
    @Transactional(readOnly = true)
    public List<ManagerResponse> getAllManagers() {
        return managerRepository.findAll().stream()
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
