package com.vsms.servicerequestservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@FeignClient(name = "billing-service")
public interface BillingClient {

    // Auto-generate invoice when service is completed
    @PostMapping("/api/invoices/generate/{serviceRequestId}")
    Map<String, Object> generateInvoice(@PathVariable Integer serviceRequestId);
}
