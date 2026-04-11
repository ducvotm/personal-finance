package com.example.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Budget response data")
public class BudgetResponse {

    @Schema(description = "Budget ID")
    private Long id;

    @Schema(description = "Budget amount")
    private BigDecimal amount;

    @Schema(description = "Budget period start date")
    private LocalDate periodStart;

    @Schema(description = "Budget period end date")
    private LocalDate periodEnd;

    @Schema(description = "Period type (MONTHLY, WEEKLY, YEARLY)")
    private String periodType;

    @Schema(description = "Amount spent in this period")
    private BigDecimal spentAmount;

    @Schema(description = "Remaining amount (amount - spentAmount)")
    private BigDecimal remainingAmount;

    @Schema(description = "Percentage of budget used")
    private Double percentUsed;

    @Schema(description = "Whether budget is active")
    private Boolean isActive;

    @Schema(description = "Budget creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Category ID")
    private Long categoryId;

    @Schema(description = "Category name")
    private String categoryName;

    @Schema(description = "Category icon")
    private String categoryIcon;

    @Schema(description = "Category color")
    private String categoryColor;
}