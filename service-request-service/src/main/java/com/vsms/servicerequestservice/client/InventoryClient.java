package com.vsms.servicerequestservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Feign client for Inventory Service
 * Circuit breaker enabled via
 * spring.cloud.openfeign.circuitbreaker.enabled=true
 */
@FeignClient(name = "inventory-service", fallback = InventoryClientFallback.class)
public interface InventoryClient {

    @GetMapping("/api/part-requests/service/{serviceRequestId}/total-cost")
    Map<String, Object> getPartsCostForService(@PathVariable Integer serviceRequestId);
}
