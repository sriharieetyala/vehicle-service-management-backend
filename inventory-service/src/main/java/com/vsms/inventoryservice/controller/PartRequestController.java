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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/part-requests")
@RequiredArgsConstructor
public class PartRequestController {

        private final PartRequestService partRequestService;

        // Request parts for job (role check done at gateway)
        @PostMapping
        public ResponseEntity<CreatedResponse> createRequest(
                        @Valid @RequestBody PartRequestCreateDTO dto) {
                PartRequestResponse response = partRequestService.createRequest(dto);
                return new ResponseEntity<>(
                                CreatedResponse.builder().id(response.getId()).build(),
                                HttpStatus.CREATED);
        }

        // Get pending requests (role check done at gateway)
        @GetMapping("/pending")
        public ResponseEntity<ApiResponse<List<PartRequestResponse>>> getPendingRequests() {
                return ResponseEntity.ok(ApiResponse.success(partRequestService.getPendingRequests()));
        }

        // Approve request (role check done at gateway)
        @PutMapping("/{id}/approve")
        public ResponseEntity<ApiResponse<PartRequestResponse>> approveRequest(
                        @PathVariable Integer id,
                        @RequestParam(required = false) Integer approvedBy) {
                return ResponseEntity.ok(
                                ApiResponse.success("Request approved, stock updated",
                                                partRequestService.approveRequest(id, approvedBy)));
        }

        // Reject request (role check done at gateway)
        @PutMapping("/{id}/reject")
        public ResponseEntity<ApiResponse<PartRequestResponse>> rejectRequest(
                        @PathVariable Integer id,
                        @RequestParam(required = false) Integer rejectedBy,
                        @RequestParam(required = false) String reason) {
                return ResponseEntity.ok(
                                ApiResponse.success("Request rejected",
                                                partRequestService.rejectRequest(id, rejectedBy, reason)));
        }

        // Get total parts cost (role check done at gateway)
        @GetMapping("/service/{serviceRequestId}/total-cost")
        public ResponseEntity<ApiResponse<java.math.BigDecimal>> getTotalCostForService(
                        @PathVariable Integer serviceRequestId) {
                return ResponseEntity.ok(
                                ApiResponse.success(partRequestService.getTotalCostForService(serviceRequestId)));
        }

        // Get requests by technician (role check done at gateway)
        @GetMapping("/technician/{technicianId}")
        public ResponseEntity<ApiResponse<List<PartRequestResponse>>> getByTechnician(
                        @PathVariable Integer technicianId) {
                return ResponseEntity.ok(
                                ApiResponse.success(partRequestService.getByTechnicianId(technicianId)));
        }
}
