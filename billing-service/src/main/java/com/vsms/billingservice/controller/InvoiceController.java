package com.vsms.billingservice.controller;

import com.vsms.billingservice.dto.request.PaymentDTO;
import com.vsms.billingservice.dto.response.ApiResponse;
import com.vsms.billingservice.dto.response.InvoiceResponse;
import com.vsms.billingservice.dto.response.RevenueStats;
import com.vsms.billingservice.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    // 1. Generate invoice for a completed service request
    @PostMapping("/generate/{serviceRequestId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> generateInvoice(
            @PathVariable Integer serviceRequestId) {
        return new ResponseEntity<>(
                ApiResponse.success("Invoice generated", invoiceService.generateInvoice(serviceRequestId)),
                HttpStatus.CREATED);
    }

    // 2. Get all invoices (Manager)
    @GetMapping
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getAllInvoices() {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getAllInvoices()));
    }

    // 3. Get my invoices (Customer)
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getMyInvoices(
            @RequestParam Integer customerId) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getMyInvoices(customerId)));
    }

    // 5. Get unpaid invoices (Manager)
    @GetMapping("/unpaid")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getUnpaidInvoices() {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getUnpaidInvoices()));
    }

    // 6. Pay invoice (Customer clicks PAY button)
    @PutMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<InvoiceResponse>> payInvoice(
            @PathVariable Integer id,
            @Valid @RequestBody PaymentDTO dto) {
        return ResponseEntity.ok(
                ApiResponse.success("Payment successful", invoiceService.payInvoice(id, dto)));
    }

    // 7. Get revenue stats (Manager)
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<RevenueStats>> getRevenueStats() {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getRevenueStats()));
    }
}
