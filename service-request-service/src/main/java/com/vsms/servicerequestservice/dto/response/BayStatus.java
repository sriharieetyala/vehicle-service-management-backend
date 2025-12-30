package com.vsms.servicerequestservice.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BayStatus {
    private Integer bayNumber;
    private boolean occupied;
    private Integer serviceRequestId; // null if not occupied
}
