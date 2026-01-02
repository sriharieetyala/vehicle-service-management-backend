package com.vsms.inventoryservice.controller;

import com.vsms.inventoryservice.dto.request.PartCreateDTO;
import com.vsms.inventoryservice.dto.request.PartUpdateDTO;
import com.vsms.inventoryservice.dto.response.ApiResponse;
import com.vsms.inventoryservice.dto.response.CreatedResponse;
import com.vsms.inventoryservice.dto.response.PartResponse;
import com.vsms.inventoryservice.enums.PartCategory;
import com.vsms.inventoryservice.service.PartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
public class PartController {

    private final PartService partService;

    // 1. Add new part (Manager/Inventory Manager only) - returns just ID
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'INVENTORY_MANAGER', 'ADMIN')")
    public ResponseEntity<CreatedResponse> createPart(
            @Valid @RequestBody PartCreateDTO dto) {
        PartResponse response = partService.createPart(dto);
        return new ResponseEntity<>(
                CreatedResponse.builder().id(response.getId()).build(),
                HttpStatus.CREATED);
    }

    // 2. Get all parts (Manager, Inventory Manager, Technician)
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'INVENTORY_MANAGER', 'TECHNICIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<PartResponse>>> getAllParts(
            @RequestParam(required = false) PartCategory category) {
        return ResponseEntity.ok(ApiResponse.success(partService.getAllParts(category)));
    }

    // 3. Get low stock parts (Manager, Inventory Manager)
    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('MANAGER', 'INVENTORY_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<PartResponse>>> getLowStockParts() {
        return ResponseEntity.ok(ApiResponse.success(partService.getLowStockParts()));
    }
}
