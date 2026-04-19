package com.example.finance.ai.model;

import lombok.Builder;
import lombok.Value;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Value
@Builder
public class FinanceContext {
    LocalDate startDate;
    LocalDate endDate;
    BigDecimal totalIncome;
    BigDecimal totalExpense;
    BigDecimal netBalance;
    List<CategoryTotal> topExpenseCategories;
    List<BudgetSnapshot> budgets;

    YearMonth creatorAnalysisMonth;
    List<CreatorIncomeBySource> creatorIncomeBySource;
    BigDecimal creatorBaselineIncome;
    BigDecimal creatorVolatilityBuffer;
    BigDecimal creatorBudgetCommitments;
    BigDecimal creatorSafeToSpend;

    @Value
    @Builder
    public static class CategoryTotal {
        String categoryName;
        BigDecimal amount;
    }

    @Value
    @Builder
    public static class BudgetSnapshot {
        String categoryName;
        BigDecimal budgetAmount;
        BigDecimal spentAmount;
        BigDecimal remainingAmount;
        double percentUsed;
    }

    @Value
    @Builder
    public static class CreatorIncomeBySource {
        String source;
        BigDecimal amount;
    }
}
