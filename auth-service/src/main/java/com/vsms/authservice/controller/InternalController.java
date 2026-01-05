package com.vsms.authservice.controller;

import com.vsms.authservice.dto.response.ApiResponse;
import com.vsms.authservice.dto.response.CustomerResponse;
import com.vsms.authservice.dto.response.ManagerResponse;
import com.vsms.authservice.service.CustomerService;
import com.vsms.authservice.service.ManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Internal API endpoints for service-to-service communication.
 * These endpoints have NO authentication - only for internal microservice
 * calls.
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    private final CustomerService customerService;
    private final ManagerService managerService;

    /**
     * Get customer by ID - no auth required (internal service call)
     */
    @GetMapping("/customers/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable Integer id) {
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all managers - no auth required (internal service call)
     */
    @GetMapping("/managers")
    public ResponseEntity<ApiResponse<List<ManagerResponse>>> getAllManagers() {
        List<ManagerResponse> managers = managerService.getAllManagers(null);
        return ResponseEntity.ok(ApiResponse.success(managers));
    }
}
