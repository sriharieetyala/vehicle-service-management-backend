package com.vsms.servicerequestservice.controller;

import com.vsms.servicerequestservice.dto.request.AssignTechnicianDTO;
import com.vsms.servicerequestservice.dto.request.ServiceNotesDTO;
import com.vsms.servicerequestservice.dto.request.ServiceRequestCreateDTO;
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

    // 4. Get all (with optional status filter)
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getAll(
            @RequestParam(required = false) RequestStatus status) {
        return ResponseEntity.ok(ApiResponse.success(service.getAll(status)));
    }

    // 5. Get pending requests
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getPending() {
        return ResponseEntity.ok(ApiResponse.success(service.getPendingRequests()));
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

    // 8. Start work
    @PutMapping("/{id}/start")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> start(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Work started", service.startWork(id)));
    }

    // 9. Complete work
    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> complete(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Work completed", service.completeWork(id)));
    }

    // 10. Add notes
    @PutMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> addNotes(
            @PathVariable Integer id,
            @RequestBody ServiceNotesDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Notes added", service.addNotes(id, dto)));
    }

    // 11. Cancel request
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> cancel(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Request cancelled", service.cancelRequest(id)));
    }

    // 12. Get count
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getCount(
            @RequestParam(required = false) RequestStatus status) {
        return ResponseEntity.ok(ApiResponse.success(service.getCount(status)));
    }

    // 13. Get dashboard stats
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStats>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(service.getStats()));
    }

    // 14. Get all bay status
    @GetMapping("/bays")
    public ResponseEntity<ApiResponse<List<BayStatus>>> getAllBays() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllBayStatus()));
    }

    // 15. Get available bays
    @GetMapping("/bays/available")
    public ResponseEntity<ApiResponse<List<Integer>>> getAvailableBays() {
        return ResponseEntity.ok(ApiResponse.success(service.getAvailableBays()));
    }

    // 16. Get total bays count
    @GetMapping("/bays/total")
    public ResponseEntity<ApiResponse<Integer>> getTotalBays() {
        return ResponseEntity.ok(ApiResponse.success(service.getTotalBays()));
    }
}
