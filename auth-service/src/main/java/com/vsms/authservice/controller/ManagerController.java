package com.vsms.authservice.controller;

import com.vsms.authservice.dto.request.ManagerCreateRequest;
import com.vsms.authservice.dto.request.ManagerUpdateRequest;
import com.vsms.authservice.dto.response.ApiResponse;
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

@RestController
@RequestMapping("/api/managers")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;

    // Only Admin can create managers
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<ManagerResponse>> createManager(
            @Valid @RequestBody ManagerCreateRequest request) {
        ManagerResponse response = managerService.createManager(request);
        return new ResponseEntity<>(
                ApiResponse.success("Manager created successfully. Credentials sent to email.", response),
                HttpStatus.CREATED);
    }

    // Only Admin can view all managers
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ManagerResponse>>> getAllManagers(
            @RequestParam(required = false) Department department) {
        List<ManagerResponse> managers = managerService.getAllManagers(department);
        return ResponseEntity.ok(ApiResponse.success(managers));
    }

    // Manager can update own profile, Admin can update any
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ManagerResponse>> updateManager(
            @PathVariable Integer id,
            @Valid @RequestBody ManagerUpdateRequest request) {
        ManagerResponse response = managerService.updateManager(id, request);
        return ResponseEntity.ok(ApiResponse.success("Manager updated successfully", response));
    }

    // Only Admin can delete managers
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteManager(@PathVariable Integer id) {
        managerService.deleteManager(id);
        return ResponseEntity.ok(ApiResponse.success("Manager deactivated", null));
    }
}
