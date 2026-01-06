package com.vsms.servicerequestservice.dto.response;

import com.vsms.servicerequestservice.enums.Priority;
import com.vsms.servicerequestservice.enums.RequestStatus;
import com.vsms.servicerequestservice.enums.ServiceType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestResponse {
    private Integer id;
    private Integer customerId;
    private Integer vehicleId;
    private Integer technicianId;
    private Integer bayNumber;
    private ServiceType serviceType;
    private String description;
    private Priority priority;
    private RequestStatus status;
    private Boolean pickupRequired;
    private String pickupAddress;
    private Float laborCost;
    private Float partsCost;
    private Float finalCost;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime preferredDate;
}
