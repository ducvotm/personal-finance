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
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Budget creation/update request payload")
public class BudgetRequest {

    @Schema(description = "Budget amount", example = "500.00")
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Schema(description = "Budget period start date", example = "2024-01-01")
    @NotNull(message = "Period start date is required")
    private LocalDate periodStart;

    @Schema(description = "Budget period end date", example = "2024-01-31")
    @NotNull(message = "Period end date is required")
    private LocalDate periodEnd;

    @Schema(description = "Period type (MONTHLY, WEEKLY, YEARLY)", example = "MONTHLY")
    @NotBlank(message = "Period type is required")
    private String periodType;

    @Schema(description = "Category ID", example = "1")
    @NotNull(message = "Category ID is required")
    private Long categoryId;
}