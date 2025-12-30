package com.vsms.inventoryservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartRequestCreateDTO {

    @NotNull(message = "Part ID is required")
    private Integer partId;

    @NotNull(message = "Service Request ID is required")
    private Integer serviceRequestId;

    @NotNull(message = "Technician ID is required")
    private Integer technicianId;

    @NotNull(message = "Requested quantity is required")
    @Min(value = 1, message = "Requested quantity must be at least 1")
    private Integer requestedQuantity;

    private String notes;
}
