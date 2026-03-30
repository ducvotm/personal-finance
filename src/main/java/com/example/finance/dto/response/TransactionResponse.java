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
@Schema(description = "Transaction response data")
public class TransactionResponse {
    @Schema(description = "Transaction ID")
    private Long id;

    @Schema(description = "Transaction amount")
    private BigDecimal amount;

    @Schema(description = "Transaction type (INCOME or EXPENSE)")
    private String type;

    @Schema(description = "Date of transaction")
    private LocalDate transactionDate;

    @Schema(description = "Transaction description")
    private String description;

    @Schema(description = "Additional notes")
    private String note;

    @Schema(description = "Whether this is a recurring transaction")
    private Boolean isRecurring;

    @Schema(description = "Recurring frequency")
    private String recurringFrequency;

    @Schema(description = "Transaction creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Account ID")
    private Long accountId;

    @Schema(description = "Account name")
    private String accountName;

    @Schema(description = "Category ID")
    private Long categoryId;

    @Schema(description = "Category name")
    private String categoryName;
}
