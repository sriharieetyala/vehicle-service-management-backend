package com.vsms.authservice.controller;

import com.vsms.authservice.dto.request.InventoryManagerCreateRequest;
import com.vsms.authservice.dto.request.InventoryManagerUpdateRequest;
import com.vsms.authservice.dto.response.ApiResponse;
import com.vsms.authservice.dto.response.InventoryManagerResponse;
import com.vsms.authservice.service.InventoryManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory-managers")
@RequiredArgsConstructor
public class InventoryManagerController {

    private final InventoryManagerService inventoryManagerService;

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryManagerResponse>> createInventoryManager(
            @Valid @RequestBody InventoryManagerCreateRequest request) {
        InventoryManagerResponse response = inventoryManagerService.createInventoryManager(request);
        return new ResponseEntity<>(
                ApiResponse.success("Inventory Manager created successfully", response),
                HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryManagerResponse>>> getAllInventoryManagers() {
        List<InventoryManagerResponse> managers = inventoryManagerService.getAllInventoryManagers();
        return ResponseEntity.ok(ApiResponse.success(managers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryManagerResponse>> getInventoryManagerById(@PathVariable Integer id) {
        InventoryManagerResponse response = inventoryManagerService.getInventoryManagerById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryManagerResponse>> updateInventoryManager(
            @PathVariable Integer id,
            @Valid @RequestBody InventoryManagerUpdateRequest request) {
        InventoryManagerResponse response = inventoryManagerService.updateInventoryManager(id, request);
        return ResponseEntity.ok(ApiResponse.success("Inventory Manager updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInventoryManager(@PathVariable Integer id) {
        inventoryManagerService.deleteInventoryManager(id);
        return ResponseEntity.ok(ApiResponse.success("Inventory Manager deactivated", null));
    }
}
