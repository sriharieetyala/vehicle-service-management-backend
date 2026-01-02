package com.vsms.servicerequestservice.client;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Fallback for InventoryClient - returns zero cost (graceful degradation)
 */
@Component
public class InventoryClientFallback implements InventoryClient {

    @Override
    public Map<String, Object> getPartsCostForService(Integer serviceRequestId) {
        System.err.println("Circuit Breaker: Inventory service unavailable. Returning zero cost for service request "
                + serviceRequestId);
        // Return fallback data - invoice can be updated later
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("totalCost", 0.0);
        fallback.put("fallback", true);
        return fallback;
    }
}
