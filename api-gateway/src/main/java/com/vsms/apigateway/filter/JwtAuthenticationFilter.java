package com.vsms.apigateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Map;

/**
 * API Gateway filter for JWT validation and role-based access control.
 * 
 * Security Flow:
 * 1. Check if endpoint is public (no auth needed)
 * 2. Validate JWT token
 * 3. Check if user's role is allowed for the requested path
 * 4. Add user info headers for downstream services
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String jwtSecret;

    // ROLE-PATH ACCESS CONTROL
    // Format: "METHOD:PATH_PREFIX" -> List of allowed roles
    // More specific paths are checked first in the matching logic

    private static final Map<String, List<String>> ROLE_ACCESS_MAP = Map.ofEntries(
            // AUTH-SERVICE
            // Auth endpoints - any authenticated
            Map.entry("GET:/api/auth/me", List.of("CUSTOMER", "TECHNICIAN", "MANAGER", "ADMIN")),
            Map.entry("POST:/api/auth/logout", List.of("CUSTOMER", "TECHNICIAN", "MANAGER", "ADMIN")),
            Map.entry("PUT:/api/auth/change-password", List.of("CUSTOMER", "TECHNICIAN", "MANAGER", "ADMIN")),

            // Customer endpoints
            Map.entry("GET:/api/customers", List.of("ADMIN")),
            Map.entry("GET:/api/customers/", List.of("CUSTOMER", "ADMIN")),
            Map.entry("PUT:/api/customers/", List.of("CUSTOMER", "ADMIN")),
            Map.entry("DELETE:/api/customers/", List.of("CUSTOMER", "ADMIN")),

            // Technician endpoints
            Map.entry("GET:/api/technicians/pending", List.of("ADMIN")),
            Map.entry("GET:/api/technicians/available", List.of("MANAGER", "ADMIN")),
            Map.entry("GET:/api/technicians/by-specialization", List.of("MANAGER", "ADMIN")),
            Map.entry("GET:/api/technicians", List.of("MANAGER", "ADMIN")),
            Map.entry("GET:/api/technicians/", List.of("TECHNICIAN", "MANAGER", "ADMIN")),
            Map.entry("PUT:/api/technicians/", List.of("TECHNICIAN", "MANAGER", "ADMIN")),
            Map.entry("DELETE:/api/technicians/", List.of("TECHNICIAN", "ADMIN")),

            // Manager endpoints
            Map.entry("POST:/api/managers", List.of("ADMIN")),
            Map.entry("GET:/api/managers", List.of("ADMIN")),
            Map.entry("PUT:/api/managers/", List.of("MANAGER", "ADMIN")),
            Map.entry("DELETE:/api/managers/", List.of("ADMIN")),

            // ========== VEHICLE-SERVICE ==========
            Map.entry("POST:/api/vehicles", List.of("CUSTOMER")),
            Map.entry("GET:/api/vehicles/customer/", List.of("CUSTOMER", "MANAGER", "ADMIN")),
            Map.entry("GET:/api/vehicles/", List.of("CUSTOMER", "MANAGER", "ADMIN")),
            Map.entry("PUT:/api/vehicles/", List.of("CUSTOMER")),
            Map.entry("DELETE:/api/vehicles/", List.of("CUSTOMER")),

            // ========== SERVICE-REQUEST-SERVICE ==========
            Map.entry("POST:/api/service-requests", List.of("CUSTOMER")),
            Map.entry("GET:/api/service-requests/customer/", List.of("CUSTOMER")),
            Map.entry("GET:/api/service-requests/vehicle/", List.of("CUSTOMER", "MANAGER")),
            Map.entry("GET:/api/service-requests/technician/", List.of("TECHNICIAN", "MANAGER")),
            Map.entry("GET:/api/service-requests/stats", List.of("MANAGER", "ADMIN")),
            Map.entry("GET:/api/service-requests/bays/available", List.of("MANAGER", "ADMIN")),
            Map.entry("GET:/api/service-requests/bays", List.of("MANAGER", "ADMIN")),
            Map.entry("GET:/api/service-requests", List.of("MANAGER", "ADMIN")),
            Map.entry("GET:/api/service-requests/", List.of("CUSTOMER", "TECHNICIAN", "MANAGER", "ADMIN")),
            // Specific PUT actions
            Map.entry("PUT:/api/service-requests/", List.of("CUSTOMER", "TECHNICIAN", "MANAGER", "ADMIN")),

            // Invoice endpoints
            Map.entry("POST:/api/invoices/generate/", List.of("MANAGER", "ADMIN")),
            Map.entry("GET:/api/invoices/my", List.of("CUSTOMER")),
            Map.entry("GET:/api/invoices/unpaid", List.of("MANAGER", "ADMIN")),
            Map.entry("GET:/api/invoices/stats", List.of("MANAGER", "ADMIN")),
            Map.entry("GET:/api/invoices", List.of("MANAGER", "ADMIN")),
            Map.entry("PUT:/api/invoices/", List.of("CUSTOMER")),

            // ========== INVENTORY-SERVICE ==========
            Map.entry("POST:/api/parts", List.of("MANAGER", "INVENTORY_MANAGER")),
            Map.entry("GET:/api/parts/low-stock", List.of("MANAGER", "INVENTORY_MANAGER")),
            Map.entry("GET:/api/parts", List.of("TECHNICIAN", "MANAGER", "INVENTORY_MANAGER")),
            Map.entry("GET:/api/parts/", List.of("TECHNICIAN", "MANAGER", "INVENTORY_MANAGER")),
            Map.entry("PUT:/api/parts/", List.of("MANAGER", "INVENTORY_MANAGER")),

            Map.entry("POST:/api/part-requests", List.of("TECHNICIAN")),
            Map.entry("GET:/api/part-requests/pending", List.of("MANAGER", "INVENTORY_MANAGER")),
            Map.entry("GET:/api/part-requests/technician/", List.of("TECHNICIAN")),
            Map.entry("PUT:/api/part-requests/", List.of("MANAGER", "INVENTORY_MANAGER")),
            Map.entry("GET:/api/part-requests", List.of("TECHNICIAN", "MANAGER", "INVENTORY_MANAGER")));

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();
        String method = request.getMethod().name();

        // 1. Allow public endpoints (no auth needed)
        if (isOpenEndpoint(path, method)) {
            return chain.filter(exchange);
        }

        // 2. Check for Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 3. Validate token
        String token = authHeader.substring(7);
        if (!validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Extract user info
        String email = getEmailFromToken(token);
        String role = getRoleFromToken(token);
        String userId = getUserIdFromToken(token);

        // 4. Check role-based access
        if (!isRoleAllowedForPath(role, method, path)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        // 5. Add user info headers for downstream services
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Email", email)
                .header("X-User-Role", role)
                .header("X-User-Id", userId)
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    /**
     * Check if the user's role is allowed to access the given method+path.
     */
    private boolean isRoleAllowedForPath(String role, String method, String path) {
        // ADMIN can access everything
        if ("ADMIN".equals(role)) {
            return true;
        }

        String key = method + ":" + path;

        // Check exact match first
        if (ROLE_ACCESS_MAP.containsKey(key)) {
            return ROLE_ACCESS_MAP.get(key).contains(role);
        }

        // Check prefix matches (for paths with IDs like /api/customers/123)
        // Sort entries by path length (longer first) for more specific matches
        for (Map.Entry<String, List<String>> entry : ROLE_ACCESS_MAP.entrySet()) {
            String mapKey = entry.getKey();
            String[] parts = mapKey.split(":", 2);
            String mapMethod = parts[0];
            String mapPath = parts[1];

            if (method.equals(mapMethod)) {
                // Check if the request path starts with the map path
                // e.g., path "/api/customers/123" matches mapPath "/api/customers/"
                if (mapPath.endsWith("/") && path.startsWith(mapPath)) {
                    return entry.getValue().contains(role);
                }
            }
        }

        // Deny by default (secure)
        return false;
    }

    private boolean isOpenEndpoint(String path, String method) {
        // POST /api/customers and /api/technicians are for registration
        if ((path.equals("/api/customers") || path.equals("/api/technicians")) && method.equals("POST")) {
            return true;
        }
        // Login is open
        if (path.equals("/api/auth/login")) {
            return true;
        }
        return false;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private String getEmailFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }

    private String getRoleFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
        return claims.get("role", String.class);
    }

    private String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
        Integer id = claims.get("userId", Integer.class);
        return id != null ? id.toString() : "";
    }

    @Override
    public int getOrder() {
        return -1; // Run first
    }
}
