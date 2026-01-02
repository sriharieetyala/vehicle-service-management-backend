package com.vsms.vehicleservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsms.vehicleservice.dto.request.VehicleCreateRequest;
import com.vsms.vehicleservice.dto.request.VehicleUpdateRequest;
import com.vsms.vehicleservice.dto.response.VehicleResponse;
import com.vsms.vehicleservice.enums.FuelType;
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

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private VehicleController vehicleController;

    private VehicleResponse testResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(vehicleController).build();
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
    }

    @Test
    void createVehicle_Success() throws Exception {
        VehicleCreateRequest request = new VehicleCreateRequest();
        request.setCustomerId(1);
        request.setPlateNumber("KA01AB1234");
        request.setBrand("Toyota");
        request.setModel("Camry");
        request.setYear(2022);
        request.setFuelType(FuelType.PETROL);

        when(vehicleService.createVehicle(any(VehicleCreateRequest.class))).thenReturn(testResponse);

        mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(vehicleService, times(1)).createVehicle(any(VehicleCreateRequest.class));
    }

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
    void updateVehicle_Success() throws Exception {
        VehicleUpdateRequest request = new VehicleUpdateRequest();
        request.setBrand("Honda");
        request.setModel("Civic");

        when(vehicleService.updateVehicle(eq(1), any(VehicleUpdateRequest.class))).thenReturn(testResponse);

        mockMvc.perform(put("/api/vehicles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(vehicleService, times(1)).updateVehicle(eq(1), any(VehicleUpdateRequest.class));
    }

    @Test
    void deleteVehicle_Success() throws Exception {
        doNothing().when(vehicleService).deleteVehicle(1);

        mockMvc.perform(delete("/api/vehicles/1"))
                .andExpect(status().isOk());

        verify(vehicleService, times(1)).deleteVehicle(1);
    }
}
