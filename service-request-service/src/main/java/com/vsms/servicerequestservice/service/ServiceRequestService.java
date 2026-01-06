package com.vsms.servicerequestservice.service;

import com.vsms.servicerequestservice.client.AuthServiceClient;
import com.vsms.servicerequestservice.client.InventoryClient;
import com.vsms.servicerequestservice.client.VehicleClient;
import com.vsms.servicerequestservice.dto.request.AssignTechnicianDTO;
import com.vsms.servicerequestservice.dto.request.CompleteWorkDTO;
import com.vsms.servicerequestservice.dto.request.ServiceRequestCreateDTO;
import com.vsms.servicerequestservice.dto.request.StatusUpdateDTO;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ServiceRequestService {

    private final ServiceRequestRepository repository;
    private final InvoiceService invoiceService;
    private final VehicleClient vehicleClient;
    private final InventoryClient inventoryClient;
    private final AuthServiceClient authServiceClient;
    private final NotificationPublisher notificationPublisher;

    @Value("${service.total-bays:20}")
    private int totalBays;

    public ServiceRequestResponse createRequest(ServiceRequestCreateDTO dto) {
        // Validate vehicle exists and belongs to customer
        try {
            Map<String, Object> response = vehicleClient.getVehicleById(dto.getVehicleId());
            @SuppressWarnings("unchecked")
            Map<String, Object> vehicleData = (Map<String, Object>) response.get("data");

            if (vehicleData == null) {
                throw new ResourceNotFoundException("Vehicle", "id", dto.getVehicleId());
            }

            Integer vehicleCustomerId = (Integer) vehicleData.get("customerId");
            if (!dto.getCustomerId().equals(vehicleCustomerId)) {
                throw new BadRequestException("Vehicle does not belong to this customer");
            }
        } catch (ResourceNotFoundException | BadRequestException
                | com.vsms.servicerequestservice.exception.ServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Vehicle validation failed: {}", e.getMessage());
            throw new ResourceNotFoundException("Vehicle", "id", dto.getVehicleId());
        }

        // Validate pickup address if pickup required
        boolean pickupRequired = Boolean.TRUE.equals(dto.getPickupRequired());
        if (pickupRequired && (dto.getPickupAddress() == null || dto.getPickupAddress().isBlank())) {
            throw new BadRequestException("Pickup address is required when pickup is requested");
        }

        // Parse preferred date if provided
        LocalDateTime preferredDate = null;
        if (dto.getPreferredDate() != null && !dto.getPreferredDate().isBlank()) {
            preferredDate = LocalDateTime.parse(dto.getPreferredDate() + "T09:00:00");
        }

        ServiceRequest request = ServiceRequest.builder()
                .customerId(dto.getCustomerId())
                .vehicleId(dto.getVehicleId())
                .serviceType(dto.getServiceType())
                .description(dto.getDescription())
                .priority(dto.getPriority() != null ? dto.getPriority() : Priority.NORMAL)
                .pickupRequired(pickupRequired)
                .pickupAddress(dto.getPickupAddress())
                .preferredDate(preferredDate)
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
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getByVehicleId(Integer vehicleId) {
        return repository.findByVehicleId(vehicleId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getAll(RequestStatus status) {
        List<ServiceRequest> requests = (status != null)
                ? repository.findByStatus(status)
                : repository.findAll();
        return requests.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getPendingRequests() {
        return repository.findByStatus(RequestStatus.PENDING).stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Reschedule a service request - only for PENDING or ASSIGNED status
     */
    public ServiceRequestResponse reschedule(Integer id, String newDate) {
        ServiceRequest request = findById(id);
        if (request.getStatus() != RequestStatus.PENDING && request.getStatus() != RequestStatus.ASSIGNED) {
            throw new BadRequestException("Can only reschedule pending or assigned requests");
        }
        LocalDateTime preferredDate = LocalDateTime.parse(newDate + "T09:00:00");
        request.setPreferredDate(preferredDate);
        log.info("Service request {} rescheduled to {}", id, preferredDate);
        return mapToResponse(repository.save(request));
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getByTechnicianId(Integer technicianId, RequestStatus status) {
        List<ServiceRequest> requests = (status != null)
                ? repository.findByTechnicianIdAndStatus(technicianId, status)
                : repository.findByTechnicianId(technicianId);
        return requests.stream().map(this::mapToResponse).toList();
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
        request.setStatus(RequestStatus.ASSIGNED);

        // Auto-increment technician workload
        try {
            authServiceClient.updateWorkload(dto.getTechnicianId(), "INCREMENT");
            log.info("Technician {} workload incremented", dto.getTechnicianId());
        } catch (Exception e) {
            log.warn("Could not increment workload for technician {}: {}", dto.getTechnicianId(), e.getMessage());
        }

        log.info("Technician {} assigned to request {} in bay {}", dto.getTechnicianId(), id, dto.getBayNumber());
        return mapToResponse(repository.save(request));
    }

    /**
     * Consolidated status update - handles IN_PROGRESS, COMPLETED, and CLOSED
     */
    public ServiceRequestResponse updateStatus(Integer id, StatusUpdateDTO dto) {
        ServiceRequest request = findById(id);

        switch (dto.getStatus()) {
            case IN_PROGRESS:
                if (request.getStatus() != RequestStatus.ASSIGNED) {
                    throw new BadRequestException("Can only start work on assigned requests");
                }
                request.setStatus(RequestStatus.IN_PROGRESS);
                request.setStartedAt(LocalDateTime.now());
                log.info("Work started on request {}", id);
                break;

            case COMPLETED:
                if (request.getStatus() != RequestStatus.IN_PROGRESS) {
                    throw new BadRequestException("Can only complete requests that are in progress");
                }
                request.setStatus(RequestStatus.COMPLETED);
                request.setCompletedAt(LocalDateTime.now());
                log.info("Request {} completed by technician, bay {} now free", id, request.getBayNumber());

                // Auto-decrement technician workload
                try {
                    authServiceClient.updateWorkload(request.getTechnicianId(), "DECREMENT");
                    log.info("Technician {} workload decremented", request.getTechnicianId());
                } catch (Exception e) {
                    log.warn("Could not decrement workload for technician {}: {}", request.getTechnicianId(),
                            e.getMessage());
                }
                break;

            case CLOSED:
                if (request.getStatus() != RequestStatus.COMPLETED) {
                    throw new BadRequestException("Can only close completed requests");
                }
                request.setStatus(RequestStatus.CLOSED);
                log.info("Request {} closed after payment", id);
                break;

            default:
                throw new BadRequestException("Invalid status. Use IN_PROGRESS, COMPLETED, or CLOSED");
        }

        return mapToResponse(repository.save(request));
    }

    /**
     * Manager sets pricing (parts + labor) and triggers invoice generation
     */
    public ServiceRequestResponse setPricing(Integer id, CompleteWorkDTO dto) {
        ServiceRequest request = findById(id);
        if (request.getStatus() != RequestStatus.COMPLETED) {
            throw new BadRequestException("Can only set pricing for completed requests");
        }
        if (request.getFinalCost() != null && request.getFinalCost() > 0) {
            throw new BadRequestException("Pricing already set for this request");
        }

        // Calculate final cost = parts + labor
        Float partsCost = dto.getPartsCost() != null ? dto.getPartsCost() : 0f;
        Float laborCost = dto.getLaborCost() != null ? dto.getLaborCost() : 0f;
        Float totalCost = partsCost + laborCost;

        request.setFinalCost(totalCost);
        ServiceRequest savedRequest = repository.save(request);
        log.info("Manager set pricing for request {}: parts={}, labor={}, total={}", id, partsCost, laborCost,
                totalCost);

        // Auto-generate invoice after pricing is set
        try {
            invoiceService.generateInvoice(id);
            log.info("Invoice auto-generated for service request {}", id);
        } catch (Exception e) {
            log.warn("Could not auto-generate invoice for request {}: {}", id, e.getMessage());
        }

        // Send service completed notification
        try {
            String customerEmail = getCustomerEmail(request.getCustomerId());
            String customerName = getCustomerName(request.getCustomerId());
            String vehicleInfo = getVehicleInfo(request.getVehicleId());

            if (customerEmail != null) {
                log.info("Sending service completed notification to: {}", customerEmail);
                notificationPublisher.publishServiceCompleted(
                        customerName,
                        customerEmail,
                        vehicleInfo,
                        request.getServiceType().name(),
                        Long.valueOf(request.getId()));
            } else {
                log.warn("Customer email not found for customerId: {}. Service completed notification skipped.",
                        request.getCustomerId());
            }
        } catch (Exception e) {
            log.error("Could not send notification for request {}: {}", id, e.getMessage(), e);
        }

        return mapToResponse(savedRequest);
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

    /**
     * Get parts cost from inventory service for a service request (for billing)
     */
    @Transactional(readOnly = true)
    public java.math.BigDecimal getPartsCostFromInventory(Integer serviceRequestId) {
        try {
            var response = inventoryClient.getPartsCostForService(serviceRequestId);
            if (response != null && response.get("data") != null) {
                Object data = response.get("data");
                if (data instanceof Number) {
                    return new java.math.BigDecimal(data.toString());
                }
            }
            return java.math.BigDecimal.ZERO;
        } catch (Exception e) {
            log.warn("Could not fetch parts cost from inventory for request {}: {}", serviceRequestId, e.getMessage());
            return java.math.BigDecimal.ZERO;
        }
    }

    /**
     * Close request (called by billing service when payment is received)
     */
    public ServiceRequestResponse closeRequest(Integer id) {
        ServiceRequest request = findById(id);
        if (request.getStatus() != RequestStatus.COMPLETED) {
            throw new BadRequestException("Can only close completed requests");
        }
        request.setStatus(RequestStatus.CLOSED);
        log.info("Request {} closed after payment", id);
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
                .pickupRequired(request.getPickupRequired())
                .pickupAddress(request.getPickupAddress())
                .finalCost(request.getFinalCost())
                .startedAt(request.getStartedAt())
                .completedAt(request.getCompletedAt())
                .createdAt(request.getCreatedAt())
                .preferredDate(request.getPreferredDate())
                .build();
    }

    /**
     * Check if a customer owns a service request (for @PreAuthorize ownership
     * checks)
     */
    @Transactional(readOnly = true)
    public boolean isOwner(Integer serviceRequestId, Integer customerId) {
        return repository.findById(serviceRequestId)
                .map(sr -> sr.getCustomerId().equals(customerId))
                .orElse(false);
    }

    /**
     * Get customer email from auth-service
     */
    @SuppressWarnings("unchecked")
    private String getCustomerEmail(Integer customerId) {
        try {
            Map<String, Object> response = authServiceClient.getCustomerById(customerId);
            if (response != null && response.get("data") != null) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                return (String) data.get("email");
            }
        } catch (Exception e) {
            log.warn("Could not fetch customer email for {}: {}", customerId, e.getMessage());
        }
        return null;
    }

    /**
     * Get customer name from auth-service
     */
    @SuppressWarnings("unchecked")
    private String getCustomerName(Integer customerId) {
        try {
            Map<String, Object> response = authServiceClient.getCustomerById(customerId);
            if (response != null && response.get("data") != null) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                String firstName = (String) data.get("firstName");
                String lastName = (String) data.get("lastName");
                if (firstName != null && lastName != null) {
                    return firstName + " " + lastName;
                } else if (firstName != null) {
                    return firstName;
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch customer name for {}: {}", customerId, e.getMessage());
        }
        return "Customer";
    }

    /**
     * Get vehicle info from vehicle-service
     */
    @SuppressWarnings("unchecked")
    private String getVehicleInfo(Integer vehicleId) {
        try {
            Map<String, Object> response = vehicleClient.getVehicleById(vehicleId);
            if (response != null && response.get("data") != null) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                String brand = (String) data.get("brand");
                String model = (String) data.get("model");
                String plateNumber = (String) data.get("plateNumber");
                if (brand != null && model != null) {
                    return brand + " " + model + (plateNumber != null ? " (" + plateNumber + ")" : "");
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch vehicle info for {}: {}", vehicleId, e.getMessage());
        }
        return "Vehicle";
    }
}
