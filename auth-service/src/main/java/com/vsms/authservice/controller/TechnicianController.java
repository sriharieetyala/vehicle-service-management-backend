package com.vsms.authservice.controller;

import com.vsms.authservice.dto.request.TechnicianCreateRequest;
import com.vsms.authservice.dto.response.ApiResponse;
import com.vsms.authservice.dto.response.TechnicianResponse;
import com.vsms.authservice.enums.Specialization;
import com.vsms.authservice.service.TechnicianService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
public class TechnicianController {

    private final TechnicianService technicianService;

    @PostMapping
    public ResponseEntity<ApiResponse<TechnicianResponse>> createTechnician(
            @Valid @RequestBody TechnicianCreateRequest request) {
        TechnicianResponse response = technicianService.createTechnician(request);
        return new ResponseEntity<>(
                ApiResponse.success("Technician registration submitted, awaiting approval", response),
                HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TechnicianResponse>>> getAllTechnicians() {
        List<TechnicianResponse> technicians = technicianService.getAllTechnicians();
        return ResponseEntity.ok(ApiResponse.success(technicians));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TechnicianResponse>> getTechnicianById(@PathVariable Integer id) {
        TechnicianResponse response = technicianService.getTechnicianById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<TechnicianResponse>>> getAvailableTechnicians() {
        List<TechnicianResponse> technicians = technicianService.getAvailableTechnicians();
        return ResponseEntity.ok(ApiResponse.success(technicians));
    }

    @GetMapping("/by-specialization")
    public ResponseEntity<ApiResponse<List<TechnicianResponse>>> getBySpecialization(
            @RequestParam Specialization spec) {
        List<TechnicianResponse> technicians = technicianService.getAvailableBySpecialization(spec);
        return ResponseEntity.ok(ApiResponse.success(technicians));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<TechnicianResponse>>> getPendingTechnicians() {
        List<TechnicianResponse> technicians = technicianService.getPendingTechnicians();
        return ResponseEntity.ok(ApiResponse.success(technicians));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<TechnicianResponse>> approveTechnician(@PathVariable Integer id) {
        TechnicianResponse response = technicianService.approveTechnician(id);
        return ResponseEntity.ok(ApiResponse.success("Technician approved successfully", response));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectTechnician(@PathVariable Integer id) {
        technicianService.rejectTechnician(id);
        return ResponseEntity.ok(ApiResponse.success("Technician rejected", null));
    }

    @PutMapping("/{id}/duty")
    public ResponseEntity<ApiResponse<TechnicianResponse>> toggleDutyStatus(@PathVariable Integer id) {
        TechnicianResponse response = technicianService.toggleDutyStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Duty status toggled", response));
    }

    @PutMapping("/{id}/workload/increment")
    public ResponseEntity<ApiResponse<Void>> incrementWorkload(@PathVariable Integer id) {
        technicianService.incrementWorkload(id);
        return ResponseEntity.ok(ApiResponse.success("Workload incremented", null));
    }

    @PutMapping("/{id}/workload/decrement")
    public ResponseEntity<ApiResponse<Void>> decrementWorkload(@PathVariable Integer id) {
        technicianService.decrementWorkload(id);
        return ResponseEntity.ok(ApiResponse.success("Workload decremented", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTechnician(@PathVariable Integer id) {
        technicianService.deleteTechnician(id);
        return ResponseEntity.ok(ApiResponse.success("Technician deactivated", null));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getTechnicianCount() {
        long count = technicianService.getTechnicianCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
