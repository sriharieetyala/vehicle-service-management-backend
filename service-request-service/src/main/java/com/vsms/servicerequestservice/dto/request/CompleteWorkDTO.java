package com.vsms.servicerequestservice.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteWorkDTO {

    private Float partsCost; // Cost of parts used
    private Float laborCost; // Optional: labor charge
}
