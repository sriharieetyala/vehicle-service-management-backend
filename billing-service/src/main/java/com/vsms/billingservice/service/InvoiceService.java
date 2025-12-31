package com.vsms.billingservice.service;

import com.vsms.billingservice.client.InventoryClient;
import com.vsms.billingservice.client.ServiceRequestClient;
import com.vsms.billingservice.dto.request.PaymentDTO;
import com.vsms.billingservice.dto.response.InvoiceResponse;
import com.vsms.billingservice.dto.response.RevenueStats;
import com.vsms.billingservice.entity.Invoice;
import com.vsms.billingservice.enums.InvoiceStatus;
import com.vsms.billingservice.exception.BadRequestException;
import com.vsms.billingservice.exception.ResourceNotFoundException;
import com.vsms.billingservice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InvoiceService {

    private final InvoiceRepository repository;
    private final ServiceRequestClient serviceRequestClient;
    private final InventoryClient inventoryClient;
    private final NotificationPublisher notificationPublisher;

    public InvoiceResponse generateInvoice(Integer serviceRequestId) {
        // Check if invoice already exists
        if (repository.existsByServiceRequestId(serviceRequestId)) {
            throw new BadRequestException("Invoice already exists for service request: " + serviceRequestId);
        }

        // Get service request details (labor cost, customer ID)
        Map<String, Object> srResponse = serviceRequestClient.getServiceRequest(serviceRequestId);
        Map<String, Object> srData = (Map<String, Object>) srResponse.get("data");

        if (srData == null) {
            throw new ResourceNotFoundException("ServiceRequest", "id", serviceRequestId);
        }

        Integer customerId = (Integer) srData.get("customerId");
        BigDecimal laborCost = getBigDecimal(srData.get("finalCost"));
        if (laborCost == null || laborCost.compareTo(BigDecimal.ZERO) == 0) {
            laborCost = getBigDecimal(srData.get("estimatedCost"));
        }
        if (laborCost == null) {
            laborCost = BigDecimal.ZERO;
        }

        // Get parts cost from inventory service
        BigDecimal partsCost = BigDecimal.ZERO;
        try {
            Map<String, Object> partsResponse = inventoryClient.getPartsCostForService(serviceRequestId);
            if (partsResponse != null && partsResponse.get("data") != null) {
                partsCost = getBigDecimal(partsResponse.get("data"));
            }
        } catch (Exception e) {
            log.warn("Could not fetch parts cost for service {}: {}", serviceRequestId, e.getMessage());
            // Continue with zero parts cost if inventory service is unavailable
        }

        // Calculate total
        BigDecimal totalAmount = laborCost.add(partsCost);

        // Generate invoice number
        String invoiceNumber = generateInvoiceNumber();

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .serviceRequestId(serviceRequestId)
                .customerId(customerId)
                .laborCost(laborCost)
                .partsCost(partsCost)
                .totalAmount(totalAmount)
                .status(InvoiceStatus.PENDING)
                .build();

        Invoice saved = repository.save(invoice);
        log.info("Invoice {} generated for service request {}, total: {}", invoiceNumber, serviceRequestId,
                totalAmount);

        // Publish notification event
        try {
            notificationPublisher.publishInvoiceGenerated(
                    invoiceNumber,
                    "Customer", // TODO: Get customer name from auth-service
                    "customer@email.com", // TODO: Get customer email from auth-service
                    totalAmount);
        } catch (Exception e) {
            log.warn("Could not publish invoice generated event: {}", e.getMessage());
        }

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Integer id) {
        return mapToResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getMyInvoices(Integer customerId) {
        return repository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getByServiceRequest(Integer serviceRequestId) {
        return repository.findByServiceRequestId(serviceRequestId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "serviceRequestId", serviceRequestId));
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getUnpaidInvoices() {
        return repository.findByStatus(InvoiceStatus.PENDING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public InvoiceResponse payInvoice(Integer id, PaymentDTO dto) {
        Invoice invoice = findById(id);

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BadRequestException("Invoice is already paid");
        }

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaymentMethod(dto.getPaymentMethod());
        invoice.setPaidAt(LocalDateTime.now());

        Invoice updated = repository.save(invoice);
        log.info("Invoice {} paid via {}", invoice.getInvoiceNumber(), dto.getPaymentMethod());

        // Publish notification event
        try {
            notificationPublisher.publishInvoicePaid(
                    invoice.getInvoiceNumber(),
                    "Customer", // TODO: Get customer name
                    "manager@vsms.com", // TODO: Get manager email
                    invoice.getTotalAmount(),
                    dto.getPaymentMethod().name());
        } catch (Exception e) {
            log.warn("Could not publish invoice paid event: {}", e.getMessage());
        }

        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public RevenueStats getRevenueStats() {
        List<Invoice> allInvoices = repository.findAll();
        List<Invoice> paidInvoices = repository.findByStatus(InvoiceStatus.PAID);
        List<Invoice> unpaidInvoices = repository.findByStatus(InvoiceStatus.PENDING);

        BigDecimal totalRevenue = allInvoices.stream()
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal collectedRevenue = paidInvoices.stream()
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingRevenue = unpaidInvoices.stream()
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return RevenueStats.builder()
                .totalInvoices(allInvoices.size())
                .paidInvoices(paidInvoices.size())
                .unpaidInvoices(unpaidInvoices.size())
                .totalRevenue(totalRevenue)
                .collectedRevenue(collectedRevenue)
                .pendingRevenue(pendingRevenue)
                .build();
    }

    private Invoice findById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
    }

    private String generateInvoiceNumber() {
        long count = repository.count() + 1;
        return String.format("INV-%d-%04d", Year.now().getValue(), count);
    }

    private BigDecimal getBigDecimal(Object value) {
        if (value == null)
            return null;
        if (value instanceof BigDecimal)
            return (BigDecimal) value;
        if (value instanceof Number)
            return BigDecimal.valueOf(((Number) value).doubleValue());
        if (value instanceof String)
            return new BigDecimal((String) value);
        return null;
    }

    private InvoiceResponse mapToResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .serviceRequestId(invoice.getServiceRequestId())
                .customerId(invoice.getCustomerId())
                .laborCost(invoice.getLaborCost())
                .partsCost(invoice.getPartsCost())
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus())
                .paymentMethod(invoice.getPaymentMethod())
                .paidAt(invoice.getPaidAt())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
}
