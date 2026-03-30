package com.example.finance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login request payload")
public class LoginRequest {

    @Schema(description = "Username for authentication", example = "johndoe")
    @NotBlank(message = "Username is required")
    private String username;

    @Schema(description = "Password for authentication", example = "password123")
    @NotBlank(message = "Password is required")
    private String password;
}
