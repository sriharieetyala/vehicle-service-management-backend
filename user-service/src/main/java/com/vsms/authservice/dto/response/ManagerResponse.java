package com.vsms.authservice.dto.response;

import com.vsms.authservice.enums.Department;
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
public class ManagerResponse {

    private Integer id;
    private Integer userId;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String employeeId;
    private Department department; // Enum instead of String
    private UserStatus status;
    private LocalDateTime createdAt;
}
