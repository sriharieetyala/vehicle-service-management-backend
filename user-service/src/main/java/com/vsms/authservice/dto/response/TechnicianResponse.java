package com.vsms.authservice.dto.response;

import com.vsms.authservice.enums.Specialization;
import com.vsms.authservice.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianResponse {

    private Integer id;
    private Integer userId;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String employeeId;
    private Specialization specialization;
    private Integer experienceYears;
    private Boolean onDuty;
    private Integer currentWorkload;
    private Integer maxCapacity;
    private BigDecimal rating;
    private UserStatus status;
    private LocalDateTime createdAt;
}
