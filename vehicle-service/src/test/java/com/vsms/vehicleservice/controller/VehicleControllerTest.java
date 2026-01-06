package com.vsms.vehicleservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsms.vehicleservice.dto.request.VehicleCreateRequest;
import com.vsms.vehicleservice.dto.request.VehicleUpdateRequest;
import com.vsms.vehicleservice.dto.response.VehicleResponse;
import com.vsms.vehicleservice.enums.FuelType;
import com.vsms.vehicleservice.enums.VehicleType;
import com.vsms.vehicleservice.exception.GlobalExceptionHandler;
import com.vsms.vehicleservice.security.SecurityHelper;
import com.vsms.vehicleservice.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VehicleControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private VehicleService vehicleService;

    @Mock
    private SecurityHelper securityHelper;

    @InjectMocks
    private VehicleController vehicleController;

    private VehicleResponse testResponse;
    private VehicleCreateRequest createRequest;
    private VehicleUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(vehicleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        testResponse = VehicleResponse.builder()
                .id(1)
                .customerId(1)
                .plateNumber("KA01AB1234")
                .brand("Toyota")
                .model("Camry")
                .year(2022)
                .fuelType(FuelType.PETROL)
                .createdAt(LocalDateTime.now())
                .build();

        createRequest = new VehicleCreateRequest();
        createRequest.setCustomerId(1);
        createRequest.setPlateNumber("KA01AB1234");
        createRequest.setBrand("Toyota");
        createRequest.setModel("Camry");
        createRequest.setYear(2022);
        createRequest.setFuelType(FuelType.PETROL);
        createRequest.setVehicleType(VehicleType.FOUR_WHEELER);

        updateRequest = new VehicleUpdateRequest();
        updateRequest.setBrand("Honda");
        updateRequest.setModel("Civic");

        // Default: simulate manager/admin access (bypasses ownership checks)
        when(securityHelper.getCurrentUserId()).thenReturn(1);
        when(securityHelper.isManagerOrAdmin()).thenReturn(true);
    }

    // ===== CREATE VEHICLE TESTS =====

    @Test
    void createVehicle_Success() throws Exception {
        when(vehicleService.createVehicle(any(VehicleCreateRequest.class))).thenReturn(testResponse);

        mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(vehicleService, times(1)).createVehicle(any(VehicleCreateRequest.class));
    }

    @Test
    void createVehicle_CustomerCanCreateForSelf() throws Exception {
        when(securityHelper.getCurrentUserId()).thenReturn(1);
        when(securityHelper.isManagerOrAdmin()).thenReturn(false);
        when(vehicleService.createVehicle(any(VehicleCreateRequest.class))).thenReturn(testResponse);

        mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void createVehicle_CustomerCannotCreateForOthers() throws Exception {
        when(securityHelper.getCurrentUserId()).thenReturn(999); // Different user
        when(securityHelper.isManagerOrAdmin()).thenReturn(false);

        mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createVehicle_NullUserId_AllowsCreation() throws Exception {
        when(securityHelper.getCurrentUserId()).thenReturn(null);
        when(vehicleService.createVehicle(any(VehicleCreateRequest.class))).thenReturn(testResponse);

        mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }

    // ===== GET VEHICLE BY ID TESTS =====

    @Test
    void getVehicleById_Success() throws Exception {
        when(vehicleService.getVehicleById(1)).thenReturn(testResponse);

        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.brand").value("Toyota"));

        verify(vehicleService, times(1)).getVehicleById(1);
    }

    @Test
    void getVehicleById_CustomerOwnsVehicle_ReturnsOk() throws Exception {
        when(securityHelper.getCurrentUserId()).thenReturn(1);
        when(securityHelper.isManagerOrAdmin()).thenReturn(false);
        when(vehicleService.getVehicleById(1)).thenReturn(testResponse);

        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getVehicleById_CustomerDoesNotOwnVehicle_Unauthorized() throws Exception {
        when(securityHelper.getCurrentUserId()).thenReturn(999);
        when(securityHelper.isManagerOrAdmin()).thenReturn(false);
        when(vehicleService.getVehicleById(1)).thenReturn(testResponse);

        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getVehicleById_NullUserId_AllowsAccess() throws Exception {
        when(securityHelper.getCurrentUserId()).thenReturn(null);
        when(vehicleService.getVehicleById(1)).thenReturn(testResponse);

        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isOk());
    }

    // ===== GET VEHICLES BY CUSTOMER ID TESTS =====

    @Test
    void getVehiclesByCustomerId_Success() throws Exception {
        when(vehicleService.getVehiclesByCustomerId(1)).thenReturn(List.of(testResponse));

        mockMvc.perform(get("/api/vehicles/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1));

        verify(vehicleService, times(1)).getVehiclesByCustomerId(1);
    }

    @Test
    void getVehiclesByCustomerId_CustomerOwns_ReturnsOk() throws Exception {
        when(securityHelper.getCurrentUserId()).thenReturn(1);
        when(securityHelper.isManagerOrAdmin()).thenReturn(false);
        when(vehicleService.getVehiclesByCustomerId(1)).thenReturn(List.of(testResponse));

        mockMvc.perform(get("/api/vehicles/customer/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getVehiclesByCustomerId_CustomerAccessOther_Unauthorized() throws Exception {
        when(securityHelper.getCurrentUserId()).thenReturn(999);
        when(securityHelper.isManagerOrAdmin()).thenReturn(false);

        mockMvc.perform(get("/api/vehicles/customer/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getVehiclesByCustomerId_NullUserId_AllowsAccess() throws Exception {
        when(securityHelper.getCurrentUserId()).thenReturn(null);
        when(vehicleService.getVehiclesByCustomerId(1)).thenReturn(List.of(testResponse));

        mockMvc.perform(get("/api/vehicles/customer/1"))
                .andExpect(status().isOk());
    }

    // ===== UPDATE VEHICLE TESTS =====

    @Test
    void updateVehicle_Success() throws Exception {
        when(vehicleService.updateVehicle(eq(1), any(VehicleUpdateRequest.class))).thenReturn(testResponse);

        mockMvc.perform(put("/api/vehicles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(vehicleService, times(1)).updateVehicle(eq(1), any(VehicleUpdateRequest.class));
    }

    @Test
    void updateVehicle_OwnerCanUpdate() throws Exception {
        when(securityHelper.getCurrentUserId()).thenReturn(1);
        when(securityHelper.isManagerOrAdmin()).thenReturn(false);
        when(vehicleService.isOwner(1, 1)).thenReturn(true);
        when(vehicleService.updateVehicle(eq(1), any(VehicleUpdateRequest.class))).thenReturn(testResponse);

        mockMvc.perform(put("/api/vehicles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void updateVehicle_NonOwnerCannotUpdate() throws Exception {
        when(securityHelper.getCurrentUserId()).thenReturn(999);
        when(securityHelper.isManagerOrAdmin()).thenReturn(false);
        when(vehicleService.isOwner(1, 999)).thenReturn(false);

        mockMvc.perform(put("/api/vehicles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateVehicle_NullUserId_AllowsUpdate() throws Exception {
        when(securityHelper.getCurrentUserId()).thenReturn(null);
        when(vehicleService.updateVehicle(eq(1), any(VehicleUpdateRequest.class))).thenReturn(testResponse);

        mockMvc.perform(put("/api/vehicles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    // ===== DELETE VEHICLE TESTS =====

    @Test
    void deleteVehicle_Success() throws Exception {
        doNothing().when(vehicleService).deleteVehicle(1);

        mockMvc.perform(delete("/api/vehicles/1"))
                .andExpect(status().isOk());

        verify(vehicleService, times(1)).deleteVehicle(1);
    }

    @Test
    void deleteVehicle_OwnerCanDelete() throws Exception {
        when(securityHelper.getCurrentUserId()).thenReturn(1);
        when(securityHelper.isManagerOrAdmin()).thenReturn(false);
        when(vehicleService.isOwner(1, 1)).thenReturn(true);
        doNothing().when(vehicleService).deleteVehicle(1);

        mockMvc.perform(delete("/api/vehicles/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteVehicle_NonOwnerCannotDelete() throws Exception {
        when(securityHelper.getCurrentUserId()).thenReturn(999);
        when(securityHelper.isManagerOrAdmin()).thenReturn(false);
        when(vehicleService.isOwner(1, 999)).thenReturn(false);

        mockMvc.perform(delete("/api/vehicles/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteVehicle_NullUserId_AllowsDelete() throws Exception {
        when(securityHelper.getCurrentUserId()).thenReturn(null);
        doNothing().when(vehicleService).deleteVehicle(1);

        mockMvc.perform(delete("/api/vehicles/1"))
                .andExpect(status().isOk());
    }

    // ===== MANAGER ACCESS TESTS =====

    @Test
    void managerCanAccessAnyVehicle() throws Exception {
        when(securityHelper.getCurrentUserId()).thenReturn(999); // Different user
        when(securityHelper.isManagerOrAdmin()).thenReturn(true); // But is manager
        when(vehicleService.getVehicleById(1)).thenReturn(testResponse);

        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isOk());
    }

    @Test
    void managerCanUpdateAnyVehicle() throws Exception {
        when(securityHelper.getCurrentUserId()).thenReturn(999);
        when(securityHelper.isManagerOrAdmin()).thenReturn(true);
        when(vehicleService.updateVehicle(eq(1), any(VehicleUpdateRequest.class))).thenReturn(testResponse);

        mockMvc.perform(put("/api/vehicles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void managerCanDeleteAnyVehicle() throws Exception {
        when(securityHelper.getCurrentUserId()).thenReturn(999);
        when(securityHelper.isManagerOrAdmin()).thenReturn(true);
        doNothing().when(vehicleService).deleteVehicle(1);

        mockMvc.perform(delete("/api/vehicles/1"))
                .andExpect(status().isOk());
    }
}
