package com.vsms.servicerequestservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsms.servicerequestservice.dto.request.ServiceRequestCreateDTO;
import com.vsms.servicerequestservice.dto.response.DashboardStats;
import com.vsms.servicerequestservice.dto.response.ServiceRequestResponse;
import com.vsms.servicerequestservice.enums.Priority;
import com.vsms.servicerequestservice.enums.RequestStatus;
import com.vsms.servicerequestservice.enums.ServiceType;
import com.vsms.servicerequestservice.service.ServiceRequestService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ServiceRequestControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ServiceRequestService service;

    @InjectMocks
    private ServiceRequestController controller;

    private ServiceRequestResponse testResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        testResponse = ServiceRequestResponse.builder()
                .id(1)
                .customerId(1)
                .vehicleId(1)
                .serviceType(ServiceType.REGULAR_SERVICE)
                .priority(Priority.NORMAL)
                .status(RequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void create_Success() throws Exception {
        ServiceRequestCreateDTO dto = new ServiceRequestCreateDTO();
        dto.setCustomerId(1);
        dto.setVehicleId(1);
        dto.setServiceType(ServiceType.REGULAR_SERVICE);

        when(service.createRequest(any())).thenReturn(testResponse);

        mockMvc.perform(post("/api/service-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(service, times(1)).createRequest(any());
    }

    @Test
    void getById_Success() throws Exception {
        when(service.getById(1)).thenReturn(testResponse);

        mockMvc.perform(get("/api/service-requests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(service, times(1)).getById(1);
    }

    @Test
    void getByCustomer_Success() throws Exception {
        when(service.getByCustomerId(1)).thenReturn(List.of(testResponse));

        mockMvc.perform(get("/api/service-requests/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(service, times(1)).getByCustomerId(1);
    }

    @Test
    void getAll_Success() throws Exception {
        when(service.getAll(null)).thenReturn(List.of(testResponse));

        mockMvc.perform(get("/api/service-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service, times(1)).getAll(null);
    }

    @Test
    void getStats_Success() throws Exception {
        DashboardStats stats = DashboardStats.builder()
                .totalRequests(100)
                .pendingRequests(20)
                .build();
        when(service.getStats()).thenReturn(stats);

        mockMvc.perform(get("/api/service-requests/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRequests").value(100));

        verify(service, times(1)).getStats();
    }

    @Test
    void getAvailableBays_Success() throws Exception {
        when(service.getAvailableBays()).thenReturn(List.of(1, 2, 3));

        mockMvc.perform(get("/api/service-requests/bays/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        verify(service, times(1)).getAvailableBays();
    }

    @Test
    void cancel_Success() throws Exception {
        when(service.cancelRequest(1)).thenReturn(testResponse);

        mockMvc.perform(put("/api/service-requests/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service, times(1)).cancelRequest(1);
    }

    @Test
    void getByVehicle_Success() throws Exception {
        when(service.getByVehicleId(1)).thenReturn(List.of(testResponse));

        mockMvc.perform(get("/api/service-requests/vehicle/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service, times(1)).getByVehicleId(1);
    }

    @Test
    void getByTechnician_Success() throws Exception {
        when(service.getByTechnicianId(5, null)).thenReturn(List.of(testResponse));

        mockMvc.perform(get("/api/service-requests/technician/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service, times(1)).getByTechnicianId(5, null);
    }

    @Test
    void assign_Success() throws Exception {
        com.vsms.servicerequestservice.dto.request.AssignTechnicianDTO dto = new com.vsms.servicerequestservice.dto.request.AssignTechnicianDTO();
        dto.setTechnicianId(5);
        dto.setBayNumber(3);

        when(service.assignTechnician(eq(1), any())).thenReturn(testResponse);

        mockMvc.perform(put("/api/service-requests/1/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(service, times(1)).assignTechnician(eq(1), any());
    }

    @Test
    void updateStatus_Success() throws Exception {
        com.vsms.servicerequestservice.dto.request.StatusUpdateDTO dto = new com.vsms.servicerequestservice.dto.request.StatusUpdateDTO();
        dto.setStatus(RequestStatus.IN_PROGRESS);

        when(service.updateStatus(eq(1), any())).thenReturn(testResponse);

        mockMvc.perform(put("/api/service-requests/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(service, times(1)).updateStatus(eq(1), any());
    }

    @Test
    void setPricing_Success() throws Exception {
        com.vsms.servicerequestservice.dto.request.CompleteWorkDTO dto = new com.vsms.servicerequestservice.dto.request.CompleteWorkDTO();
        dto.setPartsCost(200f);
        dto.setLaborCost(300f);

        when(service.setPricing(eq(1), any())).thenReturn(testResponse);

        mockMvc.perform(put("/api/service-requests/1/set-pricing")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(service, times(1)).setPricing(eq(1), any());
    }

    @Test
    void getAllBays_Success() throws Exception {
        var bayStatus = com.vsms.servicerequestservice.dto.response.BayStatus.builder()
                .bayNumber(1).occupied(true).build();
        when(service.getAllBayStatus()).thenReturn(List.of(bayStatus));

        mockMvc.perform(get("/api/service-requests/bays"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        verify(service, times(1)).getAllBayStatus();
    }

    @Test
    void getPartsCost_Success() throws Exception {
        when(service.getPartsCostFromInventory(1)).thenReturn(java.math.BigDecimal.valueOf(150));

        mockMvc.perform(get("/api/service-requests/1/parts-cost"))
                .andExpect(status().isOk());

        verify(service, times(1)).getPartsCostFromInventory(1);
    }
}
