package com.vsms.servicerequestservice.controller;

import com.vsms.servicerequestservice.dto.request.AssignTechnicianDTO;
import com.vsms.servicerequestservice.dto.request.CompleteWorkDTO;
import com.vsms.servicerequestservice.dto.request.ServiceRequestCreateDTO;
import com.vsms.servicerequestservice.dto.request.StatusUpdateDTO;
import com.vsms.servicerequestservice.dto.response.ApiResponse;
import com.vsms.servicerequestservice.dto.response.BayStatus;
import com.vsms.servicerequestservice.dto.response.DashboardStats;
import com.vsms.servicerequestservice.dto.response.ServiceRequestResponse;
import com.vsms.servicerequestservice.enums.RequestStatus;
import com.vsms.servicerequestservice.service.ServiceRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-requests")
@RequiredArgsConstructor
public class ServiceRequestController {

    private final ServiceRequestService service;

    // 1. Create service request
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> create(
            @Valid @RequestBody ServiceRequestCreateDTO dto) {
        return new ResponseEntity<>(
                ApiResponse.success("Service request created", service.createRequest(dto)),
                HttpStatus.CREATED);
    }

    // 2. Get by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    // 3. Get by customer ID
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getByCustomer(
            @PathVariable Integer customerId) {
        return ResponseEntity.ok(ApiResponse.success(service.getByCustomerId(customerId)));
    }

    // 4. Get vehicle service history
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getByVehicle(
            @PathVariable Integer vehicleId) {
        return ResponseEntity.ok(ApiResponse.success(service.getByVehicleId(vehicleId)));
    }

    // 5. Get all (with optional status filter)
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getAll(
            @RequestParam(required = false) RequestStatus status) {
        return ResponseEntity.ok(ApiResponse.success(service.getAll(status)));
    }

    // 6. Get by technician ID
    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getByTechnician(
            @PathVariable Integer technicianId,
            @RequestParam(required = false) RequestStatus status) {
        return ResponseEntity.ok(ApiResponse.success(service.getByTechnicianId(technicianId, status)));
    }

    // 7. Assign technician + bay
    @PutMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> assign(
            @PathVariable Integer id,
            @Valid @RequestBody AssignTechnicianDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Technician assigned", service.assignTechnician(id, dto)));
    }

    // 8. Update status (Technician: IN_PROGRESS or COMPLETED)
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> updateStatus(
            @PathVariable Integer id,
            @Valid @RequestBody StatusUpdateDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", service.updateStatus(id, dto)));
    }

    // 10. Set Pricing (Manager only - sets parts + labor costs, generates invoice)
    @PutMapping("/{id}/set-pricing")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> setPricing(
            @PathVariable Integer id,
            @RequestBody CompleteWorkDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Pricing set", service.setPricing(id, dto)));
    }

    // 11. Cancel request
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> cancel(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Request cancelled", service.cancelRequest(id)));
    }

    // 11. Get dashboard stats
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStats>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(service.getStats()));
    }

    // 12. Get all bay status
    @GetMapping("/bays")
    public ResponseEntity<ApiResponse<List<BayStatus>>> getAllBays() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllBayStatus()));
    }

    // 13. Get available bays
    @GetMapping("/bays/available")
    public ResponseEntity<ApiResponse<List<Integer>>> getAvailableBays() {
        return ResponseEntity.ok(ApiResponse.success(service.getAvailableBays()));
    }

    // 14. Get parts cost for a service request (Manager - for billing)
    @GetMapping("/{id}/parts-cost")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getPartsCost(@PathVariable Integer id) {
        return ResponseEntity
                .ok(ApiResponse.success("Parts cost from inventory", service.getPartsCostFromInventory(id)));
    }
}
