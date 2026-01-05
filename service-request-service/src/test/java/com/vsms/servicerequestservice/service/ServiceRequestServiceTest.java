package com.vsms.servicerequestservice.service;

import com.vsms.servicerequestservice.client.AuthServiceClient;
import com.vsms.servicerequestservice.client.InventoryClient;
import com.vsms.servicerequestservice.client.VehicleClient;
import com.vsms.servicerequestservice.dto.request.AssignTechnicianDTO;
import com.vsms.servicerequestservice.dto.request.ServiceRequestCreateDTO;
import com.vsms.servicerequestservice.dto.request.StatusUpdateDTO;
import com.vsms.servicerequestservice.dto.response.DashboardStats;
import com.vsms.servicerequestservice.dto.response.ServiceRequestResponse;
import com.vsms.servicerequestservice.entity.ServiceRequest;
import com.vsms.servicerequestservice.enums.Priority;
import com.vsms.servicerequestservice.enums.RequestStatus;
import com.vsms.servicerequestservice.enums.ServiceType;
import com.vsms.servicerequestservice.exception.BadRequestException;
import com.vsms.servicerequestservice.exception.ResourceNotFoundException;
import com.vsms.servicerequestservice.repository.ServiceRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceRequestServiceTest {

    @Mock
    private ServiceRequestRepository repository;
    @Mock
    private InvoiceService invoiceService;
    @Mock
    private VehicleClient vehicleClient;
    @Mock
    private InventoryClient inventoryClient;
    @Mock
    private AuthServiceClient authServiceClient;
    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private ServiceRequestService service;

    private ServiceRequest testRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "totalBays", 20);

        testRequest = ServiceRequest.builder()
                .id(1)
                .customerId(1)
                .vehicleId(1)
                .serviceType(ServiceType.REGULAR_SERVICE)
                .description("Oil change")
                .priority(Priority.NORMAL)
                .status(RequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createRequest_Success() {
        ServiceRequestCreateDTO dto = new ServiceRequestCreateDTO();
        dto.setCustomerId(1);
        dto.setVehicleId(1);
        dto.setServiceType(ServiceType.REGULAR_SERVICE);
        dto.setDescription("Oil change");

        Map<String, Object> vehicleData = Map.of("customerId", 1);
        Map<String, Object> vehicleResponse = Map.of("data", vehicleData);

        when(vehicleClient.getVehicleById(1)).thenReturn(vehicleResponse);
        when(repository.save(any(ServiceRequest.class))).thenAnswer(inv -> {
            ServiceRequest sr = inv.getArgument(0);
            sr.setId(1);
            sr.setCreatedAt(LocalDateTime.now());
            return sr;
        });

        ServiceRequestResponse response = service.createRequest(dto);

        assertNotNull(response);
        assertEquals(1, response.getCustomerId());
        verify(repository, times(1)).save(any(ServiceRequest.class));
    }

    @Test
    void createRequest_VehicleNotBelongsToCustomer_ThrowsException() {
        ServiceRequestCreateDTO dto = new ServiceRequestCreateDTO();
        dto.setCustomerId(1);
        dto.setVehicleId(1);
        dto.setServiceType(ServiceType.REGULAR_SERVICE);

        Map<String, Object> vehicleData = Map.of("customerId", 999);
        Map<String, Object> vehicleResponse = Map.of("data", vehicleData);

        when(vehicleClient.getVehicleById(1)).thenReturn(vehicleResponse);

        assertThrows(BadRequestException.class, () -> service.createRequest(dto));
    }

    @Test
    void createRequest_PickupRequiredNoAddress_ThrowsException() {
        ServiceRequestCreateDTO dto = new ServiceRequestCreateDTO();
        dto.setCustomerId(1);
        dto.setVehicleId(1);
        dto.setServiceType(ServiceType.REGULAR_SERVICE);
        dto.setPickupRequired(true);
        dto.setPickupAddress("");

        Map<String, Object> vehicleData = Map.of("customerId", 1);
        Map<String, Object> vehicleResponse = Map.of("data", vehicleData);

        when(vehicleClient.getVehicleById(1)).thenReturn(vehicleResponse);

        assertThrows(BadRequestException.class, () -> service.createRequest(dto));
    }

    @Test
    void getById_Success() {
        when(repository.findById(1)).thenReturn(Optional.of(testRequest));

        ServiceRequestResponse response = service.getById(1);

        assertNotNull(response);
        assertEquals(1, response.getId());
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(999));
    }

    @Test
    void getByCustomerId_Success() {
        when(repository.findByCustomerId(1)).thenReturn(List.of(testRequest));

        List<ServiceRequestResponse> responses = service.getByCustomerId(1);

        assertEquals(1, responses.size());
    }

    @Test
    void assignTechnician_Success() {
        AssignTechnicianDTO dto = new AssignTechnicianDTO();
        dto.setTechnicianId(5);
        dto.setBayNumber(3);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(repository.findOccupiedBays()).thenReturn(List.of(1, 2));
        when(repository.save(any(ServiceRequest.class))).thenReturn(testRequest);

        ServiceRequestResponse response = service.assignTechnician(1, dto);

        assertNotNull(response);
        verify(authServiceClient, times(1)).updateWorkload(5, "INCREMENT");
    }

    @Test
    void assignTechnician_BayOccupied_ThrowsException() {
        testRequest.setStatus(RequestStatus.PENDING);
        AssignTechnicianDTO dto = new AssignTechnicianDTO();
        dto.setTechnicianId(5);
        dto.setBayNumber(1);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(repository.findOccupiedBays()).thenReturn(List.of(1, 2));

        assertThrows(BadRequestException.class, () -> service.assignTechnician(1, dto));
    }

    @Test
    void updateStatus_ToInProgress_Success() {
        testRequest.setStatus(RequestStatus.ASSIGNED);
        StatusUpdateDTO dto = new StatusUpdateDTO();
        dto.setStatus(RequestStatus.IN_PROGRESS);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(repository.save(any(ServiceRequest.class))).thenReturn(testRequest);

        ServiceRequestResponse response = service.updateStatus(1, dto);

        assertNotNull(response);
        assertEquals(RequestStatus.IN_PROGRESS, testRequest.getStatus());
    }

    @Test
    void updateStatus_ToCompleted_Success() {
        testRequest.setStatus(RequestStatus.IN_PROGRESS);
        testRequest.setTechnicianId(5);
        StatusUpdateDTO dto = new StatusUpdateDTO();
        dto.setStatus(RequestStatus.COMPLETED);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(repository.save(any(ServiceRequest.class))).thenReturn(testRequest);

        ServiceRequestResponse response = service.updateStatus(1, dto);

        assertNotNull(response);
        verify(authServiceClient, times(1)).updateWorkload(5, "DECREMENT");
    }

    @Test
    void cancelRequest_Success() {
        testRequest.setStatus(RequestStatus.PENDING);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(repository.save(any(ServiceRequest.class))).thenReturn(testRequest);

        ServiceRequestResponse response = service.cancelRequest(1);

        assertNotNull(response);
        assertEquals(RequestStatus.CANCELLED, testRequest.getStatus());
    }

    @Test
    void cancelRequest_AlreadyCompleted_ThrowsException() {
        testRequest.setStatus(RequestStatus.COMPLETED);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));

        assertThrows(BadRequestException.class, () -> service.cancelRequest(1));
    }

    @Test
    void getStats_ReturnsStats() {
        when(repository.count()).thenReturn(100L);
        when(repository.countByStatus(any())).thenReturn(20L);

        DashboardStats stats = service.getStats();

        assertNotNull(stats);
        assertEquals(100, stats.getTotalRequests());
    }

    @Test
    void getAvailableBays_ReturnsAvailable() {
        when(repository.findOccupiedBays()).thenReturn(List.of(1, 2, 3));

        List<Integer> availableBays = service.getAvailableBays();

        assertFalse(availableBays.contains(1));
        assertFalse(availableBays.contains(2));
        assertTrue(availableBays.contains(4));
    }

    @Test
    void getByVehicleId_Success() {
        when(repository.findByVehicleId(1)).thenReturn(List.of(testRequest));

        List<ServiceRequestResponse> responses = service.getByVehicleId(1);

        assertEquals(1, responses.size());
    }

    @Test
    void getAll_WithStatus_Success() {
        when(repository.findByStatus(RequestStatus.PENDING)).thenReturn(List.of(testRequest));

        List<ServiceRequestResponse> responses = service.getAll(RequestStatus.PENDING);

        assertEquals(1, responses.size());
    }

    @Test
    void getAll_WithoutStatus_Success() {
        when(repository.findAll()).thenReturn(List.of(testRequest));

        List<ServiceRequestResponse> responses = service.getAll(null);

        assertEquals(1, responses.size());
    }

    @Test
    void getPendingRequests_Success() {
        when(repository.findByStatus(RequestStatus.PENDING)).thenReturn(List.of(testRequest));

        List<ServiceRequestResponse> responses = service.getPendingRequests();

        assertEquals(1, responses.size());
    }

    @Test
    void getByTechnicianId_WithStatus_Success() {
        testRequest.setTechnicianId(5);
        when(repository.findByTechnicianIdAndStatus(5, RequestStatus.IN_PROGRESS)).thenReturn(List.of(testRequest));

        List<ServiceRequestResponse> responses = service.getByTechnicianId(5, RequestStatus.IN_PROGRESS);

        assertEquals(1, responses.size());
    }

    @Test
    void getByTechnicianId_WithoutStatus_Success() {
        testRequest.setTechnicianId(5);
        when(repository.findByTechnicianId(5)).thenReturn(List.of(testRequest));

        List<ServiceRequestResponse> responses = service.getByTechnicianId(5, null);

        assertEquals(1, responses.size());
    }

    @Test
    void assignTechnician_NotPending_ThrowsException() {
        testRequest.setStatus(RequestStatus.IN_PROGRESS);
        AssignTechnicianDTO dto = new AssignTechnicianDTO();
        dto.setTechnicianId(5);
        dto.setBayNumber(3);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));

        assertThrows(BadRequestException.class, () -> service.assignTechnician(1, dto));
    }

    @Test
    void assignTechnician_InvalidBayNumber_ThrowsException() {
        testRequest.setStatus(RequestStatus.PENDING);
        AssignTechnicianDTO dto = new AssignTechnicianDTO();
        dto.setTechnicianId(5);
        dto.setBayNumber(25); // > totalBays (20)

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));

        assertThrows(BadRequestException.class, () -> service.assignTechnician(1, dto));
    }

    @Test
    void updateStatus_ToClosed_Success() {
        testRequest.setStatus(RequestStatus.COMPLETED);
        StatusUpdateDTO dto = new StatusUpdateDTO();
        dto.setStatus(RequestStatus.CLOSED);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(repository.save(any(ServiceRequest.class))).thenReturn(testRequest);

        ServiceRequestResponse response = service.updateStatus(1, dto);

        assertNotNull(response);
        assertEquals(RequestStatus.CLOSED, testRequest.getStatus());
    }

    @Test
    void updateStatus_ToInProgressFromWrongStatus_ThrowsException() {
        testRequest.setStatus(RequestStatus.PENDING);
        StatusUpdateDTO dto = new StatusUpdateDTO();
        dto.setStatus(RequestStatus.IN_PROGRESS);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));

        assertThrows(BadRequestException.class, () -> service.updateStatus(1, dto));
    }

    @Test
    void updateStatus_ToCompletedFromWrongStatus_ThrowsException() {
        testRequest.setStatus(RequestStatus.PENDING);
        StatusUpdateDTO dto = new StatusUpdateDTO();
        dto.setStatus(RequestStatus.COMPLETED);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));

        assertThrows(BadRequestException.class, () -> service.updateStatus(1, dto));
    }

    @Test
    void updateStatus_ToClosedFromWrongStatus_ThrowsException() {
        testRequest.setStatus(RequestStatus.IN_PROGRESS);
        StatusUpdateDTO dto = new StatusUpdateDTO();
        dto.setStatus(RequestStatus.CLOSED);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));

        assertThrows(BadRequestException.class, () -> service.updateStatus(1, dto));
    }

    @Test
    void updateStatus_InvalidStatus_ThrowsException() {
        testRequest.setStatus(RequestStatus.PENDING);
        StatusUpdateDTO dto = new StatusUpdateDTO();
        dto.setStatus(RequestStatus.PENDING);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));

        assertThrows(BadRequestException.class, () -> service.updateStatus(1, dto));
    }

    @Test
    void setPricing_Success() {
        testRequest.setStatus(RequestStatus.COMPLETED);
        testRequest.setFinalCost(null);
        com.vsms.servicerequestservice.dto.request.CompleteWorkDTO dto = new com.vsms.servicerequestservice.dto.request.CompleteWorkDTO();
        dto.setPartsCost(200f);
        dto.setLaborCost(300f);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(repository.save(any(ServiceRequest.class))).thenReturn(testRequest);

        ServiceRequestResponse response = service.setPricing(1, dto);

        assertNotNull(response);
        assertEquals(500f, testRequest.getFinalCost());
    }

    @Test
    void setPricing_NotCompleted_ThrowsException() {
        testRequest.setStatus(RequestStatus.IN_PROGRESS);
        com.vsms.servicerequestservice.dto.request.CompleteWorkDTO dto = new com.vsms.servicerequestservice.dto.request.CompleteWorkDTO();

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));

        assertThrows(BadRequestException.class, () -> service.setPricing(1, dto));
    }

    @Test
    void setPricing_AlreadySet_ThrowsException() {
        testRequest.setStatus(RequestStatus.COMPLETED);
        testRequest.setFinalCost(500f);
        com.vsms.servicerequestservice.dto.request.CompleteWorkDTO dto = new com.vsms.servicerequestservice.dto.request.CompleteWorkDTO();

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));

        assertThrows(BadRequestException.class, () -> service.setPricing(1, dto));
    }

    @Test
    void closeRequest_Success() {
        testRequest.setStatus(RequestStatus.COMPLETED);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(repository.save(any(ServiceRequest.class))).thenReturn(testRequest);

        ServiceRequestResponse response = service.closeRequest(1);

        assertNotNull(response);
        assertEquals(RequestStatus.CLOSED, testRequest.getStatus());
    }

    @Test
    void closeRequest_NotCompleted_ThrowsException() {
        testRequest.setStatus(RequestStatus.IN_PROGRESS);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));

        assertThrows(BadRequestException.class, () -> service.closeRequest(1));
    }

    @Test
    void getCount_WithStatus_ReturnsCount() {
        when(repository.countByStatus(RequestStatus.PENDING)).thenReturn(25L);

        Integer count = service.getCount(RequestStatus.PENDING);

        assertEquals(25, count);
    }

    @Test
    void getCount_WithoutStatus_ReturnsTotal() {
        when(repository.count()).thenReturn(100L);

        Integer count = service.getCount(null);

        assertEquals(100, count);
    }

    @Test
    void getTotalBays_ReturnsTotal() {
        int total = service.getTotalBays();

        assertEquals(20, total);
    }

    @Test
    void getAllBayStatus_ReturnsAllBays() {
        when(repository.findOccupiedBays()).thenReturn(List.of(1, 2));
        when(repository.findByBayNumberAndStatusIn(eq(1), anyList())).thenReturn(Optional.of(testRequest));
        when(repository.findByBayNumberAndStatusIn(eq(2), anyList())).thenReturn(Optional.of(testRequest));

        var bayStatuses = service.getAllBayStatus();

        assertEquals(20, bayStatuses.size());
        assertTrue(bayStatuses.get(0).isOccupied());
        assertFalse(bayStatuses.get(3).isOccupied());
    }

    @Test
    void getPartsCostFromInventory_Success() {
        when(inventoryClient.getPartsCostForService(1)).thenReturn(Map.of("data", 150.0));

        java.math.BigDecimal cost = service.getPartsCostFromInventory(1);

        assertEquals(new java.math.BigDecimal("150.0"), cost);
    }

    @Test
    void getPartsCostFromInventory_Exception_ReturnsZero() {
        when(inventoryClient.getPartsCostForService(1)).thenThrow(new RuntimeException("Service unavailable"));

        java.math.BigDecimal cost = service.getPartsCostFromInventory(1);

        assertEquals(java.math.BigDecimal.ZERO, cost);
    }

    @Test
    void createRequest_VehicleDataNull_ThrowsException() {
        ServiceRequestCreateDTO dto = new ServiceRequestCreateDTO();
        dto.setCustomerId(1);
        dto.setVehicleId(1);
        dto.setServiceType(ServiceType.REGULAR_SERVICE);

        Map<String, Object> vehicleResponse = Map.of("data", new Object());

        when(vehicleClient.getVehicleById(1)).thenReturn(Map.of("other", "data"));

        assertThrows(ResourceNotFoundException.class, () -> service.createRequest(dto));
    }

    @Test
    void createRequest_VehicleClientException_ThrowsException() {
        ServiceRequestCreateDTO dto = new ServiceRequestCreateDTO();
        dto.setCustomerId(1);
        dto.setVehicleId(1);
        dto.setServiceType(ServiceType.REGULAR_SERVICE);

        when(vehicleClient.getVehicleById(1)).thenThrow(new RuntimeException("Service unavailable"));

        assertThrows(ResourceNotFoundException.class, () -> service.createRequest(dto));
    }

    @Test
    void createRequest_WithPickup_Success() {
        ServiceRequestCreateDTO dto = new ServiceRequestCreateDTO();
        dto.setCustomerId(1);
        dto.setVehicleId(1);
        dto.setServiceType(ServiceType.REPAIR);
        dto.setPriority(Priority.URGENT);
        dto.setPickupRequired(true);
        dto.setPickupAddress("123 Main St");

        Map<String, Object> vehicleData = Map.of("customerId", 1);
        Map<String, Object> vehicleResponse = Map.of("data", vehicleData);

        when(vehicleClient.getVehicleById(1)).thenReturn(vehicleResponse);
        when(repository.save(any(ServiceRequest.class))).thenAnswer(inv -> {
            ServiceRequest sr = inv.getArgument(0);
            sr.setId(1);
            return sr;
        });

        ServiceRequestResponse response = service.createRequest(dto);

        assertNotNull(response);
        assertTrue(response.getPickupRequired());
    }

    @Test
    void reschedule_Success() {
        testRequest.setStatus(RequestStatus.PENDING);
        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(repository.save(any(ServiceRequest.class))).thenReturn(testRequest);

        ServiceRequestResponse response = service.reschedule(1, "2026-01-15");

        assertNotNull(response);
        assertNotNull(testRequest.getPreferredDate());
    }

    @Test
    void reschedule_AssignedStatus_Success() {
        testRequest.setStatus(RequestStatus.ASSIGNED);
        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(repository.save(any(ServiceRequest.class))).thenReturn(testRequest);

        ServiceRequestResponse response = service.reschedule(1, "2026-01-20");

        assertNotNull(response);
    }

    @Test
    void reschedule_WrongStatus_ThrowsException() {
        testRequest.setStatus(RequestStatus.IN_PROGRESS);
        when(repository.findById(1)).thenReturn(Optional.of(testRequest));

        assertThrows(BadRequestException.class, () -> service.reschedule(1, "2026-01-15"));
    }

    @Test
    void reschedule_CompletedStatus_ThrowsException() {
        testRequest.setStatus(RequestStatus.COMPLETED);
        when(repository.findById(1)).thenReturn(Optional.of(testRequest));

        assertThrows(BadRequestException.class, () -> service.reschedule(1, "2026-01-15"));
    }

    @Test
    void isOwner_ReturnsTrue_WhenMatch() {
        when(repository.findById(1)).thenReturn(Optional.of(testRequest));

        boolean result = service.isOwner(1, 1);

        assertTrue(result);
    }

    @Test
    void isOwner_ReturnsFalse_WhenNoMatch() {
        when(repository.findById(1)).thenReturn(Optional.of(testRequest));

        boolean result = service.isOwner(1, 999);

        assertFalse(result);
    }

    @Test
    void isOwner_ReturnsFalse_WhenNotFound() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        boolean result = service.isOwner(999, 1);

        assertFalse(result);
    }

    @Test
    void createRequest_WithPreferredDate_Success() {
        ServiceRequestCreateDTO dto = new ServiceRequestCreateDTO();
        dto.setCustomerId(1);
        dto.setVehicleId(1);
        dto.setServiceType(ServiceType.REGULAR_SERVICE);
        dto.setPreferredDate("2026-02-15");

        Map<String, Object> vehicleData = Map.of("customerId", 1);
        Map<String, Object> vehicleResponse = Map.of("data", vehicleData);

        when(vehicleClient.getVehicleById(1)).thenReturn(vehicleResponse);
        when(repository.save(any(ServiceRequest.class))).thenAnswer(inv -> {
            ServiceRequest sr = inv.getArgument(0);
            sr.setId(1);
            sr.setCreatedAt(LocalDateTime.now());
            return sr;
        });

        ServiceRequestResponse response = service.createRequest(dto);

        assertNotNull(response);
    }

    @Test
    void createRequest_WithNullPickupRequired_DefaultsFalse() {
        ServiceRequestCreateDTO dto = new ServiceRequestCreateDTO();
        dto.setCustomerId(1);
        dto.setVehicleId(1);
        dto.setServiceType(ServiceType.REGULAR_SERVICE);
        dto.setPickupRequired(null);

        Map<String, Object> vehicleData = Map.of("customerId", 1);
        Map<String, Object> vehicleResponse = Map.of("data", vehicleData);

        when(vehicleClient.getVehicleById(1)).thenReturn(vehicleResponse);
        when(repository.save(any(ServiceRequest.class))).thenAnswer(inv -> {
            ServiceRequest sr = inv.getArgument(0);
            sr.setId(1);
            return sr;
        });

        ServiceRequestResponse response = service.createRequest(dto);

        assertNotNull(response);
    }

    @Test
    void setPricing_WithNullCosts_UsesZero() {
        testRequest.setStatus(RequestStatus.COMPLETED);
        testRequest.setFinalCost(null);
        com.vsms.servicerequestservice.dto.request.CompleteWorkDTO dto = new com.vsms.servicerequestservice.dto.request.CompleteWorkDTO();
        dto.setPartsCost(null);
        dto.setLaborCost(null);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(repository.save(any(ServiceRequest.class))).thenReturn(testRequest);

        ServiceRequestResponse response = service.setPricing(1, dto);

        assertNotNull(response);
        assertEquals(0f, testRequest.getFinalCost());
    }

    @Test
    void setPricing_WithOnlyPartsCost_Success() {
        testRequest.setStatus(RequestStatus.COMPLETED);
        testRequest.setFinalCost(null);
        com.vsms.servicerequestservice.dto.request.CompleteWorkDTO dto = new com.vsms.servicerequestservice.dto.request.CompleteWorkDTO();
        dto.setPartsCost(200f);
        dto.setLaborCost(null);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(repository.save(any(ServiceRequest.class))).thenReturn(testRequest);

        ServiceRequestResponse response = service.setPricing(1, dto);

        assertNotNull(response);
        assertEquals(200f, testRequest.getFinalCost());
    }

    @Test
    void setPricing_InvoiceGenerationFails_StillCompletes() {
        testRequest.setStatus(RequestStatus.COMPLETED);
        testRequest.setFinalCost(null);
        com.vsms.servicerequestservice.dto.request.CompleteWorkDTO dto = new com.vsms.servicerequestservice.dto.request.CompleteWorkDTO();
        dto.setPartsCost(200f);
        dto.setLaborCost(300f);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(repository.save(any(ServiceRequest.class))).thenReturn(testRequest);
        doThrow(new RuntimeException("Invoice error")).when(invoiceService).generateInvoice(1);

        ServiceRequestResponse response = service.setPricing(1, dto);

        assertNotNull(response);
    }

    @Test
    void setPricing_NotificationFails_StillCompletes() {
        testRequest.setStatus(RequestStatus.COMPLETED);
        testRequest.setFinalCost(null);
        testRequest.setCustomerId(1);
        testRequest.setVehicleId(1);
        com.vsms.servicerequestservice.dto.request.CompleteWorkDTO dto = new com.vsms.servicerequestservice.dto.request.CompleteWorkDTO();
        dto.setPartsCost(100f);
        dto.setLaborCost(100f);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(repository.save(any(ServiceRequest.class))).thenReturn(testRequest);
        when(authServiceClient.getCustomerById(1)).thenReturn(Map.of("data", Map.of("email", "test@test.com", "firstName", "Test")));
        doThrow(new RuntimeException("Notification error")).when(notificationPublisher).publishServiceCompleted(anyString(), anyString(), anyString(), anyString(), anyLong());

        ServiceRequestResponse response = service.setPricing(1, dto);

        assertNotNull(response);
    }

    @Test
    void assignTechnician_WorkloadUpdateFails_StillCompletes() {
        testRequest.setStatus(RequestStatus.PENDING);
        AssignTechnicianDTO dto = new AssignTechnicianDTO();
        dto.setTechnicianId(5);
        dto.setBayNumber(3);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(repository.findOccupiedBays()).thenReturn(List.of(1, 2));
        when(repository.save(any(ServiceRequest.class))).thenReturn(testRequest);
        doThrow(new RuntimeException("Auth service down")).when(authServiceClient).updateWorkload(5, "INCREMENT");

        ServiceRequestResponse response = service.assignTechnician(1, dto);

        assertNotNull(response);
    }

    @Test
    void updateStatus_ToCompleted_WorkloadUpdateFails_StillCompletes() {
        testRequest.setStatus(RequestStatus.IN_PROGRESS);
        testRequest.setTechnicianId(5);
        StatusUpdateDTO dto = new StatusUpdateDTO();
        dto.setStatus(RequestStatus.COMPLETED);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(repository.save(any(ServiceRequest.class))).thenReturn(testRequest);
        doThrow(new RuntimeException("Auth service down")).when(authServiceClient).updateWorkload(5, "DECREMENT");

        ServiceRequestResponse response = service.updateStatus(1, dto);

        assertNotNull(response);
        assertEquals(RequestStatus.COMPLETED, testRequest.getStatus());
    }

    @Test
    void cancelRequest_FromAssignedStatus_Success() {
        testRequest.setStatus(RequestStatus.ASSIGNED);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(repository.save(any(ServiceRequest.class))).thenReturn(testRequest);

        ServiceRequestResponse response = service.cancelRequest(1);

        assertNotNull(response);
        assertEquals(RequestStatus.CANCELLED, testRequest.getStatus());
    }

    @Test
    void cancelRequest_FromInProgress_Success() {
        testRequest.setStatus(RequestStatus.IN_PROGRESS);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));
        when(repository.save(any(ServiceRequest.class))).thenReturn(testRequest);

        ServiceRequestResponse response = service.cancelRequest(1);

        assertNotNull(response);
        assertEquals(RequestStatus.CANCELLED, testRequest.getStatus());
    }

    @Test
    void getPartsCostFromInventory_NullResponse_ReturnsZero() {
        when(inventoryClient.getPartsCostForService(1)).thenReturn(null);

        java.math.BigDecimal cost = service.getPartsCostFromInventory(1);

        assertEquals(java.math.BigDecimal.ZERO, cost);
    }

    @Test
    void getPartsCostFromInventory_NullData_ReturnsZero() {
        // Use HashMap to allow null values since Map.of() doesn't support nulls
        Map<String, Object> responseWithNullData = new java.util.HashMap<>();
        responseWithNullData.put("data", null);
        when(inventoryClient.getPartsCostForService(1)).thenReturn(responseWithNullData);

        java.math.BigDecimal cost = service.getPartsCostFromInventory(1);

        assertEquals(java.math.BigDecimal.ZERO, cost);
    }

    @Test
    void assignTechnician_BayNumberZero_ThrowsException() {
        testRequest.setStatus(RequestStatus.PENDING);
        AssignTechnicianDTO dto = new AssignTechnicianDTO();
        dto.setTechnicianId(5);
        dto.setBayNumber(0);

        when(repository.findById(1)).thenReturn(Optional.of(testRequest));

        assertThrows(BadRequestException.class, () -> service.assignTechnician(1, dto));
    }

    @Test
    void getAllBayStatus_NoOccupiedBays_AllAvailable() {
        when(repository.findOccupiedBays()).thenReturn(List.of());

        var bayStatuses = service.getAllBayStatus();

        assertEquals(20, bayStatuses.size());
        assertFalse(bayStatuses.get(0).isOccupied());
        assertFalse(bayStatuses.get(19).isOccupied());
    }

    @Test
    void getAllBayStatus_OccupiedBayNoRequest_HandlesGracefully() {
        when(repository.findOccupiedBays()).thenReturn(List.of(1));
        when(repository.findByBayNumberAndStatusIn(eq(1), anyList())).thenReturn(Optional.empty());

        var bayStatuses = service.getAllBayStatus();

        assertEquals(20, bayStatuses.size());
        assertTrue(bayStatuses.get(0).isOccupied());
        assertNull(bayStatuses.get(0).getServiceRequestId());
    }

    @Test
    void createRequest_PickupRequiredWithNullAddress_ThrowsException() {
        ServiceRequestCreateDTO dto = new ServiceRequestCreateDTO();
        dto.setCustomerId(1);
        dto.setVehicleId(1);
        dto.setServiceType(ServiceType.REGULAR_SERVICE);
        dto.setPickupRequired(true);
        dto.setPickupAddress(null);

        Map<String, Object> vehicleData = Map.of("customerId", 1);
        Map<String, Object> vehicleResponse = Map.of("data", vehicleData);

        when(vehicleClient.getVehicleById(1)).thenReturn(vehicleResponse);

        assertThrows(BadRequestException.class, () -> service.createRequest(dto));
    }
}
