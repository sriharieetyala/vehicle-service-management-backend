package com.vsms.inventoryservice.service;

import com.vsms.inventoryservice.dto.request.PartCreateDTO;
import com.vsms.inventoryservice.dto.request.PartUpdateDTO;
import com.vsms.inventoryservice.dto.response.PartResponse;
import com.vsms.inventoryservice.entity.Part;
import com.vsms.inventoryservice.enums.PartCategory;
import com.vsms.inventoryservice.exception.BadRequestException;
import com.vsms.inventoryservice.exception.ResourceNotFoundException;
import com.vsms.inventoryservice.repository.PartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartServiceTest {

    @Mock
    private PartRepository repository;

    @InjectMocks
    private PartService partService;

    private Part testPart;
    private PartCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        testPart = Part.builder()
                .id(1)
                .partNumber("ENG-001")
                .name("Oil Filter")
                .description("Engine oil filter")
                .category(PartCategory.FILTERS)
                .quantity(50)
                .unitPrice(BigDecimal.valueOf(25.99))
                .reorderLevel(10)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createDTO = new PartCreateDTO();
        createDTO.setPartNumber("ENG-001");
        createDTO.setName("Oil Filter");
        createDTO.setDescription("Engine oil filter");
        createDTO.setCategory(PartCategory.FILTERS);
        createDTO.setQuantity(50);
        createDTO.setUnitPrice(BigDecimal.valueOf(25.99));
        createDTO.setReorderLevel(10);
    }

    @Test
    void createPart_Success() {
        when(repository.existsByPartNumber("ENG-001")).thenReturn(false);
        when(repository.save(any(Part.class))).thenReturn(testPart);

        PartResponse response = partService.createPart(createDTO);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("ENG-001", response.getPartNumber());
        assertEquals("Oil Filter", response.getName());
        verify(repository, times(1)).save(any(Part.class));
    }

    @Test
    void createPart_DuplicatePartNumber_ThrowsException() {
        when(repository.existsByPartNumber("ENG-001")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> partService.createPart(createDTO));
        verify(repository, never()).save(any(Part.class));
    }

    @Test
    void getAllParts_NoCategory_ReturnsAll() {
        when(repository.findAll()).thenReturn(List.of(testPart));

        List<PartResponse> responses = partService.getAllParts(null);

        assertEquals(1, responses.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void getAllParts_WithCategory_ReturnsFiltered() {
        when(repository.findByCategory(PartCategory.FILTERS)).thenReturn(List.of(testPart));

        List<PartResponse> responses = partService.getAllParts(PartCategory.FILTERS);

        assertEquals(1, responses.size());
        verify(repository, times(1)).findByCategory(PartCategory.FILTERS);
    }

    @Test
    void getPartById_Exists_ReturnsResponse() {
        when(repository.findById(1)).thenReturn(Optional.of(testPart));

        PartResponse response = partService.getPartById(1);

        assertNotNull(response);
        assertEquals(1, response.getId());
    }

    @Test
    void getPartById_NotFound_ThrowsException() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> partService.getPartById(999));
    }

    @Test
    void updatePart_AllFields_Success() {
        PartUpdateDTO updateDTO = new PartUpdateDTO();
        updateDTO.setName("Updated Filter");
        updateDTO.setDescription("Updated description");
        updateDTO.setCategory(PartCategory.ENGINE);
        updateDTO.setQuantity(100);
        updateDTO.setUnitPrice(BigDecimal.valueOf(30.00));
        updateDTO.setReorderLevel(20);

        when(repository.findById(1)).thenReturn(Optional.of(testPart));
        when(repository.save(any(Part.class))).thenReturn(testPart);

        PartResponse response = partService.updatePart(1, updateDTO);

        assertNotNull(response);
        verify(repository, times(1)).save(testPart);
    }

    @Test
    void updatePart_PartialUpdate_Success() {
        PartUpdateDTO updateDTO = new PartUpdateDTO();
        updateDTO.setName("Only Name Updated");

        when(repository.findById(1)).thenReturn(Optional.of(testPart));
        when(repository.save(any(Part.class))).thenReturn(testPart);

        PartResponse response = partService.updatePart(1, updateDTO);

        assertNotNull(response);
        verify(repository, times(1)).save(testPart);
    }

    @Test
    void updatePart_NotFound_ThrowsException() {
        PartUpdateDTO updateDTO = new PartUpdateDTO();
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> partService.updatePart(999, updateDTO));
    }

    @Test
    void getLowStockParts_ReturnsLowStock() {
        testPart.setQuantity(5); // Below reorder level of 10
        when(repository.findLowStockParts()).thenReturn(List.of(testPart));

        List<PartResponse> responses = partService.getLowStockParts();

        assertEquals(1, responses.size());
        assertTrue(responses.get(0).isLowStock());
    }

    @Test
    void reduceStock_SufficientStock_Success() {
        when(repository.findById(1)).thenReturn(Optional.of(testPart));
        when(repository.save(any(Part.class))).thenReturn(testPart);

        partService.reduceStock(1, 10);

        assertEquals(40, testPart.getQuantity());
        verify(repository, times(1)).save(testPart);
    }

    @Test
    void reduceStock_InsufficientStock_ThrowsException() {
        when(repository.findById(1)).thenReturn(Optional.of(testPart));

        assertThrows(BadRequestException.class, () -> partService.reduceStock(1, 100));
    }

    @Test
    void reduceStock_PartNotFound_ThrowsException() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> partService.reduceStock(999, 10));
    }

    @Test
    void mapToResponse_SetsLowStockTrue_WhenBelowReorderLevel() {
        testPart.setQuantity(5); // Below reorder level of 10
        when(repository.findById(1)).thenReturn(Optional.of(testPart));

        PartResponse response = partService.getPartById(1);

        assertTrue(response.isLowStock());
    }

    @Test
    void mapToResponse_SetsLowStockFalse_WhenAboveReorderLevel() {
        testPart.setQuantity(50); // Above reorder level of 10
        when(repository.findById(1)).thenReturn(Optional.of(testPart));

        PartResponse response = partService.getPartById(1);

        assertFalse(response.isLowStock());
    }
}
