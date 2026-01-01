package com.vsms.servicerequestservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "vehicle-service")
public interface VehicleClient {

    // Get vehicle by ID to validate it exists and get its customerId
    @GetMapping("/api/vehicles/{id}")
    Map<String, Object> getVehicleById(@PathVariable Integer id);
}
