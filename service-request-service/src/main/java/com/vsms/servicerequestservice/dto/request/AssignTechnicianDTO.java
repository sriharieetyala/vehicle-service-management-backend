package com.vsms.servicerequestservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignTechnicianDTO {

    @NotNull(message = "Technician ID is required")
    private Integer technicianId;

    @NotNull(message = "Bay number is required")
    private Integer bayNumber;

    private Float estimatedCost;
}
