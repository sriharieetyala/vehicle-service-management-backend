package com.vsms.servicerequestservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Feign client for Vehicle Service
 * Circuit breaker enabled via
 * spring.cloud.openfeign.circuitbreaker.enabled=true
 */
@FeignClient(name = "vehicle-service", fallback = VehicleClientFallback.class)
public interface VehicleClient {

    @GetMapping("/api/vehicles/{id}")
    Map<String, Object> getVehicleById(@PathVariable Integer id);
}
