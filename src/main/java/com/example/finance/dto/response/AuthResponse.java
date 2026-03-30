package com.example.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response containing tokens and user info")
public class AuthResponse {
    @Schema(description = "JWT access token")
    private String token;

    @Schema(description = "Refresh token for token renewal")
    private String refreshToken;

    @Schema(description = "Token type (always Bearer)")
    private String type;

    @Schema(description = "User ID")
    private Long userId;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "User email")
    private String email;

    public static AuthResponse of(String token, String refreshToken, Long userId, String username, String email) {
        return AuthResponse.builder().token(token).refreshToken(refreshToken).type("Bearer").userId(userId)
                .username(username).email(email).build();
    }
}
