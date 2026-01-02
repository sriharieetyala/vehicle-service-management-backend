package com.vsms.vehicleservice.service;

import com.vsms.vehicleservice.client.AuthServiceClient;
import com.vsms.vehicleservice.dto.request.VehicleCreateRequest;
import com.vsms.vehicleservice.dto.request.VehicleUpdateRequest;
import com.vsms.vehicleservice.dto.response.VehicleResponse;
import com.vsms.vehicleservice.entity.Vehicle;
import com.vsms.vehicleservice.enums.FuelType;
import com.vsms.vehicleservice.exception.DuplicateResourceException;
import com.vsms.vehicleservice.exception.ResourceNotFoundException;
import com.vsms.vehicleservice.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        testVehicle = Vehicle.builder()
                .id(1)
                .customerId(1)
                .plateNumber("KA01AB1234")
                .brand("Toyota")
                .model("Camry")
                .year(2022)
                .fuelType(FuelType.PETROL)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createVehicle_Success() {
        VehicleCreateRequest request = new VehicleCreateRequest();
        request.setCustomerId(1);
        request.setPlateNumber("KA01AB1234");
        request.setBrand("Toyota");
        request.setModel("Camry");
        request.setYear(2022);
        request.setFuelType(FuelType.PETROL);

        when(authServiceClient.getCustomerById(1)).thenReturn(null);
        when(vehicleRepository.existsByPlateNumber("KA01AB1234")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> {
            Vehicle v = inv.getArgument(0);
            v.setId(1);
            v.setCreatedAt(LocalDateTime.now());
            return v;
        });

        VehicleResponse response = vehicleService.createVehicle(request);

        assertNotNull(response);
        assertEquals("KA01AB1234", response.getPlateNumber());
        assertEquals("Toyota", response.getBrand());
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    void createVehicle_CustomerNotFound_ThrowsException() {
        VehicleCreateRequest request = new VehicleCreateRequest();
        request.setCustomerId(999);
        request.setPlateNumber("KA01AB1234");

        when(authServiceClient.getCustomerById(999)).thenThrow(new RuntimeException("Not found"));

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.createVehicle(request));
    }

    @Test
    void createVehicle_DuplicatePlate_ThrowsException() {
        VehicleCreateRequest request = new VehicleCreateRequest();
        request.setCustomerId(1);
        request.setPlateNumber("KA01AB1234");

        when(authServiceClient.getCustomerById(1)).thenReturn(null);
        when(vehicleRepository.existsByPlateNumber("KA01AB1234")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> vehicleService.createVehicle(request));
    }

    @Test
    void getVehicleById_Success() {
        when(vehicleRepository.findById(1)).thenReturn(Optional.of(testVehicle));

        VehicleResponse response = vehicleService.getVehicleById(1);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("Toyota", response.getBrand());
    }

    @Test
    void getVehicleById_NotFound_ThrowsException() {
        when(vehicleRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.getVehicleById(999));
    }

    @Test
    void getVehiclesByCustomerId_Success() {
        when(vehicleRepository.findByCustomerId(1)).thenReturn(List.of(testVehicle));

        List<VehicleResponse> responses = vehicleService.getVehiclesByCustomerId(1);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("KA01AB1234", responses.get(0).getPlateNumber());
    }

    @Test
    void updateVehicle_Success() {
        VehicleUpdateRequest request = new VehicleUpdateRequest();
        request.setBrand("Honda");
        request.setModel("Civic");
        request.setYear(2023);
        request.setFuelType(FuelType.HYBRID);

        when(vehicleRepository.findById(1)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);

        VehicleResponse response = vehicleService.updateVehicle(1, request);

        assertNotNull(response);
        verify(vehicleRepository, times(1)).save(testVehicle);
    }

    @Test
    void deleteVehicle_Success() {
        when(vehicleRepository.existsById(1)).thenReturn(true);
        doNothing().when(vehicleRepository).deleteById(1);

        vehicleService.deleteVehicle(1);

        verify(vehicleRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteVehicle_NotFound_ThrowsException() {
        when(vehicleRepository.existsById(999)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.deleteVehicle(999));
    }
}
