package com.vsms.servicerequestservice.controller;

import com.vsms.servicerequestservice.dto.request.AssignTechnicianDTO;
import com.vsms.servicerequestservice.dto.request.CompleteWorkDTO;
import com.vsms.servicerequestservice.dto.request.ServiceRequestCreateDTO;
import com.vsms.servicerequestservice.dto.request.StatusUpdateDTO;
import com.vsms.servicerequestservice.dto.response.ApiResponse;
import com.vsms.servicerequestservice.dto.response.BayStatus;
import com.vsms.servicerequestservice.dto.response.CreatedResponse;
import com.vsms.servicerequestservice.dto.response.DashboardStats;
import com.vsms.servicerequestservice.dto.response.ServiceRequestResponse;
import com.vsms.servicerequestservice.enums.RequestStatus;
import com.vsms.servicerequestservice.service.ServiceRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-requests")
@RequiredArgsConstructor
public class ServiceRequestController {

    private final ServiceRequestService service;

    // Create service request (role check done at gateway)
    @PostMapping
    public ResponseEntity<CreatedResponse> create(
            @Valid @RequestBody ServiceRequestCreateDTO dto) {
        ServiceRequestResponse response = service.createRequest(dto);
        return new ResponseEntity<>(
                CreatedResponse.builder().id(response.getId()).build(),
                HttpStatus.CREATED);
    }

    // Get by ID (role check done at gateway)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    // Get by customer ID - ownership check: customer can only see their own
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or #customerId == authentication.principal.id")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getByCustomer(
            @PathVariable Integer customerId) {
        return ResponseEntity.ok(ApiResponse.success(service.getByCustomerId(customerId)));
    }

    // Get vehicle service history (role check done at gateway)
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getByVehicle(
            @PathVariable Integer vehicleId) {
        return ResponseEntity.ok(ApiResponse.success(service.getByVehicleId(vehicleId)));
    }

    // Get all (role check done at gateway)
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getAll(
            @RequestParam(required = false) RequestStatus status) {
        return ResponseEntity.ok(ApiResponse.success(service.getAll(status)));
    }

    // Get by technician ID (role check done at gateway)
    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getByTechnician(
            @PathVariable Integer technicianId,
            @RequestParam(required = false) RequestStatus status) {
        return ResponseEntity.ok(ApiResponse.success(service.getByTechnicianId(technicianId, status)));
    }

    // Assign technician + bay (role check done at gateway)
    @PutMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> assign(
            @PathVariable Integer id,
            @Valid @RequestBody AssignTechnicianDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Technician assigned", service.assignTechnician(id, dto)));
    }

    // Update status (role check done at gateway)
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> updateStatus(
            @PathVariable Integer id,
            @Valid @RequestBody StatusUpdateDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", service.updateStatus(id, dto)));
    }

    // Set Pricing (role check done at gateway)
    @PutMapping("/{id}/set-pricing")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> setPricing(
            @PathVariable Integer id,
            @RequestBody CompleteWorkDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Pricing set", service.setPricing(id, dto)));
    }

    // Cancel request - ownership check: customer can only cancel their own
    @PutMapping("/{id}/cancel")
    @PreAuthorize("@serviceRequestService.isOwner(#id, authentication.principal.id)")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> cancel(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Request cancelled", service.cancelRequest(id)));
    }

    // Get dashboard stats (role check done at gateway)
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStats>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(service.getStats()));
    }

    // Get all bay status (role check done at gateway)
    @GetMapping("/bays")
    public ResponseEntity<ApiResponse<List<BayStatus>>> getAllBays() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllBayStatus()));
    }

    // Get available bays (role check done at gateway)
    @GetMapping("/bays/available")
    public ResponseEntity<ApiResponse<List<Integer>>> getAvailableBays() {
        return ResponseEntity.ok(ApiResponse.success(service.getAvailableBays()));
    }

    // Get parts cost (role check done at gateway)
    @GetMapping("/{id}/parts-cost")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getPartsCost(@PathVariable Integer id) {
        return ResponseEntity
                .ok(ApiResponse.success("Parts cost from inventory", service.getPartsCostFromInventory(id)));
    }
}
