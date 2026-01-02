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

    // 1. Create service request (Customer only) - returns just ID
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CreatedResponse> create(
            @Valid @RequestBody ServiceRequestCreateDTO dto) {
        ServiceRequestResponse response = service.createRequest(dto);
        return new ResponseEntity<>(
                CreatedResponse.builder().id(response.getId()).build(),
                HttpStatus.CREATED);
    }

    // 2. Get by ID (Any authenticated user)
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    // 3. Get by customer ID (Customer, Manager, Admin)
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getByCustomer(
            @PathVariable Integer customerId) {
        return ResponseEntity.ok(ApiResponse.success(service.getByCustomerId(customerId)));
    }

    // 4. Get vehicle service history (Customer, Manager, Admin)
    @GetMapping("/vehicle/{vehicleId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getByVehicle(
            @PathVariable Integer vehicleId) {
        return ResponseEntity.ok(ApiResponse.success(service.getByVehicleId(vehicleId)));
    }

    // 5. Get all (Manager, Admin only)
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getAll(
            @RequestParam(required = false) RequestStatus status) {
        return ResponseEntity.ok(ApiResponse.success(service.getAll(status)));
    }

    // 6. Get by technician ID (Technician, Manager, Admin)
    @GetMapping("/technician/{technicianId}")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getByTechnician(
            @PathVariable Integer technicianId,
            @RequestParam(required = false) RequestStatus status) {
        return ResponseEntity.ok(ApiResponse.success(service.getByTechnicianId(technicianId, status)));
    }

    // 7. Assign technician + bay (Manager only)
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> assign(
            @PathVariable Integer id,
            @Valid @RequestBody AssignTechnicianDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Technician assigned", service.assignTechnician(id, dto)));
    }

    // 8. Update status (Technician only)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> updateStatus(
            @PathVariable Integer id,
            @Valid @RequestBody StatusUpdateDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", service.updateStatus(id, dto)));
    }

    // 10. Set Pricing (Manager only)
    @PutMapping("/{id}/set-pricing")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> setPricing(
            @PathVariable Integer id,
            @RequestBody CompleteWorkDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Pricing set", service.setPricing(id, dto)));
    }

    // 11. Cancel request (Customer only)
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> cancel(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Request cancelled", service.cancelRequest(id)));
    }

    // 11. Get dashboard stats (Manager, Admin)
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStats>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(service.getStats()));
    }

    // 12. Get all bay status (Manager, Admin)
    @GetMapping("/bays")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<BayStatus>>> getAllBays() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllBayStatus()));
    }

    // 13. Get available bays (Manager, Admin)
    @GetMapping("/bays/available")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Integer>>> getAvailableBays() {
        return ResponseEntity.ok(ApiResponse.success(service.getAvailableBays()));
    }

    // 14. Get parts cost (Manager)
    @GetMapping("/{id}/parts-cost")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getPartsCost(@PathVariable Integer id) {
        return ResponseEntity
                .ok(ApiResponse.success("Parts cost from inventory", service.getPartsCostFromInventory(id)));
    }
}
