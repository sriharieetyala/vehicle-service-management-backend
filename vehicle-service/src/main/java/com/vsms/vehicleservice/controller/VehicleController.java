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

/**
 * Vehicle Controller with ownership validation.
 * Gateway handles role-based access, this controller handles ownership checks.
 */
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final SecurityHelper securityHelper;

    // Create vehicle - any customer can create (for themselves)
    @PostMapping
    public ResponseEntity<CreatedResponse> createVehicle(
            @Valid @RequestBody VehicleCreateRequest request) {
        // Ensure customer can only create vehicles for themselves (unless
        // manager/admin)
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

    // Get vehicle by ID - ownership check
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleById(@PathVariable Integer id) {
        VehicleResponse vehicle = vehicleService.getVehicleById(id);

        // Check ownership (managers/admins can see any)
        Integer currentUserId = securityHelper.getCurrentUserId();
        if (currentUserId != null && !securityHelper.isManagerOrAdmin()
                && !vehicle.getCustomerId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only view your own vehicles");
        }

        return ResponseEntity.ok(ApiResponse.success(vehicle));
    }

    // Get vehicles by customer - ownership check
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getVehiclesByCustomerId(
            @PathVariable Integer customerId) {
        // Check ownership (managers/admins can see any customer's vehicles)
        Integer currentUserId = securityHelper.getCurrentUserId();
        if (currentUserId != null && !securityHelper.isManagerOrAdmin()
                && !customerId.equals(currentUserId)) {
            throw new UnauthorizedException("You can only view your own vehicles");
        }

        List<VehicleResponse> vehicles = vehicleService.getVehiclesByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(vehicles));
    }

    // Update vehicle - ownership check
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @PathVariable Integer id,
            @Valid @RequestBody VehicleUpdateRequest request) {
        // Check ownership
        Integer currentUserId = securityHelper.getCurrentUserId();
        if (currentUserId != null && !securityHelper.isManagerOrAdmin()
                && !vehicleService.isOwner(id, currentUserId)) {
            throw new UnauthorizedException("You can only update your own vehicles");
        }

        VehicleResponse response = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(ApiResponse.success("Vehicle updated successfully", response));
    }

    // Delete vehicle - ownership check
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Integer id) {
        // Check ownership
        Integer currentUserId = securityHelper.getCurrentUserId();
        if (currentUserId != null && !securityHelper.isManagerOrAdmin()
                && !vehicleService.isOwner(id, currentUserId)) {
            throw new UnauthorizedException("You can only delete your own vehicles");
        }

        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok().build();
    }
}
