package com.vsms.vehicleservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Helper to get current user info from SecurityContext.
 */
@Component
public class SecurityHelper {

    /**
     * Get current authenticated user's ID from UserPrincipal.
     * Returns null if not authenticated or principal doesn't have ID.
     */
    public Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            return principal.getId();
        }
        return null;
    }

    /**
     * Get current authenticated user's role.
     */
    public String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            return principal.getRole();
        }
        return null;
    }

    /**
     * Check if current user is MANAGER or ADMIN.
     */
    public boolean isManagerOrAdmin() {
        String role = getCurrentUserRole();
        return "MANAGER".equals(role) || "ADMIN".equals(role);
    }
}
