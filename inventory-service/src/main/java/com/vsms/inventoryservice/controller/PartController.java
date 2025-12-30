package com.vsms.inventoryservice.controller;

import com.vsms.inventoryservice.dto.request.PartCreateDTO;
import com.vsms.inventoryservice.dto.request.PartUpdateDTO;
import com.vsms.inventoryservice.dto.response.ApiResponse;
import com.vsms.inventoryservice.dto.response.PartResponse;
import com.vsms.inventoryservice.enums.PartCategory;
import com.vsms.inventoryservice.service.PartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
public class PartController {

    private final PartService partService;

    // 1. Add new part
    @PostMapping
    public ResponseEntity<ApiResponse<PartResponse>> createPart(
            @Valid @RequestBody PartCreateDTO dto) {
        return new ResponseEntity<>(
                ApiResponse.success("Part created successfully", partService.createPart(dto)),
                HttpStatus.CREATED);
    }

    // 2. Get all parts (with optional category filter)
    @GetMapping
    public ResponseEntity<ApiResponse<List<PartResponse>>> getAllParts(
            @RequestParam(required = false) PartCategory category) {
        return ResponseEntity.ok(ApiResponse.success(partService.getAllParts(category)));
    }

    // 3. Get part by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartResponse>> getPartById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(partService.getPartById(id)));
    }

    // 4. Update part
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PartResponse>> updatePart(
            @PathVariable Integer id,
            @Valid @RequestBody PartUpdateDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Part updated successfully", partService.updatePart(id, dto)));
    }

    // 5. Get low stock parts
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<PartResponse>>> getLowStockParts() {
        return ResponseEntity.ok(ApiResponse.success(partService.getLowStockParts()));
    }
}
