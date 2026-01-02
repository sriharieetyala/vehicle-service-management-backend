package com.vsms.inventoryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsms.inventoryservice.dto.request.PartRequestCreateDTO;
import com.vsms.inventoryservice.dto.response.PartRequestResponse;
import com.vsms.inventoryservice.enums.RequestStatus;
import com.vsms.inventoryservice.service.PartRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PartRequestControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private PartRequestService partRequestService;

    @InjectMocks
    private PartRequestController partRequestController;

    private PartRequestResponse testResponse;
    private PartRequestCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(partRequestController).build();
        objectMapper = new ObjectMapper();

        testResponse = PartRequestResponse.builder()
                .id(1)
                .partId(1)
                .partNumber("ENG-001")
                .partName("Oil Filter")
                .serviceRequestId(100)
                .technicianId(5)
                .requestedQuantity(10)
                .status(RequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        createDTO = new PartRequestCreateDTO();
        createDTO.setPartId(1);
        createDTO.setServiceRequestId(100);
        createDTO.setTechnicianId(5);
        createDTO.setRequestedQuantity(10);
    }

    @Test
    void createRequest_Success() throws Exception {
        when(partRequestService.createRequest(any(PartRequestCreateDTO.class))).thenReturn(testResponse);

        mockMvc.perform(post("/api/part-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(partRequestService, times(1)).createRequest(any(PartRequestCreateDTO.class));
    }

    @Test
    void getPendingRequests_Success() throws Exception {
        when(partRequestService.getPendingRequests()).thenReturn(List.of(testResponse));

        mockMvc.perform(get("/api/part-requests/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));

        verify(partRequestService, times(1)).getPendingRequests();
    }

    @Test
    void approveRequest_Success() throws Exception {
        testResponse.setStatus(RequestStatus.APPROVED);
        when(partRequestService.approveRequest(eq(1), any())).thenReturn(testResponse);

        mockMvc.perform(put("/api/part-requests/1/approve")
                .param("approvedBy", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        verify(partRequestService, times(1)).approveRequest(eq(1), eq(10));
    }

    @Test
    void approveRequest_NoApprovedBy_Success() throws Exception {
        testResponse.setStatus(RequestStatus.APPROVED);
        when(partRequestService.approveRequest(eq(1), isNull())).thenReturn(testResponse);

        mockMvc.perform(put("/api/part-requests/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        verify(partRequestService, times(1)).approveRequest(eq(1), isNull());
    }

    @Test
    void rejectRequest_Success() throws Exception {
        testResponse.setStatus(RequestStatus.REJECTED);
        when(partRequestService.rejectRequest(eq(1), any(), any())).thenReturn(testResponse);

        mockMvc.perform(put("/api/part-requests/1/reject")
                .param("rejectedBy", "10")
                .param("reason", "Out of stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));

        verify(partRequestService, times(1)).rejectRequest(eq(1), eq(10), eq("Out of stock"));
    }

    @Test
    void getTotalCostForService_Success() throws Exception {
        when(partRequestService.getTotalCostForService(100)).thenReturn(BigDecimal.valueOf(259.90));

        mockMvc.perform(get("/api/part-requests/service/100/total-cost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(259.90));

        verify(partRequestService, times(1)).getTotalCostForService(100);
    }
}
