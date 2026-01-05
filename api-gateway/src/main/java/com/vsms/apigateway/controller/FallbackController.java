package com.vsms.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Fallback controller for circuit breaker.
 * Returns 503 Service Unavailable when downstream services are down.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> fallbackGet() {
        return buildFallbackResponse();
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> fallbackPost() {
        return buildFallbackResponse();
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> fallbackPut() {
        return buildFallbackResponse();
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> fallbackDelete() {
        return buildFallbackResponse();
    }

    private ResponseEntity<Map<String, Object>> buildFallbackResponse() {
        Map<String, Object> response = Map.of(
                "success", false,
                "message", "Service temporarily unavailable. Please try again later.",
                "timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
