package com.example.finance.service;

import com.example.finance.dto.response.SafeToSpendResponse;
import com.example.finance.entity.Budget;
import com.example.finance.repository.AccountRepository;
import com.example.finance.repository.BudgetRepository;
import com.example.finance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SafeToSpendService {

    private static final int MONTH_WINDOW = 3;

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public SafeToSpendResponse calculateMonthlySafeToSpend(Long userId, YearMonth month) {
        List<BigDecimal> incomeHistory = loadMonthlyIncomeHistory(userId, month);
        BigDecimal baselineIncome = average(incomeHistory);
        BigDecimal volatilityBuffer = standardDeviation(incomeHistory);
        BigDecimal budgetCommitments = getBudgetCommitments(userId, month);
        BigDecimal availableBalance = accountRepository.getTotalBalanceByUserId(userId);

        BigDecimal projectedSafeToSpend = baselineIncome.subtract(volatilityBuffer).subtract(budgetCommitments);
        BigDecimal safeToSpend = projectedSafeToSpend.min(availableBalance).max(BigDecimal.ZERO);

        return SafeToSpendResponse.builder().month(month).baselineIncome(baselineIncome)
                .volatilityBuffer(volatilityBuffer).budgetCommitments(budgetCommitments).safeToSpend(safeToSpend)
                .build();
    }

    private List<BigDecimal> loadMonthlyIncomeHistory(Long userId, YearMonth targetMonth) {
        List<BigDecimal> monthlyIncome = new ArrayList<>();
        for (int offset = MONTH_WINDOW - 1; offset >= 0; offset--) {
            YearMonth month = targetMonth.minusMonths(offset);
            LocalDate startDate = month.atDay(1);
            LocalDate endDate = month.atEndOfMonth();
            BigDecimal totalIncome = transactionRepository.getTotalByUserIdAndTypeAndDateRange(userId, "INCOME",
                    startDate, endDate);
            monthlyIncome.add(totalIncome != null ? totalIncome : BigDecimal.ZERO);
        }
        return monthlyIncome;
    }

    private BigDecimal getBudgetCommitments(Long userId, YearMonth targetMonth) {
        LocalDate monthStart = targetMonth.atDay(1);
        LocalDate monthEnd = targetMonth.atEndOfMonth();
        List<Budget> activeBudgets = budgetRepository.findActiveBudgetsByUserAndDateRange(userId, monthStart, monthEnd);
        return activeBudgets.stream().map(Budget::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal standardDeviation(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal mean = average(values);
        BigDecimal squaredDifferenceSum = values.stream().map(value -> value.subtract(mean))
                .map(diff -> diff.multiply(diff)).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal variance = squaredDifferenceSum.divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue())).setScale(2, RoundingMode.HALF_UP);
    }
}
