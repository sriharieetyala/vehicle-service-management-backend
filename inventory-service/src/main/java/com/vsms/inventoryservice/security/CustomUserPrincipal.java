package com.vsms.inventoryservice.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.security.Principal;

@Getter
@AllArgsConstructor
public class CustomUserPrincipal implements Principal {
    private final Integer id;
    private final String email;
    private final String role;

    @Override
    public String getName() {
        return email;
    }
}
