package com.vsms.vehicleservice.controller;

import com.vsms.vehicleservice.dto.request.VehicleCreateRequest;
import com.vsms.vehicleservice.dto.request.VehicleUpdateRequest;
import com.vsms.vehicleservice.dto.response.ApiResponse;
import com.vsms.vehicleservice.dto.response.VehicleResponse;
import com.vsms.vehicleservice.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(
            @Valid @RequestBody VehicleCreateRequest request) {
        VehicleResponse response = vehicleService.createVehicle(request);
        return new ResponseEntity<>(
                ApiResponse.success("Vehicle registered successfully", response),
                HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleById(@PathVariable Integer id) {
        VehicleResponse response = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getVehiclesByCustomerId(
            @PathVariable Integer customerId) {
        List<VehicleResponse> vehicles = vehicleService.getVehiclesByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(vehicles));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @PathVariable Integer id,
            @Valid @RequestBody VehicleUpdateRequest request) {
        VehicleResponse response = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(ApiResponse.success("Vehicle updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(@PathVariable Integer id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle deleted successfully", null));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getVehicleCount() {
        long count = vehicleService.getVehicleCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
