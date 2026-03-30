package com.example.finance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Account creation/update request payload")
public class AccountRequest {

    @Schema(description = "Account name", example = "My Savings Account")
    @NotBlank(message = "Account name is required")
    private String name;

    @Schema(description = "Account type", example = "SAVINGS")
    @NotBlank(message = "Account type is required")
    private String type;

    @Schema(description = "Initial balance", example = "1000.00")
    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.0", message = "Balance must be non-negative")
    private BigDecimal balance;

    @Schema(description = "Bank account number (optional)", example = "1234567890")
    private String accountNumber;

    @Schema(description = "Currency code", example = "USD")
    private String currency;

    @Schema(description = "Account description", example = "Primary savings account")
    private String description;
}
