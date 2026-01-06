package com.vsms.servicerequestservice.service;

import com.vsms.servicerequestservice.client.AuthServiceClient;
import com.vsms.servicerequestservice.client.InventoryClient;
import com.vsms.servicerequestservice.dto.request.PaymentDTO;
import com.vsms.servicerequestservice.dto.response.InvoiceResponse;
import com.vsms.servicerequestservice.dto.response.RevenueStats;
import com.vsms.servicerequestservice.entity.Invoice;
import com.vsms.servicerequestservice.entity.ServiceRequest;
import com.vsms.servicerequestservice.enums.InvoiceStatus;
import com.vsms.servicerequestservice.enums.RequestStatus;
import com.vsms.servicerequestservice.exception.BadRequestException;
import com.vsms.servicerequestservice.exception.ResourceNotFoundException;
import com.vsms.servicerequestservice.repository.InvoiceRepository;
import com.vsms.servicerequestservice.repository.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final InventoryClient inventoryClient;
    private final AuthServiceClient authServiceClient;
    private final NotificationPublisher notificationPublisher;

    public InvoiceResponse generateInvoice(Integer serviceRequestId) {
        if (invoiceRepository.existsByServiceRequestId(serviceRequestId)) {
            throw new BadRequestException("Invoice already exists for service request: " + serviceRequestId);
        }

        ServiceRequest sr = serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceRequest", "id", serviceRequestId));

        BigDecimal totalFromPricing = floatToBigDecimal(sr.getFinalCost());
        if (totalFromPricing == null) {
            totalFromPricing = BigDecimal.ZERO;
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
        }

        BigDecimal laborCost = totalFromPricing.subtract(partsCost);
        if (laborCost.compareTo(BigDecimal.ZERO) < 0) {
            laborCost = BigDecimal.ZERO;
        }

        String invoiceNumber = generateInvoiceNumber();

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .serviceRequestId(serviceRequestId)
                .customerId(sr.getCustomerId())
                .laborCost(laborCost)
                .partsCost(partsCost)
                .totalAmount(totalFromPricing)
                .status(InvoiceStatus.PENDING)
                .build();

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice {} generated for service request {}, total: {}", invoiceNumber, serviceRequestId,
                totalFromPricing);

        // Send notification to customer with real email
        try {
            String customerEmail = getCustomerEmail(sr.getCustomerId());
            String customerName = getCustomerName(sr.getCustomerId());

            if (customerEmail == null) {
                log.warn("Customer email not found for customerId: {}. Notification skipped.", sr.getCustomerId());
            } else {
                log.info("Sending invoice generated notification to: {}", customerEmail);
                notificationPublisher.publishInvoiceGenerated(
                        invoiceNumber,
                        customerName,
                        customerEmail,
                        totalFromPricing);
            }
        } catch (Exception e) {
            log.error("Could not publish invoice generated event: {}", e.getMessage(), e);
        }

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getMyInvoices(Integer customerId) {
        return invoiceRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getUnpaidInvoices() {
        return invoiceRepository.findByStatus(InvoiceStatus.PENDING).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public InvoiceResponse payInvoice(Integer id, PaymentDTO dto) {
        Invoice invoice = findById(id);

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BadRequestException("Invoice is already paid");
        }

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaymentMethod(dto.getPaymentMethod());
        invoice.setPaidAt(LocalDateTime.now());

        Invoice updated = invoiceRepository.save(invoice);
        log.info("Invoice {} paid via {}", invoice.getInvoiceNumber(), dto.getPaymentMethod());

        // Auto-close the service request
        ServiceRequest sr = serviceRequestRepository.findById(invoice.getServiceRequestId()).orElse(null);
        if (sr != null) {
            sr.setStatus(RequestStatus.CLOSED);
            serviceRequestRepository.save(sr);
            log.info("Service request {} closed after payment", invoice.getServiceRequestId());
        }
        // Note: Manager notification removed - invoice status is visible in dashboard
        // Service request auto-closes after payment which is sufficient
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public RevenueStats getRevenueStats() {
        List<Invoice> allInvoices = invoiceRepository.findAll();
        List<Invoice> paidInvoices = invoiceRepository.findByStatus(InvoiceStatus.PAID);
        List<Invoice> unpaidInvoices = invoiceRepository.findByStatus(InvoiceStatus.PENDING);

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
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
    }

    private String generateInvoiceNumber() {
        long count = invoiceRepository.count() + 1;
        return String.format("INV-%d-%04d", Year.now().getValue(), count);
    }

    // Get labor and parts costs for a service request (for ServiceRequestResponse)
    @Transactional(readOnly = true)
    public Float[] getCostsByServiceRequestId(Integer serviceRequestId) {
        return invoiceRepository.findByServiceRequestId(serviceRequestId)
                .map(inv -> new Float[] {
                        inv.getLaborCost() != null ? inv.getLaborCost().floatValue() : null,
                        inv.getPartsCost() != null ? inv.getPartsCost().floatValue() : null
                })
                .orElse(new Float[] { null, null });
    }

    private BigDecimal getBigDecimal(Object value) {
        if (value == null)
            return null;
        if (value instanceof BigDecimal bd)
            return bd;
        if (value instanceof Number num)
            return BigDecimal.valueOf(num.doubleValue());
        if (value instanceof String str)
            return new BigDecimal(str);
        return null;
    }

    private BigDecimal floatToBigDecimal(Float value) {
        if (value == null)
            return null;
        return BigDecimal.valueOf(value.doubleValue());
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

    /**
     * Check if a customer owns an invoice (for @PreAuthorize ownership checks)
     */
    @Transactional(readOnly = true)
    public boolean isOwner(Integer invoiceId, Integer customerId) {
        return invoiceRepository.findById(invoiceId)
                .map(inv -> inv.getCustomerId().equals(customerId))
                .orElse(false);
    }

    /**
     * Get customer email from auth-service
     */
    @SuppressWarnings("unchecked")
    private String getCustomerEmail(Integer customerId) {
        try {
            Map<String, Object> response = authServiceClient.getCustomerById(customerId);
            if (response != null && response.get("data") != null) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                return (String) data.get("email");
            }
        } catch (Exception e) {
            log.warn("Could not fetch customer email for {}: {}", customerId, e.getMessage());
        }
        return null;
    }

    /**
     * Get customer name from auth-service
     */
    @SuppressWarnings("unchecked")
    private String getCustomerName(Integer customerId) {
        try {
            Map<String, Object> response = authServiceClient.getCustomerById(customerId);
            if (response != null && response.get("data") != null) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                String firstName = (String) data.get("firstName");
                String lastName = (String) data.get("lastName");
                if (firstName != null && lastName != null) {
                    return firstName + " " + lastName;
                } else if (firstName != null) {
                    return firstName;
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch customer name for {}: {}", customerId, e.getMessage());
        }
        return "Customer";
    }
}
