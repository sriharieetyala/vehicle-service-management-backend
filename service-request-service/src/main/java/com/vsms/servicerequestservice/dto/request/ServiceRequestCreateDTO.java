package com.vsms.servicerequestservice.dto.request;

import com.vsms.servicerequestservice.enums.Priority;
import com.vsms.servicerequestservice.enums.ServiceType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestCreateDTO {

    @NotNull(message = "Customer ID is required")
    private Integer customerId;

    @NotNull(message = "Vehicle ID is required")
    private Integer vehicleId;

    @NotNull(message = "Service type is required")
    private ServiceType serviceType;

    private String description;

    private Priority priority; // Default to NORMAL if not provided

    private Boolean pickupRequired; // true = technician picks up, false = customer drops off

    private String pickupAddress; // Required when pickupRequired = true
}
