package com.vsms.vehicleservice.controller;

import com.vsms.vehicleservice.dto.request.VehicleCreateRequest;
import com.vsms.vehicleservice.dto.request.VehicleUpdateRequest;
import com.vsms.vehicleservice.dto.response.ApiResponse;
import com.vsms.vehicleservice.dto.response.CreatedResponse;
import com.vsms.vehicleservice.dto.response.VehicleResponse;
import com.vsms.vehicleservice.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    // Create vehicle (role check at gateway)
    @PostMapping
    public ResponseEntity<CreatedResponse> createVehicle(
            @Valid @RequestBody VehicleCreateRequest request) {
        VehicleResponse response = vehicleService.createVehicle(request);
        return new ResponseEntity<>(
                CreatedResponse.builder().id(response.getId()).build(),
                HttpStatus.CREATED);
    }

    // Get vehicle by ID - ownership check: customer can only see their own
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or @vehicleService.isOwner(#id, authentication.principal.id)")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleById(@PathVariable Integer id) {
        VehicleResponse response = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Get vehicles by customer - ownership check
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or #customerId == authentication.principal.id")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getVehiclesByCustomerId(
            @PathVariable Integer customerId) {
        List<VehicleResponse> vehicles = vehicleService.getVehiclesByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(vehicles));
    }

    // Update vehicle - ownership check
    @PutMapping("/{id}")
    @PreAuthorize("@vehicleService.isOwner(#id, authentication.principal.id)")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @PathVariable Integer id,
            @Valid @RequestBody VehicleUpdateRequest request) {
        VehicleResponse response = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(ApiResponse.success("Vehicle updated successfully", response));
    }

    // Delete vehicle - ownership check
    @DeleteMapping("/{id}")
    @PreAuthorize("@vehicleService.isOwner(#id, authentication.principal.id)")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Integer id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok().build();
    }
}
