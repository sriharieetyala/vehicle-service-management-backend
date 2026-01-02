package com.vsms.authservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filter that reads user info from API Gateway headers.
 * When requests come through the gateway, it adds X-User-Role, X-User-Email,
 * X-User-Id headers.
 * This filter reads those headers and sets the Spring SecurityContext.
 */
@Slf4j
@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String role = request.getHeader("X-User-Role");
        String email = request.getHeader("X-User-Email");
        String userId = request.getHeader("X-User-Id");

        // If gateway headers are present, use them (request came through API Gateway)
        if (role != null && email != null && !role.isEmpty() && !email.isEmpty()) {
            Integer id = null;
            if (userId != null && !userId.isEmpty()) {
                try {
                    id = Integer.parseInt(userId);
                } catch (NumberFormatException e) {
                    log.warn("Invalid userId header: {}", userId);
                }
            }

            // Use CustomUserPrincipal for compatibility with @PreAuthorize expressions
            CustomUserPrincipal principal = new CustomUserPrincipal(id, email, role);
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Set security context from gateway headers: {} with role {}", email, role);
        }
        // If no gateway headers, the existing JwtAuthenticationFilter will handle it

        filterChain.doFilter(request, response);
    }
}
