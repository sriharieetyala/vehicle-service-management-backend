package com.vsms.inventoryservice.service;

import com.vsms.inventoryservice.dto.request.PartCreateDTO;
import com.vsms.inventoryservice.dto.request.PartUpdateDTO;
import com.vsms.inventoryservice.dto.response.PartResponse;
import com.vsms.inventoryservice.entity.Part;
import com.vsms.inventoryservice.enums.PartCategory;
import com.vsms.inventoryservice.exception.BadRequestException;
import com.vsms.inventoryservice.exception.ResourceNotFoundException;
import com.vsms.inventoryservice.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// PartService handles inventory parts management
// Inventory managers use this to add new parts and update stock levels
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PartService {

    private final PartRepository repository;

    // Add a new part to the inventory catalog
    public PartResponse createPart(PartCreateDTO dto) {
        // Check for duplicate part numbers
        if (repository.existsByPartNumber(dto.getPartNumber())) {
            throw new BadRequestException("Part with number '" + dto.getPartNumber() + "' already exists");
        }

        Part part = Part.builder()
                .partNumber(dto.getPartNumber())
                .name(dto.getName())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .quantity(dto.getQuantity())
                .unitPrice(dto.getUnitPrice())
                .reorderLevel(dto.getReorderLevel())
                .build();

        Part saved = repository.save(part);
        log.info("Part created: {} - {}", saved.getPartNumber(), saved.getName());
        return mapToResponse(saved);
    }

    // Get all parts with optional category filter
    @Transactional(readOnly = true)
    public List<PartResponse> getAllParts(PartCategory category) {
        List<Part> parts = (category != null)
                ? repository.findByCategory(category)
                : repository.findAll();
        return parts.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // Get a single part by ID
    @Transactional(readOnly = true)
    public PartResponse getPartById(Integer id) {
        return mapToResponse(findById(id));
    }

    // Update part details like stock quantity and price
    // Only updates fields that are provided in the request
    public PartResponse updatePart(Integer id, PartUpdateDTO dto) {
        Part part = findById(id);

        if (dto.getName() != null) {
            part.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            part.setDescription(dto.getDescription());
        }
        if (dto.getCategory() != null) {
            part.setCategory(dto.getCategory());
        }
        if (dto.getQuantity() != null) {
            part.setQuantity(dto.getQuantity());
        }
        if (dto.getUnitPrice() != null) {
            part.setUnitPrice(dto.getUnitPrice());
        }
        if (dto.getReorderLevel() != null) {
            part.setReorderLevel(dto.getReorderLevel());
        }

        Part updated = repository.save(part);
        log.info("Part updated: {}", updated.getPartNumber());
        return mapToResponse(updated);
    }

    // Get parts that are below reorder level for restocking alerts
    @Transactional(readOnly = true)
    public List<PartResponse> getLowStockParts() {
        return repository.findLowStockParts().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Called by PartRequestService when a request is approved
    // Reduces the stock by the requested quantity
    public void reduceStock(Integer partId, int quantity) {
        Part part = findById(partId);
        int newQuantity = part.getQuantity() - quantity;
        if (newQuantity < 0) {
            throw new BadRequestException("Cannot reduce stock below 0");
        }
        part.setQuantity(newQuantity);
        repository.save(part);
        log.info("Stock reduced for part {}: {} -> {}", part.getPartNumber(), part.getQuantity() + quantity,
                newQuantity);
    }

    // Helper method to find part by ID or throw exception
    Part findById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Part", "id", id));
    }

    // Maps Part entity to response DTO
    private PartResponse mapToResponse(Part part) {
        return PartResponse.builder()
                .id(part.getId())
                .partNumber(part.getPartNumber())
                .name(part.getName())
                .description(part.getDescription())
                .category(part.getCategory())
                .quantity(part.getQuantity())
                .unitPrice(part.getUnitPrice())
                .reorderLevel(part.getReorderLevel())
                .lowStock(part.getQuantity() <= part.getReorderLevel())
                .createdAt(part.getCreatedAt())
                .updatedAt(part.getUpdatedAt())
                .build();
    }
}
