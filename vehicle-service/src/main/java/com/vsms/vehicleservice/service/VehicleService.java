package com.vsms.vehicleservice.service;

import com.vsms.vehicleservice.client.AuthServiceClient;
import com.vsms.vehicleservice.dto.request.VehicleCreateRequest;
import com.vsms.vehicleservice.dto.request.VehicleUpdateRequest;
import com.vsms.vehicleservice.dto.response.ApiResponse;
import com.vsms.vehicleservice.dto.response.VehicleResponse;
import com.vsms.vehicleservice.entity.Vehicle;
import com.vsms.vehicleservice.exception.DuplicateResourceException;
import com.vsms.vehicleservice.exception.ResourceNotFoundException;
import com.vsms.vehicleservice.repository.VehicleRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final AuthServiceClient authServiceClient;

    /**
     * Register a new vehicle for a customer
     * First validates that customer exists in auth-service via Feign call
     */
    public VehicleResponse createVehicle(VehicleCreateRequest request) {
        // STEP 1: Validate customer exists via Feign call to auth-service
        validateCustomerExists(request.getCustomerId());

        // STEP 2: Check plate number not duplicate
        if (vehicleRepository.existsByPlateNumber(request.getPlateNumber())) {
            throw new DuplicateResourceException("Vehicle", "plateNumber", request.getPlateNumber());
        }

        // STEP 3: Create vehicle
        Vehicle vehicle = Vehicle.builder()
                .customerId(request.getCustomerId())
                .plateNumber(request.getPlateNumber().toUpperCase())
                .brand(request.getBrand())
                .model(request.getModel())
                .year(request.getYear())
                .fuelType(request.getFuelType())
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Vehicle created for customer {}: {}", request.getCustomerId(), saved.getPlateNumber());
        return mapToResponse(saved);
    }

    /**
     * Validate customer exists by calling auth-service
     * This is the ACTUAL inter-service communication
     */
    private void validateCustomerExists(Integer customerId) {
        try {
            log.info("Calling auth-service to validate customer {} exists", customerId);

            // THIS LINE MAKES HTTP CALL TO: GET http://auth-service/api/customers/{id}
            ApiResponse<?> response = authServiceClient.getCustomerById(customerId);

            if (!response.isSuccess()) {
                throw new ResourceNotFoundException("Customer", "id", customerId);
            }
            log.info("Customer {} validated successfully via auth-service", customerId);
        } catch (FeignException.NotFound e) {
            log.error("Customer {} not found in auth-service", customerId);
            throw new ResourceNotFoundException("Customer", "id", customerId);
        } catch (FeignException e) {
            log.error("Error calling auth-service: {}", e.getMessage());
            throw new RuntimeException("Unable to validate customer. Auth service unavailable.");
        }
    }

    /**
     * Get vehicle by ID
     */
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Integer id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));
        return mapToResponse(vehicle);
    }

    /**
     * Get all vehicles for a customer
     */
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByCustomerId(Integer customerId) {
        return vehicleRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update vehicle details
     */
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

        Vehicle updated = vehicleRepository.save(vehicle);
        return mapToResponse(updated);
    }

    /**
     * Delete vehicle
     */
    public void deleteVehicle(Integer id) {
        if (!vehicleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicle", "id", id);
        }
        vehicleRepository.deleteById(id);
    }

    /**
     * Get total vehicle count
     */
    @Transactional(readOnly = true)
    public long getVehicleCount() {
        return vehicleRepository.count();
    }

    /**
     * Map entity to response DTO
     */
    private VehicleResponse mapToResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .customerId(vehicle.getCustomerId())
                .plateNumber(vehicle.getPlateNumber())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .fuelType(vehicle.getFuelType())
                .createdAt(vehicle.getCreatedAt())
                .build();
    }
}
