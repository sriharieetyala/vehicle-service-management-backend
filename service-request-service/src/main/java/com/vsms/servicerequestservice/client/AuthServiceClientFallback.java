package com.vsms.servicerequestservice.client;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * Fallback for AuthServiceClient - graceful degradation
 */
@Component
public class AuthServiceClientFallback implements AuthServiceClient {

    @Override
    public void updateWorkload(Integer id, String action) {
        System.err.println("Circuit Breaker: Auth service unavailable. Skipping workload update for technician " + id);
    }

    @Override
    public Map<String, Object> getCustomerById(Integer id) {
        System.err.println("Circuit Breaker: Auth service unavailable. Cannot fetch customer: " + id);
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> getAllManagers() {
        System.err.println("Circuit Breaker: Auth service unavailable. Cannot fetch managers.");
        return Collections.emptyMap();
    }
}
