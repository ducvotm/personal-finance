package com.example.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Account response data")
public class AccountResponse {
    @Schema(description = "Account ID")
    private Long id;

    @Schema(description = "Account name")
    private String name;

    @Schema(description = "Account type")
    private String type;

    @Schema(description = "Current balance")
    private BigDecimal balance;

    @Schema(description = "Bank account number")
    private String accountNumber;

    @Schema(description = "Currency code")
    private String currency;

    @Schema(description = "Account description")
    private String description;

    @Schema(description = "Whether the account is active")
    private Boolean isActive;

    @Schema(description = "Account creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
