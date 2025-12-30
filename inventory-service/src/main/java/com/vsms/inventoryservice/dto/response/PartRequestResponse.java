package com.vsms.inventoryservice.dto.response;

import com.vsms.inventoryservice.enums.RequestStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartRequestResponse {

    private Integer id;
    private Integer partId;
    private String partNumber;
    private String partName;
    private Integer serviceRequestId;
    private Integer technicianId;
    private Integer requestedQuantity;
    private RequestStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private Integer processedBy;
}
