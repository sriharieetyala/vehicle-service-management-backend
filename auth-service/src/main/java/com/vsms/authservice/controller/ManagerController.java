package com.vsms.authservice.controller;

import com.vsms.authservice.dto.request.ManagerCreateRequest;
import com.vsms.authservice.dto.request.ManagerUpdateRequest;
import com.vsms.authservice.dto.response.ApiResponse;
import com.vsms.authservice.dto.response.CreatedResponse;
import com.vsms.authservice.dto.response.ManagerResponse;
import com.vsms.authservice.enums.Department;
import com.vsms.authservice.service.ManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ManagerController handles all manager related operations
// Only admins can create managers as they don't self register
@RestController
@RequestMapping("/api/managers")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;

    // Admin creates managers with temp password that must be changed on first login
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CreatedResponse> createManager(
            @Valid @RequestBody ManagerCreateRequest request) {
        ManagerResponse response = managerService.createManager(request);
        return new ResponseEntity<>(
                CreatedResponse.builder().id(response.getId()).build(),
                HttpStatus.CREATED);
    }

    // Get all managers with optional department filter
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ManagerResponse>>> getAllManagers(
            @RequestParam(required = false) Department department) {
        List<ManagerResponse> managers = managerService.getAllManagers(department);
        return ResponseEntity.ok(ApiResponse.success(managers));
    }

    // Managers can update their own profile, admins can update any
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ManagerResponse>> updateManager(
            @PathVariable Integer id,
            @Valid @RequestBody ManagerUpdateRequest request) {
        ManagerResponse response = managerService.updateManager(id, request);
        return ResponseEntity.ok(ApiResponse.success("Manager updated successfully", response));
    }

    // Only admins can delete manager accounts
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteManager(@PathVariable Integer id) {
        managerService.deleteManager(id);
        return ResponseEntity.ok().build();
    }
}
