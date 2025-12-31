package com.vsms.notificationservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceCompletedEvent {
    private Integer serviceRequestId;
    private String customerName;
    private String customerEmail;
    private String vehicleInfo;
    private String serviceName;
}
