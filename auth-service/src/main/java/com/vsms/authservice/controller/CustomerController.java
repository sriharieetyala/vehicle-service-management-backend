package com.vsms.authservice.controller;

import com.vsms.authservice.dto.request.CustomerCreateRequest;
import com.vsms.authservice.dto.request.CustomerUpdateRequest;
import com.vsms.authservice.dto.response.ApiResponse;
import com.vsms.authservice.dto.response.CreatedResponse;
import com.vsms.authservice.dto.response.CustomerResponse;
import com.vsms.authservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // Public - Customer registration - returns just ID
    @PostMapping
    public ResponseEntity<CreatedResponse> createCustomer(
            @Valid @RequestBody CustomerCreateRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return new ResponseEntity<>(
                CreatedResponse.builder().id(response.getId()).build(),
                HttpStatus.CREATED);
    }

    // Get all customers (role check done at gateway)
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers() {
        List<CustomerResponse> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    // Customer can view own, Manager/Admin can view any (ownership check)
    @PreAuthorize("#id == authentication.principal.id or hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable Integer id) {
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Customer can update own profile (ownership check)
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable Integer id,
            @Valid @RequestBody CustomerUpdateRequest request) {
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", response));
    }

    // Customer can delete own account, or Admin (ownership check)
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Integer id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok().build();
    }
}
