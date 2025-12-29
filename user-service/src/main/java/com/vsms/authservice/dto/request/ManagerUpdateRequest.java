package com.vsms.authservice.dto.request;

import com.vsms.authservice.enums.Department;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerUpdateRequest {

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    private Department department; // Enum: SERVICE_BAY, PARTS, BILLING, CUSTOMER_SERVICE
}
