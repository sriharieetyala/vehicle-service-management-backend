package com.vsms.billingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "service-request-service")
public interface ServiceRequestClient {

    // Get service request details (for labor cost and customer ID)
    @GetMapping("/api/service-requests/{id}")
    Map<String, Object> getServiceRequest(@PathVariable Integer id);
}
