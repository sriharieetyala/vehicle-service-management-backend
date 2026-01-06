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

// InternalController provides endpoints for service to service communication
// These have no auth because they are only called by other microservices
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    private final CustomerService customerService;
    private final ManagerService managerService;

    // Called by service request service to get customer info for notifications
    @GetMapping("/customers/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable Integer id) {
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Called by service request service to get manager emails for notifications
    @GetMapping("/managers")
    public ResponseEntity<ApiResponse<List<ManagerResponse>>> getAllManagers() {
        List<ManagerResponse> managers = managerService.getAllManagers(null);
        return ResponseEntity.ok(ApiResponse.success(managers));
    }
}
