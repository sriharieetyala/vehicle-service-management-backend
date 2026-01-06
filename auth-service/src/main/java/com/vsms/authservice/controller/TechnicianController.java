package com.vsms.authservice.controller;

import com.vsms.authservice.dto.request.TechnicianCreateRequest;
import com.vsms.authservice.dto.response.ApiResponse;
import com.vsms.authservice.dto.response.CreatedResponse;
import com.vsms.authservice.dto.response.TechnicianResponse;
import com.vsms.authservice.enums.Specialization;
import com.vsms.authservice.service.TechnicianService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TechnicianController handles technician registration and management
// This includes the approval workflow where admin reviews new technicians
@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
public class TechnicianController {

    private final TechnicianService technicianService;

    // Technicians can register but they stay in PENDING status until approved
    @PostMapping
    public ResponseEntity<CreatedResponse> createTechnician(
            @Valid @RequestBody TechnicianCreateRequest request) {
        TechnicianResponse response = technicianService.createTechnician(request);
        return new ResponseEntity<>(
                CreatedResponse.builder().id(response.getId()).build(),
                HttpStatus.CREATED);
    }

    // Get all technicians for manager to see the team
    @GetMapping
    public ResponseEntity<ApiResponse<List<TechnicianResponse>>> getAllTechnicians() {
        List<TechnicianResponse> technicians = technicianService.getAllTechnicians();
        return ResponseEntity.ok(ApiResponse.success(technicians));
    }

    // Get single technician by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TechnicianResponse>> getTechnicianById(@PathVariable Integer id) {
        TechnicianResponse response = technicianService.getTechnicianById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Get technicians who are on duty and have capacity for new tasks
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<TechnicianResponse>>> getAvailableTechnicians() {
        List<TechnicianResponse> technicians = technicianService.getAvailableTechnicians();
        return ResponseEntity.ok(ApiResponse.success(technicians));
    }

    // Filter technicians by their specialization for better task assignment
    @GetMapping("/by-specialization")
    public ResponseEntity<ApiResponse<List<TechnicianResponse>>> getBySpecialization(
            @RequestParam Specialization spec) {
        List<TechnicianResponse> technicians = technicianService.getAvailableBySpecialization(spec);
        return ResponseEntity.ok(ApiResponse.success(technicians));
    }

    // Get technicians waiting for admin approval
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<TechnicianResponse>>> getPendingTechnicians() {
        List<TechnicianResponse> technicians = technicianService.getPendingTechnicians();
        return ResponseEntity.ok(ApiResponse.success(technicians));
    }

    // Admin can approve or reject technician applications
    // I send email notifications for both outcomes
    @PutMapping("/{id}/review")
    public ResponseEntity<ApiResponse<TechnicianResponse>> reviewTechnician(
            @PathVariable Integer id,
            @RequestParam String action) {
        if ("APPROVE".equalsIgnoreCase(action)) {
            TechnicianResponse response = technicianService.approveTechnician(id);
            return ResponseEntity.ok(ApiResponse.success("Technician approved", response));
        } else if ("REJECT".equalsIgnoreCase(action)) {
            technicianService.rejectTechnician(id);
            return ResponseEntity.ok(ApiResponse.success("Technician rejected", null));
        } else {
            throw new IllegalArgumentException("Invalid action. Use APPROVE or REJECT");
        }
    }

    // Technicians can toggle their on duty status when starting or ending shift
    @PreAuthorize("#id == authentication.principal.id or hasRole('MANAGER')")
    @PutMapping("/{id}/duty")
    public ResponseEntity<ApiResponse<TechnicianResponse>> toggleDutyStatus(@PathVariable Integer id) {
        TechnicianResponse response = technicianService.toggleDutyStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Duty status toggled", response));
    }

    // Internal endpoint called by service request service to track workload
    @PutMapping("/{id}/workload")
    public ResponseEntity<ApiResponse<Void>> updateWorkload(
            @PathVariable Integer id,
            @RequestParam String action) {
        if ("INCREMENT".equalsIgnoreCase(action)) {
            technicianService.incrementWorkload(id);
            return ResponseEntity.ok(ApiResponse.success("Workload incremented", null));
        } else if ("DECREMENT".equalsIgnoreCase(action)) {
            technicianService.decrementWorkload(id);
            return ResponseEntity.ok(ApiResponse.success("Workload decremented", null));
        } else {
            throw new IllegalArgumentException("Invalid action. Use INCREMENT or DECREMENT");
        }
    }

    // Technicians can delete their own account
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTechnician(@PathVariable Integer id) {
        technicianService.deleteTechnician(id);
        return ResponseEntity.ok().build();
    }
}
