package com.vsms.inventoryservice.controller;

import com.vsms.inventoryservice.dto.request.PartRequestCreateDTO;
import com.vsms.inventoryservice.dto.response.ApiResponse;
import com.vsms.inventoryservice.dto.response.CreatedResponse;
import com.vsms.inventoryservice.dto.response.PartRequestResponse;
import com.vsms.inventoryservice.service.PartRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/part-requests")
@RequiredArgsConstructor
public class PartRequestController {

        private final PartRequestService partRequestService;

        // 6. Request parts for job (Technician only) - returns just ID
        @PostMapping
        @PreAuthorize("hasRole('TECHNICIAN')")
        public ResponseEntity<CreatedResponse> createRequest(
                        @Valid @RequestBody PartRequestCreateDTO dto) {
                PartRequestResponse response = partRequestService.createRequest(dto);
                return new ResponseEntity<>(
                                CreatedResponse.builder().id(response.getId()).build(),
                                HttpStatus.CREATED);
        }

        // 7. Get pending requests (Manager, Inventory Manager)
        @GetMapping("/pending")
        @PreAuthorize("hasAnyRole('MANAGER', 'INVENTORY_MANAGER', 'ADMIN')")
        public ResponseEntity<ApiResponse<List<PartRequestResponse>>> getPendingRequests() {
                return ResponseEntity.ok(ApiResponse.success(partRequestService.getPendingRequests()));
        }

        // 7. Approve request (Manager, Inventory Manager)
        @PutMapping("/{id}/approve")
        @PreAuthorize("hasAnyRole('MANAGER', 'INVENTORY_MANAGER', 'ADMIN')")
        public ResponseEntity<ApiResponse<PartRequestResponse>> approveRequest(
                        @PathVariable Integer id,
                        @RequestParam(required = false) Integer approvedBy) {
                return ResponseEntity.ok(
                                ApiResponse.success("Request approved, stock updated",
                                                partRequestService.approveRequest(id, approvedBy)));
        }

        // 8. Reject request (Manager, Inventory Manager)
        @PutMapping("/{id}/reject")
        @PreAuthorize("hasAnyRole('MANAGER', 'INVENTORY_MANAGER', 'ADMIN')")
        public ResponseEntity<ApiResponse<PartRequestResponse>> rejectRequest(
                        @PathVariable Integer id,
                        @RequestParam(required = false) Integer rejectedBy,
                        @RequestParam(required = false) String reason) {
                return ResponseEntity.ok(
                                ApiResponse.success("Request rejected",
                                                partRequestService.rejectRequest(id, rejectedBy, reason)));
        }

        // 10. Get total parts cost (Manager)
        @GetMapping("/service/{serviceRequestId}/total-cost")
        @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
        public ResponseEntity<ApiResponse<java.math.BigDecimal>> getTotalCostForService(
                        @PathVariable Integer serviceRequestId) {
                return ResponseEntity.ok(
                                ApiResponse.success(partRequestService.getTotalCostForService(serviceRequestId)));
        }
}
