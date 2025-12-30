package com.vsms.vehicleservice.client;

import com.vsms.vehicleservice.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client to communicate with Auth Service
 * "auth-service" is the name registered in Eureka
 */
@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    @GetMapping("/api/customers/{id}")
    ApiResponse<?> getCustomerById(@PathVariable("id") Integer id);
}
