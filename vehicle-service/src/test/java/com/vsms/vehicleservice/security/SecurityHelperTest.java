package com.vsms.vehicleservice.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class SecurityHelperTest {

    private SecurityHelper securityHelper;

    @BeforeEach
    void setUp() {
        securityHelper = new SecurityHelper();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserId_WithValidPrincipal_ReturnsId() {
        UserPrincipal principal = new UserPrincipal(42, "test@test.com", "CUSTOMER");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Integer userId = securityHelper.getCurrentUserId();

        assertEquals(42, userId);
    }

    @Test
    void getCurrentUserId_WithNullAuth_ReturnsNull() {
        SecurityContextHolder.clearContext();

        Integer userId = securityHelper.getCurrentUserId();

        assertNull(userId);
    }

    @Test
    void getCurrentUserId_WithNonUserPrincipal_ReturnsNull() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "stringPrincipal", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Integer userId = securityHelper.getCurrentUserId();

        assertNull(userId);
    }

    @Test
    void getCurrentUserRole_WithValidPrincipal_ReturnsRole() {
        UserPrincipal principal = new UserPrincipal(1, "manager@test.com", "MANAGER");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        String role = securityHelper.getCurrentUserRole();

        assertEquals("MANAGER", role);
    }

    @Test
    void getCurrentUserRole_WithNullAuth_ReturnsNull() {
        SecurityContextHolder.clearContext();

        String role = securityHelper.getCurrentUserRole();

        assertNull(role);
    }

    @Test
    void getCurrentUserRole_WithNonUserPrincipal_ReturnsNull() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "stringPrincipal", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        String role = securityHelper.getCurrentUserRole();

        assertNull(role);
    }

    @Test
    void isManagerOrAdmin_WithManager_ReturnsTrue() {
        UserPrincipal principal = new UserPrincipal(1, "mgr@test.com", "MANAGER");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertTrue(securityHelper.isManagerOrAdmin());
    }

    @Test
    void isManagerOrAdmin_WithAdmin_ReturnsTrue() {
        UserPrincipal principal = new UserPrincipal(1, "admin@test.com", "ADMIN");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertTrue(securityHelper.isManagerOrAdmin());
    }

    @Test
    void isManagerOrAdmin_WithCustomer_ReturnsFalse() {
        UserPrincipal principal = new UserPrincipal(1, "cust@test.com", "CUSTOMER");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertFalse(securityHelper.isManagerOrAdmin());
    }

    @Test
    void isManagerOrAdmin_WithNullRole_ReturnsFalse() {
        SecurityContextHolder.clearContext();

        assertFalse(securityHelper.isManagerOrAdmin());
    }

    @Test
    void isManagerOrAdmin_WithTechnician_ReturnsFalse() {
        UserPrincipal principal = new UserPrincipal(1, "tech@test.com", "TECHNICIAN");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertFalse(securityHelper.isManagerOrAdmin());
    }
}
