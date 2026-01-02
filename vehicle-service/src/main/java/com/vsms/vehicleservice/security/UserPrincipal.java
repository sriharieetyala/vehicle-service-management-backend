package com.vsms.vehicleservice.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPrincipal {
    private Integer id;
    private String email;
    private String role;
}
