package com.vsms.authservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final HeaderAuthenticationFilter headerAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - registration and login
                        .requestMatchers(HttpMethod.POST, "/api/customers").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/technicians").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()

                        // Internal endpoints (Feign calls)
                        .requestMatchers("/api/technicians/*/workload").permitAll()

                        // Manager endpoints
                        .requestMatchers("/api/managers/**").hasAnyRole("ADMIN")

                        // Technician review - Admin only
                        .requestMatchers("/api/technicians/*/review").hasRole("ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated())
                // HeaderAuthenticationFilter runs FIRST - reads X-User-Role from gateway
                .addFilterBefore(headerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // JwtAuthenticationFilter runs after - for direct service access
                .addFilterAfter(jwtAuthenticationFilter, HeaderAuthenticationFilter.class);

        return http.build();
    }
}
