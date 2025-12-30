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

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PartRequestService {

    private final PartRequestRepository repository;
    private final PartService partService;

    public PartRequestResponse createRequest(PartRequestCreateDTO dto) {
        // Validate part exists
        Part part = partService.findById(dto.getPartId());

        // Check if sufficient stock is available
        if (part.getQuantity() < dto.getRequestedQuantity()) {
            throw new InsufficientStockException(part.getPartNumber(), part.getQuantity(), dto.getRequestedQuantity());
        }

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

    @Transactional(readOnly = true)
    public List<PartRequestResponse> getPendingRequests() {
        return repository.findByStatus(RequestStatus.PENDING).stream()
                .map(req -> {
                    Part part = partService.findById(req.getPartId());
                    return mapToResponse(req, part);
                })
                .collect(Collectors.toList());
    }

    public PartRequestResponse approveRequest(Integer id, Integer approvedBy) {
        PartRequest request = findById(id);

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Can only approve pending requests");
        }

        Part part = partService.findById(request.getPartId());

        // Check stock availability
        if (part.getQuantity() < request.getRequestedQuantity()) {
            throw new InsufficientStockException(part.getPartNumber(), part.getQuantity(),
                    request.getRequestedQuantity());
        }

        // Reduce stock
        partService.reduceStock(request.getPartId(), request.getRequestedQuantity());

        // Update request status
        request.setStatus(RequestStatus.APPROVED);
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessedBy(approvedBy);

        PartRequest updated = repository.save(request);
        log.info("Part request {} approved. Stock reduced by {}", id, request.getRequestedQuantity());

        // Fetch updated part for response
        Part updatedPart = partService.findById(request.getPartId());
        return mapToResponse(updated, updatedPart);
    }

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

    private PartRequest findById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PartRequest", "id", id));
    }

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
}
