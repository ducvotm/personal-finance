package com.example.finance.dto.response;

import com.example.finance.entity.IncomeSource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Creator income totals grouped by source")
public class IncomeSourceSummaryResponse {
    @Schema(description = "Creator income source")
    private IncomeSource source;

    @Schema(description = "Total income amount for the source")
    private BigDecimal total;
}
