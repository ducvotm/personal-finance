package com.example.finance.service;

import com.example.finance.ai.model.FinanceContext;
import com.example.finance.dto.response.BudgetResponse;
import com.example.finance.dto.response.IncomeSourceSummaryResponse;
import com.example.finance.dto.response.SafeToSpendResponse;
import com.example.finance.dto.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceContextBuilder {

    private final TransactionService transactionService;
    private final BudgetService budgetService;
    private final SafeToSpendService safeToSpendService;

    @Transactional(readOnly = true)
    public FinanceContext build(Long userId, LocalDate startDate, LocalDate endDate) {
        BigDecimal totalIncome = transactionService.getTotalIncome(userId, startDate, endDate);
        BigDecimal totalExpense = transactionService.getTotalExpense(userId, startDate, endDate);
        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        List<TransactionResponse> transactions = transactionService.getTransactionsByDateRange(userId, startDate,
                endDate);
        Map<String, BigDecimal> categoryTotals = transactions.stream()
                .filter(t -> "EXPENSE".equalsIgnoreCase(t.getType()))
                .collect(Collectors.groupingBy(TransactionResponse::getCategoryName,
                        Collectors.reducing(BigDecimal.ZERO, TransactionResponse::getAmount, BigDecimal::add)));

        List<FinanceContext.CategoryTotal> topCategories = categoryTotals.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue(Comparator.reverseOrder())).limit(3)
                .map(entry -> FinanceContext.CategoryTotal.builder().categoryName(entry.getKey())
                        .amount(entry.getValue()).build())
                .toList();

        List<FinanceContext.BudgetSnapshot> budgetSnapshots = budgetService
                .getBudgetsByDateRange(userId, startDate, endDate).stream().map(this::toBudgetSnapshot).toList();

        YearMonth creatorMonth = YearMonth.from(endDate);
        List<FinanceContext.CreatorIncomeBySource> creatorIncomeBySource = transactionService
                .getIncomeSummaryBySource(userId, creatorMonth).stream().map(this::toCreatorIncomeLine).toList();
        SafeToSpendResponse safeToSpend = safeToSpendService.calculateMonthlySafeToSpend(userId, creatorMonth);

        return FinanceContext.builder().startDate(startDate).endDate(endDate).totalIncome(totalIncome)
                .totalExpense(totalExpense).netBalance(netBalance).topExpenseCategories(topCategories)
                .budgets(budgetSnapshots).creatorAnalysisMonth(creatorMonth)
                .creatorIncomeBySource(creatorIncomeBySource).creatorBaselineIncome(safeToSpend.getBaselineIncome())
                .creatorVolatilityBuffer(safeToSpend.getVolatilityBuffer())
                .creatorBudgetCommitments(safeToSpend.getBudgetCommitments())
                .creatorSafeToSpend(safeToSpend.getSafeToSpend()).build();
    }

    private FinanceContext.CreatorIncomeBySource toCreatorIncomeLine(IncomeSourceSummaryResponse row) {
        return FinanceContext.CreatorIncomeBySource.builder().source(row.getSource().name()).amount(row.getTotal())
                .build();
    }

    private FinanceContext.BudgetSnapshot toBudgetSnapshot(BudgetResponse budget) {
        return FinanceContext.BudgetSnapshot.builder().categoryName(budget.getCategoryName())
                .budgetAmount(budget.getAmount()).spentAmount(budget.getSpentAmount())
                .remainingAmount(budget.getRemainingAmount()).percentUsed(budget.getPercentUsed()).build();
    }
}
