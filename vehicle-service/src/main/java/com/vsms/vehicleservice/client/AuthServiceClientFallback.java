package com.vsms.vehicleservice.client;

import com.vsms.vehicleservice.dto.response.ApiResponse;
import com.vsms.vehicleservice.exception.ServiceUnavailableException;
import org.springframework.stereotype.Component;

/**
 * Fallback for AuthServiceClient when auth-service is unavailable
 */
@Component
public class AuthServiceClientFallback implements AuthServiceClient {

    @Override
    public ApiResponse<?> getCustomerById(Integer id) {
        System.err.println("Circuit Breaker: Auth service unavailable. Cannot verify customer " + id);
        throw new ServiceUnavailableException("Auth service is unavailable. Cannot verify customer.");
    }
}
