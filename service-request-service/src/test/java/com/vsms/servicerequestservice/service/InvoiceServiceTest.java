package com.vsms.servicerequestservice.service;

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
        verify(notificationPublisher, times(1)).publishInvoiceGenerated(anyString(), anyString(), anyString(), any());
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
    void payInvoice_NotificationFails_StillCompletes() {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentMethod(PaymentMethod.CASH);

        when(invoiceRepository.findById(1)).thenReturn(Optional.of(testInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.of(testServiceRequest));
        doThrow(new RuntimeException("Notification failed")).when(notificationPublisher)
                .publishInvoicePaid(anyString(), anyString(), anyString(), any(), anyString());

        InvoiceResponse response = invoiceService.payInvoice(1, dto);

        assertNotNull(response);
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
}
