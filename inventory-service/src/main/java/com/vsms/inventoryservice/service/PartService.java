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

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PartService {

    private final PartRepository repository;

    public PartResponse createPart(PartCreateDTO dto) {
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

    @Transactional(readOnly = true)
    public List<PartResponse> getAllParts(PartCategory category) {
        List<Part> parts = (category != null)
                ? repository.findByCategory(category)
                : repository.findAll();
        return parts.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PartResponse getPartById(Integer id) {
        return mapToResponse(findById(id));
    }

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

    @Transactional(readOnly = true)
    public List<PartResponse> getLowStockParts() {
        return repository.findLowStockParts().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Internal method for PartRequestService to reduce stock
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

    Part findById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Part", "id", id));
    }

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
