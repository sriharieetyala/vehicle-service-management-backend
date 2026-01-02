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

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    // Generate invoice (role check done at gateway)
    @PostMapping("/generate/{serviceRequestId}")
    public ResponseEntity<InvoiceCreatedResponse> generateInvoice(
            @PathVariable Integer serviceRequestId) {
        InvoiceResponse response = invoiceService.generateInvoice(serviceRequestId);
        return new ResponseEntity<>(
                InvoiceCreatedResponse.builder().invoiceNumber(response.getInvoiceNumber()).build(),
                HttpStatus.CREATED);
    }

    // Get all invoices (role check done at gateway)
    @GetMapping
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getAllInvoices() {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getAllInvoices()));
    }

    // Get my invoices - ownership check: customer can only see their own
    @GetMapping("/my")
    @PreAuthorize("#customerId == authentication.principal.id")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getMyInvoices(
            @RequestParam Integer customerId) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getMyInvoices(customerId)));
    }

    // Get unpaid invoices (role check done at gateway)
    @GetMapping("/unpaid")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getUnpaidInvoices() {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getUnpaidInvoices()));
    }

    // Pay invoice - ownership check: customer can only pay their own
    @PutMapping("/{id}/pay")
    @PreAuthorize("@invoiceService.isOwner(#id, authentication.principal.id)")
    public ResponseEntity<ApiResponse<InvoiceResponse>> payInvoice(
            @PathVariable Integer id,
            @Valid @RequestBody PaymentDTO dto) {
        return ResponseEntity.ok(
                ApiResponse.success("Payment successful", invoiceService.payInvoice(id, dto)));
    }

    // Get revenue stats (role check done at gateway)
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<RevenueStats>> getRevenueStats() {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getRevenueStats()));
    }
}
