package com.example.finance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String type;
    private Long userId;
    private String username;
    private String email;

    public static AuthResponse of(String token, String refreshToken, Long userId, String username, String email) {
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .type("Bearer")
                .userId(userId)
                .username(username)
                .email(email)
                .build();
    }
}
