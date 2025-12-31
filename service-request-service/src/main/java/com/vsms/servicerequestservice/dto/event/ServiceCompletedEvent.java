package com.vsms.servicerequestservice.dto.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceCompletedEvent {
    private String customerName;
    private String customerEmail;
    private String vehicleInfo;
    private String serviceName;
    private Long serviceRequestId;
}
