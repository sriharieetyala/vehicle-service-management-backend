package com.vsms.billingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "service-request-service")
public interface ServiceRequestClient {

    // Get service request details (for labor cost and customer ID)
    @GetMapping("/api/service-requests/{id}")
    Map<String, Object> getServiceRequest(@PathVariable Integer id);

    // Update service request status (to CLOSED after payment)
    @PutMapping("/api/service-requests/{id}/status")
    Map<String, Object> updateStatus(@PathVariable Integer id, @RequestBody Map<String, String> body);
}
