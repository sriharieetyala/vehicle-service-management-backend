package com.vsms.vehicleservice.service;

import com.vsms.vehicleservice.client.AuthServiceClient;
import com.vsms.vehicleservice.dto.request.VehicleCreateRequest;
import com.vsms.vehicleservice.dto.request.VehicleUpdateRequest;
import com.vsms.vehicleservice.dto.response.ApiResponse;
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
    private ApiResponse<?> mockCustomerResponse;

    @BeforeEach
    void setUp() {
        mockCustomerResponse = new ApiResponse<>(true, "Customer found", new Object(), LocalDateTime.now());

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

        doReturn(mockCustomerResponse).when(authServiceClient).getCustomerById(1);
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

        when(authServiceClient.getCustomerById(999)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.createVehicle(request));
    }

    @Test
    void createVehicle_DuplicatePlate_ThrowsException() {
        VehicleCreateRequest request = new VehicleCreateRequest();
        request.setCustomerId(1);
        request.setPlateNumber("KA01AB1234");

        doReturn(mockCustomerResponse).when(authServiceClient).getCustomerById(1);
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

    @Test
    void getVehicleCount_Success() {
        when(vehicleRepository.count()).thenReturn(5L);

        long count = vehicleService.getVehicleCount();

        assertEquals(5L, count);
    }

    @Test
    void isOwner_ReturnsTrue_WhenCustomerOwnsVehicle() {
        when(vehicleRepository.findById(1)).thenReturn(Optional.of(testVehicle));

        boolean result = vehicleService.isOwner(1, 1);

        assertTrue(result);
    }

    @Test
    void isOwner_ReturnsFalse_WhenCustomerDoesNotOwnVehicle() {
        when(vehicleRepository.findById(1)).thenReturn(Optional.of(testVehicle));

        boolean result = vehicleService.isOwner(1, 999);

        assertFalse(result);
    }

    @Test
    void isOwner_ReturnsFalse_WhenVehicleNotFound() {
        when(vehicleRepository.findById(999)).thenReturn(Optional.empty());

        boolean result = vehicleService.isOwner(999, 1);

        assertFalse(result);
    }

    @Test
    void updateVehicle_PartialUpdate_OnlyBrand() {
        VehicleUpdateRequest request = new VehicleUpdateRequest();
        request.setBrand("Mazda");
        // Other fields null

        when(vehicleRepository.findById(1)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);

        VehicleResponse response = vehicleService.updateVehicle(1, request);

        assertNotNull(response);
        verify(vehicleRepository, times(1)).save(testVehicle);
        assertEquals("Mazda", testVehicle.getBrand());
        // Model unchanged
        assertEquals("Camry", testVehicle.getModel());
    }

    @Test
    void updateVehicle_NotFound_ThrowsException() {
        VehicleUpdateRequest request = new VehicleUpdateRequest();
        request.setBrand("Test");

        when(vehicleRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.updateVehicle(999, request));
    }

    @Test
    void getVehiclesByCustomerId_EmptyList_ReturnsEmpty() {
        when(vehicleRepository.findByCustomerId(999)).thenReturn(List.of());

        List<VehicleResponse> responses = vehicleService.getVehiclesByCustomerId(999);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void updateVehicle_AllFields_UpdatesAll() {
        VehicleUpdateRequest request = new VehicleUpdateRequest();
        request.setBrand("BMW");
        request.setModel("X5");
        request.setYear(2024);
        request.setFuelType(FuelType.DIESEL);

        when(vehicleRepository.findById(1)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);

        vehicleService.updateVehicle(1, request);

        assertEquals("BMW", testVehicle.getBrand());
        assertEquals("X5", testVehicle.getModel());
        assertEquals(2024, testVehicle.getYear());
        assertEquals(FuelType.DIESEL, testVehicle.getFuelType());
    }
}
