package com.vsms.authservice.dto.event;

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
