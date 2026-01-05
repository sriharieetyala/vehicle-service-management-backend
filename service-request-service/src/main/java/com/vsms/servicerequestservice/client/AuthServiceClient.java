package com.vsms.servicerequestservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Feign client for Auth Service
 * Circuit breaker enabled via
 * spring.cloud.openfeign.circuitbreaker.enabled=true
 */
@FeignClient(name = "auth-service", fallback = AuthServiceClientFallback.class)
public interface AuthServiceClient {

    @PutMapping("/api/technicians/{id}/workload")
    void updateWorkload(@PathVariable Integer id, @RequestParam String action);

    /**
     * Get customer by ID - uses internal endpoint (no auth)
     * Returns: { success: true, data: { id, email, firstName, lastName, ... } }
     */
    @GetMapping("/internal/customers/{id}")
    Map<String, Object> getCustomerById(@PathVariable Integer id);

    /**
     * Get all managers - uses internal endpoint (no auth)
     * Returns: { success: true, data: [{ id, email, firstName, lastName, ... },
     * ...] }
     */
    @GetMapping("/internal/managers")
    Map<String, Object> getAllManagers();
}
