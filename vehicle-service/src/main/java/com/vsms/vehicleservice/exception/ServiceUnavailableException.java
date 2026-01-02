package com.vsms.vehicleservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a dependent service is unavailable
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String serviceName, String operation) {
        super(serviceName + " is unavailable. Cannot perform: " + operation);
    }
}
