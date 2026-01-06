package com.vsms.servicerequestservice.controller;

import com.vsms.servicerequestservice.dto.request.PaymentDTO;
import com.vsms.servicerequestservice.dto.response.ApiResponse;
import com.vsms.servicerequestservice.dto.response.InvoiceCreatedResponse;
import com.vsms.servicerequestservice.dto.response.InvoiceResponse;
import com.vsms.servicerequestservice.dto.response.RevenueStats;
import com.vsms.servicerequestservice.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// InvoiceController handles invoice generation and payment processing
// Invoices are generated after service completion and paid by customers
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    // Generate invoice for a completed service request
    // Returns the invoice number for customer reference
    @PostMapping("/generate/{serviceRequestId}")
    public ResponseEntity<InvoiceCreatedResponse> generateInvoice(
            @PathVariable Integer serviceRequestId) {
        InvoiceResponse response = invoiceService.generateInvoice(serviceRequestId);
        return new ResponseEntity<>(
                InvoiceCreatedResponse.builder().invoiceNumber(response.getInvoiceNumber()).build(),
                HttpStatus.CREATED);
    }

    // Get all invoices for admin and manager dashboards
    @GetMapping
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getAllInvoices() {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getAllInvoices()));
    }

    // Get invoices for the logged in customer
    @GetMapping("/my")
    @PreAuthorize("#customerId == authentication.principal.id")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getMyInvoices(
            @RequestParam Integer customerId) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getMyInvoices(customerId)));
    }

    // Get unpaid invoices for follow up
    @GetMapping("/unpaid")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getUnpaidInvoices() {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getUnpaidInvoices()));
    }

    // Customer pays their invoice with payment details
    @PutMapping("/{id}/pay")
    @PreAuthorize("@invoiceService.isOwner(#id, authentication.principal.id)")
    public ResponseEntity<ApiResponse<InvoiceResponse>> payInvoice(
            @PathVariable Integer id,
            @Valid @RequestBody PaymentDTO dto) {
        return ResponseEntity.ok(
                ApiResponse.success("Payment successful", invoiceService.payInvoice(id, dto)));
    }

    // Get revenue statistics for manager dashboard
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<RevenueStats>> getRevenueStats() {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getRevenueStats()));
    }
}
