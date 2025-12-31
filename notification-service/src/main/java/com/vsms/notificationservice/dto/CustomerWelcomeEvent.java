package com.vsms.notificationservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerWelcomeEvent {
    private String customerName;
    private String email;
}
