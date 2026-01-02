package com.vsms.inventoryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsms.inventoryservice.dto.request.PartCreateDTO;
import com.vsms.inventoryservice.dto.response.PartResponse;
import com.vsms.inventoryservice.enums.PartCategory;
import com.vsms.inventoryservice.service.PartService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PartControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private PartService partService;

    @InjectMocks
    private PartController partController;

    private PartResponse testPartResponse;
    private PartCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(partController).build();
        objectMapper = new ObjectMapper();

        testPartResponse = PartResponse.builder()
                .id(1)
                .partNumber("ENG-001")
                .name("Oil Filter")
                .category(PartCategory.FILTERS)
                .quantity(50)
                .unitPrice(BigDecimal.valueOf(25.99))
                .lowStock(false)
                .build();

        createDTO = new PartCreateDTO();
        createDTO.setPartNumber("ENG-001");
        createDTO.setName("Oil Filter");
        createDTO.setCategory(PartCategory.FILTERS);
        createDTO.setQuantity(50);
        createDTO.setUnitPrice(BigDecimal.valueOf(25.99));
        createDTO.setReorderLevel(10);
    }

    @Test
    void createPart_Success() throws Exception {
        when(partService.createPart(any(PartCreateDTO.class))).thenReturn(testPartResponse);

        mockMvc.perform(post("/api/parts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(partService, times(1)).createPart(any(PartCreateDTO.class));
    }

    @Test
    void getAllParts_NoCategory_Success() throws Exception {
        when(partService.getAllParts(null)).thenReturn(List.of(testPartResponse));

        mockMvc.perform(get("/api/parts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].partNumber").value("ENG-001"));

        verify(partService, times(1)).getAllParts(null);
    }

    @Test
    void getAllParts_WithCategory_Success() throws Exception {
        when(partService.getAllParts(PartCategory.FILTERS)).thenReturn(List.of(testPartResponse));

        mockMvc.perform(get("/api/parts")
                .param("category", "FILTERS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        verify(partService, times(1)).getAllParts(PartCategory.FILTERS);
    }

    @Test
    void getLowStockParts_Success() throws Exception {
        testPartResponse.setLowStock(true);
        when(partService.getLowStockParts()).thenReturn(List.of(testPartResponse));

        mockMvc.perform(get("/api/parts/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].lowStock").value(true));

        verify(partService, times(1)).getLowStockParts();
    }
}
