package com.vsms.servicerequestservice.service;

import com.vsms.servicerequestservice.client.BillingClient;
import com.vsms.servicerequestservice.dto.request.AssignTechnicianDTO;
import com.vsms.servicerequestservice.dto.request.ServiceNotesDTO;
import com.vsms.servicerequestservice.dto.request.ServiceRequestCreateDTO;
import com.vsms.servicerequestservice.dto.response.BayStatus;
import com.vsms.servicerequestservice.dto.response.DashboardStats;
import com.vsms.servicerequestservice.dto.response.ServiceRequestResponse;
import com.vsms.servicerequestservice.entity.ServiceRequest;
import com.vsms.servicerequestservice.enums.Priority;
import com.vsms.servicerequestservice.enums.RequestStatus;
import com.vsms.servicerequestservice.exception.BadRequestException;
import com.vsms.servicerequestservice.exception.ResourceNotFoundException;
import com.vsms.servicerequestservice.repository.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ServiceRequestService {

    private final ServiceRequestRepository repository;
    private final BillingClient billingClient;

    @Value("${service.total-bays:10}")
    private int totalBays;

    public ServiceRequestResponse createRequest(ServiceRequestCreateDTO dto) {
        ServiceRequest request = ServiceRequest.builder()
                .customerId(dto.getCustomerId())
                .vehicleId(dto.getVehicleId())
                .serviceType(dto.getServiceType())
                .description(dto.getDescription())
                .priority(dto.getPriority() != null ? dto.getPriority() : Priority.NORMAL)
                .status(RequestStatus.PENDING)
                .build();

        ServiceRequest saved = repository.save(request);
        log.info("Service request created: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public ServiceRequestResponse getById(Integer id) {
        return mapToResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getByCustomerId(Integer customerId) {
        return repository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getAll(RequestStatus status) {
        List<ServiceRequest> requests = (status != null)
                ? repository.findByStatus(status)
                : repository.findAll();
        return requests.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getPendingRequests() {
        return repository.findByStatus(RequestStatus.PENDING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getByTechnicianId(Integer technicianId, RequestStatus status) {
        List<ServiceRequest> requests = (status != null)
                ? repository.findByTechnicianIdAndStatus(technicianId, status)
                : repository.findByTechnicianId(technicianId);
        return requests.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ServiceRequestResponse assignTechnician(Integer id, AssignTechnicianDTO dto) {
        ServiceRequest request = findById(id);
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Can only assign technician to pending requests");
        }

        // Validate bay number
        if (dto.getBayNumber() < 1 || dto.getBayNumber() > totalBays) {
            throw new BadRequestException("Bay number must be between 1 and " + totalBays);
        }

        // Check if bay is available
        List<Integer> occupiedBays = repository.findOccupiedBays();
        if (occupiedBays.contains(dto.getBayNumber())) {
            throw new BadRequestException("Bay " + dto.getBayNumber() + " is already occupied");
        }

        request.setTechnicianId(dto.getTechnicianId());
        request.setBayNumber(dto.getBayNumber());
        request.setEstimatedCost(dto.getEstimatedCost());
        request.setStatus(RequestStatus.ASSIGNED);

        log.info("Technician {} assigned to request {} in bay {}", dto.getTechnicianId(), id, dto.getBayNumber());
        return mapToResponse(repository.save(request));
    }

    public ServiceRequestResponse startWork(Integer id) {
        ServiceRequest request = findById(id);
        if (request.getStatus() != RequestStatus.ASSIGNED) {
            throw new BadRequestException("Can only start work on assigned requests");
        }
        request.setStatus(RequestStatus.IN_PROGRESS);
        request.setStartedAt(LocalDateTime.now());
        log.info("Work started on request {}", id);
        return mapToResponse(repository.save(request));
    }

    public ServiceRequestResponse completeWork(Integer id) {
        ServiceRequest request = findById(id);
        if (request.getStatus() != RequestStatus.IN_PROGRESS) {
            throw new BadRequestException("Can only complete requests that are in progress");
        }
        request.setStatus(RequestStatus.COMPLETED);
        request.setCompletedAt(LocalDateTime.now());
        ServiceRequest savedRequest = repository.save(request);
        log.info("Request {} completed, bay {} now free", id, request.getBayNumber());

        // Auto-generate invoice
        try {
            billingClient.generateInvoice(id);
            log.info("Invoice auto-generated for service request {}", id);
        } catch (Exception e) {
            log.warn("Could not auto-generate invoice for request {}: {}", id, e.getMessage());
            // Don't fail the completion if billing service is unavailable
        }

        return mapToResponse(savedRequest);
    }

    public ServiceRequestResponse addNotes(Integer id, ServiceNotesDTO dto) {
        ServiceRequest request = findById(id);
        request.setServiceNotes(dto.getServiceNotes());
        if (dto.getFinalCost() != null) {
            request.setFinalCost(dto.getFinalCost());
        }
        return mapToResponse(repository.save(request));
    }

    public ServiceRequestResponse cancelRequest(Integer id) {
        ServiceRequest request = findById(id);
        if (request.getStatus() == RequestStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel completed requests");
        }
        request.setStatus(RequestStatus.CANCELLED);
        log.info("Request {} cancelled", id);
        return mapToResponse(repository.save(request));
    }

    @Transactional(readOnly = true)
    public Integer getCount(RequestStatus status) {
        if (status != null) {
            return (int) repository.countByStatus(status);
        }
        return (int) repository.count();
    }

    @Transactional(readOnly = true)
    public DashboardStats getStats() {
        return DashboardStats.builder()
                .totalRequests((int) repository.count())
                .pendingRequests((int) repository.countByStatus(RequestStatus.PENDING))
                .inProgressRequests((int) repository.countByStatus(RequestStatus.IN_PROGRESS))
                .completedRequests((int) repository.countByStatus(RequestStatus.COMPLETED))
                .cancelledRequests((int) repository.countByStatus(RequestStatus.CANCELLED))
                .build();
    }

    // Bay tracking methods
    @Transactional(readOnly = true)
    public List<BayStatus> getAllBayStatus() {
        List<Integer> occupiedBays = repository.findOccupiedBays();
        List<BayStatus> bayStatuses = new ArrayList<>();

        for (int i = 1; i <= totalBays; i++) {
            boolean occupied = occupiedBays.contains(i);
            Integer serviceRequestId = null;

            if (occupied) {
                var activeStatuses = List.of(RequestStatus.ASSIGNED, RequestStatus.IN_PROGRESS);
                var request = repository.findByBayNumberAndStatusIn(i, activeStatuses);
                serviceRequestId = request.map(ServiceRequest::getId).orElse(null);
            }

            bayStatuses.add(BayStatus.builder()
                    .bayNumber(i)
                    .occupied(occupied)
                    .serviceRequestId(serviceRequestId)
                    .build());
        }
        return bayStatuses;
    }

    @Transactional(readOnly = true)
    public List<Integer> getAvailableBays() {
        List<Integer> occupiedBays = repository.findOccupiedBays();
        List<Integer> availableBays = new ArrayList<>();
        for (int i = 1; i <= totalBays; i++) {
            if (!occupiedBays.contains(i)) {
                availableBays.add(i);
            }
        }
        return availableBays;
    }

    @Transactional(readOnly = true)
    public int getTotalBays() {
        return totalBays;
    }

    private ServiceRequest findById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceRequest", "id", id));
    }

    private ServiceRequestResponse mapToResponse(ServiceRequest request) {
        return ServiceRequestResponse.builder()
                .id(request.getId())
                .customerId(request.getCustomerId())
                .vehicleId(request.getVehicleId())
                .technicianId(request.getTechnicianId())
                .bayNumber(request.getBayNumber())
                .serviceType(request.getServiceType())
                .description(request.getDescription())
                .priority(request.getPriority())
                .status(request.getStatus())
                .serviceNotes(request.getServiceNotes())
                .estimatedCost(request.getEstimatedCost())
                .finalCost(request.getFinalCost())
                .scheduledDate(request.getScheduledDate())
                .startedAt(request.getStartedAt())
                .completedAt(request.getCompletedAt())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
