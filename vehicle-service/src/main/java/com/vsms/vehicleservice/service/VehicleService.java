package com.vsms.vehicleservice.service;

import com.vsms.vehicleservice.dto.request.VehicleCreateRequest;
import com.vsms.vehicleservice.dto.request.VehicleUpdateRequest;
import com.vsms.vehicleservice.dto.response.VehicleResponse;
import com.vsms.vehicleservice.entity.Vehicle;
import com.vsms.vehicleservice.exception.DuplicateResourceException;
import com.vsms.vehicleservice.exception.ResourceNotFoundException;
import com.vsms.vehicleservice.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    /**
     * Register a new vehicle for a customer
     */
    public VehicleResponse createVehicle(VehicleCreateRequest request) {
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
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);
        return mapToResponse(saved);
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
     * Update vehicle details (brand, model, year, fuelType - NOT customerId or
     * plateNumber)
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
