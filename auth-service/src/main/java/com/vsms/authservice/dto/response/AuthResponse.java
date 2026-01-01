package com.vsms.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Integer userId;
    private String email;
    private String role;
    private String firstName;
    private String lastName;

    public static AuthResponse of(String accessToken, String refreshToken, Integer userId,
            String email, String role, String firstName, String lastName) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(userId)
                .email(email)
                .role(role)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }
}
