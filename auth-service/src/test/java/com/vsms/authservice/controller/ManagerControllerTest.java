package com.vsms.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsms.authservice.dto.request.ManagerCreateRequest;
import com.vsms.authservice.dto.request.ManagerUpdateRequest;
import com.vsms.authservice.dto.response.ManagerResponse;
import com.vsms.authservice.enums.Department;
import com.vsms.authservice.enums.UserStatus;
import com.vsms.authservice.service.ManagerService;
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
class ManagerControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ManagerService managerService;

    @InjectMocks
    private ManagerController managerController;

    private ManagerResponse testResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(managerController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        testResponse = ManagerResponse.builder()
                .id(1)
                .userId(1)
                .email("manager@test.com")
                .phone("1234567890")
                .firstName("Manager")
                .lastName("User")
                .employeeId("MGR-001")
                .department(Department.SERVICE_BAY)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createManager_Success() throws Exception {
        ManagerCreateRequest request = new ManagerCreateRequest();
        request.setEmail("manager@test.com");
        request.setPassword("password123");
        request.setPhone("1234567890");
        request.setFirstName("Manager");
        request.setLastName("User");
        request.setEmployeeId("MGR-001");
        request.setDepartment(Department.SERVICE_BAY);

        when(managerService.createManager(any(ManagerCreateRequest.class))).thenReturn(testResponse);

        mockMvc.perform(post("/api/managers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(managerService, times(1)).createManager(any(ManagerCreateRequest.class));
    }

    @Test
    void getAllManagers_NoDepartmentFilter_Success() throws Exception {
        when(managerService.getAllManagers(null)).thenReturn(List.of(testResponse));

        mockMvc.perform(get("/api/managers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1));

        verify(managerService, times(1)).getAllManagers(null);
    }

    @Test
    void getAllManagers_WithDepartmentFilter_Success() throws Exception {
        when(managerService.getAllManagers(Department.SERVICE_BAY)).thenReturn(List.of(testResponse));

        mockMvc.perform(get("/api/managers")
                .param("department", "SERVICE_BAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(managerService, times(1)).getAllManagers(Department.SERVICE_BAY);
    }

    @Test
    void updateManager_Success() throws Exception {
        ManagerUpdateRequest request = new ManagerUpdateRequest();
        request.setFirstName("UpdatedName");
        request.setPhone("9876543210");

        when(managerService.updateManager(eq(1), any(ManagerUpdateRequest.class))).thenReturn(testResponse);

        mockMvc.perform(put("/api/managers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(managerService, times(1)).updateManager(eq(1), any(ManagerUpdateRequest.class));
    }

    @Test
    void deleteManager_Success() throws Exception {
        doNothing().when(managerService).deleteManager(1);

        mockMvc.perform(delete("/api/managers/1"))
                .andExpect(status().isOk());

        verify(managerService, times(1)).deleteManager(1);
    }
}
