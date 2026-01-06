package com.vsms.vehicleservice.service;

import com.vsms.vehicleservice.client.AuthServiceClient;
import com.vsms.vehicleservice.dto.request.VehicleCreateRequest;
import com.vsms.vehicleservice.dto.request.VehicleUpdateRequest;
import com.vsms.vehicleservice.dto.response.VehicleResponse;
import com.vsms.vehicleservice.entity.Vehicle;
import com.vsms.vehicleservice.exception.DuplicateResourceException;
import com.vsms.vehicleservice.exception.ResourceNotFoundException;
import com.vsms.vehicleservice.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// VehicleService handles all business logic for vehicle operations
// I call auth service via Feign to validate customer exists before creating vehicle
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final AuthServiceClient authServiceClient;

    // Register a new vehicle for a customer
    // I validate the customer exists in auth service first via Feign
    public VehicleResponse createVehicle(VehicleCreateRequest request) {
        var customerResponse = authServiceClient.getCustomerById(request.getCustomerId());
        if (customerResponse == null) {
            throw new ResourceNotFoundException("Customer", "id", request.getCustomerId());
        }

        // Check for duplicate plate number
        if (vehicleRepository.existsByPlateNumber(request.getPlateNumber())) {
            throw new DuplicateResourceException("Vehicle", "plateNumber", request.getPlateNumber());
        }

        Vehicle vehicle = Vehicle.builder()
                .customerId(request.getCustomerId())
                .plateNumber(request.getPlateNumber().toUpperCase())
                .brand(request.getBrand())
                .model(request.getModel())
                .year(request.getYear())
                .fuelType(request.getFuelType())
                .vehicleType(request.getVehicleType())
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Vehicle registered for customer {}: {}", request.getCustomerId(), saved.getPlateNumber());
        return mapToResponse(saved);
    }

    // Get a single vehicle by ID
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Integer id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));
        return mapToResponse(vehicle);
    }

    // Get all vehicles for a specific customer
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByCustomerId(Integer customerId) {
        return vehicleRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Update vehicle details like brand, model, year
    // I don't allow changing plate number or customer ID
    public VehicleResponse updateVehicle(Integer id, VehicleUpdateRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));

        if (request.getBrand() != null) {
            vehicle.setBrand(request.getBrand());
        }
        if (request.getModel() != null) {
            vehicle.setModel(request.getModel());
        }
        if (request.getYear() != null) {
            vehicle.setYear(request.getYear());
        }
        if (request.getFuelType() != null) {
            vehicle.setFuelType(request.getFuelType());
        }
        if (request.getVehicleType() != null) {
            vehicle.setVehicleType(request.getVehicleType());
        }

        Vehicle updated = vehicleRepository.save(vehicle);
        return mapToResponse(updated);
    }

    // Delete a vehicle from the system
    public void deleteVehicle(Integer id) {
        if (!vehicleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicle", "id", id);
        }
        vehicleRepository.deleteById(id);
    }

    // Get total vehicle count for dashboard stats
    @Transactional(readOnly = true)
    public long getVehicleCount() {
        return vehicleRepository.count();
    }

    // Check if a customer owns a specific vehicle for ownership validation
    @Transactional(readOnly = true)
    public boolean isOwner(Integer vehicleId, Integer customerId) {
        return vehicleRepository.findById(vehicleId)
                .map(v -> v.getCustomerId().equals(customerId))
                .orElse(false);
    }

    // Maps Vehicle entity to response DTO
    private VehicleResponse mapToResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .customerId(vehicle.getCustomerId())
                .plateNumber(vehicle.getPlateNumber())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .fuelType(vehicle.getFuelType())
                .vehicleType(vehicle.getVehicleType())
                .createdAt(vehicle.getCreatedAt())
                .build();
    }
}
