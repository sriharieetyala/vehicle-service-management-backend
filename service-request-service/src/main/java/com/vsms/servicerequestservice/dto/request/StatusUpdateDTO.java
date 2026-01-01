package com.vsms.servicerequestservice.dto.request;

import com.vsms.servicerequestservice.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusUpdateDTO {

    @NotNull(message = "Status is required")
    private RequestStatus status; // IN_PROGRESS or COMPLETED
}
