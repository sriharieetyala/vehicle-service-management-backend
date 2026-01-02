package com.vsms.servicerequestservice.client;

import com.vsms.servicerequestservice.exception.ServiceUnavailableException;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Fallback for VehicleClient when vehicle-service is unavailable
 */
@Component
public class VehicleClientFallback implements VehicleClient {

    @Override
    public Map<String, Object> getVehicleById(Integer id) {
        System.err.println("Circuit Breaker: Vehicle service unavailable. Cannot get vehicle " + id);
        throw new ServiceUnavailableException("Vehicle service is unavailable.");
    }
}
