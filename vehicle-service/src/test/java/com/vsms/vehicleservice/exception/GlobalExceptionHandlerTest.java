package com.vsms.vehicleservice.exception;

import com.vsms.vehicleservice.dto.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleResourceNotFound_ReturnsNotFoundStatus() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Vehicle", "id", 1);

        ResponseEntity<ApiResponse<Void>> response = handler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Vehicle"));
    }

    @Test
    void handleDuplicateResource_ReturnsConflictStatus() {
        DuplicateResourceException ex = new DuplicateResourceException("Vehicle", "plateNumber", "KA01AB1234");

        ResponseEntity<ApiResponse<Void>> response = handler.handleDuplicateResource(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("KA01AB1234"));
    }

    @Test
    void handleServiceUnavailable_ReturnsServiceUnavailableStatus() {
        ServiceUnavailableException ex = new ServiceUnavailableException("Auth service is down");

        ResponseEntity<ApiResponse<Void>> response = handler.handleServiceUnavailable(ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Auth service"));
    }

    @Test
    void handleValidationExceptions_ReturnsBadRequestWithFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("vehicle", "plateNumber", "Plate number is required");
        FieldError fieldError2 = new FieldError("vehicle", "brand", "Brand is required");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<ApiResponse<Map<String, String>>> response = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals("Plate number is required", response.getBody().getData().get("plateNumber"));
        assertEquals("Brand is required", response.getBody().getData().get("brand"));
    }

    @Test
    void handleGenericException_ReturnsInternalServerError() {
        Exception ex = new RuntimeException("Something went wrong");

        ResponseEntity<ApiResponse<Void>> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Internal server error"));
    }
}
