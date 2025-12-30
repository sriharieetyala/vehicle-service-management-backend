package com.vsms.servicerequestservice.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceNotesDTO {
    private String serviceNotes;
    private Float finalCost;
}
