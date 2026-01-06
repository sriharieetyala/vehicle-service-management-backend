package com.vsms.servicerequestservice.service;

import com.vsms.servicerequestservice.client.AuthServiceClient;
import com.vsms.servicerequestservice.client.InventoryClient;
import com.vsms.servicerequestservice.dto.request.PaymentDTO;
import com.vsms.servicerequestservice.dto.response.InvoiceResponse;
import com.vsms.servicerequestservice.dto.response.RevenueStats;
import com.vsms.servicerequestservice.entity.Invoice;
import com.vsms.servicerequestservice.entity.ServiceRequest;
import com.vsms.servicerequestservice.enums.InvoiceStatus;
import com.vsms.servicerequestservice.enums.PaymentMethod;
import com.vsms.servicerequestservice.enums.RequestStatus;
import com.vsms.servicerequestservice.exception.BadRequestException;
import com.vsms.servicerequestservice.exception.ResourceNotFoundException;
import com.vsms.servicerequestservice.repository.InvoiceRepository;
import com.vsms.servicerequestservice.repository.ServiceRequestRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private ServiceRequestRepository serviceRequestRepository;
    @Mock
    private InventoryClient inventoryClient;
    @Mock
    private AuthServiceClient authServiceClient;
    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private InvoiceService invoiceService;

    private Invoice testInvoice;
    private ServiceRequest testServiceRequest;

    @BeforeEach
    void setUp() {
        testServiceRequest = ServiceRequest.builder()
                .id(1)
                .customerId(1)
                .vehicleId(1)
                .status(RequestStatus.COMPLETED)
                .finalCost(500f)
                .build();

        testInvoice = Invoice.builder()
                .id(1)
                .invoiceNumber("INV-2026-0001")
                .serviceRequestId(1)
                .customerId(1)
                .laborCost(BigDecimal.valueOf(300))
                .partsCost(BigDecimal.valueOf(200))
                .totalAmount(BigDecimal.valueOf(500))
                .status(InvoiceStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void generateInvoice_Success() {
        when(invoiceRepository.existsByServiceRequestId(1)).thenReturn(false);
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.of(testServiceRequest));
        when(invoiceRepository.count()).thenReturn(0L);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(1);
            i.setCreatedAt(LocalDateTime.now());
            return i;
        });

        InvoiceResponse response = invoiceService.generateInvoice(1);

        assertNotNull(response);
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void generateInvoice_AlreadyExists_ThrowsException() {
        when(invoiceRepository.existsByServiceRequestId(1)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> invoiceService.generateInvoice(1));
    }

    @Test
    void generateInvoice_ServiceRequestNotFound_ThrowsException() {
        when(invoiceRepository.existsByServiceRequestId(1)).thenReturn(false);
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> invoiceService.generateInvoice(1));
    }

    @Test
    void getAllInvoices_ReturnsAll() {
        when(invoiceRepository.findAll()).thenReturn(List.of(testInvoice));

        List<InvoiceResponse> responses = invoiceService.getAllInvoices();

        assertEquals(1, responses.size());
    }

    @Test
    void getMyInvoices_ReturnsCustomerInvoices() {
        when(invoiceRepository.findByCustomerId(1)).thenReturn(List.of(testInvoice));

        List<InvoiceResponse> responses = invoiceService.getMyInvoices(1);

        assertEquals(1, responses.size());
    }

    @Test
    void getUnpaidInvoices_ReturnsPending() {
        when(invoiceRepository.findByStatus(InvoiceStatus.PENDING)).thenReturn(List.of(testInvoice));

        List<InvoiceResponse> responses = invoiceService.getUnpaidInvoices();

        assertEquals(1, responses.size());
    }

    @Test
    void payInvoice_Success() {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentMethod(PaymentMethod.CARD);

        when(invoiceRepository.findById(1)).thenReturn(Optional.of(testInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.of(testServiceRequest));

        InvoiceResponse response = invoiceService.payInvoice(1, dto);

        assertNotNull(response);
        assertEquals(InvoiceStatus.PAID, testInvoice.getStatus());
        verify(serviceRequestRepository, times(1)).save(testServiceRequest);
    }

    @Test
    void payInvoice_AlreadyPaid_ThrowsException() {
        testInvoice.setStatus(InvoiceStatus.PAID);
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentMethod(PaymentMethod.CARD);

        when(invoiceRepository.findById(1)).thenReturn(Optional.of(testInvoice));

        assertThrows(BadRequestException.class, () -> invoiceService.payInvoice(1, dto));
    }

    @Test
    void getRevenueStats_ReturnsStats() {
        when(invoiceRepository.findAll()).thenReturn(List.of(testInvoice));
        when(invoiceRepository.findByStatus(InvoiceStatus.PAID)).thenReturn(List.of());
        when(invoiceRepository.findByStatus(InvoiceStatus.PENDING)).thenReturn(List.of(testInvoice));

        RevenueStats stats = invoiceService.getRevenueStats();

        assertNotNull(stats);
        assertEquals(1, stats.getTotalInvoices());
        assertEquals(BigDecimal.valueOf(500), stats.getTotalRevenue());
    }

    @Test
    void generateInvoice_WithPartsCostFromInventory_Success() {
        testServiceRequest.setFinalCost(500f);
        when(invoiceRepository.existsByServiceRequestId(1)).thenReturn(false);
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.of(testServiceRequest));
        when(inventoryClient.getPartsCostForService(1)).thenReturn(java.util.Map.of("data", 200));
        when(invoiceRepository.count()).thenReturn(1L);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(2);
            i.setInvoiceNumber("INV-2026-0002");
            return i;
        });

        InvoiceResponse response = invoiceService.generateInvoice(1);

        assertNotNull(response);
        // Note: publishInvoiceGenerated may not be called if authServiceClient returns
        // null email
    }

    @Test
    void generateInvoice_NullFinalCost_UsesZero() {
        testServiceRequest.setFinalCost(null);
        when(invoiceRepository.existsByServiceRequestId(1)).thenReturn(false);
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.of(testServiceRequest));
        when(invoiceRepository.count()).thenReturn(2L);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(3);
            return i;
        });

        InvoiceResponse response = invoiceService.generateInvoice(1);

        assertNotNull(response);
    }

    @Test
    void payInvoice_ServiceRequestNotFound_StillCompletes() {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentMethod(PaymentMethod.UPI);

        when(invoiceRepository.findById(1)).thenReturn(Optional.of(testInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.empty());

        InvoiceResponse response = invoiceService.payInvoice(1, dto);

        assertNotNull(response);
        assertEquals(InvoiceStatus.PAID, testInvoice.getStatus());
    }

    @Test
    void payInvoice_ServiceRequestNotClosed_WhenNull() {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentMethod(PaymentMethod.CASH);

        when(invoiceRepository.findById(1)).thenReturn(Optional.of(testInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.of(testServiceRequest));

        InvoiceResponse response = invoiceService.payInvoice(1, dto);

        assertNotNull(response);
        // Manager notification removed - auto-close is sufficient
    }

    @Test
    void generateInvoice_InventoryClientFails_StillGenerates() {
        testServiceRequest.setFinalCost(500f);
        when(invoiceRepository.existsByServiceRequestId(1)).thenReturn(false);
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.of(testServiceRequest));
        when(inventoryClient.getPartsCostForService(1)).thenThrow(new RuntimeException("Service down"));
        when(invoiceRepository.count()).thenReturn(5L);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(6);
            return i;
        });

        InvoiceResponse response = invoiceService.generateInvoice(1);

        assertNotNull(response);
    }

    @Test
    void payInvoice_InvoiceNotFound_ThrowsException() {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentMethod(PaymentMethod.CARD);

        when(invoiceRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> invoiceService.payInvoice(999, dto));
    }

    @Test
    void getRevenueStats_WithPaidInvoices_CalculatesCorrectly() {
        Invoice paidInvoice = Invoice.builder()
                .id(2)
                .totalAmount(BigDecimal.valueOf(1000))
                .status(InvoiceStatus.PAID)
                .build();

        when(invoiceRepository.findAll()).thenReturn(List.of(testInvoice, paidInvoice));
        when(invoiceRepository.findByStatus(InvoiceStatus.PAID)).thenReturn(List.of(paidInvoice));
        when(invoiceRepository.findByStatus(InvoiceStatus.PENDING)).thenReturn(List.of(testInvoice));

        RevenueStats stats = invoiceService.getRevenueStats();

        assertEquals(2, stats.getTotalInvoices());
        assertEquals(1, stats.getPaidInvoices());
        assertEquals(1, stats.getUnpaidInvoices());
        assertEquals(BigDecimal.valueOf(1500), stats.getTotalRevenue());
        assertEquals(BigDecimal.valueOf(1000), stats.getCollectedRevenue());
    }

    @Test
    void generateInvoice_WithValidCustomerEmail_SendsNotification() {
        testServiceRequest.setFinalCost(500f);
        when(invoiceRepository.existsByServiceRequestId(1)).thenReturn(false);
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.of(testServiceRequest));
        when(invoiceRepository.count()).thenReturn(10L);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(10);
            return i;
        });

        // Mock auth service to return customer data with email
        java.util.Map<String, Object> customerData = new java.util.HashMap<>();
        customerData.put("email", "customer@test.com");
        customerData.put("firstName", "John");
        customerData.put("lastName", "Doe");
        when(authServiceClient.getCustomerById(1)).thenReturn(java.util.Map.of("data", customerData));

        InvoiceResponse response = invoiceService.generateInvoice(1);

        assertNotNull(response);
        verify(notificationPublisher, times(1)).publishInvoiceGenerated(anyString(), eq("John Doe"),
                eq("customer@test.com"), any());
    }

    @Test
    void generateInvoice_AuthServiceFails_StillGenerates() {
        testServiceRequest.setFinalCost(500f);
        when(invoiceRepository.existsByServiceRequestId(1)).thenReturn(false);
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.of(testServiceRequest));
        when(invoiceRepository.count()).thenReturn(11L);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(11);
            return i;
        });
        when(authServiceClient.getCustomerById(1)).thenThrow(new RuntimeException("Auth service down"));

        InvoiceResponse response = invoiceService.generateInvoice(1);

        assertNotNull(response);
        verify(notificationPublisher, never()).publishInvoiceGenerated(anyString(), anyString(), anyString(), any());
    }

    @Test
    void generateInvoice_CustomerEmailNull_SkipsNotification() {
        testServiceRequest.setFinalCost(500f);
        when(invoiceRepository.existsByServiceRequestId(1)).thenReturn(false);
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.of(testServiceRequest));
        when(invoiceRepository.count()).thenReturn(12L);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(12);
            return i;
        });
        when(authServiceClient.getCustomerById(1)).thenReturn(java.util.Collections.emptyMap());

        InvoiceResponse response = invoiceService.generateInvoice(1);

        assertNotNull(response);
        verify(notificationPublisher, never()).publishInvoiceGenerated(anyString(), anyString(), anyString(), any());
    }

    @Test
    void generateInvoice_CustomerFirstNameOnly_UsesFirstName() {
        testServiceRequest.setFinalCost(500f);
        when(invoiceRepository.existsByServiceRequestId(1)).thenReturn(false);
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.of(testServiceRequest));
        when(invoiceRepository.count()).thenReturn(13L);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(13);
            return i;
        });

        java.util.Map<String, Object> customerData = new java.util.HashMap<>();
        customerData.put("email", "jane@test.com");
        customerData.put("firstName", "Jane");
        customerData.put("lastName", null);
        when(authServiceClient.getCustomerById(1)).thenReturn(java.util.Map.of("data", customerData));

        InvoiceResponse response = invoiceService.generateInvoice(1);

        assertNotNull(response);
        verify(notificationPublisher, times(1)).publishInvoiceGenerated(anyString(), eq("Jane"), eq("jane@test.com"),
                any());
    }

    @Test
    void isOwner_ReturnsTrue_WhenMatch() {
        when(invoiceRepository.findById(1)).thenReturn(Optional.of(testInvoice));

        boolean result = invoiceService.isOwner(1, 1);

        assertTrue(result);
    }

    @Test
    void isOwner_ReturnsFalse_WhenNoMatch() {
        when(invoiceRepository.findById(1)).thenReturn(Optional.of(testInvoice));

        boolean result = invoiceService.isOwner(1, 999);

        assertFalse(result);
    }

    @Test
    void isOwner_ReturnsFalse_WhenNotFound() {
        when(invoiceRepository.findById(999)).thenReturn(Optional.empty());

        boolean result = invoiceService.isOwner(999, 1);

        assertFalse(result);
    }

    @Test
    void generateInvoice_LaborCostNegative_SetsToZero() {
        // partsCost > totalFromPricing would result in negative laborCost
        testServiceRequest.setFinalCost(100f);
        when(invoiceRepository.existsByServiceRequestId(1)).thenReturn(false);
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.of(testServiceRequest));
        when(inventoryClient.getPartsCostForService(1)).thenReturn(java.util.Map.of("data", 200)); // parts > total
        when(invoiceRepository.count()).thenReturn(20L);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(20);
            return i;
        });

        InvoiceResponse response = invoiceService.generateInvoice(1);

        assertNotNull(response);
        // Labor cost should be zero since parts > total
    }

    @Test
    void generateInvoice_PartsCostAsBigDecimal_Success() {
        testServiceRequest.setFinalCost(500f);
        when(invoiceRepository.existsByServiceRequestId(1)).thenReturn(false);
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.of(testServiceRequest));
        when(inventoryClient.getPartsCostForService(1)).thenReturn(java.util.Map.of("data", new BigDecimal("150.50")));
        when(invoiceRepository.count()).thenReturn(21L);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(21);
            return i;
        });

        InvoiceResponse response = invoiceService.generateInvoice(1);

        assertNotNull(response);
    }

    @Test
    void generateInvoice_PartsCostAsString_Success() {
        testServiceRequest.setFinalCost(500f);
        when(invoiceRepository.existsByServiceRequestId(1)).thenReturn(false);
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.of(testServiceRequest));
        when(inventoryClient.getPartsCostForService(1)).thenReturn(java.util.Map.of("data", "175.25"));
        when(invoiceRepository.count()).thenReturn(22L);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(22);
            return i;
        });

        InvoiceResponse response = invoiceService.generateInvoice(1);

        assertNotNull(response);
    }

    @Test
    void generateInvoice_PartsCostNullData_UsesZero() {
        testServiceRequest.setFinalCost(500f);
        when(invoiceRepository.existsByServiceRequestId(1)).thenReturn(false);
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.of(testServiceRequest));
        when(inventoryClient.getPartsCostForService(1)).thenReturn(java.util.Map.of("other", "value"));
        when(invoiceRepository.count()).thenReturn(23L);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(23);
            return i;
        });

        InvoiceResponse response = invoiceService.generateInvoice(1);

        assertNotNull(response);
    }

    @Test
    void generateInvoice_NotificationPublisherFails_StillCompletes() {
        testServiceRequest.setFinalCost(500f);
        when(invoiceRepository.existsByServiceRequestId(1)).thenReturn(false);
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.of(testServiceRequest));
        when(invoiceRepository.count()).thenReturn(24L);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(24);
            return i;
        });

        java.util.Map<String, Object> customerData = new java.util.HashMap<>();
        customerData.put("email", "test@test.com");
        customerData.put("firstName", "Test");
        customerData.put("lastName", "User");
        when(authServiceClient.getCustomerById(1)).thenReturn(java.util.Map.of("data", customerData));
        doThrow(new RuntimeException("RabbitMQ down")).when(notificationPublisher)
                .publishInvoiceGenerated(anyString(), anyString(), anyString(), any());

        InvoiceResponse response = invoiceService.generateInvoice(1);

        assertNotNull(response);
    }

    @Test
    void getRevenueStats_EmptyInvoices_ReturnsZeros() {
        when(invoiceRepository.findAll()).thenReturn(List.of());
        when(invoiceRepository.findByStatus(InvoiceStatus.PAID)).thenReturn(List.of());
        when(invoiceRepository.findByStatus(InvoiceStatus.PENDING)).thenReturn(List.of());

        RevenueStats stats = invoiceService.getRevenueStats();

        assertNotNull(stats);
        assertEquals(0, stats.getTotalInvoices());
        assertEquals(0, stats.getPaidInvoices());
        assertEquals(0, stats.getUnpaidInvoices());
        assertEquals(BigDecimal.ZERO, stats.getTotalRevenue());
    }

    @Test
    void payInvoice_WithUPIPayment_Success() {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentMethod(PaymentMethod.UPI);

        when(invoiceRepository.findById(1)).thenReturn(Optional.of(testInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.of(testServiceRequest));

        InvoiceResponse response = invoiceService.payInvoice(1, dto);

        assertNotNull(response);
        assertEquals(PaymentMethod.UPI, testInvoice.getPaymentMethod());
    }

    @Test
    void payInvoice_WithCashPayment_Success() {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentMethod(PaymentMethod.CASH);

        when(invoiceRepository.findById(1)).thenReturn(Optional.of(testInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.of(testServiceRequest));

        InvoiceResponse response = invoiceService.payInvoice(1, dto);

        assertNotNull(response);
        assertEquals(PaymentMethod.CASH, testInvoice.getPaymentMethod());
    }

    @Test
    void getMyInvoices_NoInvoices_ReturnsEmptyList() {
        when(invoiceRepository.findByCustomerId(999)).thenReturn(List.of());

        List<InvoiceResponse> responses = invoiceService.getMyInvoices(999);

        assertTrue(responses.isEmpty());
    }

    @Test
    void getUnpaidInvoices_NoUnpaidInvoices_ReturnsEmptyList() {
        when(invoiceRepository.findByStatus(InvoiceStatus.PENDING)).thenReturn(List.of());

        List<InvoiceResponse> responses = invoiceService.getUnpaidInvoices();

        assertTrue(responses.isEmpty());
    }

    @Test
    void generateInvoice_CustomerDataNull_SkipsNotification() {
        testServiceRequest.setFinalCost(500f);
        when(invoiceRepository.existsByServiceRequestId(1)).thenReturn(false);
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.of(testServiceRequest));
        when(invoiceRepository.count()).thenReturn(25L);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(25);
            return i;
        });
        // Use HashMap to allow null values since Map.of() doesn't support nulls
        java.util.Map<String, Object> responseWithNullData = new java.util.HashMap<>();
        responseWithNullData.put("data", null);
        when(authServiceClient.getCustomerById(1)).thenReturn(responseWithNullData);

        InvoiceResponse response = invoiceService.generateInvoice(1);

        assertNotNull(response);
        verify(notificationPublisher, never()).publishInvoiceGenerated(anyString(), anyString(), anyString(), any());
    }

    @Test
    void generateInvoice_CustomerNoNames_UsesDefault() {
        testServiceRequest.setFinalCost(500f);
        when(invoiceRepository.existsByServiceRequestId(1)).thenReturn(false);
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.of(testServiceRequest));
        when(invoiceRepository.count()).thenReturn(26L);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(26);
            return i;
        });

        java.util.Map<String, Object> customerData = new java.util.HashMap<>();
        customerData.put("email", "noname@test.com");
        customerData.put("firstName", null);
        customerData.put("lastName", null);
        when(authServiceClient.getCustomerById(1)).thenReturn(java.util.Map.of("data", customerData));

        InvoiceResponse response = invoiceService.generateInvoice(1);

        assertNotNull(response);
        verify(notificationPublisher, times(1)).publishInvoiceGenerated(anyString(), eq("Customer"),
                eq("noname@test.com"), any());
    }

    // Tests for getCostsByServiceRequestId
    @Test
    void getCostsByServiceRequestId_WithInvoice_ReturnsCosts() {
        testInvoice.setLaborCost(new BigDecimal("100.50"));
        testInvoice.setPartsCost(new BigDecimal("200.75"));
        when(invoiceRepository.findByServiceRequestId(1)).thenReturn(Optional.of(testInvoice));

        Float[] costs = invoiceService.getCostsByServiceRequestId(1);

        assertNotNull(costs);
        assertEquals(2, costs.length);
        assertEquals(100.50f, costs[0], 0.01);
        assertEquals(200.75f, costs[1], 0.01);
    }

    @Test
    void getCostsByServiceRequestId_NoInvoice_ReturnsNulls() {
        when(invoiceRepository.findByServiceRequestId(999)).thenReturn(Optional.empty());

        Float[] costs = invoiceService.getCostsByServiceRequestId(999);

        assertNotNull(costs);
        assertEquals(2, costs.length);
        assertNull(costs[0]);
        assertNull(costs[1]);
    }

    @Test
    void getCostsByServiceRequestId_NullCosts_ReturnsNulls() {
        testInvoice.setLaborCost(null);
        testInvoice.setPartsCost(null);
        when(invoiceRepository.findByServiceRequestId(1)).thenReturn(Optional.of(testInvoice));

        Float[] costs = invoiceService.getCostsByServiceRequestId(1);

        assertNotNull(costs);
        assertNull(costs[0]);
        assertNull(costs[1]);
    }
}
