package com.vsms.authservice.dto.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagerCreatedEvent {
    private String managerName;
    private String email;
    private String username;
    private String temporaryPassword;
}
