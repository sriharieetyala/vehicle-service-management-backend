package com.vsms.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsms.authservice.dto.request.TechnicianCreateRequest;
import com.vsms.authservice.dto.response.TechnicianResponse;
import com.vsms.authservice.enums.Specialization;
import com.vsms.authservice.enums.UserStatus;
import com.vsms.authservice.service.TechnicianService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TechnicianControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TechnicianService technicianService;

    @InjectMocks
    private TechnicianController technicianController;

    private TechnicianResponse testResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(technicianController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        testResponse = TechnicianResponse.builder()
                .id(1)
                .userId(1)
                .email("tech@test.com")
                .phone("1234567890")
                .firstName("Tech")
                .lastName("User")
                .employeeId("TECH-00001")
                .specialization(Specialization.ENGINE)
                .experienceYears(5)
                .onDuty(true)
                .currentWorkload(2)
                .maxCapacity(5)
                .rating(new java.math.BigDecimal("4.5"))
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createTechnician_Success() throws Exception {
        TechnicianCreateRequest request = new TechnicianCreateRequest();
        request.setEmail("tech@test.com");
        request.setPassword("password123");
        request.setPhone("1234567890");
        request.setFirstName("Tech");
        request.setLastName("User");
        request.setSpecialization(Specialization.ENGINE);
        request.setExperienceYears(5);

        when(technicianService.createTechnician(any(TechnicianCreateRequest.class))).thenReturn(testResponse);

        mockMvc.perform(post("/api/technicians")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(technicianService, times(1)).createTechnician(any(TechnicianCreateRequest.class));
    }

    @Test
    void getAllTechnicians_Success() throws Exception {
        when(technicianService.getAllTechnicians()).thenReturn(List.of(testResponse));

        mockMvc.perform(get("/api/technicians"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1));

        verify(technicianService, times(1)).getAllTechnicians();
    }

    @Test
    void getTechnicianById_Success() throws Exception {
        when(technicianService.getTechnicianById(1)).thenReturn(testResponse);

        mockMvc.perform(get("/api/technicians/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(technicianService, times(1)).getTechnicianById(1);
    }

    @Test
    void getAvailableTechnicians_Success() throws Exception {
        when(technicianService.getAvailableTechnicians()).thenReturn(List.of(testResponse));

        mockMvc.perform(get("/api/technicians/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(technicianService, times(1)).getAvailableTechnicians();
    }

    @Test
    void getBySpecialization_Success() throws Exception {
        when(technicianService.getAvailableBySpecialization(Specialization.ENGINE)).thenReturn(List.of(testResponse));

        mockMvc.perform(get("/api/technicians/by-specialization")
                .param("spec", "ENGINE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(technicianService, times(1)).getAvailableBySpecialization(Specialization.ENGINE);
    }

    @Test
    void getPendingTechnicians_Success() throws Exception {
        when(technicianService.getPendingTechnicians()).thenReturn(List.of(testResponse));

        mockMvc.perform(get("/api/technicians/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(technicianService, times(1)).getPendingTechnicians();
    }

    @Test
    void reviewTechnician_Approve_Success() throws Exception {
        when(technicianService.approveTechnician(1)).thenReturn(testResponse);

        mockMvc.perform(put("/api/technicians/1/review")
                .param("action", "APPROVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Technician approved"));

        verify(technicianService, times(1)).approveTechnician(1);
    }

    @Test
    void reviewTechnician_Reject_Success() throws Exception {
        doNothing().when(technicianService).rejectTechnician(1);

        mockMvc.perform(put("/api/technicians/1/review")
                .param("action", "REJECT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Technician rejected"));

        verify(technicianService, times(1)).rejectTechnician(1);
    }

    @Test
    void toggleDutyStatus_Success() throws Exception {
        when(technicianService.toggleDutyStatus(1)).thenReturn(testResponse);

        mockMvc.perform(put("/api/technicians/1/duty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Duty status toggled"));

        verify(technicianService, times(1)).toggleDutyStatus(1);
    }

    @Test
    void updateWorkload_Increment_Success() throws Exception {
        doNothing().when(technicianService).incrementWorkload(1);

        mockMvc.perform(put("/api/technicians/1/workload")
                .param("action", "INCREMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Workload incremented"));

        verify(technicianService, times(1)).incrementWorkload(1);
    }

    @Test
    void updateWorkload_Decrement_Success() throws Exception {
        doNothing().when(technicianService).decrementWorkload(1);

        mockMvc.perform(put("/api/technicians/1/workload")
                .param("action", "DECREMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Workload decremented"));

        verify(technicianService, times(1)).decrementWorkload(1);
    }

    @Test
    void deleteTechnician_Success() throws Exception {
        doNothing().when(technicianService).deleteTechnician(1);

        mockMvc.perform(delete("/api/technicians/1"))
                .andExpect(status().isOk());

        verify(technicianService, times(1)).deleteTechnician(1);
    }
}
