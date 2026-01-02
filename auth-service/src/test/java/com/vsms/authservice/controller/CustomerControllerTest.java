package com.vsms.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsms.authservice.dto.request.CustomerCreateRequest;
import com.vsms.authservice.dto.request.CustomerUpdateRequest;
import com.vsms.authservice.dto.response.CustomerResponse;
import com.vsms.authservice.enums.UserStatus;
import com.vsms.authservice.service.CustomerService;
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
class CustomerControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private CustomerResponse testResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(customerController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        testResponse = CustomerResponse.builder()
                .id(1)
                .userId(1)
                .email("customer@test.com")
                .phone("1234567890")
                .firstName("John")
                .lastName("Doe")
                .address("123 Main St")
                .city("TestCity")
                .state("TS")
                .zipCode("12345")
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createCustomer_Success() throws Exception {
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setEmail("customer@test.com");
        request.setPassword("password123");
        request.setPhone("1234567890");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setAddress("123 Main St");
        request.setCity("TestCity");
        request.setState("TS");
        request.setZipCode("12345");

        when(customerService.createCustomer(any(CustomerCreateRequest.class))).thenReturn(testResponse);

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(customerService, times(1)).createCustomer(any(CustomerCreateRequest.class));
    }

    @Test
    void getAllCustomers_Success() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(List.of(testResponse));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1));

        verify(customerService, times(1)).getAllCustomers();
    }

    @Test
    void getCustomerById_Success() throws Exception {
        when(customerService.getCustomerById(1)).thenReturn(testResponse);

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.firstName").value("John"));

        verify(customerService, times(1)).getCustomerById(1);
    }

    @Test
    void updateCustomer_Success() throws Exception {
        CustomerUpdateRequest request = new CustomerUpdateRequest();
        request.setFirstName("UpdatedName");
        request.setPhone("9876543210");

        when(customerService.updateCustomer(eq(1), any(CustomerUpdateRequest.class))).thenReturn(testResponse);

        mockMvc.perform(put("/api/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(customerService, times(1)).updateCustomer(eq(1), any(CustomerUpdateRequest.class));
    }

    @Test
    void deleteCustomer_Success() throws Exception {
        doNothing().when(customerService).deleteCustomer(1);

        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isOk());

        verify(customerService, times(1)).deleteCustomer(1);
    }
}
