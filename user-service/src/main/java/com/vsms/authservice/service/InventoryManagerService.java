package com.vsms.authservice.service;

import com.vsms.authservice.dto.request.InventoryManagerCreateRequest;
import com.vsms.authservice.dto.request.InventoryManagerUpdateRequest;
import com.vsms.authservice.dto.response.InventoryManagerResponse;
import com.vsms.authservice.entity.AppUser;
import com.vsms.authservice.entity.InventoryManager;
import com.vsms.authservice.enums.Role;
import com.vsms.authservice.enums.UserStatus;
import com.vsms.authservice.exception.DuplicateResourceException;
import com.vsms.authservice.exception.ResourceNotFoundException;
import com.vsms.authservice.repository.AppUserRepository;
import com.vsms.authservice.repository.InventoryManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryManagerService {

    private final InventoryManagerRepository inventoryManagerRepository;
    private final AppUserRepository appUserRepository;

    /**
     * Admin creates a new inventory manager
     */
    public InventoryManagerResponse createInventoryManager(InventoryManagerCreateRequest request) {
        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        if (inventoryManagerRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new DuplicateResourceException("Inventory Manager", "employeeId", request.getEmployeeId());
        }

        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        AppUser appUser = AppUser.builder()
                .email(request.getEmail())
                .passwordHash(tempPassword)
                .phone(request.getPhone())
                .role(Role.INVENTORY_MANAGER)
                .status(UserStatus.ACTIVE)
                .mustChangePassword(true)
                .build();

        InventoryManager inventoryManager = InventoryManager.builder()
                .user(appUser)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .employeeId(request.getEmployeeId())
                .build();

        InventoryManager saved = inventoryManagerRepository.save(inventoryManager);
        return mapToResponse(saved);
    }

    /**
     * Get inventory manager by ID
     */
    @Transactional(readOnly = true)
    public InventoryManagerResponse getInventoryManagerById(Integer id) {
        InventoryManager invManager = inventoryManagerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory Manager", "id", id));
        return mapToResponse(invManager);
    }

    /**
     * Get all inventory managers
     */
    @Transactional(readOnly = true)
    public List<InventoryManagerResponse> getAllInventoryManagers() {
        return inventoryManagerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update inventory manager profile
     */
    public InventoryManagerResponse updateInventoryManager(Integer id, InventoryManagerUpdateRequest request) {
        InventoryManager invManager = inventoryManagerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory Manager", "id", id));

        if (request.getPhone() != null) {
            invManager.getUser().setPhone(request.getPhone());
        }
        if (request.getFirstName() != null) {
            invManager.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            invManager.setLastName(request.getLastName());
        }

        InventoryManager updated = inventoryManagerRepository.save(invManager);
        return mapToResponse(updated);
    }

    /**
     * Deactivate inventory manager
     */
    public void deleteInventoryManager(Integer id) {
        InventoryManager invManager = inventoryManagerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory Manager", "id", id));
        invManager.getUser().setStatus(UserStatus.INACTIVE);
        inventoryManagerRepository.save(invManager);
    }

    /**
     * Map entity to response DTO
     */
    private InventoryManagerResponse mapToResponse(InventoryManager invManager) {
        return InventoryManagerResponse.builder()
                .id(invManager.getId())
                .userId(invManager.getUser().getId())
                .email(invManager.getUser().getEmail())
                .phone(invManager.getUser().getPhone())
                .firstName(invManager.getFirstName())
                .lastName(invManager.getLastName())
                .employeeId(invManager.getEmployeeId())
                .status(invManager.getUser().getStatus())
                .createdAt(invManager.getCreatedAt())
                .build();
    }
}
