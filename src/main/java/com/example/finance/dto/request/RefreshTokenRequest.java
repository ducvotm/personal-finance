package com.example.finance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Refresh token request payload")
public class RefreshTokenRequest {

    @Schema(description = "Valid refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    @Schema(description = "User ID associated with the refresh token", example = "1")
    @NotNull(message = "User ID is required")
    private Long userId;
}
