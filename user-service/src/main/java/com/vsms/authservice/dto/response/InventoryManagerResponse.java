package com.vsms.authservice.dto.response;

import com.vsms.authservice.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryManagerResponse {

    private Integer id;
    private Integer userId;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String employeeId;
    private UserStatus status;
    private LocalDateTime createdAt;
}
