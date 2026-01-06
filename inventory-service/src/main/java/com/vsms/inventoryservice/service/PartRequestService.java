package com.vsms.inventoryservice.service;

import com.vsms.inventoryservice.dto.request.PartRequestCreateDTO;
import com.vsms.inventoryservice.dto.response.PartRequestResponse;
import com.vsms.inventoryservice.entity.Part;
import com.vsms.inventoryservice.entity.PartRequest;
import com.vsms.inventoryservice.enums.RequestStatus;
import com.vsms.inventoryservice.exception.BadRequestException;
import com.vsms.inventoryservice.exception.InsufficientStockException;
import com.vsms.inventoryservice.exception.ResourceNotFoundException;
import com.vsms.inventoryservice.repository.PartRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// PartRequestService handles part requests from technicians
// Technicians request parts for jobs and inventory managers approve or reject
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PartRequestService {

    private final PartRequestRepository repository;
    private final PartService partService;

    // Technicians submit requests for parts needed for a service job
    // Stock check is not done here, inventory manager will check when approving
    public PartRequestResponse createRequest(PartRequestCreateDTO dto) {
        Part part = partService.findById(dto.getPartId());

        PartRequest request = PartRequest.builder()
                .partId(dto.getPartId())
                .serviceRequestId(dto.getServiceRequestId())
                .technicianId(dto.getTechnicianId())
                .requestedQuantity(dto.getRequestedQuantity())
                .notes(dto.getNotes())
                .status(RequestStatus.PENDING)
                .build();

        PartRequest saved = repository.save(request);
        log.info("Part request created: {} for {} units of part {}",
                saved.getId(), dto.getRequestedQuantity(), part.getPartNumber());
        return mapToResponse(saved, part);
    }

    // Get all pending requests for inventory manager to review
    @Transactional(readOnly = true)
    public List<PartRequestResponse> getPendingRequests() {
        return repository.findByStatus(RequestStatus.PENDING).stream()
                .map(req -> {
                    Part part = partService.findById(req.getPartId());
                    return mapToResponse(req, part);
                })
                .collect(Collectors.toList());
    }

    // Inventory manager approves the part request
    // This checks stock availability and deducts the quantity
    public PartRequestResponse approveRequest(Integer id, Integer approvedBy) {
        PartRequest request = findById(id);

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Can only approve pending requests");
        }

        Part part = partService.findById(request.getPartId());

        // Check if we have enough stock to fulfill the request
        if (part.getQuantity() < request.getRequestedQuantity()) {
            throw new InsufficientStockException(part.getPartNumber(), part.getQuantity(),
                    request.getRequestedQuantity());
        }

        // Reduce the stock by the requested amount
        partService.reduceStock(request.getPartId(), request.getRequestedQuantity());

        request.setStatus(RequestStatus.APPROVED);
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessedBy(approvedBy);

        PartRequest updated = repository.save(request);
        log.info("Part request {} approved. Stock reduced by {}", id, request.getRequestedQuantity());

        Part updatedPart = partService.findById(request.getPartId());
        return mapToResponse(updated, updatedPart);
    }

    // Inventory manager rejects the part request with optional reason
    public PartRequestResponse rejectRequest(Integer id, Integer rejectedBy, String reason) {
        PartRequest request = findById(id);

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Can only reject pending requests");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessedBy(rejectedBy);
        if (reason != null && !reason.isBlank()) {
            request.setNotes(request.getNotes() != null
                    ? request.getNotes() + " | Rejected: " + reason
                    : "Rejected: " + reason);
        }

        PartRequest updated = repository.save(request);
        log.info("Part request {} rejected", id);

        Part part = partService.findById(request.getPartId());
        return mapToResponse(updated, part);
    }

    // Helper method to find request by ID or throw exception
    private PartRequest findById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PartRequest", "id", id));
    }

    // Maps PartRequest entity to response DTO
    private PartRequestResponse mapToResponse(PartRequest request, Part part) {
        return PartRequestResponse.builder()
                .id(request.getId())
                .partId(request.getPartId())
                .partNumber(part.getPartNumber())
                .partName(part.getName())
                .serviceRequestId(request.getServiceRequestId())
                .technicianId(request.getTechnicianId())
                .requestedQuantity(request.getRequestedQuantity())
                .status(request.getStatus())
                .notes(request.getNotes())
                .createdAt(request.getCreatedAt())
                .processedAt(request.getProcessedAt())
                .processedBy(request.getProcessedBy())
                .build();
    }

    // Calculate total parts cost for a service request for invoice
    @Transactional(readOnly = true)
    public java.math.BigDecimal getTotalCostForService(Integer serviceRequestId) {
        List<PartRequest> approvedRequests = repository.findByServiceRequestId(serviceRequestId)
                .stream()
                .filter(req -> req.getStatus() == RequestStatus.APPROVED)
                .collect(Collectors.toList());

        return approvedRequests.stream()
                .map(req -> {
                    Part part = partService.findById(req.getPartId());
                    return part.getUnitPrice().multiply(java.math.BigDecimal.valueOf(req.getRequestedQuantity()));
                })
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    // Get all part requests made by a specific technician
    @Transactional(readOnly = true)
    public List<PartRequestResponse> getByTechnicianId(Integer technicianId) {
        return repository.findByTechnicianId(technicianId).stream()
                .map(req -> {
                    Part part = partService.findById(req.getPartId());
                    return mapToResponse(req, part);
                })
                .collect(Collectors.toList());
    }
}
