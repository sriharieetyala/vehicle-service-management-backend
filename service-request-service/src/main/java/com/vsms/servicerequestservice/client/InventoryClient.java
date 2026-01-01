package com.vsms.servicerequestservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    // Get total parts cost for a service request
    @GetMapping("/api/part-requests/service/{serviceRequestId}/total-cost")
    Map<String, Object> getPartsCostForService(@PathVariable Integer serviceRequestId);
}
