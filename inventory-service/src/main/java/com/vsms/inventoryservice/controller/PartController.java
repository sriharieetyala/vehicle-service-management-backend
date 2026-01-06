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
import org.springframework.web.bind.annotation.*;

import java.util.List;

// PartController handles inventory parts management
// Inventory managers can add new parts and update stock levels
@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
public class PartController {

    private final PartService partService;

    // Add a new part to the inventory catalog
    @PostMapping
    public ResponseEntity<CreatedResponse> createPart(
            @Valid @RequestBody PartCreateDTO dto) {
        PartResponse response = partService.createPart(dto);
        return new ResponseEntity<>(
                CreatedResponse.builder().id(response.getId()).build(),
                HttpStatus.CREATED);
    }

    // Get all parts with optional category filter
    @GetMapping
    public ResponseEntity<ApiResponse<List<PartResponse>>> getAllParts(
            @RequestParam(required = false) PartCategory category) {
        return ResponseEntity.ok(ApiResponse.success(partService.getAllParts(category)));
    }

    // Get parts that are below reorder level for restocking
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<PartResponse>>> getLowStockParts() {
        return ResponseEntity.ok(ApiResponse.success(partService.getLowStockParts()));
    }

    // Update part details like stock quantity and price
    // Used by inventory manager when restocking parts
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PartResponse>> updatePart(
            @PathVariable Integer id,
            @Valid @RequestBody PartUpdateDTO dto) {
        PartResponse response = partService.updatePart(id, dto);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Get a single part by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartResponse>> getPartById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(partService.getPartById(id)));
    }
}
