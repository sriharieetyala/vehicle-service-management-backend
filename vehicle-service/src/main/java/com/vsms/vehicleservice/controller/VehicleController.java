package com.vsms.vehicleservice.controller;

import com.vsms.vehicleservice.dto.request.VehicleCreateRequest;
import com.vsms.vehicleservice.dto.request.VehicleUpdateRequest;
import com.vsms.vehicleservice.dto.response.ApiResponse;
import com.vsms.vehicleservice.dto.response.CreatedResponse;
import com.vsms.vehicleservice.dto.response.VehicleResponse;
import com.vsms.vehicleservice.exception.UnauthorizedException;
import com.vsms.vehicleservice.security.SecurityHelper;
import com.vsms.vehicleservice.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// VehicleController handles all vehicle registration and management
// Gateway handles role checks, I handle ownership validation here
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final SecurityHelper securityHelper;

    // Create a new vehicle for the logged in customer
    // Customers can only register vehicles for themselves
    @PostMapping
    public ResponseEntity<CreatedResponse> createVehicle(
            @Valid @RequestBody VehicleCreateRequest request) {
        Integer currentUserId = securityHelper.getCurrentUserId();
        if (currentUserId != null && !securityHelper.isManagerOrAdmin()
                && !currentUserId.equals(request.getCustomerId())) {
            throw new UnauthorizedException("You can only register vehicles for yourself");
        }

        VehicleResponse response = vehicleService.createVehicle(request);
        return new ResponseEntity<>(
                CreatedResponse.builder().id(response.getId()).build(),
                HttpStatus.CREATED);
    }

    // Get vehicle by ID with ownership check
    // Customers can only see their own vehicles
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleById(@PathVariable Integer id) {
        VehicleResponse vehicle = vehicleService.getVehicleById(id);

        Integer currentUserId = securityHelper.getCurrentUserId();
        if (currentUserId != null && !securityHelper.isManagerOrAdmin()
                && !vehicle.getCustomerId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only view your own vehicles");
        }

        return ResponseEntity.ok(ApiResponse.success(vehicle));
    }

    // Get all vehicles for a customer
    // Managers can see any customer's vehicles
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getVehiclesByCustomerId(
            @PathVariable Integer customerId) {
        Integer currentUserId = securityHelper.getCurrentUserId();
        if (currentUserId != null && !securityHelper.isManagerOrAdmin()
                && !customerId.equals(currentUserId)) {
            throw new UnauthorizedException("You can only view your own vehicles");
        }

        List<VehicleResponse> vehicles = vehicleService.getVehiclesByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(vehicles));
    }

    // Update vehicle details like plate number or model
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @PathVariable Integer id,
            @Valid @RequestBody VehicleUpdateRequest request) {
        Integer currentUserId = securityHelper.getCurrentUserId();
        if (currentUserId != null && !securityHelper.isManagerOrAdmin()
                && !vehicleService.isOwner(id, currentUserId)) {
            throw new UnauthorizedException("You can only update your own vehicles");
        }

        VehicleResponse response = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(ApiResponse.success("Vehicle updated successfully", response));
    }

    // Delete a vehicle from the system
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Integer id) {
        Integer currentUserId = securityHelper.getCurrentUserId();
        if (currentUserId != null && !securityHelper.isManagerOrAdmin()
                && !vehicleService.isOwner(id, currentUserId)) {
            throw new UnauthorizedException("You can only delete your own vehicles");
        }

        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok().build();
    }
}
