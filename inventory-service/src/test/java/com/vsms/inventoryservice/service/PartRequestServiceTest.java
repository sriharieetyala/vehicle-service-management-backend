package com.vsms.inventoryservice.service;

import com.vsms.inventoryservice.dto.request.PartRequestCreateDTO;
import com.vsms.inventoryservice.dto.response.PartRequestResponse;
import com.vsms.inventoryservice.entity.Part;
import com.vsms.inventoryservice.entity.PartRequest;
import com.vsms.inventoryservice.enums.PartCategory;
import com.vsms.inventoryservice.enums.RequestStatus;
import com.vsms.inventoryservice.exception.BadRequestException;
import com.vsms.inventoryservice.exception.InsufficientStockException;
import com.vsms.inventoryservice.exception.ResourceNotFoundException;
import com.vsms.inventoryservice.repository.PartRequestRepository;
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
class PartRequestServiceTest {

    @Mock
    private PartRequestRepository repository;

    @Mock
    private PartService partService;

    @InjectMocks
    private PartRequestService partRequestService;

    private Part testPart;
    private PartRequest testRequest;
    private PartRequestCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        testPart = Part.builder()
                .id(1)
                .partNumber("ENG-001")
                .name("Oil Filter")
                .category(PartCategory.FILTERS)
                .quantity(50)
                .unitPrice(BigDecimal.valueOf(25.99))
                .reorderLevel(10)
                .build();

        testRequest = PartRequest.builder()
                .id(1)
                .partId(1)
                .serviceRequestId(100)
                .technicianId(5)
                .requestedQuantity(10)
                .status(RequestStatus.PENDING)
                .notes("Need for oil change")
                .createdAt(LocalDateTime.now())
                .build();

