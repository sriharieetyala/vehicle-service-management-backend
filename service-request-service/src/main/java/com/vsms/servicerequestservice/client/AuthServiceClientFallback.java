package com.vsms.servicerequestservice.client;

import org.springframework.stereotype.Component;

/**
 * Fallback for AuthServiceClient - graceful degradation (skip workload update)
 */
@Component
public class AuthServiceClientFallback implements AuthServiceClient {

    @Override
    public void updateWorkload(Integer id, String action) {
        System.err.println("Circuit Breaker: Auth service unavailable. Skipping workload update for technician " + id);
        // Graceful degradation - just log and continue (non-critical operation)
    }
}
