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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/managers")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;

    @PostMapping
    public ResponseEntity<ApiResponse<ManagerResponse>> createManager(
            @Valid @RequestBody ManagerCreateRequest request) {
        ManagerResponse response = managerService.createManager(request);
        return new ResponseEntity<>(
                ApiResponse.success("Manager created successfully. Credentials sent to email.", response),
                HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ManagerResponse>>> getAllManagers(
            @RequestParam(required = false) Department department) {
        List<ManagerResponse> managers = managerService.getAllManagers(department);
        return ResponseEntity.ok(ApiResponse.success(managers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ManagerResponse>> getManagerById(@PathVariable Integer id) {
        ManagerResponse response = managerService.getManagerById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ManagerResponse>> updateManager(
            @PathVariable Integer id,
            @Valid @RequestBody ManagerUpdateRequest request) {
        ManagerResponse response = managerService.updateManager(id, request);
        return ResponseEntity.ok(ApiResponse.success("Manager updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteManager(@PathVariable Integer id) {
        managerService.deleteManager(id);
        return ResponseEntity.ok(ApiResponse.success("Manager deactivated", null));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getManagerCount(
            @RequestParam(required = false) Department department) {
        long count = managerService.getManagerCount(department);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
