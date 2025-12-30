package com.vsms.servicerequestservice.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStats {
    private Integer totalRequests;
    private Integer pendingRequests;
    private Integer inProgressRequests;
    private Integer completedRequests;
    private Integer cancelledRequests;
}
