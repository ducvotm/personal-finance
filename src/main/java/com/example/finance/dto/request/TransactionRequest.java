package com.example.finance.dto.request;

import com.example.finance.entity.IncomeSource;
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
@Schema(description = "Transaction creation/update request payload")
public class TransactionRequest {

    @Schema(description = "Transaction amount", example = "50.00")
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Schema(description = "Transaction type (INCOME or EXPENSE)", example = "EXPENSE")
    @NotBlank(message = "Transaction type is required")
    private String type;

    @Schema(description = "Income source for creator income entries", example = "BRAND")
    private IncomeSource incomeSource;

    @Schema(description = "Date of transaction", example = "2024-01-15")
    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

    @Schema(description = "Transaction description", example = "Weekly groceries")
    private String description;

    @Schema(description = "Additional notes", example = "Bought food for the week")
    private String note;

    @Schema(description = "Whether this is a recurring transaction", example = "false")
    private Boolean isRecurring;

    @Schema(description = "Recurring frequency (if recurring)", example = "WEEKLY")
    private String recurringFrequency;

    @Schema(description = "Account ID", example = "1")
    @NotNull(message = "Account ID is required")
    private Long accountId;

    @Schema(description = "Category ID", example = "1")
    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