        createDTO = new PartRequestCreateDTO();
        createDTO.setPartId(1);
        createDTO.setServiceRequestId(100);
        createDTO.setTechnicianId(5);
        createDTO.setRequestedQuantity(10);
        createDTO.setNotes("Need for oil change");
    }

    @Test
    void createRequest_Success() {
        when(partService.findById(1)).thenReturn(testPart);
        when(repository.save(any(PartRequest.class))).thenReturn(testRequest);

        PartRequestResponse response = partRequestService.createRequest(createDTO);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals(100, response.getServiceRequestId());
        verify(repository, times(1)).save(any(PartRequest.class));
    }

    // Note: Stock check during create was removed - technicians can request any
    // quantity
    // Inventory manager approves/rejects based on availability

    @Test
    void createRequest_PartNotFound_ThrowsException() {
        when(partService.findById(1)).thenThrow(new ResourceNotFoundException("Part", "id", 1));

        assertThrows(ResourceNotFoundException.class, () -> partRequestService.createRequest(createDTO));
    }

    @Test
    void getPendingRequests_ReturnsRequests() {
        when(repository.findByStatus(RequestStatus.PENDING)).thenReturn(List.of(testRequest));
        when(partService.findById(1)).thenReturn(testPart);

        List<PartRequestResponse> responses = partRequestService.getPendingRequests();

        assertEquals(1, responses.size());
        assertEquals(RequestStatus.PENDING, responses.get(0).getStatus());
    }

    @Test
    void approveRequest_Success() {
        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(partService.findById(1)).thenReturn(testPart);
        doNothing().when(partService).reduceStock(1, 10);
        when(repository.save(any(PartRequest.class))).thenReturn(testRequest);

        PartRequestResponse response = partRequestService.approveRequest(1, 10);

        assertNotNull(response);
        assertEquals(RequestStatus.APPROVED, testRequest.getStatus());
        verify(partService, times(1)).reduceStock(1, 10);
    }

    @Test
    void approveRequest_NotPending_ThrowsException() {
        testRequest.setStatus(RequestStatus.APPROVED);
        when(repository.findById(1)).thenReturn(Optional.of(testRequest));

        assertThrows(BadRequestException.class, () -> partRequestService.approveRequest(1, 10));
    }

    @Test
    void approveRequest_InsufficientStock_ThrowsException() {
        testPart.setQuantity(5); // Less than requested 10
        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(partService.findById(1)).thenReturn(testPart);

        assertThrows(InsufficientStockException.class, () -> partRequestService.approveRequest(1, 10));
    }

    @Test
    void approveRequest_NotFound_ThrowsException() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> partRequestService.approveRequest(999, 10));
    }

    @Test
    void rejectRequest_Success() {
        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(repository.save(any(PartRequest.class))).thenReturn(testRequest);
        when(partService.findById(1)).thenReturn(testPart);

        PartRequestResponse response = partRequestService.rejectRequest(1, 10, "Out of stock");

        assertNotNull(response);
        assertEquals(RequestStatus.REJECTED, testRequest.getStatus());
        assertTrue(testRequest.getNotes().contains("Rejected: Out of stock"));
    }

    @Test
    void rejectRequest_NoReason_Success() {
        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(repository.save(any(PartRequest.class))).thenReturn(testRequest);
        when(partService.findById(1)).thenReturn(testPart);

        PartRequestResponse response = partRequestService.rejectRequest(1, 10, null);

        assertNotNull(response);
        assertEquals(RequestStatus.REJECTED, testRequest.getStatus());
    }

    @Test
    void rejectRequest_NotPending_ThrowsException() {
        testRequest.setStatus(RequestStatus.REJECTED);
        when(repository.findById(1)).thenReturn(Optional.of(testRequest));

        assertThrows(BadRequestException.class, () -> partRequestService.rejectRequest(1, 10, "reason"));
    }

    @Test
    void rejectRequest_BlankReason_DoesNotAppend() {
        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(repository.save(any(PartRequest.class))).thenReturn(testRequest);
        when(partService.findById(1)).thenReturn(testPart);

        partRequestService.rejectRequest(1, 10, "   ");

        assertFalse(testRequest.getNotes().contains("Rejected:"));
    }

    @Test
    void rejectRequest_NullNotes_SetsRejectionReason() {
        testRequest.setNotes(null);
        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(repository.save(any(PartRequest.class))).thenReturn(testRequest);
        when(partService.findById(1)).thenReturn(testPart);

        partRequestService.rejectRequest(1, 10, "Not needed");

        assertEquals("Rejected: Not needed", testRequest.getNotes());
    }

    @Test
    void getTotalCostForService_ReturnsCalculatedCost() {
        testRequest.setStatus(RequestStatus.APPROVED);
        when(repository.findByServiceRequestId(100)).thenReturn(List.of(testRequest));
        when(partService.findById(1)).thenReturn(testPart);

        BigDecimal totalCost = partRequestService.getTotalCostForService(100);

        // 10 units * 25.99 = 259.90
        assertEquals(new BigDecimal("259.90"), totalCost);
    }

    @Test
    void getTotalCostForService_NoApprovedRequests_ReturnsZero() {
        testRequest.setStatus(RequestStatus.PENDING); // Not approved
        when(repository.findByServiceRequestId(100)).thenReturn(List.of(testRequest));

        BigDecimal totalCost = partRequestService.getTotalCostForService(100);

        assertEquals(BigDecimal.ZERO, totalCost);
    }

    @Test
    void getTotalCostForService_MultipleApproved_SumsCorrectly() {
        PartRequest request2 = PartRequest.builder()
                .id(2)
                .partId(1)
                .serviceRequestId(100)
                .requestedQuantity(5)
                .status(RequestStatus.APPROVED)
                .build();

        testRequest.setStatus(RequestStatus.APPROVED);

        when(repository.findByServiceRequestId(100)).thenReturn(List.of(testRequest, request2));
        when(partService.findById(1)).thenReturn(testPart);

        BigDecimal totalCost = partRequestService.getTotalCostForService(100);

        // (10 + 5) * 25.99 = 389.85
        assertEquals(new BigDecimal("389.85"), totalCost);
    }
}
