package com.vsms.notificationservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianRejectedEvent {
    private String technicianName;
    private String email;
    private String reason;
}
