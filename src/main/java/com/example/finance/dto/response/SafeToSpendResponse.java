package com.example.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Monthly safe-to-spend calculation")
public class SafeToSpendResponse {
    @Schema(description = "Target month for the calculation")
    private YearMonth month;

    @Schema(description = "Average income baseline from recent months")
    private BigDecimal baselineIncome;

    @Schema(description = "Volatility buffer amount deducted from baseline")
    private BigDecimal volatilityBuffer;

    @Schema(description = "Active budget commitments in target month")
    private BigDecimal budgetCommitments;

    @Schema(description = "Final safe-to-spend amount")
    private BigDecimal safeToSpend;
}
