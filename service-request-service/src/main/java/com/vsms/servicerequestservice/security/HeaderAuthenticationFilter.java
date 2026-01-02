package com.vsms.servicerequestservice.security;

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
 * Reads user info from API Gateway headers and sets SecurityContext.
 * Needed for ownership checks in @PreAuthorize.
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

        if (role != null && email != null && !role.isEmpty() && !email.isEmpty()) {
            Integer id = null;
            if (userId != null && !userId.isEmpty()) {
                try {
                    id = Integer.parseInt(userId);
                } catch (NumberFormatException e) {
                    log.warn("Invalid userId header: {}", userId);
                }
            }

            UserPrincipal principal = new UserPrincipal(id, email, role);
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
